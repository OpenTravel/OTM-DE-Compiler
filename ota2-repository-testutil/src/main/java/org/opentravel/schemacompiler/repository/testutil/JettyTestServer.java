/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.schemacompiler.repository.testutil;

import org.apache.jasper.compiler.TldCache;
import org.apache.jasper.runtime.JspFactoryImpl;
import org.apache.jasper.servlet.JspServlet;
import org.apache.jasper.servlet.TldScanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.SimpleInstanceManager;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.opentravel.repocommon.index.FreeTextSearchService;
import org.opentravel.repocommon.index.FreeTextSearchServiceFactory;
import org.opentravel.repocommon.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.jsp.JspFactory;

/**
 * Encapsulates the configuration and run-time environment of an OTA2.0 repository that is launched as an embedded Jetty
 * application to facilitate testing.
 * 
 * @author S. Livezey
 */
public class JettyTestServer {

    @SuppressWarnings("squid:S1075")
    private static final String CONTEXT_PATH = "/ota2-repository-service";
    @SuppressWarnings("squid:S1075")
    private static final String SEARCH_INDEX_PATH = "/search-index";
    private static final String REPOSITORY_SERVLET_CLASS = "org.opentravel.reposervice.repository.RepositoryServlet";
    private static final String AUTH_FILTER_CLASS = "org.opentravel.reposervice.repository.BasicAuthFilter";
    private static final String USER_DIR = "user.dir";

    private static Logger log = LogManager.getLogger( JettyTestServer.class );

    private Server jettyServer;
    private File repositorySnapshotLocation;
    private File repositoryRuntimeLocation;
    private File repositoryIndexLocation;
    private int port;

    /**
     * Constructor that specifies the configuration of the Jetty server to be used for testing.
     * 
     * @param port the server port to which HTTP requests will be directed on the local host
     * @param snapshotLocation the folder location that contains an initial snapshot of the OTA2.0 repository
     * @param testClass the test class that is starting the server instance
     * @param repositoryConfigFile location of the main OTA2 repository configuration file
     */
    public JettyTestServer(int port, File snapshotLocation, Class<?> testClass, File repositoryConfigFile) {
        this.repositorySnapshotLocation = snapshotLocation;
        this.repositoryRuntimeLocation = new File( System.getProperty( USER_DIR ),
            "/target/test-workspace/" + testClass.getSimpleName() + "/test-repository" );
        this.repositoryIndexLocation = new File( repositoryRuntimeLocation.getParentFile(), SEARCH_INDEX_PATH );
        this.port = port;

        if ((repositorySnapshotLocation != null) && !repositorySnapshotLocation.exists()) {
            throw new IllegalArgumentException(
                "Repository Snapshot Not Found: " + repositorySnapshotLocation.getAbsolutePath() );
        }
        if (!repositoryRuntimeLocation.exists() && !repositoryRuntimeLocation.mkdirs()) {
            throw new IllegalArgumentException(
                "Unable to create run-rime repository folder: " + repositoryRuntimeLocation.getAbsolutePath() );
        }

        System.setProperty( "test.class", testClass.getSimpleName() );
        System.setProperty( "ota2.repository.config", repositoryConfigFile.getAbsolutePath() );
        RepositoryComponentFactory.resetDefault();
    }

    /**
     * Initializes the run-time repository from the snapshot and launches the Jetty server.
     * 
     * @throws Exception thrown if the server cannot be started
     */
    public synchronized void start() throws Exception {
        start( false );
    }

    /**
     * Initializes the run-time repository from the snapshot and launches the Jetty server.
     * 
     * @param enableWebConsole flag indicating whether the repository web console should be enabled
     * @throws Exception thrown if the server cannot be started
     */
    @SuppressWarnings("unchecked")
    public synchronized void start(boolean enableWebConsole) throws Exception {
        if (jettyServer != null) {
            throw new IllegalStateException( "The Jetty server is already running." );
        }
        Class<? extends Servlet> servletClass = (Class<? extends Servlet>) Class.forName( REPOSITORY_SERVLET_CLASS );
        Constructor<? extends Servlet> servletConstructor = servletClass.getConstructor( ResourceConfig.class );
        ServletContextHandler context = new ServletContextHandler( ServletContextHandler.SESSIONS );
        ResourceConfig resourceConfig = new ResourceConfig();

        resourceConfig.packages( "org.opentravel.reposervice.repository", "org.opentravel.reposervice.providers" );

        context.setContextPath( CONTEXT_PATH );
        context.setResourceBase( System.getProperty( USER_DIR ) + "/src/main/webapp" );
        context.addServlet( new ServletHolder( servletConstructor.newInstance( resourceConfig ) ), "/service/*" );

        if (enableWebConsole) {
            configureConsoleSupport( context );
        }

        jettyServer = new Server( port );

        ErrorHandler errH = new ErrorHandler();
        errH.setShowStacks( true );

        jettyServer.setHandler( context );
        context.setErrorHandler( errH );

        initializeRuntimeRepository();
        jettyServer.start();

        initializeRepositoryServices();
        indexTestRepository();
    }

    /**
     * Configures the given servlet context to provide JSP processing services.
     * 
     * @param context the servlet context to be configured
     * @throws IOException thrown if there was a problem scanning for or loading a TLD
     * @throws SAXException thrown if there was a problem parsing a TLD
     * @throws ClassNotFoundException thrown if the required servlet filter class is not defined on the local classpath
     */
    @SuppressWarnings("unchecked")
    private void configureConsoleSupport(ServletContextHandler context)
        throws IOException, SAXException, ClassNotFoundException {
        File targetTemp = new File( System.getProperty( USER_DIR ), "target/jsp-temp" );
        // ServletHolder holderDefault = new ServletHolder( "default", new DefaultServlet() );
        ServletHolder holderJsp = new ServletHolder( "jsp", new JspServlet() );
        Class<? extends Filter> authFilterClass = (Class<? extends Filter>) Class.forName( AUTH_FILTER_CLASS );
        TldScanner scanner = new TldScanner( context.getServletContext(), true, false, true );
        DispatcherServlet mvcServlet = new DispatcherServlet();

        mvcServlet.setContextConfigLocation( "/WEB-INF/console-servlet.xml" );
        context.addServlet( new ServletHolder( "console", mvcServlet ), "/console/*" );
        context.addFilter( authFilterClass, "/console/*", EnumSet.of( DispatcherType.REQUEST ) );

        targetTemp.mkdirs();
        context.setAttribute( "javax.servlet.context.tempdir", targetTemp );
        context.setAttribute( JarScanner.class.getName(), new StandardJarScanner() );
        context.setAttribute( InstanceManager.class.getName(), new SimpleInstanceManager() );
        context.setClassLoader( new URLClassLoader( new URL[0], this.getClass().getClassLoader() ) );

        holderJsp.setInitOrder( 0 );
        // holderJsp.setInitParameter( "logVerbosityLevel", "DEBUG" );
        holderJsp.setInitParameter( "development", "true" );
        holderJsp.setInitParameter( "fork", "false" );
        holderJsp.setInitParameter( "xpoweredBy", "false" );
        holderJsp.setInitParameter( "compilerTargetVM", "1.8" );
        holderJsp.setInitParameter( "compilerSourceVM", "1.8" );
        holderJsp.setInitParameter( "keepgenerated", "true" );
        JspFactory.setDefaultFactory( new JspFactoryImpl() );

        // holderDefault.setInitParameter( "resourceBase", context.getResourceBase() );
        // holderDefault.setInitParameter( "dirAllowed", "true" );

        // context.addServlet( holderDefault, "/" );
        context.addServlet( holderJsp, "*.jsp" );

        scanner.scan();
        context.setAttribute( TldCache.SERVLET_CONTEXT_ATTRIBUTE_NAME, new TldCache( context.getServletContext(),
            scanner.getUriTldResourcePathMap(), scanner.getTldResourcePathTaglibXmlMap() ) );
    }

    /**
     * Adds this test server instance to the given repository manager.
     * 
     * @param manager the repository manager instance to configure
     * @return RemoteRepository
     * @throws RepositoryException thrown if the configuration settings cannot be modified
     */
    public RemoteRepository configureRepositoryManager(RepositoryManager manager) throws RepositoryException {
        RemoteRepository testRepository = (RemoteRepository) manager.getRepository( "test-repository" );

        if (testRepository == null) {
            testRepository = manager.addRemoteRepository( getBaseRepositoryUrl() );
        }
        return testRepository;
    }

    /**
     * Returns the base URL of the OTM repository deployed to the Jetty server.
     * 
     * @return String
     */
    public String getBaseRepositoryUrl() {
        return "http://localhost:" + port + CONTEXT_PATH;
    }

    /**
     * Returns a repository URL using the relative path provided.
     * 
     * @param urlPath the relative URL path from the base
     * @return String
     */
    @SuppressWarnings("squid:S1075")
    public String getRepositoryUrl(String urlPath) {
        if (urlPath == null) {
            urlPath = "";
        } else if (!urlPath.startsWith( "/" )) {
            urlPath = "/" + urlPath;
        }
        return getBaseRepositoryUrl() + urlPath;
    }

    /**
     * Shuts down the Jetty server.
     * 
     * @throws Exception thrown if the server cannot be shut down
     */
    public synchronized void stop() throws Exception {
        if (jettyServer == null) {
            throw new IllegalStateException( "The Jetty server is not running." );
        }
        jettyServer.stop();
        jettyServer = null;
    }

    /**
     * Pings the Jetty service with a meta-data request that forces the initialization of the repository web service.
     * 
     * @throws RepositoryException thrown if the remote repository is not available
     */
    private void initializeRepositoryServices() throws RepositoryException {
        new RemoteRepositoryUtils().getRepositoryMetadata( "http://localhost:" + port + CONTEXT_PATH );
    }

    /**
     * Indexes the contents of the server's test repository.
     * 
     * @throws IOException thrown if the indexing service cannot be initialized
     * @throws RepositoryException thrown if an error occurs during repository indexing
     */
    private void indexTestRepository() throws IOException, RepositoryException {
        FreeTextSearchServiceFactory
            .initializeSingleton( RepositoryComponentFactory.getDefault().getRepositoryManager() );
        FreeTextSearchService service = FreeTextSearchServiceFactory.getInstance();

        while (!service.isRunning()) {
            try {
                log.info( "Waiting for Indexing Service startup..." );
                Thread.sleep( 100 );

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        service.indexAllRepositoryItems();
    }

    /**
     * Initializes the run-time OTA2.0 repository by deleting any existing files and copying all of the files in the
     * snapshot folder location.
     * 
     * @throws IOException thrown if the contents of the repository cannot be initialized
     */
    private void initializeRuntimeRepository() throws IOException {
        if (repositorySnapshotLocation != null) {
            RepositoryTestUtils.deleteContents( repositoryRuntimeLocation );
            RepositoryTestUtils.copyContents( repositorySnapshotLocation, repositoryRuntimeLocation );
        }
        RepositoryTestUtils.deleteContents( repositoryIndexLocation );
    }

}

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

package org.opentravel.schemacompiler.repository;

import org.opentravel.schemacompiler.index.FreeTextSearchServiceFactory;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.util.RepositoryTestUtils;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import java.io.File;
import java.util.List;

/**
 * Abstract base class that defines common methods used during live repository testing.
 * 
 * @author S. Livezey
 */
public abstract class RepositoryTestBase {

    protected static final boolean DEBUG = true;

    protected static ThreadLocal<RepositoryManager> repositoryManager = new ThreadLocal<>();
    protected static ThreadLocal<Repository> testRepository = new ThreadLocal<>();
    protected static ThreadLocal<JettyTestServer> jettyServer = new ThreadLocal<>();
    protected static ThreadLocal<LdapTestServer> ldapServer = new ThreadLocal<>();
    protected static ThreadLocal<GreenMail> smtpServer = new ThreadLocal<>();
    protected static ThreadLocal<File> wipFolder = new ThreadLocal<>();
    protected static ThreadLocal<TLModel> model = new ThreadLocal<>();

    protected static File defaultRepositoryConfig =
        new File( System.getProperty( "user.dir" ), "/target/test-classes/ota2-repository-config.xml" );
    protected static File ldapLookupRepositoryConfig =
        new File( System.getProperty( "user.dir" ), "/target/test-classes/ota2-repository-config-ldaplookup.xml" );
    protected static File ldapSearchRepositoryConfig =
        new File( System.getProperty( "user.dir" ), "/target/test-classes/ota2-repository-config-ldapsearch.xml" );
    protected static File jmsIndexRepositoryConfig =
        new File( System.getProperty( "user.dir" ), "/target/test-classes/ota2-repository-config-jmsindex.xml" );
    protected static File svnRepositoryConfig =
        new File( System.getProperty( "user.dir" ), "/target/test-classes/ota2-repository-config-svn.xml" );

    protected static void setupWorkInProcessArea(Class<?> testClass) throws Exception {
        File wipSnapshot = new File( System.getProperty( "user.dir" ), "/src/test/resources/test-data" );
        wipFolder.set( new File( System.getProperty( "user.dir" ),
            "/target/test-workspace/" + testClass.getSimpleName() + "/wip" ) );

        if (wipFolder.get().exists()) {
            RepositoryTestUtils.deleteContents( wipFolder.get() );
        }
        RepositoryTestUtils.copyContents( wipSnapshot, wipFolder.get() );
    }

    protected synchronized static void startTestServer(String repositorySnapshotFolder, int port, Class<?> testClass)
        throws Exception {
        startTestServer( repositorySnapshotFolder, port, defaultRepositoryConfig, true, false, testClass );
    }

    protected synchronized static void startTestServer(String repositorySnapshotFolder, int port, File repositoryConfig,
        boolean enableRealTimeIndexing, boolean enableWebConsole, Class<?> testClass) throws Exception {
        File localRepository = new File( System.getProperty( "user.dir" ),
            "/target/test-workspace/" + testClass.getSimpleName() + "/local-repository" );
        File snapshotBase = new File( System.getProperty( "user.dir" ), "/src/test/resources/repo-snapshots" );
        File repositorySnapshot = new File( snapshotBase, repositorySnapshotFolder );

        if (localRepository.exists()) {
            RepositoryTestUtils.deleteContents( localRepository );
        }
        localRepository.mkdirs();
        FreeTextSearchServiceFactory.setRealTimeIndexing( enableRealTimeIndexing );

        repositoryManager.set( new RepositoryManager( localRepository ) );

        jettyServer.set( new JettyTestServer( port, repositorySnapshot, testClass, repositoryConfig ) );
        jettyServer.get().start( enableWebConsole );

        testRepository.set( jettyServer.get().configureRepositoryManager( repositoryManager.get() ) );
        repositoryManager.get().setCredentials( testRepository.get(), "testuser", "password" );
    }

    protected static void shutdownTestServer() throws Exception {
        jettyServer.get().stop();
    }

    /**
     * Configures and startes an LDAP test server using the information provided.
     * 
     * @param port the port number where the LDAP server will listen for requests
     * @param ldifFilePath the path of the LDIF file to use when populating the directory (relative to
     *        /src/test/resources)
     * @throws Exception thrown if the directory server cannot be created or started
     */
    protected static void startLdapTestServer(int port, String ldifFilePath) throws Exception {
        LdapTestServer testServer = new LdapTestServer( port, ldifFilePath );

        ldapServer.set( testServer );
        testServer.start();
    }

    /**
     * Shuts down the LDAP test server on the current thread.
     * 
     * @throws Exception thrown if the directory server cannot be shut down
     */
    protected static void stopLdapTestServer() throws Exception {
        LdapTestServer testServer = ldapServer.get();

        testServer.stop();
        ldapServer.set( null );
    }

    /**
     * Starts an SMTP test server on the specified localhost port.
     * 
     * @param port the port to use for processing SMTP requests
     * @throws Exception thrown if the SMTP server cannot be created or started
     */
    protected static void startSmtpTestServer(int port) throws Exception {
        GreenMail greenMail = new GreenMail( new ServerSetup( port, null, ServerSetup.PROTOCOL_SMTP ) );

        smtpServer.set( greenMail );
        greenMail.setUser( "testuser", "password" );
        greenMail.start();
        System.setProperty( "smtp.port", port + "" );
    }

    /**
     * Shuts down the SMTP test server on the current thread.
     * 
     * @throws Exception thrown if the SMTP server cannot be shut down
     */
    protected static void stopSmtpTestServer() throws Exception {
        GreenMail greenMail = smtpServer.get();

        greenMail.stop();
        smtpServer.set( null );
    }

    protected ProjectItem findProjectItem(Project project, String filename) {
        ProjectItem result = null;

        for (ProjectItem item : project.getProjectItems()) {
            if (item.getFilename().equals( filename )) {
                result = item;
                break;
            }
        }
        return result;
    }

    protected RepositoryItem findRepositoryItem(List<RepositoryItem> itemList, String filename) {
        RepositoryItem result = null;

        for (RepositoryItem item : itemList) {
            if (item.getFilename().equals( filename )) {
                result = item;
                break;
            }
        }
        return result;
    }

}

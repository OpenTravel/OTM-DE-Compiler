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

import org.opentravel.schemacompiler.index.FreeTextSearchServiceFactory;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.unboundid.ldap.sdk.LDAPException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Base class for tests that utilize a remote OTM repository.
 */
public abstract class AbstractRepositoryTest {

    private static final String DEFAULT_REPO_SNAPSHOTS_LOCATION = "/src/test/resources/repo-snapshots";
    private static final String DEFAULT_WIP_SNAPSHOTS_LOCATION = "/src/test/resources/test-data";
    private static final String TEST_WORKSPACE_LOCATION = "/target/test-workspace/";
    private static final String USER_DIR = "user.dir";

    private static final String TESTUSER_ID = "testuser";
    private static final String TESTUSER_CREDENTIAL = "password";

    protected static ThreadLocal<RepositoryManager> repositoryManager = new ThreadLocal<>();
    protected static ThreadLocal<Repository> testRepository = new ThreadLocal<>();
    protected static ThreadLocal<JettyTestServer> jettyServer = new ThreadLocal<>();
    protected static ThreadLocal<LdapTestServer> ldapServer = new ThreadLocal<>();
    protected static ThreadLocal<GreenMail> smtpServer = new ThreadLocal<>();
    protected static ThreadLocal<File> wipFolder = new ThreadLocal<>(); // Provides access to local test files
    protected static ThreadLocal<TLModel> model = new ThreadLocal<>();

    /**
     * Initializes the work-in-process area for the given test class. Any content that exists in the WIP directory will
     * be deleted and re-initialized with files from the tests source snapshot location.
     * 
     * @param testClass the test class for which to initialize a WIP location
     * @throws IOException thrown if the WIP location cannot be initialized
     */
    protected static void setupWorkInProcessArea(Class<?> testClass) throws IOException {
        setupWorkInProcessArea( testClass, DEFAULT_WIP_SNAPSHOTS_LOCATION );
    }

    /**
     * Initializes the work-in-process area for the given test class. Any content that exists in the WIP directory will
     * be deleted and re-initialized with files from the tests source snapshot location.
     * 
     * @param testClass the test class for which to initialize a WIP location
     * @param wipSnapshotsLocation the base folder location where WIP snapshot source files are stored
     * @throws IOException thrown if the WIP location cannot be initialized
     */
    protected static void setupWorkInProcessArea(Class<?> testClass, String wipSnapshotsLocation) throws IOException {
        File wipSnapshot = new File( System.getProperty( USER_DIR ), wipSnapshotsLocation );

        wipFolder.set(
            new File( System.getProperty( USER_DIR ), TEST_WORKSPACE_LOCATION + testClass.getSimpleName() + "/wip" ) );

        if (wipFolder.get().exists()) {
            RepositoryTestUtils.deleteContents( wipFolder.get() );
        }
        RepositoryTestUtils.copyContents( wipSnapshot, wipFolder.get() );
    }

    /**
     * Initializes a local repository for use by the specified test class.
     * 
     * @param repositorySnapshotFolder the relative path location of the snapshot source folder
     * @param testClass the test class for which to initialize the repository
     * @throws RepositoryException thrown if the local repository manager cannot be created
     * @throws IOException thrown if the repository content cannot be initialized
     */
    protected static void setupLocalRepository(String repositorySnapshotFolder, Class<?> testClass)
        throws RepositoryException, IOException {
        setupLocalRepository( repositorySnapshotFolder, testClass, DEFAULT_REPO_SNAPSHOTS_LOCATION );
    }

    /**
     * Initializes a local repository for use by the specified test class.
     * 
     * @param repositorySnapshotFolder the relative path location of the snapshot source folder
     * @param testClass the test class for which to initialize the repository
     * @param repoSnapshotsLocation the base folder for all snapshot source templates
     * @throws RepositoryException thrown if the local repository manager cannot be created
     * @throws IOException thrown if the repository content cannot be initialized
     */
    protected static void setupLocalRepository(String repositorySnapshotFolder, Class<?> testClass,
        String repoSnapshotsLocation) throws RepositoryException, IOException {
        File localRepository = new File( System.getProperty( USER_DIR ),
            TEST_WORKSPACE_LOCATION + testClass.getSimpleName() + "/local-repository" );
        File snapshotBase = new File( System.getProperty( USER_DIR ), repoSnapshotsLocation );
        File repositorySnapshot = new File( snapshotBase, repositorySnapshotFolder );

        if (localRepository.exists()) {
            RepositoryTestUtils.deleteContents( localRepository );
        }
        localRepository.mkdirs();
        RepositoryTestUtils.copyContents( repositorySnapshot, localRepository );

        RepositoryManager rm = new RepositoryManager( localRepository );

        testRepository.set( rm );
        repositoryManager.set( rm );
    }

    /**
     * Initializes the remote repository and launches a Jetty server that can be used to access and manage its content.
     * 
     * @param repositorySnapshotFolder the relative path location of the snapshot source folder
     * @param port the port on which the Jetty web service will run
     * @param repositoryConfig the location of the repository's configuration settings file
     * @param enableRealTimeIndexing flag indicating whether real-time indexing should be enabled for the repository
     *        server
     * @param enableWebConsole flag indicating whether the web console should be enabled for the repository server
     * @param testClass the test class for which to initialize the repository
     * @throws RepositoryException thrown if the repository content cannot be initialized or the server cannot be
     *         launched
     */
    protected static synchronized void startTestServer(String repositorySnapshotFolder, int port, File repositoryConfig,
        boolean enableRealTimeIndexing, boolean enableWebConsole, Class<?> testClass) throws RepositoryException {
        startTestServer( repositorySnapshotFolder, port, repositoryConfig, enableRealTimeIndexing, enableWebConsole,
            testClass, DEFAULT_REPO_SNAPSHOTS_LOCATION );
    }

    /**
     * Initializes the remote repository and launches a Jetty server that can be used to access and manage its content.
     * 
     * @param repositorySnapshotFolder the relative path location of the snapshot source folder
     * @param port the port on which the Jetty web service will run
     * @param repositoryConfig the location of the repository's configuration settings file
     * @param enableRealTimeIndexing flag indicating whether real-time indexing should be enabled for the repository
     *        server
     * @param enableWebConsole flag indicating whether the web console should be enabled for the repository server
     * @param testClass the test class for which to initialize the repository
     * @param repoSnapshotsLocation the base folder for all snapshot source templates
     * @throws RepositoryException thrown if the repository content cannot be initialized or the server cannot be
     *         launched
     */
    protected static synchronized void startTestServer(String repositorySnapshotFolder, int port, File repositoryConfig,
        boolean enableRealTimeIndexing, boolean enableWebConsole, Class<?> testClass, String repoSnapshotsLocation)
        throws RepositoryException {
        File repositoryLocation = new File( System.getProperty( USER_DIR ),
            TEST_WORKSPACE_LOCATION + testClass.getSimpleName() + "/server-repository" );
        File snapshotBase = new File( System.getProperty( USER_DIR ), repoSnapshotsLocation );
        File repositorySnapshot = new File( snapshotBase, repositorySnapshotFolder );

        if (repositoryLocation.exists()) {
            RepositoryTestUtils.deleteContents( repositoryLocation );
        }
        repositoryLocation.mkdirs();

        FreeTextSearchServiceFactory.setRealTimeIndexing( enableRealTimeIndexing );
        repositoryManager.set( new RepositoryManager( repositoryLocation ) );

        try {
            jettyServer.set( new JettyTestServer( port, repositorySnapshot, testClass, repositoryConfig ) );
            jettyServer.get().start( enableWebConsole );

        } catch (Exception e) {
            throw new RepositoryException( "Error starting Jetty test server", e );
        }

        testRepository.set( jettyServer.get().configureRepositoryManager( repositoryManager.get() ) );
        repositoryManager.get().setCredentials( testRepository.get(), TESTUSER_ID, TESTUSER_CREDENTIAL );
    }

    /**
     * Shuts down the Jetty test server for the OTM repository.
     * 
     * @throws RepositoryException thrown if an error occurs during server shutdown
     */
    protected static void shutdownTestServer() throws RepositoryException {
        try {
            jettyServer.get().stop();

        } catch (Exception e) {
            throw new RepositoryException( "Error shutting down Jetty test server", e );
        }
    }

    /**
     * Configures and startes an LDAP test server using the information provided.
     * 
     * @param port the port number where the LDAP server will listen for requests
     * @param ldifFilePath the path of the LDIF file to use when populating the directory (relative to
     *        /src/test/resources)
     * @throws LDAPException thrown if the directory server cannot be created or started
     * @throws IOException thrown if the directory server cannot be initialized
     */
    protected static void startLdapTestServer(int port, String ldifFilePath) throws LDAPException, IOException {
        LdapTestServer testServer = new LdapTestServer( port, ldifFilePath );

        ldapServer.set( testServer );
        testServer.start();
    }

    /**
     * Shuts down the LDAP test server on the current thread.
     * 
     * @throws LDAPException thrown if the directory server cannot be shut down
     */
    protected static void stopLdapTestServer() throws LDAPException {
        LdapTestServer testServer = ldapServer.get();

        testServer.stop();
        ldapServer.set( null );
    }

    /**
     * Starts an SMTP test server on the specified localhost port.
     * 
     * @param port the port to use for processing SMTP requests
     */
    protected static void startSmtpTestServer(int port) {
        GreenMail greenMail = new GreenMail( new ServerSetup( port, null, ServerSetup.PROTOCOL_SMTP ) );

        smtpServer.set( greenMail );
        greenMail.setUser( TESTUSER_ID, TESTUSER_CREDENTIAL );
        greenMail.start();
        System.setProperty( "smtp.port", port + "" );
    }

    /**
     * Shuts down the SMTP test server on the current thread.
     */
    protected static void stopSmtpTestServer() {
        GreenMail greenMail = smtpServer.get();

        greenMail.stop();
        smtpServer.set( null );
    }

    /**
     * Returns the project item with the specified filename.
     * 
     * @param project the project from which to return a project item
     * @param filename the filename of the project item to find
     * @return ProjectItem
     */
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

    /**
     * Returns the repository item with the specified filename from the list provided.
     * 
     * @param itemList the list of repository items
     * @param filename the filename of the repository item to find
     * @return RepositoryItem
     */
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

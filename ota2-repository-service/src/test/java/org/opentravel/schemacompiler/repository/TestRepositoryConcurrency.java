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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.RepositoryUsers;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.UserInfo;
import org.opentravel.schemacompiler.repository.impl.ProjectFileUtils;
import org.opentravel.schemacompiler.svn.SvnserveProcess;
import org.opentravel.schemacompiler.svn.TestOptions;
import org.opentravel.schemacompiler.util.RepositoryTestUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;

/**
 * Verifies the operation of the OTM repository when running SVN persistence
 * while being accessed by multiple users for write operations.
 */
public class TestRepositoryConcurrency {
	
    private static final String USERFILE_SCHEMA_CONTEXT =
    		":org.w3._2001.xmlschema:org.opentravel.ns.ota2.repositoryinfoext_v01_00";
    private static final JAXBContext jaxbContext;
	
	private final File svnRepositoryFolder = new File( System.getProperty("user.dir"),
			"/target/test-workspace/" + this.getClass().getSimpleName() + "/svn-repository" );
	private final File svnConfigFolder = new File( svnRepositoryFolder, "/conf" );
	private final File repositorySnapshotFolder = new File( System.getProperty("user.dir"),
			"/src/test/resources/repo-snapshots/concurrency-repository" );
	private final File testRepositoryFolder = new File( System.getProperty("user.dir"),
			"/target/test-workspace/" + this.getClass().getSimpleName() + "/test-repository" );
	private final String repositoryUuid = UUID.randomUUID().toString();
	
	// Server-side processes and components
	private SvnserveProcess svnServerProcess;
	private JettyTestServer jettyServer;
	private SVNAdminClient adminClient;
	private SVNClientManager svnClient;
	
	// Client-side user processes
    protected static ThreadLocal<RepositoryManager> repositoryManager = new ThreadLocal<RepositoryManager>();
    protected static ThreadLocal<Repository> testRepository = new ThreadLocal<Repository>();
    protected static ThreadLocal<File> wipFolder = new ThreadLocal<File>();
    
	@BeforeClass
	public static void checkSvnInstalled() throws Exception {
		assumeTrue( Boolean.parseBoolean( System.getProperty("svnInstalled", "false") ) );
	}
	
	@Before
	public void setupOtmRepository() throws Exception {
		adminClient = new SVNAdminClient( new BasicAuthenticationManager( "admin", "admin" ),
				SVNWCUtil.createDefaultOptions( svnConfigFolder, true ) );
		svnClient = SVNClientManager.newInstance(
                SVNWCUtil.createDefaultOptions( svnConfigFolder, true ), "admin", "admin" );
		SVNCommitClient commitClient = svnClient.getCommitClient();
		SVNURL repositoryUrl;
		
		// Clean up from the last test run
        RepositoryTestUtils.deleteContents( svnRepositoryFolder );
        RepositoryTestUtils.deleteContents( testRepositoryFolder );
        
		// Create the SVN repository and start the server daemon process
		repositoryUrl = adminClient.doCreateRepository( svnRepositoryFolder, repositoryUuid, false, true );
		svnServerProcess = SvnserveProcess.run( TestOptions.getDefaults( repositoryUrl ), svnRepositoryFolder );
		
		// Import the initial content into the SVN repository and check it out to a working
		// folder location
		commitClient.doImport( repositorySnapshotFolder, repositoryUrl, "Initial commit",
				SVNProperties.wrap( Collections.emptyMap() ), true, true, SVNDepth.INFINITY );
		svnClient.getUpdateClient().doCheckout( svnServerProcess.getUrl(), testRepositoryFolder,
				SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true );
		System.out.println("SVN Repository Running At: " + svnServerProcess.getUrl().toString());
		
		// Configure and start the Jetty test server for the OTM repository
		System.setProperty("svn.configFolder", System.getProperty("user.home") + "/.subversion");
		System.setProperty("svn.credentialsFile", System.getProperty("user.dir") +
				"/src/test/resources/svn-config/svnCredentials.properties");
        System.setProperty("ota2.repository.realTimeIndexing", "true");
        
        jettyServer = new JettyTestServer(9400, null, this.getClass(),
        		RepositoryTestBase.svnRepositoryConfig);
        jettyServer.start();
		System.out.println("OTA2 Repository Running...");
	}
	
	@After
	public void shutdownSvnRepository() throws Exception {
		System.out.println("Shutting Down...");
        if (jettyServer != null) jettyServer.stop();
        if (svnServerProcess != null) svnServerProcess.shutdown();
	}
	
	/** Setup local repository, test remote repository, and WIP folder for user in current thread. */
	protected void setupUserEnvironment(String userId) throws Exception {
		File localRepositoryFolder = new File( System.getProperty("user.dir"),
				"/target/test-workspace/" + this.getClass().getSimpleName() +
				"/local-repositories/" + userId );
		File userWipFolder = new File( System.getProperty("user.dir"),
				"/target/test-workspace/" + this.getClass().getSimpleName() +
				"/wip/" + userId );
		
        RepositoryTestUtils.deleteContents(localRepositoryFolder);
        RepositoryTestUtils.deleteContents(userWipFolder);
        localRepositoryFolder.mkdirs();
        userWipFolder.mkdirs();
        
        repositoryManager.set(new RepositoryManager(localRepositoryFolder));
        testRepository.set(jettyServer.configureRepositoryManager(repositoryManager.get()));
        repositoryManager.get().setCredentials(testRepository.get(), userId, "password");
        wipFolder.set(userWipFolder);
	}
	
	@Test
	public void testSvnConcurrency() throws Exception {
		List<String> testUserIds = loadRepositoryUsers();
		ExecutorService executor = Executors.newFixedThreadPool( testUserIds.size() );
		List<Future<Boolean>> taskResults = new ArrayList<>();
		boolean allSuccessful = true;
		
		// Start concurrent processes for each test user in the repository
		try {
			System.out.print("Launching concurrent processes for " + testUserIds.size() +" user(s)");
			for (String userId : testUserIds) {
				final String _userId = userId;
				Callable<Boolean> userTask = new Callable<Boolean>() {
					public Boolean call() throws Exception {
						boolean success = false;
						try {
							setupUserEnvironment( _userId );
							performUserAction( _userId );
							success = true;
							
						} catch (Throwable t) {
							t.printStackTrace( System.out );
						}
						return success;
					}
				};
				
				taskResults.add( executor.submit( userTask ) );
			}
			
			for (Future<Boolean> taskResult  : taskResults) {
				allSuccessful &= taskResult.get();
			}
			System.out.println();
			assertTrue( "One or more background processes encountered errrors.", allSuccessful );
			
		} finally {
			executor.shutdownNow();
		}
		
		// Verify that no uncommitted SVN changes exist in the test repository
		final List<File> uncommittedFiles = new ArrayList<>();
		ISVNStatusHandler handler = new  ISVNStatusHandler() {
			public void handleStatus(SVNStatus status) throws SVNException {
				uncommittedFiles.add( status.getFile() );
			}
		};
		svnClient.getStatusClient().doStatus( testRepositoryFolder, SVNRevision.HEAD,
				SVNDepth.INFINITY, true, false, true, false, handler, new ArrayList<String>() );
		
		if (!uncommittedFiles.isEmpty()) {
			System.out.println("Uncommitted SVN Files:");
			
			for (File file : uncommittedFiles) {
				System.out.println("  " + file.getName());
			}
			fail("Uncommitted files exist in the SVN repository.");
		}
	}
	
	/** Called for each concurrent user that is running within a separate executor thread. */
	protected void performUserAction(String userId) throws Exception {
		RepositoryItem item = testRepository.get().getRepositoryItem(
				"http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/concurrency-test",
				"Library_" + userId + "_1_0_0.otm", "1.0.0" );
        File repositoryFile = URLUtils.toFile( repositoryManager.get().getContentLocation( item ) );
        File wipFile = repositoryManager.get().getFileManager().getLibraryWIPContentLocation(
                item.getBaseNamespace(), item.getFilename());
		
		repositoryManager.get().lock( item );
        new ProjectFileUtils().copyFile( repositoryFile, wipFile );
		
		for (int i = 1; i <= 5; i++) {
			repositoryManager.get().commit( item, "Commit #" + i );
			System.out.print(".");
		}
		repositoryManager.get().unlock( item, true, "Final commit" );
	}
	
	@SuppressWarnings("unchecked")
	protected List<String> loadRepositoryUsers() throws Exception {
		File userFile = new File( repositorySnapshotFolder, "repository-users.xml" );
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<RepositoryUsers> documentElement =
        		(JAXBElement<RepositoryUsers>) unmarshaller.unmarshal( userFile );
        RepositoryUsers repoUsers = documentElement.getValue();
		List<String> useridList = new ArrayList<>();
		
		for (UserInfo userInfo : repoUsers.getUser()) {
			String userId = userInfo.getUserId();
			
			if (userId.startsWith("testuser")) {
				useridList.add( userId );
			}
		}
		return useridList;
	}
	
    /**
     * Initializes the validation schema and shared JAXB context.
     */
    static {
        try {
            jaxbContext = JAXBContext.newInstance( USERFILE_SCHEMA_CONTEXT );

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}

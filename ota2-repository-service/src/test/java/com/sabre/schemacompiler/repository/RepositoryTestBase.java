/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.repository;

import java.io.File;
import java.util.List;

import com.sabre.schemacompiler.util.RepositoryTestUtils;

/**
 * Abstract base class that defines common methods used during live repository testing.
 * 
 * @author S. Livezey
 */
public abstract class RepositoryTestBase {
	
	protected static final boolean DEBUG = true;
	
	protected static ThreadLocal<RepositoryManager> repositoryManager = new ThreadLocal<RepositoryManager>();
	protected static ThreadLocal<Repository> testRepository = new ThreadLocal<Repository>();
	protected static ThreadLocal<JettyTestServer> jettyServer = new ThreadLocal<JettyTestServer>();
	protected static ThreadLocal<File> wipFolder = new ThreadLocal<File>();
	
	protected static void setupWorkInProcessArea(Class<?> testClass) throws Exception {
		File wipSnapshot = new File(System.getProperty("user.dir"), "/src/test/resources/test-data");
		wipFolder.set( new File(System.getProperty("user.dir"), "/target/test-workspace/" + testClass.getSimpleName() + "/wip") );
		
		if (wipFolder.get().exists()) {
			RepositoryTestUtils.deleteContents(wipFolder.get());
		}
		RepositoryTestUtils.copyContents(wipSnapshot, wipFolder.get());
	}
	
	protected synchronized static void startTestServer(String repositorySnapshotFolder, int port, Class<?> testClass) throws Exception {
		System.setProperty("ota2.repository.realTimeIndexing", "true");
		File localRepository = new File(System.getProperty("user.dir"), "/target/test-workspace/" + testClass.getSimpleName() + "/local-repository");
		File snapshotBase = new File(System.getProperty("user.dir"), "/src/test/resources/repo-snapshots");
		File repositorySnapshot = new File(snapshotBase, repositorySnapshotFolder);
		
		if (localRepository.exists()) {
			RepositoryTestUtils.deleteContents(localRepository);
		}
		localRepository.mkdirs();
		repositoryManager.set( new RepositoryManager( localRepository ) );
		
		jettyServer.set( new JettyTestServer(port, repositorySnapshot, testClass) );
		jettyServer.get().start();
		
		testRepository.set( jettyServer.get().configureRepositoryManager(repositoryManager.get()) );
		repositoryManager.get().setCredentials(testRepository.get(), "testuser", "password");
	}
	
	protected static void shutdownTestServer() throws Exception {
		jettyServer.get().stop();
	}
	
	protected ProjectItem findProjectItem(Project project, String filename) {
		ProjectItem result = null;
		
		for (ProjectItem item : project.getProjectItems()) {
			if (item.getFilename().equals(filename)) {
				result = item;
				break;
			}
		}
		return result;
	}
	
	protected RepositoryItem findRepositoryItem(List<RepositoryItem> itemList, String filename) {
		RepositoryItem result = null;
		
		for (RepositoryItem item : itemList) {
			if (item.getFilename().equals(filename)) {
				result = item;
				break;
			}
		}
		return result;
	}
	
}

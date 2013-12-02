/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.util.RepositoryTestUtils;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;

/**
 * Verifies the operation of items published to a user's local repository.
 * 
 * @author S. Livezey
 */
public class TestLocalRepositoryFunctions extends TestRepositoryFunctions {
	
	@BeforeClass
	public static void setupLocalRepository() throws Exception {
		File localRepository = new File(System.getProperty("user.dir"),
				"/target/test-workspace/" + TestLocalRepositoryFunctions.class.getSimpleName() + "/local-repository");
		File snapshotBase = new File(System.getProperty("user.dir"), "/src/test/resources/repo-snapshots");
		File repositorySnapshot = new File(snapshotBase, "versions-repository");
		
		if (localRepository.exists()) {
			RepositoryTestUtils.deleteContents(localRepository);
		}
		localRepository.mkdirs();
		RepositoryTestUtils.copyContents(repositorySnapshot, localRepository);
		
		RepositoryManager rm = new RepositoryManager( localRepository );
		
		testRepository.set( rm );
		repositoryManager.set( rm );
		setupWorkInProcessArea( TestLocalRepositoryFunctions.class );
	}
	
	@Test
	public void testAutoLoadPriorVersions() throws Exception {
		ProjectManager projectManager = new ProjectManager( new TLModel(), false, repositoryManager.get() );
		File projectFile = new File(wipFolder.get(), "/projects/version_test_4.xml");
		
		if (!projectFile.exists()) {
			throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
		}
		
		ValidationFindings findings = new ValidationFindings();
		Project project = projectManager.loadProject(projectFile, findings);
		
		// Verify that the project loaded correctly
		if (findings.hasFinding(FindingType.ERROR)) {
			RepositoryTestUtils.printFindings( findings );
		}
		assertFalse( findings.hasFinding(FindingType.ERROR) );
		
		// Make sure the project manager automatically loaded previous versions of the library that
		// were not explicitly called out in the project
		assertNotNull( findProjectItem(project, "Version_Test_1_1_1.otm") );
		assertNotNull( findProjectItem(project, "Version_Test_1_1_0.otm") );
		assertNotNull( findProjectItem(project, "Version_Test_1_0_0.otm") );
		
		// NOTE: For the local repository test, the above libraries contain no types.  It is important
		//       that no content is added to the files because it verifies a bug fix that prevented the
		//       loading of earlier library versions if the later version contains no content.
	}
	
}

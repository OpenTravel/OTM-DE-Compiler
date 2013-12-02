/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLLibraryStatus;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.util.RepositoryTestUtils;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;

/**
 * Verifies the operation of the Repository Web Service by launching a Jetty server to run the
 * web service.  Operations are accessed via remote URL connection to the Jetty server running on
 * the local host.
 * 
 * <p>NOTE: The test cases defined in this class are intended to be executed as a consecutive
 * sequence of operations.  Attempting to run a single test individually may result in a failure
 * due to an invalid repository state.
 * 
 * @author S. Livezey
 */
public abstract class TestRepositoryFunctions extends RepositoryTestBase {
	
	@Test
	public void testAllFunctions() throws Exception {
		test_01_PublishLibrary();
		test_02_LockLibrary();
		test_03_CommitLibrary();
		test_04_RevertLibrary();
		test_05_UnlockLibrary();
		test_06_PromoteLibrary();
		test_07_RecalculateLibraryCrc();
		test_08_DemoteLibrary();
		test_09_DeleteLibrary();
		test_10_CreateNamespace();
		test_11_ListNamespaceChildren();
		test_12_DeleteNamespace();
		test_13_CreateRootNamespace();
		test_14_DeleteRootNamespace();
	}
	
	public void test_01_PublishLibrary() throws Exception {
		if (DEBUG) System.out.println("PUBLISH - Publishing new content to the remote repository. [" + getClass().getSimpleName() + "]");
		ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
		File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");
		
		if (!projectFile.exists()) {
			throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
		}
		
		ValidationFindings findings = new ValidationFindings();
		Project project = projectManager.loadProject(projectFile, findings);
		ProjectItem projectItem = findProjectItem(project, "library_1_p2.xml");
		
		// Verify that the project loaded correctly
		if (findings.hasFinding(FindingType.ERROR)) {
			RepositoryTestUtils.printFindings( findings );
		}
		assertFalse( findings.hasFinding(FindingType.ERROR) );
		assertNotNull( projectItem );
		
		// Publish the item and make sure that everything worked correctly
		try {
			projectManager.publish(projectItem, testRepository.get());
			Assert.fail("Expected exception not thrown: " + PublishWithLocalDependenciesException.class.getName());
			
		} catch (PublishWithLocalDependenciesException e) {
			// Verifies that unmanaged dependencies are recognized and the publication
			// job can be successfully resubmitted
			projectManager.publish(e.getRequiredPublications(), testRepository.get());
		}
		
		assertNotNull( projectItem );
		assertNotNull( projectItem.getContent() );
		assertNotNull( projectItem.getContent().getOwningModel() );
		assertNotNull( projectItem.getRepository() );
		assertEquals( "library_1_p2_2_0_0.otm", projectItem.getFilename() );
		assertEquals( RepositoryItemState.MANAGED_UNLOCKED, projectItem.getState() );
		
		if (DEBUG) System.out.println("DONE - Success.");
	}
	
	public void test_02_LockLibrary() throws Exception {
		if (DEBUG) System.out.println("LOCK - Obtaining a lock for a managed project item. [" + getClass().getSimpleName() + "]");
		ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
		File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");
		
		if (!projectFile.exists()) {
			throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
		}
		
		ValidationFindings findings = new ValidationFindings();
		Project project = projectManager.loadProject(projectFile, findings);
		ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");
		
		// Verify that the project loaded correctly
		if (findings.hasFinding(FindingType.ERROR)) {
			RepositoryTestUtils.printFindings( findings );
		}
		assertFalse( findings.hasFinding(FindingType.ERROR) );
		assertNotNull( projectItem );
		
		projectManager.lock(projectItem, project.getProjectFile().getParentFile());
		
		// Close the project and re-open to make sure the item's status is WIP (not locked)
		projectManager.closeProject( project );
		project = projectManager.loadProject(projectFile, findings);
		projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");
		
		assertNotNull( projectItem );
		assertEquals( RepositoryItemState.MANAGED_WIP, projectItem.getState() );
		
		// Also make sure that listing the repository item returns the correct state of WIP
		List<RepositoryItem> itemList = testRepository.get().listItems(projectItem.getBaseNamespace(), true, true);
		
		for (RepositoryItem item : itemList) {
			if (item.getFilename().equals(projectItem.getFilename())) {
				assertEquals( RepositoryItemState.MANAGED_WIP, item.getState() );
			}
		}
		
		if (DEBUG) System.out.println("DONE - Success.");
	}
	
	public void test_03_CommitLibrary() throws Exception {
		if (DEBUG) System.out.println("COMMIT - Committing changes to the remote repository. [" + getClass().getSimpleName() + "]");
		ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
		File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");
		
		if (!projectFile.exists()) {
			throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
		}
		
		ValidationFindings findings = new ValidationFindings();
		Project project = projectManager.loadProject(projectFile, findings);
		ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");
		
		// Verify that the project loaded correctly
		if (findings.hasFinding(FindingType.ERROR)) {
			RepositoryTestUtils.printFindings( findings );
		}
		assertFalse( findings.hasFinding(FindingType.ERROR) );
		assertNotNull( projectItem );
		
		// Update the content so we can tell if the library has been modified
		TLLibrary library = (TLLibrary) projectItem.getContent();
		library.setComments("Library Committed: " + new Date().toString());
		projectManager.saveProject(project);
		
		// Commit the changes to the repository
		projectManager.commit(projectItem);
		if (DEBUG) System.out.println("DONE - Success.");
	}
	
	public void test_04_RevertLibrary() throws Exception {
		if (DEBUG) System.out.println("REVERT - Reverting to original content for a managed project item. [" + getClass().getSimpleName() + "]");
		ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
		File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");
		
		if (!projectFile.exists()) {
			throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
		}
		
		ValidationFindings findings = new ValidationFindings();
		Project project = projectManager.loadProject(projectFile, findings);
		ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");
		
		// Verify that the project loaded correctly
		if (findings.hasFinding(FindingType.ERROR)) {
			RepositoryTestUtils.printFindings( findings );
		}
		assertFalse( findings.hasFinding(FindingType.ERROR) );
		assertNotNull( projectItem );
		
		// Update the content so we can tell if the library has been modified
		TLLibrary library = (TLLibrary) projectItem.getContent();
		library.setComments("Pre-Revert Comments: " + new Date().toString());
		projectManager.saveProject(project);
		
		// Commit the changes to the repository
		projectManager.revert(projectItem);
		if (DEBUG) System.out.println("DONE - Success.");
	}
	
	public void test_05_UnlockLibrary() throws Exception {
		if (DEBUG) System.out.println("UNLOCK - Releasing lock for a managed project item. [" + getClass().getSimpleName() + "]");
		ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
		File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");
		
		if (!projectFile.exists()) {
			throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
		}
		
		ValidationFindings findings = new ValidationFindings();
		Project project = projectManager.loadProject(projectFile, findings);
		ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");
		
		// Verify that the project loaded correctly
		if (findings.hasFinding(FindingType.ERROR)) {
			RepositoryTestUtils.printFindings( findings );
		}
		assertFalse( findings.hasFinding(FindingType.ERROR) );
		assertNotNull( projectItem );
		
		// Update the content so we can tell if the library has been modified
		TLLibrary library = (TLLibrary) projectItem.getContent();
		library.setComments("Library Unlocked: " + new Date().toString());
		projectManager.saveProject(project);
		
		// Commit the changes to the repository
		projectManager.unlock(projectItem, true);
		if (DEBUG) System.out.println("DONE - Success.");
	}
	
	public void test_06_PromoteLibrary() throws Exception {
		if (DEBUG) System.out.println("PROMOTE - Promoting a managed project item. [" + getClass().getSimpleName() + "]");
		ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
		File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");
		
		if (!projectFile.exists()) {
			throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
		}
		
		ValidationFindings findings = new ValidationFindings();
		Project project = projectManager.loadProject(projectFile, findings);
		ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");
		
		// Verify that the project loaded correctly
		if (findings.hasFinding(FindingType.ERROR)) {
			RepositoryTestUtils.printFindings( findings );
		}
		assertFalse( findings.hasFinding(FindingType.ERROR) );
		assertNotNull( projectItem );
		
		projectManager.promote(projectItem);
		assertEquals( TLLibraryStatus.FINAL, projectItem.getStatus() );
		if (DEBUG) System.out.println("DONE - Success.");
	}
	
	public void test_07_RecalculateLibraryCrc() throws Exception {
		if (DEBUG) System.out.println("RECALCULATE-CRC - Recalculating CRC for a managed project item. [" + getClass().getSimpleName() + "]");
		ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
		File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");
		
		if (!projectFile.exists()) {
			throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
		}
		
		ValidationFindings findings = new ValidationFindings();
		Project project = projectManager.loadProject(projectFile, findings);
		ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");
		
		// Verify that the project loaded correctly
		if (findings.hasFinding(FindingType.ERROR)) {
			RepositoryTestUtils.printFindings( findings );
		}
		assertFalse( findings.hasFinding(FindingType.ERROR) );
		assertNotNull( projectItem );
		
		repositoryManager.get().recalculateCrc(projectItem);
		if (DEBUG) System.out.println("DONE - Success.");
	}
	
	public void test_08_DemoteLibrary() throws Exception {
		if (DEBUG) System.out.println("DEMOTE - Demoting a managed project item. [" + getClass().getSimpleName() + "]");
		ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
		File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");
		
		if (!projectFile.exists()) {
			throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
		}
		
		ValidationFindings findings = new ValidationFindings();
		Project project = projectManager.loadProject(projectFile, findings);
		ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");
		
		// Verify that the project loaded correctly
		if (findings.hasFinding(FindingType.ERROR)) {
			RepositoryTestUtils.printFindings( findings );
		}
		assertFalse( findings.hasFinding(FindingType.ERROR) );
		assertNotNull( projectItem );
		
		projectManager.demote(projectItem);
		assertEquals( TLLibraryStatus.DRAFT, projectItem.getStatus() );
		if (DEBUG) System.out.println("DONE - Success.");
	}
	
	public void test_09_DeleteLibrary() throws Exception {
		if (DEBUG) System.out.println("DELETE - Delete a managed project item. [" + getClass().getSimpleName() + "]");
		ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
		File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");
		
		if (!projectFile.exists()) {
			throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
		}
		
		ValidationFindings findings = new ValidationFindings();
		Project project = projectManager.loadProject(projectFile, findings);
		ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");
		
		// Verify that the project loaded correctly
		if (findings.hasFinding(FindingType.ERROR)) {
			RepositoryTestUtils.printFindings( findings );
		}
		assertFalse( findings.hasFinding(FindingType.ERROR) );
		assertNotNull( projectItem );
		
		repositoryManager.get().delete( projectItem );
		
		try {
			repositoryManager.get().getRepositoryItem(
					projectItem.getBaseNamespace(), projectItem.getFilename(), projectItem.getVersion() );
			Assert.fail("Expected Repository Exception - Not Thrown");
			
		} catch (RepositoryException e) {
			// No error - exception was expected, so the test passed
		}
		if (DEBUG) System.out.println("DONE - Success.");
	}
	
	public void test_10_CreateNamespace() throws Exception {
		if (DEBUG) System.out.println("CREATE NAMESPACE - Create a managed namespace item. [" + getClass().getSimpleName() + "]");
		String managedNS = "http://www.OpenTravel.org/ns/Test-NS";
		List<String> repositoryNamespaces = testRepository.get().listBaseNamespaces();
		
		// Make sure the namespace does not exist yet
		assertFalse( repositoryNamespaces.contains(managedNS) );
		
		// Create the namespace and verify that it exists
		testRepository.get().createNamespace( managedNS );
		repositoryNamespaces = testRepository.get().listBaseNamespaces();
		assertTrue( repositoryNamespaces.contains(managedNS) );
		
		if (DEBUG) System.out.println("DONE - Success.");
	}
	
	public void test_11_ListNamespaceChildren() throws Exception {
		if (DEBUG) System.out.println("LIST NAMESPACE CHILDREN - Find the children of a managed namespace. [" + getClass().getSimpleName() + "]");
		List<String> nsChildren = testRepository.get().listNamespaceChildren("http://www.OpenTravel.org/ns");
		
		assertEquals( 2, nsChildren.size() );
		assertTrue( nsChildren.contains("OTA2") );
		assertTrue( nsChildren.contains("Test-NS") );
		
		if (DEBUG) System.out.println("DONE - Success.");
	}
	
	public void test_12_DeleteNamespace() throws Exception {
		if (DEBUG) System.out.println("DELETE NAMESPACE - Delete a managed namespace item. [" + getClass().getSimpleName() + "]");
		String managedNS = "http://www.OpenTravel.org/ns/Test-NS";
		List<String> repositoryNamespaces = testRepository.get().listBaseNamespaces();
		
		// Make sure the namespace exists (it was created during the previous operation)
		assertTrue( repositoryNamespaces.contains(managedNS) );
		
		// Delete the namespace and verify that it no longer exists
		testRepository.get().deleteNamespace( managedNS );
		repositoryNamespaces = testRepository.get().listBaseNamespaces();
		assertFalse( repositoryNamespaces.contains(managedNS) );
		
		if (DEBUG) System.out.println("DONE - Success.");
	}
	
	public void test_13_CreateRootNamespace() throws Exception {
		if (DEBUG) System.out.println("CREATE ROOT NAMESPACE - Create a managed namespace item. [" + getClass().getSimpleName() + "]");
		String rootNS = "http://www.testnamespace.com";
		List<String> rootNamespaces = testRepository.get().listRootNamespaces();
		
		// Make sure the namespace does not exist yet
		assertFalse( rootNamespaces.contains(rootNS) );
		
		// Create the namespace and verify that it exists
		testRepository.get().createRootNamespace( rootNS );
		rootNamespaces = testRepository.get().listRootNamespaces();
		assertTrue( rootNamespaces.contains(rootNS) );
		
		// Attempt to create some conflicting namespaces and verify that exceptions are thrown
		String[] conflictingRootNamespaces = new String[] {
			"http://www.testnamespace.com/ns1", "http://testnamespace.com"
		};
		
		for (String conflictingRootNS : conflictingRootNamespaces) {
			try {
				testRepository.get().createRootNamespace( conflictingRootNS );
				fail("Able to create conflicting root namespace: " + conflictingRootNS);
				
			} catch (RepositoryException e) {
				// Expected exception - test passed
			}
		}
		if (DEBUG) System.out.println("DONE - Success.");
	}
	
	public void test_14_DeleteRootNamespace() throws Exception {
		if (DEBUG) System.out.println("DELETE ROOT NAMESPACE - Delete a managed namespace item. [" + getClass().getSimpleName() + "]");
		String rootNS = "http://www.testnamespace.com";
		List<String> rootNamespaces = testRepository.get().listRootNamespaces();
		
		// Make sure the namespace exists (it was created during the previous operation)
		assertTrue( rootNamespaces.contains(rootNS) );
		
		// Delete the namespace and verify that it no longer exists
		testRepository.get().deleteRootNamespace( rootNS );
		rootNamespaces = testRepository.get().listRootNamespaces();
		assertFalse( rootNamespaces.contains(rootNS) );
		
		if (DEBUG) System.out.println("DONE - Success.");
	}
	
}

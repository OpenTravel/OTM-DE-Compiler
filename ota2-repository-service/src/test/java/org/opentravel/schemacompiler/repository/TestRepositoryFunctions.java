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
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.release.ReleaseManager;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.PublishWithLocalDependenciesException;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemacompiler.util.RepositoryTestUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Verifies the operation of the Repository Web Service by launching a Jetty server to run the web
 * service. Operations are accessed via remote URL connection to the Jetty server running on the
 * local host.
 * 
 * <p>
 * NOTE: The test cases defined in this class are intended to be executed as a consecutive sequence
 * of operations. Attempting to run a single test individually may result in a failure due to an
 * invalid repository state.
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
        test_09_UpdateLibraryStatus();
        test_10_GetLibraryHistory();
    	test_11_createBetaAndFinalReleases();
    	test_12_publishRelease();
    	test_13_deleteRelease();
        test_14_DeleteLibrary();
        test_15_CreateNamespace();
        test_15a_CreateNamespaceError();
        test_16_ListNamespaceChildren();
        test_17_DeleteNamespace();
        test_18_CreateRootNamespace();
        test_19_DeleteRootNamespace();
    }

    public void test_01_PublishLibrary() throws Exception {
        if (DEBUG)
            System.out.println("PUBLISH - Publishing new content to the remote repository. ["
                    + getClass().getSimpleName() + "]");
        ProjectManager projectManager = new ProjectManager(new TLModel(), true,
                repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);
        ProjectItem projectItem = findProjectItem(project, "library_1_p2.xml");

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
        assertNotNull(projectItem);

        // Publish the item and make sure that everything worked correctly
        try {
            projectManager.publish(projectItem, testRepository.get());
            Assert.fail("Expected exception not thrown: "
                    + PublishWithLocalDependenciesException.class.getName());

        } catch (PublishWithLocalDependenciesException e) {
            // Verifies that unmanaged dependencies are recognized and the publication
            // job can be successfully resubmitted
            projectManager.publish(e.getRequiredPublications(), testRepository.get());
        }

        assertNotNull(projectItem);
        assertNotNull(projectItem.getContent());
        assertNotNull(projectItem.getContent().getOwningModel());
        assertNotNull(projectItem.getRepository());
        assertEquals("library_1_p2_2_0_0.otm", projectItem.getFilename());
        assertEquals(RepositoryItemState.MANAGED_UNLOCKED, projectItem.getState());

        if (DEBUG)
            System.out.println("DONE - Success.");
    }

    public void test_02_LockLibrary() throws Exception {
        if (DEBUG)
            System.out.println("LOCK - Obtaining a lock for a managed project item. ["
                    + getClass().getSimpleName() + "]");
        ProjectManager projectManager = new ProjectManager(new TLModel(), true,
                repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);
        ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
        assertNotNull(projectItem);

        projectManager.lock(projectItem, project.getProjectFile().getParentFile());

        // Close the project and re-open to make sure the item's status is WIP (not locked)
        projectManager.closeProject(project);
        project = projectManager.loadProject(projectFile, findings);
        projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");

        assertNotNull(projectItem);
        assertEquals(RepositoryItemState.MANAGED_WIP, projectItem.getState());

        // Also make sure that listing the repository item returns the correct state of WIP
        List<RepositoryItem> itemList = testRepository.get().listItems(
                projectItem.getBaseNamespace(), TLLibraryStatus.DRAFT, true);

        for (RepositoryItem item : itemList) {
            if (item.getFilename().equals(projectItem.getFilename())) {
                assertEquals(RepositoryItemState.MANAGED_WIP, item.getState());
            }
        }
        
        // Verifiy that the locked library is returned by Repository.getLockedItems()
//        List<RepositoryItem> lockedItems = projectItem.getRepository().getLockedItems();
//        
//        assertEquals(1, lockedItems.size());
//        assertEquals("library_1_p2_2_0_0.otm", lockedItems.get(0).getFilename());
        
        if (DEBUG)
            System.out.println("DONE - Success.");
    }

    public void test_03_CommitLibrary() throws Exception {
        if (DEBUG)
            System.out.println("COMMIT - Committing changes to the remote repository. ["
                    + getClass().getSimpleName() + "]");
        ProjectManager projectManager = new ProjectManager(new TLModel(), true,
                repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);
        ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
        assertNotNull(projectItem);

        // Update the content so we can tell if the library has been modified
        TLLibrary library = (TLLibrary) projectItem.getContent();
        library.setComments("Library Committed: " + new Date().toString());
        projectManager.saveProject(project);

        // Commit the changes to the repository
        projectManager.commit(projectItem, "Updated library comments.");
        if (DEBUG)
            System.out.println("DONE - Success.");
    }

    public void test_04_RevertLibrary() throws Exception {
        if (DEBUG)
            System.out
                    .println("REVERT - Reverting to original content for a managed project item. ["
                            + getClass().getSimpleName() + "]");
        ProjectManager projectManager = new ProjectManager(new TLModel(), true,
                repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);
        ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
        assertNotNull(projectItem);

        // Update the content so we can tell if the library has been modified
        TLLibrary library = (TLLibrary) projectItem.getContent();
        library.setComments("Pre-Revert Comments: " + new Date().toString());
        projectManager.saveProject(project);

        // Commit the changes to the repository
        projectManager.revert(projectItem);
        if (DEBUG)
            System.out.println("DONE - Success.");
    }

    public void test_05_UnlockLibrary() throws Exception {
        if (DEBUG)
            System.out.println("UNLOCK - Releasing lock for a managed project item. ["
                    + getClass().getSimpleName() + "]");
        ProjectManager projectManager = new ProjectManager(new TLModel(), true,
                repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);
        ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
        assertNotNull(projectItem);

        // Update the content so we can tell if the library has been modified
        TLLibrary library = (TLLibrary) projectItem.getContent();
        library.setComments("Library Unlocked: " + new Date().toString());
        projectManager.saveProject(project);

        // Commit the changes to the repository
        projectManager.unlock(projectItem, true, "Unlocking with commit.");
        if (DEBUG)
            System.out.println("DONE - Success.");
    }

    public void test_06_PromoteLibrary() throws Exception {
        if (DEBUG)
            System.out.println("PROMOTE - Promoting a managed project item. ["
                    + getClass().getSimpleName() + "]");
        ProjectManager projectManager = new ProjectManager(new TLModel(), true, repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);
        ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
        assertNotNull(projectItem);

        projectManager.promote(projectItem);
        if (OTM16Upgrade.otm16Enabled) {
        	// If OTM 1.6 is enabled, we must promote a second time to get the library
        	// into the FINAL status
            projectManager.promote(projectItem);
        }
        
        assertEquals(TLLibraryStatus.FINAL, projectItem.getStatus());
        if (DEBUG)
            System.out.println("DONE - Success.");
    }

    public void test_07_RecalculateLibraryCrc() throws Exception {
        if (DEBUG)
            System.out.println("RECALCULATE-CRC - Recalculating CRC for a managed project item. ["
                    + getClass().getSimpleName() + "]");
        ProjectManager projectManager = new ProjectManager(new TLModel(), true,
                repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);
        ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
        assertNotNull(projectItem);

        repositoryManager.get().recalculateCrc(projectItem);
        if (DEBUG)
            System.out.println("DONE - Success.");
    }

    public void test_08_DemoteLibrary() throws Exception {
        if (DEBUG)
            System.out.println("DEMOTE - Demoting a managed project item. ["
                    + getClass().getSimpleName() + "]");
        ProjectManager projectManager = new ProjectManager(new TLModel(), true,
                repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);
        ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
        assertNotNull(projectItem);

        projectManager.demote(projectItem);
        if (OTM16Upgrade.otm16Enabled) {
        	// If OTM 1.6 is enabled, we must demote a second time to get the library
        	// back into the DRAFT status
            projectManager.demote(projectItem);
        }
        
        assertEquals(TLLibraryStatus.DRAFT, projectItem.getStatus());
        if (DEBUG)
            System.out.println("DONE - Success.");
    }
    
    public void test_09_UpdateLibraryStatus() throws Exception {
        if (DEBUG)
            System.out.println("UPDATE-STATUS - Updating status of a managed project item. ["
                    + getClass().getSimpleName() + "]");
        ProjectManager projectManager = new ProjectManager(new TLModel(), true,
                repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);
        ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
        assertNotNull(projectItem);

        projectManager.updateStatus(projectItem, TLLibraryStatus.OBSOLETE);
        assertEquals(TLLibraryStatus.OBSOLETE, projectItem.getStatus());
        projectManager.updateStatus(projectItem, TLLibraryStatus.DRAFT);
        assertEquals(TLLibraryStatus.DRAFT, projectItem.getStatus());
        if (DEBUG)
            System.out.println("DONE - Success.");
    }

    public void test_10_GetLibraryHistory() throws Exception {
        System.out.println("GET-HISTORY - Retrieving commit history of a managed project item. ["
                + getClass().getSimpleName() + "]");
        ProjectManager projectManager = new ProjectManager(new TLModel(), true,
                repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);
        ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
        assertNotNull(projectItem);
        
        RepositoryItemHistory history = repositoryManager.get().getHistory( projectItem );
        int expectedCommits = OTM16Upgrade.otm16Enabled ? 8 : 6;
        
        assertEquals( projectItem.getBaseNamespace(), history.getRepositoryItem().getBaseNamespace() );
        assertEquals( projectItem.getFilename(), history.getRepositoryItem().getFilename() );
        assertEquals( projectItem.getVersion(), history.getRepositoryItem().getVersion() );
        assertEquals( expectedCommits, history.getCommitHistory().size() );
        
        for (RepositoryItemCommit itemCommit : history.getCommitHistory()) {
        	assertNotNull(itemCommit.getUser());
        	assertNotNull(itemCommit.getEffectiveOn());
        	assertNotNull(itemCommit.getRemarks());
        }
    }
    
    public void test_11_createBetaAndFinalReleases() throws Exception {
        if (DEBUG)
            System.out.println("CREATE RELEASES - Creating beta and final releases. ["
                    + getClass().getSimpleName() + "]");
        
        // Load the project that contains the library we have been making updates to
        ProjectManager projectManager = new ProjectManager(new TLModel(), true,
                repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);
        ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");
        
        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
        assertNotNull(projectItem);
        
        // Create a release for the main project item
        RepositoryItemHistory history = repositoryManager.get().getHistory( projectItem );
    	ReleaseManager releaseManager = new ReleaseManager( repositoryManager.get() );
    	ValidationFindings releaseFindings = new ValidationFindings();
    	RepositoryItemCommit commit = history.getCommitHistory().get( 1 );
    	
    	releaseManager.createNewRelease(
    			"http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package",
    			"TestRelease", wipFolder.get() );
    	releaseManager.getRelease().setDefaultEffectiveDate( commit.getEffectiveOn() );
    	releaseManager.addPrincipalItem( projectItem );
    	releaseManager.loadReleaseModel( releaseFindings );
    	releaseManager.saveRelease();
    	
        // Verify that the release loaded correctly
        if (releaseFindings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(releaseFindings);
        }
        assertFalse(releaseFindings.hasFinding(FindingType.ERROR));
    }
    
    public void test_12_publishRelease() throws Exception {
    	// TODO: Implement the test_12_publishRelease() method
    }
    
    public void test_13_deleteRelease() throws Exception {
    	// TODO: Implement the test_13_deleteRelease() method
    }
    
    public void test_14_DeleteLibrary() throws Exception {
        if (DEBUG)
            System.out.println("DELETE - Delete a managed project item. ["
                    + getClass().getSimpleName() + "]");
        ProjectManager projectManager = new ProjectManager(new TLModel(), true,
                repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/project_1.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);
        ProjectItem projectItem = findProjectItem(project, "library_1_p2_2_0_0.otm");

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
        assertNotNull(projectItem);

        repositoryManager.get().delete(projectItem);

        try {
            repositoryManager.get().getRepositoryItem(projectItem.getBaseNamespace(),
                    projectItem.getFilename(), projectItem.getVersion());
            Assert.fail("Expected Repository Exception - Not Thrown");

        } catch (RepositoryException e) {
            // No error - exception was expected, so the test passed
        }
        if (DEBUG)
            System.out.println("DONE - Success.");
    }

    public void test_15_CreateNamespace() throws Exception {
        if (DEBUG)
            System.out.println("CREATE NAMESPACE - Create a managed namespace item. ["
                    + getClass().getSimpleName() + "]");
        String managedNS = "http://www.OpenTravel.org/ns/Test-NS";
        List<String> repositoryNamespaces = testRepository.get().listBaseNamespaces();

        // Make sure the namespace does not exist yet
        assertFalse(repositoryNamespaces.contains(managedNS));

        // Create the namespace and verify that it exists
        testRepository.get().createNamespace(managedNS);
        repositoryNamespaces = testRepository.get().listBaseNamespaces();
        assertTrue(repositoryNamespaces.contains(managedNS));

        if (DEBUG)
            System.out.println("DONE - Success.");
    }
    
    public void test_15a_CreateNamespaceError() throws Exception {
        if (DEBUG)
            System.out.println("CREATE NAMESPACE (Error Test) - Attempt to create conflicting namespace. ["
                    + getClass().getSimpleName() + "]");
        String managedNS = "http://www.OpenTravel.org/NS/Test-NS/ns2"; // case-sensitive conflict with test_10
        List<String> repositoryNamespaces = testRepository.get().listBaseNamespaces();

        // Make sure the namespace does not exist yet
        assertFalse(repositoryNamespaces.contains(managedNS));

        // Attempt to create the namespace (should fail with RepositoryException)
        try {
        	testRepository.get().createNamespace(managedNS);
        	fail("Expected exception not thrown.");
        	
        } catch (RepositoryException e) {
        	// No action - failure is the expected result
        }
        repositoryNamespaces = testRepository.get().listBaseNamespaces();
        assertFalse(repositoryNamespaces.contains(managedNS));

        if (DEBUG)
            System.out.println("DONE - Success.");
    }

    public void test_16_ListNamespaceChildren() throws Exception {
        if (DEBUG)
            System.out
                    .println("LIST NAMESPACE CHILDREN - Find the children of a managed namespace. ["
                            + getClass().getSimpleName() + "]");
        List<String> nsChildren = testRepository.get().listNamespaceChildren(
                "http://www.OpenTravel.org/ns");

        assertEquals(2, nsChildren.size());
        assertTrue(nsChildren.contains("OTA2"));
        assertTrue(nsChildren.contains("Test-NS"));

        if (DEBUG)
            System.out.println("DONE - Success.");
    }

    public void test_17_DeleteNamespace() throws Exception {
        if (DEBUG)
            System.out.println("DELETE NAMESPACE - Delete a managed namespace item. ["
                    + getClass().getSimpleName() + "]");
        String managedNS = "http://www.OpenTravel.org/ns/Test-NS";
        List<String> repositoryNamespaces = testRepository.get().listBaseNamespaces();

        // Make sure the namespace exists (it was created during the previous operation)
        assertTrue(repositoryNamespaces.contains(managedNS));

        // Delete the namespace and verify that it no longer exists
        testRepository.get().deleteNamespace(managedNS);
        repositoryNamespaces = testRepository.get().listBaseNamespaces();
        assertFalse(repositoryNamespaces.contains(managedNS));

        if (DEBUG)
            System.out.println("DONE - Success.");
    }

    public void test_18_CreateRootNamespace() throws Exception {
        if (DEBUG)
            System.out.println("CREATE ROOT NAMESPACE - Create a managed namespace item. ["
                    + getClass().getSimpleName() + "]");
        String rootNS = "http://www.testnamespace.com";
        List<String> rootNamespaces = testRepository.get().listRootNamespaces();

        // Make sure the namespace does not exist yet
        assertFalse(rootNamespaces.contains(rootNS));

        // Create the namespace and verify that it exists
        testRepository.get().createRootNamespace(rootNS);
        rootNamespaces = testRepository.get().listRootNamespaces();
        assertTrue(rootNamespaces.contains(rootNS));

        // Attempt to create some conflicting namespaces and verify that exceptions are thrown
        String[] conflictingRootNamespaces = new String[] { "http://www.testnamespace.com/ns1",
                "http://testnamespace.com" };

        for (String conflictingRootNS : conflictingRootNamespaces) {
            try {
                testRepository.get().createRootNamespace(conflictingRootNS);
                fail("Able to create conflicting root namespace: " + conflictingRootNS);

            } catch (RepositoryException e) {
                // Expected exception - test passed
            }
        }
        
        // Create a child namespace to ensure the root is configured properly
        String childNS = rootNS + "/test";
        
        testRepository.get().createNamespace(childNS);
        testRepository.get().deleteNamespace(childNS);
        
        if (DEBUG)
            System.out.println("DONE - Success.");
    }

    public void test_19_DeleteRootNamespace() throws Exception {
        if (DEBUG)
            System.out.println("DELETE ROOT NAMESPACE - Delete a managed namespace item. ["
                    + getClass().getSimpleName() + "]");
        String rootNS = "http://www.testnamespace.com";
        List<String> rootNamespaces = testRepository.get().listRootNamespaces();

        // Make sure the namespace exists (it was created during the previous operation)
        assertTrue(rootNamespaces.contains(rootNS));

        // Delete the namespace and verify that it no longer exists
        testRepository.get().deleteRootNamespace(rootNS);
        rootNamespaces = testRepository.get().listRootNamespaces();
        assertFalse(rootNamespaces.contains(rootNS));

        if (DEBUG)
            System.out.println("DONE - Success.");
    }

}

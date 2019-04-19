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

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.ns.ota2.release_v01_00.ReleaseStatus;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.testutil.RepositoryTestUtils;
import org.opentravel.schemacompiler.task.CommonCompilerTaskOptions;
import org.opentravel.schemacompiler.task.CompileAllCompilerTask;
import org.opentravel.schemacompiler.task.CompileAllTaskOptions;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * Verifies the operation of the Repository Web Service by launching a Jetty server to run the web service. Operations
 * are accessed via remote URL connection to the Jetty server running on the local host.
 * 
 * <p>
 * NOTE: The test cases defined in this class are intended to be executed as a consecutive sequence of operations.
 * Attempting to run a single test individually may result in a failure due to an invalid repository state.
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
        test_11_CreateRelease();
        test_12_PublishRelease();
        test_13_CompileRelease();
        test_14_CreateServiceAssembly();
        test_15_PublishServiceAssembly();
        test_16_UnpublishServiceAssembly();
        test_17_NewReleaseVersion();
        test_18_UnpublishRelease();
        test_19_DeleteLibrary();
        test_20_CreateNamespace();
        test_20a_CreateNamespaceError();
        test_21_ListNamespaceChildren();
        test_22_DeleteNamespace();
        test_23_CreateRootNamespace();
        test_24_DeleteRootNamespace();
    }

    public void test_01_PublishLibrary() throws Exception {
        logDebug( "PUBLISH - Publishing new content to the remote repository. [%s]" );
        ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
        File projectFile = new File( wipFolder.get(), "/projects/project_1.xml" );

        if (!projectFile.exists()) {
            throw new FileNotFoundException( "Test File Not Found: " + projectFile.getAbsolutePath() );
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject( projectFile, findings );
        ProjectItem projectItem = findProjectItem( project, "library_1_p2.xml" );

        // Verify that the project loaded correctly
        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertNotNull( projectItem );

        // Publish the item and make sure that everything worked correctly
        try {
            projectManager.publish( projectItem, testRepository.get() );
            Assert.fail( "Expected exception not thrown: " + PublishWithLocalDependenciesException.class.getName() );

        } catch (PublishWithLocalDependenciesException e) {
            // Verifies that unmanaged dependencies are recognized and the publication
            // job can be successfully resubmitted
            projectManager.publish( e.getRequiredPublications(), testRepository.get() );
        }

        assertNotNull( projectItem );
        assertNotNull( projectItem.getContent() );
        assertNotNull( projectItem.getContent().getOwningModel() );
        assertNotNull( projectItem.getRepository() );
        assertEquals( "library_1_p2_2_0_0.otm", projectItem.getFilename() );
        assertEquals( RepositoryItemState.MANAGED_UNLOCKED, projectItem.getState() );
        model.set( projectManager.getModel() );

        logDebug( "DONE - Success." );
    }

    public void test_02_LockLibrary() throws Exception {
        logDebug( "LOCK - Obtaining a lock for a managed project item. [%s]" );
        ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
        File projectFile = new File( wipFolder.get(), "/projects/project_1.xml" );

        if (!projectFile.exists()) {
            throw new FileNotFoundException( "Test File Not Found: " + projectFile.getAbsolutePath() );
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject( projectFile, findings );
        ProjectItem projectItem = findProjectItem( project, "library_1_p2_2_0_0.otm" );

        // Verify that the project loaded correctly
        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertNotNull( projectItem );

        projectManager.lock( projectItem );

        // Close the project and re-open to make sure the item's status is WIP (not locked)
        projectManager.closeProject( project );
        project = projectManager.loadProject( projectFile, findings );
        projectItem = findProjectItem( project, "library_1_p2_2_0_0.otm" );

        assertNotNull( projectItem );
        assertEquals( RepositoryItemState.MANAGED_WIP, projectItem.getState() );

        // Also make sure that listing the repository item returns the correct state of WIP
        List<RepositoryItem> itemList =
            testRepository.get().listItems( projectItem.getBaseNamespace(), TLLibraryStatus.DRAFT, true );

        for (RepositoryItem item : itemList) {
            if (item.getFilename().equals( projectItem.getFilename() )) {
                assertEquals( RepositoryItemState.MANAGED_WIP, item.getState() );
            }
        }
        model.set( projectManager.getModel() );

        logDebug( "DONE - Success." );
    }

    public void test_03_CommitLibrary() throws Exception {
        logDebug( "COMMIT - Committing changes to the remote repository. [%s]" );
        ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
        File projectFile = new File( wipFolder.get(), "/projects/project_1.xml" );

        if (!projectFile.exists()) {
            throw new FileNotFoundException( "Test File Not Found: " + projectFile.getAbsolutePath() );
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject( projectFile, findings );
        ProjectItem projectItem = findProjectItem( project, "library_1_p2_2_0_0.otm" );

        // Verify that the project loaded correctly
        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertNotNull( projectItem );

        // Update the content so we can tell if the library has been modified
        TLLibrary library = (TLLibrary) projectItem.getContent();
        library.setComments( "Library Committed: " + new Date().toString() );
        projectManager.saveProject( project );

        // Commit the changes to the repository
        projectManager.commit( projectItem, "Updated library comments." );
        model.set( projectManager.getModel() );

        logDebug( "DONE - Success." );
    }

    public void test_04_RevertLibrary() throws Exception {
        if (DEBUG)
            System.out.println( "REVERT - Reverting to original content for a managed project item. ["
                + getClass().getSimpleName() + "]" );
        ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
        File projectFile = new File( wipFolder.get(), "/projects/project_1.xml" );

        if (!projectFile.exists()) {
            throw new FileNotFoundException( "Test File Not Found: " + projectFile.getAbsolutePath() );
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject( projectFile, findings );
        ProjectItem projectItem = findProjectItem( project, "library_1_p2_2_0_0.otm" );

        // Verify that the project loaded correctly
        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertNotNull( projectItem );

        // Update the content so we can tell if the library has been modified
        TLLibrary library = (TLLibrary) projectItem.getContent();
        library.setComments( "Pre-Revert Comments: " + new Date().toString() );
        projectManager.saveProject( project );

        // Commit the changes to the repository
        projectManager.revert( projectItem );
        model.set( projectManager.getModel() );

        logDebug( "DONE - Success." );
    }

    public void test_05_UnlockLibrary() throws Exception {
        logDebug( "UNLOCK - Releasing lock for a managed project item. [%s]" );
        ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
        File projectFile = new File( wipFolder.get(), "/projects/project_1.xml" );

        if (!projectFile.exists()) {
            throw new FileNotFoundException( "Test File Not Found: " + projectFile.getAbsolutePath() );
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject( projectFile, findings );
        ProjectItem projectItem = findProjectItem( project, "library_1_p2_2_0_0.otm" );

        // Verify that the project loaded correctly
        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertNotNull( projectItem );

        // Update the content so we can tell if the library has been modified
        TLLibrary library = (TLLibrary) projectItem.getContent();
        library.setComments( "Library Unlocked: " + new Date().toString() );
        projectManager.saveProject( project );

        // Commit the changes to the repository
        projectManager.unlock( projectItem, true, "Unlocking with commit." );
        model.set( projectManager.getModel() );

        logDebug( "DONE - Success." );
    }

    public void test_06_PromoteLibrary() throws Exception {
        logDebug( "PROMOTE - Promoting a managed project item. [%s]" );
        ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
        File projectFile = new File( wipFolder.get(), "/projects/project_1.xml" );

        if (!projectFile.exists()) {
            throw new FileNotFoundException( "Test File Not Found: " + projectFile.getAbsolutePath() );
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject( projectFile, findings );
        ProjectItem projectItem = findProjectItem( project, "library_1_p2_2_0_0.otm" );

        // Verify that the project loaded correctly
        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertNotNull( projectItem );

        // We must promote twice to get the library to the FINAL status
        projectManager.promote( projectItem );
        projectManager.promote( projectItem );

        assertEquals( TLLibraryStatus.FINAL, projectItem.getStatus() );
        model.set( projectManager.getModel() );

        logDebug( "DONE - Success." );
    }

    public void test_07_RecalculateLibraryCrc() throws Exception {
        logDebug( "RECALCULATE-CRC - Recalculating CRC for a managed project item. [%s]" );
        ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
        File projectFile = new File( wipFolder.get(), "/projects/project_1.xml" );

        if (!projectFile.exists()) {
            throw new FileNotFoundException( "Test File Not Found: " + projectFile.getAbsolutePath() );
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject( projectFile, findings );
        ProjectItem projectItem = findProjectItem( project, "library_1_p2_2_0_0.otm" );

        // Verify that the project loaded correctly
        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertNotNull( projectItem );

        repositoryManager.get().recalculateCrc( projectItem );
        model.set( projectManager.getModel() );

        logDebug( "DONE - Success." );
    }

    public void test_08_DemoteLibrary() throws Exception {
        logDebug( "DEMOTE - Demoting a managed project item. [%s]" );
        ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
        File projectFile = new File( wipFolder.get(), "/projects/project_1.xml" );

        if (!projectFile.exists()) {
            throw new FileNotFoundException( "Test File Not Found: " + projectFile.getAbsolutePath() );
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject( projectFile, findings );
        ProjectItem projectItem = findProjectItem( project, "library_1_p2_2_0_0.otm" );

        // Verify that the project loaded correctly
        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertNotNull( projectItem );

        // We must demote twice to get the library back into the DRAFT status
        projectManager.demote( projectItem );
        projectManager.demote( projectItem );

        assertEquals( TLLibraryStatus.DRAFT, projectItem.getStatus() );
        model.set( projectManager.getModel() );

        logDebug( "DONE - Success." );
    }

    public void test_09_UpdateLibraryStatus() throws Exception {
        logDebug( "UPDATE-STATUS - Updating status of a managed project item. [%s]" );
        ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
        File projectFile = new File( wipFolder.get(), "/projects/project_1.xml" );

        if (!projectFile.exists()) {
            throw new FileNotFoundException( "Test File Not Found: " + projectFile.getAbsolutePath() );
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject( projectFile, findings );
        ProjectItem projectItem = findProjectItem( project, "library_1_p2_2_0_0.otm" );

        // Verify that the project loaded correctly
        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertNotNull( projectItem );

        projectManager.updateStatus( projectItem, TLLibraryStatus.OBSOLETE );
        assertEquals( TLLibraryStatus.OBSOLETE, projectItem.getStatus() );
        projectManager.updateStatus( projectItem, TLLibraryStatus.DRAFT );
        assertEquals( TLLibraryStatus.DRAFT, projectItem.getStatus() );
        model.set( projectManager.getModel() );

        logDebug( "DONE - Success." );
    }

    public void test_10_GetLibraryHistory() throws Exception {
        logDebug( "GET-HISTORY - Retrieving commit history of a managed project item. [%s]" );
        ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
        File projectFile = new File( wipFolder.get(), "/projects/project_1.xml" );

        if (!projectFile.exists()) {
            throw new FileNotFoundException( "Test File Not Found: " + projectFile.getAbsolutePath() );
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject( projectFile, findings );
        ProjectItem projectItem = findProjectItem( project, "library_1_p2_2_0_0.otm" );

        // Verify that the project loaded correctly
        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertNotNull( projectItem );

        RepositoryItemHistory history = repositoryManager.get().getHistory( projectItem );

        assertEquals( projectItem.getBaseNamespace(), history.getRepositoryItem().getBaseNamespace() );
        assertEquals( projectItem.getFilename(), history.getRepositoryItem().getFilename() );
        assertEquals( projectItem.getVersion(), history.getRepositoryItem().getVersion() );
        assertEquals( 8, history.getCommitHistory().size() );

        for (RepositoryItemCommit itemCommit : history.getCommitHistory()) {
            assertNotNull( itemCommit.getUser() );
            assertNotNull( itemCommit.getEffectiveOn() );
            assertNotNull( itemCommit.getRemarks() );
        }
        model.set( projectManager.getModel() );

        logDebug( "DONE - Success." );
    }

    public void test_11_CreateRelease() throws Exception {
        logDebug( "CREATE RELEASE - Creating beta release. [%s]" );

        // Load the project that contains the library we have been making updates to
        ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
        File projectFile = new File( wipFolder.get(), "/projects/project_1.xml" );

        if (!projectFile.exists()) {
            throw new FileNotFoundException( "Test File Not Found: " + projectFile.getAbsolutePath() );
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject( projectFile, findings );
        ProjectItem projectItem = findProjectItem( project, "library_1_p2_2_0_0.otm" );
        ReleaseCompileOptions compileOptions = new ReleaseCompileOptions();

        // Verify that the release loaded correctly
        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertNotNull( projectItem );

        // Create a release for the main project item
        RepositoryItemHistory history = repositoryManager.get().getHistory( projectItem );
        ReleaseManager releaseManager = new ReleaseManager( repositoryManager.get() );
        ValidationFindings releaseFindings = new ValidationFindings();
        RepositoryItemCommit commit = history.getCommitHistory().get( 1 );

        releaseManager.createNewRelease( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package", "TestRelease",
            wipFolder.get() );
        releaseManager.getRelease().setDefaultEffectiveDate( commit.getEffectiveOn() );
        releaseManager.addPrincipalMember( projectItem );
        releaseManager.loadReleaseModel( releaseFindings );
        compileOptions.applyTaskOptions( getReleaseCompileOptions() );
        releaseManager.getRelease().setCompileOptions( compileOptions );
        releaseManager.saveRelease();
        validateRelease( releaseManager, releaseFindings, 1 );

        // Create another release by importing from a project (should be a duplicate of the one we created manually)
        releaseManager = new ReleaseManager( repositoryManager.get() );
        releaseFindings = new ValidationFindings();
        releaseManager.importFromProject( projectFile, releaseFindings );
        validateRelease( releaseManager, releaseFindings, 4 );

        model.set( projectManager.getModel() );

        logDebug( "DONE - Success." );
    }

    private void validateRelease(ReleaseManager releaseManager, ValidationFindings releaseFindings,
        int expectedPrincipalMembers) {
        // Verify that the release loaded correctly
        if (releaseFindings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( releaseFindings );
        }
        assertFalse( releaseFindings.hasFinding( FindingType.ERROR ) );
        assertEquals( ReleaseStatus.DRAFT, releaseManager.getRelease().getStatus() );

        // Verify that all principal and referenced libraries were identified
        Release release = releaseManager.getRelease();
        Set<String> itemFilenames = getReleaseItemFilenames( release.getAllMembers() );

        assertEquals( expectedPrincipalMembers, release.getPrincipalMembers().size() );
        assertEquals( (4 - expectedPrincipalMembers), release.getReferencedMembers().size() );
        assertTrue( itemFilenames.contains( "library_1_p2_2_0_0.otm" ) );
        assertTrue( itemFilenames.contains( "library_2_p2_2_0_0.otm" ) );
        assertTrue( itemFilenames.contains( "library_2_p1_1_0_0.otm" ) );
        assertTrue( itemFilenames.contains( "library_1_p1_1_0_0.otm" ) );
    }

    public void test_12_PublishRelease() throws Exception {
        logDebug( "PUBLISH RELEASE - Publishing release to repository. [%s]" );
        ReleaseManager releaseManager = new ReleaseManager( repositoryManager.get() );
        File releaseFile = new File( wipFolder.get(), "/TestRelease_1_0_0.otr" );
        ValidationFindings findings = new ValidationFindings();

        releaseManager.loadRelease( releaseFile, findings );

        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );

        // Publish the release
        ReleaseItem releaseItem = releaseManager.publishRelease( testRepository.get() );

        assertNotNull( releaseItem );
        assertNotNull( releaseItem.getContent() );
        assertNotNull( releaseItem.getRepository() );
        assertEquals( "TestRelease_1_0_0.otr", releaseItem.getFilename() );
        assertEquals( RepositoryItemState.MANAGED_UNLOCKED, releaseItem.getState() );
        assertEquals( TLLibraryStatus.FINAL, releaseItem.getStatus() );
        assertEquals( ReleaseStatus.BETA, releaseManager.getRelease().getStatus() );

        // Attempt to delete the principal library from the repository (should fail)
        if (this.getClass().getName().contains( "Remote" )) { // only test when running non-local
            try {
                RepositoryItem principalLib =
                    releaseItem.getContent().getPrincipalMembers().get( 0 ).getRepositoryItem();
                repositoryManager.get().delete( principalLib );
                fail( "Expected exception not thrown." );

            } catch (RepositoryException e) {
                // Expected exception - no action required
            }
        }
        model.set( releaseManager.getModel() );

        logDebug( "DONE - Success." );
    }

    public void test_13_CompileRelease() throws Exception {
        logDebug( "COMPILE RELEASE - Compiling the repository-managed release. [%s]" );
        List<RepositoryItem> releaseItems =
            testRepository.get().listItems( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package",
                TLLibraryStatus.DRAFT, false, RepositoryItemType.RELEASE );
        File outputFolder = new File( wipFolder.get(), "/compiled-output" );
        CompileAllCompilerTask task = new CompileAllCompilerTask( repositoryManager.get() );
        RepositoryItem releaseRepoItem = null;
        ValidationFindings findings;

        for (RepositoryItem repoItem : releaseItems) {
            if (repoItem.getFilename().equals( "TestRelease_1_0_0.otr" )) {
                releaseRepoItem = repoItem;
                break;
            }
        }
        assertNotNull( releaseRepoItem );

        task.setOutputFolder( outputFolder.getAbsolutePath() );
        findings = task.compileOutput( releaseRepoItem );

        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        model.set( null );

        logDebug( "DONE - Success." );
    }

    public void test_14_CreateServiceAssembly() throws Exception {
        logDebug( "CREATE SERVICE ASSEMBLY - Creating and loading a service assembly. [%s]" );
        File assemblyFile = new File( wipFolder.get(), "/TestAssembly.osm" );
        ServiceAssemblyManager assemblyManager = new ServiceAssemblyManager( repositoryManager.get() );
        ReleaseManager releaseManager = new ReleaseManager( repositoryManager.get() );
        ServiceAssembly assembly = assemblyManager.newAssembly(
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/assemblies", "TestAssembly", "1.0.0", assemblyFile );
        ServiceAssemblyMember providerMember = new ServiceAssemblyMember();
        ServiceAssemblyMember consumerMember = new ServiceAssemblyMember();
        RepositoryItem releaseItem = loadManagedRelease( "TestRelease_1_0_0.otr", releaseManager );
        TLModel consumerModel, providerModel, implementationModel;
        ValidationFindings findings = new ValidationFindings();

        providerMember.setReleaseItem( releaseItem );
        providerMember.setResourceName(
            new QName( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2", "SampleResource" ) );
        consumerMember.setReleaseItem( releaseItem );
        assembly.addProviderApi( providerMember );
        assembly.addConsumerApi( providerMember );
        assemblyManager.saveAssembly( assembly );

        assembly = assemblyManager.loadAssembly( assemblyFile, findings );
        assertFalse( findings.hasFinding( FindingType.ERROR ) );

        consumerModel = assemblyManager.loadConsumerModel( assembly, findings = new ValidationFindings() );
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertEquals( 4, consumerModel.getUserDefinedLibraries().size() );

        providerModel = assemblyManager.loadProviderModel( assembly, findings = new ValidationFindings() );
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertEquals( 4, providerModel.getUserDefinedLibraries().size() );

        implementationModel = assemblyManager.loadImplementationModel( assembly, findings = new ValidationFindings() );
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertEquals( 4, implementationModel.getUserDefinedLibraries().size() );

        logDebug( "DONE - Success." );
    }

    public void test_15_PublishServiceAssembly() throws Exception {
        logDebug( "PUBLISH SERVICE ASSEMBLY - Publishing a service assembly. [%s]" );
        File assemblyFile = new File( wipFolder.get(), "/TestAssembly.osm" );
        ServiceAssemblyManager assemblyManager = new ServiceAssemblyManager( repositoryManager.get() );
        ValidationFindings findings = new ValidationFindings();
        ServiceAssembly assembly = assemblyManager.loadAssembly( assemblyFile, findings );
        ServiceAssemblyItem assemblyItem;

        assertFalse( findings.hasFinding( FindingType.ERROR ) );

        assemblyItem = assemblyManager.publishAssembly( assembly, testRepository.get() );
        assertNotNull( assemblyItem );
        assertEquals( assembly.getNamespace(), assemblyItem.getNamespace() );
        assertEquals( assembly.getBaseNamespace(), assemblyItem.getBaseNamespace() );
        assertEquals( assembly.getName(), assemblyItem.getLibraryName() );
        assertEquals( assembly.getVersion(), assemblyItem.getVersion() );
        assertEquals( assembly, assemblyItem.getContent() );

        // Attempt to delete the release from the repository (should fail)
        if (this.getClass().getName().contains( "Remote" )) { // only test when running non-local
            try {
                RepositoryItem releaseItem = assembly.getAllApis().get( 0 ).getReleaseItem();

                repositoryManager.get().delete( releaseItem );
                fail( "Expected exception not thrown." );

            } catch (RepositoryException e) {
                // Expected exception - no action required
            }
        }

        // List the assemblies in the repository's namespace and be sure that the one we just published is returned
        List<RepositoryItem> assemblyItemList = testRepository.get().listItems( assembly.getBaseNamespace(),
            TLLibraryStatus.FINAL, false, RepositoryItemType.ASSEMBLY );

        assertEquals( 1, assemblyItemList.size() );
        assertEquals( "TestAssembly_1_0_0.osm", assemblyItemList.get( 0 ).getFilename() );

        // Load the assembly using the repository item we just retrieved
        ServiceAssembly reloadedAssembly =
            assemblyManager.loadAssembly( assemblyItemList.get( 0 ), findings = new ValidationFindings() );

        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertEquals( assembly.getNamespace(), reloadedAssembly.getNamespace() );
        assertEquals( assembly.getBaseNamespace(), reloadedAssembly.getBaseNamespace() );
        assertEquals( assembly.getName(), reloadedAssembly.getName() );
        assertEquals( assembly.getVersion(), reloadedAssembly.getVersion() );

        logDebug( "DONE - Success." );
    }

    public void test_16_UnpublishServiceAssembly() throws Exception {
        logDebug( "UNPUBLISH SERVICE ASSEMBLY - Unpublishing a service assembly. [%s]" );
        ServiceAssemblyManager assemblyManager = new ServiceAssemblyManager( repositoryManager.get() );
        RepositoryItem assemblyItem = repositoryManager.get().getRepositoryItem(
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/assemblies", "TestAssembly_1_0_0.osm", "1.0.0" );
        File saveFile = new File( wipFolder.get(), "TestAssembly_1_0_0.osm" );

        assertNotNull( assemblyItem );
        assemblyManager.unpublishAssembly( assemblyItem, wipFolder.get() );
        assertTrue( saveFile.exists() );

        logDebug( "DONE - Success." );
    }

    public void test_17_NewReleaseVersion() throws Exception {
        logDebug( "NEW RELEASE VERSION - Creating new version of a release. [%s]" );
        ValidationFindings findings = new ValidationFindings();
        ReleaseManager releaseManager = new ReleaseManager( repositoryManager.get() );
        ReleaseManager newVersionReleaseManager;

        loadManagedRelease( "TestRelease_1_0_0.otr", releaseManager );
        newVersionReleaseManager = releaseManager.newVersion( wipFolder.get(), findings );

        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );

        Set<String> principalFilenames =
            getReleaseItemFilenames( newVersionReleaseManager.getRelease().getPrincipalMembers() );
        Set<String> referencedFilenames =
            getReleaseItemFilenames( newVersionReleaseManager.getRelease().getReferencedMembers() );

        assertNotNull( newVersionReleaseManager );
        assertNotNull( newVersionReleaseManager.getRelease() );
        assertEquals( releaseManager.getRelease().getBaseNamespace(),
            newVersionReleaseManager.getRelease().getBaseNamespace() );
        assertEquals( releaseManager.getRelease().getName(), newVersionReleaseManager.getRelease().getName() );
        assertEquals( releaseManager.getRelease().getDefaultEffectiveDate(),
            newVersionReleaseManager.getRelease().getDefaultEffectiveDate() );
        assertEquals( "2.0.0", newVersionReleaseManager.getRelease().getVersion() );
        assertEquals( ReleaseStatus.DRAFT, newVersionReleaseManager.getRelease().getStatus() );

        assertEquals( 1, principalFilenames.size() );
        assertEquals( 3, referencedFilenames.size() );
        assertTrue( principalFilenames.contains( "library_1_p2_2_0_0.otm" ) );
        assertTrue( referencedFilenames.contains( "library_2_p2_2_0_0.otm" ) );
        assertTrue( referencedFilenames.contains( "library_2_p1_1_0_0.otm" ) );
        assertTrue( referencedFilenames.contains( "library_1_p1_1_0_0.otm" ) );
        model.set( releaseManager.getModel() );

        logDebug( "DONE - Success." );
    }

    public void test_18_UnpublishRelease() throws Exception {
        logDebug( "UNPUBLISH RELEASE - Unpublishing a release from the repository. [%s]" );
        ReleaseManager releaseManager = new ReleaseManager( repositoryManager.get() );
        ReleaseManager localReleaseManager;

        loadManagedRelease( "TestRelease_1_0_0.otr", releaseManager );
        localReleaseManager = releaseManager.unpublishRelease( wipFolder.get() );

        assertNotNull( localReleaseManager );
        assertNotNull( localReleaseManager.getRelease() );
        assertNotNull( localReleaseManager.getRelease().getReleaseUrl() );
        assertTrue( URLUtils.isFileURL( localReleaseManager.getRelease().getReleaseUrl() ) );
        assertEquals( wipFolder.get(),
            URLUtils.toFile( localReleaseManager.getRelease().getReleaseUrl() ).getParentFile() );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package",
            localReleaseManager.getRelease().getBaseNamespace() );
        assertEquals( "TestRelease", localReleaseManager.getRelease().getName() );
        assertNotNull( localReleaseManager.getRelease().getDefaultEffectiveDate() );
        assertEquals( "1.0.0", localReleaseManager.getRelease().getVersion() );
        assertEquals( ReleaseStatus.DRAFT, localReleaseManager.getRelease().getStatus() );

        // Attempt to search for the unpublished release (should return zero results)
        List<RepositorySearchResult> searchResults =
            repositoryManager.get().search( "TestRelease", TLLibraryStatus.DRAFT, false, RepositoryItemType.RELEASE );

        assertEquals( 0, searchResults.size() );
        model.set( releaseManager.getModel() );

        logDebug( "DONE - Success." );
    }

    private Set<String> getReleaseItemFilenames(List<ReleaseMember> memberList) {
        Set<String> filenames = new HashSet<>();

        for (ReleaseMember member : memberList) {
            filenames.add( member.getRepositoryItem().getFilename() );
        }
        return filenames;
    }

    private ReleaseItem loadManagedRelease(String releaseFilename, ReleaseManager releaseManager) throws Exception {
        List<RepositoryItem> releaseItems =
            testRepository.get().listItems( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package",
                TLLibraryStatus.DRAFT, false, RepositoryItemType.RELEASE );
        ValidationFindings findings = new ValidationFindings();
        RepositoryItem releaseRepoItem = null;
        ReleaseItem releaseItem;

        for (RepositoryItem repoItem : releaseItems) {
            if (repoItem.getFilename().equals( releaseFilename )) {
                releaseRepoItem = repoItem;
                break;
            }
        }
        assertNotNull( releaseRepoItem );
        releaseItem = releaseManager.loadRelease( releaseRepoItem, findings );

        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertNotNull( releaseManager.getRelease() );
        assertNotNull( releaseManager.getModel() );
        assertEquals( ReleaseStatus.BETA, releaseManager.getRelease().getStatus() );
        assertEquals( 4, releaseManager.getModel().getUserDefinedLibraries().size() );

        return releaseItem;
    }

    public void test_19_DeleteLibrary() throws Exception {
        logDebug( "DELETE - Delete a managed project item. [%s]" );
        ProjectManager projectManager = new ProjectManager( new TLModel(), true, repositoryManager.get() );
        File projectFile = new File( wipFolder.get(), "/projects/project_1.xml" );

        if (!projectFile.exists()) {
            throw new FileNotFoundException( "Test File Not Found: " + projectFile.getAbsolutePath() );
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject( projectFile, findings );
        ProjectItem projectItem = findProjectItem( project, "library_1_p2_2_0_0.otm" );

        // Verify that the project loaded correctly
        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertNotNull( projectItem );

        repositoryManager.get().delete( projectItem );

        try {
            repositoryManager.get().getRepositoryItem( projectItem.getBaseNamespace(), projectItem.getFilename(),
                projectItem.getVersion() );
            Assert.fail( "Expected Repository Exception - Not Thrown" );

        } catch (RepositoryException e) {
            // No error - exception was expected, so the test passed
        }
        model.set( projectManager.getModel() );

        logDebug( "DONE - Success." );
    }

    public void test_20_CreateNamespace() throws Exception {
        logDebug( "CREATE NAMESPACE - Create a managed namespace item. [%s]" );
        String managedNS = "http://www.OpenTravel.org/ns/Test-NS";
        List<String> repositoryNamespaces = testRepository.get().listBaseNamespaces();

        // Make sure the namespace does not exist yet
        assertFalse( repositoryNamespaces.contains( managedNS ) );

        // Create the namespace and verify that it exists
        testRepository.get().createNamespace( managedNS );
        repositoryNamespaces = testRepository.get().listBaseNamespaces();
        assertTrue( repositoryNamespaces.contains( managedNS ) );
        model.set( null );

        logDebug( "DONE - Success." );
    }

    public void test_20a_CreateNamespaceError() throws Exception {
        logDebug( "CREATE NAMESPACE (Error Test) - Attempt to create conflicting namespace. [%s]" );
        String managedNS = "http://www.OpenTravel.org/NS/Test-NS/ns2"; // case-sensitive conflict with test_10
        List<String> repositoryNamespaces = testRepository.get().listBaseNamespaces();

        // Make sure the namespace does not exist yet
        assertFalse( repositoryNamespaces.contains( managedNS ) );

        // Attempt to create the namespace (should fail with RepositoryException)
        try {
            testRepository.get().createNamespace( managedNS );
            fail( "Expected exception not thrown." );

        } catch (RepositoryException e) {
            // No action - failure is the expected result
        }
        repositoryNamespaces = testRepository.get().listBaseNamespaces();
        assertFalse( repositoryNamespaces.contains( managedNS ) );
        model.set( null );

        logDebug( "DONE - Success." );
    }

    public void test_21_ListNamespaceChildren() throws Exception {
        logDebug( "LIST NAMESPACE CHILDREN - Find the children of a managed namespace. [%s]" );
        List<String> nsChildren = testRepository.get().listNamespaceChildren( "http://www.OpenTravel.org/ns" );

        assertEquals( 2, nsChildren.size() );
        assertTrue( nsChildren.contains( "OTA2" ) );
        assertTrue( nsChildren.contains( "Test-NS" ) );

        logDebug( "DONE - Success." );
    }

    public void test_22_DeleteNamespace() throws Exception {
        logDebug( "DELETE NAMESPACE - Delete a managed namespace item. [%s]" );
        String managedNS = "http://www.OpenTravel.org/ns/Test-NS";
        List<String> repositoryNamespaces = testRepository.get().listBaseNamespaces();

        // Make sure the namespace exists (it was created during the previous operation)
        assertTrue( repositoryNamespaces.contains( managedNS ) );

        // Delete the namespace and verify that it no longer exists
        testRepository.get().deleteNamespace( managedNS );
        repositoryNamespaces = testRepository.get().listBaseNamespaces();
        assertFalse( repositoryNamespaces.contains( managedNS ) );
        model.set( null );

        logDebug( "DONE - Success." );
    }

    public void test_23_CreateRootNamespace() throws Exception {
        logDebug( "CREATE ROOT NAMESPACE - Create a managed namespace item. [%s]" );
        String rootNS = "http://www.testnamespace.com";
        List<String> rootNamespaces = testRepository.get().listRootNamespaces();

        // Make sure the namespace does not exist yet
        assertFalse( rootNamespaces.contains( rootNS ) );

        // Create the namespace and verify that it exists
        testRepository.get().createRootNamespace( rootNS );
        rootNamespaces = testRepository.get().listRootNamespaces();
        assertTrue( rootNamespaces.contains( rootNS ) );

        // Attempt to create some conflicting namespaces and verify that exceptions are thrown
        String[] conflictingRootNamespaces =
            new String[] {"http://www.testnamespace.com/ns1", "http://testnamespace.com"};

        for (String conflictingRootNS : conflictingRootNamespaces) {
            try {
                testRepository.get().createRootNamespace( conflictingRootNS );
                fail( "Able to create conflicting root namespace: " + conflictingRootNS );

            } catch (RepositoryException e) {
                // Expected exception - test passed
            }
        }

        // Create a child namespace to ensure the root is configured properly
        String childNS = rootNS + "/test";

        testRepository.get().createNamespace( childNS );
        testRepository.get().deleteNamespace( childNS );
        model.set( null );

        logDebug( "DONE - Success." );
    }

    public void test_24_DeleteRootNamespace() throws Exception {
        logDebug( "DELETE ROOT NAMESPACE - Delete a managed namespace item. [%s]" );
        String rootNS = "http://www.testnamespace.com";
        List<String> rootNamespaces = testRepository.get().listRootNamespaces();

        // Make sure the namespace exists (it was created during the previous operation)
        assertTrue( rootNamespaces.contains( rootNS ) );

        // Delete the namespace and verify that it no longer exists
        testRepository.get().deleteRootNamespace( rootNS );
        rootNamespaces = testRepository.get().listRootNamespaces();
        assertFalse( rootNamespaces.contains( rootNS ) );
        model.set( null );

        logDebug( "DONE - Success." );
    }

    protected void logDebug(String message) {
        if (DEBUG) {
            System.out.println( String.format( message, getClass().getSimpleName() ) );
        }
    }

    protected CompileAllTaskOptions getReleaseCompileOptions() {
        return new CompileAllTaskOptions() {
            public String getResourceBaseUrl() {
                return "http://www.opentravel.org/resources";
            }

            public URL getServiceLibraryUrl() {
                return null;
            }

            public String getServiceEndpointUrl() {
                return "http://www.opentravel.org/services";
            }

            public boolean isSuppressOptionalFields() {
                return false;
            }

            public boolean isGenerateMaxDetailsForExamples() {
                return true;
            }

            public boolean isGenerateExamples() {
                return true;
            }

            public Integer getExampleMaxRepeat() {
                return 2;
            }

            public Integer getExampleMaxDepth() {
                return 2;
            }

            public String getExampleContext() {
                return null;
            }

            public String getOutputFolder() {
                return null;
            }

            public String getCatalogLocation() {
                return null;
            }

            public boolean isSuppressOtmExtensions() {
                return false;
            }

            public boolean isCompileSwagger() {
                return true;
            }

            public boolean isCompileServices() {
                return true;
            }

            public boolean isCompileSchemas() {
                return true;
            }

            public boolean isCompileJsonSchemas() {
                return true;
            }

            public boolean isCompileHtml() {
                return true;
            }

            public void applyTaskOptions(CommonCompilerTaskOptions taskOptions) {}
        };
    }

}

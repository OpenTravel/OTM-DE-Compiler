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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.opentravel.schemacompiler.ic.ModelIntegrityChecker;
import org.opentravel.schemacompiler.loader.LoaderConstants;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;

/**
 * Verifies the operation of the <code>LibraryProjectLoader</code>.
 * 
 * @author S. Livezey
 */
public class TestProjectManager {

    public static final String PACKAGE_2_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2";
    public static final String PACKAGE_3_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v3";

    @Test
    public void testLoadUnmanagedProjectItems() throws Exception {
        File projectTestFolder = new File( SchemaCompilerTestUtils.getBaseProjectLocation() );
        File projectFile = new File( projectTestFolder, "/project_1.xml" );
        assertTrue( projectFile.exists() );
        ValidationFindings findings = new ValidationFindings();
        ProjectManager projectManager = new ProjectManager( false );
        Project project = projectManager.loadProject( projectFile, findings );
        SchemaCompilerTestUtils.printFindings( findings, FindingType.ERROR );

        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertEquals( "http://www.OpenTravel.org/projects/test/p1", project.getProjectId() );
        assertEquals( "Test Project #1", project.getName() );
        assertEquals( "Test description for the project.", project.getDescription() );

        // Verify that all of the required project-items were loaded
        List<String> globalProjectItems = getProjectItemNames( projectManager );
        List<String> localProjectItems = getProjectItemNames( project );

        assertTrue( globalProjectItems.contains( "library_1_p1.xml" ) );
        assertTrue( globalProjectItems.contains( "library_2_p1.xml" ) );
        assertTrue( globalProjectItems.contains( "library_1_p2.xml" ) );
        assertTrue( globalProjectItems.contains( "library_2_p2.xml" ) );
        assertFalse( globalProjectItems.contains( "library_3_ext.xml" ) );

        assertEquals( 4, localProjectItems.size() );
        assertTrue( localProjectItems.contains( "library_1_p1.xml" ) );
        assertTrue( localProjectItems.contains( "library_2_p1.xml" ) );
        assertTrue( localProjectItems.contains( "library_1_p2.xml" ) );
        assertTrue( localProjectItems.contains( "library_2_p2.xml" ) );
    }

    @Test
    public void testNewProjectAddUnmanagedProjectItems() throws Exception {
        File libraryFile =
            new File( SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package_v2/library_1_p2.xml" );
        File projectTestFolder = new File( SchemaCompilerTestUtils.getTestProjectLocation() );
        File projectFile = new File( projectTestFolder, "/test_project.xml" );
        ProjectManager projectManager = new ProjectManager( false );
        Project project = projectManager.newProject( projectFile, "http://www.OpenTravel.org/projects/test/t1",
            "Test Project", "Description of the test project" );

        projectManager.addUnmanagedProjectItem( libraryFile, project );

        // Verify that all of the required project-items were loaded
        List<String> globalProjectItems = getProjectItemNames( projectManager );
        List<String> localProjectItems = getProjectItemNames( project );

        assertTrue( globalProjectItems.contains( "library_1_p1.xml" ) );
        assertTrue( globalProjectItems.contains( "library_2_p1.xml" ) );
        assertTrue( globalProjectItems.contains( "library_1_p2.xml" ) );
        assertTrue( globalProjectItems.contains( "library_2_p2.xml" ) );
        assertFalse( globalProjectItems.contains( "library_3_ext.xml" ) );

        assertEquals( 4, localProjectItems.size() );
        assertTrue( localProjectItems.contains( "library_1_p1.xml" ) );
        assertTrue( localProjectItems.contains( "library_2_p1.xml" ) );
        assertTrue( localProjectItems.contains( "library_1_p2.xml" ) );
        assertTrue( localProjectItems.contains( "library_2_p2.xml" ) );
    }

    @Test
    public void testOpenMultipleProjects() throws Exception {
        File libraryFile =
            new File( SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package_v1/library_1_p1.xml" );
        File projectBaseFolder = new File( SchemaCompilerTestUtils.getBaseProjectLocation() );
        File projectTestFolder = new File( SchemaCompilerTestUtils.getTestProjectLocation() );
        File project1File = new File( projectBaseFolder, "/project_1.xml" );
        File project2File = new File( projectTestFolder, "/test_project.xml" );
        ProjectManager projectManager = new ProjectManager( false );
        Project project1 = projectManager.loadProject( project1File );
        Project project2 = projectManager.newProject( project2File, "http://www.OpenTravel.org/projects/test/t1",
            "Test Project", "Description of the test project" );

        projectManager.addUnmanagedProjectItem( libraryFile, project2 );

        // Verify that all of the required project-items were loaded
        List<String> globalProjectItems = getProjectItemNames( projectManager );
        List<String> project1Items = getProjectItemNames( project1 );
        List<String> project2Items = getProjectItemNames( project2 );

        assertTrue( globalProjectItems.contains( "library_1_p1.xml" ) );
        assertTrue( globalProjectItems.contains( "library_2_p1.xml" ) );
        assertTrue( globalProjectItems.contains( "library_1_p2.xml" ) );
        assertTrue( globalProjectItems.contains( "library_2_p2.xml" ) );
        assertFalse( globalProjectItems.contains( "library_3_ext.xml" ) );

        assertEquals( 4, project1Items.size() );
        assertTrue( project1Items.contains( "library_1_p1.xml" ) );
        assertTrue( project1Items.contains( "library_2_p1.xml" ) );
        assertTrue( project1Items.contains( "library_1_p2.xml" ) );
        assertTrue( project1Items.contains( "library_2_p2.xml" ) );

        assertEquals( 1, project2Items.size() );
        assertTrue( project2Items.contains( "library_1_p1.xml" ) );
    }

    @Test
    public void testSaveProject() throws Exception {
        File libraryFile =
            new File( SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package_v2/library_1_p2.xml" );
        File projectTestFolder = new File( SchemaCompilerTestUtils.getTestProjectLocation() );
        File projectFile = new File( projectTestFolder, "/test_project.xml" );
        ProjectManager projectManager = new ProjectManager( false );
        Project project = projectManager.newProject( projectFile, "http://www.OpenTravel.org/projects/test/t1",
            "Test Project", "Description of the test project" );
        ValidationFindings saveFindings = new ValidationFindings();

        projectManager.addUnmanagedProjectItem( libraryFile, project );

        for (ProjectItem item : project.getProjectItems()) {
            item.getContent()
                .setLibraryUrl( URLUtils.toURL( new File( projectTestFolder, "/" + item.getFilename() ) ) );
        }
        projectManager.saveProject( project, saveFindings );
        SchemaCompilerTestUtils.printFindings( saveFindings );
        assertFalse( saveFindings.hasFinding() );

        projectManager.closeAll();
        project = null;

        // Re-load the file we just finished saving
        ValidationFindings loadFindings = new ValidationFindings();
        project = projectManager.loadProject( projectFile, loadFindings );
        SchemaCompilerTestUtils.printFindings( loadFindings );
        assertFalse( loadFindings.hasFinding( FindingType.ERROR ) );

        // Verify that all of the required project-items were included in the file we just re-loaded
        List<String> globalProjectItems = getProjectItemNames( projectManager );
        List<String> localProjectItems = getProjectItemNames( project );

        assertTrue( globalProjectItems.contains( "library_1_p1.xml" ) );
        assertTrue( globalProjectItems.contains( "library_2_p1.xml" ) );
        assertTrue( globalProjectItems.contains( "library_1_p2.xml" ) );
        assertTrue( globalProjectItems.contains( "library_2_p2.xml" ) );
        assertFalse( globalProjectItems.contains( "library_3_ext.xml" ) );

        assertEquals( 4, localProjectItems.size() );
        assertTrue( localProjectItems.contains( "library_1_p1.xml" ) );
        assertTrue( localProjectItems.contains( "library_2_p1.xml" ) );
        assertTrue( localProjectItems.contains( "library_1_p2.xml" ) );
        assertTrue( localProjectItems.contains( "library_2_p2.xml" ) );
    }

    @Test
    public void testRemoveProjectItem() throws Exception {
        File projectTestFolder = new File( SchemaCompilerTestUtils.getBaseProjectLocation() );
        File projectFile = new File( projectTestFolder, "/project_1.xml" );
        File libraryFile = new File( projectTestFolder, "../libraries_1_5/test-package_v2/library_3_ext.xml" );
        ValidationFindings findings = new ValidationFindings();
        ProjectManager projectManager = new ProjectManager( false );
        Project project = projectManager.loadProject( projectFile );
        List<ProjectItem> items =
            projectManager.addUnmanagedProjectItems( Arrays.asList( new File[] {libraryFile} ), project, findings );

        SchemaCompilerTestUtils.printFindings( findings );
        assertFalse( findings.hasFinding( FindingType.ERROR ) );
        assertTrue( getProjectItemNames( projectManager ).contains( "library_3_ext.xml" ) );
        assertTrue( getProjectItemNames( project ).contains( "library_3_ext.xml" ) );

        assertEquals( 1, items.size() );
        project.remove( items.get( 0 ) );

        assertFalse( getProjectItemNames( projectManager ).contains( "library_3_ext.xml" ) );
        assertFalse( getProjectItemNames( project ).contains( "library_3_ext.xml" ) );
    }

    @Test
    public void testCloseProject() throws Exception {
        File projectTestFolder = new File( SchemaCompilerTestUtils.getBaseProjectLocation() );
        File projectFile = new File( projectTestFolder, "/project_1.xml" );
        ProjectManager projectManager = new ProjectManager( false );
        Project project = projectManager.loadProject( projectFile );

        assertTrue( projectManager.getAllProjects().contains( project ) );

        projectManager.closeProject( project );
        assertFalse( projectManager.getAllProjects().contains( project ) );

        // Ensure that all of the remaining project items are part of the built-in project
        for (ProjectItem item : projectManager.getAllProjectItems()) {
            assertTrue( item.getContent() instanceof BuiltInLibrary );
        }
    }

    @Test
    public void testAddProjectDependencyViaTypeAssignment() throws Exception {
        File projectTestFolder = new File( SchemaCompilerTestUtils.getBaseProjectLocation() );
        File project1File = new File( projectTestFolder, "/project_1.xml" );
        File project2File = new File( projectTestFolder, "/project_2.xml" );
        ProjectManager projectManager = new ProjectManager( false );
        Project project1 = projectManager.loadProject( project1File );
        Project project2 = projectManager.loadProject( project2File );
        TLLibrary library2p2 = (TLLibrary) projectManager.getModel().getLibrary( PACKAGE_2_NAMESPACE, "library_2_p2" );
        TLLibrary sampleLibrary =
            (TLLibrary) projectManager.getModel().getLibrary( PACKAGE_3_NAMESPACE, "sample_library" );
        TLSimple existingSimpleType = library2p2.getSimpleType( "Counter_4" );
        TLSimple newSimpleType = new TLSimple();

        List<String> localProjectItems = getProjectItemNames( project2 );

        assertEquals( 2, project2.getProjectItems().size() );
        assertTrue( localProjectItems.contains( "sample_library.xml" ) );
        assertTrue( localProjectItems.contains( "included_library.xml" ) );

        assertTrue( projectManager.getAllProjects().contains( project1 ) );
        assertTrue( projectManager.getAllProjects().contains( project2 ) );

        assertNotNull( existingSimpleType );
        projectManager.getModel().addListener( new ModelIntegrityChecker() );

        // Create a new library dependency by constructing a new simple type and adding it to a
        // library
        newSimpleType.setName( "TestSimpleType" );
        newSimpleType.setParentType( existingSimpleType );
        sampleLibrary.addNamedMember( newSimpleType );

        // Verify that the project item was created by the type assignment action
        localProjectItems = getProjectItemNames( project2 );
        assertEquals( 6, project2.getProjectItems().size() );
        assertTrue( localProjectItems.contains( "sample_library.xml" ) );
        assertTrue( localProjectItems.contains( "included_library.xml" ) );
        assertTrue( localProjectItems.contains( "library_1_p1.xml" ) );
        assertTrue( localProjectItems.contains( "library_1_p2.xml" ) );
        assertTrue( localProjectItems.contains( "library_2_p2.xml" ) ); // added via type assignment
        assertTrue( localProjectItems.contains( "library_2_p1.xml" ) ); // added because of dependency
                                                                        // on type assignment
    }

    @Test
    public void testLoadManagedProjectItem_unknownRepository() throws Exception {
        RepositoryManager mockRepositoryManager = mock( RepositoryManager.class );
        Set<String> messageKeys;

        when( mockRepositoryManager.getRepository( "mock-repository" ) ).thenReturn( null );
        messageKeys = loadManagedProject( mockRepositoryManager );

        assertEquals( 1, messageKeys.size() );
        assertTrue( messageKeys.contains( LoaderConstants.ERROR_LOADING_FROM_REPOSITORY ) );
    }

    @Test
    public void testLoadManagedProjectItem_unresolvedRepository() throws Exception {
        RepositoryManager mockRepositoryManager = mock( RepositoryManager.class );
        RepositoryItemImpl mockItem = new RepositoryItemImpl();
        Set<String> messageKeys;

        mockItem.setRepository( null );
        when( mockRepositoryManager.getRepository( "mock-repository" ) ).thenReturn( mockRepositoryManager );
        when( mockRepositoryManager.getRepositoryItem( "http://www.mock-repository.org/testns", "MockLibrary_1_0_0.otm",
            "1.0.0" ) ).thenReturn( mockItem );

        messageKeys = loadManagedProject( mockRepositoryManager );

        assertEquals( 1, messageKeys.size() );
        assertTrue( messageKeys.contains( LoaderConstants.ERROR_MISSING_REPOSITORY ) );
    }

    @Test
    public void testLoadManagedProjectItem_repositoryUnavailable() throws Exception {
        RepositoryManager mockRepositoryManager = mock( RepositoryManager.class );
        RepositoryItemImpl mockItem = new RepositoryItemImpl();
        Set<String> messageKeys;

        mockItem.setRepository( null );
        when( mockRepositoryManager.getRepository( "mock-repository" ) ).thenReturn( mockRepositoryManager );
        when( mockRepositoryManager.getRepositoryItem( "http://www.mock-repository.org/testns", "MockLibrary_1_0_0.otm",
            "1.0.0" ) ).thenThrow( RepositoryUnavailableException.class );

        messageKeys = loadManagedProject( mockRepositoryManager );

        assertEquals( 1, messageKeys.size() );
        assertTrue( messageKeys.contains( LoaderConstants.ERROR_REPOSITORY_UNAVAILABLE ) );
    }

    private Set<String> loadManagedProject(RepositoryManager mockRepositoryManager) throws Exception {
        File projectTestFolder = new File( SchemaCompilerTestUtils.getBaseProjectLocation() );
        File projectFile = new File( projectTestFolder, "/project_4.xml" );
        ProjectManager projectManager = new ProjectManager( new TLModel(), false, mockRepositoryManager );
        ValidationFindings findings = new ValidationFindings();

        projectManager.loadProject( projectFile, findings, null );
        return getFindingMessageKeys( findings );
    }

    @Test
    public void testGetVersionChain() throws Exception {
        File projectTestFolder = new File( SchemaCompilerTestUtils.getBaseProjectLocation() );
        File projectFile = new File( projectTestFolder, "/project_1.xml" );
        ProjectManager projectManager = new ProjectManager( false );

        projectManager.loadProject( projectFile );

        AbstractLibrary tlLibrary = projectManager.getModel()
            .getLibrary( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2", "library_1_p2" );
        AbstractLibrary biLibrary =
            projectManager.getModel().getLibrary( XMLConstants.W3C_XML_SCHEMA_NS_URI, "XMLSchema" );
        ProjectItem tlItem = projectManager.getProjectItem( tlLibrary );
        ProjectItem biItem = projectManager.getProjectItem( biLibrary );

        assertEquals( 1, projectManager.getVersionChain( tlItem ).size() );
        assertEquals( 1, projectManager.getVersionChain( biItem ).size() );
        assertEquals( 0, projectManager.getVersionChain( null ).size() );
    }

    @Test(expected = RepositoryException.class)
    public void testAddUnmanagedProjectItem_unsavedLibrary() throws Exception {
        File projectTestFolder = new File( SchemaCompilerTestUtils.getBaseProjectLocation() );
        File projectFile = new File( projectTestFolder, "/project_1.xml" );
        ProjectManager projectManager = new ProjectManager( false );
        Project project = projectManager.loadProject( projectFile );
        TLLibrary unsavedLibrary = new TLLibrary();

        projectManager.addUnmanagedProjectItem( unsavedLibrary, project );
    }

    @Test(expected = RepositoryException.class)
    public void testAddUnmanagedProjectItem_managedLibrary() throws Exception {
        File projectTestFolder = new File( SchemaCompilerTestUtils.getBaseProjectLocation() );
        File projectFile = new File( projectTestFolder, "/project_1.xml" );
        ProjectManager projectManager = new ProjectManager( false );
        Project project = projectManager.loadProject( projectFile );
        File repositoryLocation = projectManager.getRepositoryManager().getRepositoryLocation();
        File libraryLocation = new File( repositoryLocation, "/managed-library_1_0_0.otm" );
        TLLibrary managedLibrary = new TLLibrary();

        managedLibrary.setLibraryUrl( URLUtils.toURL( libraryLocation ) );
        projectManager.addUnmanagedProjectItem( managedLibrary, project );
    }

    @Test
    public void testAddUnmanagedProjectItem_existingLibrary() throws Exception {
        File projectTestFolder = new File( SchemaCompilerTestUtils.getBaseProjectLocation() );
        File projectFile = new File( projectTestFolder, "/project_1.xml" );
        ProjectManager projectManager = new ProjectManager( false );
        Project project = projectManager.loadProject( projectFile );
        int originalItemCount = projectManager.getAllProjectItems().size();
        ProjectItem item = project.getProjectItems().get( 0 );

        projectManager.addUnmanagedProjectItem( item.getContent(), project );
        assertEquals( originalItemCount, projectManager.getAllProjectItems().size() );
    }

    private List<String> getProjectItemNames(ProjectManager projectManager) {
        List<String> projectItemNames = new ArrayList<String>();

        for (ProjectItem item : projectManager.getAllProjectItems()) {
            projectItemNames.add( item.getFilename() );
        }
        return projectItemNames;
    }

    private List<String> getProjectItemNames(Project project) {
        List<String> projectItemNames = new ArrayList<String>();

        for (ProjectItem item : project.getProjectItems()) {
            projectItemNames.add( item.getFilename() );
        }
        return projectItemNames;
    }

    protected Set<String> getFindingMessageKeys(ValidationFindings f) {
        Set<String> messageKeys = new HashSet<>();

        for (ValidationFinding finding : f.getAllFindingsAsList()) {
            messageKeys.add( finding.getMessageKey() );
        }
        return messageKeys;
    }

}

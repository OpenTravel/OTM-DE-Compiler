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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.sabre.schemacompiler.ic.ModelIntegrityChecker;
import com.sabre.schemacompiler.model.BuiltInLibrary;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.util.SchemaCompilerTestUtils;
import com.sabre.schemacompiler.util.URLUtils;
import com.sabre.schemacompiler.validate.FindingMessageFormat;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;

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
		File projectFile = new File(projectTestFolder, "/project_1.xml");
		assertTrue(projectFile.exists());
		ValidationFindings findings = new ValidationFindings();
		ProjectManager projectManager = new ProjectManager( false );
		Project project = projectManager.loadProject(projectFile, findings);
		SchemaCompilerTestUtils.printFindings(findings, FindingType.ERROR);
		
		assertFalse(findings.hasFinding(FindingType.ERROR));
		assertEquals("http://www.OpenTravel.org/projects/test/p1", project.getProjectId());
		assertEquals("Test Project #1", project.getName());
		assertEquals("Test description for the project.", project.getDescription());
		
		// Verify that all of the required project-items were loaded
		List<String> globalProjectItems = getProjectItemNames(projectManager);
		List<String> localProjectItems = getProjectItemNames(project);
		
		assertTrue(globalProjectItems.contains("library_1_p1.xml"));
		assertTrue(globalProjectItems.contains("library_2_p1.xml"));
		assertTrue(globalProjectItems.contains("library_1_p2.xml"));
		assertTrue(globalProjectItems.contains("library_2_p2.xml"));
		assertFalse(globalProjectItems.contains("library_3_ext.xml"));
		
		assertEquals(4, localProjectItems.size());
		assertTrue(localProjectItems.contains("library_1_p1.xml"));
		assertTrue(localProjectItems.contains("library_2_p1.xml"));
		assertTrue(localProjectItems.contains("library_1_p2.xml"));
		assertTrue(localProjectItems.contains("library_2_p2.xml"));
	}
	
	@Test
	public void testNewProjectAddUnmanagedProjectItems() throws Exception {
		File libraryFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package_v2/library_1_p2.xml");
		File projectTestFolder = new File( SchemaCompilerTestUtils.getTestProjectLocation() );
		File projectFile = new File(projectTestFolder, "/test_project.xml");
		ProjectManager projectManager = new ProjectManager( false );
		Project project = projectManager.newProject(projectFile,
				"http://www.OpenTravel.org/projects/test/t1", "Test Project", "Description of the test project");
		
		projectManager.addUnmanagedProjectItem(libraryFile, project);
		
		// Verify that all of the required project-items were loaded
		List<String> globalProjectItems = getProjectItemNames(projectManager);
		List<String> localProjectItems = getProjectItemNames(project);
		
		assertTrue(globalProjectItems.contains("library_1_p1.xml"));
		assertTrue(globalProjectItems.contains("library_2_p1.xml"));
		assertTrue(globalProjectItems.contains("library_1_p2.xml"));
		assertTrue(globalProjectItems.contains("library_2_p2.xml"));
		assertFalse(globalProjectItems.contains("library_3_ext.xml"));
		
		assertEquals(4, localProjectItems.size());
		assertTrue(localProjectItems.contains("library_1_p1.xml"));
		assertTrue(localProjectItems.contains("library_2_p1.xml"));
		assertTrue(localProjectItems.contains("library_1_p2.xml"));
		assertTrue(localProjectItems.contains("library_2_p2.xml"));
	}
	
	@Test
	public void testOpenMultipleProjects() throws Exception {
		File libraryFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package_v1/library_1_p1.xml");
		File projectBaseFolder = new File( SchemaCompilerTestUtils.getBaseProjectLocation() );
		File projectTestFolder = new File( SchemaCompilerTestUtils.getTestProjectLocation() );
		File project1File = new File(projectBaseFolder, "/project_1.xml");
		File project2File = new File(projectTestFolder, "/test_project.xml");
		ProjectManager projectManager = new ProjectManager( false );
		Project project1 = projectManager.loadProject(project1File);
		Project project2 = projectManager.newProject(project2File,
				"http://www.OpenTravel.org/projects/test/t1", "Test Project", "Description of the test project");
		
		projectManager.addUnmanagedProjectItem(libraryFile, project2);
		
		// Verify that all of the required project-items were loaded
		List<String> globalProjectItems = getProjectItemNames(projectManager);
		List<String> project1Items = getProjectItemNames(project1);
		List<String> project2Items = getProjectItemNames(project2);
		
		assertTrue(globalProjectItems.contains("library_1_p1.xml"));
		assertTrue(globalProjectItems.contains("library_2_p1.xml"));
		assertTrue(globalProjectItems.contains("library_1_p2.xml"));
		assertTrue(globalProjectItems.contains("library_2_p2.xml"));
		assertFalse(globalProjectItems.contains("library_3_ext.xml"));
		
		assertEquals(4, project1Items.size());
		assertTrue(project1Items.contains("library_1_p1.xml"));
		assertTrue(project1Items.contains("library_2_p1.xml"));
		assertTrue(project1Items.contains("library_1_p2.xml"));
		assertTrue(project1Items.contains("library_2_p2.xml"));
		
		assertEquals(1, project2Items.size());
		assertTrue(project2Items.contains("library_1_p1.xml"));
	}
	
	@Test
	public void testSaveProject() throws Exception {
		File libraryFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package_v2/library_1_p2.xml");
		File projectTestFolder = new File( SchemaCompilerTestUtils.getTestProjectLocation() );
		File projectFile = new File(projectTestFolder, "/test_project.xml");
		ProjectManager projectManager = new ProjectManager( false );
		Project project = projectManager.newProject(projectFile,
				"http://www.OpenTravel.org/projects/test/t1", "Test Project", "Description of the test project");
		
		projectManager.addUnmanagedProjectItem(libraryFile, project);
		
		for (ProjectItem item : project.getProjectItems()) {
			item.getContent().setLibraryUrl( URLUtils.toURL( new File(projectTestFolder, "/" + item.getFilename()) ) );
		}
		projectManager.saveProject(project);
		projectManager.closeAll();
		project = null;
		
		// Re-load the file we just finished saving
		project = projectManager.loadProject(projectFile);
		
		// Verify that all of the required project-items were included in the file we just re-loaded
		List<String> globalProjectItems = getProjectItemNames(projectManager);
		List<String> localProjectItems = getProjectItemNames(project);
		
		assertTrue(globalProjectItems.contains("library_1_p1.xml"));
		assertTrue(globalProjectItems.contains("library_2_p1.xml"));
		assertTrue(globalProjectItems.contains("library_1_p2.xml"));
		assertTrue(globalProjectItems.contains("library_2_p2.xml"));
		assertFalse(globalProjectItems.contains("library_3_ext.xml"));
		
		assertEquals(4, localProjectItems.size());
		assertTrue(localProjectItems.contains("library_1_p1.xml"));
		assertTrue(localProjectItems.contains("library_2_p1.xml"));
		assertTrue(localProjectItems.contains("library_1_p2.xml"));
		assertTrue(localProjectItems.contains("library_2_p2.xml"));
	}
	
	@Test
	public void testRemoveProjectItem() throws Exception {
		File projectTestFolder = new File( SchemaCompilerTestUtils.getBaseProjectLocation() );
		File projectFile = new File(projectTestFolder, "/project_1.xml");
		File libraryFile = new File(projectTestFolder, "../libraries_1_4/test-package_v2/library_3_ext.xml");
		ValidationFindings findings = new ValidationFindings();
		ProjectManager projectManager = new ProjectManager( false );
		Project project = projectManager.loadProject(projectFile);
		List<ProjectItem> items = projectManager.addUnmanagedProjectItems(
				Arrays.asList(new File[] { libraryFile }), project, findings);
		
		SchemaCompilerTestUtils.printFindings(findings);
		assertFalse( findings.hasFinding(FindingType.ERROR) );
		assertTrue( getProjectItemNames(projectManager).contains("library_3_ext.xml") );
		assertTrue( getProjectItemNames(project).contains("library_3_ext.xml") );
		
		assertEquals( 1, items.size() );
		project.remove( items.get(0) );
		
		assertFalse( getProjectItemNames(projectManager).contains("library_3_ext.xml") );
		assertFalse( getProjectItemNames(project).contains("library_3_ext.xml") );
	}
	
	@Test
	public void testCloseProject() throws Exception {
		File projectTestFolder = new File( SchemaCompilerTestUtils.getBaseProjectLocation() );
		File projectFile = new File(projectTestFolder, "/project_1.xml");
		ProjectManager projectManager = new ProjectManager( false );
		Project project = projectManager.loadProject(projectFile);
		
		assertTrue( projectManager.getAllProjects().contains(project) );
		
		projectManager.closeProject(project);
		assertFalse( projectManager.getAllProjects().contains(project) );
		
		// Ensure that all of the remaining project items are part of the built-in project
		for (ProjectItem item : projectManager.getAllProjectItems()) {
			assertTrue( item.getContent() instanceof BuiltInLibrary );
		}
	}
	
	@Test
	public void testAddProjectDependencyViaTypeAssignment() throws Exception {
		File projectTestFolder = new File( SchemaCompilerTestUtils.getBaseProjectLocation() );
		File project1File = new File(projectTestFolder, "/project_1.xml");
		File project2File = new File(projectTestFolder, "/project_2.xml");
		ProjectManager projectManager = new ProjectManager( false );
		Project project1 = projectManager.loadProject(project1File);
		Project project2 = projectManager.loadProject(project2File);
		TLLibrary library2p2 = (TLLibrary) projectManager.getModel().getLibrary(PACKAGE_2_NAMESPACE, "library_2_p2");
		TLLibrary sampleLibrary = (TLLibrary) projectManager.getModel().getLibrary(PACKAGE_3_NAMESPACE, "sample_library");
		TLSimple existingSimpleType = library2p2.getSimpleType("Counter_4");
		TLSimple newSimpleType = new TLSimple();
		
		List<String> localProjectItems = getProjectItemNames(project2);
		
		assertEquals(2, project2.getProjectItems().size());
		assertTrue(localProjectItems.contains("sample_library.xml"));
		assertTrue(localProjectItems.contains("included_library.xml"));
		
		assertTrue(projectManager.getAllProjects().contains(project1));
		assertTrue(projectManager.getAllProjects().contains(project2));
		
		assertNotNull(existingSimpleType);
		projectManager.getModel().addListener( new ModelIntegrityChecker() );
		
		// Create a new library dependency by constructing a new simple type and adding it to a library
		newSimpleType.setName("TestSimpleType");
		newSimpleType.setParentType(existingSimpleType);
		sampleLibrary.addNamedMember(newSimpleType);
		
		// Verify that the project item was created by the type assignment action
		localProjectItems = getProjectItemNames(project2);
		assertEquals(6, project2.getProjectItems().size());
		assertTrue(localProjectItems.contains("sample_library.xml"));
		assertTrue(localProjectItems.contains("included_library.xml"));
		assertTrue(localProjectItems.contains("library_1_p1.xml"));
		assertTrue(localProjectItems.contains("library_1_p2.xml"));
		assertTrue(localProjectItems.contains("library_2_p2.xml")); // added via type assignment
		assertTrue(localProjectItems.contains("library_2_p1.xml")); // added because of dependency on type assignment
	}
	
//	@Test
	public void testLoadProject_manualTest() throws Exception {
		File projectTestFolder = new File( System.getProperty("user.dir"), "../../../temp/models" );
		File projectFile = new File(projectTestFolder, "/MyProject.otp");
		assertTrue(projectFile.exists());
		ValidationFindings findings = new ValidationFindings();
		ProjectManager projectManager = new ProjectManager( false );
		Project project = projectManager.loadProject(projectFile, findings);
		SchemaCompilerTestUtils.printFindings(findings, FindingType.ERROR);
		
		System.out.println("Project Items:");
		for (ProjectItem item : project.getProjectItems()) {
			System.out.println("  " + item.getFilename());
		}
		
		System.out.println("\nErrors/Warnings:");
		for (String message : findings.getAllValidationMessages(FindingMessageFormat.IDENTIFIED_FORMAT)) {
			System.out.println("  " + message);
		}
	}
	
//	@Test
	public void testListNamespaces_manualTest() throws Exception {
		RepositoryManager rm = RepositoryManager.getDefault();
		RemoteRepository remoteRepo = rm.listRemoteRepositories().get(0);
		List<String> nsList = remoteRepo.listBaseNamespaces();
		
		for (String ns : nsList) {
			System.out.println(ns);
		}
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
	
}

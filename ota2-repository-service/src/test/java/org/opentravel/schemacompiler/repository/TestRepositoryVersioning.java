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

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.util.RepositoryTestUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.version.MajorVersionHelper;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.PatchVersionHelper;

/**
 * Verifies the operation of the Repository Web Service by launching a Jetty server to run the web
 * service. Operations are accessed via remote URL connection to the Jetty server running on the
 * local host.
 * 
 * @author S. Livezey
 */
public class TestRepositoryVersioning extends RepositoryTestBase {

    @BeforeClass
    public static void setupTests() throws Exception {
        startSmtpTestServer( 1592 );
        setupWorkInProcessArea(TestRepositoryVersioning.class);
        startTestServer("versions-repository", 9293, TestRepositoryVersioning.class);
    }

    @AfterClass
    public static void tearDownTests() throws Exception {
        shutdownTestServer();
    }

    @Test
    public void testMajorVersionHelper() throws Exception {
        ProjectManager projectManager = new ProjectManager(new TLModel(), false,
                repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/version_test_1.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);
        ProjectItem projectItem = findProjectItem(project, "Version_Test_1_0_0.otm");

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
        assertNotNull(projectItem);

        // Create a new major version
        TLLibrary library = (TLLibrary) projectItem.getContent();
        TLLibrary newVersion = new MajorVersionHelper(project).createNewMajorVersion(library);

        // Ensure the new version includes types from the later minor/patch versions in the
        // repository
        // that were not originally in the project
        assertNotNull(newVersion);
        assertEquals("2.0.0", newVersion.getVersion());
        assertNotNull(newVersion.getSimpleType("SimpleType_01_00"));
        assertNotNull(newVersion.getSimpleType("SimpleType_01_01"));
    }

    @Test
    public void testMinorVersionHelper() throws Exception {
        ProjectManager projectManager = new ProjectManager(new TLModel(), false,
                repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/version_test_1.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);
        ProjectItem projectItem = findProjectItem(project, "Version_Test_1_0_0.otm");

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
        assertNotNull(projectItem);

        // Create a new major version
        TLLibrary library = (TLLibrary) projectItem.getContent();
        TLLibrary newVersion = new MinorVersionHelper(project).createNewMinorVersion(library);

        // Ensure the new version includes types from the later minor/patch versions in the
        // repository
        // that were not originally in the project
        assertNotNull(newVersion);
        assertEquals("1.2.0", newVersion.getVersion());
    }

    @Test
    public void testPatchVersionHelper() throws Exception {
        ProjectManager projectManager = new ProjectManager(new TLModel(), false,
                repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/version_test_2.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);
        ProjectItem projectItem = findProjectItem(project, "Version_Test_1_1_0.otm");

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
        assertNotNull(projectItem);

        // Create a new major version
        TLLibrary library = (TLLibrary) projectItem.getContent();
        TLLibrary newVersion = new PatchVersionHelper(project).createNewPatchVersion(library);

        // Ensure the new version skips the patch that exists in the repository, but was not loaded
        // into the project
        assertNotNull(newVersion);
        assertEquals("1.1.2", newVersion.getVersion());
    }

    @Test
    public void testAutoLoadPriorVersions() throws Exception {
        ProjectManager projectManager = new ProjectManager(new TLModel(), false,
                repositoryManager.get());
        File projectFile = new File(wipFolder.get(), "/projects/version_test_3.xml");

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject(projectFile, findings);

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));

        // Make sure the project manager automatically loaded previous versions of the library that
        // were not explicitly called out in the project
        assertNotNull(findProjectItem(project, "Version_Test_1_1_1.otm"));
        assertNotNull(findProjectItem(project, "Version_Test_1_1_0.otm"));
        assertNotNull(findProjectItem(project, "Version_Test_1_0_0.otm"));
    }

}

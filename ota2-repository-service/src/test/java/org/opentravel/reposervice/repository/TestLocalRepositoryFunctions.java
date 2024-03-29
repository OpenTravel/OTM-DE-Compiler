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

package org.opentravel.reposervice.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.testutil.RepositoryTestUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Verifies the operation of items published to a user's local repository.
 * 
 * @author S. Livezey
 */
public class TestLocalRepositoryFunctions extends TestRepositoryFunctions {

    @BeforeClass
    public static void setup() throws Exception {
        setupLocalRepository( "versions-repository", TestLocalRepositoryFunctions.class );
        setupWorkInProcessArea( TestLocalRepositoryFunctions.class );
    }

    @Override
    public void test_02_LockLibrary() throws Exception {
        Repository repository = testRepository.get();
        List<RepositoryItem> lockedItems;

        super.test_02_LockLibrary();

        lockedItems = repository.getLockedItems();
        assertEquals( 1, lockedItems.size() );
        assertEquals( "library_1_p2_2_0_0.otm", lockedItems.get( 0 ).getFilename() );
    }

    @Override
    public void test_21_ListNamespaceChildren() throws Exception {
        super.test_21_ListNamespaceChildren();
        assertEquals( 16, testRepository.get().listAllNamespaces().size() );
    }

    @Test
    public void testAutoLoadPriorVersions() throws Exception {
        ProjectManager projectManager = new ProjectManager( new TLModel(), false, repositoryManager.get() );
        File projectFile = new File( wipFolder.get(), "/projects/version_test_4.xml" );

        if (!projectFile.exists()) {
            throw new FileNotFoundException( "Test File Not Found: " + projectFile.getAbsolutePath() );
        }

        ValidationFindings findings = new ValidationFindings();
        Project project = projectManager.loadProject( projectFile, findings );

        // Verify that the project loaded correctly
        if (findings.hasFinding( FindingType.ERROR )) {
            RepositoryTestUtils.printFindings( findings );
        }
        assertFalse( findings.hasFinding( FindingType.ERROR ) );

        // Make sure the project manager automatically loaded previous versions of the library that
        // were not explicitly called out in the project
        assertNotNull( findProjectItem( project, "Version_Test_1_1_1.otm" ) );
        assertNotNull( findProjectItem( project, "Version_Test_1_1_0.otm" ) );
        assertNotNull( findProjectItem( project, "Version_Test_1_0_0.otm" ) );

        // NOTE: For the local repository test, the above libraries contain no types. It is
        // important
        // that no content is added to the files because it verifies a bug fix that prevented the
        // loading of earlier library versions if the later version contains no content.
    }

}

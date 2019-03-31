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

package org.opentravel.schemacompiler.diff;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.util.ModelComparator;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;

import java.io.File;

/**
 * Verifies the operation of the <code>ProjectComparator</code> and its associated utility classes.
 */
public class TestProjectComparator extends AbstractDiffTest {

    protected ProjectManager projectManager = new ProjectManager( false );

    @Test
    public void testCompareProjects_withDifferences() throws Exception {
        ModelComparator comparator = new ModelComparator( ModelCompareOptions.getDefaultOptions() );
        Project oldProject = loadProject( "/test-package-diff/project-1.xml" );
        Project newProject = loadProject( "/test-package-diff/project-2.xml" );
        ProjectChangeSet changeSet;

        changeSet = comparator.compareProjects( oldProject, newProject );
        comparator.compareProjects( oldProject, newProject, new ByteArrayOutputStream() );
        assertNotNull( changeSet );
        assertTrue( changeSet.getChangeItems().size() > 0 );

        changeSet = comparator.compareProjects( newProject, oldProject );
        comparator.compareProjects( newProject, oldProject, new ByteArrayOutputStream() );
        assertNotNull( changeSet );
        assertTrue( changeSet.getChangeItems().size() > 0 );
    }

    @Test
    public void testCompareProjects_noDifferences() throws Exception {
        ModelComparator comparator = new ModelComparator( ModelCompareOptions.getDefaultOptions() );
        Project project = loadProject( "/test-package-diff/project-1.xml" );
        ProjectChangeSet changeSet;

        changeSet = comparator.compareProjects( project, project );
        comparator.compareProjects( project, project, new ByteArrayOutputStream() );
        assertNotNull( changeSet );
        assertTrue( changeSet.getChangeItems().size() == 0 );
    }

    protected Project loadProject(String projectPath) throws Exception {
        return projectManager.loadProject( new File( SchemaCompilerTestUtils.getBaseLibraryLocation() + projectPath ) );
    }

}

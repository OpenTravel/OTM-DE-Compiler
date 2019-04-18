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

package org.opentravel.schemacompiler.mvn;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.junit.Test;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryManager;

import java.io.File;

/**
 * Verifies the functions of the <code>UpdateRepositorySnapshotMojo</code> build plugin.
 */
public class TestUpdateRepositorySnapshotMojo extends AbstractRepositoryMojoTest {

    @Test
    public void testUpdateFromProject() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-5/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );

        executeInitializeMojo( config );
        executeUpdateMojo( config );
    }

    @Test
    public void testUpdateFromManagedRelease() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-6/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );

        executeInitializeMojo( config );
        executeUpdateMojo( config );
    }

    @Test
    public void testUpdateFromUnmanagedRelease() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-7/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );

        executeInitializeMojo( config );
        executeUpdateMojo( config );
    }

    @Test
    public void testUpdateFromManagedAssembly() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-10/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );

        executeInitializeMojo( config );
        executeUpdateMojo( config );
    }

    @Test
    public void testUpdateFromManagedAssembly_selectModelTypes() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-10/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );
        String[] modelTypes = {"provider", "consumer", "implementation"};

        for (String modelType : modelTypes) {
            config.getChild( "assembly" ).getChild( "modelType" ).setValue( modelType );
            executeInitializeMojo( config );
            executeUpdateMojo( config );
        }
    }

    @Test
    public void testUpdateFromUnmanagedAssembly() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-8/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );

        executeInitializeMojo( config );
        executeUpdateMojo( config );
    }

    @Test(expected = MojoFailureException.class)
    public void testRepositoryError() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-5/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );

        config.getChild( "snapshotProjectFolder" ).setValue( "${basedir}/target/test-output/snapshot-test-99" );
        executeInitializeMojo( config );

        repositoryManager = mock( RepositoryManager.class );
        when( repositoryManager.getRepositoryItem( anyString(), anyString(), anyString() ) )
            .thenThrow( RepositoryException.class );

        config.getChild( "release" ).getChild( "filename" ).setValue( "Version_Test_1_0_0.otm" );
        executeUpdateMojo( config );
    }

}

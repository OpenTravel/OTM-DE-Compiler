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

import java.io.File;

import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.junit.Test;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * Verifies the functions of the <code>InitRepositorySnapshotMojo</code> build plugin.
 */
public class TestInitRepositorySnapshotMojo extends AbstractRepositoryMojoTest {
    
    @Test
    public void testInitFromProject() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-1/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );
        
        executeInitializeMojo( config );
    }
    
    @Test
    public void testInitFromManagedRelease() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-2/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );
        
        executeInitializeMojo( config );
    }
    
    @Test
    public void testInitFromUnmanagedRelease() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-3/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );
        
        executeInitializeMojo( config );
    }
    
    @Test
    public void testInitFromAssembly() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-4/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );
        
        executeInitializeMojo( config );
    }
    
    @Test( expected = MojoFailureException.class )
    public void testProjectNotFound() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-1/pom.xml" );
        File missingProject = new File( testModelsFolder, "/MissingProject.otp" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );
        
        config.getChild( "otmProject" ).setValue( missingProject.getAbsolutePath() );
        executeInitializeMojo( config, false );
    }
    
    @Test( expected = MojoFailureException.class )
    public void testInvalidRelease() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-2/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );
        
        config.getChild( "release" ).getChild( "filename" ).setValue( "Version_Test_1_0_0.otm" );
        executeInitializeMojo( config, false );
    }
    
    @Test( expected = MojoFailureException.class )
    public void testRepositoryError() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-1/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );
        
        repositoryManager = mock( RepositoryManager.class );
        when( repositoryManager.getRepositoryItem( anyString(), anyString(), anyString() ) )
                .thenThrow( RepositoryException.class );
        
        config.getChild( "release" ).getChild( "filename" ).setValue( "Version_Test_1_0_0.otm" );
        config.getChild( "snapshotProjectFolder" ).setValue( "${basedir}/target/test-output/snapshot-test-99" );
        executeInitializeMojo( config );
    }
    
}

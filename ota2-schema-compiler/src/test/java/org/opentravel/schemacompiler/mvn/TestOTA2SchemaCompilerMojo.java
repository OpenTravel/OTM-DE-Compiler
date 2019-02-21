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

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.SilentLog;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * Verifies the functions of the <code>OTA2SchemaCompilerMojo</code> build plugin.
 */
public class TestOTA2SchemaCompilerMojo {
    
    private static final File testProjectsFolder = new File(
            System.getProperty( "user.dir" ), "/src/test/resources/test-projects" );
    private static final File testModelsFolder = new File(
            System.getProperty( "user.dir" ), "/src/test/resources/test-models" );
    private static final File testRepositoryFolder = new File(
            System.getProperty( "user.dir" ), "/src/test/resources/test-repository" );
    
    @Rule
    public MojoRule rule = new MojoRule();
    
    @Test
    public void testCompileLibrary() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-1/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-schema-compiler", pomFile );
        
        executeMojo( config );
    }
    
    @Test
    public void testCompileManagedRelease() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-2/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-schema-compiler", pomFile );
        
        executeMojo( config );
    }
    
    @Test( expected = MojoExecutionException.class )
    public void testLibraryNotFound() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-1/pom.xml" );
        File missingLibrary = new File( testModelsFolder, "/MissingLibrary.otm" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-schema-compiler", pomFile );
        
        config.getChild( "libraryFile" ).setValue( missingLibrary.getAbsolutePath() );
        executeMojo( config );
    }
    
    @Test( expected = MojoExecutionException.class )
    public void testLibraryNotSpecified() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-1/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-schema-compiler", pomFile );
        
        config.getChild( "libraryFile" ).setValue( null );
        executeMojo( config );
    }
    
    @Test( expected = MojoExecutionException.class )
    public void testInvalidRelease() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-2/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-schema-compiler", pomFile );
        
        config.getChild( "release" ).getChild( "filename" ).setValue( "Version_Test_1_0_0.otm" );
        executeMojo( config );
    }
    
    @Test( expected = MojoExecutionException.class )
    public void testInvalidOutputFolder() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-1/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-schema-compiler", pomFile );
        
        config.getChild( "outputFolder" ).setValue( pomFile.getAbsolutePath() );
        executeMojo( config );
    }
    
    @Test( expected = MojoExecutionException.class )
    public void testUncreatableOutputFolder() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-1/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-schema-compiler", pomFile );
        
        config.getChild( "outputFolder" ).setValue( pomFile.getAbsolutePath() + "/output" );
        executeMojo( config );
    }
    
    @Test( expected = MojoExecutionException.class )
    public void testInvalidBindingStyle() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-1/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-schema-compiler", pomFile );
        
        config.getChild( "bindingStyle" ).setValue( "XYZ" );
        executeMojo( config );
    }
    
    protected void executeMojo(PlexusConfiguration config) throws Exception {
        OTA2SchemaCompilerMojo mojo = new OTA2SchemaCompilerMojo();
        
        mojo = (OTA2SchemaCompilerMojo) rule.configureMojo( mojo, config );
        mojo.initRepositoryManager( new RepositoryManager( testRepositoryFolder ) );
        mojo.setLog( new SilentLog() );
        mojo.execute();
    }
    
}

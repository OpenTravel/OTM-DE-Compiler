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

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.MojoRule;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.junit.Rule;
import org.junit.Test;

/**
 * Verifies the functions of the <code>OTA2SchemaCompilerMojo</code> build plugin.
 */
public class TestOTA2SchemaCompilerMojo {
    
    private static final File testProjectsFolder = new File(
            System.getProperty( "user.dir" ), "/src/test/resources/test-projects" );
    
    @Rule
    public MojoRule rule = new MojoRule();
    
    @Test
    public void testCompileLibrary() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-1/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-schema-compiler", pomFile );
        Mojo mojo = new OTA2SchemaCompilerMojo();
        
        mojo = rule.configureMojo( mojo, config );
        mojo.execute();
    }
    
}

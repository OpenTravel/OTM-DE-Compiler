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

import javax.xml.bind.JAXBElement;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.SilentLog;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.opentravel.ns.ota2.project_v01_00.ProjectItemType;
import org.opentravel.ns.ota2.project_v01_00.ProjectType;
import org.opentravel.ns.ota2.project_v01_00.UnmanagedProjectItemType;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.ProjectFileUtils;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Base class for unit tests of repository snapshot build plugins.
 */
public abstract class AbstractRepositoryMojoTest {
    
    protected static final File testProjectsFolder = new File(
            System.getProperty( "user.dir" ), "/src/test/resources/test-projects" );
    protected static final File testModelsFolder = new File(
            System.getProperty( "user.dir" ), "/src/test/resources/test-models" );
    protected static final File testRepositoryFolder = new File(
            System.getProperty( "user.dir" ), "/src/test/resources/test-repository" );
    
    protected RepositoryManager repositoryManager;
    
    @Rule
    public MojoRule rule = new MojoRule() {
        @Override protected void before() throws Throwable {
            super.before();
            repositoryManager = new RepositoryManager( testRepositoryFolder );
        }
        
    };
    
    protected void executeInitializeMojo(PlexusConfiguration config) throws Exception {
        executeInitializeMojo( config, true );
    }
    
    protected void executeInitializeMojo(PlexusConfiguration config, boolean purgeOutputFolder) throws Exception {
        String snapshotFolderStr = config.getChild( "snapshotProjectFolder" ).getValue();
        File snapshotFolder = new File( snapshotFolderStr.replace( "${basedir}", System.getProperty( "user.dir" ) ) );
        InitRepositorySnapshotMojo mojo = new InitRepositorySnapshotMojo( repositoryManager );
        
        if (purgeOutputFolder) {
            FileUtils.deleteDirectory( snapshotFolder );
        }
        mojo = (InitRepositorySnapshotMojo) rule.configureMojo( mojo, config );
        mojo.setLog( new SilentLog() );
        mojo.execute();
        
        validateSnapshotProject( snapshotFolder );
    }
    
    protected void executeUpdateMojo(PlexusConfiguration config) throws Exception {
        String snapshotFolderStr = config.getChild( "snapshotProjectFolder" ).getValue();
        File snapshotFolder = new File( snapshotFolderStr.replace( "${basedir}", System.getProperty( "user.dir" ) ) );
        UpdateRepositorySnapshotMojo mojo = new UpdateRepositorySnapshotMojo( repositoryManager );
        
        mojo = (UpdateRepositorySnapshotMojo) rule.configureMojo( mojo, config );
        mojo.setLog( new SilentLog() );
        mojo.execute();
        
        validateSnapshotProject( snapshotFolder );
    }
    
    protected void validateSnapshotProject(File snapshotFolder) throws Exception {
        ValidationFindings findings = new ValidationFindings();
        File snapshotFile = null;
        
        for (File file : snapshotFolder.listFiles()) {
            if (file.getName().endsWith( "-snapshot.otp" )) {
                snapshotFile = file;
                break;
            }
        }
        Assert.assertNotNull( "Snapshot project file not created.", snapshotFile );
        ProjectType project = new ProjectFileUtils().loadJaxbProjectFile( snapshotFile, findings );
        
        if (findings.hasFinding()) {
            Assert.fail( "Snapshot project file contains errors or warnings." );
        }
        
        for (JAXBElement<? extends ProjectItemType> itemElement : project.getProjectItemBase()) {
            ProjectItemType item = itemElement.getValue();
            File itemFile;
            
            Assert.assertTrue(
                    "Snapshot project contains one or more repository-managed libraries.",
                    (item instanceof UnmanagedProjectItemType) );
            itemFile = new File( snapshotFolder, "/" + ((UnmanagedProjectItemType) item).getFileLocation() );
            Assert.assertTrue( "The snapshot project library '" + itemFile.getName() + "' could not be located.",
                    (itemFile.exists() && itemFile.isFile()) );
        }
    }
    
}

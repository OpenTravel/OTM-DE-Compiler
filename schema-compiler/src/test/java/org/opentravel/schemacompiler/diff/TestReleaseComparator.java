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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.opentravel.ns.ota2.release_v01_00.ReleaseStatus;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.Release;
import org.opentravel.schemacompiler.repository.ReleaseCompileOptions;
import org.opentravel.schemacompiler.repository.ReleaseManager;
import org.opentravel.schemacompiler.repository.ReleaseMember;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl;
import org.opentravel.schemacompiler.util.ModelComparator;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * Verifies the operation of the <code>ReleaseComparator</code> and its
 * associated utility classes.
 */
@RunWith( MockitoJUnitRunner.class )
public class TestReleaseComparator extends AbstractDiffTest {
    
    @Mock private RemoteRepository mockRepository;
    
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks( this );
        when( mockRepository.getEndpointUrl() ).thenReturn( "http://www.mock-repository.org" );
    }
    
    @Test
    public void testCompareReleases_withDifferences() throws Exception {
        ModelComparator comparator = new ModelComparator( ModelCompareOptions.getDefaultOptions() );
        ReleaseManager oldRelease = loadRelease( "Release-1", "1.0.0", "/test-package-diff/project-1.xml" );
        ReleaseManager newRelease = loadRelease( "Release-2", "1.0.0", "/test-package-diff/project-2.xml" );
        ReleaseChangeSet changeSet;
        
        changeSet = comparator.compareReleases( oldRelease, newRelease );
        comparator.compareReleases( oldRelease, newRelease, new ByteArrayOutputStream() );
        assertNotNull( changeSet );
        assertTrue( changeSet.getChangeItems().size() > 0 );
        
        changeSet = comparator.compareReleases( newRelease, oldRelease );
        comparator.compareReleases( newRelease, oldRelease, new ByteArrayOutputStream() );
        assertNotNull( changeSet );
        assertTrue( changeSet.getChangeItems().size() > 0 );
    }
    
    @Test
    public void testCompareReleases_noDifferences() throws Exception {
        ModelComparator comparator = new ModelComparator( ModelCompareOptions.getDefaultOptions() );
        ReleaseManager release = loadRelease( "Release-1", "1.0.0", "/test-package-diff/project-1.xml" );
        ReleaseChangeSet changeSet;
        
        changeSet = comparator.compareReleases( release, release );
        comparator.compareReleases( release, release, new ByteArrayOutputStream() );
        assertNotNull( changeSet );
        assertTrue( changeSet.getChangeItems().size() == 0 );
    }
    
    protected ReleaseManager loadRelease(String releaseName, String releaseVersion, String libraryPath) throws Exception {
        String releaseFilename = releaseName + "_" + releaseVersion.replace( '.', '_' ) + ".otr";
        File releaseFile = new File( SchemaCompilerTestUtils.getBaseLibraryLocation() + "/releases/" + releaseFilename );
        RepositoryManager mockRepositoryManager = mock( RepositoryManager.class );
        ReleaseManager releaseManager = mock( ReleaseManager.class );
        TLModel model = loadModel( libraryPath );
        Release release = new Release();
        
        when( mockRepository.getEndpointUrl() ).thenReturn( "http://www.mock-repository.org" );
        when( releaseManager.getRelease() ).thenReturn( release );
        when( releaseManager.getModel() ).thenReturn( model );
        
        release.setBaseNamespace( "http://www.opentravel.org/releases" );
        release.setReleaseUrl( URLUtils.toURL( releaseFile ) );
        release.setName( releaseName );
        release.setVersion( releaseVersion );
        release.setStatus( ReleaseStatus.DRAFT );
        release.setCompileOptions( new ReleaseCompileOptions() );
        
        for (TLLibrary library : model.getUserDefinedLibraries()) {
            RepositoryItemImpl item = new RepositoryItemImpl();
            ReleaseMember member = new ReleaseMember();
            
            item.setRepository( mockRepository );
            item.setBaseNamespace( library.getBaseNamespace() );
            item.setNamespace( library.getNamespace() );
            item.setLibraryName( library.getName() );
            item.setFilename( library.getName() + "_" + library.getVersion().replace( '.', '_' ) + ".otm" );
            item.setVersion( library.getVersion() );
            item.setVersionScheme( library.getVersionScheme() );
            item.setStatus( library.getStatus() );
            item.setState( RepositoryItemState.MANAGED_UNLOCKED );
            member.setRepositoryItem( item );
            
            when( mockRepositoryManager.getRepositoryItem(
                    URLUtils.toFile( library.getLibraryUrl() ) ) ).thenReturn( item );
            release.getPrincipalMembers().add( member );
        }
        return releaseManager;
    }
    
}

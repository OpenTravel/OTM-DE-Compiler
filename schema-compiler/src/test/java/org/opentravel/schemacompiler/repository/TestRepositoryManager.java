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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryStatus;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RefreshPolicy;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryState;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.repository.impl.DefaultRepositoryFileManager;
import org.opentravel.schemacompiler.repository.impl.LibraryContentWrapper;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryUtils;
import org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Verifies the functions of the <code>RepositoryManager</code> class.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestRepositoryManager {

    private static final String[] VALID_REPO_IDS =
        new String[] {"Valid", "Letter", "host", ";host", "&ole", "Id%20TEst"};
    private static final String[] INVALID_REPO_IDS = new String[] {":Inalid", "@Letter", null, "Valid", "Valid"};
    private static final String[] INVALID_REPO_DNS =
        new String[] {"ValidName", "ValidName", "ValidName", null, StringUtils.repeat( 'A', 257 )};

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private RepositoryManager repositoryManager;
    private RepositoryFileManager mockFileManager;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks( this );
        mockFileManager = spy( new DefaultRepositoryFileManager( folder.getRoot() ) );
        repositoryManager = new RepositoryManager( mockFileManager );
    }

    @Test
    public void testUpdateLocalRepositoryIdentity() throws Exception {
        for (String valid : VALID_REPO_IDS) {
            repositoryManager.updateLocalRepositoryIdentity( valid, "ValidName" );
        }

        for (int i = 0; i < INVALID_REPO_IDS.length; i++) {
            try {
                repositoryManager.updateLocalRepositoryIdentity( INVALID_REPO_IDS[i], INVALID_REPO_DNS[i] );
                fail( "Repository ID '" + INVALID_REPO_IDS[i] + "' should be invalid!" );

            } catch (RepositoryException re) {
                // Ignore - expected exception was thrown
            }
        }
    }

    @Test
    public void testRemoteRepositoryManagement() throws Exception {
        RemoteRepositoryUtils remoteUtilsMock = mock( RemoteRepositoryUtils.class );
        RepositoryInfoType localRepoInfo = mockFileManager.loadRepositoryMetadata();
        RepositoryInfoType mockRemoteRepo = getRemoteRepositoryInfo();
        String endpointUrl = "http://www.remote-repository.org/ota2-repository-service";
        List<RemoteRepository> remoteRepositories;
        RemoteRepository remoteRepo;

        localRepoInfo.setRemoteRepositories( null );
        when( mockFileManager.loadRepositoryMetadata() ).thenReturn( localRepoInfo );
        when( remoteUtilsMock.getRepositoryMetadata( endpointUrl ) ).thenReturn( mockRemoteRepo );

        repositoryManager.setRemoteUtils( remoteUtilsMock );
        repositoryManager.addRemoteRepository( endpointUrl );
        remoteRepositories = repositoryManager.listRemoteRepositories();

        assertEquals( 1, remoteRepositories.size() );
        assertEquals( mockRemoteRepo.getID(), (remoteRepo = remoteRepositories.get( 0 )).getId() );
        assertEquals( mockRemoteRepo.getDisplayName(), remoteRepo.getDisplayName() );
        assertEquals( endpointUrl, remoteRepo.getEndpointUrl() );

        assertEquals( RefreshPolicy.DAILY, remoteRepo.getRefreshPolicy() );
        repositoryManager.setRefreshPolicy( remoteRepo, RefreshPolicy.ON_DEMAND );
        assertEquals( RefreshPolicy.ON_DEMAND, remoteRepo.getRefreshPolicy() );

        repositoryManager.removeRemoteRepository( remoteRepo );
        assertEquals( 0, repositoryManager.listRemoteRepositories().size() );
    }

    @Test(expected = RepositoryException.class)
    public void testAddRemoteRepository_duplicateAdd_repositoryId() throws Exception {
        RemoteRepositoryUtils remoteUtilsMock = mock( RemoteRepositoryUtils.class );
        RepositoryInfoType mockRemoteRepo = getRemoteRepositoryInfo();
        String endpointUrl1 = "http://www.remote-repository1.org/ota2-repository-service";
        String endpointUrl2 = "http://www.remote-repository2.org/ota2-repository-service";

        // Return the same repository metadata from two remote endpoint URLs
        when( remoteUtilsMock.getRepositoryMetadata( endpointUrl1 ) ).thenReturn( mockRemoteRepo );
        when( remoteUtilsMock.getRepositoryMetadata( endpointUrl2 ) ).thenReturn( mockRemoteRepo );

        repositoryManager.setRemoteUtils( remoteUtilsMock );
        repositoryManager.addRemoteRepository( endpointUrl1 );
        repositoryManager.addRemoteRepository( endpointUrl2 );
    }

    @Test(expected = RepositoryException.class)
    public void testAddRemoteRepository_duplicateAdd_repositoryUrl() throws Exception {
        RemoteRepositoryUtils remoteUtilsMock = mock( RemoteRepositoryUtils.class );
        RepositoryInfoType mockRemoteRepo1 = getRemoteRepositoryInfo();
        RepositoryInfoType mockRemoteRepo2 = getRemoteRepositoryInfo();
        String endpointUrl = "http://www.remote-repository1.org/ota2-repository-service";

        // Return different repository meta-data from the same endpoint URL
        mockRemoteRepo1.setID( mockRemoteRepo1.getID() + "1" );
        mockRemoteRepo2.setID( mockRemoteRepo2.getID() + "2" );
        when( remoteUtilsMock.getRepositoryMetadata( endpointUrl ) ).thenReturn( mockRemoteRepo1 );
        repositoryManager.setRemoteUtils( remoteUtilsMock );
        repositoryManager.addRemoteRepository( endpointUrl );

        when( remoteUtilsMock.getRepositoryMetadata( endpointUrl ) ).thenReturn( mockRemoteRepo2 );
        repositoryManager.addRemoteRepository( endpointUrl );
    }

    @Test(expected = RepositoryException.class)
    public void testAddRemoteRepository_addRemoteWithLocalId() throws Exception {
        RemoteRepositoryUtils remoteUtilsMock = mock( RemoteRepositoryUtils.class );
        RepositoryInfoType localRepoInfo = mockFileManager.loadRepositoryMetadata();
        RepositoryInfoType mockRemoteRepo = getRemoteRepositoryInfo();
        String endpointUrl = "http://www.remote-repository1.org/ota2-repository-service";

        // Return repository meta-data with the same ID as the local repository
        mockRemoteRepo.setID( localRepoInfo.getID() );
        when( remoteUtilsMock.getRepositoryMetadata( endpointUrl ) ).thenReturn( mockRemoteRepo );

        repositoryManager.setRemoteUtils( remoteUtilsMock );
        repositoryManager.addRemoteRepository( endpointUrl );
    }

    @Test(expected = RepositoryException.class)
    public void testGetRepositoryItem_missingRepository1() throws Exception {
        RepositoryItem mockItem = getTestRepositoryItem();
        String itemUri = RepositoryUtils.newURI( mockItem, true ).toString();

        itemUri = itemUri.replace( repositoryManager.getId(), "non-existent-repository" );
        repositoryManager.getRepositoryItem( itemUri );
    }

    @Test(expected = RepositoryException.class)
    public void testGetRepositoryItem_missingRepository2() throws Exception {
        RepositoryItem mockItem = getTestRepositoryItem();
        String itemUri = RepositoryUtils.newURI( mockItem, false ).toString();

        itemUri = itemUri.replace( repositoryManager.getId(), "non-existent-repository" );
        repositoryManager.getRepositoryItem( itemUri, mockItem.getNamespace() );
    }

    @Test(expected = RepositoryException.class)
    public void testGetRepositoryItem_missingNamespace() throws Exception {
        RepositoryItem mockItem = getTestRepositoryItem();
        String itemUri = RepositoryUtils.newURI( mockItem, false ).toString();

        repositoryManager.getRepositoryItem( itemUri );
    }

    @Test(expected = RepositoryException.class)
    public void testGetRepositoryItem_mismatchlibraryMetadata() throws Exception {
        RepositoryItem mockItem = getTestRepositoryItem();
        String itemUri = RepositoryUtils.newURI( mockItem, true ).toString();
        LibraryInfoType mockLibraryMetadata = getMockLibraryMetadata();

        mockLibraryMetadata.setOwningRepository( "unknown-repository" );
        mockFileManager = mock( DefaultRepositoryFileManager.class );
        mockFileManager.setRepositoryLocation( folder.getRoot() );
        repositoryManager.setFileManager( mockFileManager );
        when( mockFileManager.loadLibraryMetadata( mockItem.getBaseNamespace(), mockItem.getFilename(),
            mockItem.getVersion() ) ).thenReturn( mockLibraryMetadata );

        repositoryManager.getRepositoryItem( itemUri, mockItem.getNamespace() );
    }

    @Test(expected = RepositoryException.class)
    public void testPromote_fileUpdateFailure() throws Exception {
        LibraryInfoType mockMetadata = getMockLibraryMetadata();
        RepositoryItem mockItem;

        mockItem = setupStatusChangeMock( mockMetadata );
        repositoryManager.promote( mockItem );
    }

    @Test(expected = RepositoryException.class)
    public void testPromote_invalidLocked() throws Exception {
        LibraryInfoType mockMetadata = getMockLibraryMetadata();
        RepositoryItem mockItem;

        mockMetadata.setState( RepositoryState.MANAGED_LOCKED );
        mockItem = setupStatusChangeMock( mockMetadata );
        repositoryManager.promote( mockItem );
    }

    @Test(expected = RepositoryException.class)
    public void testPromote_obsoleteStatus() throws Exception {
        LibraryInfoType mockMetadata = getMockLibraryMetadata();
        RepositoryItem mockItem;

        mockMetadata.setStatus( LibraryStatus.OBSOLETE );
        mockItem = setupStatusChangeMock( mockMetadata );
        repositoryManager.promote( mockItem );
    }

    @Test(expected = RepositoryException.class)
    public void testPromote_unassignedStatus() throws Exception {
        LibraryInfoType mockMetadata = getMockLibraryMetadata();
        RepositoryItemImpl mockItem;

        mockItem = setupStatusChangeMock( mockMetadata );
        mockItem.setStatus( null );
        mockMetadata.setStatus( null );
        repositoryManager.promote( mockItem );
    }

    @Test(expected = RepositoryException.class)
    public void testDemote_fileUpdateFailure() throws Exception {
        LibraryInfoType mockMetadata = getMockLibraryMetadata();
        RepositoryItem mockItem;

        mockMetadata.setStatus( LibraryStatus.UNDER_REVIEW );
        mockItem = setupStatusChangeMock( mockMetadata );
        repositoryManager.demote( mockItem );
    }

    @Test(expected = RepositoryException.class)
    public void testDemote_invalidLocked() throws Exception {
        LibraryInfoType mockMetadata = getMockLibraryMetadata();
        RepositoryItem mockItem;

        mockMetadata.setState( RepositoryState.MANAGED_LOCKED );
        mockItem = setupStatusChangeMock( mockMetadata );
        repositoryManager.demote( mockItem );
    }

    @Test(expected = RepositoryException.class)
    public void testDemote_obsoleteStatus() throws Exception {
        LibraryInfoType mockMetadata = getMockLibraryMetadata();
        RepositoryItem mockItem;

        mockMetadata.setStatus( LibraryStatus.DRAFT );
        mockItem = setupStatusChangeMock( mockMetadata );
        repositoryManager.demote( mockItem );
    }

    @Test(expected = RepositoryException.class)
    public void testDemote_unassignedStatus() throws Exception {
        LibraryInfoType mockMetadata = getMockLibraryMetadata();
        RepositoryItemImpl mockItem;

        mockItem = setupStatusChangeMock( mockMetadata );
        mockItem.setStatus( null );
        mockMetadata.setStatus( null );
        repositoryManager.demote( mockItem );
    }

    private RepositoryItemImpl setupStatusChangeMock(LibraryInfoType mockLibraryMetadata) throws Exception {
        File mockLibraryFile = new File( folder.getRoot(), "/mock-library_0_0_0.otm" );
        RepositoryItemImpl mockItem = getTestRepositoryItem( mockLibraryMetadata );
        TLLibrary mockLibrary = new TLLibrary();
        LibraryContentWrapper mockWrapper = new LibraryContentWrapper( mockLibrary, mockLibraryFile, true );

        mockFileManager = mock( DefaultRepositoryFileManager.class );
        mockFileManager.setRepositoryLocation( folder.getRoot() );
        repositoryManager.setFileManager( mockFileManager );

        when( mockFileManager.loadLibraryMetadata( mockItem.getBaseNamespace(), mockItem.getFilename(),
            mockItem.getVersion() ) ).thenReturn( mockLibraryMetadata );
        when( mockFileManager.getLibraryContentLocation( mockItem.getBaseNamespace(), mockItem.getFilename(),
            mockItem.getVersion() ) ).thenReturn( mockLibraryFile );
        when( mockFileManager.loadLibraryContent( mockLibraryFile ) ).thenReturn( mockWrapper );
        doAnswer( (Answer<?>) i -> {
            throw new RepositoryException();
        } ).when( mockFileManager ).saveLibraryContent( mockWrapper );
        return mockItem;
    }

    @Test(expected = RepositoryException.class)
    public void testCreateRootNamespace_invalidURI() throws Exception {
        repositoryManager.createRootNamespace( "http::::@@@||ABC123" );
    }

    @Test(expected = RepositoryException.class)
    public void testCreateRootNamespace_invalidURI2() throws Exception {
        repositoryManager.createRootNamespace( "http://www.mock-repository.org/test-root-ns?query=param" );
    }

    @Test(expected = RepositoryException.class)
    public void testCreateRootNamespace_duplicate() throws Exception {
        String existingRootNS = repositoryManager.listRootNamespaces().get( 0 );
        String childNS = existingRootNS + "/childNS";

        repositoryManager.createNamespace( childNS );
        repositoryManager.createRootNamespace( childNS );
    }

    @Test
    public void testRefreshLocalRepositoryInfo_noLocalRepo() throws Exception {
        mockFileManager = mock( DefaultRepositoryFileManager.class );
        mockFileManager.setRepositoryLocation( folder.getRoot() );
        repositoryManager.setFileManager( mockFileManager );

        when( mockFileManager.getRepositoryMetadataLastUpdated() ).thenReturn( null );
        when( mockFileManager.getRepositoryLocation() ).thenReturn( folder.getRoot() );

        repositoryManager.refreshLocalRepositoryInfo( true ); // No exception - error logged
    }

    @Test
    public void testRefreshLocalRepositoryInfo_repoError() throws Exception {
        mockFileManager = mock( DefaultRepositoryFileManager.class );
        mockFileManager.setRepositoryLocation( folder.getRoot() );
        repositoryManager.setFileManager( mockFileManager );

        when( mockFileManager.getRepositoryMetadataLastUpdated() ).thenReturn( new Date() );
        when( mockFileManager.loadRepositoryMetadata() ).thenThrow( RepositoryException.class );

        repositoryManager.refreshLocalRepositoryInfo( true ); // No exception - warning logged
    }

    private RepositoryInfoType getRemoteRepositoryInfo() {
        RepositoryInfoType remoteRepoInfo = new RepositoryInfoType();

        remoteRepoInfo.setID( "remote-repository" );
        remoteRepoInfo.setDisplayName( "Remote Repository" );
        remoteRepoInfo.getRootNamespace().add( "http://www.remote-repository.org/rootNS1" );
        remoteRepoInfo.getRootNamespace().add( "http://www.remote-repository.org/rootNS2" );
        return remoteRepoInfo;
    }

    private RepositoryItemImpl getTestRepositoryItem() {
        return RepositoryUtils.createRepositoryItem( repositoryManager, getMockLibraryMetadata() );
    }

    private RepositoryItemImpl getTestRepositoryItem(LibraryInfoType libraryMetadata) {
        return RepositoryUtils.createRepositoryItem( repositoryManager, libraryMetadata );
    }

    private LibraryInfoType getMockLibraryMetadata() {
        LibraryInfoType metadata = new LibraryInfoType();

        metadata.setOwningRepository( repositoryManager.getId() );
        metadata.setBaseNamespace( "http://www.local-repository.org/rootNS1/nsa" );
        metadata.setNamespace( "http://www.local-repository.org/rootNS1/nsa/v1" );
        metadata.setFilename( "TestLibrary_1_0_0.otm" );
        metadata.setLibraryName( "TestLibrary" );
        metadata.setVersion( "1.0.0" );
        metadata.setStatus( LibraryStatus.DRAFT );
        metadata.setState( RepositoryState.MANAGED_UNLOCKED );
        return metadata;
    }

}

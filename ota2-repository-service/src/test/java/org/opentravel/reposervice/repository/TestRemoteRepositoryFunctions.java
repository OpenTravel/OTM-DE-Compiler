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
import static org.junit.Assert.assertTrue;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.repository.EntitySearchResult;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.Response;

/**
 * Verifies the operation of items published to a remote repository.
 * 
 * @author S. Livezey
 */
public class TestRemoteRepositoryFunctions extends TestRepositoryFunctions {

    @BeforeClass
    public static void setupTests() throws Exception {
        startSmtpTestServer( 1587 );
        setupWorkInProcessArea( TestRemoteRepositoryFunctions.class );
        startTestServer( "empty-repository", 9291, TestRemoteRepositoryFunctions.class );
    }

    @AfterClass
    public static void tearDownTests() throws Exception {
        shutdownTestServer();
    }

    /**
     * @see org.opentravel.reposervice.repository.resource.TestRepositoryFunctions#test_01_PublishLibrary()
     */
    @Override
    public void test_01_PublishLibrary() throws Exception {
        super.test_01_PublishLibrary();
        repositoryManager.get().refreshLocalRepositoryInfo( true );
    }

    @Override
    public void test_02_LockLibrary() throws Exception {
        RemoteRepository repository = (RemoteRepository) testRepository.get();
        List<EntitySearchResult> searchResult;
        List<RepositoryItem> lockedItems;
        List<RepositoryItem> whereUsedItems;
        TLLibrary library;
        NamedEntity entity;

        super.test_02_LockLibrary();
        repositoryManager.get().refreshRemoteRepositories();

        lockedItems = repository.getLockedItems();
        assertEquals( 1, lockedItems.size() );
        assertEquals( "library_1_p2_2_0_0.otm", lockedItems.get( 0 ).getFilename() );

        whereUsedItems = repository.getItemWhereUsed( lockedItems.get( 0 ), true );
        assertEquals( 0, whereUsedItems.size() );

        library = (TLLibrary) model.get()
            .getLibrary( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2", "library_1_p2" );
        entity = library.getBusinessObjectType( "SampleBusinessObject" );
        searchResult = repository.getEntityWhereUsed( entity, true );
        assertEquals( 4, searchResult.size() );

        entity = library.getBusinessObjectType( "EmptyBusinessObject" );
        searchResult = repository.getEntityWhereExtended( entity );
        assertEquals( 2, searchResult.size() );
    }

    @Override
    public void test_21_ListNamespaceChildren() throws Exception {
        super.test_21_ListNamespaceChildren();
        assertEquals( 9, testRepository.get().listAllNamespaces().size() );
    }

    @Test
    public void testUserAuthorizations() throws Exception {
        String readNS = "http://www.OpenTravel.org";
        String writeNS = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler";
        RepositoryPermission permission1 = ((RemoteRepository) testRepository.get()).getUserAuthorization( readNS );
        RepositoryPermission permission2 = ((RemoteRepository) testRepository.get()).getUserAuthorization( writeNS );

        assertEquals( RepositoryPermission.READ_FINAL, permission1 );
        assertEquals( RepositoryPermission.WRITE, permission2 );
    }

    @Test
    public void testGetUserAuthorizationWithNamespace() throws ClientProtocolException, IOException {
        CloseableHttpResponse ret = doGet( ((RemoteRepository) testRepository.get()).getEndpointUrl()
            + "/service/user-authorization?baseNamespace=http://www.OpenTravel.org" );
        assertEquals( Response.Status.OK.getStatusCode(), ret.getStatusLine().getStatusCode() );
    }

    @Test
    public void testGetUserAuthorizationForMissingNamespace() throws ClientProtocolException, IOException {
        CloseableHttpResponse ret =
            doGet( ((RemoteRepository) testRepository.get()).getEndpointUrl() + "/service/user-authorization" );
        assertEquals( Response.Status.UNAUTHORIZED.getStatusCode(), ret.getStatusLine().getStatusCode() );
    }

    @Test
    public void testCheckAdministrator() throws RepositoryException, ClientProtocolException, IOException {
        assertTrue( ((RemoteRepository) testRepository.get()).isAdministrator() );
    }

    private CloseableHttpResponse doGet(String url) throws ClientProtocolException, IOException {
        HttpGet get = new HttpGet( url );
        return HttpClients.createDefault().execute( get );
    }

}

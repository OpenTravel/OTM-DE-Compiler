
package org.opentravel.schemacompiler.repository;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.repository.RemoteRepository;

/**
 * Verifies the operation of items published to a remote repository.
 *
 * @author S. Livezey
 */
public class TestRemoteRepositoryFunctions extends TestRepositoryFunctions {
	
	@BeforeClass
	public static void setupTests() throws Exception {
		setupWorkInProcessArea( TestRemoteRepositoryFunctions.class );
		startTestServer( "empty-repository", 9291, TestRemoteRepositoryFunctions.class );
	}

	@AfterClass
	public static void tearDownTests() throws Exception {
		shutdownTestServer();
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
		CloseableHttpResponse ret = doGet(((RemoteRepository) testRepository.get()).getEndpointUrl()
		        + "/service/user-authorization?baseNamespace=http://www.OpenTravel.org");
		assertEquals(Response.Status.OK.getStatusCode(), ret.getStatusLine().getStatusCode());
	}

	@Test
	public void testGetUserAuthorizationForMissingNamespace() throws ClientProtocolException, IOException {
		CloseableHttpResponse ret = doGet(((RemoteRepository) testRepository.get()).getEndpointUrl()
		        + "/service/user-authorization");
		assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), ret.getStatusLine().getStatusCode());
	}

	private CloseableHttpResponse doGet(String url) throws ClientProtocolException,
	        IOException {
		HttpGet get = new HttpGet(url);
		return HttpClients.createDefault().execute(get);
	}

}

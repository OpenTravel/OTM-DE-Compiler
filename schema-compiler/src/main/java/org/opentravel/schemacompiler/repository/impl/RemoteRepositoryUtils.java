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

package org.opentravel.schemacompiler.repository.impl;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryInfoType;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.repository.RepositorySecurityException;
import org.opentravel.schemacompiler.security.PasswordHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Utility methods used for accessing remote repositories via HTTP requests.
 */
public class RemoteRepositoryUtils {

    public static final String SERVICE_CONTEXT = "/service";
    private static final String REPOSITORY_METADATA_ENDPOINT = SERVICE_CONTEXT + "/repository-metadata";

    private String userId;
    private String encryptedPassword;

    /**
     * Returns the user ID credential for the remote repository's web service.
     * 
     * @return String
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Assigns the user ID credential for the remote repository's web service.
     * 
     * @param userId the user ID value to assign
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the encryptedPassword credential for the remote repository's web service.
     * 
     * @return String
     */
    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    /**
     * Assigns the encrypted password credential for the remote repository's web service.
     * 
     * @param password the encrypted password value to assign
     */
    public void setEncryptedPassword(String password) {
        this.encryptedPassword = password;
    }

    /**
     * Contacts the repository web service at the specified endpoint URL, and returns the repository meta-data
     * information.
     * 
     * @param endpointUrl the URL endpoint of the OTA2.0 repository web service
     * @return RepositoryInfoType
     * @throws RepositoryException thrown if the remote web service is not available
     */
    @SuppressWarnings("unchecked")
    public RepositoryInfoType getRepositoryMetadata(String endpointUrl) throws RepositoryException {
        try {
            HttpGet getRequest = new HttpGet( endpointUrl + REPOSITORY_METADATA_ENDPOINT );
            HttpResponse response = execute( getRequest );
            Unmarshaller unmarshaller = RepositoryFileManager.getSharedJaxbContext().createUnmarshaller();
            JAXBElement<RepositoryInfoType> jaxbElement =
                (JAXBElement<RepositoryInfoType>) unmarshaller.unmarshal( response.getEntity().getContent() );

            return jaxbElement.getValue();

        } catch (JAXBException e) {
            throw new RepositoryException( "The format of the repository meta-data is unreadable.", e );

        } catch (IOException e) {
            throw new RepositoryException( "The remote repository is unavailable.", e );
        }
    }

    /**
     * Returns an HTTP client to use when accessing the remote repository.
     * 
     * @return HttpClient
     */
    private static HttpClient createHttpClient() {
        return HttpClientBuilder.create().useSystemProperties()
            .setDefaultCredentialsProvider( new NTLMSystemCredentialsProvider() ).build();
    }

    /**
     * Configures the 'Authorization' header on the given HTTP request using the current user ID and encrypted password
     * that is configured for this repository.
     */
    private HttpClientContext createHttpContext() {
        HttpClientContext context = HttpClientContext.create();

        if ((userId != null) && (encryptedPassword != null)) {
            AuthState target = new AuthState();
            target.update( new BasicScheme(), buildAuthorizationCredentials() );
            context.setAttribute( HttpClientContext.TARGET_AUTH_STATE, target );
        }
        return context;
    }

    /**
     * Applies the user's credentials to the given request and sends the request to the remote repository. If the
     * response is returned from this method, the caller can assume that the remote operation did not result in an
     * error.
     * 
     * @param request the request to send to the remote repository
     * @return HttpResponse
     * @throws RepositoryException thrown if an error response is received from the remote server
     * @throws IOException thrown if an error occurs during request execution
     */
    public HttpResponse executeWithAuthentication(HttpUriRequest request) throws RepositoryException, IOException {
        HttpResponse response = createHttpClient().execute( request, createHttpContext() );
        int statusCode = response.getStatusLine().getStatusCode();

        if ((statusCode < 200) || (statusCode > 299)) {
            if (statusCode == 401) {
                throw new RepositorySecurityException(
                    "User is not authorized to perform the requested action (check for out of date credentials)." );
            } else {
                throw new RepositoryException( getResponseErrorMessage( response ) );
            }
        }
        return response;
    }

    /**
     * Sends the given request to the remote repository.
     * 
     * @param request the request to send to the remote repository
     * @return HttpResponse
     * @throws RepositoryException thrown if an error response is received from the remote server
     * @throws IOException thrown if an error occurs during request execution
     */
    public HttpResponse execute(HttpUriRequest request) throws RepositoryException, IOException {
        HttpResponse response = createHttpClient().execute( request );
        int statusCode = response.getStatusLine().getStatusCode();

        if ((statusCode < 200) || (statusCode > 299)) {
            if (statusCode == 401) {
                throw new RepositorySecurityException( "User is not authorized to perform the requested action." );
            } else {
                throw new RepositoryException( getResponseErrorMessage( response ) );
            }
        }
        return response;
    }

    /**
     * Returns the HTTP authorization credentials for an HTTP request.
     * 
     * @return Credentials
     */
    public Credentials buildAuthorizationCredentials() {
        Credentials credentials = null;

        if ((userId != null) && (encryptedPassword != null)) {
            credentials = new UsernamePasswordCredentials( userId, PasswordHelper.decrypt( encryptedPassword ) );
        }
        return credentials;
    }

    /**
     * If the response status indicates an error condition and a message is provided, the text of that message is
     * returned.
     * 
     * @param response the HTTP response to process
     * @return String
     */
    private static String getResponseErrorMessage(HttpResponse response) {
        int statusCode = response.getStatusLine().getStatusCode();
        String errorMessage = null;

        if ((statusCode < 200) || (statusCode > 299)) {
            try {
                InputStream responseStream = response.getEntity().getContent();
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[256];
                int bytesRead;

                while ((bytesRead = responseStream.read( buffer )) >= 0) {
                    byteStream.write( buffer, 0, bytesRead );
                }
                responseStream.close();
                errorMessage = new String( byteStream.toByteArray(), StandardCharsets.UTF_8 );

            } catch (IOException e) {
                errorMessage = "Unknown repository error on the remote host.";
            }
        }
        return errorMessage;
    }

}

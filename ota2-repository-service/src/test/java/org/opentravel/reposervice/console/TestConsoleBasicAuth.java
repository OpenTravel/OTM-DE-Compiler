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

package org.opentravel.reposervice.console;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.reposervice.repository.RepositoryTestBase;

import com.unboundid.util.Base64;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Verifies that console requests with a BASIC authorization header will successfully authenticate a user in the current
 * session.
 */
public class TestConsoleBasicAuth extends RepositoryTestBase {

    private static final String VALID_AUTH_HEADER = "Basic " + Base64.encode( TESTUSER_ID + ":" + TESTUSER_CREDENTIAL );
    private static final String INVALID_AUTH_HEADER = "Basic " + Base64.encode( TESTUSER_ID + ":badpassword" );

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestConsoleBasicAuth.class );
        startTestServer( "versions-repository", 9301, jmsIndexRepositoryConfig, true, true,
            TestConsoleBasicAuth.class );
    }

    @Test
    public void testBasicAuthSuccess() throws Exception {
        sendRequest( VALID_AUTH_HEADER, true );
    }

    @Test
    public void testBasicAuthInvalidLogin() throws Exception {
        sendRequest( INVALID_AUTH_HEADER, false );
    }

    @Test
    public void testBasicAuthMalformedHeader() throws Exception {
        sendRequest( "MALFORMED-AUTH-HEADER", false );
    }

    private void sendRequest(String authHeader, boolean expectSuccess) throws Exception {
        URL url = new URL( jettyServer.get().getBaseRepositoryUrl() + "/console/index.html" );
        HttpURLConnection cnx = (HttpURLConnection) url.openConnection();
        boolean successfulLogin;
        String responseText;

        cnx.setRequestProperty( "Authorization", authHeader );

        try (Reader in = new InputStreamReader( cnx.getInputStream() )) {
            StringWriter out = new StringWriter();
            char[] buffer = new char[1024];
            int charsRead;

            while ((charsRead = in.read( buffer, 0, buffer.length )) >= 0) {
                out.write( buffer, 0, charsRead );
            }
            responseText = out.toString();
        }
        successfulLogin = responseText.contains( TESTUSER_ID );
        assertEquals( 200, cnx.getResponseCode() );

        if (expectSuccess) {
            Assert.assertTrue( successfulLogin );
        } else {
            Assert.assertFalse( successfulLogin );
        }
    }

}

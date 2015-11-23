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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Locale;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.repository.impl.NTLMSystemCredentialsProvider;

public class NTLMSystemCredentialsProviderTest extends SystemDefaultCredentialsProvider {

    NTLMSystemCredentialsProvider ntlmCredentialsProvider = new NTLMSystemCredentialsProvider();
    static TestAuthenticator testAuthenticator = new TestAuthenticator();

    @BeforeClass
    public static void beforeTest() {
        Authenticator.setDefault(testAuthenticator);
    }

    @Test
    public void getCredentialsForNTLMShouldReturnNLCredentials() {
        testAuthenticator.userName = "";
        Credentials credentials = ntlmCredentialsProvider
                .getCredentials(createAuthScope(AuthSchemes.NTLM));
        assertTrue(credentials instanceof NTCredentials);
    }

    @Test
    public void getCredentialsForNTMLShouldParseDoamin() {
        String domain = "DOMAIN";
        String username = "userName";
        testAuthenticator.userName = domain + "\\" + username;
        NTCredentials credentials = (NTCredentials) ntlmCredentialsProvider
                .getCredentials(createAuthScope(AuthSchemes.NTLM));
        assertEquals(domain, credentials.getDomain());
        assertEquals(username, credentials.getUserName());
    }

    @Test
    public void getCredentialsForLowerCaseDomainNTMLShouldReturnUpperCase() {
        String domain = "domain";
        String username = "userName";
        testAuthenticator.userName = domain + "\\" + username;
        NTCredentials credentials = (NTCredentials) ntlmCredentialsProvider
                .getCredentials(createAuthScope(AuthSchemes.NTLM));
        assertEquals(domain.toUpperCase(Locale.ENGLISH), credentials.getDomain());
        assertEquals(username, credentials.getUserName());
    }

    @Test
    public void getCredentialsWithoutDomainShouldReturnNull() {
        String domain = "";
        String username = "userName";
        testAuthenticator.userName = domain + "\\" + username;
        NTCredentials credentials = (NTCredentials) ntlmCredentialsProvider
                .getCredentials(createAuthScope(AuthSchemes.NTLM));
        assertEquals(null, credentials.getDomain());
        assertEquals(username, credentials.getUserName());
    }

    @Test
    public void getCredentialsWithMultipleDelimetersShouldConsiderFirstOne() {
        String domain = "DOMAIN";
        String username = "user\\Nam\\e";
        testAuthenticator.userName = domain + "\\" + username;
        NTCredentials credentials = (NTCredentials) ntlmCredentialsProvider
                .getCredentials(createAuthScope(AuthSchemes.NTLM));
        assertEquals(domain, credentials.getDomain());
        assertEquals(username, credentials.getUserName());
    }

    private AuthScope createAuthScope(String schema) {
        AuthScope scope = new AuthScope("dummyHost", 80, null, schema);
        return scope;
    }

    static class TestAuthenticator extends Authenticator {

        String userName = "";
        String password = "";

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(userName, password.toCharArray());
        }

    }
}
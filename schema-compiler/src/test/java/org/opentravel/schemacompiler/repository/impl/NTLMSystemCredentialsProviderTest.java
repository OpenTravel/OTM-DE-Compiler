/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemacompiler.repository.impl;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Locale;

import junit.framework.Assert;

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
		Credentials credentials = ntlmCredentialsProvider.getCredentials(createAuthScope(AuthSchemes.NTLM));
		Assert.assertTrue(credentials instanceof NTCredentials);
	}

	@Test
	public void getCredentialsForNTMLShouldParseDoamin() {
		String domain = "DOMAIN";
		String username = "userName";
		testAuthenticator.userName = domain + "\\" + username;
		NTCredentials credentials = (NTCredentials) ntlmCredentialsProvider
		        .getCredentials(createAuthScope(AuthSchemes.NTLM));
		Assert.assertEquals(domain, credentials.getDomain());
		Assert.assertEquals(username, credentials.getUserName());
	}

	@Test
	public void getCredentialsForLowerCaseDomainNTMLShouldReturnUpperCase() {
		String domain = "domain";
		String username = "userName";
		testAuthenticator.userName = domain + "\\" + username;
		NTCredentials credentials = (NTCredentials) ntlmCredentialsProvider
		        .getCredentials(createAuthScope(AuthSchemes.NTLM));
		Assert.assertEquals(domain.toUpperCase(Locale.ENGLISH), credentials.getDomain());
		Assert.assertEquals(username, credentials.getUserName());
	}

	@Test
	public void getCredentialsWithoutDomainShouldReturnNull() {
		String domain = "";
		String username = "userName";
		testAuthenticator.userName = domain + "\\" + username;
		NTCredentials credentials = (NTCredentials) ntlmCredentialsProvider
		        .getCredentials(createAuthScope(AuthSchemes.NTLM));
		Assert.assertEquals(null, credentials.getDomain());
		Assert.assertEquals(username, credentials.getUserName());
	}

	@Test
	public void getCredentialsWithMultipleDelimetersShouldConsiderFirstOne() {
		String domain = "DOMAIN";
		String username = "user\\Nam\\e";
		testAuthenticator.userName = domain + "\\" + username;
		NTCredentials credentials = (NTCredentials) ntlmCredentialsProvider
		        .getCredentials(createAuthScope(AuthSchemes.NTLM));
		Assert.assertEquals(domain, credentials.getDomain());
		Assert.assertEquals(username, credentials.getUserName());
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
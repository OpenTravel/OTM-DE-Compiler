/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.repository.impl;

import java.util.Locale;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;

/**
 * Extended {@link SystemDefaultCredentialsProvider} with support for NTLM proxy authentication
 * 
 * @author Pawel Jedruch
 */
public class NTLMSystemCredentialsProvider extends SystemDefaultCredentialsProvider {

	public Credentials getCredentials(final AuthScope authscope) {
		Credentials credentials = super.getCredentials(authscope);
		if (AuthSchemes.NTLM.toUpperCase(Locale.ENGLISH).equals(authscope.getScheme())) {
			credentials = super.getCredentials(authscope);
			return traslateToNTLMCredentials(credentials);
		}
		return credentials;
	}

	private NTCredentials traslateToNTLMCredentials(Credentials credentials) {
		String fullUserName = credentials.getUserPrincipal().getName();
		String[] tokens = fullUserName.split("\\\\", 2);
		String userName = "";
		String domain = null;
		if (tokens.length == 2) {
			if (tokens[0] != null && !tokens[0].isEmpty()) {
				domain = tokens[0];
			}
			userName = tokens[1];
		} else {
			userName = tokens[0];
		}
		String workstation = null; // how to support workstation ???
		return new NTCredentials(userName, credentials.getPassword(), workstation, domain);
	}
}
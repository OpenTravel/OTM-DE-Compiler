/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemacompiler.security;

import java.util.List;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;

/**
 * Registry that has the ability to identify protected namespaces and determine which users
 * have write access to libraries that are assigned to those namespaces.
 * 
 * @author S. Livezey
 */
public abstract class ProtectedNamespaceRegistry {
	
	/**
	 * Returns the default <code>ProtectedNamespaceRegistry</code> instance from the compiler's
	 * application context.
	 * 
	 * @return ProtectedNamespaceRegistry
	 */
	public static ProtectedNamespaceRegistry getInstance() {
		return (ProtectedNamespaceRegistry)
				SchemaCompilerApplicationContext.getContext().getBean(
						SchemaCompilerApplicationContext.PROTECTED_NAMESPACE_REGISTRY);
	}
	
	/**
	 * Returns the list of protected namespace groups defined in the registry.
	 * 
	 * @return List<ProtectedNamespaceGroup>
	 */
	public abstract List<ProtectedNamespaceGroup> getProtectedNamespaces();
	
	/**
	 * Returns true if the given namespace is considered to be protected by this registry.
	 * 
	 * @param namespace  the namespace URI to check
	 * @return boolean
	 */
	public boolean isProtectedNamespace(String namespace) {
		return (getMatchingNamespaceGroup(namespace) != null);
	}
	
	/**
	 * Returns true if the given namespace is either not protected, or the user's credentials allow
	 * him/her write access to the libraries in that namespace.
	 * 
	 * @param namespace  the namespace URI to check
	 * @param credentials  the user credentials to use for security checks
	 * @return boolean
	 * @throws SchemaCompilerSecurityException  thrown if an error occurs during the access check
	 */
	public boolean hasWriteAccess(String namespace, ProtectedNamespaceCredentials credentials) throws SchemaCompilerSecurityException {
		ProtectedNamespaceGroup nsGroup = getMatchingNamespaceGroup(namespace);
		boolean hasAccess;
		
		if (nsGroup != null) {
			if (credentials.getNamespaceGroups().contains(nsGroup.getGroupId())) {
				String userId = credentials.getUserId(nsGroup.getGroupId());
				String password = credentials.getPassword(nsGroup.getGroupId());
				
				hasAccess = nsGroup.hasWriteAccess(userId, password);
				
			} else { // no write access if credentials were not provided
				hasAccess = false;
			}
		} else { // not a protected namespace, so grant write access
			hasAccess = true;
		}
		return hasAccess;
	}
	
	/**
	 * Returns the protected namespace group from the registry that matches the given namespace, or null
	 * if no namespace matches were discovered.
	 * 
	 * @param namespace  the namespace URI for which to retrieve the group
	 * @return ProtectedNamespaceGroup
	 */
	private ProtectedNamespaceGroup getMatchingNamespaceGroup(String namespace) {
		ProtectedNamespaceGroup nsGroup = null;
		
		for (ProtectedNamespaceGroup grp : getProtectedNamespaces()) {
			for (String grpNS : grp.getProtectedNamespaceUris()) {
				if (isMatchingNamespace(grpNS, namespace)) {
					nsGroup = grp;
					break;
				}
			}
			if (nsGroup != null) break;
		}
		return nsGroup;
	}
	
	private boolean isMatchingNamespace(String groupNS, String testNS) {
		// Normalize the namespace components by forcing a trailing '/' at the end of both namespaces
		if (!groupNS.endsWith("/")) {
			groupNS += "/";
		}
		if (!testNS.endsWith("/")) {
			testNS += "/";
		}
		
		// Keep trying until the test namespace is shorter than the group namespace
		boolean isMatch = false;
		
		while (!isMatch && (testNS != null) && (testNS.length() >= groupNS.length())) {
			isMatch = groupNS.equals(testNS);
			testNS = trimNamespaceComponent(testNS);
		}
		return isMatch;
	}
	
	/**
	 * Trims the last namespace component from the given URI and returns the result.  If no
	 * '/' characters remain in the given string, this method will return null.
	 * 
	 * @param namespace  the namespace URI to trim
	 * @return String
	 */
	private String trimNamespaceComponent(String namespace) {
		if (namespace.endsWith("/")) {
			namespace = namespace.substring(0, namespace.lastIndexOf('/'));
		}
		int lastSlashIdx = namespace.lastIndexOf('/');
		String trimmedNamespace = null;
		
		if (lastSlashIdx >= 0) {
			trimmedNamespace = namespace.substring(0, lastSlashIdx + 1);
		}
		return trimmedNamespace;
	}
	
}

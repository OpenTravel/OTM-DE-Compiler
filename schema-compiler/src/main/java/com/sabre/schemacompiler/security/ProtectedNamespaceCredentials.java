/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the credentials provided by a user to verify write access to one or more protected
 * namespaces.
 * 
 * @author S. Livezey
 */
public final class ProtectedNamespaceCredentials {
	
	private List<String> groupIds = new ArrayList<String>();
	private Map<String,String> userIdCredentials = new HashMap<String,String>();
	private Map<String,String> passwordCredentials = new HashMap<String,String>();
	
	/**
	 * Assigns the userId and password credentials for the specified protected namespace group.
	 * 
	 * @param namespaceGroupId  the ID of the namespace group for which to assign credentials
	 * @param userId  the user ID credential for the namespace group
	 * @param password  the password credential for the namespace group
	 */
	public synchronized void setCredentials(String namespaceGroupId, String userId, String password) {
		if (namespaceGroupId == null) {
			throw new NullPointerException("The ID of the protected namespace group cannot be null.");
		}
		if ((userId == null) || (password == null)) {
			throw new NullPointerException("The userId and password credentials must both be non-null values.");
		}
		if (!groupIds.contains(namespaceGroupId)) {
			groupIds.add(namespaceGroupId);
		}
		userIdCredentials.put(namespaceGroupId, userId);
		passwordCredentials.put(namespaceGroupId, password);
	}
	
	/**
	 * Returns the ID's of the protected namespace groups for which credentials have been registered.
	 * 
	 * @return Collection<String>
	 */
	public synchronized Collection<String> getNamespaceGroups() {
		return Collections.unmodifiableCollection(groupIds);
	}
	
	/**
	 * Returns the user ID credential that was registered for the specified protected namespace group.
	 * 
	 * @param namespaceGroupId  the ID of the namespace group for which to retrieve credentials
	 * @return String
	 */
	public synchronized String getUserId(String namespaceGroupId) {
		if (!groupIds.contains(namespaceGroupId)) {
			throw new IllegalArgumentException("No credentials provided for protected namespace group: " + namespaceGroupId);
		}
		return userIdCredentials.get(namespaceGroupId);
	}
	
	/**
	 * Returns the password credential that was registered for the specified protected namespace group.
	 * 
	 * @param namespaceGroupId  the ID of the namespace group for which to retrieve credentials
	 * @return String
	 */
	public synchronized String getPassword(String namespaceGroupId) {
		if (!groupIds.contains(namespaceGroupId)) {
			throw new IllegalArgumentException("No credentials provided for protected namespace group: " + namespaceGroupId);
		}
		return passwordCredentials.get(namespaceGroupId);
	}
	
	/**
	 * Deletes the credentials for the specified protected namespace group from this collection.
	 * 
	 * @param namespaceGroupId  the ID of the namespace group for which to delete credentials
	 */
	public synchronized void removeCredentials(String namespaceGroupId) {
		if (groupIds.contains(namespaceGroupId)) {
			groupIds.remove(namespaceGroupId);
			userIdCredentials.remove(namespaceGroupId);
			passwordCredentials.remove(namespaceGroupId);
		}
	}
	
	/**
	 * Clears the credentials for all of the protected namespace groups.
	 */
	public synchronized void clear() {
		groupIds.clear();
		userIdCredentials.clear();
		passwordCredentials.clear();
	}
	
}

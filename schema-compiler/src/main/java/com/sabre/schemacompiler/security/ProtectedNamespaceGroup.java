/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.security;

import java.util.List;

/**
 * Identifies one or more protected namespaces and can verify whether a user's ID and password
 * should allow write-access to libraries assigned to the namespace(s).
 * 
 * @author S. Livezey
 */
public interface ProtectedNamespaceGroup {
	
	/**
	 * Returns the ID of this protected namespace group.
	 * 
	 * @return String
	 */
	public String getGroupId();
	
	/**
	 * Returns the user-readable title of this protected namespace group.
	 * 
	 * @return String
	 */
	public String getGroupTitle();
	
	/**
	 * Returns the list of protected namespace URI's for this group.
	 * 
	 * @return List<String>
	 */
	public List<String> getProtectedNamespaceUris();
	
	/**
	 * Returns true if the user credentials provided match those of a user who should have
	 * write access to the namespaces in this group.
	 * 
	 * @param userId  the ID of the user to check
	 * @param password  the clear-text password of the user to check
	 * @return boolean
	 * @throws SchemaCompilerSecurityException  thrown if an error occurs during the access check
	 */
	public boolean hasWriteAccess(String userId, String password) throws SchemaCompilerSecurityException;
	
}

/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.security.impl;

import java.util.ArrayList;
import java.util.List;

import com.sabre.schemacompiler.security.ProtectedNamespaceGroup;
import com.sabre.schemacompiler.security.ProtectedNamespaceRegistry;

/**
 * Default implementation of the <code>ProtectedNamespaceRegistry</code> that is designed to
 * obtain its registry entries from spring injection or some other manual assignment method.
 * 
 * @author S. Livezey
 */
public class DefaultProtectedNamespaceRegistry extends ProtectedNamespaceRegistry {
	
	private List<ProtectedNamespaceGroup> nsGroups = new ArrayList<ProtectedNamespaceGroup>();
	
	/**
	 * @see com.sabre.schemacompiler.security.ProtectedNamespaceRegistry#getProtectedNamespaces()
	 */
	@Override
	public List<ProtectedNamespaceGroup> getProtectedNamespaces() {
		return nsGroups;
	}
	
	/**
	 * Assigns the list of protected namespace groups for this registry.
	 * 
	 * @param nsGroups  the list of protected namespace groups
	 */
	public void setProtectedNamespaces(List<ProtectedNamespaceGroup> nsGroups) {
		this.nsGroups = nsGroups;
	}
	
}

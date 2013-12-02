/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.security.impl;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLLibraryStatus;
import com.sabre.schemacompiler.security.LibraryAccessController;
import com.sabre.schemacompiler.security.ProtectedNamespaceCredentials;
import com.sabre.schemacompiler.security.ProtectedNamespaceRegistry;
import com.sabre.schemacompiler.security.SchemaCompilerSecurityException;
import com.sabre.schemacompiler.util.URLUtils;

/**
 * Library access controller component that utilizes the <code>ProtectedNamespaceRegistry</code> to
 * determine if a user has write access to libraries.
 * 
 * @author S. Livezey
 */
public class ProtectedNamespaceLibraryAccessController implements LibraryAccessController {
	
	private static Logger log = LoggerFactory.getLogger(ProtectedNamespaceLibraryAccessController.class);
	
	private ProtectedNamespaceCredentials userCredentials;
	
	/**
	 * @see com.sabre.schemacompiler.security.LibraryAccessController#hasModifyPermission(com.sabre.schemacompiler.model.TLLibrary)
	 */
	@Override
	public boolean hasModifyPermission(TLLibrary library) {
		ProtectedNamespaceRegistry nsRegistry = ProtectedNamespaceRegistry.getInstance();
		boolean grantPermission = false;
		
		// Only check protected namespace credentials if the file is not read-only
		if (isWritableLibraryFile(library)) {
			if (nsRegistry.isProtectedNamespace(library.getNamespace())) {
				if (userCredentials != null) {
					try {
						grantPermission = nsRegistry.hasWriteAccess(library.getNamespace(), userCredentials);
						
					} catch (SchemaCompilerSecurityException e) {
						// Log exception and return false to deny write access
						log.error("Error determining write access to library: " + library.getName(), e);
					}
				}
			} else {
				grantPermission = true; // allow write access for all non-protected namespaces
			}
		}
		return grantPermission;
	}
	
	/**
	 * @see com.sabre.schemacompiler.security.LibraryAccessController#setUserCredentials(com.sabre.schemacompiler.security.ProtectedNamespaceCredentials)
	 */
	@Override
	public void setUserCredentials(ProtectedNamespaceCredentials userCredentials) {
		this.userCredentials = userCredentials;
	}
	
	/**
	 * Returns true if the library is located in a writable file on the local file system, and
	 * its status is non-Final (i.e. a draft library).
	 * 
	 * @param library  the library to analyze
	 * @return boolean
	 */
	private boolean isWritableLibraryFile(TLLibrary library) {
		URL libraryUrl = library.getLibraryUrl();
		boolean canModify = (libraryUrl == null) || (library.getStatus() == TLLibraryStatus.FINAL);
		
		if (!canModify && URLUtils.isFileURL(libraryUrl)) {
			File libraryFile = URLUtils.toFile(libraryUrl);
			
			// Allow the file to be written if it is a new file that does not yet exist on the file system
			canModify = !libraryFile.exists() || libraryFile.canWrite();
		}
		return canModify;
	}
	
}

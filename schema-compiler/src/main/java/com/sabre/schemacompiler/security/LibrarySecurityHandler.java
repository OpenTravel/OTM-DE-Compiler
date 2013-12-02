/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.security;

import org.springframework.context.ApplicationContext;

import com.sabre.schemacompiler.ioc.SchemaCompilerApplicationContext;
import com.sabre.schemacompiler.model.TLLibrary;

/**
 * Handler that determines whether a library can be modified by the user of an editor application.  The
 * implementation of the static methods in this class is delegated to the <code>LibraryAccessController</code>
 * instance that is configured in the Spring application context file.  If no such access controller is
 * specified, modify permission will be granted to the user by default.
 * 
 * @author S. Livezey
 */
public final class LibrarySecurityHandler {
	
	private static final LibraryAccessController accessController;
	
	/**
	 * Private contstructor to prevent instantiation of this class.
	 */
	private LibrarySecurityHandler() {}
	
	/**
	 * Returns true if the current user is allowed to modify the given library.
	 * 
	 * @param library  the user-defined library
	 * @return boolean
	 */
	public static boolean hasModifyPermission(TLLibrary library) {
		return (accessController == null) ? true : accessController.hasModifyPermission(library);
	}
	
	/**
	 * Assigns the set of security credentials that determine which (if any) protected namespaces
	 * the user has the authority to modify.
	 * 
	 * @param userCredentials  the user credentials that provide access to update protected namespaces
	 */
	public static void setUserCredentials(ProtectedNamespaceCredentials userCredentials) {
		if (accessController != null) {
			accessController.setUserCredentials(userCredentials);
		}
	}
	
	/**
	 * Initializes the <code>LibraryAccessController</code> from the Spring application context.
	 */
	static {
		try {
			ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();
			
			if (appContext.containsBean(SchemaCompilerApplicationContext.LIBRARY_ACCESS_CONTROLLER)) {
				accessController = (LibraryAccessController) appContext.getBean(
						SchemaCompilerApplicationContext.LIBRARY_ACCESS_CONTROLLER);
				
			} else {
				accessController = null;
			}
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
}

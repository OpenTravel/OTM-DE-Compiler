/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.BuiltInLibrary;
import com.sabre.schemacompiler.model.LibraryElement;
import com.sabre.schemacompiler.model.XSDLibrary;

/**
 * Filter used to determine which libraries and member artifacts should be processed during
 * code generation.
 * 
 * @author S. Livezey
 */
public interface CodeGenerationFilter {
	
	/**
	 * Returns true if artifacts for the given library should be produced during code generation
	 * processing.
	 * 
	 * @param library  the library to check
	 * @return boolean
	 */
	public boolean processLibrary(AbstractLibrary library);
	
	/**
	 * Returns true if an extension schema for the given legacy schema should be produced during
	 * code generation processing.
	 * 
	 * @param legacySchema  the legacy schema to check
	 * @return boolean
	 */
	public boolean processExtendedLibrary(XSDLibrary legacySchema);
	
	/**
	 * Returns true if output components should be generated for the given library element during
	 * code generation processing.
	 * 
	 * @param entity  the library member to check
	 * @return boolean
	 */
	public boolean processEntity(LibraryElement entity);
	
	/**
	 * Adds the given built-in library to the list of libraries allowed by this filter.
	 * 
	 * @param library  the built-in library to add
	 */
	public void addBuiltInLibrary(BuiltInLibrary library);
	
}

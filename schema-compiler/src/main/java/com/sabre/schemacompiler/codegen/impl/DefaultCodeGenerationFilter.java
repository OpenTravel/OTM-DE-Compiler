/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.impl;

import java.util.HashSet;
import java.util.Set;

import com.sabre.schemacompiler.codegen.CodeGenerationFilter;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.BuiltInLibrary;
import com.sabre.schemacompiler.model.LibraryElement;
import com.sabre.schemacompiler.model.XSDLibrary;

/**
 * Default implementation of the <code>CodeGenerationFilter</code> interface.
 * 
 * @author S. Livezey
 */
public class DefaultCodeGenerationFilter implements CodeGenerationFilter {
	
	private Set<AbstractLibrary> allowedLibraries = new HashSet<AbstractLibrary>();
	private Set<XSDLibrary> extensionLibraries = new HashSet<XSDLibrary>();
	private Set<LibraryElement> allowedEntities = new HashSet<LibraryElement>();
	
	/**
	 * Adds the given library to the list of libraries that will be allowed by this filter.
	 * 
	 * @param library  the library to allow
	 */
	public void addProcessedLibrary(AbstractLibrary library) {
		if ((library != null) && !allowedLibraries.contains(library)) {
			allowedLibraries.add(library);
		}
	}
	
	/**
	 * Adds the given library to the list of schema extensions that will be allowed by this filter.
	 * 
	 * @param legacySchema  the legacy schema for which an extension will be required
	 */
	public void addExtensionLibrary(XSDLibrary legacySchema) {
		if ((legacySchema != null) && !extensionLibraries.contains(legacySchema)) {
			extensionLibraries.add(legacySchema);
		}
	}
	
	/**
	 * Adds the given library element to the list of entities that will be allowed by this filter.
	 * 
	 * @param entity  the library element to allow
	 */
	public void addProcessedElement(LibraryElement entity) {
		if ((entity != null) && !allowedEntities.contains(entity)) {
			allowedEntities.add(entity);
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.CodeGenerationFilter#processLibrary(com.sabre.schemacompiler.model.AbstractLibrary)
	 */
	@Override
	public boolean processLibrary(AbstractLibrary library) {
		return allowedLibraries.contains(library);
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.CodeGenerationFilter#processExtendedLibrary(com.sabre.schemacompiler.model.XSDLibrary)
	 */
	@Override
	public boolean processExtendedLibrary(XSDLibrary legacySchema) {
		return extensionLibraries.contains(legacySchema);
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.CodeGenerationFilter#processEntity(com.sabre.schemacompiler.model.LibraryElement)
	 */
	@Override
	public boolean processEntity(LibraryElement entity) {
		return allowedEntities.contains(entity);
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.CodeGenerationFilter#addBuiltInLibrary(com.sabre.schemacompiler.model.BuiltInLibrary)
	 */
	@Override
	public void addBuiltInLibrary(BuiltInLibrary library) {
		addProcessedLibrary(library);
	}
	
}

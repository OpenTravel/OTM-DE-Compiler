/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.impl;

import java.util.ArrayList;
import java.util.List;

import com.sabre.schemacompiler.codegen.CodeGenerationFilter;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.BuiltInLibrary;
import com.sabre.schemacompiler.model.LibraryElement;
import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.XSDLibrary;

/**
 * Code generation filter that only allows generation of artifacts derived directly from a single
 * library member.
 * 
 * @author S. Livezey
 */
public class LibraryMemberSchemaFilter implements CodeGenerationFilter {
	
	private List<AbstractLibrary> builtInLibraries = new ArrayList<AbstractLibrary>();
	private LibraryMember libraryMember;
	private CodeGenerationFilter libraryFilter;
	
	/**
	 * Constructor that specifies the library member for which output artifacts are to be generated.
	 * 
	 * @param libraryMember  the target library member
	 * @param libraryFilter  code generation filter that contains information about the libraries upon
	 *						 which the member's schema will depend
	 */
	public LibraryMemberSchemaFilter(LibraryMember libraryMember, CodeGenerationFilter libraryFilter) {
		this.libraryMember = libraryMember;
		this.libraryFilter = libraryFilter;
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.CodeGenerationFilter#processLibrary(com.sabre.schemacompiler.model.AbstractLibrary)
	 */
	@Override
	public boolean processLibrary(AbstractLibrary library) {
		return builtInLibraries.contains(library) || libraryFilter.processLibrary(library); // delegate this value to the nested filter
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.CodeGenerationFilter#processExtendedLibrary(com.sabre.schemacompiler.model.XSDLibrary)
	 */
	@Override
	public boolean processExtendedLibrary(XSDLibrary legacySchema) {
		return libraryFilter.processExtendedLibrary(legacySchema); // delegate this value to the nested filter
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.CodeGenerationFilter#processEntity(com.sabre.schemacompiler.model.LibraryElement)
	 */
	@Override
	public boolean processEntity(LibraryElement entity) {
		return (entity == libraryMember);
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.CodeGenerationFilter#addBuiltInLibrary(com.sabre.schemacompiler.model.BuiltInLibrary)
	 */
	@Override
	public void addBuiltInLibrary(BuiltInLibrary library) {
		builtInLibraries.add(library);
	}
	
}

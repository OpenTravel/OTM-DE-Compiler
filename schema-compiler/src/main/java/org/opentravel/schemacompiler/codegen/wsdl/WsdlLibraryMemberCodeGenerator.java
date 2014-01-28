/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen.wsdl;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.impl.LibraryMemberFilenameBuilder;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;

/**
 * Code generator implementation used to generate WSDL documents from <code>TLService</code> meta-model components.
 * 
 * <p>The following context variable(s) are required when invoking this code generation module:
 * <ul>
 *   <li><code>schemacompiler.OutputFolder</code> - the folder where generated WSDL files should be stored</li>
 * </ul>
 * 
 * @author S. Livezey
 */
public class WsdlLibraryMemberCodeGenerator extends AbstractWsdlCodeGenerator<LibraryMember> {

	/**
	 * @see org.opentravel.schemacompiler.codegen.wsdl.AbstractWsdlCodeGenerator#getLibrary(java.lang.Object)
	 */
	@Override
	protected AbstractLibrary getLibrary(LibraryMember source) {
		return (source == null) ? null : source.getOwningLibrary();
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
	 */
	@Override
	protected CodeGenerationFilenameBuilder<LibraryMember> getDefaultFilenameBuilder() {
		return new LibraryMemberFilenameBuilder<LibraryMember>();
	}

}

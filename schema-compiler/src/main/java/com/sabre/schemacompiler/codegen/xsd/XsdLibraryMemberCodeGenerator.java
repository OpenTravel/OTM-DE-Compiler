/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.sabre.schemacompiler.codegen.CodeGenerationContext;
import com.sabre.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import com.sabre.schemacompiler.codegen.impl.CodegenNamespacePrefixMapper;
import com.sabre.schemacompiler.codegen.impl.LibraryMemberFilenameBuilder;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.LibraryMember;

/**
 * Code generator implementation used to generate XSD documents from meta-model components.
 * 
 * <p>The following context variable(s) are required when invoking this code generation module:
 * <ul>
 *   <li><code>schemacompiler.OutputFolder</code> - the folder where generated XSD schema files should be stored</li>
 *   <li><code>schemacompiler.SchemaFilename</code> - the name of the XSD schema file to be generated (uses library name/version if not specified)</li>
 * </ul>
 * 
 * @author S. Livezey
 */
public class XsdLibraryMemberCodeGenerator extends AbstractXsdCodeGenerator<LibraryMember> {
	
	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.AbstractXsdCodeGenerator#canGenerateOutput(com.sabre.schemacompiler.model.TLModelElement, com.sabre.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected boolean canGenerateOutput(LibraryMember source, CodeGenerationContext context) {
		return true;
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(java.lang.Object)
	 */
	@Override
	protected AbstractLibrary getLibrary(LibraryMember source) {
		return (source == null) ? null : source.getOwningLibrary();
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
	 */
	@Override
	protected CodeGenerationFilenameBuilder<LibraryMember> getDefaultFilenameBuilder() {
		return new LibraryMemberFilenameBuilder<LibraryMember>();
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#getMarshaller(com.sabre.schemacompiler.model.TLModelElement, org.w3._2001.xmlschema.Schema)
	 */
	@Override
	protected Marshaller getMarshaller(LibraryMember source, org.w3._2001.xmlschema.Schema schema) throws JAXBException {
		Marshaller m = jaxbContext.createMarshaller();
		
		m.setSchema(validationSchema);
		m.setProperty("com.sun.xml.bind.namespacePrefixMapper",
				new CodegenNamespacePrefixMapper(getLibrary(source), false, this, schema));
		return m;
	}

}

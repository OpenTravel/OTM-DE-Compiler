/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen;

import org.junit.Test;

import com.sabre.schemacompiler.model.TLLibrary;

/**
 * Verifies the operation of the XSD code generator.
 * 
 * @author S. Livezey
 */
public class TestXsdCodeGenerator extends AbstractTestCodeGenerators {
	
	@Test
	public void testGenerateLibraryXsd_package1_library1() throws Exception {
		TLLibrary library = getLibrary(PACKAGE_1_NAMESPACE, "library_1_p1");
		CodeGenerator<TLLibrary> cg = CodeGeneratorFactory.getInstance().newCodeGenerator(
				CodeGeneratorFactory.XSD_TARGET_FORMAT, TLLibrary.class);
		
		cg.generateOutput(library, getContext());
	}
	
	@Test
	public void testGenerateLibraryXsd_package1_library2() throws Exception {
		TLLibrary library = getLibrary(PACKAGE_1_NAMESPACE, "library_2_p1");
		CodeGenerator<TLLibrary> cg = CodeGeneratorFactory.getInstance().newCodeGenerator(
				CodeGeneratorFactory.XSD_TARGET_FORMAT, TLLibrary.class);
		
		cg.generateOutput(library, getContext());
	}
	
	@Test
	public void testGenerateLibraryXsd_package2_library1() throws Exception {
		TLLibrary library = getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
		CodeGenerator<TLLibrary> cg = CodeGeneratorFactory.getInstance().newCodeGenerator(
				CodeGeneratorFactory.XSD_TARGET_FORMAT, TLLibrary.class);
		
		cg.generateOutput(library, getContext());
	}
	
	@Test
	public void testGenerateLibraryXsd_package2_library2() throws Exception {
		TLLibrary library = getLibrary(PACKAGE_2_NAMESPACE, "library_2_p2");
		CodeGenerator<TLLibrary> cg = CodeGeneratorFactory.getInstance().newCodeGenerator(
				CodeGeneratorFactory.XSD_TARGET_FORMAT, TLLibrary.class);
		
		cg.generateOutput(library, getContext());
	}
	
}

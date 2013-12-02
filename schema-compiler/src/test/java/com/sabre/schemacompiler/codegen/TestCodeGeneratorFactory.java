/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.Test;

import com.sabre.schemacompiler.codegen.wsdl.WsdlLibraryMemberCodeGenerator;
import com.sabre.schemacompiler.codegen.xsd.XsdUserLibraryCodeGenerator;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLService;

/**
 * Verifies the operation of the <code>CodeGeneratorFactory</code>.
 * 
 * @author S. Livezey
 */
public class TestCodeGeneratorFactory {
	
	@Test
	public void testCodeGeneratorFactoryForXSD() throws Exception {
		CodeGenerator<TLLibrary> generator = CodeGeneratorFactory.getInstance().newCodeGenerator(
				CodeGeneratorFactory.XSD_TARGET_FORMAT, TLLibrary.class);
		
		assertNotNull(generator);
		assertEquals(XsdUserLibraryCodeGenerator.class, generator.getClass());
	}
	
	@Test
	public void testCodeGeneratorFactoryForWSDL() throws Exception {
		CodeGenerator<TLService> generator = CodeGeneratorFactory.getInstance().newCodeGenerator(
				CodeGeneratorFactory.WSDL_TARGET_FORMAT, TLService.class);
		
		assertNotNull(generator);
		assertEquals(WsdlLibraryMemberCodeGenerator.class, generator.getClass());
	}
	
}

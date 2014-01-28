
package org.opentravel.schemacompiler.codegen;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.Test;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.wsdl.WsdlLibraryMemberCodeGenerator;
import org.opentravel.schemacompiler.codegen.xsd.XsdUserLibraryCodeGenerator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLService;

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

package org.opentravel.schemacompiler.codegen;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.model.TLService;

/**
 * Verifies the operation of the WSDL code generator.
 * 
 * @author S. Livezey
 */
public class TestWsdlCodeGenerator extends AbstractTestCodeGenerators {

    @Test
    public void testGenerateLibraryXsd_library1() throws Exception {
        TLService service = getService(PACKAGE_2_NAMESPACE, "library_1_p2");
        CodeGenerator<TLService> cg = CodeGeneratorFactory.getInstance().newCodeGenerator(
                CodeGeneratorFactory.WSDL_TARGET_FORMAT, TLService.class);

        assertEquals("SampleService", service.getName());
        cg.generateOutput(service, getContext());
    }

}

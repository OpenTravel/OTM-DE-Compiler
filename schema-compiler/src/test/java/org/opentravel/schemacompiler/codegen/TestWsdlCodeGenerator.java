/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

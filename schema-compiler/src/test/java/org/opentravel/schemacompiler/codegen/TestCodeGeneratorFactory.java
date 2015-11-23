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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

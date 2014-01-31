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

import org.junit.Test;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.model.TLLibrary;

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

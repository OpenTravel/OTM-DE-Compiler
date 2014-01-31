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
package org.opentravel.schemacompiler.codegen.impl;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;

/**
 * Transformer context that provides access to a <code>CodeGenerator</code> instance.
 * 
 * @author S. Livezey
 */
public class CodeGenerationTransformerContext extends DefaultTransformerContext {

    private CodeGenerator<?> codeGenerator;
    private CodeGenerationContext codegenContext;

    /**
     * Constructor that assigns the code generator for this transformation context.
     * 
     * @param codeGenerator
     *            the code generator instance to associate with this context
     */
    public CodeGenerationTransformerContext(CodeGenerator<?> codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    /**
     * Returns the code generator instance associated with this context.
     * 
     * @return CodeGenerator<?>
     */
    public CodeGenerator<?> getCodeGenerator() {
        return codeGenerator;
    }

    /**
     * Returns the current code generation context.
     * 
     * @return CodeGenerationContext
     */
    public CodeGenerationContext getCodegenContext() {
        return codegenContext;
    }

    /**
     * Assigns the current code generation context.
     * 
     * @param codegenContext
     *            the code generation context to assign
     */
    public void setCodegenContext(CodeGenerationContext codegenContext) {
        this.codegenContext = codegenContext;
    }

}

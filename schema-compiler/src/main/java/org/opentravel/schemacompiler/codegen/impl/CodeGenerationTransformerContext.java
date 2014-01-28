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

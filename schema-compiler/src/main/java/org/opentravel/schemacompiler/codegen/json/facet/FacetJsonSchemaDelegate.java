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
package org.opentravel.schemacompiler.codegen.json.facet;

import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.transform.TransformerFactory;

/**
 * Code generation delegate for facet used to separate logic and break up the complexity of the
 * facet artifact generation process.
 * 
 * @param <F>  the type of facet for which the delegate will generate artifacts
 */
public abstract class FacetJsonSchemaDelegate<F extends TLAbstractFacet>{
	
	private static FacetCodegenDelegateFactory xsdDelegateFactory = new FacetCodegenDelegateFactory( null );
	
    protected CodeGenerationTransformerContext transformerContext;
    private F sourceFacet;

    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet  the source facet
     */
    public FacetJsonSchemaDelegate(F sourceFacet) {
        this.sourceFacet = sourceFacet;
    }

    /**
     * Returns the source facet for this delegate instance.
     * 
     * @return F
     */
    protected F getSourceFacet() {
        return sourceFacet;
    }

    /**
     * Assigns the transformer context to use when processing facet sub-elements.
     * 
     * @param transformerContext
     *            the transformer context to assign
     */
    public void setTransformerContext(CodeGenerationTransformerContext transformerContext) {
        this.transformerContext = transformerContext;
    }

    /**
     * Adds the schemas associated with the given compile-time dependency to the current list of
     * dependencies maintained by the orchestrating code generator.
     * 
     * @param dependency
     *            the compile-time dependency to add
     */
    protected void addCompileTimeDependency(SchemaDependency dependency) {
        if (transformerContext != null) {
            CodeGenerator<?> codeGenerator = transformerContext.getCodeGenerator();

            if (codeGenerator instanceof AbstractJaxbCodeGenerator) {
                ((AbstractJaxbCodeGenerator<?>) codeGenerator).addCompileTimeDependency(dependency
                        .getSchemaDeclaration());
            }
        }
    }

    /**
     * Returns the transformer factory to use when obtaining object transformers for facet
     * sub-elements.
     * 
     * @return TransformerFactory<CodeGenerationTransformerContext>
     */
    @SuppressWarnings("unchecked")
    protected TransformerFactory<CodeGenerationTransformerContext> getTransformerFactory() {
        return (TransformerFactory<CodeGenerationTransformerContext>) transformerContext
                .getTransformerFactory();
    }
    
    /**
     * Returns true if the given source facet declares any content.
     * 
     * @return boolean
     */
    public final boolean hasContent() {
    	// Delegate this to the XSD code generator so we don't have to replicate
    	// all of the logic for JSON schema generation
    	return xsdDelegateFactory.getDelegate( sourceFacet ).hasContent();
    }

}

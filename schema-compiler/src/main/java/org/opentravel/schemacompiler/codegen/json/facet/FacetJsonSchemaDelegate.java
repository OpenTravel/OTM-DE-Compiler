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
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CorrelatedCodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.json.AbstractJsonSchemaCodeGenerator;
import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;
import org.opentravel.schemacompiler.codegen.json.JsonTypeNameBuilder;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.util.JsonSchemaNamingUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;

/**
 * Code generation delegate for facet used to separate logic and break up the complexity of the
 * facet artifact generation process.
 * 
 * @param <F>  the type of facet for which the delegate will generate artifacts
 */
public abstract class FacetJsonSchemaDelegate<F extends TLAbstractFacet> {
	
	/** Reference to the XSD facet delegates that helps to avoid duplication of OTM code generation logic. */
	protected static final FacetCodegenDelegateFactory xsdDelegateFactory = new FacetCodegenDelegateFactory( null );
	
    protected CodeGenerationTransformerContext transformerContext;
    protected JsonSchemaCodegenUtils jsonUtils;
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
    public F getSourceFacet() {
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
        this.jsonUtils = new JsonSchemaCodegenUtils( transformerContext );
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

            if (codeGenerator instanceof AbstractJsonSchemaCodeGenerator) {
                ((AbstractJsonSchemaCodeGenerator<?>) codeGenerator).addCompileTimeDependency(dependency
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
    	return xsdDelegateFactory.getDelegate( sourceFacet ).hasContent();
    }
    
    /**
     * Returns true if the facet supports an extension point element.
     * 
     * @return boolean
     */
    public final boolean hasExtensionPoint() {
    	return xsdDelegateFactory.getDelegate( sourceFacet ).hasExtensionPoint();
    }

    /**
     * Returns the facet instance that should serve as the base type for the source facet. The facet
     * that is returned by this method will always belong to the same owner as the given source
     * facet.
     * 
     * @return F
     */
    public final F getLocalBaseFacet() {
    	return xsdDelegateFactory.getDelegate( sourceFacet ).getLocalBaseFacet();
    }
    
    /**
     * Generates the code artifacts of the facet. Typically, the artifacts produced for each facet
     * include a JAXB type and a global element definition. Sub-classes may extend this method to
     * add additional artifacts as required.
     * 
     * @return CorrelatedCodegenArtifacts
     */
    public CorrelatedCodegenArtifacts generateArtifacts() {
    	CorrelatedCodegenArtifacts artifacts = new CorrelatedCodegenArtifacts();

        if (hasContent()) {
            artifacts.addArtifact( sourceFacet, createDefinition() );
        }
        return artifacts;
    }
    
    /**
     * Creates the JSON definiton for the facet.
     * 
     * @return JsonSchemaNamedReference
     */
    protected abstract JsonSchemaNamedReference createDefinition();
    
    /**
     * Returns the name of the element used to represent the source facet or the specified alias.
     * 
     * @param facetAlias  the alias of the source facet element being created (may be null)
     * @return String
     */
    protected final String getElementName(TLAlias facetAlias) {
    	String elementName;
    	
    	if (facetAlias != null) {
    		elementName = getDefinitionName( facetAlias );
    	} else {
    		elementName = getDefinitionName( sourceFacet );
    	}
    	return elementName;
    }
    
	/**
	 * Transforms the OTM documentation for the given owner and assigns it to the
	 * target JSON schema provided.
	 * 
	 * @param docOwner  the OTM documentation owner
	 * @param targetSchema  the target JSON schema that will receive the documentation
	 */
	protected void transformDocumentation(TLDocumentationOwner docOwner, JsonSchema targetSchema) {
		TLDocumentation doc = DocumentationFinder.getDocumentation( docOwner );
		
		if (doc != null) {
	        ObjectTransformer<TLDocumentation, JsonDocumentation, CodeGenerationTransformerContext> transformer =
	        		getTransformerFactory().getTransformer(doc, JsonDocumentation.class);
			
	        targetSchema.setDocumentation( transformer.transform( doc ) );
		}
	}
	
	/**
	 * Transforms the OTM documentation for the given owner and assigns it to the
	 * target schema reference provided.
	 * 
	 * @param docOwner  the OTM documentation owner
	 * @param targetRef  the target schema reference that will receive the documentation
	 */
	protected void transformDocumentation(TLDocumentationOwner docOwner, JsonSchemaReference targetRef) {
		TLDocumentation doc = DocumentationFinder.getDocumentation( docOwner );
		
		if (doc != null) {
	        ObjectTransformer<TLDocumentation, JsonDocumentation, CodeGenerationTransformerContext> transformer =
	        		getTransformerFactory().getTransformer(doc, JsonDocumentation.class);
			
	        targetRef.setDocumentation( transformer.transform( doc ) );
		}
	}
	
	/**
	 * Returns the definition name for the entity as it should be represented in the JSON schema.
	 * 
	 * @param entity  the entity for which to return a definition name
	 * @return String
	 */
	protected String getDefinitionName(NamedEntity entity) {
		JsonTypeNameBuilder nameBuilder = (JsonTypeNameBuilder)
				transformerContext.getContextCacheEntry( JsonTypeNameBuilder.class.getSimpleName() );
		String definitionName;
		
		if (nameBuilder != null) {
			definitionName = nameBuilder.getJsonTypeName( entity );
		} else {
			definitionName = JsonSchemaNamingUtils.getGlobalDefinitionName( entity );
		}
		return definitionName;
	}
	
	/**
	 * Returns true if the code generator has been configured to include all name references
	 * within the same set of definitions (i.e. within a single file).
	 * 
	 * @return boolean
	 */
	protected boolean isLocalNameReferencesEnabled() {
		return transformerContext.getContextCacheEntry(
				JsonTypeNameBuilder.class.getSimpleName() ) != null;
	}
	
}

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

package org.opentravel.schemacompiler.codegen.json;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodegenTransformer;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CorrelatedCodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.json.facet.FacetJsonSchemaDelegate;
import org.opentravel.schemacompiler.codegen.json.facet.FacetJsonSchemaDelegateFactory;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentationOwner;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.JsonSchemaNamingUtils;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLMemberFieldOwner;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all <code>ObjectTransformer</code> implementations that are part of the JSON schema code generation
 * subsystem.
 * 
 * @param <S> the source type of the object transformation
 * @param <T> the target type of the object transformation
 */
public abstract class AbstractJsonSchemaTransformer<S, T> extends AbstractCodegenTransformer<S,T> {

    public static final String MEMBER_FIELD_OWNER_KEY = TLMemberFieldOwner.class.getSimpleName();

    protected JsonSchemaCodegenUtils jsonUtils;

    /**
     * @see org.opentravel.schemacompiler.transform.util.BaseTransformer#setContext(org.opentravel.schemacompiler.transform.ObjectTransformerContext)
     */
    @Override
    public void setContext(CodeGenerationTransformerContext context) {
        super.setContext( context );
        jsonUtils = new JsonSchemaCodegenUtils( context );
    }

    /**
     * Returns the member field owner currently assigned to the transform context.
     * 
     * @return NamedEntity
     */
    protected NamedEntity getMemberFieldOwner() {
        return (NamedEntity) context.getContextCacheEntry( MEMBER_FIELD_OWNER_KEY );
    }

    /**
     * Assigns the given member field owner to the transform context.
     * 
     * @param fieldOwner the member field owner to assign
     */
    protected void setMemberFieldOwner(NamedEntity fieldOwner) {
        context.setContextCacheEntry( MEMBER_FIELD_OWNER_KEY, fieldOwner );
    }

    /**
     * Returns the definition name for the entity as it should be represented in the JSON schema.
     * 
     * @param entity the entity for which to return a definition name
     * @return String
     */
    protected String getDefinitionName(NamedEntity entity) {
        JsonTypeNameBuilder nameBuilder =
            (JsonTypeNameBuilder) context.getContextCacheEntry( JsonTypeNameBuilder.class.getSimpleName() );
        String definitionName;

        if (nameBuilder != null) {
            definitionName = nameBuilder.getJsonTypeName( entity );
        } else {
            definitionName = JsonSchemaNamingUtils.getGlobalDefinitionName( entity );
        }
        return definitionName;
    }

    /**
     * Transforms the OTM documentation for the given owner and assigns it to the target JSON schema provided.
     * 
     * @param docOwner the OTM documentation owner
     * @param targetSchema the target JSON schema that will receive the documentation
     */
    protected void transformDocumentation(TLDocumentationOwner docOwner, JsonDocumentationOwner targetSchema) {
        TLDocumentation doc = DocumentationFinder.getDocumentation( docOwner );

        if (doc != null) {
            ObjectTransformer<TLDocumentation,JsonDocumentation,CodeGenerationTransformerContext> transformer =
                getTransformerFactory().getTransformer( doc, JsonDocumentation.class );

            targetSchema.setDocumentation( transformer.transform( doc ) );
        }
    }

    /**
     * Recursively generates schema artifacts for all contextual facets in the given list.
     * 
     * @param facetList the list of contextual facets
     * @param isGhostFacets flag indicating whether the list of facets is comprised of ghosts
     * @param delegateFactory the facet code generation delegate factory
     * @param artifacts the container for all generated schema artifacts
     */
    protected void generateContextualFacetArtifacts(List<TLContextualFacet> facetList, boolean isGhostFacets,
        FacetJsonSchemaDelegateFactory delegateFactory, CorrelatedCodegenArtifacts artifacts) {
        for (TLContextualFacet facet : facetList) {
            if (isGhostFacets || facet.isLocalFacet()) {
                List<TLContextualFacet> ghostFacets = FacetCodegenUtils.findGhostFacets( facet, facet.getFacetType() );

                generateFacetArtifacts( delegateFactory.getDelegate( facet ), artifacts, isGhostFacets );
                generateContextualFacetArtifacts( facet.getChildFacets(), false, delegateFactory, artifacts );
                generateContextualFacetArtifacts( ghostFacets, true, delegateFactory, artifacts );
            }
        }
    }

    /**
     * Utility method that generates both element and non-element schema content for the source facet of the given
     * delegate.
     * 
     * @param facetDelegate the facet code generation delegate
     * @param artifacts the container for all generated schema artifacts
     * @param forceGeneration flag indicating whether the artifacts should generated regardless of the code generation
     *        filter status
     */
    protected void generateFacetArtifacts(FacetJsonSchemaDelegate<? extends TLFacet> facetDelegate,
        CorrelatedCodegenArtifacts artifacts, boolean forceGeneration) {
        CodeGenerationFilter filter = forceGeneration ? null : getCodegenFilter();

        if ((filter == null) || filter.processEntity( facetDelegate.getSourceFacet() )) {
            artifacts.addAllArtifacts( facetDelegate.generateArtifacts() );
        }
    }

    /**
     * Returns the code generation filter (if any) that is associated with the current transformation context.
     * 
     * @return CodeGenerationFilter
     */
    protected CodeGenerationFilter getCodegenFilter() {
        TransformerFactory<CodeGenerationTransformerContext> factory = getTransformerFactory();
        CodeGenerationTransformerContext context = (factory == null) ? null : factory.getContext();
        CodeGenerator<?> codeGenerator = (context == null) ? null : context.getCodeGenerator();

        return (codeGenerator == null) ? null : codeGenerator.getFilter();
    }

    /**
     * Adds the schemas associated with the given compile-time dependency to the current list of dependencies maintained
     * by the orchestrating code generator.
     * 
     * @param dependency the compile-time dependency to add
     */
    protected void addCompileTimeDependency(SchemaDependency dependency) {
        addCompileTimeDependency( dependency.getSchemaDeclaration() );
    }

    /**
     * Adds the schemas associated with the given compile-time dependency to the current list of dependencies maintained
     * by the orchestrating code generator.
     * 
     * @param schemaDeclaration the compile-time schema declaration to add
     */
    protected void addCompileTimeDependency(SchemaDeclaration schemaDeclaration) {
        CodeGenerator<?> codeGenerator = context.getCodeGenerator();

        if (codeGenerator instanceof AbstractJsonSchemaCodeGenerator) {
            ((AbstractJsonSchemaCodeGenerator<?>) codeGenerator).addCompileTimeDependency( schemaDeclaration );
        }
    }

    /**
     * Returns the list of compile-time schema dependencies that have been reported during code generation.
     * 
     * @return Collection&lt;SchemaDeclaration&gt;
     */
    protected Collection<SchemaDeclaration> getCompileTimeDependencies() {
        CodeGenerator<?> codeGenerator = context.getCodeGenerator();
        Collection<SchemaDeclaration> dependencies;

        if (codeGenerator instanceof AbstractJsonSchemaCodeGenerator) {
            dependencies = ((AbstractJsonSchemaCodeGenerator<?>) codeGenerator).getCompileTimeDependencies();
        } else {
            dependencies = Collections.emptySet();
        }
        return dependencies;
    }

}

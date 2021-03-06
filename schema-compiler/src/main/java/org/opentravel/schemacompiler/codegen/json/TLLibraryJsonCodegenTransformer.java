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
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.transform.ObjectTransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs the translation from <code>TLLibrary</code> objects to the <code>JsonSchema</code> objects used to produce
 * the output.
 */
public class TLLibraryJsonCodegenTransformer extends AbstractJsonSchemaTransformer<TLLibrary,JsonSchema> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public JsonSchema transform(TLLibrary source) {
        CodeGenerationFilter filter = context.getCodeGenerator().getFilter();
        JsonSchema schema = new JsonSchema( JsonSchema.JSON_SCHEMA_DRAFT4 );
        List<JsonSchemaReference> globalDefs = new ArrayList<>();

        schema.setTitle( source.getName() );
        schema.setDocumentation( new JsonDocumentation( source.getComments() ) );
        schema.setLibraryInfo( jsonUtils.getLibraryInfo( source ) );

        // Add entries for each non-service term declaration
        for (LibraryMember member : JsonSchemaCodegenUtils.getLatestMinorVersionMembers( source )) {
            transformLibraryMember( member, filter, schema, globalDefs );
        }

        // Generate output for non-local contextual "ghost" facets in this library
        transformNonLocalContextualFacets( source, filter, schema, globalDefs );

        // Add the list of global element definitions to the schema
        if (!globalDefs.isEmpty()) {
            schema.getOneOf().addAll( globalDefs );
        }

        return schema;
    }

    /**
     * Transforms all non-local contextual facets from the given library and adds them to the JSON schema provided.
     * 
     * @param source the source library being transformed
     * @param filter the code generation filter
     * @param schema the JSON schema being generated
     * @param globalDefs the list of global JSON schema definitions
     */
    private void transformNonLocalContextualFacets(TLLibrary source, CodeGenerationFilter filter, JsonSchema schema,
        List<JsonSchemaReference> globalDefs) {
        ObjectTransformer<TLContextualFacet,CodegenArtifacts,CodeGenerationTransformerContext> cfTransformer =
            getTransformerFactory().getTransformer( TLContextualFacet.class, CodegenArtifacts.class );

        for (TLContextualFacet facet : FacetCodegenUtils.findNonLocalGhostFacets( source )) {
            TLFacetOwner facetOwner = FacetCodegenUtils.getTopLevelOwner( facet );

            if ((filter == null) || filter.processEntity( facetOwner )) {
                CodegenArtifacts artifacts = cfTransformer.transform( facet );

                if (artifacts != null) {
                    for (JsonSchemaNamedReference memberDef : artifacts
                        .getArtifactsOfType( JsonSchemaNamedReference.class )) {
                        JsonSchemaReference globalDef = new JsonSchemaReference();

                        globalDef.setSchemaPath(
                            JsonSchemaCodegenUtils.getBaseDefinitionsPath( context ) + memberDef.getName() );
                        globalDefs.add( globalDef );
                        schema.getDefinitions().add( memberDef );
                    }
                    // for (JsonSchemaReference globalDef : artifacts.getArtifactsOfType( JsonSchemaReference.class )) {
                    // globalDefs.add( globalDef );
                    // }
                }
            }
        }
    }

    /**
     * Transforms the given library member and adds it to the JSON schema provided.
     * 
     * @param member the library member to be transformed
     * @param filter the code generation filter
     * @param schema the JSON schema being generated
     * @param globalDefs the list of global JSON schema definitions
     */
    private void transformLibraryMember(LibraryMember member, CodeGenerationFilter filter, JsonSchema schema,
        List<JsonSchemaReference> globalDefs) {
        ObjectTransformer<LibraryMember,CodegenArtifacts,CodeGenerationTransformerContext> transformer =
            getTransformerFactory().getTransformer( member, CodegenArtifacts.class );
        boolean isLocalFacet = (member instanceof TLContextualFacet) && ((TLContextualFacet) member).isLocalFacet();

        // Do not generate JSON schema definitions for OTM simple types due to swagger tooling conflicts
        if (member instanceof TLSimple) {
            return;
        }

        if ((transformer != null) && !isLocalFacet && ((filter == null) || filter.processEntity( member ))) {
            CodegenArtifacts artifacts = transformer.transform( member );

            if (artifacts != null) {
                for (JsonSchemaNamedReference memberDef : artifacts
                    .getArtifactsOfType( JsonSchemaNamedReference.class )) {
                    JsonSchemaReference globalDef = new JsonSchemaReference();

                    globalDef.setSchemaPath(
                        JsonSchemaCodegenUtils.getBaseDefinitionsPath( context ) + memberDef.getName() );
                    globalDefs.add( globalDef );
                    schema.getDefinitions().add( memberDef );
                }
                // for (JsonSchemaReference globalDef : artifacts.getArtifactsOfType( JsonSchemaReference.class )) {
                // globalDefs.add( globalDef );
                // }
            }
        }
    }

}

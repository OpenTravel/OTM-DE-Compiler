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

package org.opentravel.schemacompiler.codegen.openapi;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiDocument;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.transform.ObjectTransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs the translation from <code>TLResource</code> objects to the OpenAPI model objects used to produce the
 * output.
 */
public class TLResourceOpenApiTransformer extends AbstractOpenApiCodegenTransformer<TLResource,OpenApiDocument> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public OpenApiDocument transform(TLResource source) {
        OpenApiDocument openapiDoc = new OpenApiDocument();

        // If required, generate definitions for the Swagger document
        if (isSingleFileEnabled()) {
            openapiDoc.getDefinitions().addAll( buildJsonDefinitions( source.getOwningModel() ) );
        }

        applyBindingStyle( openapiDoc );

        return openapiDoc;
    }

    /**
     * Add any extensions provided by the OpenAPI code generation binding style.
     * 
     * @param openapiDoc the OpenAPI document to which binding styles will be applied
     */
    private void applyBindingStyle(OpenApiDocument openapiDoc) {
        // TODO: Implement OpenAPI binding style extensions
    }

    /**
     * Builds the set of all definitions that should be included in the Swagger document. The definitions that are
     * included are based on the current code generation filter.
     * 
     * @param model the model from which to generate JSON definitions
     * @return List&lt;JsonSchemaNamedReference&gt;
     */
    private List<JsonSchemaNamedReference> buildJsonDefinitions(TLModel model) {
        List<JsonSchemaNamedReference> definitions = new ArrayList<>();
        CodeGenerationFilter filter = context.getCodeGenerator().getFilter();

        // Add definitions for all of the OTM entities that are within the
        // scope of the current filter
        for (TLLibrary library : model.getUserDefinedLibraries()) {
            if (!filter.processLibrary( library )) {
                continue;
            }

            for (LibraryMember member : getLibraryMembers( library )) {
                if (member instanceof TLResource) {
                    for (TLActionFacet actionFacet : ((TLResource) member).getActionFacets()) {
                        transformEntity( actionFacet, definitions );
                    }

                } else {
                    transformEntity( member, definitions );
                }
            }
        }
        return definitions;
    }

    /**
     * Returns a list of all library members. This includes declared members as well as any non-local ghost facets that
     * might exist for the library.
     * 
     * @param library the library for which to return the list of all members
     * @return List&lt;LibraryMember&gt;
     */
    private List<LibraryMember> getLibraryMembers(TLLibrary library) {
        List<LibraryMember> allMembers = new ArrayList<>();

        allMembers.addAll( JsonSchemaCodegenUtils.getLatestMinorVersionMembers( library ) );
        allMembers.addAll( FacetCodegenUtils.findNonLocalGhostFacets( library ) );
        return allMembers;
    }

    /**
     * Transforms the given OTM entity and adds it JSON definition(s) the the list provided.
     * 
     * @param entity the OTM entity to be transformed
     * @param definitions the list of JSON definitions being constructed
     */
    private void transformEntity(NamedEntity entity, List<JsonSchemaNamedReference> definitions) {
        ObjectTransformer<NamedEntity,CodegenArtifacts,CodeGenerationTransformerContext> transformer =
            getTransformerFactory().getTransformer( entity, CodegenArtifacts.class );
        CodeGenerationFilter filter = context.getCodeGenerator().getFilter();

        if ((transformer != null) && ((filter == null) || filter.processEntity( entity ))) {
            CodegenArtifacts artifacts = transformer.transform( entity );

            if (artifacts != null) {
                for (JsonSchemaNamedReference memberDef : artifacts
                    .getArtifactsOfType( JsonSchemaNamedReference.class )) {
                    definitions.add( memberDef );
                }
            }
        }
    }

    /**
     * Returns true if single-file Swagger document generation is enabled.
     * 
     * @return boolean
     */
    private boolean isSingleFileEnabled() {
        CodeGenerationContext cgContext = (context == null) ? null : context.getCodegenContext();
        boolean result = false;

        if (cgContext != null) {
            result = "true".equalsIgnoreCase( cgContext.getValue( CodeGenerationContext.CK_ENABLE_SINGLE_FILE ) );
        }
        return result;
    }

}

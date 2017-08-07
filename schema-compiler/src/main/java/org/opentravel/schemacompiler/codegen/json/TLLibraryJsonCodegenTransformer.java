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

import java.util.ArrayList;
import java.util.List;

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

/**
 * Performs the translation from <code>TLLibrary</code> objects to the <code>JsonSchema</code>
 * objects used to produce the output.
 */
public class TLLibraryJsonCodegenTransformer extends AbstractJsonSchemaTransformer<TLLibrary, JsonSchema> {

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
        for (LibraryMember member : source.getNamedMembers()) {
            ObjectTransformer<LibraryMember, CodegenArtifacts, CodeGenerationTransformerContext> transformer = getTransformerFactory()
                    .getTransformer(member, CodegenArtifacts.class);
            boolean isLocalFacet = (member instanceof TLContextualFacet) && ((TLContextualFacet) member).isLocalFacet();
            
            // Do not generate JSON schema definitions for OTM simple types due to swagger tooling conflicts
            if (member instanceof TLSimple) continue;
            
            if ((transformer != null) && !isLocalFacet && ((filter == null) || filter.processEntity(member))) {
                CodegenArtifacts artifacts = transformer.transform(member);

                if (artifacts != null) {
                    for (JsonSchemaNamedReference memberDef : artifacts.getArtifactsOfType(JsonSchemaNamedReference.class)) {
                        schema.getDefinitions().add( memberDef );
                    }
                    for (JsonSchemaReference globalDef : artifacts.getArtifactsOfType(JsonSchemaReference.class)) {
                    	globalDefs.add( globalDef );
                    }
                }
            }
        }
        
        // Generate output for non-local contextual "ghost" facets in this library
        ObjectTransformer<TLContextualFacet, CodegenArtifacts, CodeGenerationTransformerContext> cfTransformer =
        		getTransformerFactory().getTransformer(TLContextualFacet.class, CodegenArtifacts.class);
        
        for (TLContextualFacet facet : FacetCodegenUtils.findNonLocalGhostFacets( source )) {
        	TLFacetOwner facetOwner = FacetCodegenUtils.getTopLevelOwner( facet );
        	
        	if ((filter == null) || filter.processEntity(facetOwner)) {
                CodegenArtifacts artifacts = cfTransformer.transform(facet);

                if (artifacts != null) {
                    for (JsonSchemaNamedReference memberDef : artifacts.getArtifactsOfType(JsonSchemaNamedReference.class)) {
                        schema.getDefinitions().add( memberDef );
                    }
                    for (JsonSchemaReference globalDef : artifacts.getArtifactsOfType(JsonSchemaReference.class)) {
                    	globalDefs.add( globalDef );
                    }
                }
        	}
        }
        
        // Add the list of global element definitions to the schema
        if (!globalDefs.isEmpty()) {
        	schema.getOneOf().addAll( globalDefs );
        }
        
        return schema;
	}
	
}

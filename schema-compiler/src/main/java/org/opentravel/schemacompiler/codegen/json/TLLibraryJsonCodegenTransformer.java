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
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;
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

        schema.setTitle( source.getName() );
        schema.setDocumentation( new JsonDocumentation( source.getComments() ) );
        schema.setLibraryInfo( jsonUtils.getLibraryInfo( source ) );
        
        // Add entries for each non-service term declaration
        for (LibraryMember member : source.getNamedMembers()) {
            ObjectTransformer<LibraryMember, CodegenArtifacts, CodeGenerationTransformerContext> transformer = getTransformerFactory()
                    .getTransformer(member, CodegenArtifacts.class);

            if ((transformer != null) && ((filter == null) || filter.processEntity(member))) {
                CodegenArtifacts artifacts = transformer.transform(member);

                if (artifacts != null) {
                    for (JsonSchemaNamedReference schemaDef : artifacts.getArtifactsOfType(JsonSchemaNamedReference.class)) {
                        schema.getDefinitions().add( schemaDef );
                    }
                }
            }
        }
        return schema;
	}

}

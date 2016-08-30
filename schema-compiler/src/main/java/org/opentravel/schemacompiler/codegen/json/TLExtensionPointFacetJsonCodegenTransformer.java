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

import java.util.List;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>TLExtensionPointFacet</code> objects to the JSON schema elements
 * used to produce the output.
 */
public class TLExtensionPointFacetJsonCodegenTransformer extends AbstractJsonSchemaTransformer<TLExtensionPointFacet, CodegenArtifacts> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public CodegenArtifacts transform(TLExtensionPointFacet source) {
        ObjectTransformer<TLAttribute, CodegenArtifacts, CodeGenerationTransformerContext> attributeTransformer = getTransformerFactory()
                .getTransformer(TLAttribute.class, CodegenArtifacts.class);
        ObjectTransformer<TLProperty, JsonSchemaNamedReference, CodeGenerationTransformerContext> elementTransformer = getTransformerFactory()
                .getTransformer(TLProperty.class, JsonSchemaNamedReference.class);
        ObjectTransformer<TLIndicator, JsonSchemaNamedReference, CodeGenerationTransformerContext> indicatorTransformer = getTransformerFactory()
                .getTransformer(TLIndicator.class, JsonSchemaNamedReference.class);
        List<TLAttribute> attributeList = source.getAttributes();
        List<TLProperty> elementList = source.getElements();
        List<TLIndicator> indicatorList = source.getIndicators();
        CodegenArtifacts artifacts = new CodegenArtifacts();
        JsonSchemaNamedReference definition = new JsonSchemaNamedReference();
		JsonSchema defSchema = new JsonSchema();
		
        definition.setName( getDefinitionName( source ) );
        definition.setSchema( new JsonSchemaReference( defSchema ) );
        transformDocumentation( source, defSchema );
        
        for (TLProperty element : elementList) {
        	defSchema.getProperties().add( elementTransformer.transform( element ) );
        }
        for (TLIndicator indicator : indicatorList) {
            if (indicator.isPublishAsElement()) {
            	defSchema.getProperties().add( indicatorTransformer.transform( indicator ) );
            }
        }
        for (TLAttribute attribute : attributeList) {
        	defSchema.getProperties().addAll( attributeTransformer.transform( attribute )
        			.getArtifactsOfType( JsonSchemaNamedReference.class ) );
        }
        for (TLIndicator indicator : indicatorList) {
            if (!indicator.isPublishAsElement()) {
            	defSchema.getProperties().add( indicatorTransformer.transform( indicator ) );
            }
        }
        artifacts.addArtifact( definition );
		return artifacts;
	}
	
}

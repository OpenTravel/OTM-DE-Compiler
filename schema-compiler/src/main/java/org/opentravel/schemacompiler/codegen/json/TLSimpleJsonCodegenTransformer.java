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

import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.model.TLSimple;

/**
 * Performs the translation from <code>TLSimple</code> objects to the JSON schema elements
 * used to produce the output.
 */
public class TLSimpleJsonCodegenTransformer extends AbstractJsonSchemaTransformer<TLSimple, CodegenArtifacts> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public CodegenArtifacts transform(TLSimple source) {
		JsonSchemaNamedReference simple = new JsonSchemaNamedReference();
		CodegenArtifacts artifacts = new CodegenArtifacts();
		
		simple.setName( source.getName() );
		
		if (source.isListTypeInd()) {
			JsonSchema schema = new JsonSchema();
			
			transformDocumentation( source, schema );
	        schema.setEntityInfo( jsonUtils.getEntityInfo( source ) );
	        
			schema.setType( JsonType.jsonArray );
			schema.setItems( new JsonSchemaReference(
					jsonUtils.getSchemaReferencePath( source.getParentType(), source ) ) );
			simple.setSchema( new JsonSchemaReference( schema ) );
			
		} else {
			simple.setSchema( createSimpleSchema( source ) );
		}
		
		artifacts.addArtifact( simple );
		return artifacts;
	}
	
	/**
	 * Constructs the JSON schema for the given simple type.
	 * 
	 * @param source  the source object being transformed
	 * @return JsonSchemaReference
	 */
	public JsonSchemaReference createSimpleSchema(TLSimple source) {
		JsonType type = JsonType.valueOf( source.getParentType() );
		JsonSchema simpleSchema = new JsonSchema();
		boolean hasRestrictions = false;
		JsonSchema restrictionsSchema;
		JsonSchemaReference schemaRef;
		
		transformDocumentation( source, simpleSchema );
		simpleSchema.setEntityInfo( jsonUtils.getEntityInfo( source ) );
		simpleSchema.getExampleItems().addAll( jsonUtils.getExampleInfo( source ) );
		simpleSchema.getEquivalentItems().addAll( jsonUtils.getEquivalentInfo( source ) );
		
		if (type != null) { // parent must be an XSD simple
			restrictionsSchema = simpleSchema;
			restrictionsSchema.setType( type );
		} else {
			restrictionsSchema = new JsonSchema();
		}
		
        if (source.getMinLength() > 0) {
        	restrictionsSchema.setMinLength( source.getMinLength() );
        	hasRestrictions = true;
        }
        if (source.getMaxLength() > 0) {
        	restrictionsSchema.setMaxLength( source.getMaxLength() );
        	hasRestrictions = true;
        }
        if (source.getFractionDigits() > 0) {
        	// No equivalent for fraction digits in JSON schema
        }
        if (source.getTotalDigits() > 0) {
        	// No equivalent for total digits in JSON schema
        }
        if ((source.getPattern() != null) && (source.getPattern().length() > 0)) {
        	restrictionsSchema.setPattern( source.getPattern() );
        	hasRestrictions = true;
        }
        if ((source.getMinInclusive() != null) && (source.getMinInclusive().length() > 0)) {
        	restrictionsSchema.setMinimum( Integer.parseInt( source.getMinInclusive() ) );
        	restrictionsSchema.setExclusiveMinimum( false );
        	hasRestrictions = true;
        }
        if ((source.getMaxInclusive() != null) && (source.getMaxInclusive().length() > 0)) {
        	restrictionsSchema.setMaximum( Integer.parseInt( source.getMaxInclusive() ) );
        	restrictionsSchema.setExclusiveMaximum( false );
        	hasRestrictions = true;
        }
        if ((source.getMinExclusive() != null) && (source.getMinExclusive().length() > 0)) {
        	restrictionsSchema.setMinimum( Integer.parseInt( source.getMinExclusive() ) );
        	restrictionsSchema.setExclusiveMinimum( true );
        	hasRestrictions = true;
        }
        if ((source.getMaxExclusive() != null) && (source.getMaxExclusive().length() > 0)) {
        	restrictionsSchema.setMaximum( Integer.parseInt( source.getMaxExclusive() ) );
        	restrictionsSchema.setExclusiveMaximum( true );
        	hasRestrictions = true;
        }
        
		if (type == null) { // parent is not an XSD simple
			if (hasRestrictions) {
				simpleSchema.getAllOf().add( new JsonSchemaReference(
						jsonUtils.getSchemaReferencePath( source.getParentType(), source ) ) );
				simpleSchema.getAllOf().add( new JsonSchemaReference( restrictionsSchema ) );
				schemaRef = new JsonSchemaReference( simpleSchema );
				
			} else {
				schemaRef = new JsonSchemaReference(
						jsonUtils.getSchemaReferencePath( source.getParentType(), source ) );
			}
		} else { // parent is an XSD simple
			schemaRef = new JsonSchemaReference( simpleSchema );
		}
		return schemaRef;
	}
	
}

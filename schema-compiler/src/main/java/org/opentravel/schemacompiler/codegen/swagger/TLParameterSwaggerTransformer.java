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
package org.opentravel.schemacompiler.codegen.swagger;

import java.util.List;

import org.opentravel.schemacompiler.codegen.impl.QualifiedParameter;
import org.opentravel.schemacompiler.codegen.json.TLSimpleJsonCodegenTransformer;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParamType;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParameter;
import org.opentravel.schemacompiler.codegen.util.EnumCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;

/**
 * Performs the translation from <code>QualifiedParameter</code> objects to the Swagger model
 * objects used to produce the output.
 */
public class TLParameterSwaggerTransformer extends AbstractSwaggerCodegenTransformer<QualifiedParameter,SwaggerParameter> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public SwaggerParameter transform(QualifiedParameter source) {
		TLMemberField<?> fieldRef = source.getParameter().getFieldRef();
		SwaggerParameter swaggerParam = new SwaggerParameter();
		
		swaggerParam.setName( source.getParameterName() );
		swaggerParam.setIn( getParamType( source.getParameter() ) );
		swaggerParam.setRequired( isRequired( fieldRef ) );
		swaggerParam.setType( getFieldSchema( fieldRef ) );
		
		if (fieldRef instanceof TLDocumentationOwner) {
			transformDocumentation( (TLDocumentationOwner) fieldRef, swaggerParam );
		}
		if (fieldRef instanceof TLEquivalentOwner) {
			swaggerParam.getEquivalentItems().addAll(
					jsonUtils.getEquivalentInfo( (TLEquivalentOwner) fieldRef ) );
		}
		if (fieldRef instanceof TLExampleOwner) {
			swaggerParam.getExampleItems().addAll(
					jsonUtils.getExampleInfo( (TLExampleOwner) fieldRef ) );
		}
		return swaggerParam;
	}
	
	/**
	 * Returns the JSON schema for the given source field.
	 * 
	 * @param sourceField  the source field for which to return the JSON schema
	 * @return JsonSchema
	 */
	private JsonSchema getFieldSchema(TLMemberField<?> sourceField) {
		JsonSchema schema = null;
		
		if (sourceField instanceof TLIndicator) {
			schema = new JsonSchema();
			schema.setType( JsonType.jsonBoolean );
			
		} else {
			NamedEntity fieldType = null;
			
			if (sourceField instanceof TLAttribute) {
				fieldType = ((TLAttribute) sourceField).getType();
			} else {
				fieldType = ((TLProperty) sourceField).getType();
			}
			
			if (fieldType != null) {
				JsonType jsonType = JsonType.valueOf( fieldType );
				schema = new JsonSchema();
				
				while ((fieldType != null) && (jsonType == null)) {
					if (fieldType instanceof TLSimple) {
						TLSimpleJsonCodegenTransformer.applyRestrictions( (TLSimple) fieldType, schema );
						
					} else if (fieldType instanceof TLClosedEnumeration) {
						applyEnumeration( (TLClosedEnumeration) fieldType, schema );
						break; // nothing left to do
					}
					fieldType = getParentType( fieldType );
					jsonType = JsonType.valueOf( fieldType );
				}
				schema.setType( jsonType );
			}
		}
		
		// Last resort if we could not identify a type - use a string
		if (schema.getType() == null) {
			schema.setType( JsonType.jsonString );
		}
		return schema;
	}
	
	/**
	 * Returns the parent type for the given entity type.
	 * 
	 * @param childType  the child type for which to return the parent
	 * @return NamedEntity
	 */
	private NamedEntity getParentType(NamedEntity childType) {
		NamedEntity parentType = null;
		
        if (childType instanceof TLCoreObject) {
            // Special Case: For core objects, use the simple facet as the attribute type
        	parentType = ((TLCoreObject) childType).getSimpleFacet().getSimpleType();

        } else if (childType instanceof TLRole) {
            // Special Case: For role assignments, use the core object's simple facet as the
            // attribute type
        	parentType = (((TLRole) childType).getRoleEnumeration().getOwningEntity())
        			.getSimpleFacet().getSimpleType();
        	
        } else if (childType instanceof TLSimpleFacet) {
        	parentType = ((TLSimpleFacet) childType).getSimpleType();
        	
        } else if (childType instanceof TLSimple) {
        	parentType = ((TLSimple) childType).getParentType();
        }
        return parentType;
	}
	
	/**
	 * Applies the values from the given closed enumeration to the target schema.
	 * 
	 * @param closedEnum  the closed enumeration
	 * @param targetSchema  the target schema that will receive the updates
	 */
	private void applyEnumeration(TLClosedEnumeration closedEnum, JsonSchema targetSchema) {
		List<String> enumValues = targetSchema.getEnumValues();
		
        for (TLEnumValue modelEnum : EnumCodegenUtils.getInheritedValues( closedEnum )) {
        	enumValues.add( modelEnum.getLiteral() );
        }
		targetSchema.setType( JsonType.jsonString );
	}
	
	/**
	 * Returns the Swagger parameter type for the source parameter.
	 * 
	 * @param source  the source parameter
	 * @return SwaggerParamType
	 */
	private SwaggerParamType getParamType(TLParameter source) {
		SwaggerParamType paramType = null;
		
		switch (source.getLocation()) {
			case PATH:
				paramType = SwaggerParamType.PATH;
				break;
			case QUERY:
				paramType = SwaggerParamType.QUERY;
				break;
			case HEADER:
				paramType = SwaggerParamType.HEADER;
				break;
			default:
				break;
		}
		return paramType;
	}
	
	/**
	 * Returns true if the given source field is required.
	 * 
	 * @param sourceField  the source field to check
	 * @return boolean
	 */
	private boolean isRequired(TLMemberField<?> sourceField) {
		boolean required = false;
		
		if (sourceField instanceof TLAttribute) {
			required = ((TLAttribute) sourceField).isMandatory();
			
		} else if (sourceField instanceof TLProperty) {
			required = ((TLProperty) sourceField).isMandatory();
		}
		return required;
	}
	
}

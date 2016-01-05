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

import org.opentravel.schemacompiler.codegen.json.model.JsonEntityInfo;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;

/**
 * Performs the translation from <code>TLProperty</code> objects to the JSON schema elements
 * used to produce the output.
 */
public class TLPropertyJsonCodegenTransformer extends AbstractJsonSchemaTransformer<TLProperty, JsonSchemaNamedReference> {

	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public JsonSchemaNamedReference transform(TLProperty source) {
		return source.isReference() ?
				transformValueProperty( source ) : transformReferenceProperty( source );
	}
	
    /**
     * Performs the transformation of the property as a standard value element.
     * 
     * @param source the source object being transformed
     * @return JsonSchemaNamedReference
     */
    private JsonSchemaNamedReference transformValueProperty(TLProperty source) {
		JsonSchemaNamedReference jsonProperty = new JsonSchemaNamedReference();
    	JsonSchemaReference schemaRef = new JsonSchemaReference();
        TLPropertyType propertyType = PropertyCodegenUtils.resolvePropertyType(
                source.getOwner(), source.getType());

        if (!PropertyCodegenUtils.hasGlobalElement(propertyType)) {
            // If the element's name has not been specified, use the name of its assigned type
            if ((source.getName() == null) || (source.getName().length() == 0)) {
            	jsonProperty.setName( source.getType().getLocalName() );
            } else {
            	jsonProperty.setName( source.getName() );
            }

        } else {
            // If the property references a type that defines a global element, use that
        	// element name for the JSON property name
        	jsonProperty.setName( PropertyCodegenUtils.getDefaultSchemaElementName(
        			propertyType, false).getLocalPart() );
        }
    	setPropertyType( schemaRef, propertyType, source );
    	jsonProperty.setSchema( schemaRef );
    	jsonProperty.setRequired( source.isMandatory() );
		return jsonProperty;
    }
    
    /**
     * Performs the transformation of the property as the JSON equivalent of an IDREF(S)
     * element.
     * 
     * @param source the source object being transformed
     * @return JsonSchemaNamedReference
     */
    private JsonSchemaNamedReference transformReferenceProperty(TLProperty source) {
        TLPropertyType propertyType = PropertyCodegenUtils.resolvePropertyType(
                source.getOwner(), source.getType());
		JsonSchemaNamedReference jsonProperty = new JsonSchemaNamedReference();
		JsonSchema propertySchema = new JsonSchema();
        JsonEntityInfo entityInfo = new JsonEntityInfo();
        String elementName = source.getName();
        String maxOccurs = PropertyCodegenUtils.getMaxOccurs(source);
        boolean isMultipleReference;

        if (PropertyCodegenUtils.hasGlobalElement(propertyType)) {
            elementName = PropertyCodegenUtils.getDefaultSchemaElementName(propertyType, true).getLocalPart();

        } else {
            elementName = source.getName();

            if (!elementName.endsWith("Ref")) {
                // probably a VWA reference, so we need to make sure the "Ref" suffix is appended
                elementName += "Ref";
            }
        }
        jsonProperty.setName( elementName );
        entityInfo.setEntityName( source.getType().getLocalName() );

        if (maxOccurs == null) {
            isMultipleReference = false;

        } else if (maxOccurs.equals("unbounded")) {
            isMultipleReference = true;

        } else {
            try {
                isMultipleReference = Integer.parseInt(maxOccurs) > 1;

            } catch (NumberFormatException e) {
                // should never happen, but just in case...
                isMultipleReference = false;
            }
        }
        
        if (isMultipleReference) { // Array of references
        	JsonSchema itemSchema = new JsonSchema();
        	
        	itemSchema.setType( JsonType.jsonString );
        	propertySchema.setType( JsonType.jsonArray );
        	propertySchema.setItems( new JsonSchemaReference( itemSchema ) );
            entityInfo.setEntityType( JsonType.jsonString.getSchemaType() );
        	
        } else { // Single reference
        	propertySchema.setType( JsonType.jsonString );
            entityInfo.setEntityType( JsonType.jsonArray.getSchemaType() +
            		"[" + JsonType.jsonString.getSchemaType() + "]" );
        }
        jsonProperty.setSchema( new JsonSchemaReference( propertySchema ) );
        
		transformDocumentation( source, propertySchema );
		propertySchema.getEquivalentItems().addAll( JsonCodegenUtils.getEquivalentInfo( source ) );
		propertySchema.getExampleItems().addAll( JsonCodegenUtils.getExampleInfo( source ) );
		
		return jsonProperty;
    }
    
	/**
	 * Assigns the attribute type of the JSON schema reference provided.
	 * 
	 * @param attrSchemaRef  the JSON schema reference to which the attribute type will be assigned
	 * @param attributeType  the attribute type to assign
	 * @param source  the source attribute from the OTM model
	 */
	private void setPropertyType(JsonSchemaReference schemaRef, TLPropertyType propertyType, TLProperty source) {
        JsonType jsonType = JsonType.valueOf( propertyType );
        JsonSchemaReference typeRef = schemaRef;
        JsonSchemaReference docSchema = schemaRef;
        
        if (source.getRepeat() > 1) {
        	JsonSchemaReference itemSchemaRef = new JsonSchemaReference();
        	JsonSchema arraySchema = new JsonSchema();
        	
        	arraySchema.setType( JsonType.jsonArray );
        	arraySchema.setMinItems( source.isMandatory() ? 1 : 0 );
        	arraySchema.setItems( itemSchemaRef );
        	typeRef = itemSchemaRef;
        	
			if (propertyType instanceof TLListFacet) {
				TLCoreObject facetOwner = (TLCoreObject) ((TLListFacet) propertyType).getOwningEntity();
				
				if (facetOwner.getRoleEnumeration().getRoles().size() > 0) {
		        	arraySchema.setMaxItems( facetOwner.getRoleEnumeration().getRoles().size() );
				} else {
					if (source.getRepeat() <= PropertyCodegenUtils.MAX_OCCURS_UNBOUNDED_THRESHOLD) {
						arraySchema.setMaxItems( source.getRepeat() );
					}
				}
			} else {
				if (source.getRepeat() <= PropertyCodegenUtils.MAX_OCCURS_UNBOUNDED_THRESHOLD) {
					arraySchema.setMaxItems( source.getRepeat() );
				}
			}
        }
        
        if (jsonType != null) {
        	JsonSchema propertySchema = new JsonSchema();
        	
        	if (typeRef == schemaRef) { // not an array, so put the documentation here
        		transformDocumentation( source, propertySchema );
        		propertySchema.getEquivalentItems().addAll( JsonCodegenUtils.getEquivalentInfo( source ) );
        		propertySchema.getExampleItems().addAll( JsonCodegenUtils.getExampleInfo( source ) );
        		docSchema = null;
        	}
        	propertySchema.setType( jsonType );
    		typeRef.setSchema( propertySchema );
    		
        } else {
    		typeRef.setSchemaPath( getSchemaReferencePath( propertyType, source.getOwner() ) );
        }
        
        if (docSchema != null) {
    		transformDocumentation( source, docSchema );
    		docSchema.getSchemaPathEquivalentItems().addAll( JsonCodegenUtils.getEquivalentInfo( source ) );
    		docSchema.getSchemaPathExampleItems().addAll( JsonCodegenUtils.getExampleInfo( source ) );
        }
	}
	
}

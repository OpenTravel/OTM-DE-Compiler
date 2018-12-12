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

import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonEntityInfo;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.JsonSchemaNamingUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.util.SimpleTypeInfo;

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
				transformReferenceProperty( source ) : transformValueProperty( source );
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
        TLPropertyType propertyType = PropertyCodegenUtils.resolvePropertyType(source.getType());

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
        	jsonProperty.setName( JsonSchemaNamingUtils.getGlobalPropertyName( propertyType, false) );
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
        TLPropertyType propertyType = PropertyCodegenUtils.resolvePropertyType(source.getType());
		JsonSchemaNamedReference jsonProperty = new JsonSchemaNamedReference();
		JsonSchema propertySchema = new JsonSchema();
        JsonEntityInfo entityInfo = new JsonEntityInfo();
        String maxOccurs = PropertyCodegenUtils.getMaxOccurs(source);
        boolean isMultipleReference;
        String elementName;

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
        	
        	itemSchema.setType( JsonType.JSON_STRING );
        	propertySchema.setType( JsonType.JSON_ARRAY );
        	propertySchema.setItems( new JsonSchemaReference( itemSchema ) );
            entityInfo.setEntityType( JsonType.JSON_STRING.getSchemaType() );
        	
        } else { // Single reference
        	propertySchema.setType( JsonType.JSON_STRING );
            entityInfo.setEntityType( JsonType.JSON_ARRAY.getSchemaType() +
            		"[" + JsonType.JSON_STRING.getSchemaType() + "]" );
        }
        jsonProperty.setSchema( new JsonSchemaReference( propertySchema ) );
        
		transformDocumentation( source, propertySchema );
		propertySchema.getEquivalentItems().addAll( jsonUtils.getEquivalentInfo( source ) );
		propertySchema.getExampleItems().addAll( jsonUtils.getExampleInfo( source ) );
		
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
		SimpleTypeInfo simpleInfo = SimpleTypeInfo.newInstance( propertyType );
        JsonType jsonType = (simpleInfo == null) ? null : JsonType.valueOf( simpleInfo.getBaseSimpleType() );
        JsonSchemaReference typeRef = schemaRef;
        JsonSchemaReference docSchema = schemaRef;
    	String maxOccurs = null;
    	
    	// Calculate the max-occurs based on the source property or the property
    	// type (in the case of non-simple list facets).
    	TLPropertyType maxOccursType = propertyType;
    	
    	if (maxOccursType instanceof TLAlias) {
    		maxOccursType = (TLPropertyType) ((TLAlias) maxOccursType).getOwningEntity();
    	}
		if (maxOccursType instanceof TLListFacet) {
			TLListFacet listFacet = (TLListFacet) maxOccursType;
			
			if (!(listFacet.getItemFacet() instanceof TLSimpleFacet)) {
				TLCoreObject facetOwner = (TLCoreObject) listFacet.getOwningEntity();
				
				if (!facetOwner.getRoleEnumeration().getRoles().isEmpty()) {
					maxOccurs = facetOwner.getRoleEnumeration().getRoles().size() + "";
				} else {
					maxOccurs = PropertyCodegenUtils.getMaxOccurs( source );
				}
			}
		} else if ((source.getRepeat() < 0) || (source.getRepeat() > 1)) {
			maxOccurs = PropertyCodegenUtils.getMaxOccurs( source );
        }
		
		// If a max-occurs was specified, the resulting property schema should be an array
		if (maxOccurs != null) {
        	JsonSchemaReference itemSchemaRef = new JsonSchemaReference();
        	JsonSchema arraySchema = new JsonSchema();
        	
        	arraySchema.setType( JsonType.JSON_ARRAY );
        	arraySchema.setMinItems( source.isMandatory() ? 1 : null );
        	arraySchema.setItems( itemSchemaRef );
        	schemaRef.setSchema( arraySchema );
        	typeRef = itemSchemaRef;
        	
			if (!maxOccurs.equals("unbounded")) {
	        	arraySchema.setMaxItems( Integer.valueOf( maxOccurs ) );
			}
		}
        
        if ((jsonType != null) && !(source.getType() instanceof TLValueWithAttributes)
        		 && !(source.getType() instanceof TLCoreObject)) { // VWA's and cores are special cases
        	JsonSchema propertySchema = jsonUtils.buildSimpleTypeSchema( simpleInfo, jsonType );
        	
        	if (typeRef == schemaRef) { // not an array, so put the documentation here
        		transformDocumentation( source, propertySchema );
        		propertySchema.getEquivalentItems().addAll( jsonUtils.getEquivalentInfo( source ) );
        		propertySchema.getExampleItems().addAll( jsonUtils.getExampleInfo( source ) );
        		docSchema = null;
        	}
    		typeRef.setSchema( propertySchema );
    		
        } else if ((propertyType instanceof XSDSimpleType) || (propertyType instanceof XSDComplexType)
        		|| (propertyType instanceof XSDElement)) {
        	JsonDocumentation doc = new JsonDocumentation();
        	JsonSchema typeSchema = new JsonSchema();
        	
        	doc.setDescriptions( "Legacy XML schema reference - {" +
        			propertyType.getNamespace() + "}" + propertyType.getLocalName() );
        	typeSchema.setDocumentation( doc );
        	typeRef.setSchema( typeSchema );
    		
        } else {
        	TLPropertyType baseType = propertyType;
        	TLAlias alias = null;
        	
        	if (baseType instanceof TLAlias) {
        		alias = ((TLAlias) baseType);
        		baseType = (TLPropertyType) alias.getOwningEntity();
        	}
        	if (baseType instanceof TLListFacet) {
        		TLAbstractFacet facet = ((TLListFacet) baseType).getItemFacet();
        		
        		if (facet instanceof TLFacet) {
            		TLFacet itemFacet = (TLFacet) facet;
            		
            		if (alias != null) {
            			TLAlias ownerAlias = AliasCodegenUtils.getOwnerAlias( alias );
            			alias = AliasCodegenUtils.getFacetAlias( ownerAlias, itemFacet.getFacetType(),
            					FacetCodegenUtils.getFacetName( itemFacet ) );
            		}
            		baseType = itemFacet;
        		}
        	}
        	
        	if (alias != null) {
        		typeRef.setSchemaPath( jsonUtils.getSchemaReferencePath( alias, getMemberFieldOwner() ) );
        	} else {
        		typeRef.setSchemaPath( jsonUtils.getSchemaReferencePath( baseType, getMemberFieldOwner() ) );
        	}
        }
        
        if (docSchema != null) {
    		transformDocumentation( source, docSchema );
    		jsonUtils.applySimpleTypeDocumentation( docSchema, source.getType() );
    		docSchema.getEquivalentItems().addAll( jsonUtils.getEquivalentInfo( source ) );
    		docSchema.getExampleItems().addAll( jsonUtils.getExampleInfo( source ) );
        }
	}
	
}

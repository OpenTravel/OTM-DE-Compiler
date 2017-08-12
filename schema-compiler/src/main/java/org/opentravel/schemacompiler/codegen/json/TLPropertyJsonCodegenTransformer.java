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

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
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
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
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
public class TLPropertyJsonCodegenTransformer extends AbstractJsonSchemaTransformer<TLProperty, CodegenArtifacts> {
	
	private FacetCodegenDelegateFactory delegateFactory = new FacetCodegenDelegateFactory( null );
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public CodegenArtifacts transform(TLProperty source) {
    	CodegenArtifacts artifacts = new CodegenArtifacts();
    	
    	if (source.isReference()) {
    		artifacts.addArtifact( transformReferenceProperty( source ) );
    		
    	} else {
    		artifacts.addAllArtifacts( transformValueProperty( source ) );
    	}
    	return artifacts;
	}
	
    /**
     * Performs the transformation of the property as a standard value element.
     * 
     * @param source the source object being transformed
     * @return List<JsonSchemaNamedReference>
     */
    private List<JsonSchemaNamedReference> transformValueProperty(TLProperty source) {
    	List<TLPropertyType> propertyTypes = getResolvedPropertyTypes( source );
    	boolean isSubstitutableProperty = (propertyTypes.size() > 1);
    	List<JsonSchemaNamedReference> jsonProperties = new ArrayList<>();
    	StringBuilder subgrpDefinition = new StringBuilder();
    	
    	for (TLPropertyType propertyType : propertyTypes) {
    		JsonSchemaNamedReference jsonProperty = new JsonSchemaNamedReference();
        	JsonSchemaReference schemaRef = new JsonSchemaReference();

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
            	if (isSubstitutableProperty) {
            		QName substitutableName = null;
            		
            		if (propertyType instanceof TLAlias) {
                    	substitutableName = XsdCodegenUtils.getSubstitutableElementName( (TLAlias) propertyType );
            			
            		} else if (propertyType instanceof TLFacet) {
                    	substitutableName = XsdCodegenUtils.getSubstitutableElementName( (TLFacet) propertyType );
            		}
            		
            		if (substitutableName != null) {
            			jsonProperty.setName( substitutableName.getLocalPart() );
            		} else {
                    	jsonProperty.setName( JsonSchemaNamingUtils.getGlobalPropertyName( propertyType, false) );
            		}
            		
            	} else {
                	jsonProperty.setName( JsonSchemaNamingUtils.getGlobalPropertyName( propertyType, false) );
            	}
            }
            
            // Begin building the label, just in case we have a substitutable element
            if (subgrpDefinition.length() > 0) {
            	subgrpDefinition.append(", ");
            }
            subgrpDefinition.append( jsonProperty.getName() );
            
        	setPropertyType( schemaRef, propertyType, source );
        	jsonProperty.setSchema( schemaRef );
        	jsonProperty.setRequired( source.isMandatory() );
        	jsonProperties.add( jsonProperty );
    	}
    	
    	// Add some supplemental documentation for substituable properties
    	if (isSubstitutableProperty) {
    		String docText = "Only one allowed of: " + subgrpDefinition.toString();
    		
    		for (JsonSchemaNamedReference jsonProperty : jsonProperties) {
    			jsonUtils.applySupplementalDescription( jsonProperty.getSchema(), docText );
    		}
    	}
    	
		return jsonProperties;
    }
    
    /**
     * Returns a list of property types for which to render JSON schema properties.  If
     * the assigned type of the given property is a top-level business, choice, or core
     * object, the resulting list may contain multiple entries, one for each possible
     * facet of the entity.  If the assigned type is not substitutable, a single-value
     * list containing the original resolved property type will be returned.
     * 
     * @param source
     * @return
     */
    private List<TLPropertyType> getResolvedPropertyTypes(TLProperty source) {
        TLPropertyType assignedType = PropertyCodegenUtils.resolvePropertyType(
                source.getOwner(), source.getType());
        List<TLPropertyType> propertyTypes = new ArrayList<>();
    	List<TLFacet> candidateFacets = new ArrayList<>();
        TLAlias assignedAlias = null;
    	
        if (assignedType instanceof TLAlias) {
        	assignedAlias = (TLAlias) assignedType;
        	assignedType = (TLPropertyType) assignedAlias.getOwningEntity();
        }
        
    	if (assignedType instanceof TLBusinessObject) {
    		TLBusinessObject entity = (TLBusinessObject) assignedType;
    		
    		candidateFacets.add( entity.getIdFacet() );
    		candidateFacets.add( entity.getSummaryFacet() );
    		addContextualFacets( entity.getCustomFacets(), candidateFacets );
    		candidateFacets.add( entity.getDetailFacet() );
    		
    	} else if (assignedType instanceof TLChoiceObject) {
    		TLChoiceObject entity = (TLChoiceObject) assignedType;
    		
    		candidateFacets.add( entity.getSharedFacet() );
    		addContextualFacets( entity.getChoiceFacets(), candidateFacets );
    		
    	} else if (assignedType instanceof TLCoreObject) {
    		TLCoreObject entity = (TLCoreObject) assignedType;
    		
    		candidateFacets.add( entity.getSummaryFacet() );
    		candidateFacets.add( entity.getDetailFacet() );
    	}
    	
    	if (!candidateFacets.isEmpty()) {
    		for (TLFacet facet : candidateFacets) {
    			FacetCodegenDelegate<TLFacet> facetDelegate = delegateFactory.getDelegate( facet );
    			
    			if ((facetDelegate != null) && facetDelegate.hasContent()) {
    				if (assignedAlias != null) {
    					AliasCodegenUtils.getFacetAlias( assignedAlias,
    							facet.getFacetType(), FacetCodegenUtils.getFacetName( facet ) );
    					
    				} else {
        				propertyTypes.add( facet );
    				}
    			}
    		}
    		
    	} else {
    		propertyTypes.add( assignedType ); // not a substitution group
    	}
    	return propertyTypes;
    }
    
    /**
     * Adds the given list of contextual facets and all of thier children to
     * the given target list.
     * 
     * @param sourceList  the source list of contextual facets
     * @param targetList  the list of facets being constructed
     */
    private void addContextualFacets(List<TLContextualFacet> sourceList, List<TLFacet> targetList) {
    	for (TLContextualFacet facet : sourceList) {
    		targetList.add( facet );
    		addContextualFacets( facet.getChildFacets(), targetList );
    	}
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
				
				if (facetOwner.getRoleEnumeration().getRoles().size() > 0) {
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
        	
        	arraySchema.setType( JsonType.jsonArray );
        	arraySchema.setMinItems( source.isMandatory() ? 1 : null );
        	arraySchema.setItems( itemSchemaRef );
        	schemaRef.setSchema( arraySchema );
        	typeRef = itemSchemaRef;
        	
			if (!maxOccurs.equals("unbounded")) {
	        	arraySchema.setMaxItems( Integer.valueOf( maxOccurs ) );
			}
		}
        
        if ((jsonType != null) && !(source.getType() instanceof TLValueWithAttributes)) {
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
    		docSchema.getEquivalentItems().addAll( jsonUtils.getEquivalentInfo( source ) );
    		docSchema.getExampleItems().addAll( jsonUtils.getExampleInfo( source ) );
    		
    		if (simpleInfo != null) {
        		jsonUtils.applySimpleTypeDocumentation( docSchema, source.getType() );
    		}
        }
	}
	
}

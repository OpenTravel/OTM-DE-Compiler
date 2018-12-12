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
package org.opentravel.schemacompiler.codegen.util;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * Static utility methods used to obtain schema property and definition names for
 * OTM entities.
 */
public class JsonSchemaNamingUtils {
	
	/**
	 * Private constructor to prevent instantiation.
	 */
	private JsonSchemaNamingUtils() {}
	
    /**
     * Returns the globally-accessible type name for the given entity in the XML schema output.
     * <p>
     * NOTE: In some cases (e.g. action facets of abstract resources), the resulting value may
     * be null.  This indicates that there is no global schema type name associated with the
     * given model entity.
     * 
     * @param modelEntity  the model entity for which to return the element name
     * @return String
     */
    public static String getGlobalDefinitionName(NamedEntity modelEntity) {
    	String definitionName = getGlobalElementName( modelEntity );
    	
    	if (definitionName == null) {
    		definitionName = XsdCodegenUtils.getGlobalTypeName( modelEntity );
    	}
        return definitionName;
    }

    /**
     * Returns the globally-accessible type name for the given entity in the XML schema output.
     * <p>
     * NOTE: In some cases (e.g. action facets of abstract resources), the resulting value may
     * be null.  This indicates that there is no global schema type name associated with the
     * given model entity.
     * 
     * @param modelEntity  the model entity for which to return the element name
     * @param referencingProperty
     * @return String
     */
    public static String getGlobalDefinitionName(NamedEntity modelEntity, TLProperty referencingProperty) {
    	String definitionName = getGlobalElementName( modelEntity );
    	
    	if (definitionName == null) {
    		definitionName = XsdCodegenUtils.getGlobalTypeName( modelEntity, referencingProperty );
    	}
        return definitionName;
    }
    
    /**
     * Returns the global XML element name for the given entity or null if the entity
     * does not have one.
     * 
     * @param modelEntity  the model entity for which to return the element name
     * @return String
     */
    private static String getGlobalElementName(NamedEntity modelEntity) {
    	QName elementName = null;
    	
    	if (modelEntity instanceof TLAlias) {
    		TLAliasOwner aliasOwner = ((TLAlias) modelEntity).getOwningEntity();
    		
    		if (aliasOwner instanceof TLFacet) {
    			elementName = XsdCodegenUtils.getSubstitutableElementName( (TLAlias) modelEntity );
    			
    		} else {
        		TLFacet facet = null;
        		
        		if (aliasOwner instanceof TLBusinessObject) {
        			facet = ((TLBusinessObject) aliasOwner).getIdFacet();
        			
        		} else if (aliasOwner instanceof TLCoreObject) {
        			facet = ((TLCoreObject) aliasOwner).getSummaryFacet();
        			
        		} else if (aliasOwner instanceof TLChoiceObject) {
        			facet = ((TLChoiceObject) aliasOwner).getSharedFacet();
        		}
        		
        		if (facet != null) {
        			TLAlias facetAlias = AliasCodegenUtils.getFacetAlias(
        					(TLAlias) modelEntity, facet.getFacetType() );
        			
        			elementName = XsdCodegenUtils.getSubstitutableElementName( facetAlias );
        		}
    		}
    		
    		// Last resort since all aliases must have a global element name
    		if (elementName == null) {
        		elementName = XsdCodegenUtils.getGlobalElementName( modelEntity );
    		}
    		
    	} else {
    		TLFacet entityFacet = null;
    		
    		if (modelEntity instanceof TLFacet) {
    			entityFacet = (TLFacet) modelEntity;
    			
    		} else if (modelEntity instanceof TLBusinessObject) {
    			entityFacet = ((TLBusinessObject) modelEntity).getIdFacet();
    			
    		} else if (modelEntity instanceof TLCoreObject) {
    			entityFacet = ((TLCoreObject) modelEntity).getSummaryFacet();
    			
    		} else if (modelEntity instanceof TLChoiceObject) {
    			entityFacet = ((TLChoiceObject) modelEntity).getSharedFacet();
    		}
    		
    		if (entityFacet != null) {
    			elementName = XsdCodegenUtils.getSubstitutableElementName( entityFacet );
    		}
    	}
    	return (elementName == null) ? null : elementName.getLocalPart();
    }
    
    /**
     * Returns the globally-accessible type name for the given entity in the XML schema output.
     * <p>
     * NOTE: In some cases (e.g. action facets of abstract resources), the resulting value may
     * be null.  This indicates that there is no global schema type name associated with the
     * given model entity.
     * 
     * @param modelEntity  the model entity for which to return the element name
     * @return String
     */
    public static String getGlobalPropertyName(NamedEntity modelEntity) {
    	return getGlobalPropertyName( modelEntity, false );
    }

    /**
     * Returns the globally-accessible type name for the given entity in the XML schema output.
     * <p>
     * NOTE: In some cases (e.g. action facets of abstract resources), the resulting value may
     * be null.  This indicates that there is no global schema type name associated with the
     * given model entity.
     * 
     * @param modelEntity  the model entity for which to return the element name
     * @param isReferenceProperty  flag indicating whether the property name should represent an IDREF
     * @return String
     */
    public static String getGlobalPropertyName(NamedEntity modelEntity, boolean isReferenceProperty) {
    	QName propertyName = PropertyCodegenUtils.getDefaultXmlElementName( modelEntity, isReferenceProperty );
    	return (propertyName == null) ? null : propertyName.getLocalPart();
    }
    
}

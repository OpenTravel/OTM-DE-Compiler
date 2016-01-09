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
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * Static utility methods used to obtain schema property and definition names for
 * OTM entities.
 */
public class JsonSchemaNamingUtils {
	
    /**
     * Returns the globally-accessible type name for the given entity in the XML schema output.
     * <p>
     * NOTE: In some cases (e.g. action facets of abstract resources), the resulting value may
     * be null.  This indicates that there is no global schema type name associated with the
     * given model entity.
     * 
     * @param modelEntity
     *            the model entity for which to return the element name
     * @return String
     */
    public static String getGlobalDefinitionName(NamedEntity modelEntity) {
        return XsdCodegenUtils.getGlobalTypeName( modelEntity );
    }

    /**
     * Returns the globally-accessible type name for the given entity in the XML schema output.
     * <p>
     * NOTE: In some cases (e.g. action facets of abstract resources), the resulting value may
     * be null.  This indicates that there is no global schema type name associated with the
     * given model entity.
     * 
     * @param modelEntity
     *            the model entity for which to return the element name
     * @param referencingProperty
     * @return String
     */
    public static String getGlobalDefinitionName(NamedEntity modelEntity, TLProperty referencingProperty) {
        return XsdCodegenUtils.getGlobalTypeName( modelEntity, referencingProperty );
    }
    
    /**
     * Returns the globally-accessible type name for the given entity in the XML schema output.
     * <p>
     * NOTE: In some cases (e.g. action facets of abstract resources), the resulting value may
     * be null.  This indicates that there is no global schema type name associated with the
     * given model entity.
     * 
     * @param modelEntity
     *            the model entity for which to return the element name
     * @return String
     */
    public static String getGlobalPropertyName(NamedEntity modelEntity) {
    	return getGlobalPropertyName( modelEntity, false );
    	/*
    	String propertyName;
    	
    	if ((modelEntity instanceof TLBusinessObject) || (modelEntity instanceof TLCoreObject)
    			|| (modelEntity instanceof TLChoiceObject)) {
    		propertyName = modelEntity.getLocalName();
    		
    	} else {
    		QName propertyQName = XsdCodegenUtils.getGlobalElementName( modelEntity );
    		propertyName = (propertyQName == null) ? null : propertyQName.getLocalPart();
    	}
    	return propertyName;
    	*/
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
    	/*
    	String propertyName = getGlobalPropertyName( modelEntity );
    	
    	if ((propertyName != null) && isReferenceProperty) {
    		propertyName += "Ref";
    	}
    	return propertyName;
    	*/
    }
    
}

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

package org.opentravel.schemacompiler.codegen.json.facet;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * Code generation delegate for <code>TLFacet</code> instances with a facet type of
 * <code>UPDATE</code> and a facet owner of type <code>TLBusinessObject</code>.
 */
public class BusinessObjectUpdateFacetJsonSchemaDelegate extends TLFacetJsonSchemaDelegate {
	
    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet  the source facet
     */
    public BusinessObjectUpdateFacetJsonSchemaDelegate(TLFacet sourceFacet) {
        super(sourceFacet);
    }

	/**
	 * @see org.opentravel.schemacompiler.codegen.json.facet.TLFacetJsonSchemaDelegate#createDefinitions()
	 */
	@Override
	protected List<JsonSchemaNamedReference> createDefinitions() {
		List<TLAttribute> attributeList = getAttributes();
		List<TLProperty> elementList = getElements();
		List<JsonSchemaNamedReference> definitions = new ArrayList<>();
		
		// Add 'update' indicators for optional elements and attributes
		for (TLAttribute attribute : attributeList) {
			if (!attribute.isMandatory()) {
				String indicatorName = XsdCodegenUtils.getUpdateIndicatorName( attribute );
				String fieldName = attribute.getName();
				
				if (attribute.isReference()) {
					QName elementName = XsdCodegenUtils.getGlobalElementName( attribute.getType() );
					
					if (elementName != null) {
						fieldName = elementName.getLocalPart();
					}
				}
				addUpdateIndicator( indicatorName, fieldName, definitions );
			}
		}
		
		for (TLProperty element : elementList) {
			if (!element.isMandatory()) {
				String indicatorName = XsdCodegenUtils.getUpdateIndicatorName( element );
				QName elementName = XsdCodegenUtils.getGlobalElementName( element.getType() );
				String fieldName = (elementName != null) ? elementName.getLocalPart() : element.getName();
				
				addUpdateIndicator( indicatorName, fieldName, definitions );
			}
		}
		
		// Add all of the standard attributes/indicators defined in the model
		definitions.addAll( super.createDefinitions() );
		
		return definitions;
	}
	
	/**
	 * Adds an 'update' indicator for the optional field with the given name.
	 * 
	 * @param indicatorName  the name of the update indicator
	 * @param fieldName  the name of the optional attribute or element
	 * @param definitions  the list of JSON schema definitions to which the new indicator will be added
	 */
	protected void addUpdateIndicator(String indicatorName, String fieldName, List<JsonSchemaNamedReference> definitions) {
		JsonSchemaNamedReference updateIndicator = new JsonSchemaNamedReference();
		JsonDocumentation indicatorDoc = new JsonDocumentation();
		JsonSchema indicatorSchema = new JsonSchema();
		
        indicatorSchema.setType( JsonType.jsonBoolean );
        indicatorSchema.setDocumentation( indicatorDoc );
        indicatorDoc.setDescriptions( "Indicates whether an update to the '" + fieldName + "' field has been supplied." );
        updateIndicator.setName( indicatorName );
        updateIndicator.setSchema( new JsonSchemaReference( indicatorSchema ) );
        definitions.add( updateIndicator );
	}
	
}

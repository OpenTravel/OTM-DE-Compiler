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
package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.codegen.example.ExampleValueGenerator;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimple;


/**
 * @author Eric.Bronson
 *
 */
public class AttributeDocumentationBuilder extends
		FieldDocumentationBuilder<TLAttribute> {

	/**
	 * @param manager
	 */
	public AttributeDocumentationBuilder(TLAttribute t) {
		super(t);
		name = t.getName();		
		isRequired = t.isMandatory();
		
		TLPropertyType  propertyType = t.getType();	
		type = DocumentationBuilderFactory.getInstance().getDocumentationBuilder(propertyType);
		typeName = propertyType.getNamespace() +":" + propertyType.getLocalName();
		TLAttributeOwner attributeOwner = t.getOwner();
				
		exampleValue = ExampleValueGenerator.getInstance(null).getExampleValue(t, attributeOwner);
				
		if(propertyType instanceof TLSimple){
			pattern = ((TLSimple) propertyType).getPattern();
		}
		maxOcurrences = 1;
	}
	
	@Override
	public DocumentationBuilderType getDocType() {
		return DocumentationBuilderType.ATTRIBUTE;
	}

	@Override
	public void build() throws Exception {
		// No action required
	}


}

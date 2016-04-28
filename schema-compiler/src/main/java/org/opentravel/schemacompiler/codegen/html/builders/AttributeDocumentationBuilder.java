/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.codegen.example.ExampleValueGenerator;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLAttributeType;
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
		
		TLAttributeType  propertyType = t.getType();	
		type = DocumentationBuilderFactory.getInstance().getDocumentationBuilder(propertyType);
		typeName = propertyType.getNamespace() +":" + propertyType.getLocalName();
		TLAttributeOwner attributeOwner = t.getOwner();
				
		exampleValue = ExampleValueGenerator.getInstance(null).getExampleValue(t, attributeOwner);
		
		javaFieldName = getVariableName(t.getName());
		
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
		// TODO Auto-generated method stub
		
	}


}

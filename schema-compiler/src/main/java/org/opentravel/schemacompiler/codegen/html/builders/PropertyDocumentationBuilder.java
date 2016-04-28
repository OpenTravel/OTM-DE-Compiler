package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.codegen.example.ExampleValueGenerator;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimple;



/**
 * @author Eric.Bronson
 *
 */
public class PropertyDocumentationBuilder extends
		FieldDocumentationBuilder<TLProperty> {

	/**
	 * @param manager
	 */
	public PropertyDocumentationBuilder(TLProperty t) {
		super(t);
		name = t.getName();
		isRequired = t.isMandatory();

		TLPropertyType propertyType = t.getType();

		type = DocumentationBuilderFactory.getInstance().getDocumentationBuilder(propertyType);
		typeName = propertyType.getNamespace() + ":"
				+ propertyType.getLocalName();
		if(propertyType instanceof TLListFacet){
			TLListFacet listFacet = (TLListFacet) propertyType;
			// only core objects have list facets
			TLCoreObject owner = (TLCoreObject) listFacet.getItemFacet().getOwningEntity();
			maxOcurrences = owner.getRoleEnumeration().getRoles().size();
		}else{
			maxOcurrences = t.getRepeat() == 0 ? 1 : t.getRepeat();
		}
		
		TLPropertyOwner propertyOwner = t.getOwner();

		exampleValue = ExampleValueGenerator.getInstance(null).getExampleValue(
				t, propertyOwner);

		javaFieldName = getVariableName(t.getName());

		if (propertyType instanceof TLSimple) {
			pattern = ((TLSimple) propertyType).getPattern();
		}
	}

	@Override
	public DocumentationBuilderType getDocType() {
		return DocumentationBuilderType.PROPERTY;
	}


	@Override
	public void build() throws Exception {
		// TODO Auto-generated method stub
		
	}

}

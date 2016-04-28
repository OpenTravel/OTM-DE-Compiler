/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.model.TLEnumValue;

/**
 * @author Eric.Bronson
 *
 */
public class EnumValueDocumentationBuilder extends AbstractDocumentationBuilder<TLEnumValue> {
	
	/**
	 * @param manager
	 */
	public EnumValueDocumentationBuilder(TLEnumValue t) {
		super(t);
		name = t.getLiteral();
	}

	@Override
	public DocumentationBuilderType getDocType() {
		return null;
	}

	@Override
	public void build() throws Exception {
		// TODO Auto-generated method stub
		
	}
}

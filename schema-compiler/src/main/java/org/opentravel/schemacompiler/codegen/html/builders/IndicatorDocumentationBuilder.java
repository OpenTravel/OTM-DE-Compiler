/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.model.TLIndicator;


/**
 * @author Eric.Bronson
 *
 */
public class IndicatorDocumentationBuilder extends
		FieldDocumentationBuilder<TLIndicator> {
	
	/**
	 * @param manager
	 */
	public IndicatorDocumentationBuilder(TLIndicator t) {
		super(t);
		String indName = t.getName();
		name = indName;
		if(!indName.endsWith("Ind")){
			indName = indName + "Ind";
		}
		javaFieldName = getVariableName(indName);
	}
	
	@Override
	public DocumentationBuilderType getDocType() {
		return DocumentationBuilderType.INDICATOR;
	}

	@Override
	public void build() throws Exception {
		// TODO Auto-generated method stub
		
	}

}

/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.EnumValue;
import org.opentravel.ns.ota2.librarymodel_v01_04.EnumerationClosed;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>EnumerationClosed</code> type to the
 * <code>TLClosedEnumeration</code> type.
 *
 * @author S. Livezey
 */
public class EnumerationClosedTransformer extends BaseTransformer<EnumerationClosed,TLClosedEnumeration,DefaultTransformerContext> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLClosedEnumeration transform(EnumerationClosed source) {
		ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
				getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
		TLClosedEnumeration enumType = new TLClosedEnumeration();
		
		enumType.setName(source.getName());
		
		if (source.getDocumentation() != null) {
			enumType.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		if (source.getValue() != null) {
			ObjectTransformer<EnumValue,TLEnumValue,DefaultTransformerContext> valueTransformer =
					getTransformerFactory().getTransformer(EnumValue.class, TLEnumValue.class);
			
			for (EnumValue jaxbValue : source.getValue()) {
				enumType.addValue( valueTransformer.transform(jaxbValue) );
			}
		}
		return enumType;
	}
	
}

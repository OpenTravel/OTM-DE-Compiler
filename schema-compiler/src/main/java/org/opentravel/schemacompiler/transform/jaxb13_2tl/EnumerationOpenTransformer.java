/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform.jaxb13_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_03.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_03.EnumValue;
import org.opentravel.ns.ota2.librarymodel_v01_03.EnumerationOpen;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>EnumerationOpen</code> type to the
 * <code>TLOpenEnumeration</code> type.
 *
 * @author S. Livezey
 */
public class EnumerationOpenTransformer extends BaseTransformer<EnumerationOpen,TLOpenEnumeration,DefaultTransformerContext> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLOpenEnumeration transform(EnumerationOpen source) {
		ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
				getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
		TLOpenEnumeration enumType = new TLOpenEnumeration();
		
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

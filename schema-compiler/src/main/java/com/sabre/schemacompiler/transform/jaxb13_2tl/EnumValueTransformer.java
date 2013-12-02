/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.jaxb13_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_03.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_03.EnumValue;
import org.opentravel.ns.ota2.librarymodel_v01_03.Equivalent;

import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>EnumValue</code> type to the
 * <code>TLEnumValue</code> type.
 *
 * @author S. Livezey
 */
public class EnumValueTransformer extends BaseTransformer<EnumValue,TLEnumValue,DefaultTransformerContext> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLEnumValue transform(EnumValue source) {
		ObjectTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(Equivalent.class, TLEquivalent.class);
		TLEnumValue enumValue = new TLEnumValue();
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			enumValue.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		for (Equivalent sourceEquiv : source.getEquivalent()) {
			enumValue.addEquivalent( equivTransformer.transform(sourceEquiv) );
		}
		enumValue.setLiteral( trimString(source.getLiteral()) );
		return enumValue;
	}
	
}

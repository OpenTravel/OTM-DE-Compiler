/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.EnumValue;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;

import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLEnumValue</code> type to the
 * <code>EnumValue</code> type.
 *
 * @author S. Livezey
 */
public class TLEnumValueTransformer extends BaseTransformer<TLEnumValue,EnumValue,SymbolResolverTransformerContext> {

	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object, com.sabre.schemacompiler.transform.SymbolResolver)
	 */
	@Override
	public EnumValue transform(TLEnumValue source) {
		ObjectTransformer<TLEquivalent,Equivalent,SymbolResolverTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(TLEquivalent.class, Equivalent.class);
		EnumValue enumValue = new EnumValue();
		
		if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
			ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
			
			enumValue.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		for (TLEquivalent sourceEquiv : source.getEquivalents()) {
			enumValue.getEquivalent().add( equivTransformer.transform(sourceEquiv) );
		}
		enumValue.setLiteral( trimString(source.getLiteral(), false) );
		return enumValue;
	}
	
}

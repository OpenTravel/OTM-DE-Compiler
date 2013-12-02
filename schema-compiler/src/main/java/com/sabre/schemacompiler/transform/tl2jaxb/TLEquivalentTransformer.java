/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;

import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLEquivalent</code> type to the
 * <code>Equivalent</code> type.
 *
 * @author S. Livezey
 */
public class TLEquivalentTransformer extends BaseTransformer<TLEquivalent,Equivalent,SymbolResolverTransformerContext> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public Equivalent transform(TLEquivalent source) {
		Equivalent equiv = new Equivalent();
		
		equiv.setContext( trimString(source.getContext(), false) );
		equiv.setValue( trimString(source.getDescription()) );
		return equiv;
	}
	
}

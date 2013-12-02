/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.Example;

import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLExample</code> type to the
 * <code>Example</code> type.
 *
 * @author S. Livezey
 */
public class TLExampleTransformer extends BaseTransformer<TLExample,Example,SymbolResolverTransformerContext> {

	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public Example transform(TLExample source) {
		Example example = new Example();
		
		example.setContext( trimString(source.getContext(), false) );
		example.setValue( trimString(source.getValue()) );
		return example;
	}
	
}

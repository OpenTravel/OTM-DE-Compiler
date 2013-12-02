/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.jaxb13_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_03.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_03.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_03.SimpleFacet;

import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;


/**
 * Handles the transformation of objects from the <code>SimpleFacet</code> type to the
 * <code>TLSimpleFacet</code> type.
 *
 * @author S. Livezey
 */
public class SimpleFacetTransformer extends BaseTransformer<SimpleFacet,TLSimpleFacet,SymbolResolverTransformerContext> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLSimpleFacet transform(SimpleFacet source) {
		ObjectTransformer<Equivalent,TLEquivalent,SymbolResolverTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(Equivalent.class, TLEquivalent.class);
		String exampleValue = trimString(source.getEx());
		final TLSimpleFacet facet = new TLSimpleFacet();
		
		facet.setSimpleTypeName( trimString(source.getType()) );
		
		if (exampleValue != null) {
			TLExample example = new TLExample();
			
			example.setContext(LibraryTransformer.DEFAULT_CONTEXT_ID);
			example.setValue(exampleValue);
			facet.addExample(example);
		}
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,SymbolResolverTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			facet.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		for (Equivalent sourceEquiv : source.getEquivalent()) {
			facet.addEquivalent( equivTransformer.transform(sourceEquiv) );
		}
		
		return facet;
	}
	
}

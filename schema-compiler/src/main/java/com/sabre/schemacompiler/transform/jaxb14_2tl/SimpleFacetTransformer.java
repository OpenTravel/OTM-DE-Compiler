/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Example;
import org.opentravel.ns.ota2.librarymodel_v01_04.SimpleFacet;

import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;


/**
 * Handles the transformation of objects from the <code>SimpleFacet</code> type to the
 * <code>TLSimpleFacet</code> type.
 *
 * @author S. Livezey
 */
public class SimpleFacetTransformer extends BaseTransformer<SimpleFacet,TLSimpleFacet,DefaultTransformerContext> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLSimpleFacet transform(SimpleFacet source) {
		ObjectTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(Equivalent.class, TLEquivalent.class);
		ObjectTransformer<Example,TLExample,DefaultTransformerContext> exampleTransformer =
				getTransformerFactory().getTransformer(Example.class, TLExample.class);
		final TLSimpleFacet facet = new TLSimpleFacet();
		
		facet.setSimpleTypeName( trimString(source.getType()) );
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			facet.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		for (Equivalent sourceEquiv : source.getEquivalent()) {
			facet.addEquivalent( equivTransformer.transform(sourceEquiv) );
		}
		
		for (Example sourceExample : source.getExample()) {
			facet.addExample( exampleTransformer.transform(sourceExample) );
		}
		
		return facet;
	}
	
}

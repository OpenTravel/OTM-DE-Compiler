/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.FacetContextual;

import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLFacet</code> type to the
 * <code>FacetContextual</code> type.
 *
 * @author S. Livezey
 */
public class TLFacetToContextualFacetTransformer extends TLComplexTypeTransformer<TLFacet,FacetContextual> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public FacetContextual transform(TLFacet source) {
		FacetContextual facet = new FacetContextual();
		
		if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
			ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
			
			facet.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		facet.setNotExtendable( source.isNotExtendable() );
		facet.setContext( trimString(source.getContext(), false) );
		facet.setLabel( trimString(source.getLabel(), false) );
		facet.getAttribute().addAll(transformAttributes(source.getAttributes()));
		facet.getElement().addAll(transformElements(source.getElements()));
		facet.getIndicator().addAll(transformIndicators(source.getIndicators()));
		
		return facet;
	}
	
}

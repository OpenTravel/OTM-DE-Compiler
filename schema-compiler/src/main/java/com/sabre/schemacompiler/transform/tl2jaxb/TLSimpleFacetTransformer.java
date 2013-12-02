/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Example;
import org.opentravel.ns.ota2.librarymodel_v01_04.SimpleFacet;

import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLSimpleFacet</code> type to the
 * <code>SimpleFacet</code> type.
 *
 * @author S. Livezey
 */
public class TLSimpleFacetTransformer extends BaseTransformer<TLSimpleFacet,SimpleFacet,SymbolResolverTransformerContext> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public SimpleFacet transform(TLSimpleFacet source) {
		ObjectTransformer<TLEquivalent,Equivalent,SymbolResolverTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(TLEquivalent.class, Equivalent.class);
		ObjectTransformer<TLExample,Example,SymbolResolverTransformerContext> exTransformer =
				getTransformerFactory().getTransformer(TLExample.class, Example.class);
		NamedEntity simpleType = source.getSimpleType();
		SimpleFacet facet = new SimpleFacet();
		
		if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
			ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
			
			facet.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		for (TLEquivalent sourceEquiv : source.getEquivalents()) {
			facet.getEquivalent().add( equivTransformer.transform(sourceEquiv) );
		}
		
		for (TLExample sourceEx : source.getExamples()) {
			facet.getExample().add( exTransformer.transform(sourceEx) );
		}
		
		if (simpleType != null) {
			facet.setType( context.getSymbolResolver().buildEntityName(
					simpleType.getNamespace(), simpleType.getLocalName()) );
		} else {
			facet.setType( trimString(source.getSimpleTypeName(), false) );
		}
		return facet;
	}
	
}

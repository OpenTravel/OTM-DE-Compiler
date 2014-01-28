/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_04.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_04.Operation;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLOperation</code> type to the
 * <code>Operation</code> type.
 *
 * @author S. Livezey
 */
public class TLOperationTransformer extends BaseTransformer<TLOperation,Operation,SymbolResolverTransformerContext> {
	
	@Override
	public Operation transform(TLOperation source) {
		ObjectTransformer<TLFacet,Facet,SymbolResolverTransformerContext> facetTransformer =
				getTransformerFactory().getTransformer(TLFacet.class, Facet.class);
		ObjectTransformer<TLEquivalent,Equivalent,SymbolResolverTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(TLEquivalent.class, Equivalent.class);
		Operation operation = new Operation();
		
		operation.setName( trimString(source.getName(), false) );
		operation.setNotExtendable( source.isNotExtendable() );
		
		if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
			ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
			
			operation.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		for (TLEquivalent sourceEquiv : source.getEquivalents()) {
			operation.getEquivalent().add( equivTransformer.transform(sourceEquiv) );
		}
		
		operation.setRequest(facetTransformer.transform(source.getRequest()));
		operation.setResponse(facetTransformer.transform(source.getResponse()));
		operation.setNotification(facetTransformer.transform(source.getNotification()));
		
		if (source.getExtension() != null) {
			ObjectTransformer<TLExtension,Extension,SymbolResolverTransformerContext> extensionTransformer =
					getTransformerFactory().getTransformer(TLExtension.class, Extension.class);
			
			operation.setExtension( extensionTransformer.transform(source.getExtension()) );
		}
		
		return operation;
	}
	
}

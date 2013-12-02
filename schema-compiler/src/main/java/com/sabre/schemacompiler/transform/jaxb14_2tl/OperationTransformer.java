/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_04.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_04.Operation;

import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Operation</code> type to the
 * <code>TLOperation</code> type.
 *
 * @author S. Livezey
 */
public class OperationTransformer extends BaseTransformer<Operation,TLOperation,DefaultTransformerContext> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLOperation transform(Operation source) {
		ObjectTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(Equivalent.class, TLEquivalent.class);
		ObjectTransformer<Facet,TLFacet,DefaultTransformerContext> facetTransformer =
				getTransformerFactory().getTransformer(Facet.class, TLFacet.class);
		final TLOperation operation = new TLOperation();
		
		operation.setName( trimString(source.getName()) );
		operation.setNotExtendable( (source.isNotExtendable() == null) ? false : source.isNotExtendable() );
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			operation.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		if (source.getExtension() != null) {
			ObjectTransformer<Extension,TLExtension,DefaultTransformerContext> extensionTransformer =
					getTransformerFactory().getTransformer(Extension.class, TLExtension.class);
			
			operation.setExtension( extensionTransformer.transform(source.getExtension()) );
		}
		
		for (Equivalent sourceEquiv : source.getEquivalent()) {
			operation.addEquivalent( equivTransformer.transform(sourceEquiv) );
		}
		
		if (source.getRequest() != null) {
			operation.setRequest(facetTransformer.transform(source.getRequest()));
		}
		if (source.getResponse() != null) {
			operation.setResponse(facetTransformer.transform(source.getResponse()));
		}
		if (source.getNotification() != null) {
			operation.setNotification(facetTransformer.transform(source.getNotification()));
		}
		
		return operation;
	}
	
}

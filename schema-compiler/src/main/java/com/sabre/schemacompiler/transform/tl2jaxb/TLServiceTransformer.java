/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Operation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Service;

import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLService;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLService</code> type to the
 * <code>Service</code> type.
 *
 * @author S. Livezey
 */
public class TLServiceTransformer extends BaseTransformer<TLService,Service,SymbolResolverTransformerContext> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public Service transform(TLService source) {
		ObjectTransformer<TLOperation,Operation,SymbolResolverTransformerContext> operationTransformer =
				getTransformerFactory().getTransformer(TLOperation.class,Operation.class);
		ObjectTransformer<TLEquivalent,Equivalent,SymbolResolverTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(TLEquivalent.class, Equivalent.class);
		Service service = new Service();
		
		service.setName( trimString(source.getName(), false) );
		
		if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
			ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
			
			service.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		for (TLEquivalent sourceEquiv : source.getEquivalents()) {
			service.getEquivalent().add( equivTransformer.transform(sourceEquiv) );
		}
		
		for (TLOperation modelOperation : source.getOperations()) {
			service.getOperation().add( operationTransformer.transform(modelOperation) );
		}
		return service;
	}
	
}

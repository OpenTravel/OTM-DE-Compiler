/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.ContextDeclaration;
import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;

import com.sabre.schemacompiler.model.TLContext;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLContext</code> type to the
 * <code>ContextDeclaration</code> type.
 *
 * @author S. Livezey
 */
public class TLContextTransformer extends BaseTransformer<TLContext,ContextDeclaration,SymbolResolverTransformerContext> {

	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public ContextDeclaration transform(TLContext source) {
		ContextDeclaration context = new ContextDeclaration();
		
		context.setContext(source.getContextId());
		context.setApplicationContext(source.getApplicationContext());
		
		if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
			ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
			
			context.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		return context;
	}
	
}

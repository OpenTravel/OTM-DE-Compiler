/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.ContextDeclaration;
import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>ContextDeclaration</code> type to the
 * <code>TLContext</code> type.
 *
 * @author S. Livezey
 */
public class ContextDeclarationTransformer extends BaseTransformer<ContextDeclaration,TLContext,DefaultTransformerContext> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLContext transform(ContextDeclaration source) {
		TLContext context = new TLContext();
		
		context.setContextId( source.getContext() );
		context.setApplicationContext( source.getApplicationContext() );
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			context.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		return context;
	}
	
}

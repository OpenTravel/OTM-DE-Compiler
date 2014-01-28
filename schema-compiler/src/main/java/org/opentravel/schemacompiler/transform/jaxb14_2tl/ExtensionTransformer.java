/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Extension;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Extension</code> type to the
 * <code>TLExtension</code> type.
 *
 * @author S. Livezey
 */
public class ExtensionTransformer extends BaseTransformer<Extension,TLExtension,DefaultTransformerContext> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLExtension transform(Extension source) {
		final TLExtension extension = new TLExtension();
		
		extension.setExtendsEntityName( source.getExtends() );
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			extension.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		return extension;
	}
	
}

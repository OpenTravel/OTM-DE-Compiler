/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_04.ExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLExtensionPointFacet</code> type to the
 * <code>ExtensionPointFacet</code> type.
 *
 * @author S. Livezey
 */
public class TLExtensionPointFacetTransformer extends TLComplexTypeTransformer<TLExtensionPointFacet,ExtensionPointFacet> {

	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public ExtensionPointFacet transform(TLExtensionPointFacet source) {
		ExtensionPointFacet facet = new ExtensionPointFacet();
		
		if (source.getExtension() != null) {
			ObjectTransformer<TLExtension,Extension,SymbolResolverTransformerContext> extensionTransformer =
					getTransformerFactory().getTransformer(TLExtension.class, Extension.class);
			
			facet.setExtension( extensionTransformer.transform(source.getExtension()) );
		}
		
		if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
			ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
			
			facet.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		facet.getAttribute().addAll( transformAttributes(source.getAttributes()) );
		facet.getElement().addAll( transformElements(source.getElements()) );
		facet.getIndicator().addAll( transformIndicators(source.getIndicators()) );
		
		return facet;
	}
	
}

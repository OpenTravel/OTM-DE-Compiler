/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_04.ExtensionPointFacet;

import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;

/**
 * Handles the transformation of objects from the <code>ExtensionPointFacet</code> type to the
 * <code>TLExtensionPointFacet</code> type.
 *
 * @author S. Livezey
 */
public class ExtensionPointFacetTransformer extends ComplexTypeTransformer<ExtensionPointFacet,TLExtensionPointFacet> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLExtensionPointFacet transform(ExtensionPointFacet source) {
		TLExtensionPointFacet facet = new TLExtensionPointFacet();
		
		if (source.getExtension() != null) {
			ObjectTransformer<Extension,TLExtension,DefaultTransformerContext> extensionTransformer =
					getTransformerFactory().getTransformer(Extension.class, TLExtension.class);
			
			facet.setExtension( extensionTransformer.transform(source.getExtension()) );
		}
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			facet.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		for (TLAttribute attribute : transformAttributes(source.getAttribute())) {
			facet.addAttribute(attribute);
		}
		for (TLProperty element : transformElements(source.getElement())) {
			facet.addElement(element);
		}
		for (TLIndicator indicator : transformIndicators(source.getIndicator())) {
			facet.addIndicator(indicator);
		}
		
		return facet;
	}
	
}

/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Facet;

import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;

/**
 * Handles the transformation of objects from the <code>Facet</code> type to the
 * <code>TLFacet</code> type.
 *
 * @author S. Livezey
 */
public class FacetTransformer extends ComplexTypeTransformer<Facet,TLFacet> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLFacet transform(Facet source) {
		TLFacet facet = new TLFacet();
		
		facet.setNotExtendable(false);
		
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

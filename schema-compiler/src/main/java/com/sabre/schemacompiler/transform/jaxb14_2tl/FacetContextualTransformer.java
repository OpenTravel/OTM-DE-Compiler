/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.FacetContextual;

import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLProperty;

/**
 * Handles the transformation of objects from the <code>FacetContextual</code> type to the
 * <code>TLFacet</code> type.
 *
 * @author S. Livezey
 */
public class FacetContextualTransformer extends ComplexTypeTransformer<FacetContextual,TLFacet> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLFacet transform(FacetContextual source) {
		final TLFacet facet = new TLFacet();
		
		facet.setContext( trimString(source.getContext()) );
		facet.setLabel( trimString(source.getLabel()) );
		facet.setNotExtendable( (source.isNotExtendable() == null) ? false : source.isNotExtendable() );
		
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

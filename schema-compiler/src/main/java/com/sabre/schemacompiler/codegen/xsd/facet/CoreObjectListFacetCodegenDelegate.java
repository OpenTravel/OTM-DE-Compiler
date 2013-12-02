/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd.facet;

import org.w3._2001.xmlschema.Annotated;

import com.sabre.schemacompiler.model.TLListFacet;

/**
 * Code generation delegate for <code>TLListFacet</code> instances with a facet type of <code>INFO</code>
 * and a facet owner of type <code>TLCoreObject</code>.
 * 
 * @author S. Livezey
 */
public class CoreObjectListFacetCodegenDelegate extends TLListFacetCodegenDelegate {
	
	/**
	 * Constructor that specifies the source facet for which code artifacts are being
	 * generated.
	 * 
	 * @param sourceFacet  the source facet
	 */
	public CoreObjectListFacetCodegenDelegate(TLListFacet sourceFacet) {
		super(sourceFacet);
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#createType()
	 */
	@Override
	protected Annotated createType() {
		return null; // No type generated for complex list facet types
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#getLocalBaseFacet()
	 */
	@Override
	public TLListFacet getLocalBaseFacet() {
		return null;
	}
	
}

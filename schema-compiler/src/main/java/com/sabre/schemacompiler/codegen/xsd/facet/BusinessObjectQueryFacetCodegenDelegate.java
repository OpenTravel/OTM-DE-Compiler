/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd.facet;

import javax.xml.namespace.QName;

import com.sabre.schemacompiler.ioc.SchemaDependency;
import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLFacet;

/**
 * Code generation delegate for <code>TLFacet</code> instances with a facet type of <code>ID</code>
 * and a facet owner of type <code>TLBusinessObject</code>.
 * 
 * @author S. Livezey
 */
public class BusinessObjectQueryFacetCodegenDelegate extends BusinessObjectFacetCodegenDelegate {
	
	/**
	 * Constructor that specifies the source facet for which code artifacts are being
	 * generated.
	 * 
	 * @param sourceFacet  the source facet
	 */
	public BusinessObjectQueryFacetCodegenDelegate(TLFacet sourceFacet) {
		super(sourceFacet);
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#hasContent()
	 */
	@Override
	public boolean hasContent() {
		return getSourceFacet().declaresContent() || (getBaseFacet() != null);
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacet()
	 */
	@Override
	public TLFacet getLocalBaseFacet() {
		return null; // No base type for query facets
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getSubstitutionGroup(com.sabre.schemacompiler.model.TLAlias)
	 */
	@Override
	protected QName getSubstitutionGroup(TLAlias facetAlias) {
		return null; // No substitution group for query facets
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getExtensionPointElement()
	 */
	@Override
	public QName getExtensionPointElement() {
		QName extensionPointQName = null;
		
		if (getBaseFacet() == null) {
			SchemaDependency extensionPoint = SchemaDependency.getExtensionPointQueryElement();
			
			extensionPointQName = extensionPoint.toQName();
			addCompileTimeDependency(extensionPoint);
		}
		return extensionPointQName;
	}

}

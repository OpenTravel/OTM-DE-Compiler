/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd.facet;

import javax.xml.namespace.QName;

import com.sabre.schemacompiler.ioc.SchemaDependency;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLFacet;

/**
 * Code generation delegate for <code>TLFacet</code> instances with a facet type of <code>SUMMARY</code>
 * and a facet owner of type <code>TLBusinessObject</code>.
 * 
 * @author S. Livezey
 */
public class BusinessObjectSummaryFacetCodegenDelegate extends BusinessObjectFacetCodegenDelegate {
	
	/**
	 * Constructor that specifies the source facet for which code artifacts are being
	 * generated.
	 * 
	 * @param sourceFacet  the source facet
	 */
	public BusinessObjectSummaryFacetCodegenDelegate(TLFacet sourceFacet) {
		super(sourceFacet);
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#hasNonSubstitutableElement()
	 */
	@Override
	protected boolean hasNonSubstitutableElement() {
		return true;
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacet()
	 */
	@Override
	public TLFacet getLocalBaseFacet() {
		TLFacet sourceFacet = getSourceFacet();
		TLFacet baseFacet = null;
		
		if (sourceFacet.getOwningEntity() instanceof TLBusinessObject) {
			FacetCodegenDelegateFactory factory = new FacetCodegenDelegateFactory(transformerContext);
			TLBusinessObject businessObject = (TLBusinessObject) sourceFacet.getOwningEntity();
			TLFacet parentFacet = businessObject.getIdFacet();
			
			if (factory.getDelegate(parentFacet).hasContent()) {
				baseFacet = parentFacet;
			}
		}
		return baseFacet;
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getExtensionPointElement()
	 */
	@Override
	public QName getExtensionPointElement() {
		SchemaDependency extensionPoint = SchemaDependency.getExtensionPointSummaryElement();
		QName extensionPointQName = extensionPoint.toQName();
		
		addCompileTimeDependency(extensionPoint);
		return extensionPointQName;
	}

}

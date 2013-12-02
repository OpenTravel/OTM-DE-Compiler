/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd.facet;

import javax.xml.namespace.QName;

import com.sabre.schemacompiler.ioc.SchemaDependency;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLFacet;

/**
 * Code generation delegate for <code>TLFacet</code> instances with a facet type of <code>DETAIL</code>
 * and a facet owner of type <code>TLCoreObject</code>.
 * 
 * @author S. Livezey
 */
public class CoreObjectDetailFacetCodegenDelegate extends CoreObjectFacetCodegenDelegate {
	
	/**
	 * Constructor that specifies the source facet for which code artifacts are being
	 * generated.
	 * 
	 * @param sourceFacet  the source facet
	 */
	public CoreObjectDetailFacetCodegenDelegate(TLFacet sourceFacet) {
		super(sourceFacet);
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacet()
	 */
	@Override
	public TLFacet getLocalBaseFacet() {
		TLFacet sourceFacet = getSourceFacet();
		TLFacet baseFacet = null;
		
		if (sourceFacet.getOwningEntity() instanceof TLCoreObject) {
			FacetCodegenDelegateFactory factory = new FacetCodegenDelegateFactory(transformerContext);
			TLCoreObject coreObject = (TLCoreObject) sourceFacet.getOwningEntity();
			TLFacet parentFacet = coreObject.getSummaryFacet();
			
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
		SchemaDependency extensionPoint = SchemaDependency.getExtensionPointDetailElement();
		QName extensionPointQName = extensionPoint.toQName();
		
		addCompileTimeDependency(extensionPoint);
		return extensionPointQName;
	}

}

/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd.facet;

import javax.xml.namespace.QName;

import com.sabre.schemacompiler.ioc.SchemaDependency;
import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLFacet;

/**
 * Code generation delegate for <code>TLFacet</code> instances with a facet type of <code>REQUEST</code>,
 * <code>RESPONSE</code>, or <code>NOTIFICATION</code> and a facet owner of type <code>TLOperation</code>.
 * 
 * @author S. Livezey
 */
public class OperationFacetCodegenDelegate extends TLFacetCodegenDelegate {
	
	/**
	 * Constructor that specifies the source facet for which code artifacts are being
	 * generated.
	 * 
	 * @param sourceFacet  the source facet
	 */
	public OperationFacetCodegenDelegate(TLFacet sourceFacet) {
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
		return null;
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getSubstitutionGroup(com.sabre.schemacompiler.model.TLAlias)
	 */
	@Override
	protected QName getSubstitutionGroup(TLAlias facetAlias) {
		SchemaDependency messagePayload = getMessagePayload();
		QName subGrp = null;
		
		if (messagePayload != null) {
			addCompileTimeDependency(messagePayload);
			subGrp = messagePayload.toQName();
		}
		return subGrp;
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getBaseFacetTypeName()
	 */
	@Override
	protected QName getLocalBaseFacetTypeName() {
		SchemaDependency messagePayload = getMessagePayload();
		QName typeName = null;
		
		if (messagePayload != null) {
			addCompileTimeDependency(messagePayload);
			typeName = messagePayload.toQName();
		}
		return typeName;
	}
	
	/**
	 * Returns the base message payload to use for the source facet of this delegate.
	 * 
	 * @return SchemaDependency
	 */
	protected SchemaDependency getMessagePayload() {
		SchemaDependency messagePayload = null;
		
		switch (getSourceFacet().getFacetType()) {
			case REQUEST:
				messagePayload = SchemaDependency.getRequestPayload();
				break;
			case RESPONSE:
				messagePayload = SchemaDependency.getResponsePayload();
				break;
			case NOTIFICATION:
				messagePayload = SchemaDependency.getNotifPayload();
				break;
		}
		return messagePayload;
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getExtensionPointElement()
	 */
	@Override
	public QName getExtensionPointElement() {
		QName extensionPointQName = null;
		
		if (getBaseFacet() == null) {
			SchemaDependency extensionPoint = SchemaDependency.getExtensionPointElement();
			
			extensionPointQName = extensionPoint.toQName();
			addCompileTimeDependency(extensionPoint);
		}
		return extensionPointQName;
	}

}

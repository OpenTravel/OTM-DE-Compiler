/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd.facet;

import javax.xml.namespace.QName;

import org.w3._2001.xmlschema.Annotation;

import com.sabre.schemacompiler.codegen.util.XsdCodegenUtils;
import com.sabre.schemacompiler.codegen.xsd.TLDocumentationCodegenTransformer;
import com.sabre.schemacompiler.ioc.SchemaDependency;
import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLDocumentationOwner;
import com.sabre.schemacompiler.model.TLFacet;

/**
 * Code generation delegate for <code>TLFacet</code> instances with a facet type of <code>SUMMARY</code>
 * and a facet owner of type <code>TLCoreObject</code>.
 * 
 * @author S. Livezey
 */
public class CoreObjectSummaryFacetCodegenDelegate extends CoreObjectFacetCodegenDelegate {
	
	/**
	 * Constructor that specifies the source facet for which code artifacts are being
	 * generated.
	 * 
	 * @param sourceFacet  the source facet
	 */
	public CoreObjectSummaryFacetCodegenDelegate(TLFacet sourceFacet) {
		super(sourceFacet);
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#hasSubstitutionGroupElement()
	 */
	@Override
	protected boolean hasSubstitutionGroupElement() {
		return !XsdCodegenUtils.isSimpleCoreObject( getSourceFacet().getOwningEntity() );
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#hasNonSubstitutableElement()
	 */
	@Override
	protected boolean hasNonSubstitutableElement() {
		return !XsdCodegenUtils.isSimpleCoreObject( getSourceFacet().getOwningEntity() );
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getSubstitutionGroup(com.sabre.schemacompiler.model.TLAlias)
	 */
	@Override
	protected QName getSubstitutionGroup(TLAlias facetAlias) {
		return XsdCodegenUtils.isSimpleCoreObject( getSourceFacet().getOwningEntity() ) ?
				null : super.getSubstitutionGroup(facetAlias);
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacet()
	 */
	@Override
	public TLFacet getLocalBaseFacet() {
		return null; // No base type for core object summary facets
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

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createJaxbDocumentation(com.sabre.schemacompiler.model.TLDocumentationOwner)
	 */
	@Override
	protected Annotation createJaxbDocumentation(TLDocumentationOwner entity) {
		Annotation annotation = null;
		
		if (entity instanceof TLFacet) {
			TLFacet sourceFacet = (TLFacet) entity;
			
			if (XsdCodegenUtils.isSimpleCoreObject(sourceFacet.getOwningEntity())) {
				Annotation ownerAnnotation = super.createJaxbDocumentation( (TLDocumentationOwner) sourceFacet.getOwningEntity() );
				Annotation facetAnnotation = super.createJaxbDocumentation( sourceFacet );
				
				annotation = TLDocumentationCodegenTransformer.mergeDocumentation( ownerAnnotation, facetAnnotation );
			}
		}
		if (annotation == null) {
			annotation = super.createJaxbDocumentation( entity );
		}
		return annotation;
	}

}

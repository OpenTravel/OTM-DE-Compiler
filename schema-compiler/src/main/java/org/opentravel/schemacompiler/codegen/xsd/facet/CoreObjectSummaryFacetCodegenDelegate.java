
package org.opentravel.schemacompiler.codegen.xsd.facet;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.TLDocumentationCodegenTransformer;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.w3._2001.xmlschema.Annotation;

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
	 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#hasSubstitutionGroupElement()
	 */
	@Override
	protected boolean hasSubstitutionGroupElement() {
		return !XsdCodegenUtils.isSimpleCoreObject( getSourceFacet().getOwningEntity() );
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#hasNonSubstitutableElement()
	 */
	@Override
	protected boolean hasNonSubstitutableElement() {
		return !XsdCodegenUtils.isSimpleCoreObject( getSourceFacet().getOwningEntity() );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getSubstitutionGroup(org.opentravel.schemacompiler.model.TLAlias)
	 */
	@Override
	protected QName getSubstitutionGroup(TLAlias facetAlias) {
		return XsdCodegenUtils.isSimpleCoreObject( getSourceFacet().getOwningEntity() ) ?
				null : super.getSubstitutionGroup(facetAlias);
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacet()
	 */
	@Override
	public TLFacet getLocalBaseFacet() {
		return null; // No base type for core object summary facets
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getExtensionPointElement()
	 */
	@Override
	public QName getExtensionPointElement() {
		SchemaDependency extensionPoint = SchemaDependency.getExtensionPointSummaryElement();
		QName extensionPointQName = extensionPoint.toQName();
		
		addCompileTimeDependency(extensionPoint);
		return extensionPointQName;
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createJaxbDocumentation(org.opentravel.schemacompiler.model.TLDocumentationOwner)
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

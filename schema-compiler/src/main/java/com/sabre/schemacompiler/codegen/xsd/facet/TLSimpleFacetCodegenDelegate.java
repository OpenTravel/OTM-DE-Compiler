/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd.facet;

import javax.xml.namespace.QName;

import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Restriction;
import org.w3._2001.xmlschema.SimpleType;
import org.w3._2001.xmlschema.TopLevelSimpleType;

import com.sabre.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import com.sabre.schemacompiler.codegen.util.XsdCodegenUtils;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.transform.ObjectTransformer;

/**
 * Code generation delegate for <code>TLSimpleFacet</code> instances with a facet type of <code>SIMPLE</code>
 * and a facet owner of type <code>TLCoreObject</code>.
 * 
 * @author S. Livezey
 */
public class TLSimpleFacetCodegenDelegate extends FacetCodegenDelegate<TLSimpleFacet> {
	
	/**
	 * Constructor that specifies the source facet for which code artifacts are being
	 * generated.
	 * 
	 * @param sourceFacet  the source facet
	 */
	public TLSimpleFacetCodegenDelegate(TLSimpleFacet sourceFacet) {
		super(sourceFacet);
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#generateElements()
	 */
	@Override
	public FacetCodegenElements generateElements() {
		return new FacetCodegenElements(); // No global elements generated for simple facets
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#createType()
	 */
	@Override
	protected Annotated createType() {
		TLSimpleFacet sourceFacet = getSourceFacet();
		Restriction restriction = new Restriction();
		SimpleType type = null;
		QName baseType;
		
		type = new TopLevelSimpleType();
		type.setName( XsdCodegenUtils.getGlobalTypeName(sourceFacet) );
		type.setRestriction(restriction);
		
		if (sourceFacet.getSimpleType() instanceof TLCoreObject) {
			// Special Case: For core objects, use the simple facet as the base type
			TLCoreObject coreObject = (TLCoreObject) sourceFacet.getSimpleType();
			TLSimpleFacet coreSimple = coreObject.getSimpleFacet();
			
			baseType = new QName(coreSimple.getNamespace(), XsdCodegenUtils.getGlobalTypeName(coreSimple));
			
		} else { // normal case
			baseType = new QName(sourceFacet.getSimpleType().getNamespace(),
					XsdCodegenUtils.getGlobalTypeName(sourceFacet.getSimpleType()));
		}
		restriction.setBase(baseType);
		
		if (sourceFacet.getDocumentation() != null) {
			ObjectTransformer<TLDocumentation,Annotation,CodeGenerationTransformerContext> docTransformer =
				getTransformerFactory().getTransformer(sourceFacet.getDocumentation(), Annotation.class);
			
			type.setAnnotation( docTransformer.transform(sourceFacet.getDocumentation()) );
		}
		XsdCodegenUtils.addAppInfo(sourceFacet, type);
		return type;
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#getLocalBaseFacet()
	 */
	@Override
	public TLSimpleFacet getLocalBaseFacet() {
		return null;
	}
	
}

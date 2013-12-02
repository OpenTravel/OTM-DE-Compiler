/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd;

import java.math.BigInteger;

import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Attribute;
import org.w3._2001.xmlschema.Element;
import org.w3._2001.xmlschema.TopLevelElement;

import com.sabre.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import com.sabre.schemacompiler.codegen.util.XsdCodegenUtils;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>TLIndicator</code> objects to the JAXB nodes used
 * to produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLIndicatorCodegenTransformer extends AbstractXsdTransformer<TLIndicator,Annotated> {

	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public Annotated transform(TLIndicator source) {
		boolean publishAsElement = source.isPublishAsElement() && !(source.getOwner() instanceof TLValueWithAttributes);
		String indicatorName = source.getName();
		Annotated indicator;
		
		if (!indicatorName.endsWith("Ind")) {
			indicatorName += "Ind";
		}
		
		if (publishAsElement) {
			Element indicatorElement = new TopLevelElement();
			
			indicatorElement.setName(indicatorName);
			indicatorElement.setType(XsdCodegenUtils.XSD_BOOLEAN_TYPE);
			indicatorElement.setMinOccurs(BigInteger.ZERO);
			indicatorElement.setMaxOccurs(BigInteger.ONE.toString());
			indicator = indicatorElement;
			
		} else { // publish as attribute (default)
			Attribute indicatorAttr = new Attribute();
			
			indicatorAttr.setName(indicatorName);
			indicatorAttr.setType(XsdCodegenUtils.XSD_BOOLEAN_TYPE);
			indicatorAttr.setUse("optional");
			indicator = indicatorAttr;
		}
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<TLDocumentation,Annotation,CodeGenerationTransformerContext> docTransformer =
				getTransformerFactory().getTransformer(source.getDocumentation(), Annotation.class);
			
			indicator.setAnnotation( docTransformer.transform(source.getDocumentation()) );
		}
		return indicator;
	}
	
}

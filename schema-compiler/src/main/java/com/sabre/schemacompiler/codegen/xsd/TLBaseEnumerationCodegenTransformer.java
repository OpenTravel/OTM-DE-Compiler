/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd;

import javax.xml.bind.JAXBElement;

import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.NoFixedFacet;

import com.sabre.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.transform.ObjectTransformer;

/**
 * Base class for enumeration transformers that provides common methods and functions.
 * 
 * @param <S>  the source type of the object transformation
 * @param <T>  the target type of the object transformation
 * @author S. Livezey
 */
public abstract class TLBaseEnumerationCodegenTransformer<S,T> extends AbstractXsdTransformer<S,T> {
	
	public static final String OPEN_ENUM_VALUE = "Other_";
	
	/**
	 * Constructs an XML schema representation of the given meta-model enumeration value.
	 * 
	 * @param modelEnum  the enumeration value from the compiler meta-model
	 * @return JAXBElement<NoFixedFacet>
	 */
	protected JAXBElement<NoFixedFacet> createEnumValue(TLEnumValue modelEnum) {
		NoFixedFacet facet = new NoFixedFacet();
		
		facet.setValue(modelEnum.getLiteral());
		
		if (modelEnum.getDocumentation() != null) {
			ObjectTransformer<TLDocumentation,Annotation,CodeGenerationTransformerContext> docTransformer =
				getTransformerFactory().getTransformer(modelEnum.getDocumentation(), Annotation.class);
			
			facet.setAnnotation( docTransformer.transform(modelEnum.getDocumentation()) );
		}
		return jaxbObjectFactory.createEnumeration(facet);
	}
	
}

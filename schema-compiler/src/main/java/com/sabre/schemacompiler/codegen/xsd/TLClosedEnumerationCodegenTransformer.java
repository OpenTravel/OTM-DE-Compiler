/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Restriction;
import org.w3._2001.xmlschema.SimpleType;
import org.w3._2001.xmlschema.TopLevelSimpleType;

import com.sabre.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import com.sabre.schemacompiler.codegen.impl.CodegenArtifacts;
import com.sabre.schemacompiler.codegen.util.XsdCodegenUtils;
import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>TLClosedEnumeration</code> objects to the JAXB nodes used
 * to produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLClosedEnumerationCodegenTransformer extends TLBaseEnumerationCodegenTransformer<TLClosedEnumeration,CodegenArtifacts> {

	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public CodegenArtifacts transform(TLClosedEnumeration source) {
		CodegenArtifacts artifacts = new CodegenArtifacts();
		SimpleType xsdEnum = new TopLevelSimpleType();
		Restriction restriction = new Restriction();
		
		xsdEnum.setName(source.getName());
		restriction.setBase( new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string") );
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<TLDocumentation,Annotation,CodeGenerationTransformerContext> docTransformer =
				getTransformerFactory().getTransformer(source.getDocumentation(), Annotation.class);
			
			xsdEnum.setAnnotation( docTransformer.transform(source.getDocumentation()) );
		}
		XsdCodegenUtils.addAppInfo(source, xsdEnum);
		
		for (TLEnumValue modelEnum : source.getValues()) {
			restriction.getFacets().add( createEnumValue(modelEnum) );
		}
		xsdEnum.setRestriction(restriction);
		artifacts.addArtifact(xsdEnum);
		
		return artifacts;
	}
	
}

/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen.xsd;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Attribute;
import org.w3._2001.xmlschema.ComplexType;
import org.w3._2001.xmlschema.Restriction;
import org.w3._2001.xmlschema.SimpleContent;
import org.w3._2001.xmlschema.SimpleExtensionType;
import org.w3._2001.xmlschema.SimpleType;
import org.w3._2001.xmlschema.TopLevelComplexType;
import org.w3._2001.xmlschema.TopLevelSimpleType;

/**
 * Performs the translation from <code>TLOpenEnumeration</code> objects to the JAXB nodes used
 * to produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLOpenEnumerationCodegenTransformer extends TLBaseEnumerationCodegenTransformer<TLOpenEnumeration,CodegenArtifacts> {
	
	private static final TLEnumValue OTHER_ENUM_VALUE;
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public CodegenArtifacts transform(TLOpenEnumeration source) {
		CodegenArtifacts artifacts = new CodegenArtifacts();
		
		artifacts.addArtifact( createEnumComplexType(source) );
		artifacts.addArtifact( createEnumSimpleType(source) );
		return artifacts;
	}
	
	/**
	 * Constructs the complex type component of the closed enumeration.
	 * 
	 * @param source  the source meta-model enumeration
	 * @return ComplexType
	 */
	protected ComplexType createEnumComplexType(TLOpenEnumeration source) {
		SchemaDependency enumExtension = SchemaDependency.getEnumExtension();
		ComplexType complexEnum = new TopLevelComplexType();
		SimpleContent simpleContent = new SimpleContent();
		SimpleExtensionType extension = new SimpleExtensionType();
		Attribute attribute = new Attribute();
		
		complexEnum.setName(source.getName());
		complexEnum.setSimpleContent(simpleContent);
		XsdCodegenUtils.addAppInfo(source, complexEnum);
		simpleContent.setExtension(extension);
		extension.setBase(new QName(source.getNamespace(), source.getLocalName() + "_Base"));
		extension.getAttributeOrAttributeGroup().add(attribute);
		attribute.setName("extension");
		attribute.setType(enumExtension.toQName());
		addCompileTimeDependency(enumExtension);
		return complexEnum;
	}
	
	/**
	 * Constructs the simple type component of the closed enumeration.
	 * 
	 * @param source  the source meta-model enumeration
	 * @return SimpleType
	 */
	protected SimpleType createEnumSimpleType(TLOpenEnumeration source) {
		SimpleType simpleEnum = new TopLevelSimpleType();
		Restriction restriction = new Restriction();
		
		simpleEnum.setName(source.getName() + "_Base");
		restriction.setBase( new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string") );
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<TLDocumentation,Annotation,CodeGenerationTransformerContext> docTransformer =
				getTransformerFactory().getTransformer(source.getDocumentation(), Annotation.class);
			
			simpleEnum.setAnnotation( docTransformer.transform(source.getDocumentation()) );
		}
		XsdCodegenUtils.addAppInfo(source, simpleEnum);
		
		for (TLEnumValue modelEnum : source.getValues()) {
			restriction.getFacets().add( createEnumValue(modelEnum) );
		}
		restriction.getFacets().add( createEnumValue(OTHER_ENUM_VALUE) );
		simpleEnum.setRestriction(restriction);
		return simpleEnum;
	}
	
	/**
	 * Initializes the "Other" enum value instance used for open enumeration declarations.
	 */
	static {
		try {
			TLEnumValue otherEnum = new TLEnumValue();
			
			otherEnum.setLiteral("Other_");
			OTHER_ENUM_VALUE = otherEnum;
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
}

/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd;

import javax.xml.namespace.QName;

import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Attribute;
import org.w3._2001.xmlschema.ComplexType;
import org.w3._2001.xmlschema.SimpleContent;
import org.w3._2001.xmlschema.SimpleExtensionType;
import org.w3._2001.xmlschema.TopLevelComplexType;

import com.sabre.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import com.sabre.schemacompiler.codegen.impl.CodegenArtifacts;
import com.sabre.schemacompiler.codegen.util.PropertyCodegenUtils;
import com.sabre.schemacompiler.codegen.util.XsdCodegenUtils;
import com.sabre.schemacompiler.ioc.SchemaDependency;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLOpenEnumeration;
import com.sabre.schemacompiler.model.TLPropertyType;
import com.sabre.schemacompiler.model.TLRoleEnumeration;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>TLValueWithAttributes</code> objects to the JAXB nodes used
 * to produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLValueWithAttributesCodegenTransformer extends AbstractXsdTransformer<TLValueWithAttributes,CodegenArtifacts> {

	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public CodegenArtifacts transform(TLValueWithAttributes source) {
		ObjectTransformer<TLDocumentation,Annotation,CodeGenerationTransformerContext> docTransformer =
				getTransformerFactory().getTransformer(source.getDocumentation(), Annotation.class);
		ObjectTransformer<TLAttribute,CodegenArtifacts,CodeGenerationTransformerContext> attributeTransformer =
				getTransformerFactory().getTransformer(TLAttribute.class, CodegenArtifacts.class);
		ObjectTransformer<TLIndicator,Annotated,CodeGenerationTransformerContext> indicatorTransformer =
				getTransformerFactory().getTransformer(TLIndicator.class, Annotated.class);
		NamedEntity vwaParentType = getBaseParentType(source);
		CodegenArtifacts artifacts = new CodegenArtifacts();
		ComplexType targetType = new TopLevelComplexType();
		SimpleContent simpleContent = new SimpleContent();
		SimpleExtensionType extType = new SimpleExtensionType();
		
		targetType.setName(source.getName());
		
		if (source.getDocumentation() != null) {
			targetType.setAnnotation( docTransformer.transform(source.getDocumentation()) );
		}
		XsdCodegenUtils.addAppInfo(source, targetType);
		
		if (vwaParentType != null) {
			if ((vwaParentType instanceof TLOpenEnumeration) || (vwaParentType instanceof TLRoleEnumeration)) {
				extType.setBase( new QName(vwaParentType.getNamespace(),
						XsdCodegenUtils.getGlobalTypeName( (TLPropertyType) vwaParentType) + "_Base") );
			} else {
				extType.setBase( new QName(vwaParentType.getNamespace(),
						XsdCodegenUtils.getGlobalTypeName( (TLPropertyType) vwaParentType)) );
			}
		} else {
			SchemaDependency emptyElement = SchemaDependency.getEmptyElement();
			
			extType.setBase( emptyElement.toQName() );
			addCompileTimeDependency( emptyElement );
		}
		if (source.getValueDocumentation() != null) {
			simpleContent.setAnnotation( docTransformer.transform(source.getValueDocumentation()) );
		}
		simpleContent.setExtension(extType);
		targetType.setSimpleContent(simpleContent);
		
		// Transform the attributes and indicators of the target type
		if ((vwaParentType instanceof TLOpenEnumeration) || (vwaParentType instanceof TLRoleEnumeration)) {
			SchemaDependency enumExtension = SchemaDependency.getEnumExtension();
			Attribute extAttribute = new Attribute();
			
			extAttribute.setName("extension");
			extAttribute.setType(enumExtension.toQName());
			addCompileTimeDependency(enumExtension);
			extType.getAttributeOrAttributeGroup().add(extAttribute);
		}
		for (TLAttribute modelAttribute : PropertyCodegenUtils.getInheritedAttributes(source)) {
			extType.getAttributeOrAttributeGroup().addAll(
					attributeTransformer.transform(modelAttribute).getArtifactsOfType(Attribute.class) );
		}
		for (TLIndicator modelIndicator : PropertyCodegenUtils.getInheritedIndicators(source)) {
			Annotated jaxbAttribute = indicatorTransformer.transform(modelIndicator);
			
			if (jaxbAttribute != null) {
				extType.getAttributeOrAttributeGroup().add(jaxbAttribute);
			}
		}
		
		artifacts.addArtifact(targetType);
		return artifacts;
	}
	
	/**
	 * Returns the root parent type for the given VWA.  If the parent type is another VWA, this method
	 * will search the hierarchy for the base (non-VWA) parent type.
	 * 
	 * @param vwa  the VWA for which schema artifacts are being generated
	 * @return NamedEntity
	 */
	private NamedEntity getBaseParentType(TLValueWithAttributes vwa) {
		NamedEntity parentType = vwa.getParentType();
		
		while (parentType instanceof TLValueWithAttributes) {
			parentType = ((TLValueWithAttributes) parentType).getParentType();
		}
		return parentType;
	}
	
}

/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.opentravel.ns.ota2.appinfo_v01_00.OTA2Entity;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Appinfo;
import org.w3._2001.xmlschema.Attribute;
import org.w3._2001.xmlschema.ComplexType;
import org.w3._2001.xmlschema.Documentation;
import org.w3._2001.xmlschema.ExplicitGroup;
import org.w3._2001.xmlschema.FormChoice;
import org.w3._2001.xmlschema.Include;
import org.w3._2001.xmlschema.NoFixedFacet;
import org.w3._2001.xmlschema.OpenAttrs;
import org.w3._2001.xmlschema.Restriction;
import org.w3._2001.xmlschema.Schema;
import org.w3._2001.xmlschema.SimpleType;
import org.w3._2001.xmlschema.TopLevelComplexType;
import org.w3._2001.xmlschema.TopLevelElement;
import org.w3._2001.xmlschema.TopLevelSimpleType;

import com.sabre.schemacompiler.codegen.CodeGenerationContext;
import com.sabre.schemacompiler.codegen.CodeGenerationException;
import com.sabre.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import com.sabre.schemacompiler.codegen.impl.CodegenNamespacePrefixMapper;
import com.sabre.schemacompiler.codegen.impl.LibraryMemberFilenameBuilder;
import com.sabre.schemacompiler.codegen.impl.LibraryTrimmedFilenameBuilder;
import com.sabre.schemacompiler.codegen.impl.RASOperationType;
import com.sabre.schemacompiler.codegen.util.XsdCodegenUtils;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLLibrary;

/**
 * Code generator implementation used to generate XML schema documents for RAS services from meta-model
 * business objects.
 * 
 * <p>The following context variable(s) are required when invoking this code generation module:
 * <ul>
 *   <li><code>schemacompiler.OutputFolder</code> - the folder where generated files are normally stored</li>
 *   <li><code>schemacompiler.RASOutputFolder</code> - the folder where generated RAS files are to be stored;
 *   			if omitted, this will be in a /ras sub-folder of the root output folder</li>
 * </ul>
 * 
 * @author S. Livezey
 */
public class XsdRASCodeGenerator extends AbstractXsdCodeGenerator<TLBusinessObject>  {
	
	protected static org.w3._2001.xmlschema.ObjectFactory jaxbObjectFactory = new org.w3._2001.xmlschema.ObjectFactory();
	private static org.opentravel.ns.ota2.appinfo_v01_00.ObjectFactory appInfoObjectFactory = new org.opentravel.ns.ota2.appinfo_v01_00.ObjectFactory();
	
	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(java.lang.Object)
	 */
	@Override
	protected AbstractLibrary getLibrary(TLBusinessObject source) {
		return source.getOwningLibrary();
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#getMarshaller(com.sabre.schemacompiler.model.TLModelElement, org.w3._2001.xmlschema.Schema)
	 */
	@Override
	protected Marshaller getMarshaller(TLBusinessObject source, org.w3._2001.xmlschema.Schema schema) throws JAXBException {
		Marshaller m = jaxbContext.createMarshaller();
		
		m.setSchema(validationSchema);
		m.setProperty("com.sun.xml.bind.namespacePrefixMapper",
				new CodegenNamespacePrefixMapper(getLibrary(source), false, this, schema));
		return m;
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
	 */
	@Override
	protected CodeGenerationFilenameBuilder<TLBusinessObject> getDefaultFilenameBuilder() {
		return new LibraryMemberFilenameBuilder<TLBusinessObject>();
	}

	/**
	 * The code generation for the RAS schemas is done within this code generator component instead
	 * of delegating to a transformer.
	 * 
	 * @see com.sabre.schemacompiler.codegen.xsd.AbstractXsdCodeGenerator#transformSourceObjectToJaxb(com.sabre.schemacompiler.model.TLModelElement, com.sabre.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected Object transformSourceObjectToJaxb(TLBusinessObject source, CodeGenerationContext context) throws CodeGenerationException {
		String targetNamespace = source.getNamespace();
		Schema schema = createSchema(targetNamespace, ((TLLibrary) source.getOwningLibrary()).getVersion());
		
		// Add the application info for this service
		Annotation schemaAnnotation = new Annotation();
		schemaAnnotation.getAppinfoOrDocumentation().add( XsdCodegenUtils.getServiceAppInfo(source, context) );
		schema.getIncludeOrImportOrRedefine().add(schemaAnnotation);
		
		// Create include for the main (trimmed) library schema
		Include incl = new Include();
		incl.setSchemaLocation( new LibraryTrimmedFilenameBuilder(source).buildFilename(source.getOwningLibrary(), "xsd") );
		schema.getIncludeOrImportOrRedefine().add(incl);
		
		// Generate the facet-type enumeration and RQ/RS types and elements for all RAS operations
		ComplexType getRQType = createType(source.getIdFacet(), RASOperationType.GET, true, true);
		ComplexType getRSType = createType(source.getIdFacet(), RASOperationType.GET, false, false);
		ComplexType createRQType = createType(source.getDetailFacet(), RASOperationType.CREATE, true, false);
		ComplexType createRSType = createType(source.getIdFacet(), RASOperationType.CREATE, false, false);
		ComplexType updateRQType = createType(source.getSummaryFacet(), RASOperationType.UPDATE, true, false);
		ComplexType updateRSType = createType(source.getSummaryFacet(), RASOperationType.UPDATE, false, false);
		ComplexType deleteRQType = createType(source.getIdFacet(), RASOperationType.DELETE, true, false);
		List<OpenAttrs> schemaContent = new ArrayList<OpenAttrs>();
		
		schemaContent.add( createElement(getRQType, targetNamespace) );
		schemaContent.add( getRQType );
		schemaContent.add( createElement(getRSType, targetNamespace) );
		schemaContent.add( getRSType );
		schemaContent.add( createElement(createRQType, targetNamespace) );
		schemaContent.add( createRQType );
		schemaContent.add( createElement(createRSType, targetNamespace) );
		schemaContent.add( createRSType );
		schemaContent.add( createElement(updateRQType, targetNamespace) );
		schemaContent.add( updateRQType );
		schemaContent.add( createElement(updateRSType, targetNamespace) );
		schemaContent.add( updateRSType );
		schemaContent.add( createElement(deleteRQType, targetNamespace) );
		schemaContent.add( deleteRQType );
		
		for (TLFacet queryFacet : source.getQueryFacets()) {
			if (!queryFacet.declaresContent()) {
				continue;
			}
			ComplexType findRQType = createType(queryFacet, RASOperationType.FIND, true, true);
			ComplexType findRSType = createQueryResponseType(queryFacet);
			
			schemaContent.add( createElement(findRQType, targetNamespace) );
			schemaContent.add( findRQType );
			schemaContent.add( createElement(findRSType, targetNamespace) );
			schemaContent.add( findRSType );
		}
		schemaContent.add( createFacetTypeEnum(source) );
		
		schema.getSimpleTypeOrComplexTypeOrGroup().addAll(schemaContent);
		return schema;
	}
	
	/**
	 * Returns a new JAXB <code>Schema</code> instance using the namespace and version information
	 * provided.
	 * 
	 * @param targetNamespace  the target namespace of the schema
	 * @param version  the version identifier of the schema
	 * @return Schema
	 */
	private Schema createSchema(String targetNamespace, String version) {
		Schema schema = new Schema();
		
		schema.setAttributeFormDefault(FormChoice.UNQUALIFIED);
		schema.setElementFormDefault(FormChoice.QUALIFIED);
		schema.setVersion(version);
		schema.setTargetNamespace(targetNamespace);
		return schema;
	}
	
	/**
	 * Creates a corresponding element definition for the given complex type.
	 * 
	 * @param type  the complex type for which to create an element
	 * @param targetNamespace  the target namespace of the schema
	 * @return TopLevelElement
	 */
	private TopLevelElement createElement(ComplexType type, String targetNamespace) {
		TopLevelElement element = new TopLevelElement();
		
		element.setName(type.getName());
		element.setType( new QName(targetNamespace, type.getName()) );
		return element;
	}
	
	/**
	 * Generates a single complex-type schema element for the request or response of a RAS operation for
	 * the specified business object.
	 * 
	 * @param sourceFacet  the business object facet that will define the type's content
	 * @param operationType  the type of operation for which the type will be created
	 * @param isRequest  flag indicating RQ/RS type name (true = request; false = response)
	 * @param hasFacetTypeAttribute  flag indicating whether a 'facetType' attribute should be included in the type definition
	 * @return ComplexType
	 */
	private ComplexType createType(TLFacet sourceFacet, RASOperationType operationType, boolean isRequest, boolean hasFacetTypeAttribute) {
		TLFacet targetFacet = findNonEmptyFacet(sourceFacet);
		TopLevelElement element = new TopLevelElement();
		ExplicitGroup sequence = new ExplicitGroup();
		ComplexType type = new TopLevelComplexType();
		
		if (isRequest) {
			type.setName( operationType.getRequestElementName(targetFacet) );
		} else {
			type.setName( operationType.getResponseElementName(targetFacet) );
		}
		if (hasFacetTypeAttribute) {
			Annotation annotation = new Annotation();
			Documentation doc = new Documentation();
			Attribute ftAttr = new Attribute();
			
			doc.getContent().add("Specifies the facet type of the element(s) to be returned in the response.");
			annotation.getAppinfoOrDocumentation().add( doc );
			
			ftAttr.setName("responseType");
			ftAttr.setType( new QName(sourceFacet.getNamespace(), targetFacet.getOwningEntity().getLocalName() + "_FacetType") );
			ftAttr.setAnnotation(annotation);
			
			type.getAttributeOrAttributeGroup().add( ftAttr );
		}
		element.setRef( XsdCodegenUtils.getGlobalElementName(targetFacet) );
		sequence.getParticle().add( jaxbObjectFactory.createElement(element) );
		type.setAnnotation(createAnnotation(targetFacet, operationType));
		type.setSequence(sequence);
		return type;
	}
	
	/**
	 * Generates a complex-type for the response element of a query operation.
	 *  
	 * @param sourceFacet  the business object facet that will define the type's content
	 * @return ComplexType
	 */
	private ComplexType createQueryResponseType(TLFacet sourceFacet) {
		TopLevelElement element = new TopLevelElement();
		ExplicitGroup sequence = new ExplicitGroup();
		ComplexType type = new TopLevelComplexType();
		
		element.setRef( XsdCodegenUtils.getGlobalElementName(sourceFacet) );
		element.setMinOccurs(BigInteger.ZERO);
		element.setMaxOccurs("unbounded");
		
		type.setName( RASOperationType.FIND.getResponseElementName(sourceFacet) );
		type.setAnnotation(createAnnotation(sourceFacet, RASOperationType.FIND));
		type.setSequence(sequence);
		sequence.getParticle().add( jaxbObjectFactory.createElement(element) );
		
		return type;
	}
	
	/**
	 * Creates an annotation element for a RAS operation type definition.
	 * 
	 * @param sourceFacet  the business object facet that will define the type's content
	 * @param operationType  the type of operation for which the type will be created
	 * @return Annotation
	 */
	private Annotation createAnnotation(TLFacet sourceFacet, RASOperationType operationType) {
		OTA2Entity ota2Entity = XsdCodegenUtils.buildEntityAppInfo( sourceFacet.getOwningEntity() );
		Annotation annotation = new Annotation();
		Appinfo appInfo = new Appinfo();
		
		appInfo.getContent().add( appInfoObjectFactory.createOTA2Entity(ota2Entity) );
		annotation.getAppinfoOrDocumentation().add(appInfo);
		return annotation;
	}
	
	/**
	 * Generates the facet-type enumeration for the given business object.
	 * 
	 * @param source  the business object for which the RAS schema is being generated
	 * @return SimpleType
	 */
	private SimpleType createFacetTypeEnum(TLBusinessObject source) {
		SimpleType facetTypeEnum = new TopLevelSimpleType();
		Restriction restriction = new Restriction();
		
		facetTypeEnum.setName(source.getName() + "_FacetType");
		restriction.setBase( new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string") );
		
		if (source.getIdFacet().declaresContent()) {
			restriction.getFacets().add( createFacetEnumValue(source.getIdFacet()) );
		}
		if (source.getSummaryFacet().declaresContent()) {
			restriction.getFacets().add( createFacetEnumValue(source.getSummaryFacet()) );
		}
		if (source.getDetailFacet().declaresContent()) {
			restriction.getFacets().add( createFacetEnumValue(source.getDetailFacet()) );
		}
		for (TLFacet customFacet : source.getCustomFacets()) {
			restriction.getFacets().add( createFacetEnumValue(customFacet) );
		}
		facetTypeEnum.setRestriction(restriction);
		return facetTypeEnum;
	}
	
	/**
	 * Constructs an XML schema representation of the given meta-model enumeration value.
	 * 
	 * @param modelEnum  the enumeration value from the compiler meta-model
	 * @return JAXBElement<NoFixedFacet>
	 */
	private JAXBElement<NoFixedFacet> createFacetEnumValue(TLFacet facet) {
		NoFixedFacet enumValue = new NoFixedFacet();
		
		enumValue.setValue(facet.getOwningEntity().getLocalName()
				+ "_" + facet.getFacetType().getIdentityName(facet.getContext(), facet.getLabel()));
		return jaxbObjectFactory.createEnumeration(enumValue);
	}
	
	/**
	 * If the specified facet does not publish any generated content, this method will return an
	 * alternate facet to which the caller can refer.
	 * 
	 * @param preferredFacet  the preferred facet to reference
	 * @return TLFacet
	 */
	private TLFacet findNonEmptyFacet(TLFacet preferredFacet) {
		TLBusinessObject facetOwner = (TLBusinessObject) preferredFacet.getOwningEntity();
		TLFacet targetFacet = preferredFacet;
		
		while (!targetFacet.declaresContent()) {
			switch (targetFacet.getFacetType()) {
				case SUMMARY:
					targetFacet = facetOwner.getIdFacet();
					break;
				case DETAIL:
				case CUSTOM:
					targetFacet = facetOwner.getSummaryFacet();
					break;
			}
		}
		return targetFacet;
	}
	
}

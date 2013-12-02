/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd;

import java.math.BigInteger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.opentravel.ns.ota2.appinfo_v01_00.OTA2Entity;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Appinfo;
import org.w3._2001.xmlschema.TopLevelElement;

import com.sabre.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import com.sabre.schemacompiler.codegen.util.PropertyCodegenUtils;
import com.sabre.schemacompiler.codegen.util.XsdCodegenUtils;
import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLAliasOwner;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLFacetOwner;
import com.sabre.schemacompiler.model.TLListFacet;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLPropertyOwner;
import com.sabre.schemacompiler.model.TLPropertyType;
import com.sabre.schemacompiler.transform.AnonymousEntityFilter;
import com.sabre.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>TLProperty</code> objects to the JAXB nodes used
 * to produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLPropertyCodegenTransformer extends AbstractXsdTransformer<TLProperty,TopLevelElement> {
	
	/**
	 * If the 'repeat' value of a property is greater than this threshold value, the XSD element definition
	 * will be created with a 'maxOccurs' value of "unbounded".
	 */
	private static final int MAX_OCCURS_UNBOUNDED_THRESHOLD = 5000;
	
	private static org.opentravel.ns.ota2.appinfo_v01_00.ObjectFactory appInfoObjectFactory = new org.opentravel.ns.ota2.appinfo_v01_00.ObjectFactory();
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TopLevelElement transform(TLProperty source) {
		TopLevelElement element = source.isReference() ? transformReferenceProperty(source) : transformValueProperty(source);
		
		// Add documentation, equivalents, and examples to the element's annotation as required
		if (source.getDocumentation() != null) {
			ObjectTransformer<TLDocumentation,Annotation,CodeGenerationTransformerContext> docTransformer =
				getTransformerFactory().getTransformer(source.getDocumentation(), Annotation.class);
			
			element.setAnnotation( docTransformer.transform(source.getDocumentation()) );
		}
		XsdCodegenUtils.addEquivalentInfo( source, element );
		return element;
	}
	
	/**
	 * Performs the transformation of the property as a standard value element.
	 * 
	 * @param source  the source object being transformed
	 * @return TopLevelElement
	 */
	private TopLevelElement transformValueProperty(TLProperty source) {
		TLPropertyOwner propertyOwner = source.getPropertyOwner();
		TLPropertyType propertyType = PropertyCodegenUtils.resolvePropertyType(source.getPropertyOwner(), source.getType());
		TopLevelElement element = new TopLevelElement();
		
		if (!PropertyCodegenUtils.hasGlobalElement(propertyType)) {
			// If the property references a type that does not define a global element, assign the name/type fields
			// of the JAXB element
			String propertyTypeNS = propertyType.getNamespace();
			
			if ((propertyTypeNS == null) || propertyTypeNS.equals(AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE)) {
				// If this type is from a chameleon schema, replace its namespace with that of the local library
				propertyTypeNS = propertyOwner.getNamespace();
			}
			element.setType( new QName(propertyType.getNamespace(), XsdCodegenUtils.getGlobalTypeName(propertyType, source)) );
			
			// If the element's name has not been specified, use the name of its assigned type
			if ((source.getName() == null) || (source.getName().length() == 0)) {
				element.setName( source.getType().getLocalName() );
			} else {
				element.setName( source.getName() );
			}
			
		} else {
			// If the property references a type that defines a global element, assign the 'ref' field of the JAXB element.
			QName propertyRef = PropertyCodegenUtils.getDefaultSchemaElementName(propertyType, false);
			String propertyTypeNS = propertyRef.getNamespaceURI();
			
			// If this type is from a chameleon schema, replace its namespace with that of the local library
			if ((propertyTypeNS == null) || propertyTypeNS.equals(AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE)) {
				propertyRef = new QName(propertyOwner.getNamespace(), propertyRef.getLocalPart());
			}
			element.setRef(propertyRef);
		}
		
		// Assign the mix/max occurs for the generated element
		if (source.isMandatory()) {
			element.setMinOccurs(BigInteger.valueOf(1));
		} else {
			element.setMinOccurs(BigInteger.valueOf(0));
		}
		if (source.getType() instanceof TLListFacet) {
			TLCoreObject facetOwner = (TLCoreObject) ((TLListFacet) source.getType()).getOwningEntity();
			
			if (facetOwner.getRoleEnumeration().getRoles().size() > 0) {
				element.setMaxOccurs( facetOwner.getRoleEnumeration().getRoles().size() + "" );
			} else {
				element.setMaxOccurs( getMaxOccurs(source) );
			}
		} else {
			element.setMaxOccurs( getMaxOccurs(source) );
		}
		XsdCodegenUtils.addExampleInfo( source, element );
		
		return element;
	}
	
	/**
	 * Performs the transformation of the property as an IDREF(S) element.
	 * 
	 * @param source  the source object being transformed
	 * @return TopLevelElement
	 */
	private TopLevelElement transformReferenceProperty(TLProperty source) {
		TLPropertyType propertyType = PropertyCodegenUtils.resolvePropertyType(source.getPropertyOwner(), source.getType());
		TopLevelElement element = new TopLevelElement();
		Annotation annotation = new Annotation();
		Appinfo appInfo = new Appinfo();
		OTA2Entity ota2Entity = new OTA2Entity();
		String elementName = source.getName();
		String maxOccurs = getMaxOccurs(source);
		boolean isMultipleReference;
		
		if (PropertyCodegenUtils.hasGlobalElement(propertyType)) {
			elementName = PropertyCodegenUtils.getDefaultSchemaElementName(propertyType, true).getLocalPart();
			
		} else {
			elementName = source.getName();
			
			if (!elementName.endsWith("Ref")) {
				// probably a VWA reference, so we need to make sure the "Ref" suffix is appended
				elementName += "Ref";
			}
		}
		
		if (maxOccurs == null) {
			isMultipleReference = false;
			
		} else if (maxOccurs.equals("unbounded")) {
			isMultipleReference = true;
			
		} else {
			try {
				isMultipleReference = Integer.parseInt(maxOccurs) > 1;
				
			} catch (NumberFormatException e) {
				// should never happen, but just in case...
				isMultipleReference = false;
			}
		}
		
		element.setName( elementName );
		element.setType( new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, isMultipleReference ? "IDREFS" : "IDREF") );
		element.setAnnotation( annotation );
		annotation.getAppinfoOrDocumentation().add( appInfo );
		ota2Entity.setType(element.getType().getLocalPart());
		ota2Entity.setValue(source.getType().getLocalName());
		appInfo.getContent().add( appInfoObjectFactory.createOTA2EntityReference(ota2Entity) );
		
		if (!source.isMandatory()) {
			element.setMinOccurs( BigInteger.ZERO );
		}
		return element;
	}
	
	/**
	 * Identifies the 'maxOccurs' value for the generated element, typically this is defined by
	 * the 'repeat' attribute of the <code>TLPropertyElement</code>.
	 * 
 	 * Special Case: Properties that reference core object list facets as their type should assign
	 * the maxOccurs attribute to the number of roles in the core object.
	 * 
	 * @param source  the model property being rendered
	 * @return String
	 */
	private String getMaxOccurs(TLProperty source) {
		TLPropertyType facetType = source.getType();
		TLListFacet listFacet = null;
		String maxOccurs = null;
		
		// Check for special case with core object list facets
		if (facetType instanceof TLListFacet) {
			listFacet = (TLListFacet) facetType;
			
		} else if (facetType instanceof TLAlias) {
			TLAlias alias = (TLAlias) facetType;
			TLAliasOwner aliasOwner = alias.getOwningEntity();
			
			if (aliasOwner instanceof TLListFacet) {
				listFacet = (TLListFacet) aliasOwner;
			}
		}
		if (listFacet != null) {
			TLFacetOwner facetOwner = listFacet.getOwningEntity();
			
			if (facetOwner instanceof TLCoreObject) {
				TLCoreObject core = (TLCoreObject) facetOwner;
				
				if (core.getRoleEnumeration().getRoles().size() > 0) {
					maxOccurs = core.getRoleEnumeration().getRoles().size() + "";
				}
			}
			listFacet.getOwningEntity();
		}
		
		// Normal processing for maxOccurs if the special case was not present
		if (maxOccurs == null) {
			if ((source.getRepeat() < 0) || (source.getRepeat() > MAX_OCCURS_UNBOUNDED_THRESHOLD)) {
				maxOccurs = "unbounded";
				
			} else if (source.getRepeat() > 0) {
				maxOccurs = source.getRepeat() + "";
			}
		}
		return maxOccurs;
	}
	
}

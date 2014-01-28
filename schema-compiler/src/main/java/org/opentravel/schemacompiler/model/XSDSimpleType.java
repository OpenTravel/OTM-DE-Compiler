/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.model;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3._2001.xmlschema.Restriction;
import org.w3._2001.xmlschema.SimpleType;
import org.w3._2001.xmlschema.TopLevelSimpleType;

/**
 * Library member that represents a simple type declaration from a legacy XML schema.
 * 
 * @author S. Livezey
 */
public class XSDSimpleType extends LibraryMember implements TLAttributeType {
	
	private String name;
	private TopLevelSimpleType jaxbType;
	private XSDFacetProfile xsdFacetProfile;
	private boolean isInitialized = false;
	
	/**
	 * Constructor that specifies the name of this model element and the underlying
	 * JAXB type from which it was created.
	 * 
	 * @param name  the name of the model element
	 * @param jaxbType  the JAXB type that was used to create this element
	 */
	public XSDSimpleType(String name, TopLevelSimpleType jaxbType) {
		this.name = name;
		this.jaxbType = jaxbType;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		AbstractLibrary owningLibrary = getOwningLibrary();
		StringBuilder identity = new StringBuilder();
		
		if (owningLibrary != null) {
			identity.append(owningLibrary.getValidationIdentity()).append(" : ");
		}
		if (name == null) {
			identity.append("[Unnamed XSD Simple Type]");
		} else {
			identity.append(name);
		}
		return identity.toString();
	}
	
	/**
	 * Returns the value of the 'name' field.
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
	 */
	@Override
	public String getLocalName() {
		return name;
	}

	/**
	 * Returns the value of the 'jaxbType' field.
	 *
	 * @return TopLevelSimpleType
	 */
	public TopLevelSimpleType getJaxbType() {
		return jaxbType;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLAttributeType#getXSDFacetProfile()
	 */
	@Override
	public XSDFacetProfile getXSDFacetProfile() {
		if (!isInitialized) {
			initializeTypeCharacteristics();
		}
		return xsdFacetProfile;
	}

	/**
	 * Initializes the XSD facet profile this simple type.
	 */
	private void initializeTypeCharacteristics() {
		if ((getOwningModel() != null) && (getOwningLibrary() != null)) {
			String rootTypename = getRootXmlTypename(getNamespace(), jaxbType);
			
			xsdFacetProfile = XSDFacetProfile.toFacetProfile(rootTypename);
			isInitialized = true;
		}
	}
	
	/**
	 * Navigates the assigned type(s) of the given simple type until an extension or restriction
	 * of a primitive XML type is discovered.  If no such type can be identified, this method will
	 * return null.
	 * 
	 * @return String
	 */
	private String getRootXmlTypename(String namespace, SimpleType jaxbType) {
		String rootTypename = null;
		
		if (namespace != null) {
			if (namespace.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
				rootTypename = jaxbType.getName();
				
			} else if (jaxbType.getRestriction() != null) {
				Restriction restriction = jaxbType.getRestriction();
				
				if (restriction.getBase() != null) {
					SimpleType baseType = findJaxbType(restriction.getBase());
					
					if (baseType != null) {
						rootTypename = getRootXmlTypename(restriction.getBase().getNamespaceURI(), baseType);
					}
				} else if (restriction.getSimpleType() != null) {
					rootTypename = getRootXmlTypename(namespace, restriction.getSimpleType());
				}
			}
		}
		return rootTypename;
	}
	
	/**
	 * Searches the model for an <code>XSDSimpleType</code> with the specified Q-Name, and returns its underlying
	 * JAXB object.
	 * 
	 * @param simpleTypeQName  the qualified name of the XML simple type to find
	 * @return SimpleType
	 */
	private SimpleType findJaxbType(QName simpleTypeQName) {
		SimpleType result = null;
		
		for (AbstractLibrary library : getOwningModel().getLibrariesForNamespace(simpleTypeQName.getNamespaceURI())) {
			LibraryMember member = library.getNamedMember(simpleTypeQName.getLocalPart());
			
			if (member instanceof XSDSimpleType) {
				result = ((XSDSimpleType) member).getJaxbType();
				break;
			}
		}
		return result;
	}
	
}

/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.model;

import org.w3._2001.xmlschema.TopLevelElement;

/**
 * Library member that represents a top-level element declaration from a legacy XML schema.
 * 
 * <p>If this element is considered to be an alias of an <code>XSDComplexType</code> instance,
 * the 'aliasedType' field will reference the aliased entity.
 * 
 * @author S. Livezey
 */
public class XSDElement extends LibraryMember implements TLPropertyType {
	
	private String name;
	private TopLevelElement jaxbElement;
	private XSDComplexType aliasedType;
	
	/**
	 * Constructor that specifies the name of this model element and the underlying
	 * JAXB element from which it was created.
	 * 
	 * @param name  the name of the model element
	 * @param jaxbElement  the JAXB element that was used to create this element
	 */
	public XSDElement(String name, TopLevelElement jaxbElement) {
		this.name = name;
		this.jaxbElement = jaxbElement;
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
			identity.append("[Unnamed XSD Element]");
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
	 * Returns the value of the 'jaxbElement' field.
	 *
	 * @return TopLevelElement
	 */
	public TopLevelElement getJaxbElement() {
		return jaxbElement;
	}

	/**
	 * Returns the complex type that is aliased by this element (may be null).
	 *
	 * @return XSDComplexType
	 */
	public XSDComplexType getAliasedType() {
		return aliasedType;
	}

	/**
	 * Assigns the complex type that is aliased by this element (may be null).
	 *
	 * @param aliasedType  the complex type instance to assign
	 */
	public void setAliasedType(XSDComplexType aliasedType) {
		this.aliasedType = aliasedType;
	}

}

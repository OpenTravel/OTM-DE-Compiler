/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.model;


/**
 * Library definition for closed enumeration types.
 * 
 * @author S. Livezey
 */
public class TLClosedEnumeration extends TLAbstractEnumeration implements TLAttributeType {
	
	/**
	 * @see com.sabre.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		AbstractLibrary owningLibrary = getOwningLibrary();
		StringBuilder identity = new StringBuilder();
		
		if (owningLibrary != null) {
			identity.append(owningLibrary.getValidationIdentity()).append(" : ");
		}
		if (getName() == null) {
			identity.append("[Unnamed Closed Enumeration Type]");
		} else {
			identity.append(getName());
		}
		return identity.toString();
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLAttributeType#getXSDFacetProfile()
	 */
	@Override
	public XSDFacetProfile getXSDFacetProfile() {
		return XSDFacetProfile.FP_String;
	}

}

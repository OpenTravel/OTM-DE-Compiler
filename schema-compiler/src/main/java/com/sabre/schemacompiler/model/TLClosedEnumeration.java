/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.model;

import com.sabre.schemacompiler.version.Versioned;


/**
 * Library definition for closed enumeration types.
 * 
 * @author S. Livezey
 */
public class TLClosedEnumeration extends TLAbstractEnumeration implements Versioned, TLAttributeType {
	
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
     * @see com.sabre.schemacompiler.version.Versioned#getVersion()
     */
    @Override
    public String getVersion() {
        AbstractLibrary owningLibrary = getOwningLibrary();
        String version = null;
        
        if (owningLibrary instanceof TLLibrary) {
            version = ((TLLibrary) owningLibrary).getVersion();
        }
        return version;
    }

    /**
     * @see com.sabre.schemacompiler.version.Versioned#getVersionScheme()
     */
    @Override
    public String getVersionScheme() {
        AbstractLibrary owningLibrary = getOwningLibrary();
        String versionScheme = null;
        
        if (owningLibrary instanceof TLLibrary) {
            versionScheme = ((TLLibrary) owningLibrary).getVersionScheme();
        }
        return versionScheme;
    }

    /**
     * @see com.sabre.schemacompiler.version.Versioned#getBaseNamespace()
     */
    @Override
    public String getBaseNamespace() {
        AbstractLibrary owningLibrary = getOwningLibrary();
        String baseNamespace;
        
        if (owningLibrary instanceof TLLibrary) {
            baseNamespace = ((TLLibrary) owningLibrary).getBaseNamespace();
        } else {
            baseNamespace = getNamespace();
        }
        return baseNamespace;
    }

    /**
     * @see com.sabre.schemacompiler.version.Versioned#isLaterVersion(com.sabre.schemacompiler.version.Versioned)
     */
    @Override
    public boolean isLaterVersion(Versioned otherVersionedItem) {
        boolean result = false;
        
        if ((otherVersionedItem != null) && otherVersionedItem.getClass().equals(this.getClass())
                && (this.getOwningLibrary() != null) && (otherVersionedItem.getOwningLibrary() != null)
                && (this.getLocalName() != null) && this.getLocalName().equals(otherVersionedItem.getLocalName())) {
            result = this.getOwningLibrary().isLaterVersion(otherVersionedItem.getOwningLibrary());
        }
        return result;
    }
	
	/**
	 * @see com.sabre.schemacompiler.model.TLAttributeType#getXSDFacetProfile()
	 */
	@Override
	public XSDFacetProfile getXSDFacetProfile() {
		return XSDFacetProfile.FP_String;
	}

}

/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.model;

/**
 * Simple attribute type for library types.
 * 
 * @author S. Livezey
 */
public interface TLAttributeType extends TLPropertyType {
	
	/**
	 * Returns the <code>XSDFacetProfile</code> value that indicates which XML schema facets
	 * are applicable to this attribute type.  If the correct facet profile cannot be identified,
	 * this method will return null. 
	 * 
	 * @return XSDFacetProfile
	 */
	public XSDFacetProfile getXSDFacetProfile();
	
}

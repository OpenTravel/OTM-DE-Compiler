/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen.impl;

import org.opentravel.schemacompiler.model.TLFacet;

/**
 * Defines types of RAS operations for which WSDL operations may be generated, as well as the naming
 * scheme for those operations and their RQ/RS elements.
 * 
 * @author S. Livezey
 */
public enum RASOperationType {
	
	GET("Get"),
	CREATE("Create"),
	UPDATE("Update"),
	DELETE("Delete"),
	FIND("Find");
	
	private String operationName;
	
	/**
	 * Constructor that specifies the name of the operation as it will appear in generated XML schema docuemtns.
	 * 
	 * @param operationName  the name of the operation
	 */
	private RASOperationType(String operationName) {
		this.operationName = operationName;
	}
	
	/**
	 * Returns the name of the WSDL operation the specified business object.
	 * 
	 * @param targetFacet  the target business object facet for the operation
	 * @return String
	 */
	public String getOperationName(TLFacet targetFacet) {
		StringBuilder opName = new StringBuilder(operationName).append("_").append(targetFacet.getOwningEntity().getLocalName());
		
		if (targetFacet.getFacetType().isContextual()) {
			opName.append("_").append(targetFacet.getFacetType().getIdentityName(targetFacet.getContext(), targetFacet.getLabel()));
		}
		return opName.toString();
	}
	
	/**
	 * Returns the name of the request element for the operation type on the specified business object.
	 * 
	 * @param targetFacet  the target business object facet for the operation
	 * @return String
	 */
	public String getRequestElementName(TLFacet targetFacet) {
		return getOperationName(targetFacet) + "RQ";
	}
	
	/**
	 * Returns the name of the response element for the operation type on the specified business object.
	 * 
	 * @param targetFacet  the target business object facet for the operation
	 * @return String
	 */
	public String getResponseElementName(TLFacet targetFacet) {
		return getOperationName(targetFacet) + "RS";
	}
	
}

/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.model;


/**
 * Indicates the type or purpose of a <code>DocumentationItem</code> element.
 * 
 * @author S. Livezey
 */
public enum TLDocumentationType {
	
	DEPRECATION("Deprecation"),
	DESCRIPTION("Description"),
	REFERENCE("Reference"),
	IMPLEMENTER("Implementer"),
	MORE_INFO("More Info"),
	OTHER_DOC("Other Doc");
	
	private String displayIdentity;
	
	/**
	 * Constructor that specifies the display identity of the value.
	 * 
	 * @param displayIdentity  the display identity string
	 */
	private TLDocumentationType(String displayIdentity) {
		this.displayIdentity = displayIdentity;
	}
	
	/**
	 * Returns the display identity string for this value.
	 * 
	 * @return String
	 */
	public String getDisplayIdentity() {
		return displayIdentity;
	}
	
}

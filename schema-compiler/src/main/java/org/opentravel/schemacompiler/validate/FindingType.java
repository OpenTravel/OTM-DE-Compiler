/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.validate;

import java.util.ResourceBundle;

/**
 * Enumeration of the various types of validation findings.
 * 
 * @author S. Livezey
 */
public enum FindingType {
	
	/** Finding type for errors. */
	ERROR("schemacompiler.findingType.ERROR", "Error"),
	
	/** Finding type for warnings. */
	WARNING("schemacompiler.findingType.WARNING", "Warning");
	
	
	private String resourceKey;
	private String displayName;
	
	/**
	 * Constructor that assigns the resource key and display name for an enum value.
	 * 
	 * @param resourceKey  the resource key for the value
	 * @param displayName  the display name for the value
	 */
	private FindingType(String resourceKey, String displayName) {
		this.resourceKey = resourceKey;
		this.displayName = displayName;
	}
	
	/**
	 * Returns the resource key for the value.
	 * 
	 * @return String
	 */
	public String getResourceKey() {
		return resourceKey;
	}
	
	/**
	 * Returns the display name for the value.
	 * 
	 * @return String
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	/**
	 * Returns the display name for the value.
	 * 
	 * @param bundle  the resource bundle to use for display name lookups
	 * @return String
	 */
	public String getDisplayName(ResourceBundle bundle) {
		String result = displayName;
		
		if ((bundle != null) && bundle.containsKey(resourceKey)) {
			result = bundle.getString(resourceKey);
		}
		return result;
	}
	
}

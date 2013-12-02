/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.loader.impl;

import java.lang.reflect.Method;

import com.sabre.schemacompiler.validate.Validatable;

/**
 * Source object wrapper for JAXB library objects used for compatability with the validation
 * framework.
 * 
 * @author S. Livezey
 */
public class LibraryValidationSource implements Validatable {
	
	private Object library;
	
	/**
	 * Constructor that specifies the library instance to be wrapped.
	 * 
	 * @param library  the library instance
	 */
	public LibraryValidationSource(Object library) {
		this.library = library;
	}
	
	/**
	 * Returns the underlying library instance.
	 * 
	 * @return Library
	 */
	public Object getLibrary() {
		return library;
	}
	
	/**
	 * @see com.sabre.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		String identity = "[Unknown Library]";
		
		if (library != null) {
			try {
				Method getNameMethod = library.getClass().getMethod("getName");
				Object nameValue = getNameMethod.invoke(library);
				
				if (nameValue instanceof String) {
					identity = (String) nameValue;
				}
			} catch (Throwable t) {
				// No Error - Return an unknown library identity
			}
		}
		return identity;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		
		if (obj instanceof LibraryValidationSource) {
			result = ( ((LibraryValidationSource) obj).library == this.library );
		}
		return result;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (library == null) ? 0 : library.hashCode();
	}
	
}

/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.loader.impl;

import java.net.URL;

import com.sabre.schemacompiler.validate.Validatable;

/**
 * Source object wrapper for URL objects used for compatability with the validation framework.
 * 
 * @author S. Livezey
 */
public class URLValidationSource implements Validatable {
	
	private URL url;
	
	/**
	 * Constructor that specifies the URL instance to be wrapped.
	 * 
	 * @param url  the URL instance
	 */
	public URLValidationSource(URL url) {
		this.url = url;
	}
	
	/**
	 * Returns the underlying URL instance.
	 * 
	 * @return URL
	 */
	public URL getUrl() {
		return url;
	}
	
	/**
	 * @see com.sabre.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		if (url == null) {
			return "[MISSING URL]";
		} else {
			String urlPath = url.getPath();
			int idx = urlPath.lastIndexOf('/');
			
			if (!urlPath.endsWith("/") && (idx >= 0)) {
				urlPath = urlPath.substring(idx + 1);
			}
			return urlPath;
		}
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		
		if (obj instanceof URLValidationSource) {
			result = ( ((URLValidationSource) obj).url == this.url );
		}
		return result;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (url == null) ? 0 : url.hashCode();
	}
	
}

/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.model;


/**
 * Interface to be implemented by all model components that can own <code>TLDocumentation</code>
 * elements.
 *
 * @author S. Livezey
 */
public interface TLDocumentationOwner extends LibraryElement {
	
	/**
	 * Returns the documentation instance that is owned by this component.
	 *
	 * @return TLDocumentation
	 */
	public TLDocumentation getDocumentation();
	
	/**
	 * Assigns the documentation instance that is owned by this component.
	 *
	 * @param documentation  the documentation instance to assign
	 */
	public void setDocumentation(TLDocumentation documentation);
	
}

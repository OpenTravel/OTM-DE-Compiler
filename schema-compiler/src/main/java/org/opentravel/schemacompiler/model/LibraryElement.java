/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.model;

/**
 * Interface to be implemented by all model entities that are contained within a library.
 * 
 * @author S. Livezey
 */
public interface LibraryElement extends ModelElement {
	
	/**
	 * Returns the library instance that owns this model element.
	 * 
	 * @return AbstractLibrary
	 */
	public AbstractLibrary getOwningLibrary();
	
	/**
	 * Creates a deep-copy of this model element.  The only difference between this one and the new
	 * copy is that the new copy will not yet be assigned to an owning library.
	 * 
	 * @return LibraryElement
	 * @throws IllegalArgumentException  thrown if this model element cannot be cloned
	 */
	public LibraryElement cloneElement();
	
	/**
	 * Creates a deep-copy of this model element.  The only difference between this one and the new
	 * copy is that the new copy will not yet be assigned to an owning library.
	 * 
	 * @param namingContext  the library whose owning that should be used for reference lookups when
	 *						 resolving names in the cloned entity; the library itself is used to resolve
	 *						 namespace prefix references during reference lookups
	 * @return LibraryElement
	 * @throws IllegalArgumentException  thrown if this model element cannot be cloned
	 */
	public LibraryElement cloneElement(AbstractLibrary namingContext);
	
}

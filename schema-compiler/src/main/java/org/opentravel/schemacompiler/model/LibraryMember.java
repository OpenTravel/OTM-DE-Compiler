/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.model;

/**
 * Type library model component that is defined as a top-level member within a
 * single library instance.
 * 
 * @author S. Livezey
 */
public abstract class LibraryMember extends TLModelElement implements NamedEntity {
	
	private AbstractLibrary owningLibrary;
	
	/**
	 * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
	 */
	@Override
	public TLModel getOwningModel() {
		return (owningLibrary == null) ? null : owningLibrary.getOwningModel();
	}

	/**
	 * @see org.opentravel.schemacompiler.model.NamedEntity#getOwningLibrary()
	 */
	public AbstractLibrary getOwningLibrary() {
		return owningLibrary;
	}
	
	/**
	 * Assigns the library instance that owns this model element.
	 *
	 * @param owningLibrary  the owning library instance to assign
	 */
	public void setOwningLibrary(AbstractLibrary owningLibrary) {
		this.owningLibrary = owningLibrary;
	}

	/**
	 * Returns the namespace of the owning library.  Sub-classes may override
	 * if the namespace of the entity is different from that of the library
	 * that defined it.
	 * 
	 * @return String
	 */
	public String getNamespace() {
		return (owningLibrary == null) ? null : owningLibrary.getNamespace();
	}
	
}

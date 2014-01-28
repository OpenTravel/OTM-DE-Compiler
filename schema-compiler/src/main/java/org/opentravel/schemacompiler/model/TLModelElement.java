/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.model;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.util.ModelElementCloner;

/**
 * Base class for all type library model elements.
 * 
 * @author S. Livezey
 */
public abstract class TLModelElement implements ModelElement {
	
	/**
	 * Default constructor.
	 */
	public TLModelElement() {}
	
	/**
	 * Publishes the given event to all registered listeners of the owning model that are
	 * capable of processing it.
	 * 
	 * @param event  the event to publish
	 */
	protected void publishEvent(ModelEvent<?> event) {
		TLModel owningModel = getOwningModel();
		
		if (owningModel != null) {
			owningModel.publishEvent(event);
		}
	}
	
	/**
	 * Creates a deep-copy of this model element.  The only difference between this one and the new
	 * copy is that the new copy will not yet be assigned to an owning library.
	 * 
	 * @return TLModelElement
	 * @throws IllegalArgumentException  thrown if this model element cannot be cloned
	 */
	public LibraryElement cloneElement() {
		return cloneElement( (this instanceof LibraryElement) ? ((LibraryElement) this).getOwningLibrary() : null );
	}

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
	public LibraryElement cloneElement(AbstractLibrary namingContext) {
		return (LibraryElement) new ModelElementCloner(
				(namingContext == null) ? null : namingContext.getOwningModel() ).clone( this, namingContext );
	}
	
}

/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.ic;

import com.sabre.schemacompiler.event.ModelEvent;
import com.sabre.schemacompiler.event.ModelEventListener;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.LibraryElement;
import com.sabre.schemacompiler.model.TLModelElement;

/**
 * Base integrity-checker class that provides common methods used by most integrity-check routines.
 * 
 * @param <E>  the event type that this listener is designed to process
 * @param <S>  the source object type for the events to be processed by this listener
 * @author S. Livezey
 */
public abstract class AbstractIntegrityChecker<E extends ModelEvent<S>,S> implements ModelEventListener<E,S> {
	
	/**
	 * Returns the library that owns the given model element, regardless of whether the
	 * ownership is direct or indirect.
	 * 
	 * @param entity  the entity for which to return the owning library
	 * @return AbstractLibrary
	 */
	protected AbstractLibrary getOwningLibrary(TLModelElement entity) {
		AbstractLibrary owningLibrary = null;
		
		if (entity instanceof LibraryElement) {
			owningLibrary = ((LibraryElement) entity).getOwningLibrary();
		}
		return owningLibrary;
	}
	
}

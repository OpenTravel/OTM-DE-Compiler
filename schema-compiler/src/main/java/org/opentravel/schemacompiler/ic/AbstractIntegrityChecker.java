
package org.opentravel.schemacompiler.ic;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventListener;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.TLModelElement;

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

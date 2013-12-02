/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.event;


/**
 * Interface to be implemented by components that will respond to <code>ModelEvent</code>
 * occurrances.
 * 
 * @param <E>  the event type that this listener is designed to process
 * @param <S>  the source object type for the events to be processed by this listener
 * @author S. Livezey
 */
public interface ModelEventListener<E extends ModelEvent<S>, S> {
	
	/**
	 * Called by the <code>TLModel</code> when a change has been detected in the meta-model
	 * composition or hierarchy.
	 * 
	 * @param event  the event that was broadcast in response to a change in the meta-model
	 */
	public void processModelEvent(E event);
	
	/**
	 * Returns the type of the event to be processed by this listener.
	 * 
	 * @return Class<?>
	 */
	public Class<?> getEventClass();
	
	/**
	 * Returns the type of the source objects for the events to be processed by this listener.
	 * 
	 * @return Class<S>
	 */
	public Class<S> getSourceObjectClass();
	
}

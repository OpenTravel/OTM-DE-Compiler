/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.event;

import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of the <code>ModelEventFilter</code> that can restrict allowable events
 * according to the <code>ModelEventType</code> and source object type.
 * 
 * @param <E>  the event type that the underlying listener is designed to process
 * @param <S>  the source object type for the events to be processed by the underlying listener
 * @author S. Livezey
 */
public class DefaultModelEventFilter<E extends ModelEvent<S>, S> extends ModelEventFilter<E,S> {
	
	private Set<ModelEventType> allowableEventTypes = new HashSet<ModelEventType>();
	private Set<Class<? extends S>> allowableSourceObjectTypes = new HashSet<Class<? extends S>>();
	
	/**
	 * Constructor that assignes the underlying listener for this filter.
	 * 
	 * @param listener
	 */
	public DefaultModelEventFilter(ModelEventListener<E, S> listener) {
		super(listener);
	}
	
	/**
	 * Assigns the allowable event types that should be forwarded by this filter.  If no event
	 * types are specified for this filter, it is assumed that all event types should be considered
	 * allowable.
	 * 
	 * @param eventTypes  the allowable event types to be forwarded
	 */
	public void setAllowableEventTypes(ModelEventType... eventTypes) {
		for (ModelEventType eventType : eventTypes) {
			allowableEventTypes.add(eventType);
		}
	}
	
	/**
	 * Assigns the allowable source object types for events that should be forwarded by this filter.  If
	 * no source object types are specified for this filter, it is assumed that all source object types
	 * should be considered allowable.
	 * 
	 * @param sourceObjectTypes  the allowable source object types for events that should be forwarded
	 */
	@SuppressWarnings("unchecked")
	public void setAllowableSourceObjectTypes(Class<?>... sourceObjectTypes) {
		for (Class<?> sourceObjectType : sourceObjectTypes) {
			allowableSourceObjectTypes.add((Class<? extends S>) sourceObjectType);
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.event.ModelEventFilter#isAllowableEvent(com.sabre.schemacompiler.event.ModelEvent)
	 */
	@Override
	protected boolean isAllowableEvent(ModelEvent<S> event) {
		boolean isAllowable = (event != null);
		
		if (isAllowable && !allowableEventTypes.isEmpty()) {
			isAllowable = allowableEventTypes.contains(event.getType());
		}
		if (isAllowable && !allowableSourceObjectTypes.isEmpty() && (event.getSource() != null)) {
			Class<?> sourceObjectType = event.getSource().getClass();
			boolean isAssignableType = false;
			
			for (Class<? extends S> allowableType : allowableSourceObjectTypes) {
				isAssignableType |= sourceObjectType.isAssignableFrom(allowableType);
			}
			isAllowable = isAssignableType;
		}
		return isAllowable;
	}
	
}

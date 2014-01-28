/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.event;

/**
 * Event used to broadcast events associated with the change in field value for a model
 * entity.  The event's event-type will indicate which field of the source entity was
 * modified.
 *
 * @param <S>  the source object type for the event
 * @param <V>  the type of the field that was modified on the model entity
 * @author S. Livezey
 */
public class ValueChangeEvent<S,V> extends ModelEvent<S> {
	
	private V oldValue;
	private V newValue;
	
	/**
	 * Constructor that specifies the event type and source object for the event.
	 * 
	 * @param type  the type of the event being broadcast
	 * @param source  the source object that was modified to create the event
	 */
	public ValueChangeEvent(ModelEventType type, S source) {
		super(type, source);
	}

	/**
	 * Returns the original field value that existed prior to the value change event.
	 *
	 * @return V
	 */
	public V getOldValue() {
		return oldValue;
	}

	/**
	 * Assigns the original field value that existed prior to the value change event.
	 *
	 * @param oldValue  the old field value to assign
	 */
	public void setOldValue(V oldValue) {
		this.oldValue = oldValue;
	}

	/**
	 * Returns the new field value that exists after the value change event.
	 *
	 * @return V
	 */
	public V getNewValue() {
		return newValue;
	}

	/**
	 * Assigns the new field value that exists after the value change event.
	 *
	 * @param newValue  the new field value to assign
	 */
	public void setNewValue(V newValue) {
		this.newValue = newValue;
	}

}

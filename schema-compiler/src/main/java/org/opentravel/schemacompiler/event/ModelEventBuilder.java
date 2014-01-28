
package org.opentravel.schemacompiler.event;

/**
 * Builder used for the construction of <code>ModelEvent</code> instances.
 * 
 * @author S. Livezey
 */
public class ModelEventBuilder {
	
	private ModelEventType eventType;
	private Object sourceObject;
	private Object affectedItem;
	private Object oldValue;
	private Object newValue;
	
	/**
	 * Default constructor.
	 */
	public ModelEventBuilder() {}
	
	/**
	 * Convenience constructor that assigns
	 * @param eventType
	 * @param sourceObject
	 */
	public ModelEventBuilder(ModelEventType eventType, Object sourceObject) {
		this.eventType = eventType;
		this.sourceObject = sourceObject;
	}
	
	/**
	 * Constructs a new <code>ModelEvent</code> using the information provided to this builder.
	 * 
	 * @return ModelEvent<?>
	 */
	public ModelEvent<?> buildEvent() {
		ModelEvent<?> modelEvent = null;
		
		if (eventType.getEventClass().equals(OwnershipEvent.class) && (affectedItem != null)) {
			OwnershipEvent<Object,Object> event = new OwnershipEvent<Object,Object>(eventType, sourceObject);
			
			event.setAffectedItem(affectedItem);
			modelEvent = event;
			
		} else if (eventType.getEventClass().equals(ValueChangeEvent.class)) {
			boolean valueChanged = (oldValue == null) ? (newValue != null) : !oldValue.equals(newValue);
			
			// Only create a value-change event if the value actually changed
			if (valueChanged) {
				ValueChangeEvent<Object,Object> event = new ValueChangeEvent<Object,Object>(eventType, sourceObject);
				
				event.setOldValue(oldValue);
				event.setNewValue(newValue);
				modelEvent = event;
			}
		} else {
			throw new IllegalArgumentException(
					"Unknown model event class: " + eventType.getEventClass().getSimpleName());
		}
		return modelEvent;
	}
	
	/**
	 * Assigns the type of event to be created.
	 *
	 * @param eventType  the event type value to assign
	 * @return ModelEventBuilder
	 */
	public ModelEventBuilder setEventType(ModelEventType eventType) {
		this.eventType = eventType;
		return this;
	}
	
	/**
	 * Assigns the source object for the event.
	 *
	 * @param sourceObject  the source object instance to assign
	 * @return ModelEventBuilder
	 */
	public ModelEventBuilder setSourceObject(Object sourceObject) {
		this.sourceObject = sourceObject;
		return this;
	}
	
	/**
	 * Assigns the affected item for ownership events.
	 *
	 * @param affectedItem  the affected item to assign
	 * @return ModelEventBuilder
	 */
	public ModelEventBuilder setAffectedItem(Object affectedItem) {
		this.affectedItem = affectedItem;
		return this;
	}
	
	/**
	 * Assigns the old field value for value change events.
	 *
	 * @param oldValue  the old field value to assign
	 * @return ModelEventBuilder
	 */
	public ModelEventBuilder setOldValue(Object oldValue) {
		this.oldValue = oldValue;
		return this;
	}
	
	/**
	 * Assigns the new field value for value change events.
	 *
	 * @param newValue  the new field value to assign
	 * @return ModelEventBuilder
	 */
	public ModelEventBuilder setNewValue(Object newValue) {
		this.newValue = newValue;
		return this;
	}

}

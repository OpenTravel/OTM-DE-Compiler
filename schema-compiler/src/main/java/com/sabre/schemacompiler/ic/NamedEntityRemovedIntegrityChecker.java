/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.ic;

import java.util.Arrays;
import java.util.List;

import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.event.OwnershipEvent;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLModelElement;

/**
 * Integrity checker component that purges all references to an entity when it is removed from the model.
 * 
 * @param <S>  the source object type for the event
 * @author S. Livezey
 */
public class NamedEntityRemovedIntegrityChecker<S extends TLModelElement> extends EntityRemovedIntegrityChecker<S,NamedEntity> {
	
	private static ModelEventType[] ELIGIBLE_EVENT_TYPES = {
		ModelEventType.MEMBER_REMOVED, ModelEventType.ROLE_REMOVED, ModelEventType.ALIAS_REMOVED,
		ModelEventType.OPERATION_REMOVED, ModelEventType.CUSTOM_FACET_REMOVED
	};
	
	private List<ModelEventType> eligibleEvents = Arrays.asList(ELIGIBLE_EVENT_TYPES);
	private Class<S> eventSourceType;
	
	/**
	 * Constructor that specifies the source type of the event.
	 * 
	 * @param eventSourceType  the source type of the event to which this listener will respond
	 */
	public NamedEntityRemovedIntegrityChecker(Class<S> eventSourceType) {
		this.eventSourceType = eventSourceType;
	}
	
	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#processModelEvent(com.sabre.schemacompiler.event.ModelEvent)
	 */
	@Override
	public void processModelEvent(OwnershipEvent<S, NamedEntity> event) {
		if (eligibleEvents.contains(event.getType())) {
			NamedEntity removedEntity = event.getAffectedItem();
			
			if (removedEntity instanceof TLModelElement) {
				purgeEntitiesFromModel( (TLModelElement) event.getAffectedItem(),
						event.getSource().getOwningModel() );
			}
		}
	}

	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#getEventClass()
	 */
	@Override
	public Class<?> getEventClass() {
		return OwnershipEvent.class;
	}

	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#getSourceObjectClass()
	 */
	@Override
	public Class<S> getSourceObjectClass() {
		return eventSourceType;
	}
	
}

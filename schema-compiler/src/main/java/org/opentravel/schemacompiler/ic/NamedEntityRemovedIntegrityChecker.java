
package org.opentravel.schemacompiler.ic;

import java.util.Arrays;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModelElement;

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
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
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
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#getEventClass()
	 */
	@Override
	public Class<?> getEventClass() {
		return OwnershipEvent.class;
	}

	/**
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#getSourceObjectClass()
	 */
	@Override
	public Class<S> getSourceObjectClass() {
		return eventSourceType;
	}
	
}

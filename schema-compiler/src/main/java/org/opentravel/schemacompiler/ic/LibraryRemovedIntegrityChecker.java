/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.ic;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLModel;

/**
 * Integrity checker component that automatically releases references to the members of a library
 * when it is removed from the model.
 *
 * @author S. Livezey
 */
public class LibraryRemovedIntegrityChecker extends EntityRemovedIntegrityChecker<TLModel,AbstractLibrary> {
	
	/**
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
	 */
	@Override
	public void processModelEvent(OwnershipEvent<TLModel, AbstractLibrary> event) {
		if (event.getType() == ModelEventType.LIBRARY_REMOVED) {
			purgeEntitiesFromModel( event.getAffectedItem(), event.getSource() );
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
	public Class<TLModel> getSourceObjectClass() {
		return TLModel.class;
	}
	
}

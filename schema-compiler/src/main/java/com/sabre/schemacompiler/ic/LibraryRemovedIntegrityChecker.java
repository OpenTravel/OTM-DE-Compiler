/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.ic;

import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.event.OwnershipEvent;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLModel;

/**
 * Integrity checker component that automatically releases references to the members of a library
 * when it is removed from the model.
 *
 * @author S. Livezey
 */
public class LibraryRemovedIntegrityChecker extends EntityRemovedIntegrityChecker<TLModel,AbstractLibrary> {
	
	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#processModelEvent(com.sabre.schemacompiler.event.ModelEvent)
	 */
	@Override
	public void processModelEvent(OwnershipEvent<TLModel, AbstractLibrary> event) {
		if (event.getType() == ModelEventType.LIBRARY_REMOVED) {
			purgeEntitiesFromModel( event.getAffectedItem(), event.getSource() );
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
	public Class<TLModel> getSourceObjectClass() {
		return TLModel.class;
	}
	
}

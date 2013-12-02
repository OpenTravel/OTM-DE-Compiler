/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.ic;

import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.event.OwnershipEvent;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLModelElement;

/**
 * Integrity checker component that automatically manages the list of imports and includes maintained
 * by a <code>TLLibrary</code> instance.  It is invoked whenever attributes or properties are added or
 * removed from a facet, taking action to add or remove imports/includes as required.
 * 
 * @author S. Livezey
 */
public class FacetMemberChangeIntegrityChecker extends ImportManagementIntegrityChecker<OwnershipEvent<TLModelElement,NamedEntity>,TLModelElement> {
	
	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#processModelEvent(com.sabre.schemacompiler.event.ModelEvent)
	 */
	@Override
	public void processModelEvent(OwnershipEvent<TLModelElement,NamedEntity> event) {
		if ((event.getType() == ModelEventType.ATTRIBUTE_ADDED) || (event.getType() == ModelEventType.PROPERTY_ADDED)
				|| (event.getType() == ModelEventType.ATTRIBUTE_REMOVED) || (event.getType() == ModelEventType.PROPERTY_REMOVED)) {
			AbstractLibrary owningLibrary = getOwningLibrary(event.getSource());
			
			if (owningLibrary instanceof TLLibrary) {
				verifyReferencedLibraries((TLLibrary) owningLibrary);
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
	public Class<TLModelElement> getSourceObjectClass() {
		return TLModelElement.class;
	}
	
}

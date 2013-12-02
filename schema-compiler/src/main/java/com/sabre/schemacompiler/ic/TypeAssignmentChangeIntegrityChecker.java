/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.ic;

import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.event.ValueChangeEvent;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLModelElement;

/**
 * Integrity checker component that automatically manages the list of imports and includes maintained
 * by a <code>TLLibrary</code> instance.  It is invoked whenever type assignments are modified within the
 * model, taking action to add or remove imports/includes as required.
 * 
 * @author S. Livezey
 */
public class TypeAssignmentChangeIntegrityChecker extends ImportManagementIntegrityChecker<ValueChangeEvent<TLModelElement,NamedEntity>,TLModelElement> {
	
	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#processModelEvent(com.sabre.schemacompiler.event.ModelEvent)
	 */
	@Override
	public void processModelEvent(ValueChangeEvent<TLModelElement, NamedEntity> event) {
		if ((event.getType() == ModelEventType.TYPE_ASSIGNMENT_MODIFIED) ||
				(event.getType() == ModelEventType.EXTENDS_ENTITY_MODIFIED)) {
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
		return ValueChangeEvent.class;
	}

	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#getSourceObjectClass()
	 */
	@Override
	public Class<TLModelElement> getSourceObjectClass() {
		return TLModelElement.class;
	}
	
}

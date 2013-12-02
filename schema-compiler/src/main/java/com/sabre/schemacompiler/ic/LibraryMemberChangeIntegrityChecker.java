/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.ic;

import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.event.OwnershipEvent;
import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.TLLibrary;

/**
 * Integrity checker component that automatically manages the list of imports and includes maintained
 * by a <code>TLLibrary</code> instance.  It is invoked whenever attributes or properties are added or
 * removed from a facet, taking action to add or remove imports/includes as required.
 * 
 * @author S. Livezey
 */
public class LibraryMemberChangeIntegrityChecker extends ImportManagementIntegrityChecker<OwnershipEvent<TLLibrary,LibraryMember>,TLLibrary> {
	
	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#processModelEvent(com.sabre.schemacompiler.event.ModelEvent)
	 */
	@Override
	public void processModelEvent(OwnershipEvent<TLLibrary, LibraryMember> event) {
		if ((event.getType() == ModelEventType.MEMBER_ADDED) || (event.getType() == ModelEventType.MEMBER_REMOVED)) {
			verifyReferencedLibraries(event.getSource());
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
	public Class<TLLibrary> getSourceObjectClass() {
		return TLLibrary.class;
	}
	
}

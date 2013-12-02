/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.ic;

import java.util.HashSet;
import java.util.Set;

import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.event.OwnershipEvent;
import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.TLContextReferrer;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.visitor.ModelNavigator;

/**
 * Integrity checker component that automatically searches for new contexts that need to be created
 * when a library member is added to the model.
 * 
 * @author S. Livezey
 */
public class ContextLibraryMemberAddedIntegrityChecker extends ContextAutoCreateIntegrityChecker<OwnershipEvent<TLLibrary,LibraryMember>,TLLibrary> {
	
	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#processModelEvent(com.sabre.schemacompiler.event.ModelEvent)
	 */
	@Override
	public void processModelEvent(OwnershipEvent<TLLibrary, LibraryMember> event) {
		if ((event.getType() == ModelEventType.MEMBER_ADDED)) {
			ContextReferrerVisitor visitor = new ContextReferrerVisitor(null);
			Set<String> visitedContextIds = new HashSet<String>();
			
			ModelNavigator.navigate(event.getAffectedItem(), visitor);
			
			for (TLContextReferrer contextReferrer : visitor.getContextReferrers()) {
				String contextId = contextReferrer.getContext();
				
				if (!visitedContextIds.contains(contextId)) {
					autoCreateContextDeclaration(event.getSource(), contextId);
					visitedContextIds.add(contextId);
				}
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
	public Class<TLLibrary> getSourceObjectClass() {
		return TLLibrary.class;
	}
	
}

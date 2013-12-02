/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.ic;

import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.event.ValueChangeEvent;
import com.sabre.schemacompiler.model.TLContext;
import com.sabre.schemacompiler.model.TLContextReferrer;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.visitor.ModelNavigator;

/**
 * Integrity checker component that automatically reassigns the 'context' value of all
 * associated <code>ContextReferrers</code> when the 'contextId' of a <code>TLContext</code>
 * declaration is modified.
 * 
 * @author S. Livezey
 */
public class ContextDeclarationChangeIntegrityChecker extends AbstractIntegrityChecker<ValueChangeEvent<TLContext,String>,TLContext> {
	
	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#processModelEvent(com.sabre.schemacompiler.event.ModelEvent)
	 */
	@Override
	public void processModelEvent(ValueChangeEvent<TLContext,String> event) {
		if (event.getType() == ModelEventType.CONTEXT_MODIFIED) {
			TLLibrary affectedLibrary = event.getSource().getOwningLibrary();
			String oldContextId = event.getOldValue();
			
			if (oldContextId != null) {
				ContextReferrerVisitor visitor = new ContextReferrerVisitor(oldContextId);
				String newContextId = event.getNewValue();
				
				ModelNavigator.navigate(affectedLibrary, visitor);
				
				for (TLContextReferrer entity : visitor.getContextReferrers()) {
					entity.setContext(newContextId);
				}
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
	public Class<TLContext> getSourceObjectClass() {
		return TLContext.class;
	}
	
}

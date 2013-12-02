/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.ic;

import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.event.ValueChangeEvent;
import com.sabre.schemacompiler.model.TLContextReferrer;
import com.sabre.schemacompiler.model.TLModelElement;

/**
 * Integrity checker component that automatically creates a <code>TLContext</code> declaration when
 * a <code>ContextReferrer</code>'s 'context' values is modified and a matching context declaration does
 * not yet exist.
 *
 * @author S. Livezey
 */
public class ContextReferrerChangeIntegrityChecker extends ContextAutoCreateIntegrityChecker<ValueChangeEvent<TLModelElement,String>,TLModelElement> {
	
	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#processModelEvent(com.sabre.schemacompiler.event.ModelEvent)
	 */
	@Override
	public void processModelEvent(ValueChangeEvent<TLModelElement,String> event) {
		if ((event.getType() == ModelEventType.CONTEXT_MODIFIED) && (event.getSource() instanceof TLContextReferrer)) {
			autoCreateContextDeclaration( ((TLContextReferrer) event.getSource()).getOwningLibrary(), event.getNewValue() );
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

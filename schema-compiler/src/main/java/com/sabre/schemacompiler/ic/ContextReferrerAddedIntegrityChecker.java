/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.ic;

import java.util.Arrays;
import java.util.List;

import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.event.OwnershipEvent;
import com.sabre.schemacompiler.model.TLContextReferrer;
import com.sabre.schemacompiler.model.TLModelElement;

/**
 * Integrity checker component that automatically creates a <code>TLContext</code> declaration when
 * a <code>ContextReferrer</code> is added to a library member and a matching context declaration does
 * not yet exist.
 *
 * @author S. Livezey
 */
public class ContextReferrerAddedIntegrityChecker extends ContextAutoCreateIntegrityChecker<OwnershipEvent<TLModelElement,TLContextReferrer>,TLModelElement> {
	
	private static ModelEventType[] ELIGIBLE_EVENT_TYPES = {
		ModelEventType.CUSTOM_FACET_ADDED, ModelEventType.QUERY_FACET_ADDED,
		ModelEventType.EXAMPLE_ADDED, ModelEventType.EQUIVALENT_ADDED,
		ModelEventType.DOC_OTHER_DOCS_ADDED
	};
	
	private List<ModelEventType> eligibleEvents = Arrays.asList(ELIGIBLE_EVENT_TYPES);
	
	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#processModelEvent(com.sabre.schemacompiler.event.ModelEvent)
	 */
	@Override
	public void processModelEvent(OwnershipEvent<TLModelElement, TLContextReferrer> event) {
		if (eligibleEvents.contains(event.getType()) && (event.getAffectedItem() instanceof TLContextReferrer)) {
			autoCreateContextDeclaration( getOwningLibrary(event.getSource()), ((TLContextReferrer) event.getAffectedItem()).getContext() );
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

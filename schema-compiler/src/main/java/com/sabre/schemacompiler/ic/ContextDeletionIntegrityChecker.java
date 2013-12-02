/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.ic;

import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.event.OwnershipEvent;
import com.sabre.schemacompiler.model.TLAdditionalDocumentationItem;
import com.sabre.schemacompiler.model.TLContext;
import com.sabre.schemacompiler.model.TLContextReferrer;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.visitor.ModelNavigator;

/**
 * Integrity checker component that automatically deletes any associated <code>TLContextReferrer</code>
 * elements from a library's members when a <code>TLContext</code> declaration is deleted from the
 * library.  In the case of a contextual facet, the context will simply be set to null instead of
 * deleting the facet.
 * 
 * @author S. Livezey
 */
public class ContextDeletionIntegrityChecker extends AbstractIntegrityChecker<OwnershipEvent<TLLibrary,TLContext>,TLLibrary> {
	
	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#processModelEvent(com.sabre.schemacompiler.event.ModelEvent)
	 */
	@Override
	public void processModelEvent(OwnershipEvent<TLLibrary, TLContext> event) {
		if (event.getType() == ModelEventType.CONTEXT_REMOVED) {
			String contextId = event.getAffectedItem().getContextId();
			
			if ((contextId != null) && !hasDuplicateContext(event.getAffectedItem(), event.getSource())) {
				ContextReferrerVisitor visitor = new ContextReferrerVisitor(contextId);
				
				ModelNavigator.navigate(event.getSource(), visitor);
				
				for (TLContextReferrer entity : visitor.getContextReferrers()) {
					if (entity instanceof TLFacet) {
						entity.setContext(null);
						
					} else if (entity instanceof TLEquivalent) {
						TLEquivalent equivalent = (TLEquivalent) entity;
						equivalent.getOwningEntity().removeEquivalent(equivalent);
						
					} else if (entity instanceof TLExample) {
						TLExample example = (TLExample) entity;
						example.getOwningEntity().removeExample(example);
						
					} else if (entity instanceof TLAdditionalDocumentationItem) {
						TLAdditionalDocumentationItem docItem = (TLAdditionalDocumentationItem) entity;
						docItem.getOwningDocumentation().removeOtherDoc(docItem);
					}
				}
			}
		}
	}
	
	/**
	 * Returns true if another context with the same ID as the deleted one still exists after
	 * the given one has been removed.
	 * 
	 * @param deletedContext  the context that was deleted from the library
	 * @param contextOwner  the library from which the context was deleted
	 * @return boolean
	 */
	private boolean hasDuplicateContext(TLContext deletedContext, TLLibrary contextOwner) {
		boolean result = false;
		
		if ((deletedContext != null) && (contextOwner != null)) {
			String deletedContextId = deletedContext.getContextId();
			
			if (deletedContextId != null) {
				for (TLContext context : contextOwner.getContexts()) {
					if (context == deletedContext) {
						continue;
					}
					if (deletedContextId.equals(context.getContextId())) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
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

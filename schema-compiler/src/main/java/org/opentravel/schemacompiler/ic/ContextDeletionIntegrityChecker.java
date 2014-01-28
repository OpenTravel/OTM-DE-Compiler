/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.ic;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLContextReferrer;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

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
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
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
	public Class<TLLibrary> getSourceObjectClass() {
		return TLLibrary.class;
	}
	
}

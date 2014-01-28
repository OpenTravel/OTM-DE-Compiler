
package org.opentravel.schemacompiler.ic;

import java.util.HashSet;
import java.util.Set;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLContextReferrer;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Integrity checker component that automatically searches for new contexts that need to be created
 * when a library member is added to the model.
 * 
 * @author S. Livezey
 */
public class ContextLibraryMemberAddedIntegrityChecker extends ContextAutoCreateIntegrityChecker<OwnershipEvent<TLLibrary,LibraryMember>,TLLibrary> {
	
	/**
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
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

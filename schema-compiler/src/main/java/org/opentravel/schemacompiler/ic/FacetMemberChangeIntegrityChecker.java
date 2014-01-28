
package org.opentravel.schemacompiler.ic;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * Integrity checker component that automatically manages the list of imports and includes maintained
 * by a <code>TLLibrary</code> instance.  It is invoked whenever attributes or properties are added or
 * removed from a facet, taking action to add or remove imports/includes as required.
 * 
 * @author S. Livezey
 */
public class FacetMemberChangeIntegrityChecker extends ImportManagementIntegrityChecker<OwnershipEvent<TLModelElement,NamedEntity>,TLModelElement> {
	
	/**
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
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
	public Class<TLModelElement> getSourceObjectClass() {
		return TLModelElement.class;
	}
	
}

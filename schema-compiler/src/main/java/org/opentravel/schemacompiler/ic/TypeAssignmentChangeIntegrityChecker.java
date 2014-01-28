
package org.opentravel.schemacompiler.ic;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * Integrity checker component that automatically manages the list of imports and includes maintained
 * by a <code>TLLibrary</code> instance.  It is invoked whenever type assignments are modified within the
 * model, taking action to add or remove imports/includes as required.
 * 
 * @author S. Livezey
 */
public class TypeAssignmentChangeIntegrityChecker extends ImportManagementIntegrityChecker<ValueChangeEvent<TLModelElement,NamedEntity>,TLModelElement> {
	
	/**
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
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
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#getEventClass()
	 */
	@Override
	public Class<?> getEventClass() {
		return ValueChangeEvent.class;
	}

	/**
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#getSourceObjectClass()
	 */
	@Override
	public Class<TLModelElement> getSourceObjectClass() {
		return TLModelElement.class;
	}
	
}

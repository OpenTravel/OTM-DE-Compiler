package org.opentravel.schemacompiler.ic;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Integrity checker component that automatically manages the list of imports and includes
 * maintained by a <code>TLLibrary</code> instance. It is invoked whenever attributes or properties
 * are added or removed from a facet, taking action to add or remove imports/includes as required.
 * 
 * @author S. Livezey
 */
public class LibraryMemberChangeIntegrityChecker extends
        ImportManagementIntegrityChecker<OwnershipEvent<TLLibrary, LibraryMember>, TLLibrary> {

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
     */
    @Override
    public void processModelEvent(OwnershipEvent<TLLibrary, LibraryMember> event) {
        if ((event.getType() == ModelEventType.MEMBER_ADDED)
                || (event.getType() == ModelEventType.MEMBER_REMOVED)) {
            verifyReferencedLibraries(event.getSource());
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

package org.opentravel.schemacompiler.ic;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelEventListener;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLService;

/**
 * Default model integrity checker instance that provides a standard set of listeners. If required,
 * this class may be extended to include additional routines required for application-specific
 * business rules.
 * 
 * @author S. Livezey
 */
public class ModelIntegrityChecker extends AbstractModelIntegrityChecker {

    /**
     * @see org.opentravel.schemacompiler.ic.AbstractModelIntegrityChecker#getListeners()
     */
    @Override
    protected List<ModelEventListener<?, ?>> getListeners() {
        List<ModelEventListener<?, ?>> listeners = new ArrayList<ModelEventListener<?, ?>>();

        listeners.add(new TypeAssignmentChangeIntegrityChecker());
        listeners.add(new FacetMemberChangeIntegrityChecker());
        listeners.add(new LibraryMemberChangeIntegrityChecker());
        listeners.add(new TypeNameIntegrityChecker());

        listeners.add(new NameChangeIntegrityChecker());
        listeners.add(new PrefixChangeIntegrityChecker());
        listeners.add(new NamespaceChangeIntegrityChecker());

        listeners.add(new LibraryRemovedIntegrityChecker());
        listeners.add(new NamedEntityRemovedIntegrityChecker<TLLibrary>(TLLibrary.class));
        listeners.add(new NamedEntityRemovedIntegrityChecker<TLBusinessObject>(
                TLBusinessObject.class));
        listeners.add(new NamedEntityRemovedIntegrityChecker<TLCoreObject>(TLCoreObject.class));
        listeners.add(new NamedEntityRemovedIntegrityChecker<TLService>(TLService.class));
        listeners.add(new NamedEntityRemovedIntegrityChecker<TLFacet>(TLFacet.class));
        listeners.add(new NamedEntityRemovedIntegrityChecker<TLListFacet>(TLListFacet.class));

        listeners.add(new ContextDeletionIntegrityChecker());
        listeners.add(new ContextDeclarationChangeIntegrityChecker());
        listeners.add(new ContextReferrerAddedIntegrityChecker());
        listeners.add(new ContextLibraryMemberAddedIntegrityChecker());
        listeners.add(new ContextReferrerChangeIntegrityChecker());

        return listeners;
    }

}

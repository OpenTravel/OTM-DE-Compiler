package org.opentravel.schemacompiler.ic;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.TLContextReferrer;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * Integrity checker component that automatically creates a <code>TLContext</code> declaration when
 * a <code>ContextReferrer</code>'s 'context' values is modified and a matching context declaration
 * does not yet exist.
 * 
 * @author S. Livezey
 */
public class ContextReferrerChangeIntegrityChecker extends
        ContextAutoCreateIntegrityChecker<ValueChangeEvent<TLModelElement, String>, TLModelElement> {

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
     */
    @Override
    public void processModelEvent(ValueChangeEvent<TLModelElement, String> event) {
        if ((event.getType() == ModelEventType.CONTEXT_MODIFIED)
                && (event.getSource() instanceof TLContextReferrer)) {
            autoCreateContextDeclaration(
                    ((TLContextReferrer) event.getSource()).getOwningLibrary(), event.getNewValue());
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

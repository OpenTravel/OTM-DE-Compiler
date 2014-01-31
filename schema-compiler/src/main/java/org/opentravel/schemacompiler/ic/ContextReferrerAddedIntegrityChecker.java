/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemacompiler.ic;

import java.util.Arrays;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.model.TLContextReferrer;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * Integrity checker component that automatically creates a <code>TLContext</code> declaration when
 * a <code>ContextReferrer</code> is added to a library member and a matching context declaration
 * does not yet exist.
 * 
 * @author S. Livezey
 */
public class ContextReferrerAddedIntegrityChecker
        extends
        ContextAutoCreateIntegrityChecker<OwnershipEvent<TLModelElement, TLContextReferrer>, TLModelElement> {

    private static ModelEventType[] ELIGIBLE_EVENT_TYPES = { ModelEventType.CUSTOM_FACET_ADDED,
            ModelEventType.QUERY_FACET_ADDED, ModelEventType.EXAMPLE_ADDED,
            ModelEventType.EQUIVALENT_ADDED, ModelEventType.DOC_OTHER_DOCS_ADDED };

    private List<ModelEventType> eligibleEvents = Arrays.asList(ELIGIBLE_EVENT_TYPES);

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
     */
    @Override
    public void processModelEvent(OwnershipEvent<TLModelElement, TLContextReferrer> event) {
        if (eligibleEvents.contains(event.getType())
                && (event.getAffectedItem() instanceof TLContextReferrer)) {
            autoCreateContextDeclaration(getOwningLibrary(event.getSource()),
                    ((TLContextReferrer) event.getAffectedItem()).getContext());
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

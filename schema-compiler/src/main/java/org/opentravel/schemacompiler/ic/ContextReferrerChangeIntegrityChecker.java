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

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.TLContextReferrer;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * Integrity checker component that automatically creates a <code>TLContext</code> declaration when a
 * <code>ContextReferrer</code>'s 'context' values is modified and a matching context declaration does not yet exist.
 * 
 * @author S. Livezey
 */
public class ContextReferrerChangeIntegrityChecker
    extends ContextAutoCreateIntegrityChecker<ValueChangeEvent<TLModelElement,String>,TLModelElement> {

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
     */
    @Override
    public void processModelEvent(ValueChangeEvent<TLModelElement,String> event) {
        if ((event.getType() == ModelEventType.CONTEXT_MODIFIED) && (event.getSource() instanceof TLContextReferrer)) {
            autoCreateContextDeclaration( ((TLContextReferrer) event.getSource()).getOwningLibrary(),
                event.getNewValue() );
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

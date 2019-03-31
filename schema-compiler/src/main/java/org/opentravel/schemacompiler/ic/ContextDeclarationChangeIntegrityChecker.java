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
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLContextReferrer;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Integrity checker component that automatically reassigns the 'context' value of all associated
 * <code>ContextReferrers</code> when the 'contextId' of a <code>TLContext</code> declaration is modified.
 * 
 * @author S. Livezey
 */
public class ContextDeclarationChangeIntegrityChecker
    extends AbstractIntegrityChecker<ValueChangeEvent<TLContext,String>,TLContext> {

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
     */
    @Override
    public void processModelEvent(ValueChangeEvent<TLContext,String> event) {
        if (event.getType() == ModelEventType.CONTEXT_MODIFIED) {
            TLLibrary affectedLibrary = event.getSource().getOwningLibrary();
            String oldContextId = event.getOldValue();

            if (oldContextId != null) {
                ContextReferrerVisitor visitor = new ContextReferrerVisitor( oldContextId );
                String newContextId = event.getNewValue();

                ModelNavigator.navigate( affectedLibrary, visitor );

                for (TLContextReferrer entity : visitor.getContextReferrers()) {
                    entity.setContext( newContextId );
                }
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
    public Class<TLContext> getSourceObjectClass() {
        return TLContext.class;
    }

}

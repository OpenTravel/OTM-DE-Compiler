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

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Integrity checker base class that provides support for the auto-creation of <code>TLContext</code> declarations.
 * 
 * @param <E> the event type that this listener is designed to process
 * @param <S> the source object type for the events to be processed by this listener
 * @author S. Livezey
 */
public abstract class ContextAutoCreateIntegrityChecker<E extends ModelEvent<S>, S>
    extends AbstractIntegrityChecker<E,S> {

    /**
     * Searches the context declarations in the given library and automatically creates one with the specified context
     * ID, if it does not already exist.
     * 
     * @param library the library that owns the context declaration(s)
     * @param contextId the context ID to search for
     */
    protected void autoCreateContextDeclaration(AbstractLibrary library, String contextId) {
        if ((contextId != null) && (library instanceof TLLibrary)) {
            TLLibrary contextLibrary = (TLLibrary) library;
            TLContext contextDeclaration = null;

            for (TLContext context : contextLibrary.getContexts()) {
                if (contextId.equals( context.getContextId() )) {
                    contextDeclaration = context;
                    break;
                }
            }

            // Create a new context declaration if one does not already exist
            if (contextDeclaration == null) {
                TLContext context = new TLContext();

                context.setContextId( contextId );
                context.setApplicationContext( contextId );
                contextLibrary.addContext( context );
            }
        }
    }

}

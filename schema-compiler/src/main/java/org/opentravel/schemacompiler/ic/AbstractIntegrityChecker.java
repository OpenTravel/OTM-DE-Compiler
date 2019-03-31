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
import org.opentravel.schemacompiler.event.ModelEventListener;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * Base integrity-checker class that provides common methods used by most integrity-check routines.
 * 
 * @param <E> the event type that this listener is designed to process
 * @param <S> the source object type for the events to be processed by this listener
 * @author S. Livezey
 */
public abstract class AbstractIntegrityChecker<E extends ModelEvent<S>, S> implements ModelEventListener<E,S> {

    /**
     * Returns the library that owns the given model element, regardless of whether the ownership is direct or indirect.
     * 
     * @param entity the entity for which to return the owning library
     * @return AbstractLibrary
     */
    protected AbstractLibrary getOwningLibrary(TLModelElement entity) {
        AbstractLibrary owningLibrary = null;

        if (entity instanceof LibraryElement) {
            owningLibrary = ((LibraryElement) entity).getOwningLibrary();
        }
        return owningLibrary;
    }

}

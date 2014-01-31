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

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventListener;

/**
 * Event listener that encapsulates a number of nested listeners responsible for maintaining the
 * integrity of a <code>TLModel</code> as changes are performed by an editor or owning applicaiton.
 * 
 * @author S. Livezey
 */
public abstract class AbstractModelIntegrityChecker implements
        ModelEventListener<ModelEvent<Object>, Object> {

    private List<ModelEventListener<?, ?>> listeners;

    /**
     * Default constructor.
     */
    public AbstractModelIntegrityChecker() {
        listeners = getListeners();
    }

    /**
     * Returns the collection of listeners that should be invoked to ensure the integrity of a
     * <code>TLModel</code> instance. By default, this method returns an empty list. Sub-classes
     * should override and add required listeners to the list.
     * 
     * @return ModelEventListener<?,?>
     */
    protected List<ModelEventListener<?, ?>> getListeners() {
        return new ArrayList<ModelEventListener<?, ?>>();
    }

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void processModelEvent(ModelEvent<Object> event) {
        for (ModelEventListener<?, ?> listener : listeners) {
            if (event.canBeProcessedBy(listener)) {
                ((ModelEventListener<ModelEvent<Object>, ?>) listener).processModelEvent(event);
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#getEventClass()
     */
    @Override
    public Class<ModelEvent<Object>> getEventClass() {
        return null;
    }

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#getSourceObjectClass()
     */
    @Override
    public Class<Object> getSourceObjectClass() {
        return null;
    }

}

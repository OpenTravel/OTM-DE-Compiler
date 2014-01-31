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
package org.opentravel.schemacompiler.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventListener;

/**
 * Model event listener that captures events for later analysis and assertions by test cases.
 * 
 * @author S. Livezey
 */
public class CapturingEventListener<E extends ModelEvent<S>, S> implements ModelEventListener<E, S> {

    private List<E> capturedEvents = new ArrayList<E>();
    private Class<E> eventClass;
    private Class<S> sourceObjectClass;

    /**
     * Constructor that specifies the run-time types used to identify the event and source object
     * types to which the listener should respond.
     * 
     * @param eventClass
     *            the type of the events to be processed by this listener
     * @param sourceObjectClass
     *            the type of source objects to be processed by this listener
     */
    @SuppressWarnings("unchecked")
    public CapturingEventListener(Class<?> eventClass, Class<?> sourceObjectClass) {
        this.eventClass = (Class<E>) eventClass;
        this.sourceObjectClass = (Class<S>) sourceObjectClass;
    }

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
     */
    @Override
    public void processModelEvent(E event) {
        capturedEvents.add((E) event);
    }

    /**
     * Returns the events that have been captured by this listener.
     * 
     * @return List<E>
     */
    public List<E> getCapturedEvents() {
        return Collections.unmodifiableList(capturedEvents);
    }

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#getEventClass()
     */
    @Override
    public Class<E> getEventClass() {
        return eventClass;
    }

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#getSourceObjectClass()
     */
    @Override
    public Class<S> getSourceObjectClass() {
        return sourceObjectClass;
    }

}

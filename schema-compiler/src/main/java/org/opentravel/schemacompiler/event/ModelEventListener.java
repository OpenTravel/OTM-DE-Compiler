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

/**
 * Interface to be implemented by components that will respond to <code>ModelEvent</code> occurrances.
 * 
 * @param <E> the event type that this listener is designed to process
 * @param <S> the source object type for the events to be processed by this listener
 * @author S. Livezey
 */
public interface ModelEventListener<E extends ModelEvent<S>, S> {

    /**
     * Called by the <code>TLModel</code> when a change has been detected in the meta-model composition or hierarchy.
     * 
     * @param event the event that was broadcast in response to a change in the meta-model
     */
    public void processModelEvent(E event);

    /**
     * Returns the type of the event to be processed by this listener.
     * 
     * @return Class&lt;?&gt;
     */
    public Class<?> getEventClass();

    /**
     * Returns the type of the source objects for the events to be processed by this listener.
     * 
     * @return Class&lt;S&gt;
     */
    public Class<S> getSourceObjectClass();

}

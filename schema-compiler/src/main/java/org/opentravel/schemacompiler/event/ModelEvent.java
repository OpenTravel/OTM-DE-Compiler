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
 * Abstract base class for all events that can be broadcast in response to changes in a <code>TLModel</code> instance.
 * 
 * @param <S> the source object type for the event
 * @author S. Livezey
 */
public class ModelEvent<S> {

    private ModelEventType type;
    private S source;

    /**
     * Constructor that specifies the event type and source object for the event.
     * 
     * @param type the type of the event being broadcast
     * @param source the source object that was modified to create the event
     */
    public ModelEvent(ModelEventType type, S source) {
        if ((type == null) || (source == null)) {
            throw new NullPointerException( "The event type and source object values cannot be null." );
        }
        this.type = type;
        this.source = source;
    }

    /**
     * Returns the value of the 'type' field.
     * 
     * @return ModelEventType
     */
    public ModelEventType getType() {
        return type;
    }

    /**
     * Returns the value of the 'source' field.
     * 
     * @return S
     */
    public S getSource() {
        return source;
    }

    /**
     * Returns true if the given listener is capable of processing this event, based on the event-type and source object
     * type.
     * 
     * @param listener the listener to check
     * @return boolean
     */
    public boolean canBeProcessedBy(ModelEventListener<?,?> listener) {
        boolean result = false;

        if (listener != null) {
            Class<?> eventType = listener.getEventClass();
            Class<?> sourceObjectType = listener.getSourceObjectClass();

            result = ((eventType == null) || eventType.isAssignableFrom( this.getClass() ))
                && ((sourceObjectType == null) || sourceObjectType.isAssignableFrom( source.getClass() ));
        }
        return result;
    }

}

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
 * Event used to broadcast events associated with the change in field value for a model entity. The event's event-type
 * will indicate which field of the source entity was modified.
 * 
 * @param <S> the source object type for the event
 * @param <V> the type of the field that was modified on the model entity
 * @author S. Livezey
 */
public class ValueChangeEvent<S, V> extends ModelEvent<S> {

    private V oldValue;
    private V newValue;

    /**
     * Constructor that specifies the event type and source object for the event.
     * 
     * @param type the type of the event being broadcast
     * @param source the source object that was modified to create the event
     */
    public ValueChangeEvent(ModelEventType type, S source) {
        super( type, source );
    }

    /**
     * Returns the original field value that existed prior to the value change event.
     * 
     * @return V
     */
    public V getOldValue() {
        return oldValue;
    }

    /**
     * Assigns the original field value that existed prior to the value change event.
     * 
     * @param oldValue the old field value to assign
     */
    public void setOldValue(V oldValue) {
        this.oldValue = oldValue;
    }

    /**
     * Returns the new field value that exists after the value change event.
     * 
     * @return V
     */
    public V getNewValue() {
        return newValue;
    }

    /**
     * Assigns the new field value that exists after the value change event.
     * 
     * @param newValue the new field value to assign
     */
    public void setNewValue(V newValue) {
        this.newValue = newValue;
    }

}

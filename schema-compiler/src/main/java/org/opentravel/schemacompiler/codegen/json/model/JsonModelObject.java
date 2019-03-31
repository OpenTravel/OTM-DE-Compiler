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

package org.opentravel.schemacompiler.codegen.json.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Interface that should be implemented by any JSON model object that has a direct JSON content representation.
 */
public interface JsonModelObject {

    /**
     * Returns the <code>JsonObject</code> representation of this type.
     * 
     * @return JsonObject
     */
    public JsonObject toJson();

    /**
     * Adds a property to a <code>JsonObject</code>.
     * 
     * @param json the JSON object to which the property will be added
     * @param propertyName the name of the property to add
     * @param propertyValue the value of the property to add
     */
    default void addProperty(JsonObject json, String propertyName, String propertyValue) {
        if (propertyValue != null) {
            json.addProperty( propertyName, propertyValue );
        }
    }

    /**
     * Adds a property to a <code>JsonObject</code>.
     * 
     * @param json the JSON object to which the property will be added
     * @param propertyName the name of the property to add
     * @param propertyValue the value of the property to add
     */
    default void addProperty(JsonObject json, String propertyName, Number propertyValue) {
        if (propertyValue != null) {
            json.addProperty( propertyName, propertyValue );
        }
    }

    /**
     * Adds a property to a <code>JsonObject</code>.
     * 
     * @param json the JSON object to which the property will be added
     * @param propertyName the name of the property to add
     * @param propertyValue the value of the property to add
     */
    default void addProperty(JsonObject json, String propertyName, Boolean propertyValue) {
        if (propertyValue != null) {
            json.addProperty( propertyName, propertyValue );
        }
    }

    /**
     * Adds an array property to a <code>JsonObject</code>.
     * 
     * @param json the JSON object to which the property will be added
     * @param propertyName the name of the property to add
     * @param propertyValues the list of array values for the property to add
     */
    default void addProperty(JsonObject json, String propertyName, List<String> propertyValues) {
        if ((propertyValues != null) && !propertyValues.isEmpty()) {
            JsonArray jsonArray = new JsonArray();

            for (String value : propertyValues) {
                jsonArray.add( value );
            }
            json.add( propertyName, jsonArray );
        }
    }

    /**
     * Adds a property to a <code>JsonObject</code>.
     * 
     * @param json the JSON object to which the property will be added
     * @param propertyName the name of the property to add
     * @param propertyValue the value of the property to add
     */
    default void addJsonProperty(JsonObject json, String propertyName, JsonModelObject propertyValue) {
        if (propertyValue != null) {
            json.add( propertyName, propertyValue.toJson() );
        }
    }

    /**
     * Adds an array property to a <code>JsonObject</code>.
     * 
     * @param json the JSON object to which the property will be added
     * @param propertyName the name of the property to add
     * @param propertyValues the list of array values for the property to add
     */
    default void addJsonProperty(JsonObject json, String propertyName, List<? extends JsonModelObject> propertyValues) {
        if ((propertyValues != null) && !propertyValues.isEmpty()) {
            JsonArray jsonArray = new JsonArray();

            for (JsonModelObject value : propertyValues) {
                jsonArray.add( value.toJson() );
            }
            json.add( propertyName, jsonArray );
        }
    }

    /**
     * Adds a series of named properties to a <code>JsonObject</code>.
     * 
     * @param json the JSON object to which the properties will be added
     * @param properties the list of properties to add
     */
    default void addJsonProperties(JsonObject json, List<? extends JsonNamedProperty> properties) {
        if ((properties != null) && !properties.isEmpty()) {
            for (JsonNamedProperty property : properties) {
                JsonModelObject propertyValue = property.getPropertyValue();

                if (propertyValue != null) {
                    json.add( property.getPropertyName(), propertyValue.toJson() );
                }
            }
        }
    }

}

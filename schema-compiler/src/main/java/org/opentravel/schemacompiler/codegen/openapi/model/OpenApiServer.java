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

package org.opentravel.schemacompiler.codegen.openapi.model;

import org.opentravel.schemacompiler.codegen.json.model.JsonModelObject;

import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Class that defines the meta-model for an OpenAPI Server object.
 */
public class OpenApiServer implements JsonModelObject {

    private String url;
    private String description;
    private Map<String,String> variables = new TreeMap<>();

    /**
     * Returns the value of the 'url' field.
     *
     * @return String
     */
    public String getUrl() {
        return url;
    }

    /**
     * Assigns the value of the 'url' field.
     *
     * @param url the field value to assign
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the value of the 'description' field.
     *
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Assigns the value of the 'description' field.
     *
     * @param description the field value to assign
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the value of the 'variables' field.
     *
     * @return Map<String,String>
     */
    public Map<String,String> getVariables() {
        return variables;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
     */
    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        JsonObject varsJson = new JsonObject();

        addProperty( json, "url", url );
        addProperty( json, "description", description );

        for (Entry<String,String> var : variables.entrySet()) {
            JsonObject defaultJson = new JsonObject();

            addProperty( defaultJson, "default", var.getValue() );
            varsJson.add( var.getKey(), defaultJson );
        }
        json.add( "variables", varsJson );
        return json;
    }

}

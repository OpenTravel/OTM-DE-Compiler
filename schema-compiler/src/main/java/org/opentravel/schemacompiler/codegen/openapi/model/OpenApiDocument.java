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
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that defines the meta-model for an OpenAPI document.
 */
public class OpenApiDocument implements JsonModelObject {

    public static final String OPENAPI_SPEC_V3 = "3.0";

    private String specVersion = OPENAPI_SPEC_V3;
    private List<JsonSchemaNamedReference> definitions = new ArrayList<>();

    /**
     * Returns the value of the 'specVersion' field.
     *
     * @return String
     */
    public String getSpecVersion() {
        return specVersion;
    }

    /**
     * Assigns the value of the 'specVersion' field.
     *
     * @param specVersion the field value to assign
     */
    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }

    /**
     * Returns the value of the 'definitions' field.
     *
     * @return List&lt;JsonSchemaNamedReference&gt;
     */
    public List<JsonSchemaNamedReference> getDefinitions() {
        return definitions;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        addProperty( json, "swagger", specVersion );

        if (!definitions.isEmpty()) {
            JsonObject defsJson = new JsonObject();

            addJsonProperties( defsJson, definitions );
            json.add( "definitions", defsJson );
        }

        return json;
    }

}

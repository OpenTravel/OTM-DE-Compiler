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

/**
 * Class that defines the OTM resource information for an OpenAPI document.
 */
public class OpenApiOtmResource implements JsonModelObject {

    private String namespace;
    private String localName;

    /**
     * Returns the value of the 'namespace' field.
     *
     * @return String
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Assigns the value of the 'namespace' field.
     *
     * @param namespace the field value to assign
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Returns the value of the 'localName' field.
     *
     * @return String
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Assigns the value of the 'localName' field.
     *
     * @param localName the field value to assign
     */
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        addProperty( json, "namespace", namespace );
        addProperty( json, "localName", localName );
        return json;
    }

}

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
import java.util.Arrays;
import java.util.List;

/**
 * Class that defines the meta-model for the components section of an OpenAPI specification document.
 */
public class OpenApiComponents implements JsonModelObject {

    private List<JsonSchemaNamedReference> schemas = new ArrayList<>();
    private List<JsonSchemaNamedReference> responses = new ArrayList<>();
    private List<JsonSchemaNamedReference> parameters = new ArrayList<>();
    private List<JsonSchemaNamedReference> examples = new ArrayList<>();
    private List<JsonSchemaNamedReference> requestBodies = new ArrayList<>();
    private List<JsonSchemaNamedReference> headers = new ArrayList<>();
    private List<JsonSchemaNamedReference> securitySchemes = new ArrayList<>();
    private List<JsonSchemaNamedReference> links = new ArrayList<>();
    private List<JsonSchemaNamedReference> callbacks = new ArrayList<>();

    /**
     * Returns true if no components of any type have been defined.
     * 
     * @return boolean
     */
    public boolean isEmpty() {
        List<List<?>> cLists = Arrays.asList( schemas, responses, parameters, examples, requestBodies, headers,
            securitySchemes, links, callbacks );
        boolean empty = true;

        for (List<?> cList : cLists) {
            empty &= cList.isEmpty();
        }
        return empty;
    }

    /**
     * Returns the value of the 'schemas' field.
     *
     * @return List<JsonSchemaNamedReference>
     */
    public List<JsonSchemaNamedReference> getSchemas() {
        return schemas;
    }

    /**
     * Returns the value of the 'responses' field.
     *
     * @return List<JsonSchemaNamedReference>
     */
    public List<JsonSchemaNamedReference> getResponses() {
        return responses;
    }

    /**
     * Returns the value of the 'parameters' field.
     *
     * @return List<JsonSchemaNamedReference>
     */
    public List<JsonSchemaNamedReference> getParameters() {
        return parameters;
    }

    /**
     * Returns the value of the 'examples' field.
     *
     * @return List<JsonSchemaNamedReference>
     */
    public List<JsonSchemaNamedReference> getExamples() {
        return examples;
    }

    /**
     * Returns the value of the 'requestBodies' field.
     *
     * @return List<JsonSchemaNamedReference>
     */
    public List<JsonSchemaNamedReference> getRequestBodies() {
        return requestBodies;
    }

    /**
     * Returns the value of the 'headers' field.
     *
     * @return List<JsonSchemaNamedReference>
     */
    public List<JsonSchemaNamedReference> getHeaders() {
        return headers;
    }

    /**
     * Returns the value of the 'securitySchemes' field.
     *
     * @return List<JsonSchemaNamedReference>
     */
    public List<JsonSchemaNamedReference> getSecuritySchemes() {
        return securitySchemes;
    }

    /**
     * Returns the value of the 'links' field.
     *
     * @return List<JsonSchemaNamedReference>
     */
    public List<JsonSchemaNamedReference> getLinks() {
        return links;
    }

    /**
     * Returns the value of the 'callbacks' field.
     *
     * @return List<JsonSchemaNamedReference>
     */
    public List<JsonSchemaNamedReference> getCallbacks() {
        return callbacks;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
     */
    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        addComponentDefinitions( "schemas", schemas, json );
        addComponentDefinitions( "responses", responses, json );
        addComponentDefinitions( "parameters", parameters, json );
        addComponentDefinitions( "examples", examples, json );
        addComponentDefinitions( "requestBodies", requestBodies, json );
        addComponentDefinitions( "headers", headers, json );
        addComponentDefinitions( "securitySchemes", securitySchemes, json );
        addComponentDefinitions( "links", links, json );
        addComponentDefinitions( "callbacks", callbacks, json );
        return json;
    }

    /**
     * Adds the given list of definition to the named section of the OpenAPI component definitions.
     * 
     * @param name the name of the component section to which the definitions will be added
     * @param definitions the list of definitions to add
     * @param componentsJson the components section to which the section will be added
     */
    private void addComponentDefinitions(String name, List<JsonSchemaNamedReference> definitions,
        JsonObject componentsJson) {
        if (!definitions.isEmpty()) {
            JsonObject defsJson = new JsonObject();

            addJsonProperties( defsJson, definitions );
            componentsJson.add( name, defsJson );
        }
    }

}

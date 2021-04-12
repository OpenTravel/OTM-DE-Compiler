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
    private List<OpenApiResponse> responses = new ArrayList<>();
    private List<OpenApiParameter> parameters = new ArrayList<>();
    private List<JsonSchemaNamedReference> examples = new ArrayList<>();
    private List<OpenApiHeader> headers = new ArrayList<>();
    private List<OpenApiSecurityScheme> securitySchemes = new ArrayList<>();

    /**
     * Returns true if no components of any type have been defined.
     * 
     * @return boolean
     */
    public boolean isEmpty() {
        List<List<?>> cLists = Arrays.asList( schemas, responses, parameters, examples, headers, securitySchemes );
        boolean empty = true;

        for (List<?> cList : cLists) {
            empty &= cList.isEmpty();
        }
        return empty;
    }

    /**
     * Returns the value of the 'schemas' field.
     *
     * @return List&lt;JsonSchemaNamedReference&gt;
     */
    public List<JsonSchemaNamedReference> getSchemas() {
        return schemas;
    }

    /**
     * Returns the value of the 'responses' field.
     *
     * @return List&lt;OpenApiResponse&gt;
     */
    public List<OpenApiResponse> getResponses() {
        return responses;
    }

    /**
     * Returns the value of the 'parameters' field.
     *
     * @return List&lt;OpenApiParameter&gt;
     */
    public List<OpenApiParameter> getParameters() {
        return parameters;
    }

    /**
     * Returns the value of the 'examples' field.
     *
     * @return List&lt;&gt;
     */
    public List<JsonSchemaNamedReference> getExamples() {
        return examples;
    }

    /**
     * Returns the value of the 'headers' field.
     *
     * @return List&lt;OpenApiHeader&gt;
     */
    public List<OpenApiHeader> getHeaders() {
        return headers;
    }

    /**
     * Returns the value of the 'securitySchemes' field.
     *
     * @return List&lt;OpenApiSecurityScheme&gt;
     */
    public List<OpenApiSecurityScheme> getSecuritySchemes() {
        return securitySchemes;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
     */
    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        if (!schemas.isEmpty()) {
            JsonObject defsJson = new JsonObject();

            addJsonProperties( defsJson, schemas );
            json.add( "schemas", defsJson );
        }
        if (!responses.isEmpty()) {
            JsonObject responsesJson = new JsonObject();

            responses.forEach( r -> responsesJson.add( r.getStatusCode() + "", r.toJson() ) );
            json.add( "responses", responsesJson );
        }
        if (!parameters.isEmpty()) {
            JsonObject paramsJson = new JsonObject();

            parameters.forEach( p -> paramsJson.add( p.getName(), p.toJson() ) );
            json.add( "parameters", paramsJson );
        }
        if (!examples.isEmpty()) {
            JsonObject examplesJson = new JsonObject();

            addJsonProperties( examplesJson, examples );
            json.add( "examples", examplesJson );
        }
        if (!headers.isEmpty()) {
            JsonObject headersJson = new JsonObject();

            headers.forEach( h -> headersJson.add( h.getName(), h.toJson() ) );
            json.add( "headers", headersJson );
        }
        if (!securitySchemes.isEmpty()) {
            JsonObject schemesJson = new JsonObject();

            securitySchemes.forEach( s -> schemesJson.add( s.getName(), s.toJson() ) );
            json.add( "securitySchemes", schemesJson );
        }
        return json;
    }

}

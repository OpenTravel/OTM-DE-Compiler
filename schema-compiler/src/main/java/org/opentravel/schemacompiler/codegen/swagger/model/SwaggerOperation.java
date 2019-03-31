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

package org.opentravel.schemacompiler.codegen.swagger.model;

import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;
import org.opentravel.schemacompiler.codegen.json.model.JsonContextualValue;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentationOwner;
import org.opentravel.schemacompiler.codegen.json.model.JsonModelObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class that defines the meta-model for a Swagger Operation object.
 */
public class SwaggerOperation implements JsonDocumentationOwner, JsonModelObject {

    private String summary;
    private JsonDocumentation documentation;
    private String operationId;
    private List<String> consumes = new ArrayList<>();
    private List<String> produces = new ArrayList<>();
    private List<SwaggerParameter> parameters = new ArrayList<>();
    private List<SwaggerParameter> globalParameters = new ArrayList<>();
    private List<SwaggerResponse> responses = new ArrayList<>();
    private List<String> schemes = new ArrayList<>();
    private boolean deprecated;

    /**
     * Returns the value of the 'summary' field.
     *
     * @return String
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Assigns the value of the 'summary' field.
     *
     * @param summary the field value to assign
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Returns the value of the 'documentation' field.
     *
     * @return JsonDocumentation
     */
    public JsonDocumentation getDocumentation() {
        return documentation;
    }

    /**
     * Assigns the value of the 'documentation' field.
     *
     * @param documentation the field value to assign
     */
    public void setDocumentation(JsonDocumentation documentation) {
        this.documentation = documentation;
    }

    /**
     * Returns the value of the 'operationId' field.
     *
     * @return String
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Assigns the value of the 'operationId' field.
     *
     * @param operationId the field value to assign
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * Returns the value of the 'consumes' field.
     *
     * @return List&lt;String&gt;
     */
    public List<String> getConsumes() {
        return consumes;
    }

    /**
     * Returns the value of the 'produces' field.
     *
     * @return List&lt;String&gt;
     */
    public List<String> getProduces() {
        return produces;
    }

    /**
     * Returns the value of the 'parameters' field.
     *
     * @return List&lt;SwaggerParameter&gt;
     */
    public List<SwaggerParameter> getParameters() {
        return parameters;
    }

    /**
     * Returns the value of the 'globalParameters' field.
     *
     * @return List&lt;SwaggerParameter&gt;
     */
    public List<SwaggerParameter> getGlobalParameters() {
        return globalParameters;
    }

    /**
     * Returns the value of the 'responses' field.
     *
     * @return List&lt;SwaggerResponse&gt;
     */
    public List<SwaggerResponse> getResponses() {
        return responses;
    }

    /**
     * Returns the value of the 'schemes' field.
     *
     * @return List&lt;String&gt;
     */
    public List<String> getSchemes() {
        return schemes;
    }

    /**
     * Returns the value of the 'deprecated' field.
     *
     * @return boolean
     */
    public boolean isDeprecated() {
        return deprecated;
    }

    /**
     * Assigns the value of the 'deprecated' field.
     *
     * @param deprecated the field value to assign
     */
    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonDocumentationOwner#getEquivalentItems()
     */
    @Override
    public List<JsonContextualValue> getEquivalentItems() {
        return Collections.emptyList();
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonDocumentationOwner#getExampleItems()
     */
    @Override
    public List<JsonContextualValue> getExampleItems() {
        return Collections.emptyList();
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        addProperty( json, "summary", summary );
        JsonSchemaCodegenUtils.createOtmAnnotations( json, this );
        addProperty( json, "operationId", operationId );
        addProperty( json, "consumes", consumes );
        addProperty( json, "produces", produces );

        if (!parameters.isEmpty()) {
            JsonArray jsonArray = new JsonArray();

            parameters.forEach( p -> jsonArray.add( p.toJson() ) );

            if (!globalParameters.isEmpty()) {
                for (SwaggerParameter param : globalParameters) {
                    JsonObject paramRef = new JsonObject();

                    paramRef.addProperty( "$ref", "#/parameters/" + param.getName() );
                    jsonArray.add( paramRef );
                }
            }
            json.add( "parameters", jsonArray );
        }
        if (!responses.isEmpty()) {
            JsonObject responsesJson = new JsonObject();

            for (SwaggerResponse response : responses) {
                if (response.isDefaultResponse()) {
                    responsesJson.add( "default", response.toJson() );
                } else {
                    responsesJson.add( response.getStatusCode() + "", response.toJson() );
                }
            }
            json.add( "responses", responsesJson );
        }

        addProperty( json, "schemes", schemes );

        return json;
    }

}

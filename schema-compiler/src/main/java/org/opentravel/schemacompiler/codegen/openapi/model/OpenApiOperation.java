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
 * Class that defines the meta-model for an OpenAPI Operation object.
 */
public class OpenApiOperation implements JsonDocumentationOwner, JsonModelObject {

    private String summary;
    private JsonDocumentation documentation;
    private String operationId;
    private List<OpenApiParameter> parameters = new ArrayList<>();
    private OpenApiRequestBody requestBody;
    private List<OpenApiResponse> responses = new ArrayList<>();
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
     * Returns the value of the 'parameters' field.
     *
     * @return List&lt;OpenApiParameter&gt;
     */
    public List<OpenApiParameter> getParameters() {
        return parameters;
    }

    /**
     * Returns the value of the 'requestBody' field.
     *
     * @return OpenApiRequestBody
     */
    public OpenApiRequestBody getRequestBody() {
        return requestBody;
    }

    /**
     * Assigns the value of the 'requestBody' field.
     *
     * @param requestBody the field value to assign
     */
    public void setRequestBody(OpenApiRequestBody requestBody) {
        this.requestBody = requestBody;
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

        if (!parameters.isEmpty()) {
            JsonArray jsonArray = new JsonArray();

            parameters.forEach( p -> jsonArray.add( p.toJson() ) );
            json.add( "parameters", jsonArray );
        }

        if (requestBody != null) {
            json.add( "requestBody", requestBody.toJson() );
        }
        if (!responses.isEmpty()) {
            JsonObject responsesJson = new JsonObject();

            for (OpenApiResponse response : responses) {
                if (response.isDefaultResponse()) {
                    responsesJson.add( "default", response.toJson() );
                } else {
                    responsesJson.add( response.getStatusCode() + "", response.toJson() );
                }
            }
            json.add( "responses", responsesJson );
        }


        return json;
    }

}

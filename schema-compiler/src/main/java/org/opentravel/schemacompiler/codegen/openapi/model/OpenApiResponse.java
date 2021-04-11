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
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerResponse;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class that defines the meta-model for an OpenAPI Response object.
 */
public class OpenApiResponse implements JsonDocumentationOwner, JsonModelObject {

    private boolean defaultResponse;
    private Integer statusCode;
    private List<OpenApiMediaType> content = new ArrayList<>();
    private List<OpenApiHeader> headers = new ArrayList<>();
    private List<JsonSchemaNamedReference> headerRefs = new ArrayList<>();
    private JsonDocumentation documentation;

    /**
     * Default constructor.
     */
    public OpenApiResponse() {}

    /**
     * Constructor that creates an OpenAPI response from the given Swagger response.
     * 
     * @param swaggerResponse the Swagger response instance
     */
    public OpenApiResponse(SwaggerResponse swaggerResponse) {
        this.defaultResponse = swaggerResponse.isDefaultResponse();
        this.statusCode = swaggerResponse.getStatusCode();
        this.documentation = swaggerResponse.getDocumentation();

        if (swaggerResponse.getSchema() != null) {
            OpenApiMediaType jsonContent = new OpenApiMediaType();

            jsonContent.setMediaType( "application/json" );
            jsonContent.setRequestType( swaggerResponse.getSchema() );
            content.add( jsonContent );
        }
        if (swaggerResponse.getXmlSchema() != null) {
            JsonSchemaReference xmlSchema = new JsonSchemaReference( swaggerResponse.getXmlSchema().getSchemaPath() );
            OpenApiMediaType xmlContent = new OpenApiMediaType();

            xmlContent.setMediaType( "application/xml" );
            xmlContent.setRequestType( xmlSchema );
            content.add( xmlContent );
        }
        swaggerResponse.getHeaders().forEach( h -> headers.add( new OpenApiHeader( h ) ) );
    }

    /**
     * Returns the value of the 'defaultResponse' field.
     *
     * @return boolean
     */
    public boolean isDefaultResponse() {
        return defaultResponse;
    }

    /**
     * Assigns the value of the 'defaultResponse' field.
     *
     * @param defaultResponse the field value to assign
     */
    public void setDefaultResponse(boolean defaultResponse) {
        this.defaultResponse = defaultResponse;
    }

    /**
     * Returns the value of the 'statusCode' field.
     *
     * @return Integer
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the value of the 'content' field.
     *
     * @return List&lt;OpenApiMediaType&gt;
     */
    public List<OpenApiMediaType> getContent() {
        return content;
    }

    /**
     * Assigns the value of the 'statusCode' field.
     *
     * @param statusCode the field value to assign
     */
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
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
     * Returns the value of the 'headerRefs' field.
     *
     * @return List&lt;JsonSchemaNamedReference&gt;
     */
    public List<JsonSchemaNamedReference> getHeaderRefs() {
        return headerRefs;
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
    @Override
    public JsonObject toJson() {
        JsonObject contentJson = new JsonObject();
        JsonObject json = new JsonObject();

        JsonSchemaCodegenUtils.createOtmAnnotations( json, this );

        if (json.get( "description" ) == null) {
            json.addProperty( "description", "" );
        }

        if (!headers.isEmpty() || !headerRefs.isEmpty()) {
            JsonObject headersJson = new JsonObject();

            for (OpenApiHeader header : headers) {
                addJsonProperty( headersJson, header.getName(), header );
            }
            for (JsonSchemaNamedReference headerRef : headerRefs) {
                headersJson.add( headerRef.getName(), headerRef.getSchema().toJson() );
            }
            json.add( "headers", headersJson );
        }

        if (!content.isEmpty()) {
            for (OpenApiMediaType mediaType : content) {
                contentJson.add( mediaType.getMediaType(), mediaType.toJson() );
            }
            json.add( "content", contentJson );
        }
        return json;
    }

}

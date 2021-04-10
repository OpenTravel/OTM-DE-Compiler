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

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that defines the meta-model for an OpenAPI request body.
 */
public class OpenApiRequestBody implements JsonModelObject, JsonDocumentationOwner {

    private List<OpenApiMediaType> content = new ArrayList<>();
    private String description;
    private boolean required;
    private JsonDocumentation documentation;
    private List<JsonContextualValue> equivalentItems = new ArrayList<>();
    private List<JsonContextualValue> exampleItems = new ArrayList<>();

    /**
     * Returns the value of the 'content' field.
     *
     * @return List<OpenApiMediaType>
     */
    public List<OpenApiMediaType> getContent() {
        return content;
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
     * Returns the value of the 'required' field.
     *
     * @return boolean
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Assigns the value of the 'required' field.
     *
     * @param required the field value to assign
     */
    public void setRequired(boolean required) {
        this.required = required;
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
     * Returns the list of equivalent item definitions for this schema.
     *
     * @return List&lt;JsonContextualValue&gt;
     */
    public List<JsonContextualValue> getEquivalentItems() {
        return equivalentItems;
    }

    /**
     * Returns the list of EXAMPLE value definitions for this schema.
     *
     * @return List&lt;JsonContextualValue&gt;
     */
    public List<JsonContextualValue> getExampleItems() {
        return exampleItems;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
     */
    @Override
    public JsonObject toJson() {
        JsonObject contentJson = new JsonObject();
        JsonObject json = new JsonObject();

        addProperty( json, "description", description );
        JsonSchemaCodegenUtils.createOtmAnnotations( json, this );

        for (OpenApiMediaType mediaType : content) {
            contentJson.add( mediaType.getMediaType(), mediaType.toJson() );
        }
        json.add( "content", contentJson );
        return json;
    }

}

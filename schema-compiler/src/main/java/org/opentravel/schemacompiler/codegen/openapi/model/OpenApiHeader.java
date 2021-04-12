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
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerHeader;

import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;

/**
 * Class that defines the meta-model for an OpenAPI Header object.
 */
public class OpenApiHeader implements JsonDocumentationOwner, JsonModelObject {

    private String name;
    private JsonDocumentation documentation;
    private boolean required;
    private JsonSchema type;

    /**
     * Default constructor.
     */
    public OpenApiHeader() {}

    /**
     * Constructor that creates an OpenAPI header from the given Swagger header.
     * 
     * @param swaggerHeader the Swagger header instance
     */
    public OpenApiHeader(SwaggerHeader swaggerHeader) {
        this.name = swaggerHeader.getName();
        this.documentation = swaggerHeader.getDocumentation();
        this.type = swaggerHeader.getType();
        this.required = false;
    }

    /**
     * Returns the value of the 'name' field.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Assigns the value of the 'name' field.
     *
     * @param name the field value to assign
     */
    public void setName(String name) {
        this.name = name;
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
     * Returns the value of the 'type' field.
     *
     * @return JsonSchema
     */
    public JsonSchema getType() {
        return type;
    }

    /**
     * Assigns the value of the 'type' field.
     *
     * @param type the field value to assign
     */
    public void setType(JsonSchema type) {
        this.type = type;
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

        JsonSchemaCodegenUtils.createOtmAnnotations( json, this );
        addProperty( json, "required", required );

        if (type != null) {
            json.add( "schema", type.toJson() );
        }
        return json;
    }

}

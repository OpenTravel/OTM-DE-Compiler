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
import org.opentravel.schemacompiler.codegen.json.model.JsonNamedProperty;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParameter;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that defines the meta-model for an OpenAPI Parameter object.
 */
public class OpenApiParameter implements JsonDocumentationOwner, JsonNamedProperty, JsonModelObject {

    private String name;
    private OpenApiParamType in;
    private JsonDocumentation documentation;
    private List<JsonContextualValue> equivalentItems = new ArrayList<>();
    private List<JsonContextualValue> exampleItems = new ArrayList<>();
    private boolean required;
    private JsonSchema type;

    /**
     * Default constructor.
     */
    public OpenApiParameter() {}

    /**
     * Constructor that creates an OpenAPI parameter from the given Swagger parameter.
     * 
     * @param swaggerParam the Swagger parameter instance
     */
    public OpenApiParameter(SwaggerParameter swaggerParam) {
        this.name = swaggerParam.getName();
        this.in = OpenApiParamType.fromSwaggerParamType( swaggerParam.getIn() );
        this.documentation = swaggerParam.getDocumentation();
        this.equivalentItems.addAll( swaggerParam.getEquivalentItems() );
        this.exampleItems.addAll( swaggerParam.getExampleItems() );
        this.required = swaggerParam.isRequired();
        this.type = swaggerParam.getType();
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
     * Returns the value of the 'in' field.
     *
     * @return OpenApiParamType
     */
    public OpenApiParamType getIn() {
        return in;
    }

    /**
     * Assigns the value of the 'in' field.
     *
     * @param in the field value to assign
     */
    public void setIn(OpenApiParamType in) {
        this.in = in;
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
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonNamedProperty#getPropertyName()
     */
    @Override
    public String getPropertyName() {
        return getName();
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonNamedProperty#getPropertyValue()
     */
    @Override
    public JsonModelObject getPropertyValue() {
        return this;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        addProperty( json, "name", name );

        if (in != null) {
            json.addProperty( "in", in.getInValue() );
        }

        JsonSchemaCodegenUtils.createOtmAnnotations( json, this );
        addProperty( json, "required", (in == OpenApiParamType.PATH) || required );

        if (type != null) {
            json.add( "schema", type.toJson() );
        }
        return json;
    }

}

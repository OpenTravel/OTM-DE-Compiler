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
import org.opentravel.schemacompiler.codegen.json.model.JsonNamedProperty;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Class that defines the meta-model for a Swagger Parameter object.
 */
public class SwaggerParameter implements JsonDocumentationOwner, JsonNamedProperty, JsonModelObject {

    private String name;
    private SwaggerParamType in;
    private JsonDocumentation documentation;
    private List<JsonContextualValue> equivalentItems = new ArrayList<>();
    private List<JsonContextualValue> exampleItems = new ArrayList<>();
    private boolean required;
    private JsonSchemaReference requestSchema;
    private SwaggerXmlSchemaRef requestXmlSchema;
    private JsonSchema type;

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
     * @return SwaggerParamType
     */
    public SwaggerParamType getIn() {
        return in;
    }

    /**
     * Assigns the value of the 'in' field.
     *
     * @param in the field value to assign
     */
    public void setIn(SwaggerParamType in) {
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
     * Returns the value of the 'requestSchema' field.
     *
     * @return JsonSchemaReference
     */
    public JsonSchemaReference getRequestSchema() {
        return requestSchema;
    }

    /**
     * Assigns the value of the 'requestSchema' field.
     *
     * @param requestSchema the field value to assign
     */
    public void setRequestSchema(JsonSchemaReference requestSchema) {
        this.requestSchema = requestSchema;
    }

    /**
     * Returns the value of the 'requestXmlSchema' field.
     *
     * @return SwaggerXmlSchemaRef
     */
    public SwaggerXmlSchemaRef getRequestXmlSchema() {
        return requestXmlSchema;
    }

    /**
     * Assigns the value of the 'requestXmlSchema' field.
     *
     * @param requestXmlSchema the field value to assign
     */
    public void setRequestXmlSchema(SwaggerXmlSchemaRef requestXmlSchema) {
        this.requestXmlSchema = requestXmlSchema;
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
        addProperty( json, "required", (in == SwaggerParamType.PATH) || required );

        if (in == SwaggerParamType.BODY) {
            addJsonProperty( json, "schema", requestSchema );
            addJsonProperty( json, "x-xml-schema", requestXmlSchema );

        } else if (type != null) {
            JsonObject typeSchema = type.toJson();

            for (Entry<String,JsonElement> typeEntry : typeSchema.entrySet()) {
                json.add( typeEntry.getKey(), typeEntry.getValue() );
            }
        }
        return json;
    }

}

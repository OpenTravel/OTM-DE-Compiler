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

package org.opentravel.schemacompiler.codegen.json.model;

import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that defines the meta-model for a JSON schema.
 */
public class JsonSchema implements JsonDocumentationOwner, JsonModelObject {

    public static final String JSON_SCHEMA_CURRENT = "http://json-schema.org/schema#";
    public static final String JSON_SCHEMA_DRAFT4 = "http://json-schema.org/draft-04/schema#";

    public static final JsonSchema EMPTY_SCHEMA = new JsonSchema();

    private String id;
    private String schemaSpec;
    private String title;
    private JsonLibraryInfo libraryInfo;
    private JsonEntityInfo entityInfo;
    private JsonDocumentation documentation;
    private List<JsonContextualValue> equivalentItems = new ArrayList<>();
    private List<JsonContextualValue> exampleItems = new ArrayList<>();
    private List<JsonSchemaNamedReference> definitions = new ArrayList<>();
    private List<JsonSchemaNamedReference> properties = new ArrayList<>();
    private List<JsonSchemaNamedReference> patternProperties = new ArrayList<>();
    private JsonSchemaReference additionalProperties;
    private String discriminator;
    private JsonType type;
    private Number multipleOf;
    private Number maximum;
    private Boolean exclusiveMaximum;
    private Number minimum;
    private Boolean exclusiveMinimum;
    private Integer maxLength;
    private Integer minLength;
    private String pattern;
    private List<String> enumValues = new ArrayList<>();
    private JsonSchemaReference items;
    private Integer maxItems;
    private Integer minItems;
    private Boolean uniqueItems;
    private JsonSchemaReference additionalItems;
    private List<JsonSchemaReference> allOf = new ArrayList<>();
    private List<JsonSchemaReference> anyOf = new ArrayList<>();
    private List<JsonSchemaReference> oneOf = new ArrayList<>();
    private JsonSchemaReference not;
    private List<JsonSchemaNamedReference> dependencies = new ArrayList<>();

    /**
     * Default constructor.
     */
    public JsonSchema() {}

    /**
     * Constructor that supplies the schema specification identifier.
     * 
     * @param schemaSpec the schema specification identifier
     */
    public JsonSchema(String schemaSpec) {
        this.schemaSpec = schemaSpec;
    }

    /**
     * Returns the ID of the schema.
     *
     * @return String
     */
    public String getId() {
        return id;
    }

    /**
     * Assigns the ID of the schema.
     *
     * @param id the schema ID to assign
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the URI constant that indicates the version of the JSON schema specification.
     *
     * @return String
     */
    public String getSchemaSpec() {
        return schemaSpec;
    }

    /**
     * Returns the title of the schema.
     *
     * @return String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Assigns the title of the schema.
     *
     * @param title the title to assign
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the library-info element for this schema.
     *
     * @return JsonLibraryInfo
     */
    public JsonLibraryInfo getLibraryInfo() {
        return libraryInfo;
    }

    /**
     * Assigns the library-info element for this schema.
     *
     * @param libraryInfo the library-info instance to assign
     */
    public void setLibraryInfo(JsonLibraryInfo libraryInfo) {
        this.libraryInfo = libraryInfo;
    }

    /**
     * Returns the entity-info element for this schema.
     *
     * @return JsonEntityInfo
     */
    public JsonEntityInfo getEntityInfo() {
        return entityInfo;
    }

    /**
     * Assigns the entity-info element for this schema.
     *
     * @param entityInfo the entity-info instance to assign
     */
    public void setEntityInfo(JsonEntityInfo entityInfo) {
        this.entityInfo = entityInfo;
    }

    /**
     * Returns the documentation for this schema.
     *
     * @return JsonDocumentation
     */
    public JsonDocumentation getDocumentation() {
        return documentation;
    }

    /**
     * Assigns the documentation for this schema.
     *
     * @param documentation the documentation item to assign
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
     * Returns the list of named definitions within this schema.
     *
     * @return List&lt;JsonSchemaNamedReference&gt;
     */
    public List<JsonSchemaNamedReference> getDefinitions() {
        return definitions;
    }

    /**
     * Returns the list of named properties for this schema.
     *
     * @return List&lt;JsonSchemaNamedReference&gt;
     */
    public List<JsonSchemaNamedReference> getProperties() {
        return properties;
    }

    /**
     * Returns the list of properties for this schema whose names are specified as regular expressions rather than
     * explicit property names.
     *
     * @return List&lt;JsonSchemaNamedReference&gt;
     */
    public List<JsonSchemaNamedReference> getPatternProperties() {
        return patternProperties;
    }

    /**
     * Returns the schema to be applied for any additional properties not explicitly defined in this schema. If null,
     * additional properties are not allowed.
     *
     * @return JsonSchemaReference
     */
    public JsonSchemaReference getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Assigns the schema to be applied for any additional properties not explicitly defined in this schema. If null,
     * additional properties are not allowed.
     *
     * @param additionalProperties the schema reference to assign
     */
    public void setAdditionalProperties(JsonSchemaReference additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
     * Returns the name of the property to be used to support polymorphism by specifying which sub-type schema
     * definition should be used to validate messages.
     *
     * @return String
     */
    public String getDiscriminator() {
        return discriminator;
    }

    /**
     * Assigns the name of the property to be used to support polymorphism by specifying which sub-type schema
     * definition should be used to validate messages.
     *
     * @param discriminator the discriminator property name to assign
     */
    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    /**
     * Returns the type assignment for this schema.
     *
     * @return JsonType
     */
    public JsonType getType() {
        return type;
    }

    /**
     * Assigns the type assignment for this schema.
     *
     * @param type the JSON type to assign
     */
    public void setType(JsonType type) {
        this.type = type;
    }

    /**
     * Returns the multiple-of restriction for numeric type values.
     *
     * @return Number
     */
    public Number getMultipleOf() {
        return multipleOf;
    }

    /**
     * Assigns the multiple-of restriction for numeric type values.
     *
     * @param multipleOf the multiple-of numeric value to assign
     */
    public void setMultipleOf(Number multipleOf) {
        this.multipleOf = multipleOf;
    }

    /**
     * Returns the maximum restriction for numeric type values.
     *
     * @return Number
     */
    public Number getMaximum() {
        return maximum;
    }

    /**
     * Assigns the maximum restriction for numeric type values.
     *
     * @param maximum the maximum numeric value to assign
     */
    public void setMaximum(Number maximum) {
        this.maximum = maximum;
    }

    /**
     * Returns the flag indicating whether the maximum restriction is inclusive or exclusive.
     *
     * @return Boolean
     */
    public Boolean getExclusiveMaximum() {
        return exclusiveMaximum;
    }

    /**
     * Assigns the flag indicating whether the maximum restriction is inclusive or exclusive.
     *
     * @param exclusiveMaximum the flag value to assign
     */
    public void setExclusiveMaximum(Boolean exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
    }

    /**
     * Returns the minimum restriction for numeric type values.
     *
     * @return Number
     */
    public Number getMinimum() {
        return minimum;
    }

    /**
     * Assigns the minimum restriction for numeric type values.
     *
     * @param minimum the minimum numeric value to assign
     */
    public void setMinimum(Number minimum) {
        this.minimum = minimum;
    }

    /**
     * Returns the flag indicating whether the minimum restriction is inclusive or exclusive.
     *
     * @return Boolean
     */
    public Boolean getExclusiveMinimum() {
        return exclusiveMinimum;
    }

    /**
     * Assigns the flag indicating whether the minimum restriction is inclusive or exclusive.
     *
     * @param exclusiveMinimum the flag value to assign
     */
    public void setExclusiveMinimum(Boolean exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
    }

    /**
     * Returns the maximum length restriction for string values.
     *
     * @return Integer
     */
    public Integer getMaxLength() {
        return maxLength;
    }

    /**
     * Assigns the maximum length restriction for string values.
     *
     * @param maxLength the maximum length value to assign
     */
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Returns the minimum length restriction for string values.
     *
     * @return Integer
     */
    public Integer getMinLength() {
        return minLength;
    }

    /**
     * Assigns the minimum length restriction for string values.
     *
     * @param minLength the minimum length value to assign
     */
    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    /**
     * Returns the regular expression restriction for string values.
     *
     * @return String
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Assigns the regular expression restriction for string values.
     *
     * @param pattern the regular expression pattern to assign
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Returns the list of allowable enumerations for string values.
     *
     * @return List&lt;String&gt;
     */
    public List<String> getEnumValues() {
        return enumValues;
    }

    /**
     * Assigns the list of allowable enumerations for string values.
     *
     * @param enumValues the list of enumeration values to assign
     */
    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues;
    }

    /**
     * Returns the item type restriction for array element values.
     *
     * @return JsonSchemaReference
     */
    public JsonSchemaReference getItems() {
        return items;
    }

    /**
     * Assigns the item type restriction for array element values.
     *
     * @param items the schema reference to assign
     */
    public void setItems(JsonSchemaReference items) {
        this.items = items;
    }

    /**
     * Returns the maximum size of an array.
     *
     * @return Integer
     */
    public Integer getMaxItems() {
        return maxItems;
    }

    /**
     * Assigns the maximum size of an array.
     *
     * @param maxItems the maximum value to assign
     */
    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }

    /**
     * Returns the minimum size of an array.
     *
     * @return Integer
     */
    public Integer getMinItems() {
        return minItems;
    }

    /**
     * Assigns the minimum size of an array.
     *
     * @param minItems the minimum value to assign
     */
    public void setMinItems(Integer minItems) {
        this.minItems = minItems;
    }

    /**
     * Returns the flag indicating whether array items must be unique values.
     *
     * @return Boolean
     */
    public Boolean getUniqueItems() {
        return uniqueItems;
    }

    /**
     * Assigns the flag indicating whether array items must be unique values.
     *
     * @param uniqueItems the flag value to assign
     */
    public void setUniqueItems(Boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }

    /**
     * Returns the schema for any items not explicitly allowed by this schema. If null, additional items are not
     * allowed.
     *
     * @return JsonSchemaReference
     */
    public JsonSchemaReference getAdditionalItems() {
        return additionalItems;
    }

    /**
     * Assigns the schema for any items not explicitly allowed by this schema. If null, additional items are not
     * allowed.
     *
     * @param additionalItems the schema reference to assign
     */
    public void setAdditionalItems(JsonSchemaReference additionalItems) {
        this.additionalItems = additionalItems;
    }

    /**
     * Returns a list of schemas, all of which must be validated for a JSON object to be considered valid.
     *
     * @return List&lt;JsonSchemaReference&gt;
     */
    public List<JsonSchemaReference> getAllOf() {
        return allOf;
    }

    /**
     * Returns a list of schemas, one or more of which must be validated for a JSON object to be considered valid.
     *
     * @return List&lt;JsonSchemaReference&gt;
     */
    public List<JsonSchemaReference> getAnyOf() {
        return anyOf;
    }

    /**
     * Returns a list of schemas, exactly one of which must be validated for a JSON object to be considered valid.
     *
     * @return List&lt;JsonSchemaReference&gt;
     */
    public List<JsonSchemaReference> getOneOf() {
        return oneOf;
    }

    /**
     * Returns a schemas, that must NOT validate in order for a JSON object to be considered valid.
     *
     * @return JsonSchemaReference
     */
    public JsonSchemaReference getNot() {
        return not;
    }

    /**
     * Assigns a schemas, that must NOT validate in order for a JSON object to be considered valid.
     *
     * @param not the 'not' schema to assign
     */
    public void setNot(JsonSchemaReference not) {
        this.not = not;
    }

    /**
     * Returns the list of dependencies for this schema.
     *
     * @return List&lt;JsonSchemaNamedReference&gt;
     */
    public List<JsonSchemaNamedReference> getDependencies() {
        return dependencies;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
     */
    public JsonObject toJson() {
        JsonObject jsonSchema = new JsonObject();

        addProperty( jsonSchema, "id", id );
        addProperty( jsonSchema, "$schema", schemaSpec );
        addProperty( jsonSchema, "title", title );
        addJsonProperty( jsonSchema, "x-otm-library", libraryInfo );
        addJsonProperty( jsonSchema, "x-otm-entity", entityInfo );
        JsonSchemaCodegenUtils.createOtmAnnotations( jsonSchema, this );

        if (!definitions.isEmpty()) {
            JsonObject schemaDefs = new JsonObject();

            addJsonProperties( schemaDefs, definitions );
            jsonSchema.add( "definitions", schemaDefs );
        }

        propertiesToJson( jsonSchema );
        addProperty( jsonSchema, "discriminator", discriminator );
        addJsonProperty( jsonSchema, "additionalProperties", additionalProperties );

        if (type != null) {
            jsonSchema.addProperty( "type", type.getSchemaType() );

            if (type.getFormat() != null) {
                jsonSchema.addProperty( "format", type.getFormat() );
            }
        }

        addProperty( jsonSchema, "multipleOf", multipleOf );
        addProperty( jsonSchema, "maximum", maximum );
        addProperty( jsonSchema, "exclusiveMaximum", exclusiveMaximum );
        addProperty( jsonSchema, "minimum", minimum );
        addProperty( jsonSchema, "exclusiveMinimum", exclusiveMinimum );
        addProperty( jsonSchema, "maxLength", maxLength );
        addProperty( jsonSchema, "minLength", minLength );
        addProperty( jsonSchema, "pattern", pattern );

        if (!enumValues.isEmpty()) {
            JsonArray enumArray = new JsonArray();

            for (String enumValue : enumValues) {
                enumArray.add( new JsonPrimitive( enumValue ) );
            }
            jsonSchema.add( "enum", enumArray );
        }

        addJsonProperty( jsonSchema, "items", items );
        addProperty( jsonSchema, "maxItems", maxItems );
        addProperty( jsonSchema, "minItems", minItems );
        addProperty( jsonSchema, "uniqueItems", uniqueItems );
        addJsonProperty( jsonSchema, "additionalItems", additionalItems );

        addJsonProperty( jsonSchema, "allOf", allOf );
        addJsonProperty( jsonSchema, "anyOf", anyOf );
        addJsonProperty( jsonSchema, "oneOf", oneOf );
        addJsonProperty( jsonSchema, "not", not );

        addJsonProperties( jsonSchema, dependencies );

        return jsonSchema;
    }

    /**
     * Provides the JSON schema content for all properties and pattern properties.
     * 
     * @param jsonSchema the JSON schema output being generated
     */
    private void propertiesToJson(JsonObject jsonSchema) {
        List<String> requiredProperties = new ArrayList<>();

        if (!properties.isEmpty()) {
            JsonObject schemaProps = new JsonObject();

            properties.forEach( p -> {
                if (p.isRequired()) {
                    requiredProperties.add( p.getName() );
                }
            } );
            addJsonProperties( schemaProps, properties );
            jsonSchema.add( "properties", schemaProps );
        }
        if (!patternProperties.isEmpty()) {
            JsonObject patternProps = new JsonObject();

            patternProperties.forEach( p -> {
                if (p.isRequired()) {
                    requiredProperties.add( p.getName() );
                }
            } );
            addJsonProperties( patternProps, patternProperties );
            jsonSchema.add( "patternProperties", patternProps );
        }
        if (!requiredProperties.isEmpty()) {
            JsonArray requiredArray = new JsonArray();

            for (String propertyName : requiredProperties) {
                requiredArray.add( propertyName );
            }
            jsonSchema.add( "required", requiredArray );
        }
    }

}

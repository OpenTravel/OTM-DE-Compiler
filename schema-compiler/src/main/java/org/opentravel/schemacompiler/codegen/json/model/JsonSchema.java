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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Class that defines the meta-model for a JSON schema.
 */
public class JsonSchema {
	
	public static final String JSON_SCHEMA_CURRENT = "http://json-schema.org/schema#";
	public static final String JSON_SCHEMA_DRAFT4  = "http://json-schema.org/draft-04/schema#";
	
	public static final JsonSchema EMPTY_SCHEMA = new JsonSchema();
	
	private String id;
	private String schemaSpec;
	private String title;
	private String description;
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
	 * Private constructor - use <code>newInstance()</code> factory methods for instantiation.
	 */
	private JsonSchema() {}
	
	/**
	 * Constructs a new JSON schema instance based on the latest (draft-4) specification
	 * version.
	 * 
	 * @return JsonSchema
	 */
	public static JsonSchema newInstance() {
		return newInstance( JSON_SCHEMA_DRAFT4 );
	}
	
	/**
	 * Constructs a new JSON schema instance based on the specification version provided.
	 * 
	 * @param schemaSpec  the schema specification identifier
	 * @return JsonSchema
	 */
	public static JsonSchema newInstance(String schemaSpec) {
		JsonSchema schema = new JsonSchema();
		
		schema.setSchemaSpec( schemaSpec );
		return schema;
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
	 * @param id  the schema ID to assign
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the URI constant that indicates the version of the JSON schema
	 * specification.
	 *
	 * @return String
	 */
	public String getSchemaSpec() {
		return schemaSpec;
	}

	/**
	 * Assigns the URI constant that indicates the version of the JSON schema
	 * specification.
	 *
	 * @param schema  the schema version URI to assign
	 */
	public void setSchemaSpec(String schema) {
		this.schemaSpec = schema;
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
	 * @param title  the title to assign
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Returns the description of the schema.
	 *
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Assigns the description of the schema.
	 *
	 * @param description  the description to assign
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the list of named definitions within this schema.
	 *
	 * @return List<JsonSchemaNamedReference>
	 */
	public List<JsonSchemaNamedReference> getDefinitions() {
		return definitions;
	}

	/**
	 * Returns the list of named properties for this schema.
	 *
	 * @return List<JsonSchemaNamedReference>
	 */
	public List<JsonSchemaNamedReference> getProperties() {
		return properties;
	}

	/**
	 * Returns the list of properties for this schema whose names are specified as
	 * regular expressions rather than explicit property names.
	 *
	 * @return List<JsonSchemaNamedReference>
	 */
	public List<JsonSchemaNamedReference> getPatternProperties() {
		return patternProperties;
	}

	/**
	 * Returns the schema to be applied for any additional properties not explicitly
	 * defined in this schema.  If null, additional properties are not allowed.
	 *
	 * @return JsonSchemaReference
	 */
	public JsonSchemaReference getAdditionalProperties() {
		return additionalProperties;
	}

	/**
	 * Assigns the schema to be applied for any additional properties not explicitly
	 * defined in this schema.  If null, additional properties are not allowed.
	 *
	 * @param additionalProperties  the schema reference to assign
	 */
	public void setAdditionalProperties(JsonSchemaReference additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

	/**
	 * Returns the name of the property to be used to support polymorphism by specifying
	 * which sub-type schema definition should be used to validate messages.
	 *
	 * @return String
	 */
	public String getDiscriminator() {
		return discriminator;
	}

	/**
	 * Assigns the name of the property to be used to support polymorphism by specifying
	 * which sub-type schema definition should be used to validate messages.
	 *
	 * @param discriminator  the discriminator property name to assign
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
	 * @param type  the JSON type to assign
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
	 * @param multipleOf  the multiple-of numeric value to assign
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
	 * @param maximum  the maximum numeric value to assign
	 */
	public void setMaximum(Number maximum) {
		this.maximum = maximum;
	}

	/**
	 * Returns the flag indicating whether the maximum restriction is inclusive or
	 * exclusive.
	 *
	 * @return Boolean
	 */
	public Boolean getExclusiveMaximum() {
		return exclusiveMaximum;
	}

	/**
	 * Assigns the flag indicating whether the maximum restriction is inclusive or
	 * exclusive.
	 *
	 * @param exclusiveMaximum  the flag value to assign
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
	 * @param minimum  the minimum numeric value to assign
	 */
	public void setMinimum(Number minimum) {
		this.minimum = minimum;
	}

	/**
	 * Returns the flag indicating whether the minimum restriction is inclusive or
	 * exclusive.
	 *
	 * @return Boolean
	 */
	public Boolean getExclusiveMinimum() {
		return exclusiveMinimum;
	}

	/**
	 * Assigns the flag indicating whether the minimum restriction is inclusive or
	 * exclusive.
	 *
	 * @param exclusiveMinimum  the flag value to assign
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
	 * @param maxLength  the maximum length value to assign
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
	 * @param minLength  the minimum length value to assign
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
	 * @param pattern  the regular expression pattern to assign
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * Returns the list of allowable enumerations for string values.
	 *
	 * @return List<String>
	 */
	public List<String> getEnumValues() {
		return enumValues;
	}

	/**
	 * Assigns the list of allowable enumerations for string values.
	 *
	 * @param enumValues  the list of enumeration values to assign
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
	 * @param items  the schema reference to assign
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
	 * @param maxItems  the maximum value to assign
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
	 * @param minItems  the minimum value to assign
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
	 * @param uniqueItems  the flag value to assign
	 */
	public void setUniqueItems(Boolean uniqueItems) {
		this.uniqueItems = uniqueItems;
	}

	/**
	 * Returns the schema for any items not explicitly allowed by this schema.  If
	 * null, additional items are not allowed.
	 *
	 * @return JsonSchemaReference
	 */
	public JsonSchemaReference getAdditionalItems() {
		return additionalItems;
	}

	/**
	 * Assigns the schema for any items not explicitly allowed by this schema.  If
	 * null, additional items are not allowed.
	 *
	 * @param additionalItems  the schema reference to assign
	 */
	public void setAdditionalItems(JsonSchemaReference additionalItems) {
		this.additionalItems = additionalItems;
	}

	/**
	 * Returns a list of schemas, all of which must be validated for a JSON object
	 * to be considered valid.
	 *
	 * @return List<JsonSchemaReference>
	 */
	public List<JsonSchemaReference> getAllOf() {
		return allOf;
	}

	/**
	 * Returns a list of schemas, one or more of which must be validated for a JSON object
	 * to be considered valid.
	 *
	 * @return List<JsonSchemaReference>
	 */
	public List<JsonSchemaReference> getAnyOf() {
		return anyOf;
	}

	/**
	 * Returns a list of schemas, exactly one of which must be validated for a JSON object
	 * to be considered valid.
	 *
	 * @return List<JsonSchemaReference>
	 */
	public List<JsonSchemaReference> getOneOf() {
		return oneOf;
	}

	/**
	 * Returns a schemas, that must NOT validate in order for a JSON object to be considered
	 * valid.
	 *
	 * @return JsonSchemaReference
	 */
	public JsonSchemaReference getNot() {
		return not;
	}

	/**
	 * Assigns a schemas, that must NOT validate in order for a JSON object to be considered
	 * valid.
	 *
	 * @param not  the 'not' schema to assign
	 */
	public void setNot(JsonSchemaReference not) {
		this.not = not;
	}
	
	/**
	 * Returns the list of dependencies for this schema.
	 *
	 * @return List<JsonSchemaNamedReference>
	 */
	public List<JsonSchemaNamedReference> getDependencies() {
		return dependencies;
	}

	/**
	 * Returns the <code>JsonObject</code> representation of this type.
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJson() {
		JsonObject jsonSchema = new JsonObject();
		
		if (id != null) {
			jsonSchema.addProperty( "id", id );
		}
		if (schemaSpec != null) {
			jsonSchema.addProperty( "$schema", schemaSpec );
		}
		if (title != null) {
			jsonSchema.addProperty( "title", title );
		}
		if (description != null) {
			jsonSchema.addProperty( "description", description );
		}
		if (!definitions.isEmpty()) {
			JsonObject schemaDefs = new JsonObject();
			
			for (JsonSchemaNamedReference definition : definitions) {
				schemaDefs.add( definition.getName(), definition.getSchema().toJson() );
			}
			jsonSchema.add( "definitions", schemaDefs );
		}
		if (!properties.isEmpty()) {
			JsonObject schemaProps = new JsonObject();
			
			for (JsonSchemaNamedReference property : properties) {
				schemaProps.add( property.getName(), property.getSchema().toJson() );
			}
			jsonSchema.add( "properties", schemaProps );
		}
		if (!patternProperties.isEmpty()) {
			JsonObject patternProps = new JsonObject();
			
			for (JsonSchemaNamedReference property : patternProperties) {
				patternProps.add( property.getName(), property.getSchema().toJson() );
			}
			jsonSchema.add( "patternProperties", patternProps );
		}
		if (additionalProperties != null) {
			jsonSchema.add( "additionalProperties", additionalProperties.toJson() );
		}
		if (type != null) {
			jsonSchema.addProperty( "type", type.getSchemaType() );
			
			if (type.getFormat() != null) {
				jsonSchema.addProperty( "format", type.getFormat() );
			}
		}
		if (multipleOf != null) {
			jsonSchema.addProperty( "multipleOf", multipleOf );
		}
		if (maximum != null) {
			jsonSchema.addProperty( "maximum", maximum );
		}
		if (exclusiveMaximum != null) {
			jsonSchema.addProperty( "exclusiveMaximum", exclusiveMaximum );
		}
		if (minimum != null) {
			jsonSchema.addProperty( "minimum", minimum );
		}
		if (exclusiveMinimum != null) {
			jsonSchema.addProperty( "exclusiveMinimum", exclusiveMinimum );
		}
		if (maxLength != null) {
			jsonSchema.addProperty( "maxLength", maxLength );
		}
		if (minLength != null) {
			jsonSchema.addProperty( "minLength", minLength );
		}
		if (pattern != null) {
			jsonSchema.addProperty( "pattern", pattern );
		}
		if (!enumValues.isEmpty()) {
			JsonArray enumArray = new JsonArray();
			
			for (String enumValue : enumValues) {
				enumArray.add( new JsonPrimitive( enumValue ) );
			}
			jsonSchema.add( "enum", enumArray );
		}
		if (items != null) {
			jsonSchema.add( "items", items.toJson() );
		}
		if (maxItems != null) {
			jsonSchema.addProperty( "maxItems", maxItems );
		}
		if (minItems != null) {
			jsonSchema.addProperty( "minItems", minItems );
		}
		if (uniqueItems != null) {
			jsonSchema.addProperty( "uniqueItems", uniqueItems );
		}
		if (additionalItems != null) {
			jsonSchema.add( "additionalItems", additionalItems.toJson() );
		}
		if (!allOf.isEmpty()) {
			JsonArray schemaAllOf = new JsonArray();
			
			for (JsonSchemaReference schemaRef : allOf) {
				schemaAllOf.add( schemaRef.getSchema().toJson() );
			}
			jsonSchema.add( "allOf", schemaAllOf );
		}
		if (!anyOf.isEmpty()) {
			JsonArray schemaAnyOf = new JsonArray();
			
			for (JsonSchemaReference schemaRef : anyOf) {
				schemaAnyOf.add( schemaRef.getSchema().toJson() );
			}
			jsonSchema.add( "anyOf", schemaAnyOf );
		}
		if (!oneOf.isEmpty()) {
			JsonArray schemaOneOf = new JsonArray();
			
			for (JsonSchemaReference schemaRef : oneOf) {
				schemaOneOf.add( schemaRef.getSchema().toJson() );
			}
			jsonSchema.add( "oneOf", schemaOneOf );
		}
		if (not != null) {
			jsonSchema.add( "not", not.toJson() );
		}
		if (!dependencies.isEmpty()) {
			JsonObject schemaDependencies = new JsonObject();
			
			for (JsonSchemaNamedReference dependency : dependencies) {
				schemaDependencies.add( dependency.getName(), dependency.getSchema().toJson() );
			}
			jsonSchema.add( "dependencies", schemaDependencies );
		}
		return jsonSchema;
	}
	
}

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

import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;

import com.google.gson.JsonObject;

/**
 * Encapsulates a reference to another JSON schema.  Reference may be either by-value
 * (included directly in the parent schema) or by-reference as a relative path.
 */
public class JsonSchemaReference  implements JsonDocumentationOwner, JsonModelObject {
	
	private JsonSchema schema;
	private String schemaPath;
	private JsonDocumentation schemaPathDocumentation;
	private List<JsonContextualValue> schemaPathEquivalentItems = new ArrayList<>();
	private List<JsonContextualValue> schemaPathExampleItems = new ArrayList<>();
	
	/**
	 * Default constructor.
	 */
	public JsonSchemaReference() {}
	
	/**
	 * Constructor that initializes this reference as a by-value schema.
	 * 
	 * @param schemaDef  the JSON schema definition to assign
	 */
	public JsonSchemaReference(JsonSchema schemaDef) {
		this.schema = schemaDef;
	}
	
	/**
	 * Constructor that initializes this reference as a by-reference schema
	 * assignments.
	 * 
	 * @param schemaPath  the schema path to assign
	 */
	public JsonSchemaReference(String schemaPath) {
		this.schemaPath = schemaPath;
	}
	
	/**
	 * Returns the schema definition for by-value schema assignments.
	 *
	 * @return JsonSchema
	 */
	public JsonSchema getSchema() {
		return schema;
	}
	
	/**
	 * Assigns the schema definition for by-value schema assignments.
	 *
	 * @param schemaDef  the JSON schema definition to assign
	 */
	public void setSchema(JsonSchema schemaDef) {
		this.schema = schemaDef;
		this.schemaPath = null;
	}

	/**
	 * Returns the schema path for by-reference schema assignments.
	 *
	 * @return String
	 */
	public String getSchemaPath() {
		return schemaPath;
	}

	/**
	 * Assigns the schema path for by-reference schema assignments.
	 *
	 * @param schemaRef  the schema path to assign
	 */
	public void setSchemaPath(String schemaRef) {
		this.schemaPath = schemaRef;
		this.schema = null;
	}
	
	/**
	 * Returns the documentation for the schema path.  If this schema reference
	 * contains a by-value schema (not a path reference), this documentation item
	 * will be ignored during marshalling.
	 *
	 * @return JsonDocumentation
	 */
	public JsonDocumentation getDocumentation() {
		return schemaPathDocumentation;
	}

	/**
	 * Assigns the documentation for the schema path.  If this schema reference
	 * contains a by-value schema (not a path reference), this documentation item
	 * will be ignored during marshalling.
	 *
	 * @param schemaPathDocumentation  the schema path documentation to assign
	 */
	public void setDocumentation(JsonDocumentation schemaPathDocumentation) {
		this.schemaPathDocumentation = schemaPathDocumentation;
	}

	/**
	 * Returns the list of equivalent item definitions for the schema path.  If this
	 * schema reference contains a by-value schema (not a path reference), these equivalent
	 * items will be ignored during marshalling.
	 *
	 * @return List<JsonContextualValue>
	 */
	public List<JsonContextualValue> getEquivalentItems() {
		return schemaPathEquivalentItems;
	}

	/**
	 * Returns the list of EXAMPLE value definitions for the schema path.  If this
	 * schema reference contains a by-value schema (not a path reference), these EXAMPLE
	 * items will be ignored during marshalling.
	 *
	 * @return List<JsonContextualValue>
	 */
	public List<JsonContextualValue> getExampleItems() {
		return schemaPathExampleItems;
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
	 */
	public JsonObject toJson() {
		JsonObject schemaRef;
		
		if (schema != null) {
			schemaRef = schema.toJson();
			
		} else {
			schemaRef = new JsonObject();
			JsonSchemaCodegenUtils.createOtmAnnotations( schemaRef, this );
			schemaRef.addProperty( "$ref", schemaPath );
		}
		return schemaRef;
	}
	
}

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

/**
 * Class used to represent a named reference to a JSON schema.
 */
public class JsonSchemaNamedReference {
	
	private String name;
	private JsonSchemaReference schema;
	
	/**
	 * Default constructor.
	 */
	public JsonSchemaNamedReference() {}
	
	/**
	 * Full constructor.
	 * 
	 * @param name  the name of the schema reference
	 * @param schema  the schema reference
	 */
	public JsonSchemaNamedReference(String name, JsonSchemaReference schema) {
		this.name = name;
		this.schema = schema;
	}
	
	/**
	 * Returns the name of the JSON type.
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Assigns the name of the JSON type.
	 *
	 * @param name  the field value to assign
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the schema reference.
	 *
	 * @return JsonSchemaReference
	 */
	public JsonSchemaReference getSchema() {
		return schema;
	}

	/**
	 * Assigns the schema reference.
	 *
	 * @param schema  the schema reference to assign
	 */
	public void setSchema(JsonSchemaReference schema) {
		this.schema = schema;
	}
	
}

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

import com.google.gson.JsonObject;

/**
 * Class that defines the meta-model for a non-standard XML schema reference
 * within a Swagger document.
 */
public class SwaggerXmlSchemaRef {
	
	private String schemaPath;

	/**
	 * Default constructor.
	 */
	public SwaggerXmlSchemaRef() {}
	
	/**
	 * Constructor that initializes the XML schema reference path.
	 * 
	 * @param schemaPath  the schema path to assign
	 */
	public SwaggerXmlSchemaRef(String schemaPath) {
		this.schemaPath = schemaPath;
	}
	
	/**
	 * Returns the value of the 'schemaPath' field.
	 *
	 * @return String
	 */
	public String getSchemaPath() {
		return schemaPath;
	}

	/**
	 * Assigns the value of the 'schemaPath' field.
	 *
	 * @param schemaPath  the field value to assign
	 */
	public void setSchemaPath(String schemaPath) {
		this.schemaPath = schemaPath;
	}
	
	/**
	 * Returns the <code>JsonObject</code> representation of this Swagger
	 * model element.
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		
		if (schemaPath != null) {
			json.addProperty( "$ref", schemaPath );
		}
		return json;
	}
	
}

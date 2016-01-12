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

import java.util.Map.Entry;

import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Class that defines the meta-model for a Swagger Parameter object.
 */
public class SwaggerParameter {
	
	private String name;
	private SwaggerParamType in;
	private String description;
	private boolean required;
	private JsonSchemaReference requestSchema;
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
	 * @param name  the field value to assign
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
	 * @param in  the field value to assign
	 */
	public void setIn(SwaggerParamType in) {
		this.in = in;
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
	 * @param description  the field value to assign
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
	 * @param required  the field value to assign
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
	 * @param requestSchema  the field value to assign
	 */
	public void setRequestSchema(JsonSchemaReference requestSchema) {
		this.requestSchema = requestSchema;
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
	 * @param type  the field value to assign
	 */
	public void setType(JsonSchema type) {
		this.type = type;
	}
	
	/**
	 * Returns the <code>JsonObject</code> representation of this Swagger
	 * model element.
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		
		if (name != null) {
			json.addProperty( "name", name );
		}
		if (in != null) {
			json.addProperty( "in", in.getInValue() );
		}
		if (description != null) {
			json.addProperty( "description", description );
		}
		if (required) {
			json.addProperty( "required", true );
		}
		if (in == SwaggerParamType.BODY) {
			if (requestSchema != null) {
				json.add( "schema", requestSchema.toJson() );
			}
		} else if (type != null) {
			JsonObject typeSchema = type.toJson();
			
			for (Entry<String,JsonElement> typeEntry : typeSchema.entrySet()) {
				json.add( typeEntry.getKey(), typeEntry.getValue() );
			}
		}
		return json;
	}

}

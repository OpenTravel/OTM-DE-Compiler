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

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;
import org.opentravel.schemacompiler.codegen.json.model.JsonContextualValue;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentationOwner;
import org.opentravel.schemacompiler.codegen.json.model.JsonModelObject;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Class that defines the meta-model for a Swagger Header object.
 */
public class SwaggerHeader implements JsonDocumentationOwner, JsonModelObject {
	
	private String name;
	private JsonDocumentation documentation;
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
	 * @param documentation  the field value to assign
	 */
	public void setDocumentation(JsonDocumentation documentation) {
		this.documentation = documentation;
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
		
		if (type != null) {
			JsonObject typeSchema = type.toJson();
			
			for (Entry<String,JsonElement> typeEntry : typeSchema.entrySet()) {
				json.add( typeEntry.getKey(), typeEntry.getValue() );
			}
		}
		return json;
	}

}

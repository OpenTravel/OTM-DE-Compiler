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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;
import org.opentravel.schemacompiler.codegen.json.model.JsonContextualValue;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentationOwner;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;

import com.google.gson.JsonObject;

/**
 * Class that defines the meta-model for a Swagger Response object.
 */
public class SwaggerResponse implements JsonDocumentationOwner {
	
	private boolean defaultResponse;
	private int statusCode;
	private JsonDocumentation documentation;
	private JsonSchemaReference schema;
	private List<SwaggerHeader> headers = new ArrayList<>();
	
	/**
	 * Returns the value of the 'defaultResponse' field.
	 *
	 * @return boolean
	 */
	public boolean isDefaultResponse() {
		return defaultResponse;
	}
	
	/**
	 * Assigns the value of the 'defaultResponse' field.
	 *
	 * @param defaultResponse  the field value to assign
	 */
	public void setDefaultResponse(boolean defaultResponse) {
		this.defaultResponse = defaultResponse;
	}
	
	/**
	 * Returns the value of the 'statusCode' field.
	 *
	 * @return int
	 */
	public int getStatusCode() {
		return statusCode;
	}
	
	/**
	 * Assigns the value of the 'statusCode' field.
	 *
	 * @param statusCode  the field value to assign
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
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
	 * Returns the value of the 'schema' field.
	 *
	 * @return JsonSchemaReference
	 */
	public JsonSchemaReference getSchema() {
		return schema;
	}
	
	/**
	 * Assigns the value of the 'schema' field.
	 *
	 * @param schema  the field value to assign
	 */
	public void setSchema(JsonSchemaReference schema) {
		this.schema = schema;
	}
	
	/**
	 * Returns the value of the 'headers' field.
	 *
	 * @return List<SwaggerHeader>
	 */
	public List<SwaggerHeader> getHeaders() {
		return headers;
	}
	
	/**
	 * Assigns the value of the 'headers' field.
	 *
	 * @param headers  the field value to assign
	 */
	public void setHeaders(List<SwaggerHeader> headers) {
		this.headers = headers;
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
	 * Returns the <code>JsonObject</code> representation of this Swagger
	 * model element.
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		
		JsonSchemaCodegenUtils.createOtmAnnotations( json, this );
		
		if (schema != null) {
			json.add( "schema", schema.toJson() );
		}
		if (!headers.isEmpty()) {
			JsonObject headersJson = new JsonObject();
			
			for (SwaggerHeader header : headers) {
				headersJson.add( header.getName(), header.toJson() );
			}
			json.add( "headers", headersJson );
		}
		return json;
	}
	
}

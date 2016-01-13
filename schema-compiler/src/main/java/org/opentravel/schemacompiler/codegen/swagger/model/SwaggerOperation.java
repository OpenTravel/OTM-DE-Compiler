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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Class that defines the meta-model for a Swagger Operation object.
 */
public class SwaggerOperation implements JsonDocumentationOwner {
	
	private String summary;
	private JsonDocumentation documentation;
	private String operationId;
	private List<String> consumes = new ArrayList<>();
	private List<String> produces = new ArrayList<>();
	private List<SwaggerParameter> parameters = new ArrayList<>();
	private List<SwaggerResponse> responses = new ArrayList<>();
	private List<String> schemes = new ArrayList<>();
	private boolean deprecated;
	
	/**
	 * Returns the value of the 'summary' field.
	 *
	 * @return String
	 */
	public String getSummary() {
		return summary;
	}
	
	/**
	 * Assigns the value of the 'summary' field.
	 *
	 * @param summary  the field value to assign
	 */
	public void setSummary(String summary) {
		this.summary = summary;
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
	 * Returns the value of the 'operationId' field.
	 *
	 * @return String
	 */
	public String getOperationId() {
		return operationId;
	}
	
	/**
	 * Assigns the value of the 'operationId' field.
	 *
	 * @param operationId  the field value to assign
	 */
	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}
	
	/**
	 * Returns the value of the 'consumes' field.
	 *
	 * @return List<String>
	 */
	public List<String> getConsumes() {
		return consumes;
	}
	
	/**
	 * Assigns the value of the 'consumes' field.
	 *
	 * @param consumes  the field value to assign
	 */
	public void setConsumes(List<String> consumes) {
		this.consumes = consumes;
	}
	
	/**
	 * Returns the value of the 'produces' field.
	 *
	 * @return List<String>
	 */
	public List<String> getProduces() {
		return produces;
	}
	
	/**
	 * Assigns the value of the 'produces' field.
	 *
	 * @param produces  the field value to assign
	 */
	public void setProduces(List<String> produces) {
		this.produces = produces;
	}
	
	/**
	 * Returns the value of the 'parameters' field.
	 *
	 * @return List<SwaggerParameter>
	 */
	public List<SwaggerParameter> getParameters() {
		return parameters;
	}
	
	/**
	 * Assigns the value of the 'parameters' field.
	 *
	 * @param parameters  the field value to assign
	 */
	public void setParameters(List<SwaggerParameter> parameters) {
		this.parameters = parameters;
	}
	
	/**
	 * Returns the value of the 'responses' field.
	 *
	 * @return List<SwaggerResponse>
	 */
	public List<SwaggerResponse> getResponses() {
		return responses;
	}
	
	/**
	 * Assigns the value of the 'responses' field.
	 *
	 * @param responses  the field value to assign
	 */
	public void setResponses(List<SwaggerResponse> responses) {
		this.responses = responses;
	}
	
	/**
	 * Returns the value of the 'schemes' field.
	 *
	 * @return List<String>
	 */
	public List<String> getSchemes() {
		return schemes;
	}
	
	/**
	 * Assigns the value of the 'schemes' field.
	 *
	 * @param schemes  the field value to assign
	 */
	public void setSchemes(List<String> schemes) {
		this.schemes = schemes;
	}
	
	/**
	 * Returns the value of the 'deprecated' field.
	 *
	 * @return boolean
	 */
	public boolean isDeprecated() {
		return deprecated;
	}
	
	/**
	 * Assigns the value of the 'deprecated' field.
	 *
	 * @param deprecated  the field value to assign
	 */
	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
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
		
		if (summary != null) {
			json.addProperty( "summary", summary );
		}
		
		JsonSchemaCodegenUtils.createOtmAnnotations( json, this );
		
		if (operationId != null) {
			json.addProperty( "operationId", operationId );
		}
		if (!consumes.isEmpty()) {
			JsonArray jsonArray = new JsonArray();
			
			for (String value : consumes) {
				jsonArray.add( value );
			}
			json.add( "consumes", jsonArray );
		}
		if (!produces.isEmpty()) {
			JsonArray jsonArray = new JsonArray();
			
			for (String value : produces) {
				jsonArray.add( value );
			}
			json.add( "produces", jsonArray );
		}
		if (!parameters.isEmpty()) {
			JsonArray jsonArray = new JsonArray();
			
			for (SwaggerParameter param : parameters) {
				jsonArray.add( param.toJson() );
			}
			json.add( "parameters", jsonArray );
		}
		if (!responses.isEmpty()) {
			JsonObject responsesJson = new JsonObject();
			
			for (SwaggerResponse response : responses) {
				if (response.isDefaultResponse()) {
					responsesJson.add( "default", response.toJson() );
				} else {
					responsesJson.add( response.getStatusCode() + "", response.toJson() );
				}
			}
			json.add( "responses", responsesJson );
		}
		if (!schemes.isEmpty()) {
			JsonArray jsonArray = new JsonArray();
			
			for (String value : schemes) {
				jsonArray.add( value );
			}
			json.add( "schemes", jsonArray );
		}
		return json;
	}

}

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
import java.util.List;

import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Class that defines the meta-model for a Swagger document.
 */
public class SwaggerDocument {
	
	public static final String SWAGGER_SPEC_V2 = "2.0";
	
	private String specVersion = SWAGGER_SPEC_V2;
	private SwaggerInfo info;
	private String host;
	private String basePath;
	private List<String> schemes = new ArrayList<>();
	private List<String> consumes = new ArrayList<>();
	private List<String> produces = new ArrayList<>();
	private List<SwaggerPathItem> pathItems = new ArrayList<>();
	private List<JsonSchemaNamedReference> definitions = new ArrayList<>();
	
	/**
	 * Returns the value of the 'specVersion' field.
	 *
	 * @return String
	 */
	public String getSpecVersion() {
		return specVersion;
	}
	
	/**
	 * Assigns the value of the 'specVersion' field.
	 *
	 * @param specVersion  the field value to assign
	 */
	public void setSpecVersion(String specVersion) {
		this.specVersion = specVersion;
	}
	
	/**
	 * Returns the value of the 'info' field.
	 *
	 * @return SwaggerInfo
	 */
	public SwaggerInfo getInfo() {
		return info;
	}
	
	/**
	 * Assigns the value of the 'info' field.
	 *
	 * @param info  the field value to assign
	 */
	public void setInfo(SwaggerInfo info) {
		this.info = info;
	}
	
	/**
	 * Returns the value of the 'host' field.
	 *
	 * @return String
	 */
	public String getHost() {
		return host;
	}
	
	/**
	 * Assigns the value of the 'host' field.
	 *
	 * @param host  the field value to assign
	 */
	public void setHost(String host) {
		this.host = host;
	}
	
	/**
	 * Returns the value of the 'basePath' field.
	 *
	 * @return String
	 */
	public String getBasePath() {
		return basePath;
	}
	
	/**
	 * Assigns the value of the 'basePath' field.
	 *
	 * @param basePath  the field value to assign
	 */
	public void setBasePath(String basePath) {
		this.basePath = basePath;
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
	 * Returns the value of the 'pathItems' field.
	 *
	 * @return List<SwaggerPathItem>
	 */
	public List<SwaggerPathItem> getPathItems() {
		return pathItems;
	}
	
	/**
	 * Assigns the value of the 'pathItems' field.
	 *
	 * @param pathItems  the field value to assign
	 */
	public void setPathItems(List<SwaggerPathItem> pathItems) {
		this.pathItems = pathItems;
	}
	
	/**
	 * Returns the value of the 'definitions' field.
	 *
	 * @return List<JsonSchemaNamedReference>
	 */
	public List<JsonSchemaNamedReference> getDefinitions() {
		return definitions;
	}
	
	/**
	 * Assigns the value of the 'definitions' field.
	 *
	 * @param definitions  the field value to assign
	 */
	public void setDefinitions(List<JsonSchemaNamedReference> definitions) {
		this.definitions = definitions;
	}
	
	/**
	 * Returns the value of the 'swaggerSpecV2' field.
	 *
	 * @return String
	 */
	public static String getSwaggerSpecV2() {
		return SWAGGER_SPEC_V2;
	}
	
	/**
	 * Returns the <code>JsonObject</code> representation of this Swagger
	 * model element.
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		
		if (specVersion != null) {
			json.addProperty( "swagger", specVersion );
		}
		if (info != null) {
			json.add( "info", info.toJson() );
		}
		if (host != null) {
			json.addProperty( "host", host );
		}
		if (basePath != null) {
			json.addProperty( "basePath", basePath );
		}
		if (!schemes.isEmpty()) {
			JsonArray jsonArray = new JsonArray();
			
			for (String value : schemes) {
				jsonArray.add( value );
			}
			json.add( "schemes", jsonArray );
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
		if (!pathItems.isEmpty()) {
			JsonObject pathsJson = new JsonObject();
			
			for (SwaggerPathItem pathItem : pathItems) {
				pathsJson.add( pathItem.getPathTemplate(), pathItem.toJson() );
			}
			json.add( "paths", pathsJson );
		}
		if (!definitions.isEmpty()) {
			JsonObject defsJson = new JsonObject();
			
			for (JsonSchemaNamedReference definition : definitions) {
				defsJson.add( definition.getName(), definition.getSchema().toJson() );
			}
			json.add( "definitons", defsJson );
		}
		return json;
	}
	
}

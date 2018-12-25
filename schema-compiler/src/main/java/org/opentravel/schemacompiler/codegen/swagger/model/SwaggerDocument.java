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

import org.opentravel.schemacompiler.codegen.json.model.JsonModelObject;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Class that defines the meta-model for a Swagger document.
 */
public class SwaggerDocument implements JsonModelObject {
	
	public static final String SWAGGER_SPEC_V2 = "2.0";
	
	private String specVersion = SWAGGER_SPEC_V2;
	private SwaggerOtmResource otmResource;
	private SwaggerInfo info;
	private String host;
	private String basePath;
	private List<SwaggerScheme> schemes = new ArrayList<>();
	private List<String> consumes = new ArrayList<>();
	private List<String> produces = new ArrayList<>();
	private List<SwaggerPathItem> pathItems = new ArrayList<>();
	private List<JsonSchemaNamedReference> definitions = new ArrayList<>();
	private List<SwaggerParameter> globalParameters = new ArrayList<>();
	private List<SwaggerSecurityScheme> securitySchemes = new ArrayList<>();
	
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
	 * Returns the value of the 'otmResource' field.
	 *
	 * @return SwaggerOtmResource
	 */
	public SwaggerOtmResource getOtmResource() {
		return otmResource;
	}

	/**
	 * Assigns the value of the 'otmResource' field.
	 *
	 * @param otmResource  the field value to assign
	 */
	public void setOtmResource(SwaggerOtmResource otmResource) {
		this.otmResource = otmResource;
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
	 * @return List<SwaggerScheme>
	 */
	public List<SwaggerScheme> getSchemes() {
		return schemes;
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
	 * Returns the value of the 'produces' field.
	 *
	 * @return List<String>
	 */
	public List<String> getProduces() {
		return produces;
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
	 * Returns the value of the 'definitions' field.
	 *
	 * @return List<JsonSchemaNamedReference>
	 */
	public List<JsonSchemaNamedReference> getDefinitions() {
		return definitions;
	}
	
	/**
	 * Returns the value of the 'globalParameters' field.
	 *
	 * @return List<SwaggerParameter>
	 */
	public List<SwaggerParameter> getGlobalParameters() {
		return globalParameters;
	}
	
	/**
	 * Returns the value of the 'securitySchemes' field.
	 *
	 * @return List<SwaggerSecurityScheme>
	 */
	public List<SwaggerSecurityScheme> getSecuritySchemes() {
		return securitySchemes;
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
	 * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
	 */
	public JsonObject toJson() {
		JsonObject pathsJson = new JsonObject();
		JsonObject json = new JsonObject();
		
		addProperty( json, "swagger", specVersion );
		addJsonProperty( json, "x-otm-resource", otmResource );
		addJsonProperty( json, "info", (info != null) ? info : new SwaggerInfo() );
		addProperty( json, "host", host );
		addProperty( json, "basePath", basePath );
		
		if (!schemes.isEmpty()) {
			JsonArray jsonArray = new JsonArray();
			
			for (SwaggerScheme value : schemes) {
				if (value != null) {
					jsonArray.add( value.getDisplayValue() );
				}
			}
			json.add( "schemes", jsonArray );
		}
		
		addProperty( json, "consumes", consumes );
		addProperty( json, "produces", produces );
		
		for (SwaggerPathItem pathItem : pathItems) {
			pathsJson.add( pathItem.getPathTemplate(), pathItem.toJson() );
		}
		json.add( "paths", pathsJson );
		
		if (!definitions.isEmpty()) {
			JsonObject defsJson = new JsonObject();
			
			addJsonProperties( defsJson, definitions );
			json.add( "definitions", defsJson );
		}
		
		if (!globalParameters.isEmpty()) {
			JsonObject paramsJson = new JsonObject();
			
			addJsonProperties( paramsJson, globalParameters );
			json.add( "parameters", paramsJson );
		}
		
		securitySchemesToJson(json);
		return json;
	}

	/**
	 * Adds information for each known security schema as properties of the given
	 * <code>JsonObject</code>.
	 * 
	 * @param json  the JSON object being populated
	 */
	private void securitySchemesToJson(JsonObject json) {
		if (!securitySchemes.isEmpty()) {
			JsonObject securityDefsJson = new JsonObject();
			JsonArray securityReqsJson = new JsonArray();
			
			for (SwaggerSecurityScheme securityScheme : securitySchemes) {
				JsonObject securityReqirement = new JsonObject();
				JsonArray scopeNames = new JsonArray();
				
				if (securityScheme.getType() == SwaggerSecurityType.OAUTH2) {
					securityScheme.getScopes().forEach( s -> scopeNames.add( s.getName() ) );
				}
				securityReqirement.add( securityScheme.getName(), scopeNames );
				securityReqsJson.add( securityReqirement );
				securityDefsJson.add( securityScheme.getName(), securityScheme.toJson() );
			}
			json.add( "securityDefinitions", securityDefsJson );
			json.add( "security", securityReqsJson );
		}
	}
	
}

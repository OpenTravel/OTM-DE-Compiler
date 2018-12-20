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

import com.google.gson.JsonObject;

/**
 * Defines a security scheme for a Swagger API specification document.
 */
public class SwaggerSecurityScheme {
	
	private String name;
	private SwaggerSecurityType type;
	private String description;
	private String parameterName;
	private SwaggerSecurityLocation in;
	private SwaggerOAuth2Flow flow;
	private String authorizationUrl;
	private String tokenUrl;
	private List<SwaggerSecurityScope> scopes = new ArrayList<>();
	
	/**
	 * Returns the <code>JsonObject</code> representation of this Swagger
	 * model element.
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		
		if (type != null) {
			json.addProperty( "type", type.getDisplayValue() );
		}
		if (description != null) {
			json.addProperty( "description", description );
		}
		if (type == SwaggerSecurityType.API_KEY) {
			if (parameterName != null) {
				json.addProperty( "name", parameterName );
			}
			if (in != null) {
				json.addProperty( "in", in.getDisplayValue() );
			}
			
		} else if (type == SwaggerSecurityType.OAUTH2) {
			buildOAuth2Json( json );
		}
		return json;
	}

	/**
	 * Populates the OAuth2 content into the JSON object provided.
	 * 
	 * @param json  the JSON object to be populated
	 */
	private void buildOAuth2Json(JsonObject json) {
		if (flow != null) {
			json.addProperty( "flow", flow.getDisplayValue() );
		}
		if (((flow == SwaggerOAuth2Flow.IMPLICIT) || (flow == SwaggerOAuth2Flow.ACCESS_CODE)
				&& (authorizationUrl != null))) {
			json.addProperty( "authorizationUrl", authorizationUrl );
		}
		if (((flow == SwaggerOAuth2Flow.PASSWORD) || (flow == SwaggerOAuth2Flow.APPLICATION)
				|| (flow == SwaggerOAuth2Flow.ACCESS_CODE)) && (tokenUrl != null)) {
			json.addProperty( "tokenUrl", tokenUrl );
		}
		if (!scopes.isEmpty() && (type == SwaggerSecurityType.OAUTH2)) {
			JsonObject jsonScopes = new JsonObject();
			
			for (SwaggerSecurityScope scope : scopes) {
				jsonScopes.addProperty( scope.getName(), scope.getDescription() );
			}
			json.add( "scopes", jsonScopes );
		}
	}
	
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
	 * Returns the value of the 'type' field.
	 *
	 * @return SwaggerSecurityType
	 */
	public SwaggerSecurityType getType() {
		return type;
	}
	
	/**
	 * Assigns the value of the 'type' field.
	 *
	 * @param type  the field value to assign
	 */
	public void setType(SwaggerSecurityType type) {
		this.type = type;
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
	 * Returns the value of the 'parameterName' field.
	 *
	 * @return String
	 */
	public String getParameterName() {
		return parameterName;
	}
	
	/**
	 * Assigns the value of the 'parameterName' field.
	 *
	 * @param parameterName  the field value to assign
	 */
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}
	
	/**
	 * Returns the value of the 'in' field.
	 *
	 * @return SwaggerSecurityLocation
	 */
	public SwaggerSecurityLocation getIn() {
		return in;
	}
	
	/**
	 * Assigns the value of the 'in' field.
	 *
	 * @param in  the field value to assign
	 */
	public void setIn(SwaggerSecurityLocation in) {
		this.in = in;
	}
	
	/**
	 * Returns the value of the 'flow' field.
	 *
	 * @return SwaggerOAuth2Flow
	 */
	public SwaggerOAuth2Flow getFlow() {
		return flow;
	}
	
	/**
	 * Assigns the value of the 'flow' field.
	 *
	 * @param flow  the field value to assign
	 */
	public void setFlow(SwaggerOAuth2Flow flow) {
		this.flow = flow;
	}
	
	/**
	 * Returns the value of the 'authorizationUrl' field.
	 *
	 * @return String
	 */
	public String getAuthorizationUrl() {
		return authorizationUrl;
	}
	
	/**
	 * Assigns the value of the 'authorizationUrl' field.
	 *
	 * @param authorizationUrl  the field value to assign
	 */
	public void setAuthorizationUrl(String authorizationUrl) {
		this.authorizationUrl = authorizationUrl;
	}
	
	/**
	 * Returns the value of the 'tokenUrl' field.
	 *
	 * @return String
	 */
	public String getTokenUrl() {
		return tokenUrl;
	}
	
	/**
	 * Assigns the value of the 'tokenUrl' field.
	 *
	 * @param tokenUrl  the field value to assign
	 */
	public void setTokenUrl(String tokenUrl) {
		this.tokenUrl = tokenUrl;
	}
	
	/**
	 * Returns the value of the 'scopes' field.
	 *
	 * @return List<SwaggerSecurityScope>
	 */
	public List<SwaggerSecurityScope> getScopes() {
		return scopes;
	}
	
}

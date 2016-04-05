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

package org.opentravel.schemacompiler.codegen.swagger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerHeader;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerOAuth2Flow;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParamType;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParameter;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerScheme;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerSecurityLocation;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerSecurityScheme;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerSecurityScope;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerSecurityType;

/**
 * Supplies default binding information for generated Swagger documents.
 */
public class DefaultSwaggerBindings implements CodeGenerationSwaggerBindings {
	
	protected List<SwaggerScheme> supportedSchemes = new ArrayList<>();
	protected List<SwaggerParameter> globalParameters = new ArrayList<>();
	protected List<SwaggerHeader> globalResponseHeaders = new ArrayList<>();
	protected List<SwaggerSecurityScheme> securitySchemes = new ArrayList<>();
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.swagger.CodeGenerationSwaggerBindings#getSupportedSchemes()
	 */
	@Override
	public List<SwaggerScheme> getSupportedSchemes() {
		return supportedSchemes;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.swagger.CodeGenerationSwaggerBindings#getGlobalParameters()
	 */
	@Override
	public List<SwaggerParameter> getGlobalParameters() {
		return globalParameters;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.swagger.CodeGenerationSwaggerBindings#getGlobalResponseHeaders()
	 */
	@Override
	public List<SwaggerHeader> getGlobalResponseHeaders() {
		return globalResponseHeaders;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.swagger.CodeGenerationSwaggerBindings#getSecuritySchemes()
	 */
	@Override
	public List<SwaggerSecurityScheme> getSecuritySchemes() {
		return securitySchemes;
	}
	
	/**
	 * Initialization method that adds a new supported scheme.
	 * 
	 * @param scheme  the URL scheme to add
	 */
	protected void addSupportedScheme(SwaggerScheme scheme) {
		if (scheme != null) {
			supportedSchemes.add( scheme );
		}
	}
	
	/**
	 * Initialization method that adds a new global parameter.
	 * 
	 * @param name  the name of the parameter
	 * @param type  the type of the parameter
	 * @param paramType  the location (header/query) of the parameter
	 * @param description  a brief description of the parameter
	 */
	protected void addGlobalParameter(String name, JsonType type, SwaggerParamType paramType, String description) {
		SwaggerParameter parameter = new SwaggerParameter();
		JsonSchema typeSchema = new JsonSchema();
		
		typeSchema.setType( type );
		parameter.setName( name );
		parameter.setType( typeSchema );
		parameter.setIn( paramType );
		parameter.setDocumentation( new JsonDocumentation( description ) );
		globalParameters.add( parameter );
	}
	
	/**
	 * Initialization method that adds a new global response header.
	 * 
	 * @param name  the name of the response header
	 * @param type  the type of the response header
	 * @param description  a brief description of the response header
	 */
	protected void addResponseHeader(String name, JsonType type, String description) {
		SwaggerHeader header = new SwaggerHeader();
		JsonSchema typeSchema = new JsonSchema();
		
		typeSchema.setType( type );
		header.setName( name );
		header.setType( typeSchema );
		header.setDocumentation( new JsonDocumentation( description ) );
		globalResponseHeaders.add( header );
	}
	
	/**
	 * Initialization method that adds a new security scheme.
	 * 
	 * @param name  the name of the security scheme
	 * @param type  the type of the security scheme
	 * @param description  a brief description of the security scheme
	 * @param parameterName  the name of the URL parameter used for credentials (APIKey types only)
	 * @param in  the location of the URL parameter (APIKey types only)
	 * @param flow  the OAuth2 security flow (OAuth2 types only)
	 * @param authorizationUrl  the authorization URL of the security service  (OAuth2 types only)
	 * @param tokenUrl  the token URL of the security service  (OAuth2 types only)
	 * @param scopes  the applicable security scope definitions  (OAuth2 types only)
	 */
	protected void addSecurityScheme(String name, SwaggerSecurityType type, String description,
			String parameterName, SwaggerSecurityLocation in, SwaggerOAuth2Flow flow,
			String authorizationUrl, String tokenUrl, SwaggerSecurityScope... scopes) {
		SwaggerSecurityScheme securityScheme = new SwaggerSecurityScheme();
		
		securityScheme.setName( name );
		securityScheme.setType( type );
		securityScheme.setDescription( description );
		securityScheme.setIn( in );
		securityScheme.setParameterName( parameterName );
		securityScheme.setFlow( flow );
		securityScheme.setAuthorizationUrl( authorizationUrl );
		securityScheme.setTokenUrl( tokenUrl );
		
		if (scopes != null) {
			securityScheme.getScopes().addAll( Arrays.asList( scopes ) );
		}
		securitySchemes.add( securityScheme );
	}
	
}

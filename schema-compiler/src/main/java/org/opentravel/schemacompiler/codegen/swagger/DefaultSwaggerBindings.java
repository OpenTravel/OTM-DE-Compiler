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
import java.util.List;

import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerHeader;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParamType;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParameter;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerResponse;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerScheme;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerSecurityScheme;

/**
 * Supplies default binding information for generated Swagger documents.
 */
public class DefaultSwaggerBindings implements CodeGenerationSwaggerBindings {
	
	protected List<SwaggerScheme> supportedSchemes = new ArrayList<>();
	protected List<SwaggerParameter> globalParameters = new ArrayList<>();
	protected List<SwaggerHeader> globalResponseHeaders = new ArrayList<>();
	protected List<SwaggerResponse> globalResponses = new ArrayList<>();
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
	 * @see org.opentravel.schemacompiler.codegen.swagger.CodeGenerationSwaggerBindings#getGlobalResponses()
	 */
	@Override
	public List<SwaggerResponse> getGlobalResponses() {
		return globalResponses;
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
	 * @param securityScheme  the security scheme to add
	 */
	protected void addSecurityScheme(SwaggerSecurityScheme securityScheme) {
		securitySchemes.add( securityScheme );
	}
	
}

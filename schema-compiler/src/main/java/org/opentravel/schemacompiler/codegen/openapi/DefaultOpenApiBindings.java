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

package org.opentravel.schemacompiler.codegen.openapi;

import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiHeader;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiParamType;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiParameter;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiResponse;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiSecurityScheme;
import org.opentravel.schemacompiler.codegen.swagger.CodeGenerationSwaggerBindings;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerScheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Supplies default binding information for generated OpenAPI documents.
 */
public class DefaultOpenApiBindings implements CodeGenerationOpenApiBindings {

    protected List<OpenApiParameter> globalParameters = new ArrayList<>();
    protected List<OpenApiHeader> globalResponseHeaders = new ArrayList<>();
    protected List<OpenApiResponse> globalResponses = new ArrayList<>();
    protected List<OpenApiSecurityScheme> securitySchemes = new ArrayList<>();

    /**
     * Default constructor.
     */
    public DefaultOpenApiBindings() {}

    /**
     * Constructor that creates bindings for an OpenAPI specification using settings from the Swagger bindings provided.
     */
    public DefaultOpenApiBindings(CodeGenerationSwaggerBindings swaggerBindings) {
        swaggerBindings.getGlobalParameters().forEach( p -> globalParameters.add( new OpenApiParameter( p ) ) );
        swaggerBindings.getGlobalResponseHeaders().forEach( h -> globalResponseHeaders.add( new OpenApiHeader( h ) ) );
        swaggerBindings.getGlobalResponses().forEach( r -> globalResponses.add( new OpenApiResponse( r ) ) );

        for (SwaggerScheme swaggerScheme : swaggerBindings.getSupportedSchemes()) {
            swaggerBindings.getSecuritySchemes()
                .forEach( s -> securitySchemes.add( new OpenApiSecurityScheme( s, swaggerScheme ) ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.openapi.CodeGenerationOpenApiBindings#getGlobalParameters()
     */
    @Override
    public List<OpenApiParameter> getGlobalParameters() {
        return globalParameters;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.openapi.CodeGenerationOpenApiBindings#getGlobalResponseHeaders()
     */
    @Override
    public List<OpenApiHeader> getGlobalResponseHeaders() {
        return globalResponseHeaders;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.openapi.CodeGenerationOpenApiBindings#getGlobalResponses()
     */
    @Override
    public List<OpenApiResponse> getGlobalResponses() {
        return globalResponses;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.openapi.CodeGenerationOpenApiBindings#getSecuritySchemes()
     */
    @Override
    public List<OpenApiSecurityScheme> getSecuritySchemes() {
        return securitySchemes;
    }

    /**
     * Initialization method that adds a new global parameter.
     * 
     * @param name the name of the parameter
     * @param type the type of the parameter
     * @param paramType the location (header/query) of the parameter
     * @param required flag indicating whether the global parameter is required
     * @param description a brief description of the parameter
     */
    public void addGlobalParameter(String name, JsonType type, OpenApiParamType paramType, boolean required,
        String description) {
        OpenApiParameter parameter = new OpenApiParameter();
        JsonSchema typeSchema = new JsonSchema();

        typeSchema.setType( type );
        parameter.setName( name );
        parameter.setType( typeSchema );
        parameter.setIn( paramType );
        parameter.setRequired( required );
        parameter.setDocumentation( new JsonDocumentation( description ) );
        globalParameters.add( parameter );
    }

    /**
     * Initialization method that adds a new global response header.
     * 
     * @param name the name of the response header
     * @param type the type of the response header
     * @param description a brief description of the response header
     */
    public void addResponseHeader(String name, JsonType type, String description) {
        OpenApiHeader header = new OpenApiHeader();
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
     * @param securityScheme the security scheme to add
     */
    public void addSecurityScheme(OpenApiSecurityScheme securityScheme) {
        securitySchemes.add( securityScheme );
    }

}

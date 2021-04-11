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

package org.opentravel.schemacompiler.codegen.openapi.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.openapi.CodeGenerationOpenApiBindings;
import org.opentravel.schemacompiler.codegen.openapi.DefaultOpenApiBindings;
import org.opentravel.schemacompiler.codegen.swagger.CodeGenerationSwaggerBindings;
import org.opentravel.schemacompiler.codegen.swagger.DefaultSwaggerBindings;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerHeader;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerOAuth2Flow;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParamType;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParameter;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerResponse;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerScheme;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerSecurityLocation;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerSecurityType;
import org.opentravel.schemacompiler.codegen.swagger.model.TestSwaggerDocument;

/**
 * Verifies the mapping of Swagger bindings to their OpenAPI equivalents.
 */
public class TestCodeGenerationOpenApiBindings {

    private static CodeGenerationSwaggerBindings swaggerBindings;
    private static CodeGenerationOpenApiBindings openapiBindings;

    @BeforeClass
    public static void setup() {
        swaggerBindings = createSwaggerBindings();
        openapiBindings = new DefaultOpenApiBindings( swaggerBindings );
    }

    @Test
    public void testAddGlobalParameter() {
        DefaultOpenApiBindings bindings = new DefaultOpenApiBindings();
        OpenApiParameter param;

        bindings.addGlobalParameter( "param9", JsonType.JSON_STRING, OpenApiParamType.QUERY, true,
            "Param description" );
        assertEquals( 1, bindings.getGlobalParameters().size() );

        param = bindings.getGlobalParameters().get( 0 );
        assertEquals( "param9", param.getName() );
        assertEquals( OpenApiParamType.QUERY, param.getIn() );
        assertEquals( "Param description", param.getDocumentation().getDescriptions()[0] );
        assertTrue( param.isRequired() );
        assertNotNull( param.getType() );
        assertEquals( JsonType.JSON_STRING, param.getType().getType() );
    }

    @Test
    public void testAddResponseHeader() {
        DefaultOpenApiBindings bindings = new DefaultOpenApiBindings();
        OpenApiHeader header;

        bindings.addResponseHeader( "header9", JsonType.JSON_STRING, "Header description" );
        assertEquals( 1, bindings.getGlobalResponseHeaders().size() );

        header = bindings.getGlobalResponseHeaders().get( 0 );
        assertEquals( "header9", header.getName() );
        assertEquals( "Header description", header.getDocumentation().getDescriptions()[0] );
        assertFalse( header.isRequired() );
        assertNotNull( header.getType() );
        assertEquals( JsonType.JSON_STRING, header.getType().getType() );
    }

    @Test
    public void testAddSecurityScheme() {
        DefaultOpenApiBindings bindings = new DefaultOpenApiBindings();
        OpenApiSecurityScheme scheme =
            new OpenApiSecurityScheme( TestSwaggerDocument.createSecurityScheme(), SwaggerScheme.HTTPS );

        bindings.addSecurityScheme( scheme );
        assertEquals( 1, bindings.getSecuritySchemes().size() );
    }

    @Test
    public void testParameterMapping() {
        assertEquals( 1, openapiBindings.getGlobalParameters().size() );
        OpenApiParameter param = openapiBindings.getGlobalParameters().get( 0 );

        assertNotNull( param );
        assertEquals( "param1", param.getName() );
        assertEquals( OpenApiParamType.QUERY, param.getIn() );
        assertEquals( "Param description", param.getDocumentation().getDescriptions()[0] );
        assertTrue( param.isRequired() );
        assertNotNull( param.getType() );
        assertEquals( JsonType.JSON_STRING, param.getType().getType() );
    }

    @Test
    public void testHeaderMapping() {
        assertEquals( 1, openapiBindings.getGlobalResponseHeaders().size() );
        OpenApiHeader header = openapiBindings.getGlobalResponseHeaders().get( 0 );

        assertNotNull( header );
        assertEquals( "header1", header.getName() );
        assertEquals( "Header description", header.getDocumentation().getDescriptions()[0] );
        assertFalse( header.isRequired() );
        assertNotNull( header.getType() );
        assertEquals( JsonType.JSON_STRING, header.getType().getType() );
    }

    @Test
    public void testResponseMapping() {
        assertEquals( 1, openapiBindings.getGlobalResponses().size() );
        OpenApiResponse response = openapiBindings.getGlobalResponses().get( 0 );

        assertFalse( response.isDefaultResponse() );
        assertEquals( 500, response.getStatusCode().intValue() );
        assertEquals( "Response description", response.getDocumentation().getDescriptions()[0] );
        assertEquals( 1, response.getContent().size() );
        assertNotNull( response.getContent().get( 0 ).getRequestType() );
        assertEquals( "#/definitions/GlobalResponse", response.getContent().get( 0 ).getRequestType().getSchemaPath() );
        assertEquals( 1, response.getHeaders().size() );
        assertEquals( "responseHeader1", response.getHeaders().get( 0 ).getName() );
    }

    @Test
    public void testSecuritySchemeMapping() {
        assertEquals( 2, openapiBindings.getSecuritySchemes().size() );
        OpenApiSecurityScheme scheme = openapiBindings.getSecuritySchemes().get( 0 );

        assertEquals( "OAuth", scheme.getName() );
        assertEquals( OpenApiSecurityType.OAUTH2, scheme.getType() );
        assertEquals( "OAuth Description", scheme.getDescription() );
        assertEquals( OpenApiSecurityLocation.HEADER, scheme.getIn() );
        assertEquals( OpenApiScheme.HTTP, scheme.getScheme() );
        assertNull( scheme.getBearerFormat() );
        assertNull( scheme.getOpenIdConnectUrl() );
        assertEquals( 1, scheme.getFlows().size() );
        assertEquals( OpenApiOAuth2FlowType.APPLICATION, scheme.getFlows().get( 0 ).getType() );
        assertEquals( "http://www.testapp.com/auth", scheme.getFlows().get( 0 ).getAuthorizationUrl() );
        assertEquals( "http://www.testapp.com/tokens", scheme.getFlows().get( 0 ).getTokenUrl() );
        assertNull( scheme.getFlows().get( 0 ).getRefreshUrl() );
        assertEquals( 2, scheme.getFlows().get( 0 ).getScopes().size() );
        assertEquals( "scope1", scheme.getFlows().get( 0 ).getScopes().get( 0 ).getName() );
        assertEquals( "scope2", scheme.getFlows().get( 0 ).getScopes().get( 1 ).getName() );
    }

    @Test
    public void testSchemeMapping() {
        assertEquals( OpenApiScheme.HTTP, OpenApiScheme.fromSwaggerScheme( SwaggerScheme.HTTP ) );
        assertEquals( OpenApiScheme.HTTPS, OpenApiScheme.fromSwaggerScheme( SwaggerScheme.HTTPS ) );
        assertEquals( OpenApiScheme.WS, OpenApiScheme.fromSwaggerScheme( SwaggerScheme.WS ) );
        assertEquals( OpenApiScheme.WSS, OpenApiScheme.fromSwaggerScheme( SwaggerScheme.WSS ) );
        assertEquals( OpenApiScheme.HTTP, OpenApiScheme.fromDisplayValue( "http" ) );
    }

    @Test
    public void testSecurityTypeMapping() {
        assertEquals( OpenApiSecurityType.BASIC,
            OpenApiSecurityType.fromSwaggerSecurityType( SwaggerSecurityType.BASIC ) );
        assertEquals( OpenApiSecurityType.API_KEY,
            OpenApiSecurityType.fromSwaggerSecurityType( SwaggerSecurityType.API_KEY ) );
        assertEquals( OpenApiSecurityType.OAUTH2,
            OpenApiSecurityType.fromSwaggerSecurityType( SwaggerSecurityType.OAUTH2 ) );
        assertEquals( OpenApiSecurityType.BASIC, OpenApiSecurityType.fromDisplayValue( "basic" ) );
    }

    @Test
    public void testSecurityLocationMapping() {
        assertEquals( OpenApiSecurityLocation.HEADER,
            OpenApiSecurityLocation.fromSwaggerSecurityLocation( SwaggerSecurityLocation.HEADER ) );
        assertEquals( OpenApiSecurityLocation.QUERY,
            OpenApiSecurityLocation.fromSwaggerSecurityLocation( SwaggerSecurityLocation.QUERY ) );
        assertEquals( OpenApiSecurityLocation.HEADER, OpenApiSecurityLocation.fromDisplayValue( "header" ) );
    }

    @Test
    public void testSecurityOAuth2FlowTypeMapping() {
        assertEquals( OpenApiOAuth2FlowType.IMPLICIT,
            OpenApiOAuth2FlowType.fromSwaggerOAuth2Flow( SwaggerOAuth2Flow.IMPLICIT ) );
        assertEquals( OpenApiOAuth2FlowType.PASSWORD,
            OpenApiOAuth2FlowType.fromSwaggerOAuth2Flow( SwaggerOAuth2Flow.PASSWORD ) );
        assertEquals( OpenApiOAuth2FlowType.APPLICATION,
            OpenApiOAuth2FlowType.fromSwaggerOAuth2Flow( SwaggerOAuth2Flow.APPLICATION ) );
        assertEquals( OpenApiOAuth2FlowType.ACCESS_CODE,
            OpenApiOAuth2FlowType.fromSwaggerOAuth2Flow( SwaggerOAuth2Flow.ACCESS_CODE ) );
        assertEquals( OpenApiOAuth2FlowType.IMPLICIT, OpenApiOAuth2FlowType.fromDisplayValue( "implicit" ) );
    }

    private static CodeGenerationSwaggerBindings createSwaggerBindings() {
        DefaultSwaggerBindings swaggerBindings = new DefaultSwaggerBindings();

        swaggerBindings.getGlobalParameters().add( createParameter() );
        swaggerBindings.getGlobalResponseHeaders().add( createHeader() );
        swaggerBindings.getGlobalResponses().add( createResponse() );
        swaggerBindings.getSecuritySchemes().add( TestSwaggerDocument.createSecurityScheme() );
        swaggerBindings.getSupportedSchemes().add( SwaggerScheme.HTTP );
        swaggerBindings.getSupportedSchemes().add( SwaggerScheme.HTTPS );
        return swaggerBindings;
    }

    private static SwaggerParameter createParameter() {
        SwaggerParameter param = new SwaggerParameter();
        JsonSchema paramSchema = new JsonSchema();

        param.setName( "param1" );
        param.setIn( SwaggerParamType.QUERY );
        param.setDocumentation( new JsonDocumentation( "Param description" ) );
        param.setRequired( true );
        param.setType( paramSchema );
        paramSchema.setType( JsonType.JSON_STRING );
        return param;
    }

    private static SwaggerHeader createHeader() {
        SwaggerHeader header = new SwaggerHeader();
        JsonSchema headerSchema = new JsonSchema();

        header.setName( "header1" );
        header.setDocumentation( new JsonDocumentation( "Header description" ) );
        header.setType( headerSchema );
        headerSchema.setType( JsonType.JSON_STRING );
        return header;
    }

    private static SwaggerResponse createResponse() {
        SwaggerResponse response = new SwaggerResponse();
        SwaggerHeader responseHeader = createHeader();

        response.setDefaultResponse( false );
        response.setStatusCode( 500 );
        response.setDocumentation( new JsonDocumentation( "Response description" ) );
        response.setSchema( new JsonSchemaReference( "#/definitions/GlobalResponse" ) );
        response.getHeaders().add( responseHeader );
        responseHeader.setName( "responseHeader1" );
        return response;
    }

}

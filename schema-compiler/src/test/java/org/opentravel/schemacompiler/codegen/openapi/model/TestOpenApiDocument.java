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
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;

/**
 * Verifies the functions of the <code>OpenApiDocument</code> class.
 */
public class TestOpenApiDocument {

    private OpenApiDocument openapiDoc;
    private JsonObject openapiJson;

    @Before
    public void setup() throws Exception {
        openapiDoc = createDocument();
        openapiJson = openapiDoc.toJson();
    }

    @Test
    public void testContact() throws Exception {
        JsonObject infoJson = openapiJson.getAsJsonObject( "info" );
        JsonObject contactJson = infoJson.getAsJsonObject( "contact" );
        OpenApiContact swaggerContact = openapiDoc.getInfo().getContact();

        assertEquals( swaggerContact.getName(), contactJson.getAsJsonPrimitive( "name" ).getAsString() );
        assertEquals( swaggerContact.getEmail(), contactJson.getAsJsonPrimitive( "email" ).getAsString() );
        assertEquals( swaggerContact.getUrl(), contactJson.getAsJsonPrimitive( "url" ).getAsString() );
    }

    @Test
    public void testLicense() throws Exception {
        JsonObject infoJson = openapiJson.getAsJsonObject( "info" );
        JsonObject licenseJson = infoJson.getAsJsonObject( "license" );
        OpenApiLicense swaggerLicense = openapiDoc.getInfo().getLicense();

        assertEquals( swaggerLicense.getName(), licenseJson.getAsJsonPrimitive( "name" ).getAsString() );
        assertEquals( swaggerLicense.getUrl(), licenseJson.getAsJsonPrimitive( "url" ).getAsString() );
    }

    @Test
    public void testSecurityScheme() throws Exception {
        JsonObject componentsJson = openapiJson.getAsJsonObject( "components" );
        JsonObject securitySchemes = componentsJson.getAsJsonObject( "securitySchemes" );
        JsonObject oauthScheme = securitySchemes.getAsJsonObject( "OAuth" );
        JsonObject oauthFlows = oauthScheme.getAsJsonObject( "flows" );
        JsonObject passwordFlow = oauthFlows.getAsJsonObject( "password" );
        JsonObject passwordScopes = passwordFlow.getAsJsonObject( "scopes" );
        OpenApiSecurityScheme openapiScheme = openapiDoc.getComponents().getSecuritySchemes().get( 0 );
        OpenApiOAuth2Flow openapiFlow = openapiScheme.getFlows().get( 0 );

        assertNotNull( passwordScopes );

        assertEquals( openapiScheme.getType().getDisplayValue(),
            oauthScheme.getAsJsonPrimitive( "type" ).getAsString() );
        assertEquals( openapiScheme.getName(), oauthScheme.getAsJsonPrimitive( "name" ).getAsString() );
        assertEquals( openapiScheme.getDescription(), oauthScheme.getAsJsonPrimitive( "description" ).getAsString() );
        assertEquals( openapiScheme.getIn().getDisplayValue(), oauthScheme.getAsJsonPrimitive( "in" ).getAsString() );
        assertEquals( openapiScheme.getScheme().getDisplayValue(),
            oauthScheme.getAsJsonPrimitive( "scheme" ).getAsString() );

        assertEquals( openapiFlow.getAuthorizationUrl(),
            passwordFlow.getAsJsonPrimitive( "authorizationUrl" ).getAsString() );
        assertEquals( openapiFlow.getTokenUrl(), passwordFlow.getAsJsonPrimitive( "tokenUrl" ).getAsString() );
        assertEquals( openapiFlow.getRefreshUrl(), passwordFlow.getAsJsonPrimitive( "refreshUrl" ).getAsString() );

        for (int i = 0; i < openapiFlow.getScopes().size(); i++) {
            assertEquals( openapiFlow.getScopes().get( i ).getDescription(),
                passwordScopes.getAsJsonPrimitive( openapiFlow.getScopes().get( i ).getName() ).getAsString() );
        }
    }

    private OpenApiDocument createDocument() {
        OpenApiDocument doc = new OpenApiDocument();
        OpenApiInfo info = new OpenApiInfo();
        OpenApiContact contact = new OpenApiContact();
        OpenApiLicense license = new OpenApiLicense();
        OpenApiSecurityScheme scheme = new OpenApiSecurityScheme();
        OpenApiOAuth2Flow flow = new OpenApiOAuth2Flow();
        OpenApiSecurityScope scope1 = new OpenApiSecurityScope();
        OpenApiSecurityScope scope2 = new OpenApiSecurityScope();

        contact.setName( "John Doe" );
        contact.setEmail( "john.doe@opentravel.org" );
        contact.setUrl( "http://www.opentravel.org/john.doe" );
        info.setContact( contact );

        license.setName( "TestLicense" );
        license.setUrl( "http://www.opentravel.org/test-license" );
        info.setLicense( license );

        scope1.setName( "scope1" );
        scope1.setDescription( "scope1 Description" );
        scope2.setName( "scope2" );
        scope2.setDescription( "scope2 Description" );

        flow.setType( OpenApiOAuth2FlowType.PASSWORD );
        flow.setAuthorizationUrl( "http://www.testapp.com/auth" );
        flow.setTokenUrl( "http://www.testapp.com/tokens" );
        flow.setRefreshUrl( "http://www.testapp.com/refresh" );
        flow.getScopes().add( scope1 );
        flow.getScopes().add( scope2 );

        scheme.setName( "OAuth" );
        scheme.setType( OpenApiSecurityType.OAUTH2 );
        scheme.setDescription( "OAuth Description" );
        scheme.setIn( OpenApiSecurityLocation.HEADER );
        scheme.setScheme( OpenApiScheme.HTTPS );
        scheme.getFlows().add( flow );
        scheme.setOpenIdConnectUrl( "http://www.testapp.com/openid-connect" );

        doc.setInfo( info );
        doc.getComponents().getSecuritySchemes().add( scheme );
        return doc;
    }

}

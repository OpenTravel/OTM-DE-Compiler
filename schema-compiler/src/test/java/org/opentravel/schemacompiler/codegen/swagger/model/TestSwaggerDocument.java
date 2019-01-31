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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Verifies the functions of the <code>SwaggerDocument</code> class.
 */
public class TestSwaggerDocument {
	
    private SwaggerDocument swaggerDoc;
    private JsonObject swaggerJson;
    
    @Before
    public void setup() throws Exception {
        swaggerDoc = createDocument();
        swaggerJson = swaggerDoc.toJson();
    }
    
    @Test
    public void testContact() throws Exception {
        JsonObject infoJson = swaggerJson.getAsJsonObject( "info" );
        JsonObject contactJson = infoJson.getAsJsonObject( "contact" );
        SwaggerContact swaggerContact = swaggerDoc.getInfo().getContact();
        
        assertEquals( swaggerContact.getName(), contactJson.getAsJsonPrimitive( "name" ).getAsString() );
        assertEquals( swaggerContact.getEmail(), contactJson.getAsJsonPrimitive( "email" ).getAsString() );
        assertEquals( swaggerContact.getUrl(), contactJson.getAsJsonPrimitive( "url" ).getAsString() );
    }
    
    @Test
    public void testLicense() throws Exception {
        JsonObject infoJson = swaggerJson.getAsJsonObject( "info" );
        JsonObject licenseJson = infoJson.getAsJsonObject( "license" );
        SwaggerLicense swaggerLicense = swaggerDoc.getInfo().getLicense();
        
        assertEquals( swaggerLicense.getName(), licenseJson.getAsJsonPrimitive( "name" ).getAsString() );
        assertEquals( swaggerLicense.getUrl(), licenseJson.getAsJsonPrimitive( "url" ).getAsString() );
    }
    
	@Test
	public void testSecurityScheme() throws Exception {
	    JsonObject securityDefs = swaggerJson.getAsJsonObject( "securityDefinitions" );
	    JsonArray securityList = swaggerJson.getAsJsonArray( "security" );
	    SwaggerSecurityScheme swaggerScheme = swaggerDoc.getSecuritySchemes().get( 0 );
	    
	    assertNotNull( securityDefs );
        assertNotNull( securityList );
        
        for (JsonElement sItem : securityList) {
            Set<String> scopeSet = new HashSet<>();
            
            for (Entry<String,JsonElement> entry : sItem.getAsJsonObject().entrySet()) {
                String securityName = entry.getKey();
                JsonArray scopeNames = (JsonArray) entry.getValue();
                
                scopeNames.forEach( s -> scopeSet.add( s.getAsString() ) );
                
                // Make sure the entries in the 'securityDefinitions' section match those of the security section
                JsonObject definition = securityDefs.getAsJsonObject( securityName );
                JsonObject jsonScopes;
                
                assertNotNull( definition );
                jsonScopes = definition.getAsJsonObject( "scopes" );
                assertNotNull( jsonScopes );
                
                assertEquals( swaggerScheme.getType().getDisplayValue(), definition.getAsJsonPrimitive( "type" ).getAsString() );
                assertEquals( swaggerScheme.getDescription(), definition.getAsJsonPrimitive( "description" ).getAsString() );
                assertEquals( swaggerScheme.getFlow().getDisplayValue(), definition.getAsJsonPrimitive( "flow" ).getAsString() );
                assertEquals( swaggerScheme.getTokenUrl(), definition.getAsJsonPrimitive( "tokenUrl" ).getAsString() );
                assertNull( definition.getAsJsonPrimitive( "authorizationUrl" ) );
                assertEquals( swaggerScheme.getScopes().size(), jsonScopes.entrySet().size() );
                
                for (Entry<String,JsonElement> scopeEntry : jsonScopes.entrySet()) {
                    if (!scopeSet.contains( scopeEntry.getKey() )) {
                        Assert.fail( "Swagger document missing OAuth scope definition: " + scopeEntry.getKey() );
                    }
                }
            }
        }
	}
	
	private SwaggerDocument createDocument() {
        SwaggerDocument doc = new SwaggerDocument();
        SwaggerInfo info = new SwaggerInfo();
        SwaggerContact contact = new SwaggerContact();
        SwaggerLicense license = new SwaggerLicense();
        SwaggerSecurityScheme scheme = new SwaggerSecurityScheme();
        SwaggerSecurityScope scope1 = new SwaggerSecurityScope();
        SwaggerSecurityScope scope2 = new SwaggerSecurityScope();
        
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
        
        scheme.setName( "OAuth" );
        scheme.setType( SwaggerSecurityType.OAUTH2 );
        scheme.setDescription( "OAuth Description" );
        scheme.setParameterName( "param-name" );
        scheme.setIn( SwaggerSecurityLocation.HEADER );
        scheme.setFlow( SwaggerOAuth2Flow.APPLICATION );
        scheme.setAuthorizationUrl( "http://www.testapp.com/auth" );
        scheme.setTokenUrl( "http://www.testapp.com/tokens" );
        scheme.getScopes().add( scope1 );
        scheme.getScopes().add( scope2 );
        
        doc.setInfo( info );
        doc.getSecuritySchemes().add( scheme );
        
        return doc;
	}
}

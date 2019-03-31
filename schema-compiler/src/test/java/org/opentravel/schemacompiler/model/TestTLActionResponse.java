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

package org.opentravel.schemacompiler.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Verifies the functions of the <code>TLActionResponse</code> class.
 */
public class TestTLActionResponse extends AbstractModelTest {

    @Test
    public void testIdentityFunctions() throws Exception {
        TLResource resource = addResource( "TestResource", library1 );
        TLAction action = addAction( "Action", resource );
        TLActionResponse response = addActionResponse( 200, null, action );

        assertEquals( "TestLibrary1.otm : TestResource/Action/Response", response.getValidationIdentity() );
    }

    @Test
    public void testDocumentationFunctions() throws Exception {
        testDocumentationFunctions( new TLActionResponse() );
    }

    @Test
    public void testStatusCodeFunctions() throws Exception {
        TLResource resource = addResource( "TestResource", library1 );
        TLAction action = addAction( "Action", resource );
        TLActionResponse response = addActionResponse( 200, null, action );

        response.addStatusCode( 204 );
        assertEquals( 2, response.getStatusCodes().size() );
        assertTrue( response.getStatusCodes().contains( 200 ) );
        assertTrue( response.getStatusCodes().contains( 204 ) );

        response.removeStatusCode( 200 );
        assertEquals( 1, response.getStatusCodes().size() );
        assertTrue( response.getStatusCodes().contains( 204 ) );
    }

    @Test
    public void testMimeTypeFunctions() throws Exception {
        TLResource resource = addResource( "TestResource", library1 );
        TLAction action = addAction( "Action", resource );
        TLActionResponse response = addActionResponse( 200, null, action );

        assertEquals( 2, response.getMimeTypes().size() );

        response.addMimeType( TLMimeType.TEXT_XML );
        assertEquals( 3, response.getMimeTypes().size() );
        assertTrue( response.getMimeTypes().contains( TLMimeType.TEXT_XML ) );

        response.removeMimeType( TLMimeType.TEXT_XML );
        assertEquals( 2, response.getMimeTypes().size() );
        assertFalse( response.getMimeTypes().contains( TLMimeType.TEXT_XML ) );
    }

}

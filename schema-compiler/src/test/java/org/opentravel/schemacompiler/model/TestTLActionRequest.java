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
 * Verifies the functions of the <code>TLActionRequest</code> class.
 */
public class TestTLActionRequest extends AbstractModelTest {
	
	@Test
	public void testIdentityFunctions() throws Exception {
		TLResource resource = addResource( "TestResource", library1 );
		TLParamGroup paramGroup = addParamGroup( "TestParamGroup",
				resource.getBusinessObjectRef().getIdFacet(), resource );
		TLAction action = addAction( "Action", resource );
		TLActionRequest request = addActionRequest( "/test", paramGroup, TLHttpMethod.GET, null, action );
		
		assertEquals( "TestLibrary1.otm : TestResource/Action/Request", request.getValidationIdentity() );
	}
	
	@Test
	public void testDocumentationFunctions() throws Exception {
		testDocumentationFunctions( new TLActionRequest() );
	}
	
	@Test
	public void testMimeTypeFunctions() throws Exception {
		TLResource resource = addResource( "TestResource", library1 );
		TLParamGroup paramGroup = addParamGroup( "TestParamGroup",
				resource.getBusinessObjectRef().getIdFacet(), resource );
		TLAction action = addAction( "Action", resource );
		TLActionRequest request = addActionRequest( "/test", paramGroup, TLHttpMethod.GET, null, action );
		
		assertEquals( 2, request.getMimeTypes().size() );
		
		request.addMimeType( TLMimeType.TEXT_XML );
		assertEquals( 3, request.getMimeTypes().size() );
		assertTrue( request.getMimeTypes().contains( TLMimeType.TEXT_XML ) );
		
		request.removeMimeType( TLMimeType.TEXT_XML );
		assertEquals( 2, request.getMimeTypes().size() );
		assertFalse( request.getMimeTypes().contains( TLMimeType.TEXT_XML ) );
	}
	
}

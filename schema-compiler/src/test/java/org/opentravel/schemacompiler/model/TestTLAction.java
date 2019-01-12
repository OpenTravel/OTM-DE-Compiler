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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Verifies the functions of the <code>TLAction</code> class.
 */
public class TestTLAction extends AbstractModelTest {
	
	@Test
	public void testIdentityFunctions() throws Exception {
		TLResource resource = addResource( "TestResource", library1 );
		TLAction action = addAction( "Action", resource );
		
		assertEquals( "TestLibrary1.otm : TestResource/Action", action.getValidationIdentity() );
	}
	
	@Test
	public void testDocumentationFunctions() throws Exception {
		testDocumentationFunctions( new TLAction() );
	}
	
	@Test
	public void testActionResponseFunctions() throws Exception {
		TLResource resource = addResource( "TestResource", library1 );
		TLAction action = addAction( "Action", resource );
		TLActionResponse response1 = addActionResponse( 200, null, action );
		TLActionResponse response2 = addActionResponse( 204, null, action );
		
		assertArrayEquals( new String[] { "200", "204" }, getNames( action.getResponses(), r -> r.getStatusCodes().get( 0 ).toString() ) );
		
		action.moveDown( response1 );
		assertArrayEquals( new String[] { "204", "200" }, getNames( action.getResponses(), r -> r.getStatusCodes().get( 0 ).toString() ) );
		
		action.moveUp( response1 );
		assertArrayEquals( new String[] { "200", "204" }, getNames( action.getResponses(), r -> r.getStatusCodes().get( 0 ).toString() ) );
		
		action.removeResponse( response2 );
		assertArrayEquals( new String[] { "200" }, getNames( action.getResponses(), r -> r.getStatusCodes().get( 0 ).toString() ) );
	}
	
}

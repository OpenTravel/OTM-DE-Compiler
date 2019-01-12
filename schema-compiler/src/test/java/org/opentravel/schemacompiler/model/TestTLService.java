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
 * Verifies the functions of the <code>TLService</code> class.
 */
public class TestTLService extends AbstractModelTest {
	
	@Test
	public void testIdentityFunctions() throws Exception {
		TLService service = addService( "TestService", library1 );
		
		assertEquals( library1.getNamespace(), service.getNamespace() );
		assertEquals( service.getName(), service.getLocalName() );
		assertEquals( "TestLibrary1.otm : TestService", service.getValidationIdentity() );
	}
	
	@Test
	public void testDocumentationFunctions() throws Exception {
		testDocumentationFunctions( addService( "TestService", library1 ) );
	}
	
	@Test
	public void testEquivalentFunctions() throws Exception {
		testEquivalentFunctions( addService( "TestService", library1 ) );
	}
	
	@Test
	public void testOperationFunctions() throws Exception {
		TLService service = addService( "TestService", library1 );
		TLOperation op1 = addOperation( "Op1", service );
		TLOperation op2 = addOperation( "Op2", service );
		
		service.addOperation( op1 );
		service.addOperation( 1, op2 );
		assertEquals( op1, service.getOperation( "Op1" ) );
		assertEquals( op2, service.getOperation( "Op2" ) );
		assertArrayEquals( new String[] { "Op1", "Op2" }, getNames( service.getOperations(), o -> o.getName() ) );
		
		op1.moveDown();
		assertArrayEquals( new String[] { "Op2", "Op1" }, getNames( service.getOperations(), o -> o.getName() ) );
		
		service.sortOperations( (f1, f2) -> f1.getName().compareTo( f2.getName() ) );
		assertArrayEquals( new String[] { "Op1", "Op2" }, getNames( service.getOperations(), o -> o.getName() ) );
		
		op2.moveUp();
		assertArrayEquals( new String[] { "Op2", "Op1" }, getNames( service.getOperations(), o -> o.getName() ) );
		
		service.removeOperation( op1 );
		assertArrayEquals( new String[] { "Op2" }, getNames( service.getOperations(), o -> o.getName() ) );
	}
	
}

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

import org.junit.Test;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * Verifies the functions of the <code>TLOperation</code> class.
 */
public class TestTLOperation extends AbstractModelTest {
	
	@Test
	public void testIdentityFunctions() throws Exception {
		TLService service = addService( "TestService", library1 );
		TLOperation op = addOperation( "TestOperation", service );
		
		assertEquals( library1.getNamespace(), op.getNamespace() );
		assertEquals( library1.getBaseNamespace(), op.getBaseNamespace() );
		assertEquals( service.getName() + "_" + op.getName(), op.getLocalName() );
		assertEquals( library1.getVersion(), op.getVersion() );
		assertEquals( "TestLibrary1.otm : TestService.TestOperation", op.getValidationIdentity() );
		assertEquals( VersionSchemeFactory.getInstance().getDefaultVersionScheme(), op.getVersionScheme() );
	}
	
	@Test
	public void testExtensionFunctions() throws Exception {
		TLService service1 = addService( "TestService1", library1 );
		TLService service2 = addService( "TestService2", library2 );
		TLOperation op1 = addOperation( "TestOperation1", service1 );
		TLOperation op2 = addOperation( "TestOperation2", service2 );
		
		addExtension( op2, op1 );
		testExtensionFunctions( op2 );
	}
	
	@Test
	public void testDocumentationFunctions() throws Exception {
		TLService service = addService( "TestService", library1 );
		TLOperation op = addOperation( "TestOperation", service );
		
		testDocumentationFunctions( op );
	}
	
	@Test
	public void testEquivalentFunctions() throws Exception {
		TLService service = addService( "TestService", library1 );
		TLOperation op = addOperation( "TestOperation", service );
		
		testEquivalentFunctions( op );
	}
	
}

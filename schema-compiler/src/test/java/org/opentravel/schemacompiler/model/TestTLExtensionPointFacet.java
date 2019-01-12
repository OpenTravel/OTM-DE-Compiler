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
 * Verifies the functions of the <code>TLExtensionPointFacet</code> class.
 */
public class TestTLExtensionPointFacet extends AbstractModelTest {
	
	@Test
	public void testIdentityFunctions() throws Exception {
		TLExtensionPointFacet epf = addExtensionPointFacet( "TestObject", library1 );
		
		assertEquals( library1.getNamespace(), epf.getNamespace() );
		assertEquals( library1.getBaseNamespace(), epf.getBaseNamespace() );
		assertEquals( "ExtensionPoint_TestObject", epf.getLocalName() );
		assertEquals( library1.getVersion(), epf.getVersion() );
		assertEquals( "TestLibrary1.otm : ExtensionPoint_TestObject", epf.getValidationIdentity() );
		assertEquals( VersionSchemeFactory.getInstance().getDefaultVersionScheme(), epf.getVersionScheme() );
	}
	
	@Test
	public void testExtensionFunctions() throws Exception {
		testExtensionFunctions( addExtensionPointFacet( "TestObject", library1 ) );
	}
	
	@Test
	public void testDocumentationFunctions() throws Exception {
		testDocumentationFunctions( addExtensionPointFacet( "TestObject", library1 ) );
	}
	
	@Test
	public void testMemberFieldFunctions() throws Exception {
		TLExtensionPointFacet epf = addExtensionPointFacet( "TestObject", library1 );
		
		testAttributeFunctions( epf );
		testPropertyFunctions( epf );
		testIndicatorFunctions( epf );
	}
	
	private TLExtensionPointFacet addExtensionPointFacet(String coreName, TLLibrary library) {
		TLExtensionPointFacet epf = new TLExtensionPointFacet();
		TLCoreObject core = addCore( coreName, library );
		
		addExtension( epf, core );
		library.addNamedMember( epf );
		return epf;
	}
	
}

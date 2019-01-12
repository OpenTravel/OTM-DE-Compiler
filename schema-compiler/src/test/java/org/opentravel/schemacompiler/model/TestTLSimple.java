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
 * Verifies the functions of the <code>TestTLSimple</code> class.
 */
public class TestTLSimple extends AbstractModelTest {
	
	@Test
	public void testIdentityFunctions() throws Exception {
		TLSimple simple = addSimple( "TestObject", library1 );
		
		assertEquals( library1.getNamespace(), simple.getNamespace() );
		assertEquals( library1.getBaseNamespace(), simple.getBaseNamespace() );
		assertEquals( simple.getName(), simple.getLocalName() );
		assertEquals( library1.getVersion(), simple.getVersion() );
		assertEquals( "TestLibrary1.otm : TestObject", simple.getValidationIdentity() );
		assertEquals( VersionSchemeFactory.getInstance().getDefaultVersionScheme(), simple.getVersionScheme() );
	}
	
	@Test
	public void testDocumentationFunctions() throws Exception {
		testDocumentationFunctions( addSimple( "TestObject", library1 ) );
	}
	
	@Test
	public void testEquivalentFunctions() throws Exception {
		testEquivalentFunctions( addSimple( "TestObject", library1 ) );
	}
	
	@Test
	public void testExampleFunctions() throws Exception {
		testExampleFunctions( addSimple( "TestObject", library1 ) );
	}
	
}

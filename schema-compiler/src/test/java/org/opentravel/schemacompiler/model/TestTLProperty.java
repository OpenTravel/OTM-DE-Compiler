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

/**
 * Verifies the functions of the <code>TLProperty</code> class.
 */
public class TestTLProperty extends AbstractModelTest {
	
	@Test
	public void testIdentityFunctions() throws Exception {
		TLCoreObject core = addCore( "TestObject", library1 );
		TLProperty element = addElement( "TestElement", core.getSummaryFacet() );
		
		assertEquals( "TestLibrary1.otm : TestObject/Summary/TestElement", element.getValidationIdentity() );
	}
	
	@Test
	public void testDocumentationFunctions() throws Exception {
		TLCoreObject core = addCore( "TestObject", library1 );
		TLProperty element = addElement( "TestElement", core.getSummaryFacet() );
		
		testDocumentationFunctions( element );
	}
	
	@Test
	public void testEquivalentFunctions() throws Exception {
		TLCoreObject core = addCore( "TestObject", library1 );
		TLProperty element = addElement( "TestElement", core.getSummaryFacet() );
		
		testEquivalentFunctions( element );
	}
	
	@Test
	public void testExampleFunctions() throws Exception {
		TLCoreObject core = addCore( "TestObject", library1 );
		TLProperty element = addElement( "TestElement", core.getSummaryFacet() );
		
		testExampleFunctions( element );
	}
	
}

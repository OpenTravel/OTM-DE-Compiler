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

import javax.xml.XMLConstants;

import org.junit.Test;

/**
 * Verifies the functions of the <code>TLListFacet</code> class.
 */
public class TestTLListFacet extends AbstractModelTest {
	
	@Test
	public void testIdentityFunctions() throws Exception {
		TLCoreObject core = addCore( "TestObject", library1 );
		TLListFacet simpleListFacet = core.getSimpleListFacet();
		TLListFacet summaryListFacet = core.getSummaryListFacet();
		
		assertEquals( library1.getNamespace(), simpleListFacet.getNamespace() );
		assertEquals( core.getName() + "_Simple_List", simpleListFacet.getLocalName() );
		assertEquals( "TestLibrary1.otm : TestObject/Simple-List", simpleListFacet.getValidationIdentity() );
		
		assertEquals( library1.getNamespace(), summaryListFacet.getNamespace() );
		assertEquals( core.getName() + "_Summary_List", summaryListFacet.getLocalName() );
		assertEquals( "TestLibrary1.otm : TestObject/Summary-List", summaryListFacet.getValidationIdentity() );
	}
	
	@Test
	public void testUnsupportedOperations() throws Exception {
		TLCoreObject core = addCore( "TestObject", library1 );
		TLListFacet facet = core.getSimpleListFacet();
		
		testNegativeCase( facet, f -> new TLListFacet( null ), NullPointerException.class );
		testNegativeCase( facet, f -> facet.setFacetType( TLFacetType.SUMMARY ), UnsupportedOperationException.class );
		testNegativeCase( facet, f -> facet.setDocumentation( null ), UnsupportedOperationException.class );
		testNegativeCase( facet, f -> facet.addAlias( null ), UnsupportedOperationException.class );
		testNegativeCase( facet, f -> facet.addAlias( 0, null ), UnsupportedOperationException.class );
		testNegativeCase( facet, f -> facet.removeAlias( null ), UnsupportedOperationException.class );
		testNegativeCase( facet, f -> facet.moveUp( null ), UnsupportedOperationException.class );
		testNegativeCase( facet, f -> facet.moveDown( null ), UnsupportedOperationException.class );
		testNegativeCase( facet, f -> facet.sortAliases( null ), UnsupportedOperationException.class );
	}
	
	@Test
	public void testDeclaresContent() throws Exception {
		TLCoreObject core = addCore( "TestObject", library1 );
		TLListFacet listFacet = core.getSimpleListFacet();
		TLSimpleFacet facet = core.getSimpleFacet();
		
		assertFalse( listFacet.declaresContent() );
		
		facet.setSimpleType( (TLAttributeType) findEntity( XMLConstants.W3C_XML_SCHEMA_NS_URI, "string" ) ); 
		assertTrue( listFacet.declaresContent() );
	}
	
}

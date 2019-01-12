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
 * Verifies the functions of the <code>TLContextualFacet</code> class.
 */
public class TestTLContextualFacet extends AbstractModelTest {
	
	@Test
	@SuppressWarnings("deprecation")
	public void testIdentityFunctions() throws Exception {
		TLChoiceObject choice = addChoice( "TestObject", library1 );
		TLContextualFacet facet = newContextualFacet( "Test", TLFacetType.CHOICE, library1 );
		TLContextualFacet nestedFacet = newContextualFacet( "NestedTest", TLFacetType.CHOICE, library2 );
		
		choice.addChoiceFacet( facet );
		facet.addChildFacet( nestedFacet );
		
		assertEquals( facet.getName(), facet.getLabel() );
		assertEquals( library1.getNamespace(), facet.getNamespace() );
		assertEquals( choice.getName() + "_" + facet.getName(), facet.getLocalName() );
		assertEquals( "TestLibrary1.otm : TestObject_Test", facet.getValidationIdentity() );
		
		assertEquals( library2.getNamespace(), nestedFacet.getNamespace() );
		assertEquals( choice.getName() + "_" + facet.getName() + "_" + nestedFacet.getName(), nestedFacet.getLocalName() );
		assertEquals( "TestLibrary2.otm : TestObject_Test_NestedTest", nestedFacet.getValidationIdentity() );
	}
	
	@Test
	public void testChildFacetFunctions() throws Exception {
		TLChoiceObject choice = addChoice( "TestObject", library1 );
		TLContextualFacet parentFacet = newContextualFacet( "Parent", TLFacetType.CHOICE, library1 );
		TLContextualFacet facet1 = newContextualFacet( "Test1", TLFacetType.CHOICE, library1 );
		TLContextualFacet facet2 = newContextualFacet( "Test2", TLFacetType.CHOICE, library1 );
		
		choice.addChoiceFacet( parentFacet );
		parentFacet.addChildFacet( facet1 );
		parentFacet.addChildFacet( facet2 );
		assertEquals( 2, parentFacet.getAllFacets().size() );
		assertEquals( facet1, parentFacet.getChildFacet( "Test1" ) );
		assertEquals( facet2, parentFacet.getChildFacet( "Test2" ) );
		assertEquals( facet1, parentFacet.getChildFacet( "Test1", library1 ) );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getNames( parentFacet.getChildFacets(), f -> f.getName() ) );
		
		parentFacet.removeChildFacet( facet1 );
		assertArrayEquals( new String[] { "Test2" }, getNames( parentFacet.getChildFacets(), f -> f.getName() ) );
	}
	
}

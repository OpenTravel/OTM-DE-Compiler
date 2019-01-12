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
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * Verifies the functions of the <code>TLChoiceObject</code> class.
 */
public class TestTLChoiceObject extends AbstractModelTest {
	
	@Test
	public void testIdentityFunctions() throws Exception {
		TLChoiceObject choice = addChoice( "TestObject", library1 );
		
		assertEquals( library1.getNamespace(), choice.getNamespace() );
		assertEquals( library1.getBaseNamespace(), choice.getBaseNamespace() );
		assertEquals( choice.getName(), choice.getLocalName() );
		assertEquals( library1.getVersion(), choice.getVersion() );
		assertEquals( "TestLibrary1.otm : TestObject", choice.getValidationIdentity() );
		assertEquals( VersionSchemeFactory.getInstance().getDefaultVersionScheme(), choice.getVersionScheme() );
	}
	
	@Test
	public void testExtensionFunctions() throws Exception {
		TLChoiceObject choice1 = addChoice( "TestObject1", library1 );
		TLChoiceObject choice2 = addChoice( "TestObject2", library1 );
		
		addExtension( choice2, choice1 );
		testExtensionFunctions( choice2 );
	}
	
	@Test
	public void testAliasFunctions() throws Exception {
		testAliasFunctions( addChoice( "TestObject", library1 ) );
	}
	
	@Test
	public void testDocumentationFunctions() throws Exception {
		testDocumentationFunctions( addChoice( "TestObject", library1 ) );
	}
	
	@Test
	public void testEquivalentFunctions() throws Exception {
		testEquivalentFunctions( addChoice( "TestObject", library1 ) );
	}
	
	@Test
	public void testNonContextualFacetFunctions() throws Exception {
		TLChoiceObject choice = addChoice( "TestObject", library1 );
		TLChoiceObject orphanChoice = new TLChoiceObject();
		TLFacet origShared = choice.getSharedFacet();
		TLFacet orphanShared = orphanChoice.getSharedFacet();
		
		choice.setSharedFacet( origShared );
		assertEquals( origShared, choice.getSharedFacet() );
		testNegativeCase( choice, e -> choice.setSharedFacet( null ), IllegalStateException.class );
		orphanChoice.setSharedFacet( null );
		assertNull( orphanChoice.getSharedFacet() );
		assertNull( orphanShared.getFacetType() );
		assertNull( orphanShared.getOwningEntity() );
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testChoiceFacetFunctions() throws Exception {
		TLChoiceObject choice = addChoice( "TestObject", library1 );
		TLContextualFacet facet1 = newContextualFacet( "Test1", TLFacetType.CHOICE, library1 );
		TLContextualFacet facet2 = newContextualFacet( "Test2", TLFacetType.CHOICE, library1 );
		
		choice.addChoiceFacet( facet1 );
		choice.addChoiceFacet( 1, facet2 );
		assertEquals( 3, choice.getAllFacets().size() );
		assertEquals( facet1, choice.getChoiceFacet( "Test1" ) );
		assertEquals( facet2, choice.getChoiceFacet( "Test2" ) );
		assertEquals( facet1, choice.getChoiceFacet( "Test1", library1 ) );
		assertEquals( facet1, choice.getChoiceFacet( null, "Test1" ) );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getNames( choice.getChoiceFacets(), f -> f.getName() ) );
		
		choice.moveChoiceFacetDown( facet1 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getNames( choice.getChoiceFacets(), f -> f.getName() ) );
		
		choice.sortChoiceFacets( (f1, f2) -> f1.getName().compareTo( f2.getName() ) );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getNames( choice.getChoiceFacets(), f -> f.getName() ) );
		
		choice.moveChoiceFacetUp( facet2 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getNames( choice.getChoiceFacets(), f -> f.getName() ) );
		
		choice.removeChoiceFacet( facet1 );
		assertArrayEquals( new String[] { "Test2" }, getNames( choice.getChoiceFacets(), f -> f.getName() ) );
	}
	
}

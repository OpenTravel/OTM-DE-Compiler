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
 * Verifies the functions of the <code>TLBusinessObject</code> class.
 */
public class TestTLBusinessObject extends AbstractModelTest {
	
	@Test
	public void testIdentityFunctions() throws Exception {
		TLBusinessObject bo = addBusinessObject( "TestObject", library1 );
		
		assertEquals( library1.getNamespace(), bo.getNamespace() );
		assertEquals( library1.getBaseNamespace(), bo.getBaseNamespace() );
		assertEquals( bo.getName(), bo.getLocalName() );
		assertEquals( library1.getVersion(), bo.getVersion() );
		assertEquals( "TestLibrary1.otm : TestObject", bo.getValidationIdentity() );
		assertEquals( VersionSchemeFactory.getInstance().getDefaultVersionScheme(), bo.getVersionScheme() );
	}
	
	@Test
	public void testExtensionFunctions() throws Exception {
		TLBusinessObject bo1 = addBusinessObject( "TestObject1", library1 );
		TLBusinessObject bo2 = addBusinessObject( "TestObject2", library1 );
		
		addExtension( bo2, bo1 );
		testExtensionFunctions( bo2 );
	}
	
	@Test
	public void testAliasFunctions() throws Exception {
		testAliasFunctions( addBusinessObject( "TestObject", library1 ) );
	}
	
	@Test
	public void testDocumentationFunctions() throws Exception {
		testDocumentationFunctions( addBusinessObject( "TestObject", library1 ) );
	}
	
	@Test
	public void testEquivalentFunctions() throws Exception {
		testEquivalentFunctions( addBusinessObject( "TestObject", library1 ) );
	}
	
	@Test
	public void testNonContextualFacetFunctions() throws Exception {
		TLBusinessObject bo = addBusinessObject( "TestObject", library1 );
		TLBusinessObject orphanBO = new TLBusinessObject();
		TLFacet origId = bo.getIdFacet();
		TLFacet orphanId = orphanBO.getIdFacet();
		TLFacet origSummary = bo.getSummaryFacet();
		TLFacet orphanSummary = orphanBO.getSummaryFacet();
		TLFacet origDetail = bo.getDetailFacet();
		TLFacet orphanDetail = orphanBO.getDetailFacet();
		
		bo.setIdFacet( origId );
		assertEquals( origId, bo.getIdFacet() );
		testNegativeCase( bo, e -> bo.setIdFacet( null ), IllegalStateException.class );
		orphanBO.setIdFacet( null );
		assertNull( orphanBO.getIdFacet() );
		assertNull( orphanId.getFacetType() );
		assertNull( orphanId.getOwningEntity() );
		
		bo.setSummaryFacet( origSummary );
		assertEquals( origSummary, bo.getSummaryFacet() );
		testNegativeCase( bo, e -> bo.setSummaryFacet( null ), IllegalStateException.class );
		orphanBO.setSummaryFacet( null );
		assertNull( orphanBO.getSummaryFacet() );
		assertNull( orphanSummary.getFacetType() );
		assertNull( orphanSummary.getOwningEntity() );
		
		bo.setDetailFacet( origDetail );
		assertEquals( origDetail, bo.getDetailFacet() );
		testNegativeCase( bo, e -> bo.setDetailFacet( null ), IllegalStateException.class );
		orphanBO.setDetailFacet( null );
		assertNull( orphanBO.getDetailFacet() );
		assertNull( orphanDetail.getFacetType() );
		assertNull( orphanDetail.getOwningEntity() );
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testCustomFacetFunctions() throws Exception {
		TLBusinessObject bo = addBusinessObject( "TestObject", library1 );
		TLContextualFacet facet1 = newContextualFacet( "Test1", TLFacetType.CUSTOM, library1 );
		TLContextualFacet facet2 = newContextualFacet( "Test2", TLFacetType.CUSTOM, library1 );
		
		bo.addCustomFacet( facet1 );
		bo.addCustomFacet( 1, facet2 );
		assertEquals( 5, bo.getAllFacets().size() );
		assertEquals( facet1, bo.getCustomFacet( "Test1" ) );
		assertEquals( facet2, bo.getCustomFacet( "Test2" ) );
		assertEquals( facet1, bo.getCustomFacet( "Test1", library1 ) );
		assertEquals( facet1, bo.getCustomFacet( null, "Test1" ) );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getNames( bo.getCustomFacets(), f -> f.getName() ) );
		
		bo.moveCustomFacetDown( facet1 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getNames( bo.getCustomFacets(), f -> f.getName() ) );
		
		bo.sortCustomFacets( (f1, f2) -> f1.getName().compareTo( f2.getName() ) );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getNames( bo.getCustomFacets(), f -> f.getName() ) );
		
		bo.moveCustomFacetUp( facet2 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getNames( bo.getCustomFacets(), f -> f.getName() ) );
		
		bo.removeCustomFacet( facet1 );
		assertArrayEquals( new String[] { "Test2" }, getNames( bo.getCustomFacets(), f -> f.getName() ) );
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testQueryFacetFunctions() throws Exception {
		TLBusinessObject bo = addBusinessObject( "TestObject", library1 );
		TLContextualFacet facet1 = newContextualFacet( "Test1", TLFacetType.QUERY, library1 );
		TLContextualFacet facet2 = newContextualFacet( "Test2", TLFacetType.QUERY, library1 );
		
		bo.addQueryFacet( facet1 );
		bo.addQueryFacet( 1, facet2 );
		assertEquals( 5, bo.getAllFacets().size() );
		assertEquals( facet1, bo.getQueryFacet( "Test1" ) );
		assertEquals( facet2, bo.getQueryFacet( "Test2" ) );
		assertEquals( facet1, bo.getQueryFacet( "Test1", library1 ) );
		assertEquals( facet1, bo.getQueryFacet( null, "Test1" ) );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getNames( bo.getQueryFacets(), f -> f.getName() ) );
		
		bo.moveQueryFacetDown( facet1 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getNames( bo.getQueryFacets(), f -> f.getName() ) );
		
		bo.sortQueryFacets( (f1, f2) -> f1.getName().compareTo( f2.getName() ) );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getNames( bo.getQueryFacets(), f -> f.getName() ) );
		
		bo.moveQueryFacetUp( facet2 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getNames( bo.getQueryFacets(), f -> f.getName() ) );
		
		bo.removeQueryFacet( facet1 );
		assertArrayEquals( new String[] { "Test2" }, getNames( bo.getQueryFacets(), f -> f.getName() ) );
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testUpdateFacetFunctions() throws Exception {
		TLBusinessObject bo = addBusinessObject( "TestObject", library1 );
		TLContextualFacet facet1 = newContextualFacet( "Test1", TLFacetType.UPDATE, library1 );
		TLContextualFacet facet2 = newContextualFacet( "Test2", TLFacetType.UPDATE, library1 );
		
		bo.addUpdateFacet( facet1 );
		bo.addUpdateFacet( 1, facet2 );
		assertEquals( 5, bo.getAllFacets().size() );
		assertEquals( facet1, bo.getUpdateFacet( "Test1" ) );
		assertEquals( facet2, bo.getUpdateFacet( "Test2" ) );
		assertEquals( facet1, bo.getUpdateFacet( "Test1", library1 ) );
		assertEquals( facet1, bo.getUpdateFacet( null, "Test1" ) );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getNames( bo.getUpdateFacets(), f -> f.getName() ) );
		
		bo.moveUpdateFacetDown( facet1 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getNames( bo.getUpdateFacets(), f -> f.getName() ) );
		
		bo.sortUpdateFacets( (f1, f2) -> f1.getName().compareTo( f2.getName() ) );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getNames( bo.getUpdateFacets(), f -> f.getName() ) );
		
		bo.moveUpdateFacetUp( facet2 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getNames( bo.getUpdateFacets(), f -> f.getName() ) );
		
		bo.removeUpdateFacet( facet1 );
		assertArrayEquals( new String[] { "Test2" }, getNames( bo.getUpdateFacets(), f -> f.getName() ) );
	}
	
}

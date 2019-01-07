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
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * Verifies the functions of the <code>TLCoreObject</code> class.
 */
public class TestTLCoreObject extends AbstractModelTest {
	
	@Test
	public void testIdentityFunctions() throws Exception {
		TLCoreObject core = addCore( "TestObject", library1 );
		
		assertEquals( library1.getNamespace(), core.getNamespace() );
		assertEquals( library1.getBaseNamespace(), core.getBaseNamespace() );
		assertEquals( core.getName(), core.getLocalName() );
		assertEquals( "TestLibrary1.otm : TestObject", core.getValidationIdentity() );
		assertEquals( VersionSchemeFactory.getInstance().getDefaultVersionScheme(), core.getVersionScheme() );
	}
	
	@Test
	public void testNonContextualFacetFunctions() throws Exception {
		TLCoreObject core = addCore( "TestObject", library1 );
		TLCoreObject orphanCore = new TLCoreObject();
		TLSimpleFacet origSimple = core.getSimpleFacet();
		TLSimpleFacet orphanSimple = orphanCore.getSimpleFacet();
		TLFacet origSummary = core.getSummaryFacet();
		TLFacet orphanSummary = orphanCore.getSummaryFacet();
		TLFacet origDetail = core.getDetailFacet();
		TLFacet orphanDetail = orphanCore.getDetailFacet();
		
		assertEquals( 2, core.getAllFacets().size() );
		
		core.setSimpleFacet( origSimple );
		assertEquals( origSimple, core.getSimpleFacet() );
		testNegativeCase( core, e -> core.setSimpleFacet( null ), IllegalStateException.class );
		orphanCore.setSimpleFacet( null );
		assertNull( orphanCore.getSimpleFacet() );
		assertNull( orphanSimple.getFacetType() );
		assertNull( orphanSimple.getOwningEntity() );
		
		core.setSummaryFacet( origSummary );
		assertEquals( origSummary, core.getSummaryFacet() );
		testNegativeCase( core, e -> core.setSummaryFacet( null ), IllegalStateException.class );
		orphanCore.setSummaryFacet( null );
		assertNull( orphanCore.getSummaryFacet() );
		assertNull( orphanSummary.getFacetType() );
		assertNull( orphanSummary.getOwningEntity() );
		
		core.setDetailFacet( origDetail );
		assertEquals( origDetail, core.getDetailFacet() );
		testNegativeCase( core, e -> core.setDetailFacet( null ), IllegalStateException.class );
		orphanCore.setDetailFacet( null );
		assertNull( orphanCore.getDetailFacet() );
		assertNull( orphanDetail.getFacetType() );
		assertNull( orphanDetail.getOwningEntity() );
	}
	
	@Test
	public void testAliasFunctions() throws Exception {
		testAliasOwnerFunctions( addCore( "TestObject", library1 ) );
	}
	
}

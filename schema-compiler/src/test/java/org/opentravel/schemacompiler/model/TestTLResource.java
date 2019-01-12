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
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * Verifies the functions of the <code>TLResource</code> class.
 */
public class TestTLResource extends AbstractModelTest {
	
	@Test
	public void testIdentityFunctions() throws Exception {
		TLResource resource = addResource( "TestResource", library1 );
		
		assertEquals( library1.getNamespace(), resource.getNamespace() );
		assertEquals( library1.getBaseNamespace(), resource.getBaseNamespace() );
		assertEquals( resource.getName(), resource.getLocalName() );
		assertEquals( library1.getVersion(), resource.getVersion() );
		assertEquals( "TestLibrary1.otm : TestResource", resource.getValidationIdentity() );
		assertEquals( VersionSchemeFactory.getInstance().getDefaultVersionScheme(), resource.getVersionScheme() );
	}
	
	@Test
	public void testExtensionFunctions() throws Exception {
		TLResource resource1 = addResource( "TestResource1", library1 );
		TLResource resource2 = addResource( "TestResource2", library1 );
		
		addExtension( resource2, resource1 );
		testExtensionFunctions( resource2 );
	}
	
	@Test
	public void testDocumentationFunctions() throws Exception {
		testDocumentationFunctions( addResource( "TestResource", library1 ) );
	}
	
	@Test
	public void testParentRefFunctions() throws Exception {
		TLResource parentResource1 = addResource( "ParentResource1", library1 );
		TLParamGroup paramGroup1 = addParamGroup( "TestParamGroup1",
				parentResource1.getBusinessObjectRef().getIdFacet(), parentResource1 );
		TLResource parentResource2 = addResource( "ParentResource2", library1 );
		TLParamGroup paramGroup2 = addParamGroup( "TestParamGroup2",
				parentResource2.getBusinessObjectRef().getIdFacet(), parentResource2 );
		TLResource resource = addResource( "TestResource", library1 );
		TLResourceParentRef parentRef1 = addParentRef( parentResource1, paramGroup1, resource );
		TLResourceParentRef parentRef2 = addParentRef( parentResource2, paramGroup2, resource );
		
		assertArrayEquals( new String[] { "ParentResource1", "ParentResource2" },
				getNames( resource.getParentRefs(), p -> p.getParentResource().getName() ) );
		
		resource.moveDown( parentRef1 );
		assertArrayEquals( new String[] { "ParentResource2", "ParentResource1" },
				getNames( resource.getParentRefs(), p -> p.getParentResource().getName() ) );
		
		resource.sortParentRefs( (p1, p2) -> p1.getParentResource().getName().compareTo( p2.getParentResource().getName() ) );
		assertArrayEquals( new String[] { "ParentResource1", "ParentResource2" },
				getNames( resource.getParentRefs(), p -> p.getParentResource().getName() ) );
		
		resource.moveUp( parentRef2 );
		assertArrayEquals( new String[] { "ParentResource2", "ParentResource1" },
				getNames( resource.getParentRefs(), p -> p.getParentResource().getName() ) );
		
		resource.removeParentRef( parentRef1 );
		assertArrayEquals( new String[] { "ParentResource2" },
				getNames( resource.getParentRefs(), p -> p.getParentResource().getName() ) );
	}
	
	@Test
	public void testParamGroupFunctions() throws Exception {
		TLResource resource = addResource( "TestResource", library1 );
		TLParamGroup paramGroup1 = addParamGroup( "ParamGroup1",
				resource.getBusinessObjectRef().getIdFacet(), resource );
		TLParamGroup paramGroup2 = addParamGroup( "ParamGroup2",
				resource.getBusinessObjectRef().getIdFacet(), resource );
		
		assertArrayEquals( new String[] { "ParamGroup1", "ParamGroup2" }, getNames( resource.getParamGroups(), p -> p.getName() ) );
		
		resource.moveDown( paramGroup1 );
		assertArrayEquals( new String[] { "ParamGroup2", "ParamGroup1" }, getNames( resource.getParamGroups(), p -> p.getName() ) );
		
		resource.sortParamGroups( (p1, p2) -> p1.getName().compareTo( p2.getName() ) );
		assertArrayEquals( new String[] { "ParamGroup1", "ParamGroup2" }, getNames( resource.getParamGroups(), p -> p.getName() ) );
		
		resource.moveUp( paramGroup2 );
		assertArrayEquals( new String[] { "ParamGroup2", "ParamGroup1" }, getNames( resource.getParamGroups(), p -> p.getName() ) );
		
		resource.removeParamGroup( paramGroup1 );
		assertArrayEquals( new String[] { "ParamGroup2" }, getNames( resource.getParamGroups(), p -> p.getName() ) );
	}
	
	@Test
	public void testActionFacetFunctions() throws Exception {
		TLResource resource = addResource( "TestResource", library1 );
		TLActionFacet actionFacet1 = addActionFacet( "ActionFacet1", TLReferenceType.OPTIONAL, "Summary", 0, null, resource );
		TLActionFacet actionFacet2 = addActionFacet( "ActionFacet2", TLReferenceType.REQUIRED, "Summary", 0, null, resource );
		
		assertArrayEquals( new String[] { "ActionFacet1", "ActionFacet2" }, getNames( resource.getActionFacets(), p -> p.getName() ) );
		
		resource.moveDown( actionFacet1 );
		assertArrayEquals( new String[] { "ActionFacet2", "ActionFacet1" }, getNames( resource.getActionFacets(), p -> p.getName() ) );
		
		resource.sortActionFacets( (p1, p2) -> p1.getName().compareTo( p2.getName() ) );
		assertArrayEquals( new String[] { "ActionFacet1", "ActionFacet2" }, getNames( resource.getActionFacets(), p -> p.getName() ) );
		
		resource.moveUp( actionFacet2 );
		assertArrayEquals( new String[] { "ActionFacet2", "ActionFacet1" }, getNames( resource.getActionFacets(), p -> p.getName() ) );
		
		resource.removeActionFacet( actionFacet1 );
		assertArrayEquals( new String[] { "ActionFacet2" }, getNames( resource.getActionFacets(), p -> p.getName() ) );
	}
	
	@Test
	public void testActionFunctions() throws Exception {
		TLResource resource = addResource( "TestResource", library1 );
		TLAction action1 = addAction( "Action1", resource );
		TLAction action2 = addAction( "Action2", resource );
		
		assertArrayEquals( new String[] { "Action1", "Action2" }, getNames( resource.getActions(), a -> a.getActionId() ) );
		
		resource.moveDown( action1 );
		assertArrayEquals( new String[] { "Action2", "Action1" }, getNames( resource.getActions(), a -> a.getActionId() ) );
		
		resource.sortActions( (a1, a2) -> a1.getActionId().compareTo( a2.getActionId() ) );
		assertArrayEquals( new String[] { "Action1", "Action2" }, getNames( resource.getActions(), a -> a.getActionId() ) );
		
		resource.moveUp( action2 );
		assertArrayEquals( new String[] { "Action2", "Action1" }, getNames( resource.getActions(), a -> a.getActionId() ) );
		
		resource.removeAction( action1 );
		assertArrayEquals( new String[] { "Action2" }, getNames( resource.getActions(), a -> a.getActionId() ) );
	}
	
}

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

/**
 * Verifies the functions of the <code>TLFolder</code> class.
 */
public class TestTLFolder extends AbstractModelTest {
	
	@Test
	public void testIdentityFunctions() throws Exception {
		TLFolder folder = addFolder( "TestFolder", library1, library1 );
		
		assertEquals( library1, folder.getOwningLibrary() );
		assertEquals( "TestLibrary1.otm : TestFolder", folder.getValidationIdentity() );
	}
	
	@Test
	public void testFolderStructure() throws Exception {
		TLChoiceObject choice1 = addChoice( "TestChoice1", library1 );
		TLChoiceObject choice2 = addChoice( "TestChoice2", library2 );
		TLContextualFacet localFacet = newContextualFacet( "Local", TLFacetType.CHOICE, library1 );
		TLContextualFacet nonLocalFacet = newContextualFacet( "NonLocal", TLFacetType.CHOICE, library1 );
		TLFolder parentFolder1 = addFolder( "ParentFolder1", library1, library1 );
		TLFolder parentFolder2 = addFolder( "ParentFolder2", library1, library1 );
		TLFolder childFolder1 = addFolder( "ChildFolder1", parentFolder1, library1 );
		TLFolder childFolder2 = addFolder( "ChildFolder2", parentFolder1, library1 );
		TLFolder library2Folder = addFolder( "ParentFolder", library2, library2 );
		TLFolder duplicateParent = new TLFolder( library1 );
		TLFolder duplicateChild = new TLFolder( library1 );
		TLFolder childFolder3 = new TLFolder( library1 );
		
		choice1.addChoiceFacet( localFacet );
		choice2.addChoiceFacet( nonLocalFacet );
		childFolder1.addEntity( choice1 );
		childFolder2.addEntity( nonLocalFacet );
		childFolder3.setName( "ChildFolder3" );
		duplicateParent.setName( parentFolder1.getName() );
		duplicateChild.setName( childFolder1.getName() );
		
		assertEquals( 0, library1.getUnfolderedMembers().size() );
		assertEquals( 2, parentFolder1.getFolders().size() );
		assertEquals( 0, parentFolder1.getEntities().size() );
		assertEquals( 0, childFolder1.getFolders().size() );
		assertEquals( 0, childFolder2.getFolders().size() );
		assertEquals( 1, childFolder1.getEntities().size() );
		assertEquals( 1, childFolder2.getEntities().size() );
		assertEquals( parentFolder1, library1.getFolder( parentFolder1.getName() ) );
		assertEquals( childFolder1, parentFolder1.getFolder( childFolder1.getName() ) );
		assertEquals( nonLocalFacet, childFolder2.getEntity( "TestChoice2_NonLocal" ) );
		childFolder2.removeEntity( nonLocalFacet );
		assertNull( childFolder2.getEntity( "TestChoice2_NonLocal" ) );
		
		parentFolder1.addFolder( childFolder3 );
		assertEquals( 3, parentFolder1.getFolders().size() );
		assertEquals( childFolder3, parentFolder1.getFolder( childFolder3.getName() ) );
		parentFolder1.removeFolder( childFolder3 );
		assertEquals( 2, parentFolder1.getFolders().size() );
		assertNull( parentFolder1.getFolder( childFolder3.getName() ) );
		
		assertEquals( parentFolder1, library1.getFolders().get( 0 ) );
		parentFolder2.setName( "AParentFolder2" ); // Forces resort of folder list
		assertEquals( parentFolder2, library1.getFolders().get( 0 ) );
		
		assertEquals( childFolder1, parentFolder1.getFolders().get( 0 ) );
		childFolder2.setName( "AChildFolder2" ); // Forces resort of folder list
		assertEquals( childFolder2, parentFolder1.getFolders().get( 0 ) );
		
		testNegativeCase( parentFolder1, f -> childFolder1.setName( null ), IllegalArgumentException.class );
		testNegativeCase( parentFolder1, f -> childFolder1.setName( childFolder2.getName() ), IllegalArgumentException.class );
		
		testNegativeCase( parentFolder1, f -> library1.addFolder( null ), IllegalArgumentException.class );
		testNegativeCase( parentFolder1, f -> library1.addFolder( library2Folder ), IllegalArgumentException.class );
		testNegativeCase( parentFolder1, f -> library1.addFolder( duplicateParent ), IllegalArgumentException.class );
		
		testNegativeCase( parentFolder1, f -> childFolder1.addFolder( null ), IllegalArgumentException.class );
		testNegativeCase( parentFolder1, f -> childFolder1.addFolder( parentFolder1 ), IllegalArgumentException.class );
		testNegativeCase( parentFolder1, f -> childFolder1.addFolder( library2Folder ), IllegalArgumentException.class );
		testNegativeCase( parentFolder1, f -> parentFolder1.addFolder( duplicateChild ), IllegalArgumentException.class );
		
		testNegativeCase( parentFolder1, f -> childFolder1.addEntity( null ), IllegalArgumentException.class );
		testNegativeCase( parentFolder1, f -> childFolder1.addEntity( new TLService() ), IllegalArgumentException.class );
		testNegativeCase( parentFolder1, f -> childFolder1.addEntity( localFacet ), IllegalArgumentException.class );
		testNegativeCase( parentFolder1, f -> childFolder1.addEntity( choice2 ), IllegalArgumentException.class );
		testNegativeCase( parentFolder1, f -> childFolder1.addEntity( choice1 ), IllegalArgumentException.class );
	}
	
	private TLFolder addFolder(String name, TLFolderOwner parentFolder, TLLibrary library) {
		TLFolder folder = new TLFolder( library );
		
		folder.setName( name );
		parentFolder.addFolder( folder );
		return folder;
	}
	
}

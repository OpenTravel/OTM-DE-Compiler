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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Verifies the functions of the <code>TLDocumentation</code> class.
 */
public class TestTLDocumentation extends AbstractModelTest {
	
	@Test
	public void testIdentityFunctions() throws Exception {
		TLChoiceObject choice = addChoice( "TestObject", library1 );
		TLDocumentation doc = newDocumentation( choice );
		TLDocumentationItem item = newDocItem( TLDocumentationType.DEPRECATION );
		
		doc.addDeprecation( item );
		assertEquals( library1, doc.getOwningLibrary() );
		assertEquals( model, doc.getOwningModel() );
		assertEquals( library1, item.getOwningLibrary() );
		assertEquals( model, item.getOwningModel() );
		assertEquals( doc, item.getOwningDocumentation() );
		
		assertEquals( "TestLibrary1.otm : TestObject/Documentation", doc.getValidationIdentity() );
		assertEquals( "TestLibrary1.otm : TestObject/Documentation/Deprecation", item.getValidationIdentity() );
	}
	
	@Test
	public void testIsEmpty() {
		TLDocumentation doc = new TLDocumentation();
		TLAdditionalDocumentationItem otherItem;
		TLDocumentationItem item;
		
		doc.setDescription( "Test Description" );
		assertFalse( doc.isEmpty() );
		doc.setDescription( null );
		assertTrue( doc.isEmpty() );
		
		doc.addDeprecation( 0, item = newDocItem( TLDocumentationType.DEPRECATION ) );
		assertFalse( doc.isEmpty() );
		doc.removeDeprecation( item );
		assertTrue( doc.isEmpty() );
		
		doc.addImplementer( 0, item = newDocItem( TLDocumentationType.IMPLEMENTER ) );
		assertFalse( doc.isEmpty() );
		doc.removeImplementer( item );
		assertTrue( doc.isEmpty() );
		
		doc.addReference( 0, item = newDocItem( TLDocumentationType.REFERENCE ) );
		assertFalse( doc.isEmpty() );
		doc.removeReference( item );
		assertTrue( doc.isEmpty() );
		
		doc.addMoreInfo( 0, item = newDocItem( TLDocumentationType.MORE_INFO ) );
		assertFalse( doc.isEmpty() );
		doc.removeMoreInfo( item );
		assertTrue( doc.isEmpty() );
		
		doc.addOtherDoc( 0, otherItem = newOtherDocItem() );
		assertFalse( doc.isEmpty() );
		doc.removeOtherDoc( otherItem );
		assertTrue( doc.isEmpty() );
	}
	
	@Test
	public void testDeprecationFunctions() throws Exception {
		TLChoiceObject choice = addChoice( "TestObject", library1 );
		TLDocumentation doc = newDocumentation( choice );
		TLDocumentationItem item1 = newDocItem( TLDocumentationType.DEPRECATION );
		TLDocumentationItem item2 = newDocItem( TLDocumentationType.DEPRECATION );
		
		item1.setText( "Test1" );
		item2.setText( "Test2" );
		doc.addDeprecation( item1 );
		doc.addDeprecation( item2 );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getTextValues( doc.getDeprecations() ) );
		
		doc.moveDeprecationDown( item1 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getTextValues( doc.getDeprecations() ) );
		
		doc.sortDeprecations( (i1, i2) -> i1.getText().compareTo( i2.getText() ) );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getTextValues( doc.getDeprecations() ) );
		
		doc.moveDeprecationUp( item2 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getTextValues( doc.getDeprecations() ) );
		
		doc.removeDeprecation( item1 );
		assertArrayEquals( new String[] { "Test2" }, getTextValues( doc.getDeprecations() ) );
	}
	
	@Test
	public void testImplementerFunctions() throws Exception {
		TLChoiceObject choice = addChoice( "TestObject", library1 );
		TLDocumentation doc = newDocumentation( choice );
		TLDocumentationItem item1 = newDocItem( TLDocumentationType.IMPLEMENTER );
		TLDocumentationItem item2 = newDocItem( TLDocumentationType.IMPLEMENTER );
		
		item1.setText( "Test1" );
		item2.setText( "Test2" );
		doc.addImplementer( item1 );
		doc.addImplementer( item2 );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getTextValues( doc.getImplementers() ) );
		
		doc.moveImplementerDown( item1 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getTextValues( doc.getImplementers() ) );
		
		doc.sortImplementers( (i1, i2) -> i1.getText().compareTo( i2.getText() ) );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getTextValues( doc.getImplementers() ) );
		
		doc.moveImplementerUp( item2 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getTextValues( doc.getImplementers() ) );
		
		doc.removeImplementer( item1 );
		assertArrayEquals( new String[] { "Test2" }, getTextValues( doc.getImplementers() ) );
	}
	
	@Test
	public void testReferenceFunctions() throws Exception {
		TLChoiceObject choice = addChoice( "TestObject", library1 );
		TLDocumentation doc = newDocumentation( choice );
		TLDocumentationItem item1 = newDocItem( TLDocumentationType.REFERENCE );
		TLDocumentationItem item2 = newDocItem( TLDocumentationType.REFERENCE );
		
		item1.setText( "Test1" );
		item2.setText( "Test2" );
		doc.addReference( item1 );
		doc.addReference( item2 );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getTextValues( doc.getReferences() ) );
		
		doc.moveReferenceDown( item1 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getTextValues( doc.getReferences() ) );
		
		doc.sortReferences( (i1, i2) -> i1.getText().compareTo( i2.getText() ) );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getTextValues( doc.getReferences() ) );
		
		doc.moveReferenceUp( item2 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getTextValues( doc.getReferences() ) );
		
		doc.removeReference( item1 );
		assertArrayEquals( new String[] { "Test2" }, getTextValues( doc.getReferences() ) );
	}
	
	@Test
	public void testMoreInfoFunctions() throws Exception {
		TLChoiceObject choice = addChoice( "TestObject", library1 );
		TLDocumentation doc = newDocumentation( choice );
		TLDocumentationItem item1 = newDocItem( TLDocumentationType.MORE_INFO );
		TLDocumentationItem item2 = newDocItem( TLDocumentationType.MORE_INFO );
		
		item1.setText( "Test1" );
		item2.setText( "Test2" );
		doc.addMoreInfo( item1 );
		doc.addMoreInfo( item2 );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getTextValues( doc.getMoreInfos() ) );
		
		doc.moveMoreInfoDown( item1 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getTextValues( doc.getMoreInfos() ) );
		
		doc.sortMoreInfo( (i1, i2) -> i1.getText().compareTo( i2.getText() ) );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getTextValues( doc.getMoreInfos() ) );
		
		doc.moveMoreInfoUp( item2 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getTextValues( doc.getMoreInfos() ) );
		
		doc.removeMoreInfo( item1 );
		assertArrayEquals( new String[] { "Test2" }, getTextValues( doc.getMoreInfos() ) );
	}
	
	@Test
	public void testOtherDocFunctions() throws Exception {
		TLChoiceObject choice = addChoice( "TestObject", library1 );
		TLDocumentation doc = newDocumentation( choice );
		TLAdditionalDocumentationItem item1 = newOtherDocItem();
		TLAdditionalDocumentationItem item2 = newOtherDocItem();
		
		item1.setText( "Test1" );
		item2.setText( "Test2" );
		doc.addOtherDoc( item1 );
		doc.addOtherDoc( item2 );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getTextValues( doc.getOtherDocs() ) );
		
		doc.moveOtherDocDown( item1 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getTextValues( doc.getOtherDocs() ) );
		
		doc.sortOtherDoc( (i1, i2) -> i1.getText().compareTo( i2.getText() ) );
		assertArrayEquals( new String[] { "Test1", "Test2" }, getTextValues( doc.getOtherDocs() ) );
		
		doc.moveOtherDocUp( item2 );
		assertArrayEquals( new String[] { "Test2", "Test1" }, getTextValues( doc.getOtherDocs() ) );
		
		doc.removeOtherDoc( item1 );
		assertArrayEquals( new String[] { "Test2" }, getTextValues( doc.getOtherDocs() ) );
	}
	
	private TLDocumentation newDocumentation(TLDocumentationOwner owner) {
		TLDocumentation doc = new TLDocumentation();
		
		doc.setOwner( owner );
		doc.setDescription( "TestDescription" );
		return doc;
	}
	
	private TLDocumentationItem newDocItem(TLDocumentationType docType) {
		TLDocumentationItem item = new TLDocumentationItem();
		
		item.setType( docType );
		item.setText( "Test - " + docType.toString() );
		return item;
	}
	
	private TLAdditionalDocumentationItem newOtherDocItem() {
		TLAdditionalDocumentationItem item = new TLAdditionalDocumentationItem();
		TLDocumentationType docType = TLDocumentationType.OTHER_DOC;
		
		item.setType( docType );
		item.setContext( "TestContext" );
		item.setText( "Test - " + docType.toString() );
		return item;
	}
	
	private String[] getTextValues(List<? extends TLDocumentationItem> itemList) {
		List<String> textValues = new ArrayList<>();
		
		itemList.forEach( i -> textValues.add( i.getText() ) );
		return textValues.toArray( new String[ textValues.size() ] );
	}
	
}

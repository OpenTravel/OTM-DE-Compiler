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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * Verifies the functions of the <code>TLClosedEnumeration</code> and <code>TLOpenEnumeration</code> classes.
 */
public class TestTLEnumerationTypes extends AbstractModelTest {
	
	@Test
	public void testClosedEnumIdentityFunctions() throws Exception {
		TLClosedEnumeration closedEnum = addClosedEnum( "TestObject", library1 );
		
		assertEquals( library1.getNamespace(), closedEnum.getNamespace() );
		assertEquals( library1.getBaseNamespace(), closedEnum.getBaseNamespace() );
		assertEquals( closedEnum.getName(), closedEnum.getLocalName() );
		assertEquals( library1.getVersion(), closedEnum.getVersion() );
		assertEquals( "TestLibrary1.otm : TestObject", closedEnum.getValidationIdentity() );
		assertEquals( VersionSchemeFactory.getInstance().getDefaultVersionScheme(), closedEnum.getVersionScheme() );
	}
	
	@Test
	public void testOpenEnumIdentityFunctions() throws Exception {
		TLOpenEnumeration openEnum = addOpenEnum( "TestObject", library1 );
		
		assertEquals( library1.getNamespace(), openEnum.getNamespace() );
		assertEquals( library1.getBaseNamespace(), openEnum.getBaseNamespace() );
		assertEquals( openEnum.getName(), openEnum.getLocalName() );
		assertEquals( library1.getVersion(), openEnum.getVersion() );
		assertEquals( "TestLibrary1.otm : TestObject", openEnum.getValidationIdentity() );
		assertEquals( VersionSchemeFactory.getInstance().getDefaultVersionScheme(), openEnum.getVersionScheme() );
	}
	
	@Test
	public void testEnumValueIdentityFunctions() throws Exception {
		TLClosedEnumeration closedEnum = addClosedEnum( "TestObject", library1 );
		TLEnumValue value = addEnumValue( "Value", closedEnum );
		
		assertEquals( "TestLibrary1.otm : TestObject/Value", value.getValidationIdentity() );
	}
	
	@Test
	public void testExtensionFunctions() throws Exception {
		TLClosedEnumeration enum1 = addClosedEnum( "TestObject1", library1 );
		TLClosedEnumeration enum2 = addClosedEnum( "TestObject2", library1 );
		
		addExtension( enum2, enum1 );
		testExtensionFunctions( enum2 );
	}
	
	@Test
	public void testDocumentationFunctions() throws Exception {
		testDocumentationFunctions( addClosedEnum( "TestObject", library1 ) );
	}
	
	@Test
	public void testValueFunctions() throws Exception {
		TLClosedEnumeration closedEnum = addClosedEnum( "TestObject", library1 );
		TLEnumValue value1 = addEnumValue( "Value1", closedEnum );
		TLEnumValue value2 = addEnumValue( "Value2", closedEnum );
		
		assertArrayEquals( new String[] { "Value1", "Value2" }, getNames( closedEnum.getValues(), v -> v.getLiteral() ) );
		
		value1.moveDown();
		assertArrayEquals( new String[] { "Value2", "Value1" }, getNames( closedEnum.getValues(), v -> v.getLiteral() ) );
		
		closedEnum.sortValues(  (v1, v2) -> v1.getLiteral().compareTo( v2.getLiteral() ) );
		assertArrayEquals( new String[] { "Value1", "Value2" }, getNames( closedEnum.getValues(), v -> v.getLiteral() ) );
		
		value2.moveUp();
		assertArrayEquals( new String[] { "Value2", "Value1" }, getNames( closedEnum.getValues(), v -> v.getLiteral() ) );
		
		closedEnum.removeValue( value1 );
		assertArrayEquals( new String[] { "Value2" }, getNames( closedEnum.getValues(), v -> v.getLiteral() ) );
	}
	
	@Test
	public void testEnumValue() throws Exception {
		TLClosedEnumeration closedEnum = addClosedEnum( "TestObject", library1 );
		TLEnumValue value = addEnumValue( "Value", closedEnum );
		
		testDocumentationFunctions( value );
		testEquivalentFunctions( value );
	}
	
	protected TLClosedEnumeration addClosedEnum(String enumName, TLLibrary library) {
		TLClosedEnumeration closedEnum = new TLClosedEnumeration();
		
		closedEnum.setName( enumName );
		library.addNamedMember( closedEnum );
		assertEquals( closedEnum, library.getClosedEnumerationType( enumName ) );
		assertTrue( library.getNamedMembers().contains( closedEnum ) );
		return closedEnum;
	}
	
	protected TLOpenEnumeration addOpenEnum(String enumName, TLLibrary library) {
		TLOpenEnumeration openEnum = new TLOpenEnumeration();
		
		openEnum.setName( enumName );
		library.addNamedMember( openEnum );
		assertEquals( openEnum, library.getOpenEnumerationType( enumName ) );
		assertTrue( library.getNamedMembers().contains( openEnum ) );
		return openEnum;
	}
	
	protected TLEnumValue addEnumValue(String literal, TLAbstractEnumeration owner) {
		TLEnumValue value = new TLEnumValue();
		
		value.setLiteral( literal );
		value.setLabel( literal + " : label" );
		
		if (owner.getValues().isEmpty()) {
			owner.addValue( 0, value );
			
		} else {
			owner.addValue( value );
		}
		return value;
	}
	
}

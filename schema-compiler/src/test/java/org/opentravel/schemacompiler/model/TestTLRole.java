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
 * Verifies the functions of the <code>TLRole</code> class.
 */
public class TestTLRole extends AbstractModelTest {
	
	@Test
	public void testIdentityFunctions() throws Exception {
		TLCoreObject core = addCore( "TestObject", library1 );
		TLRoleEnumeration roleEnum = core.getRoleEnumeration();
		TLRole role = addRole( "TestRole", core );
		
		assertEquals( library1, role.getOwningLibrary() );
		assertEquals( "TestLibrary1.otm : TestObject : Role Enumeration", roleEnum.getValidationIdentity() );
		assertEquals( "TestLibrary1.otm : TestObject : TestRole", role.getValidationIdentity() );
	}
	
	@Test
	public void testRoleFunctions() throws Exception {
		TLCoreObject core = addCore( "TestObject", library1 );
		TLRoleEnumeration roleEnum = core.getRoleEnumeration();
		TLRole role1 = addRole( "Role1", core );
		TLRole role2 = addRole( "Role2", core );
		
		roleEnum.addRole( role1 );
		roleEnum.addRole( 1, role2 );
		assertEquals( 2, roleEnum.getRoles().size() );
		assertArrayEquals( new String[] { "Role1", "Role2" }, getNames( roleEnum.getRoles(), r -> r.getName() ) );
		
		role1.moveDown();
		assertArrayEquals( new String[] { "Role2", "Role1" }, getNames( roleEnum.getRoles(), r -> r.getName() ) );
		
		roleEnum.sortRoles( (r1, r2) -> r1.getName().compareTo( r2.getName() ) );
		assertArrayEquals( new String[] { "Role1", "Role2" }, getNames( roleEnum.getRoles(), r -> r.getName() ) );
		
		role2.moveUp();
		assertArrayEquals( new String[] { "Role2", "Role1" }, getNames( roleEnum.getRoles(), r -> r.getName() ) );
		
		roleEnum.removeRole( role1 );
		assertArrayEquals( new String[] { "Role2" }, getNames( roleEnum.getRoles(), r -> r.getName() ) );
		
	}
	
	private TLRole addRole(String roleName, TLCoreObject core) throws Exception {
		TLRole role = new TLRole();
		
		role.setName( roleName );
		core.getRoleEnumeration().addRole( role );
		return role;
	}
	
}

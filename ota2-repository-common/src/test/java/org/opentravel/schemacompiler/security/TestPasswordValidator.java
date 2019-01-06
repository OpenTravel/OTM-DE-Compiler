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
package org.opentravel.schemacompiler.security;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Verifies the function of the <code>PasswordValidator</code> class.
 */
public class TestPasswordValidator {
	
	private static final String TEST_PASSWORD = "MyPassword@Opentravel";
	
	@Test
	public void testPasswordValidator_clear() throws Exception {
		String encryptedPassword = PasswordHelper.encrypt( TEST_PASSWORD );
		PasswordValidator validator = new PasswordValidator( "MD5" );
		
		assertTrue(  validator.isValidPassword( TEST_PASSWORD, encryptedPassword ) );
	}
	
	@Test
	public void testPasswordValidator_MD5() throws Exception {
		PasswordValidator validator = new PasswordValidator( "MD5" );
		String encryptedPassword = "{MD5}" + validator.digestBase64( TEST_PASSWORD );
		
		assertTrue(  validator.isValidPassword( TEST_PASSWORD, encryptedPassword ) );
	}
	
	@Test
	public void testPasswordValidator_SHA() throws Exception {
		PasswordValidator validator = new PasswordValidator( "SHA" );
		String encryptedPassword = "{SHA}" + validator.digestBase64( TEST_PASSWORD );
		
		assertTrue(  validator.isValidPassword( TEST_PASSWORD, encryptedPassword ) );
	}
	
	@Test
	public void testPasswordValidator_SSHA() throws Exception {
		PasswordValidator validator = new PasswordValidator( "SHA" );
		String encryptedPassword = "{SSHA}" + validator.digestBase64( TEST_PASSWORD );
		
		assertTrue(  validator.isValidPassword( TEST_PASSWORD, encryptedPassword ) );
	}
	
}

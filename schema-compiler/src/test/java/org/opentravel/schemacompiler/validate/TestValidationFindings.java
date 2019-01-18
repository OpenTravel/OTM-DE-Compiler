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
package org.opentravel.schemacompiler.validate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;

/**
 * Verifies the functions of the <code>ValidationFindings</code> class.
 */
public class TestValidationFindings {
	
	private static final String TEST_ERROR_KEY   = "org.opentravel.schemacompiler.TLBusinessObject.name.NULL_OR_BLANK";
	private static final String TEST_WARNING_KEY = "org.opentravel.schemacompiler.TLBusinessObject.name.PATTERN_MISMATCH";
	
	private ValidationFindings findings;
	private TLBusinessObject testSource;
	
	@Before
	public void setupFindings() throws Exception {
		testSource = new TLBusinessObject();
		findings = new ValidationFindings();
		findings.addFinding( FindingType.ERROR, testSource, TEST_ERROR_KEY );
		findings.addFinding( FindingType.WARNING, testSource, TEST_WARNING_KEY, "badName" );
	}
	
	@Test
	public void testCount() throws Exception {
		assertEquals( 2, findings.count() );
		assertEquals( 2, findings.count( testSource ) );
		assertEquals( 1, findings.count( FindingType.ERROR ) );
		assertEquals( 1, findings.count( testSource, FindingType.ERROR ) );
	}
	
	@Test
	public void testHasFinding() throws Exception {
		assertTrue( findings.hasFinding() );
		assertTrue( findings.hasFinding( testSource ) );
		assertTrue( findings.hasFinding( FindingType.ERROR ) );
		assertTrue( findings.hasFinding( FindingType.WARNING ) );
		assertTrue( findings.hasFinding( testSource, FindingType.WARNING ) );
	}
	
	@Test
	public void testGetFindingAsList() throws Exception {
		assertEquals( 2, findings.getAllFindingsAsList().size() );
		assertEquals( 2, findings.getFindingsAsList( testSource ).size() );
		assertEquals( 1, findings.getFindingsAsList( FindingType.ERROR ).size() );
		assertEquals( 1, findings.getFindingsAsList( testSource, FindingType.ERROR ).size() );
	}
	
	@Test
	public void testGetValidationMessages() throws Exception {
		String[] allMessages = findings.getAllValidationMessages( FindingMessageFormat.IDENTIFIED_FORMAT );
		String[] errorMessages = findings.getValidationMessages( FindingType.ERROR, FindingMessageFormat.IDENTIFIED_FORMAT );
		String[] sourceMessages = findings.getValidationMessages( testSource, FindingMessageFormat.IDENTIFIED_FORMAT );
		String[] sourceErrorMessages = findings.getValidationMessages( testSource, FindingType.ERROR, FindingMessageFormat.IDENTIFIED_FORMAT );
		
		assertEquals( 2, allMessages.length );
		assertEquals( 1, errorMessages.length );
		assertEquals( 2, sourceMessages.length );
		assertEquals( 1, sourceErrorMessages.length );
	}
	
}

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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Verifies the functions of the <code>ValidationBuilder</code> class.
 */
public class TestValidationBuilder {

    private static final String TEST_PROPERTY = "test";
    private static final String MESSAGE_KEY_PREFIX = "TLBusinessObject.test.";

    private DateFormat dateFormat = new SimpleDateFormat( "M/d/yyyy" );
    private Date earlyDate;
    private Date lateDate;

    TLBusinessObject sourceBO;
    TLBusinessObject duplicateBO;

    private ValidationBuilder<?> builder;
    private Validatable testSource;

    @Before
    public void initBuilder() throws Exception {
        sourceBO = new TLBusinessObject();
        sourceBO.setName( "TestObject" );
        duplicateBO = new TLBusinessObject();
        duplicateBO.setName( sourceBO.getName() );
        testSource = sourceBO;
        builder = new TestVB().setTargetObject( testSource ).setFindingType( FindingType.ERROR );
        builder.setCalendarUnitsForDateComparisons( Calendar.DAY_OF_YEAR );
        earlyDate = dateFormat.parse( "1/1/2019" );
        lateDate = dateFormat.parse( "1/2/2019" );
    }

    @Test
    public void testAssertNull() throws Exception {
        builder.setProperty( TEST_PROPERTY, "error" ).assertNull();
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_NULL );
        builder.setProperty( TEST_PROPERTY, null ).assertNull();
        assertNoFinding();
    }

    @Test
    public void testAssertNullOrBlank() throws Exception {
        builder.setProperty( TEST_PROPERTY, "error" ).assertNullOrBlank();
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_NULL_OR_BLANK );
        builder.setProperty( TEST_PROPERTY, null ).assertNullOrBlank();
        assertNoFinding();
        builder.setProperty( TEST_PROPERTY, "" ).assertNullOrBlank();
        assertNoFinding();
    }

    @Test
    public void testAssertNotNull() throws Exception {
        builder.setProperty( TEST_PROPERTY, null ).assertNotNull();
        assertHasFinding( ValidationBuilder.ERROR_NULL_VALUE );
        builder.setProperty( TEST_PROPERTY, "" ).assertNotNull();
        assertNoFinding();
        builder.setProperty( TEST_PROPERTY, "valid" ).assertNotNull();
        assertNoFinding();
    }

    @Test
    public void testAssertNotNullOrBlank() throws Exception {
        builder.setProperty( TEST_PROPERTY, null ).assertNotNullOrBlank();
        assertHasFinding( ValidationBuilder.ERROR_NULL_OR_BLANK );
        builder.setProperty( TEST_PROPERTY, "" ).assertNotNullOrBlank();
        assertHasFinding( ValidationBuilder.ERROR_NULL_OR_BLANK );
        builder.setProperty( TEST_PROPERTY, "valid" ).assertNotNullOrBlank();
        assertNoFinding();
    }

    @Test
    public void testAssertGreaterThan_long() throws Exception {
        builder.setProperty( TEST_PROPERTY, 1 ).assertGreaterThan( 1 );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_GREATER_THAN );
        builder.setProperty( TEST_PROPERTY, 1 ).assertGreaterThan( 0 );
        assertNoFinding();
    }

    @Test
    public void testAssertGreaterThan_double() throws Exception {
        builder.setProperty( TEST_PROPERTY, 1.0 ).assertGreaterThan( 1.0 );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_GREATER_THAN );
        builder.setProperty( TEST_PROPERTY, 1.0 ).assertGreaterThan( 0.0 );
        assertNoFinding();
    }

    @Test
    public void testAssertGreaterThanOrEqual_long() throws Exception {
        builder.setProperty( TEST_PROPERTY, 1 ).assertGreaterThanOrEqual( 2 );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_GREATER_THAN_OR_EQUAL );
        builder.setProperty( TEST_PROPERTY, 1 ).assertGreaterThanOrEqual( 1 );
        assertNoFinding();
        builder.setProperty( TEST_PROPERTY, 1 ).assertGreaterThanOrEqual( 0 );
        assertNoFinding();
    }

    @Test
    public void testAssertGreaterThanOrEqual_double() throws Exception {
        builder.setProperty( TEST_PROPERTY, 1.0 ).assertGreaterThanOrEqual( 2.0 );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_GREATER_THAN_OR_EQUAL );
        builder.setProperty( TEST_PROPERTY, 1.0 ).assertGreaterThanOrEqual( 1.0 );
        assertNoFinding();
        builder.setProperty( TEST_PROPERTY, 1.0 ).assertGreaterThanOrEqual( 0.0 );
        assertNoFinding();
    }

    @Test
    public void testAssertLessThan_long() throws Exception {
        builder.setProperty( TEST_PROPERTY, 1 ).assertLessThan( 1 );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_LESS_THAN );
        builder.setProperty( TEST_PROPERTY, 0 ).assertLessThan( 1 );
        assertNoFinding();
    }

    @Test
    public void testAssertLessThan_double() throws Exception {
        builder.setProperty( TEST_PROPERTY, 1.0 ).assertLessThan( 1.0 );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_LESS_THAN );
        builder.setProperty( TEST_PROPERTY, 0.0 ).assertLessThan( 1.0 );
        assertNoFinding();
    }

    @Test
    public void testAssertLessThanOrEqual_long() throws Exception {
        builder.setProperty( TEST_PROPERTY, 2 ).assertLessThanOrEqual( 1 );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_LESS_THAN_OR_EQUAL );
        builder.setProperty( TEST_PROPERTY, 1 ).assertLessThanOrEqual( 1 );
        assertNoFinding();
        builder.setProperty( TEST_PROPERTY, 0 ).assertLessThanOrEqual( 1 );
        assertNoFinding();
    }

    @Test
    public void testAssertLessThanOrEqual_double() throws Exception {
        builder.setProperty( TEST_PROPERTY, 2.0 ).assertLessThanOrEqual( 1.0 );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_LESS_THAN_OR_EQUAL );
        builder.setProperty( TEST_PROPERTY, 1.0 ).assertLessThanOrEqual( 1.0 );
        assertNoFinding();
        builder.setProperty( TEST_PROPERTY, 0.0 ).assertLessThanOrEqual( 1.0 );
        assertNoFinding();
    }

    @Test
    public void testAssertEquals_long() throws Exception {
        builder.setProperty( TEST_PROPERTY, 0 ).assertEquals( 1 );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_EQUAL );
        builder.setProperty( TEST_PROPERTY, 1 ).assertEquals( 1 );
        assertNoFinding();
    }

    @Test
    public void testAssertEquals_double() throws Exception {
        builder.setProperty( TEST_PROPERTY, 0.0 ).assertEquals( 1.0 );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_EQUAL );
        builder.setProperty( TEST_PROPERTY, 1.0 ).assertEquals( 1.0 );
        assertNoFinding();
    }

    @Test
    public void testAssertEquals_object() throws Exception {
        builder.setProperty( TEST_PROPERTY, "error" ).assertEquals( "valid" );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_EQUAL );
        builder.setProperty( TEST_PROPERTY, null ).assertEquals( "valid" );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_EQUAL );
        builder.setProperty( TEST_PROPERTY, "error" ).assertEquals( null );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_EQUAL );
        builder.setProperty( TEST_PROPERTY, "valid" ).assertEquals( "valid" );
        assertNoFinding();
    }

    @Test
    public void testAssertNotEqual_long() throws Exception {
        builder.setProperty( TEST_PROPERTY, 1 ).assertNotEqual( 1 );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_NOT_EQUAL );
        builder.setProperty( TEST_PROPERTY, 0 ).assertNotEqual( 1 );
        assertNoFinding();
    }

    @Test
    public void testAssertNotEqual_double() throws Exception {
        builder.setProperty( TEST_PROPERTY, 1.0 ).assertNotEqual( 1.0 );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_NOT_EQUAL );
        builder.setProperty( TEST_PROPERTY, 0.0 ).assertNotEqual( 1.0 );
        assertNoFinding();
    }

    @Test
    public void testAssertNotEqual_object() throws Exception {
        builder.setProperty( TEST_PROPERTY, "error" ).assertNotEqual( "error" );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_NOT_EQUAL );
        builder.setProperty( TEST_PROPERTY, null ).assertNotEqual( "valid" );
        assertNoFinding();
        builder.setProperty( TEST_PROPERTY, "valid" ).assertNotEqual( null );
        assertNoFinding();
        builder.setProperty( TEST_PROPERTY, "valid1" ).assertNotEqual( "valid2" );
        assertNoFinding();
    }

    @Test
    public void testAssertContainsNoWhitespace() throws Exception {
        builder.setProperty( TEST_PROPERTY, "in valid" ).assertContainsNoWhitespace();
        assertHasFinding( ValidationBuilder.ERROR_CANNOT_CONTAIN_WHITESPACE );
        builder.setProperty( TEST_PROPERTY, "valid" ).assertContainsNoWhitespace();
        assertNoFinding();
    }

    @Test
    public void testAssertContainsOnlyNumericCharacters() throws Exception {
        builder.setProperty( TEST_PROPERTY, "123abc" ).assertContainsOnlyNumericCharacters();
        assertHasFinding( ValidationBuilder.ERROR_NUMERIC_CHARACTERS_ONLY );
        builder.setProperty( TEST_PROPERTY, "123456" ).assertContainsOnlyNumericCharacters();
        assertNoFinding();
    }

    @Test
    public void testAssertMinimumLength() throws Exception {
        builder.setProperty( TEST_PROPERTY, "1234" ).assertMinimumLength( 5 );
        assertHasFinding( ValidationBuilder.ERROR_UNDER_MINIMUM_LENGTH );
        builder.setProperty( TEST_PROPERTY, "12345" ).assertMinimumLength( 5 );
        assertNoFinding();
    }

    @Test
    public void testAssertMaximumLength() throws Exception {
        builder.setProperty( TEST_PROPERTY, "123456" ).assertMaximumLength( 5 );
        assertHasFinding( ValidationBuilder.ERROR_EXCEEDS_MAXIMUM_LENGTH );
        builder.setProperty( TEST_PROPERTY, "12345" ).assertMaximumLength( 5 );
        assertNoFinding();
    }

    @Test
    public void testAssertExactLength() throws Exception {
        builder.setProperty( TEST_PROPERTY, "123456" ).assertExactLength( 5 );
        assertHasFinding( ValidationBuilder.ERROR_NOT_EXACT_LENGTH );
        builder.setProperty( TEST_PROPERTY, "12345" ).assertExactLength( 5 );
        assertNoFinding();
    }

    @Test
    public void testAssertPatternMatch() throws Exception {
        builder.setProperty( TEST_PROPERTY, "123456" ).assertPatternMatch( "[A-Z]+[0-9]+" );
        assertHasFinding( ValidationBuilder.ERROR_PATTERN_MISMATCH );
        builder.setProperty( TEST_PROPERTY, "ABC123" ).assertPatternMatch( "[A-Z]+[0-9]+" );
        assertNoFinding();
    }

    @Test
    public void testAssertStartsWithUppercaseLetter() throws Exception {
        builder.setProperty( TEST_PROPERTY, "abc" ).assertStartsWithUppercaseLetter();
        assertHasFinding( ValidationBuilder.ERROR_MUST_START_WITH_UPPERCASE );
        builder.setProperty( TEST_PROPERTY, "Abc" ).assertStartsWithUppercaseLetter();
        assertNoFinding();
    }

    @Test
    public void testAssertStartsWithLowercaseLetter() throws Exception {
        builder.setProperty( TEST_PROPERTY, "Abc" ).assertStartsWithLowercaseLetter();
        assertHasFinding( ValidationBuilder.ERROR_MUST_START_WITH_LOWERCASE );
        builder.setProperty( TEST_PROPERTY, "abc" ).assertStartsWithLowercaseLetter();
        assertNoFinding();
    }

    @Test
    public void testAssertMinimumSize() throws Exception {
        builder.setProperty( TEST_PROPERTY, Arrays.asList( 1, 2, 3 ) ).assertMinimumSize( 4 );
        assertHasFinding( ValidationBuilder.ERROR_UNDER_MINIMUM_SIZE );
        builder.setProperty( TEST_PROPERTY, Arrays.asList( 1, 2, 3, 4 ) ).assertMinimumSize( 4 );
        assertNoFinding();
    }

    @Test
    public void testAssertMaximumSize() throws Exception {
        builder.setProperty( TEST_PROPERTY, Arrays.asList( 1, 2, 3, 4 ) ).assertMaximumSize( 3 );
        assertHasFinding( ValidationBuilder.ERROR_EXCEEDS_MAXIMUM_SIZE );
        builder.setProperty( TEST_PROPERTY, Arrays.asList( 1, 2, 3 ) ).assertMaximumSize( 3 );
        assertNoFinding();
    }

    @Test
    public void testAssertExactSize() throws Exception {
        builder.setProperty( TEST_PROPERTY, Arrays.asList( 1, 2, 3, 4 ) ).assertExactSize( 3 );
        assertHasFinding( ValidationBuilder.ERROR_NOT_EXACT_SIZE );
        builder.setProperty( TEST_PROPERTY, Arrays.asList( 1, 2, 3 ) ).assertExactSize( 3 );
        assertNoFinding();
    }

    @Test
    public void testAssertContainsNoNullElements() throws Exception {
        builder.setProperty( TEST_PROPERTY, Arrays.asList( 1, null, 3 ) ).assertContainsNoNullElements();
        assertHasFinding( ValidationBuilder.ERROR_CONTAINS_NULL_ELEMENTS );
        builder.setProperty( TEST_PROPERTY, Arrays.asList( 1, 2, 3 ) ).assertContainsNoNullElements();
        assertNoFinding();
    }

    @Test
    public void testAssertNoDuplicates() throws Exception {
        builder.setProperty( TEST_PROPERTY, Arrays.asList( sourceBO, duplicateBO ) )
            .assertNoDuplicates( e -> ((TLBusinessObject) e).getName() );
        assertHasFinding( ValidationBuilder.ERROR_DUPLICATE_ELEMENT );
        builder.setProperty( TEST_PROPERTY, Arrays.asList( sourceBO ) )
            .assertNoDuplicates( e -> ((TLBusinessObject) e).getName() );
        assertNoFinding();
    }

    @Test
    public void testAssertBefore() throws Exception {
        builder.setProperty( TEST_PROPERTY, lateDate ).assertBefore( earlyDate );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_BEFORE );
        builder.setProperty( TEST_PROPERTY, earlyDate ).assertBefore( lateDate );
        assertNoFinding();
    }

    @Test
    public void testAssertOnOrBefore() throws Exception {
        builder.setProperty( TEST_PROPERTY, lateDate ).assertOnOrBefore( earlyDate );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_ON_OR_BEFORE );
        builder.setProperty( TEST_PROPERTY, earlyDate ).assertOnOrBefore( lateDate );
        assertNoFinding();
        builder.setProperty( TEST_PROPERTY, earlyDate ).assertOnOrBefore( earlyDate );
        assertNoFinding();
    }

    @Test
    public void testAssertAfter() throws Exception {
        builder.setProperty( TEST_PROPERTY, earlyDate ).assertAfter( lateDate );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_AFTER );
        builder.setProperty( TEST_PROPERTY, lateDate ).assertAfter( earlyDate );
        assertNoFinding();
    }

    @Test
    public void testAssertOnOrAfter() throws Exception {
        builder.setProperty( TEST_PROPERTY, earlyDate ).assertOnOrAfter( lateDate );
        assertHasFinding( ValidationBuilder.ERROR_MUST_BE_ON_OR_AFTER );
        builder.setProperty( TEST_PROPERTY, lateDate ).assertOnOrAfter( earlyDate );
        assertNoFinding();
        builder.setProperty( TEST_PROPERTY, lateDate ).assertOnOrAfter( lateDate );
        assertNoFinding();
    }

    private void assertHasFinding(String expectedMessageKey) throws Exception {
        ValidationFindings findings = builder.getFindings();
        ValidationFinding finding;

        Assert.assertEquals( 1, findings.getAllFindingsAsList().size() );
        finding = findings.getAllFindingsAsList().get( 0 );
        Assert.assertEquals( testSource, finding.getSource() );
        Assert.assertEquals( FindingType.ERROR, finding.getType() );
        Assert.assertEquals( MESSAGE_KEY_PREFIX + expectedMessageKey, finding.getMessageKey() );
        initBuilder();
    }

    private void assertNoFinding() throws Exception {
        Assert.assertTrue( builder.isEmpty() );
        initBuilder();
    }

    /**
     * Extension of the <code>ValidationBuilder</code> abstract class used for unit testing.
     */
    private static class TestVB extends ValidationBuilder<TestVB> {

        /**
         * @see org.opentravel.schemacompiler.validate.ValidationBuilder#getThis()
         */
        @Override
        protected TestVB getThis() {
            return this;
        }

    }

}

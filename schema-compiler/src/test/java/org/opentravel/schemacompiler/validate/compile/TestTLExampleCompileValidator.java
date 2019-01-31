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
package org.opentravel.schemacompiler.validate.compile;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.validate.AbstractValidatorTest;
import org.opentravel.schemacompiler.validate.ValidationBuilder;

/**
 * Verifies the functions of the <code>TLExampleCompileValidator</code> class.
 */
public class TestTLExampleCompileValidator extends AbstractValidatorTest {
    
    @Test
    public void testIllegalExampleContext() throws Exception {
        TLAttribute attribute = getMember( TLIBRARY_ERROR1_V1, "ExampleCore|@FACET:SUMMARY|attrInvalidContext", TLAttribute.class );
        Set<String> messageKeys = getFindingMessageKeys( attribute.getExamples().get( 0 ) );
        
        assertTrue( messageKeys.contains( TLContextCompileValidator.ERROR_INVALID_CONTEXT ) );
    }
    
    @Test
    public void testInvalidFractionDigits() throws Exception {
        TLSimple simple = getMember( TLIBRARY_ERROR1_V1, "TestInvalidDigits", TLSimple.class );
        Set<String> messageKeys = getFindingMessageKeys( simple.getExamples().get( 0 ) );
        
        assertTrue( messageKeys.contains( TLExampleCompileValidator.ERROR_EXCEEDS_FRACTION_DIGITS ) );
    }
    
    @Test
    public void testInvalidTotalDigits() throws Exception {
        TLSimple simple = getMember( TLIBRARY_ERROR1_V1, "TestInvalidDigits", TLSimple.class );
        Set<String> messageKeys = getFindingMessageKeys( simple.getExamples().get( 0 ) );
        
        assertTrue( messageKeys.contains( TLExampleCompileValidator.ERROR_EXCEEDS_TOTAL_DIGITS ) );
    }
    
    @Test
    public void testInvalidDate() throws Exception {
        TLAttribute attribute = getMember( TLIBRARY_ERROR1_V1, "ExampleCore|@FACET:SUMMARY|attrInvalidDate", TLAttribute.class );
        Set<String> messageKeys = getFindingMessageKeys( attribute.getExamples().get( 0 ) );
        
        assertTrue( messageKeys.contains( TLExampleCompileValidator.ERROR_INVALID_DATE ) );
    }
    
    @Test
    public void testInvalidTime() throws Exception {
        TLAttribute attribute = getMember( TLIBRARY_ERROR1_V1, "ExampleCore|@FACET:SUMMARY|attrInvalidTime", TLAttribute.class );
        Set<String> messageKeys = getFindingMessageKeys( attribute.getExamples().get( 0 ) );
        
        assertTrue( messageKeys.contains( TLExampleCompileValidator.ERROR_INVALID_TIME ) );
    }
    
    @Test
    public void testInvalidDateTime() throws Exception {
        TLAttribute attribute = getMember( TLIBRARY_ERROR1_V1, "ExampleCore|@FACET:SUMMARY|attrInvalidDateTime", TLAttribute.class );
        Set<String> messageKeys = getFindingMessageKeys( attribute.getExamples().get( 0 ) );
        
        assertTrue( messageKeys.contains( TLExampleCompileValidator.ERROR_INVALID_DATETIME ) );
    }
    
    @Test
    public void testPatternMismatch() throws Exception {
        TLSimple stringType = getMember( TLIBRARY_ERROR1_V1, "TestString", TLSimple.class );
        Set<String> messageKeys = getFindingMessageKeys( stringType.getExamples().get( 0 ) );
        
        assertTrue( messageKeys.contains( ValidationBuilder.ERROR_PATTERN_MISMATCH ) );
    }
    
    @Test
    public void testValidSimpleListExamples() throws Exception {
        TLSimple intList = getMember( TLIBRARY_ERROR1_V1, "TestIntegerList", TLSimple.class );
        TLSimple stringList = getMember( TLIBRARY_ERROR1_V1, "TestStringList", TLSimple.class );
        
        assertTrue( getFindingMessageKeys( intList.getExamples().get( 0 ) ).isEmpty() );
        assertTrue( getFindingMessageKeys( stringList.getExamples().get( 0 ) ).isEmpty() );
    }
    
}

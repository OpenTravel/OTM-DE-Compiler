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
import org.opentravel.schemacompiler.validate.AbstractValidatorTest;
import org.opentravel.schemacompiler.validate.ValidationBuilder;

/**
 * Verifies the functions of the <code>TLAttributeCompileValidator</code> class.
 */
public class TestTLAttributeCompileValidator extends AbstractValidatorTest {
    
    @Test
    public void testInvalidReferenceName() throws Exception {
        TLAttribute attribute = getMember( TLIBRARY_ERROR1_V1, "TestCore|@FACET:SUMMARY|tcDetail", TLAttribute.class );
        Set<String> messageKeys = getFindingMessageKeys( attribute );
        
        assertTrue( messageKeys.contains( TLAttributeCompileValidator.WARNING_INVALID_REFERENCE_NAME ) );
    }
    
    @Test
    public void testReferenceMissingId() throws Exception {
        TLAttribute attribute = getMember( TLIBRARY_ERROR1_V1, "TestCore|@FACET:SUMMARY|tcDetail", TLAttribute.class );
        Set<String> messageKeys = getFindingMessageKeys( attribute );
        
        assertTrue( messageKeys.contains( TLAttributeCompileValidator.ERROR_ILLEGAL_REFERENCE ) );
    }
    
    @Test
    public void testEmptyFacetReference() throws Exception {
        TLAttribute attribute = getMember( TLIBRARY_ERROR1_V1, "TestBusinessObject|@FACET:DETAIL|testCoreSimple", TLAttribute.class );
        Set<String> messageKeys = getFindingMessageKeys( attribute );
        
        assertTrue( messageKeys.contains( TLAttributeCompileValidator.ERROR_EMPTY_FACET_REFERENCED ) );
    }
    
    @Test
    public void testIllegalListFacetReference() throws Exception {
        TLAttribute attribute = getMember( TLIBRARY_ERROR1_V1, "TestCore|@FACET:SUMMARY|tcDetail", TLAttribute.class );
        Set<String> messageKeys = getFindingMessageKeys( attribute );
        
        assertTrue( messageKeys.contains( TLAttributeCompileValidator.ERROR_ILLEGAL_LIST_FACET_REFERENCE ) );
    }
    
    @Test
    public void testIllegalListFacetRepeat() throws Exception {
        TLAttribute attribute = getMember( TLIBRARY_ERROR1_V1, "TestCore|@FACET:SUMMARY|cwrRepeatRef", TLAttribute.class );
        Set<String> messageKeys = getFindingMessageKeys( attribute );
        
        assertTrue( messageKeys.contains( TLAttributeCompileValidator.WARNING_LIST_FACET_REPEAT_IGNORED ) );
    }
    
    @Test
    public void testIllegalVwaAttribute() throws Exception {
        TLAttribute attribute = getMember( TLIBRARY_ERROR1_V1, "TestCore|@FACET:SUMMARY|vwaAttr", TLAttribute.class );
        Set<String> messageKeys = getFindingMessageKeys( attribute );
        
        assertTrue( messageKeys.contains( TLAttributeCompileValidator.ERROR_ILLEGAL_VWA_ATTRIBUTE ) );
    }
    
    @Test
    public void testIllegalOpenEnumAttribute() throws Exception {
        TLAttribute attribute = getMember( TLIBRARY_ERROR1_V1, "TestCore|@FACET:SUMMARY|openEnumAttr", TLAttribute.class );
        Set<String> messageKeys = getFindingMessageKeys( attribute );
        
        assertTrue( messageKeys.contains( TLAttributeCompileValidator.ERROR_ILLEGAL_OPEN_ENUM_ATTRIBUTE ) );
    }
    
    @Test
    public void testBooleanAttribute() throws Exception {
        TLAttribute attribute = getMember( TLIBRARY_ERROR1_V1, "TestCore|@FACET:SUMMARY|booleanAttr", TLAttribute.class );
        Set<String> messageKeys = getFindingMessageKeys( attribute );
        
        assertTrue( messageKeys.contains( TLAttributeCompileValidator.WARNING_BOOLEAN_TYPE_REFERENCE ) );
    }
    
    @Test
    public void testIllegalSimpleCoreAttribute() throws Exception {
        TLAttribute attribute = getMember( TLIBRARY_ERROR1_V1, "TargetCore|@FACET:DETAIL|nonSimpleCoreAttr", TLAttribute.class );
        Set<String> messageKeys = getFindingMessageKeys( attribute );
        
        assertTrue( messageKeys.contains( TLAttributeCompileValidator.ERROR_NON_SIMPLE_CORE_AS_ATTRIBUTE ) );
    }
    
    @Test
    public void testIllegalRequiredMinorVersionAttribute() throws Exception {
        TLAttribute attribute = getMember( TLIBRARY_ERROR1_V1_1, "TestCore|@FACET:SUMMARY|testEclipseAttr1", TLAttribute.class );
        Set<String> messageKeys = getFindingMessageKeys( attribute );
        
        assertTrue( messageKeys.contains( TLAttributeCompileValidator.ERROR_ILLEGAL_REQUIRED_ATTRIBUTE ) );
    }
    
    @Test
    public void testDuplicateAttribute() throws Exception {
        TLAttribute attribute = getMember( TLIBRARY_ERROR1_V1_1, "TestCore|@FACET:SUMMARY|testEclipseAttr1", TLAttribute.class );
        Set<String> messageKeys = getFindingMessageKeys( attribute );
        
        assertTrue( messageKeys.contains( ValidationBuilder.ERROR_DUPLICATE_ELEMENT ) );
    }
    
}

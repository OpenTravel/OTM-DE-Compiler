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

import org.junit.Test;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.validate.AbstractValidatorTest;
import org.opentravel.schemacompiler.validate.ValidationBuilder;

import java.util.Set;

/**
 * Verifies the functions of the <code>TLPropertyCompileValidator</code> class.
 */
public class TestTLPropertyCompileValidator extends AbstractValidatorTest {

    @Test
    public void testInvalidReferenceName() throws Exception {
        TLProperty element =
            getMember( TLIBRARY_ERROR1_V1, "TestCore|@FACET:SUMMARY|TestBusinessObject", TLProperty.class );
        Set<String> messageKeys = getFindingMessageKeys( element );

        assertTrue( messageKeys.contains( TLPropertyCompileValidator.WARNING_INVALID_REFERENCE_NAME ) );
    }

    @Test
    public void testReferenceMissingId() throws Exception {
        TLProperty element =
            getMember( TLIBRARY_ERROR1_V1, "TestCore|@FACET:SUMMARY|TestBusinessObject", TLProperty.class );
        Set<String> messageKeys = getFindingMessageKeys( element );

        assertTrue( messageKeys.contains( TLPropertyCompileValidator.ERROR_ILLEGAL_REFERENCE ) );
    }

    @Test
    public void testInvalidIdLocation() throws Exception {
        TLProperty coreElement = getMember( TLIBRARY_ERROR1_V1, "TargetCore|@FACET:DETAIL|detailId", TLProperty.class );
        TLProperty choiceElement =
            getMember( TLIBRARY_ERROR1_V1, "@CONTEXTUAL:TestChoice_ChoiceA|choiceAId", TLProperty.class );
        TLProperty boElement =
            getMember( TLIBRARY_ERROR1_V1, "TestBusinessObject|@FACET:DETAIL|detailId", TLProperty.class );
        Set<String> messageKeys;

        messageKeys = getFindingMessageKeys( coreElement );
        assertTrue( messageKeys.contains( TLPropertyCompileValidator.WARNING_ILLEGAL_CORE_OBJECT_ID ) );

        messageKeys = getFindingMessageKeys( choiceElement );
        assertTrue( messageKeys.contains( TLPropertyCompileValidator.WARNING_ILLEGAL_CHOICE_OBJECT_ID ) );

        messageKeys = getFindingMessageKeys( boElement );
        assertTrue( messageKeys.contains( TLPropertyCompileValidator.WARNING_ILLEGAL_BUSINESS_OBJECT_ID ) );
    }

    @Test
    public void testMultipleIdDeclarations() throws Exception {
        TLFacet facet = getMember( TLIBRARY_ERROR1_V1, "TargetCore|@FACET:DETAIL", TLFacet.class );
        Set<String> messageKeys = getFindingMessageKeys( facet );

        assertTrue( messageKeys.contains( TLFacetCompileValidator.ERROR_MULTIPLE_ID_MEMBERS ) );
    }

    @Test
    public void testIllegalListFacetReference() throws Exception {
        TLProperty element =
            getMember( TLIBRARY_ERROR1_V1, "TestCore|@FACET:SUMMARY|TargetCoreDetail", TLProperty.class );
        Set<String> messageKeys = getFindingMessageKeys( element );

        assertTrue( messageKeys.contains( TLPropertyCompileValidator.ERROR_ILLEGAL_LIST_FACET_REFERENCE ) );
    }

    @Test
    public void testIllegalListFacetRepeat() throws Exception {
        TLProperty element = getMember( TLIBRARY_ERROR1_V1, "TestCore|@FACET:SUMMARY|CoreWithRoles", TLProperty.class );
        Set<String> messageKeys = getFindingMessageKeys( element );

        assertTrue( messageKeys.contains( TLPropertyCompileValidator.WARNING_LIST_FACET_REPEAT_IGNORED ) );
    }

    @Test
    public void testIllegalRequiredMinorVersionElement() throws Exception {
        TLProperty element =
            getMember( TLIBRARY_ERROR1_V1_1, "TestCore|@FACET:SUMMARY|TestEclipse2", TLProperty.class );
        Set<String> messageKeys = getFindingMessageKeys( element );

        assertTrue( messageKeys.contains( TLPropertyCompileValidator.ERROR_ILLEGAL_REQUIRED_ELEMENT ) );
    }

    @Test
    public void testDuplicateElement() throws Exception {
        TLProperty element =
            getMember( TLIBRARY_ERROR1_V1_1, "TestCore|@FACET:SUMMARY|TestEclipse2", TLProperty.class );
        Set<String> messageKeys = getFindingMessageKeys( element );

        assertTrue( messageKeys.contains( ValidationBuilder.ERROR_DUPLICATE_ELEMENT ) );
    }

}

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
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.validate.AbstractValidatorTest;

import java.util.Set;

/**
 * Verifies the functions of the <code>TLSimpleCompileValidator</code> class.
 */
public class TestTLSimpleCompileValidator extends AbstractValidatorTest {

    @Test
    public void testInvalidConstraints() throws Exception {
        TLSimple simple = getMember( TLIBRARY_ERROR1_V1, "TestConstraintError", TLSimple.class );
        Set<String> messageKeys = getFindingMessageKeys( simple );

        assertTrue( messageKeys.contains( TLSimpleCompileValidator.ERROR_INVALID_RESTRICTION ) );
        assertTrue( messageKeys.contains( TLSimpleCompileValidator.ERROR_RESTRICTION_NOT_APPLICABLE ) );
    }

    @Test
    public void testInvalidPattern() throws Exception {
        TLSimple simple = getMember( TLIBRARY_ERROR1_V1, "TestPatternError", TLSimple.class );
        Set<String> messageKeys = getFindingMessageKeys( simple );

        assertTrue( messageKeys.contains( TLSimpleCompileValidator.ERROR_INVALID_PATTERN ) );
    }

}

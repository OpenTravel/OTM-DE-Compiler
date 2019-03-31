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

package org.opentravel.schemacompiler.security.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Verifies the function of the <code>HexUtils</code> class.
 */
public class TestHexUtils {

    @Test
    public void testHexToBytes() throws Exception {
        String[] hexValues = {"abcd", "ABCD", "1234"};
        byte[][] expectedByteValues = new byte[][] {new byte[] {-85, -51}, new byte[] {-85, -51}, new byte[] {18, 52}};

        for (int i = 0; i < hexValues.length; i++) {
            assertArrayEquals( expectedByteValues[i], HexUtils.convert( hexValues[i] ) );
        }
    }

    @Test
    public void testBytesToHex() throws Exception {
        byte[][] byteValues = new byte[][] {new byte[] {-85, -51}, new byte[] {18, 52}};
        String[] expectedHexValues = {"abcd", "1234"};

        for (int i = 0; i < byteValues.length; i++) {
            assertEquals( expectedHexValues[i], HexUtils.convert( byteValues[i] ) );
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOddLengthHexString() throws Exception {
        HexUtils.convert( "ABC" );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFirstDigit() throws Exception {
        HexUtils.convert( "01AX" );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSecondDigit() throws Exception {
        HexUtils.convert( "01XA" );
    }

}

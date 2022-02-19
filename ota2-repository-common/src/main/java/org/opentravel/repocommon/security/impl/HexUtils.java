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

package org.opentravel.repocommon.security.impl;

import java.io.ByteArrayOutputStream;

/**
 * Utility methods useful in dealing used to convert byte arrays to and from strings of hexadecimal digits.
 * 
 * @author S. Livezey
 */
public class HexUtils {

    private static final String INVALID_HEXADECIMAL_STRING = "Invalid hexadecimal string - bad digit(s).";

    /**
     * Private constructor to prevent instantiation.
     */
    private HexUtils() {}

    /**
     * Convert a String of hexadecimal digits into the corresponding byte array by encoding each two hexadecimal digits
     * as a byte.
     * 
     * @param digits Hexadecimal digits representation
     * @return byte[]
     * @throws IllegalArgumentException if an invalid hexadecimal digit is found, or the input string contains an odd
     *         number of hexadecimal digits
     */
    public static byte[] convert(String digits) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (int i = 0; i < digits.length(); i += 2) {
            appendNextDigit( digits, i, baos );
        }
        return (baos.toByteArray());

    }

    /**
     * Convert a byte array into a printable format containing a String of hexadecimal digit characters (two per byte).
     * 
     * @param bytes the byte array representation
     * @return String
     */
    public static String convert(byte[] bytes) {
        StringBuilder sb = new StringBuilder( bytes.length * 2 );

        for (int i = 0; i < bytes.length; i++) {
            sb.append( convertDigit( bytes[i] >> 4 ) );
            sb.append( convertDigit( bytes[i] & 0x0f ) );
        }
        return sb.toString();

    }

    /**
     * Appends the next digit from the string provided to the output stream provided.
     * 
     * @param digits the digit string being appended
     * @param digitsPos the current position in the digit string
     * @param baos the output stream to which the digit will be written
     */
    private static void appendNextDigit(String digits, int digitsPos, ByteArrayOutputStream baos) {
        char c1 = digits.charAt( digitsPos );

        if ((digitsPos + 1) >= digits.length()) {
            throw new IllegalArgumentException( "Invalid hexadecimal string - odd numbered length." );
        }
        char c2 = digits.charAt( digitsPos + 1 );

        byte b = 0;

        if ((c1 >= '0') && (c1 <= '9')) {
            b += ((c1 - '0') * 16);
        } else if ((c1 >= 'a') && (c1 <= 'f')) {
            b += ((c1 - 'a' + 10) * 16);
        } else if ((c1 >= 'A') && (c1 <= 'F')) {
            b += ((c1 - 'A' + 10) * 16);
        } else {
            throw new IllegalArgumentException( INVALID_HEXADECIMAL_STRING );
        }

        if ((c2 >= '0') && (c2 <= '9')) {
            b += (c2 - '0');
        } else if ((c2 >= 'a') && (c2 <= 'f')) {
            b += (c2 - 'a' + 10);
        } else if ((c2 >= 'A') && (c2 <= 'F')) {
            b += (c2 - 'A' + 10);
        } else {
            throw new IllegalArgumentException( INVALID_HEXADECIMAL_STRING );
        }
        baos.write( b );
    }

    /**
     * [Private] Convert the specified value (0 .. 15) to the corresponding hexadecimal digit.
     * 
     * @param value Value to be converted
     */
    private static char convertDigit(int value) {

        value &= 0x0f;

        if (value >= 10) {
            return ((char) (value - 10 + 'a'));
        } else {
            return ((char) (value + '0'));
        }
    }

}

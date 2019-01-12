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

/**
 * Utilities to manipulate char chunks. While String is the easiest way to manipulate chars (
 * search, substrings, etc), it is known to not be the most efficient solution - Strings are
 * designed as immutable and secure objects.
 * 
 * @author dac@sun.com
 * @author James Todd [gonzo@sun.com]
 * @author Costin Manolache
 * @author Remy Maucherat
 */
public class CharChunk implements CharSequence, Cloneable {

    // --------------------
    // char[]
    private char[] buff;

    private int start;
    private int end;

    /**
     * Resets the message bytes to an uninitialized state.
     */
    public void recycle() {
        start = 0;
        end = 0;
    }

    public void allocate(int initial) {
        if (buff == null || buff.length < initial) {
            buff = new char[initial];
        }
        start = 0;
        end = 0;
    }

    // compat
    public char[] getChars() {
        return getBuffer();
    }

    public char[] getBuffer() {
        return buff;
    }

    /**
     * Returns the start offset of the bytes. For output this is the end of the buffer.
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the start offset of the bytes.
     */
    public void setOffset(int off) {
        start = off;
    }

    /**
     * Returns the length of the bytes.
     */
    public int getLength() {
        return end - start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int i) {
        end = i;
    }

    public char charAt(int index) {
        return buff[index + start];
    }

    public CharSequence subSequence(int start, int end) {
        try {
            CharChunk result = (CharChunk) this.clone();
            result.setOffset(this.start + start);
            result.setEnd(this.start + end);
            return result;
        } catch (CloneNotSupportedException e) {
            // Cannot happen
            return null;
        }
    }

    public int length() {
        return end - start;
    }

}

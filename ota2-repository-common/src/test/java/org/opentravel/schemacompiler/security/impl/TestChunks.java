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

import org.junit.Assert;
import org.junit.Test;

/**
 * Verifies the function of the <code>ByteChunk</code> class.
 */
public class TestChunks {

    @Test
    public void testByteChunk() throws Exception {
        ByteChunk chunk = new ByteChunk( 0 );

        chunk.allocate( 0, 3 );
        chunk.append( new byte[] {1, 2, 3}, 0, 3 );
        Assert.assertArrayEquals( new byte[] {1, 2, 3}, chunk.getBuffer() );

        chunk.allocate( 3, 6 );
        chunk.append( new byte[] {4, 5, 6}, 0, 3 );
        assertArrayEquals( new byte[] {4, 5, 6}, chunk.getBuffer() );
    }

    @Test
    public void testCharChunk() throws Exception {
        CharChunk chunk = new CharChunk();

        chunk.recycle();
        chunk.allocate( 4 );
        chunk.setEnd( 4 );
        System.arraycopy( new char[] {'a', 'b', 'c', 'd'}, 0, chunk.getBuffer(), 0, 3 );
        CharSequence subsequence = chunk.subSequence( 1, 2 );
        CharChunk ssChunk = (CharChunk) subsequence;

        assertEquals( 'b', subsequence.charAt( 0 ) );
        assertEquals( 'c', subsequence.charAt( 1 ) );
        assertEquals( 1, ssChunk.getStart() );
        assertEquals( 2, ssChunk.getEnd() );
        assertEquals( 1, ssChunk.getLength() );
        assertEquals( 1, ssChunk.length() );
        assertEquals( 4, ssChunk.getChars().length );
    }

}

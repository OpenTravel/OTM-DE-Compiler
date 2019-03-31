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
 * This class is used to represent a chunk of bytes, and utilities to manipulate byte[].
 * 
 * <p>
 * The buffer can be modified and used for both input and output.
 * 
 * <p>
 * There are 2 modes: The chunk can be associated with a sink - ByteInputChannel or ByteOutputChannel, which will be
 * used when the buffer is empty (on input) or filled (on output). For output, it can also grow. This operating mode is
 * selected by calling setLimit() or allocate(initial, limit) with limit != -1.
 * 
 * <p>
 * Various search and append method are defined - similar with String and StringBuffer, but operating on bytes.
 * 
 * <p>
 * This is important because it allows processing the http headers directly on the received bytes, without converting to
 * chars and Strings until the strings are needed. In addition, the charset is determined later, from headers or user
 * code.
 * 
 * @author dac@sun.com
 * @author James Todd [gonzo@sun.com]
 * @author Costin Manolache
 * @author Remy Maucherat
 */
public class ByteChunk {

    /**
     * Default encoding used to convert to strings. It should be UTF8, as most standards seem to converge, but the
     * servlet API requires 8859_1, and this object is used mostly for servlets.
     */
    public static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";

    // byte[]
    private byte[] buff;

    private int start = 0;
    private int end;

    // How much can it grow, when data is added
    private int limit = -1;

    /**
     * Constructor that specifies the initial size of the chunk.
     * 
     * @param initial the initial chunk size
     */
    public ByteChunk(int initial) {
        allocate( initial, -1 );
    }

    /**
     * Allocates the specified number of bytes for this chunk.
     * 
     * @param initial the initial size of the chunk
     * @param limit the limit by which to increase the size when the chunk grows
     */
    public void allocate(int initial, int limit) {
        if (buff == null || buff.length < initial) {
            buff = new byte[initial];
        }
        this.limit = limit;
        start = 0;
        end = 0;
    }

    /**
     * Returns the message bytes.
     * 
     * @return byte[]
     */
    public byte[] getBytes() {
        return getBuffer();
    }

    /**
     * Returns the message bytes.
     * 
     * @return byte[]
     */
    public byte[] getBuffer() {
        return buff;
    }

    /**
     * Returns the start offset of the bytes. For output this is the end of the buffer.
     * 
     * @return int
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the length of the bytes
     * 
     * @return int
     */
    public int getLength() {
        return end - start;
    }

    public int getEnd() {
        return end;
    }

    /**
     * Add data to the buffer
     * 
     * @param src the source byte array from which to append
     * @param off the offset of the first byte in the array
     * @param len the number of bytes to append
     */
    public void append(byte[] src, int off, int len) {
        // will grow, up to limit
        makeSpace( len );

        // if we don't have limit: makeSpace can grow as it wants
        if (limit < 0) {
            // assert: makeSpace made enough space
            System.arraycopy( src, off, buff, end, len );
            end += len;
            return;
        }

        // if we have limit and we're below
        if (len <= limit - end) {
            // makeSpace will grow the buffer to the limit,
            // so we have space
            System.arraycopy( src, off, buff, end, len );
            end += len;
        }
    }

    /**
     * Make space for len chars. If len is small, allocate a reserve space too. Never grow bigger than limit.
     */
    private void makeSpace(int count) {
        byte[] tmp = null;

        int newSize;
        int desiredSize = end + count;

        // Can't grow above the limit
        if (limit > 0 && desiredSize > limit) {
            desiredSize = limit;
        }

        if (buff == null) {
            if (desiredSize < 256) {
                desiredSize = 256; // take a minimum
            }
            buff = new byte[desiredSize];
        }

        // limit < buf.length ( the buffer is already big )
        // or we already have space
        if (desiredSize <= buff.length) {
            return;
        }
        // grow in larger chunks
        if (desiredSize < 2 * buff.length) {
            newSize = buff.length * 2;

            if (limit > 0 && newSize > limit) {
                newSize = limit;
            }
            tmp = new byte[newSize];
        } else {
            newSize = buff.length * 2 + count;

            if (limit > 0 && newSize > limit) {
                newSize = limit;
            }
            tmp = new byte[newSize];
        }

        System.arraycopy( buff, start, tmp, 0, end - start );
        buff = tmp;
        end = end - start;
        start = 0;
    }

}

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

package org.opentravel.schemacompiler.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Verifies the functions of the <code>CompilerExtension</code> class.
 */
public class TestCompilerExtension {

    @Test
    @SuppressWarnings("unlikely-arg-type")
    public void testEquality() throws Exception {
        CompilerExtension ext1a = new CompilerExtension( "TEST1", 10 );
        CompilerExtension ext1b = new CompilerExtension( "TEST1", 10 );
        CompilerExtension ext2a = new CompilerExtension( "TEST2", 10 );
        CompilerExtension ext2b = new CompilerExtension( "TEST2", 20 );
        CompilerExtension ext3a = new CompilerExtension( null, 10 );
        CompilerExtension ext3b = new CompilerExtension( null, 20 );
        CompilerExtension ext3c = new CompilerExtension( null, 20 );

        assertTrue( ext1a.equals( ext1b ) );
        assertFalse( ext2a.equals( ext2b ) );
        assertFalse( ext2b.equals( ext2a ) );
        assertFalse( ext3a.equals( ext3b ) );
        assertFalse( ext3b.equals( ext3a ) );
        assertTrue( ext3b.equals( ext3c ) );
        assertTrue( ext3c.equals( ext3b ) );
        assertFalse( ext2a.equals( "NOT-AN_EXTENSION" ) );
        assertEquals( ext1a.hashCode(), ext1b.hashCode() );
        assertEquals( ext3b.hashCode(), ext3c.hashCode() );
    }

    @Test
    public void testToString() throws Exception {
        CompilerExtension ext = new CompilerExtension( "TEST1", 10 );
        String extString = ext.toString();

        assertTrue( extString.contains( "TEST1" ) );
        assertTrue( extString.contains( "10" ) );
    }

    @Test
    public void testSortCompilerExtension_byRank() throws Exception {
        List<CompilerExtension> extensionList = new ArrayList<>();

        extensionList.add( null );
        extensionList.add( new CompilerExtension( "TEST3", 30 ) );
        extensionList.add( new CompilerExtension( "TEST2", 20 ) );
        extensionList.add( new CompilerExtension( "TEST1", 10 ) );

        Collections.sort( extensionList );
        assertNull( extensionList.get( 0 ) );
        assertEquals( 10, extensionList.get( 1 ).getRank() );
        assertEquals( 20, extensionList.get( 2 ).getRank() );
        assertEquals( 30, extensionList.get( 3 ).getRank() );
    }

    @Test
    public void testSortCompilerExtension_byId() throws Exception {
        List<CompilerExtension> extensionList = new ArrayList<>();

        extensionList.add( new CompilerExtension( "TEST3", 10 ) );
        extensionList.add( new CompilerExtension( "TEST2", 10 ) );
        extensionList.add( new CompilerExtension( "TEST1", 10 ) );

        Collections.sort( extensionList );
        assertEquals( "TEST1", extensionList.get( 0 ).getExtensionId() );
        assertEquals( "TEST2", extensionList.get( 1 ).getExtensionId() );
        assertEquals( "TEST3", extensionList.get( 2 ).getExtensionId() );
    }

}

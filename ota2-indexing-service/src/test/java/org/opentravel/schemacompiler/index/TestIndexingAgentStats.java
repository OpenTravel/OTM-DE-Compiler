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

package org.opentravel.schemacompiler.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Verifies the functions of the <code>IndexingAgentStatsMBean</code>.
 */
public class TestIndexingAgentStats {

    private IndexingAgentStats stats;

    @Before
    public void setup() {
        stats = new IndexingAgentStats();
        stats.setSearchIndexLocation( new File( System.getProperty( "user.dir" ) ) );
    }

    @Test
    public void testManagerAvailable() throws Exception {
        stats.setAvailable( false );
        assertFalse( stats.isAvailable() );

        stats.setAvailable( true );
        assertTrue( stats.isAvailable() );
    }

    @Test
    public void testDiskUtilizationCheck() throws Exception {
        assertTrue( stats.getSearchIndexDiskUtilization() > 0.0D );
        assertTrue( stats.getSearchIndexDiskMaxGB() > 0 );
    }

    @Test
    public void testLibraryIndexCounter() throws Exception {
        stats.resetLibraryIndexCount();
        assertEquals( 0, stats.getLibraryIndexCount() );

        stats.libraryIndexed();
        assertEquals( 1, stats.getLibraryIndexCount() );

        stats.libraryIndexed();
        assertEquals( 2, stats.getLibraryIndexCount() );

        stats.resetLibraryIndexCount();
        assertEquals( 0, stats.getLibraryIndexCount() );
    }

}

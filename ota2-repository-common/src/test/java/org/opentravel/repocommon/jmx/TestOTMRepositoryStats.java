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

package org.opentravel.repocommon.jmx;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.repocommon.jmx.OTMRepositoryStats;

import java.io.File;

/**
 * Verifies the functions of the <code>OTMRepositoryStatsMBean</code>.
 */
public class TestOTMRepositoryStats {

    private static File validFolder = new File( System.getProperty( "user.dir" ) );
    private static File invalidFolder = new File( System.getProperty( "user.dir" ) + "/invalid-folder-location" );

    private OTMRepositoryStats stats;

    @Before
    public void setup() {
        stats = new OTMRepositoryStats();
        stats.setSvnServerLocation( invalidFolder );
        stats.setRepositoryLocation( validFolder );
        stats.setSearchIndexLocation( validFolder );
    }

    @Test
    public void testCpuUtilizationCheck() throws Exception {
        assertTrue( stats.getCpuUtilization() >= 0.0D );
    }

    @Test
    public void testMemoryUtilizationCheck() throws Exception {
        assertTrue( stats.getMemoryUtilization() >= 0.0D );
    }

    @Test
    public void testDiskUtilizationChecks() throws Exception {
        // Check stats for valid folder locations
        assertTrue( stats.getSearchIndexDiskUtilization() > 0.0D );
        assertTrue( stats.getSearchIndexDiskMaxGB() > 0 );
        assertTrue( stats.getRepositoryDataDiskUtilization() > 0.0D );
        assertTrue( stats.getRepositoryDataDiskMaxGB() > 0 );

        // Check stats for invalid folder locations
        assertTrue( stats.getSvnServerDiskUtilization() < 0.0D );
        assertTrue( stats.getSvnServerDiskMaxGB() < 0 );
    }

    @Test
    public void testRepositoryAvailable() throws Exception {
        stats.setRepositoryAvailable( false );
        assertFalse( stats.isRepositoryAvailable() );

        stats.setRepositoryAvailable( true );
        assertTrue( stats.isRepositoryAvailable() );
    }

    @Test
    public void testSvnServiceAvailable() throws Exception {
        stats.setSvnServiceAvailable( false );
        assertFalse( stats.isSvnServiceAvailable() );

        stats.setSvnServiceAvailable( true );
        assertTrue( stats.isSvnServiceAvailable() );
    }

    @Test
    public void testSvnUserConfigOk() throws Exception {
        stats.setSvnUserConfigOk( false );
        assertFalse( stats.isSvnUserConfigOk() );

        stats.setSvnUserConfigOk( true );
        assertTrue( stats.isSvnUserConfigOk() );
    }

}

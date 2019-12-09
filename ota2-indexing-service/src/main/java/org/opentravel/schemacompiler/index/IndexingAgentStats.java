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

import org.opentravel.schemacompiler.jmx.OTMStandardMBean;

import java.io.File;

/**
 * Implementation of the <code>IndexingAgentStatsMBean</code> used to monitor and manage the indexing agent process.
 */
public class IndexingAgentStats extends OTMStandardMBean implements IndexingAgentStatsMBean {

    public static final String MBEAN_NAME = "org.opentravel.jmx:type=IndexingAgentStatsMBean";

    private static IndexingAgentStats instance = new IndexingAgentStats();

    private boolean agentAvailable = true;
    private int libraryIndexCount;
    private File searchIndexLocation;

    /**
     * Default constructor.
     */
    public IndexingAgentStats() {
        super( IndexingAgentStatsMBean.class );
    }

    /**
     * Returns the default MBean instance for the JVM.
     * 
     * @return IndexingAgentStats
     */
    public static IndexingAgentStats getInstance() {
        return instance;
    }

    /**
     * Assigns the file system location of the OTM repository search index.
     *
     * @param searchIndexLocation the folder location to assign
     */
    public void setSearchIndexLocation(File searchIndexLocation) {
        this.searchIndexLocation = searchIndexLocation;
    }

    /**
     * Assigns the boolean value indicating whether the indexing agent process is available.
     * 
     * @param available boolean flag indicating the availability status of the indexing agent
     */
    public void setAvailable(boolean available) {
        agentAvailable = available;
    }

    /**
     * Called when a library has been indexed by the agent to increment the index count.
     */
    public void libraryIndexed() {
        libraryIndexCount++;
    }

    /**
     * @see org.opentravel.schemacompiler.index.IndexingManagerStatsMBean#isAvailable()
     */
    @Override
    public boolean isAvailable() {
        return agentAvailable;
    }

    /**
     * @see org.opentravel.schemacompiler.jmx.OTMRepositoryStatsMBean#getSearchIndexDiskUtilization()
     */
    @Override
    public double getSearchIndexDiskUtilization() {
        return getDiskVolumeUtilization( searchIndexLocation );
    }

    /**
     * @see org.opentravel.schemacompiler.jmx.OTMRepositoryStatsMBean#getSearchIndexDiskMaxGB()
     */
    @Override
    public int getSearchIndexDiskMaxGB() {
        return getDiskVolumeMaxGB( searchIndexLocation );
    }

    /**
     * @see org.opentravel.schemacompiler.index.IndexingAgentStatsMBean#getLibraryIndexCount()
     */
    @Override
    public int getLibraryIndexCount() {
        return libraryIndexCount;
    }

    /**
     * @see org.opentravel.schemacompiler.index.IndexingAgentStatsMBean#resetLibraryIndexCount()
     */
    @Override
    @SuppressWarnings("squid:S2696")
    public void resetLibraryIndexCount() {
        libraryIndexCount = 0;
    }

}

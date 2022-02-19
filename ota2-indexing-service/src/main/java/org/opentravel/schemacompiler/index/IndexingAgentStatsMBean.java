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

import org.opentravel.repocommon.jmx.OTMBasicStatsMBean;

/**
 * MBean interface for monitoring and management of the <code>IndexingAgent</code> process.
 */
public interface IndexingAgentStatsMBean extends OTMBasicStatsMBean {

    /**
     * Returns true when the indexing agent process is available.
     * 
     * @return boolean
     */
    public boolean isAvailable();

    /**
     * Returns the disk utilization as a percentage for the volume used by OTM repository search index.
     * 
     * @return double
     */
    public double getSearchIndexDiskUtilization();

    /**
     * Returns the maximum amount of storage (in GB) available to the OTM repository search index.
     * 
     * @return int
     */
    public int getSearchIndexDiskMaxGB();

    /**
     * Returns the number of libraries that have been indexed by the agent.
     * 
     * @return int
     */
    public int getLibraryIndexCount();

    /**
     * Resets the library index count to zero.
     */
    public void resetLibraryIndexCount();

}

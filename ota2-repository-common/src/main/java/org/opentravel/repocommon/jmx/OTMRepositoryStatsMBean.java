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

/**
 * MBean interface for monitoring and management of the OTM repository process.
 */
public interface OTMRepositoryStatsMBean extends OTMBasicStatsMBean {

    /**
     * Returns true when the OTM repository server is available.
     * 
     * @return boolean
     */
    public boolean isRepositoryAvailable();

    /**
     * Returns true when the Subversion server is available.
     * 
     * @return boolean
     */
    public boolean isSvnServiceAvailable();

    /**
     * Returns true if the Subversion user configuration settings are configured correctly for the OTM repository.
     * 
     * @return boolean
     */
    public boolean isSvnUserConfigOk();

    /**
     * Returns the disk utilization as a percentage for the volume used by the Subversion server.
     * 
     * @return double
     */
    public double getSvnServerDiskUtilization();

    /**
     * Returns the maximum amount of storage (in GB) available to the Subversion server.
     * 
     * @return int
     */
    public int getSvnServerDiskMaxGB();

    /**
     * Returns the disk utilization as a percentage for the volume used for OTM repository storage.
     * 
     * @return double
     */
    public double getRepositoryDataDiskUtilization();

    /**
     * Returns the maximum amount of storage (in GB) available for OTM repository storage.
     * 
     * @return int
     */
    public int getRepositoryDataDiskMaxGB();

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

}

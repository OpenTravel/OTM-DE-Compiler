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

package org.opentravel.schemacompiler.jmx;

import java.io.File;

/**
 * Implementation of the <code>OTMRepositoryStatsMBean</code> used to monitor and manage the OTM repository process.
 */
public class OTMRepositoryStats extends OTMStandardMBean implements OTMRepositoryStatsMBean {

    public static final String MBEAN_NAME = "org.opentravel.jmx:type=OTMRepositoryStats";

    private static OTMRepositoryStats instance = new OTMRepositoryStats();

    private File svnServerLocation;
    private File repositoryLocation;
    private File searchIndexLocation;

    private boolean repositoryAvailable = true;
    private boolean svnServiceAvailable = false;
    private boolean svnUserConfigOk = false;

    /**
     * Default constructor.
     */
    public OTMRepositoryStats() {
        super( OTMRepositoryStatsMBean.class );
    }

    /**
     * Returns the default MBean instance for the JVM.
     * 
     * @return OTMRepositoryStats
     */
    public static OTMRepositoryStats getInstance() {
        return instance;
    }

    /**
     * Assigns the file system location of the Subversion server (null if SVN storage is not configured or the server is
     * running on a remote host)
     *
     * @param svnServerLocation the folder location to assign
     */
    public void setSvnServerLocation(File svnServerLocation) {
        this.svnServerLocation = svnServerLocation;
    }

    /**
     * Assigns the file system location of the OTM repository data files.
     *
     * @param repositoryLocation the folder location to assign
     */
    public void setRepositoryLocation(File repositoryLocation) {
        this.repositoryLocation = repositoryLocation;
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
     * Assigns the boolean value indicating whether the OTM repository is available.
     * 
     * @param available boolean flag indicating the availability status of the repository
     */
    public void setRepositoryAvailable(boolean available) {
        repositoryAvailable = available;
    }

    /**
     * Assigns the boolean value indicating whether the Subversion server process is available.
     * 
     * @param available boolean flag indicating the availability status of the Subversion server
     */
    public void setSvnServiceAvailable(boolean available) {
        svnServiceAvailable = available;
    }

    /**
     * Assigns the boolean value indicating whether the Subversion user credentials are configured correctly for the OTM
     * repository.
     * 
     * @param configOk boolean flag indicating the configuration status of the Subversion user credentials
     */
    public void setSvnUserConfigOk(boolean configOk) {
        svnUserConfigOk = configOk;
    }

    /**
     * @see org.opentravel.schemacompiler.jmx.OTMRepositoryStatsMBean#isRepositoryAvailable()
     */
    @Override
    public boolean isRepositoryAvailable() {
        return repositoryAvailable;
    }

    /**
     * @see org.opentravel.schemacompiler.jmx.OTMRepositoryStatsMBean#isSvnServiceAvailable()
     */
    @Override
    public boolean isSvnServiceAvailable() {
        return svnServiceAvailable;
    }

    /**
     * @see org.opentravel.schemacompiler.jmx.OTMRepositoryStatsMBean#isSvnUserConfigOk()
     */
    @Override
    public boolean isSvnUserConfigOk() {
        return svnUserConfigOk;
    }

    /**
     * @see org.opentravel.schemacompiler.jmx.OTMRepositoryStatsMBean#getSvnServerDiskUtilization()
     */
    @Override
    public double getSvnServerDiskUtilization() {
        return getDiskVolumeUtilization( svnServerLocation );
    }

    /**
     * @see org.opentravel.schemacompiler.jmx.OTMRepositoryStatsMBean#getSvnServerDiskMaxGB()
     */
    @Override
    public int getSvnServerDiskMaxGB() {
        return getDiskVolumeMaxGB( svnServerLocation );
    }

    /**
     * @see org.opentravel.schemacompiler.jmx.OTMRepositoryStatsMBean#getRepositoryDataDiskUtilization()
     */
    @Override
    public double getRepositoryDataDiskUtilization() {
        return getDiskVolumeUtilization( repositoryLocation );
    }

    /**
     * @see org.opentravel.schemacompiler.jmx.OTMRepositoryStatsMBean#getRepositoryDataDiskMaxGB()
     */
    @Override
    public int getRepositoryDataDiskMaxGB() {
        return getDiskVolumeMaxGB( repositoryLocation );
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

}

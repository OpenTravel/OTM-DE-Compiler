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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.repocommon.jmx.OTMStandardMBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the <code>IndexingManagerStatsMBean</code> used to monitor and manage the indexing manager process.
 */
public class IndexingManagerStats extends OTMStandardMBean implements IndexingManagerStatsMBean {

    public static final String MBEAN_NAME = "org.opentravel.jmx:type=IndexingManagerStatsMBean";

    private static Logger log = LogManager.getLogger( IndexingManagerStats.class );
    private static IndexingManagerStats instance = new IndexingManagerStats();

    private boolean managerAvailable = true;
    private List<Long> agentRestartTimestamps = new ArrayList<>();
    private long lastAgentStartup = -1;

    /**
     * Default constructor.
     */
    public IndexingManagerStats() {
        super( IndexingManagerStatsMBean.class );
    }

    /**
     * Returns the default MBean instance for the JVM.
     * 
     * @return IndexingManagerStats
     */
    public static IndexingManagerStats getInstance() {
        return instance;
    }

    /**
     * Assigns the boolean value indicating whether the indexing manager process is available.
     * 
     * @param available boolean flag indicating the availability status of the indexing manager
     */
    public void setAvailable(boolean available) {
        managerAvailable = available;
    }

    /**
     * Called to notify the MBean that an indexing agent process has been started (or restarted) by the indexing
     * manager.
     * 
     * @param isRestart flag indicating whether the launch of the agent process was due to a retart
     */
    public void agentStarted(boolean isRestart) {
        lastAgentStartup = System.currentTimeMillis();

        synchronized (agentRestartTimestamps) {
            if (isRestart) {
                agentRestartTimestamps.add( lastAgentStartup );

            } else {
                agentRestartTimestamps.clear();
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.index.IndexingManagerStatsMBean#isAvailable()
     */
    @Override
    public boolean isAvailable() {
        return managerAvailable;
    }

    /**
     * @see org.opentravel.schemacompiler.index.IndexingManagerStatsMBean#getMinutesSinceLastAgentStartup()
     */
    @Override
    public long getMinutesSinceLastAgentStartup() {
        return (System.currentTimeMillis() - lastAgentStartup) / 60000L;
    }

    /**
     * @see org.opentravel.schemacompiler.index.IndexingManagerStatsMBean#getRecentAgentRestartCount()
     */
    @Override
    public int getRecentAgentRestartCount() {
        synchronized (agentRestartTimestamps) {
            long fifteenMinutesAgo = System.currentTimeMillis() - 900000L;
            Iterator<Long> iterator = agentRestartTimestamps.iterator();

            // Purge any restart times from the list that are older than 15 minutes
            while (iterator.hasNext()) {
                Long rts = iterator.next();

                if (rts < fifteenMinutesAgo) {
                    iterator.remove();
                }
            }
            return agentRestartTimestamps.size();
        }
    }

    /**
     * @see org.opentravel.schemacompiler.index.IndexingManagerStatsMBean#shutdown()
     */
    @Override
    public void shutdown() {
        log.info( "Shutdown request received" );
        IndexProcessManager.shutdown();
        log.info( "Shutdown complete" );
    }

}

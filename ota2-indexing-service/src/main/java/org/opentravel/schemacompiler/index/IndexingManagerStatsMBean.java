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
 * MBean interface for monitoring and management of the <code>IndexProcessManager</code> process.
 */
public interface IndexingManagerStatsMBean extends OTMBasicStatsMBean {

    /**
     * Returns true when the indexing manager process is available.
     * 
     * @return boolean
     */
    public boolean isAvailable();

    /**
     * Returns the number of minutes since the last indexing agent process was started.
     * 
     * @return long
     */
    public long getMinutesSinceLastAgentStartup();

    /**
     * Returns the number of indexing agent restarts that have occurred within the last 15 minutes.
     * 
     * @return int
     */
    public int getRecentAgentRestartCount();

    /**
     * JMX hook to shutdown the <code>IndexProcessManager</code> and any associated indexing agent child processes.
     */
    public void shutdown();

}

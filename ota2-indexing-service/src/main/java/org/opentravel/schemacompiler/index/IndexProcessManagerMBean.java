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

/**
 * MBean interface for the <code>IndexProcessManager</code>.
 * 
 * @author S. Livezey
 */
public interface IndexProcessManagerMBean {
	
	public static final int JMX_PORT = 8080;
	public static final String JMX_SERVER_URL = "service:jmx:rmi:///jndi/rmi://localhost:" + JMX_PORT + "/jmxrmi";
	public static final String MBEAN_NAME = "org.opentravel.mbeans:type=IndexProcessManagerMBean";
	
	/**
	 * JMX hook to shutdown the <code>IndexProcessManager</code> and any
	 * associated indexing agent child processes.
	 */
	public void shutdown();
	
}

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

import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class that initiates the shutdown of the <code>IndexProcessManager</code> service.
 */
public class ShutdownIndexingService {
	
    private static Log log = LogFactory.getLog( ShutdownIndexingService.class );
    
	/**
	 * Main method invoked from the command-line.
	 * 
	 * @param args  the command-line arguments (ignored)
	 */
	public static void main(String[] args) {
		try {
	        ObjectName name = new ObjectName( IndexProcessManagerMBean.MBEAN_NAME );
			JMXServiceURL jmxUrl = new JMXServiceURL( IndexProcessManager.getJmxServerUrl() );
			JMXConnector jmxc = JMXConnectorFactory.connect( jmxUrl, null );
			
			jmxc.connect();
			jmxc.getMBeanServerConnection().invoke(name, "shutdown", null, null);
			
		} catch (Exception e) {
			log.error("Error attempting to shut down indexing service.", e);
		}
	}
	
}

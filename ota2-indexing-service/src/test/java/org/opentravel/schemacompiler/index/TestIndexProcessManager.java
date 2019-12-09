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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Verifies the functions of the <code>IndexProcessManager</code> class.
 */
public class TestIndexProcessManager extends AbstractIndexingServiceTest {

    @Before
    public void setup() throws Exception {
        setupEnvironment();
    }

    @Test
    public void testStartupAndShutdown() throws Exception {
        Thread pmThread = new Thread( () -> {
            IndexProcessManager.main( new String[0] );
        } );

        // Start the process manager and wait for everything to be up and running
        IndexProcessManager.debugMode = true;
        pmThread.start();

        synchronized (IndexProcessManager.class) {
            IndexProcessManager.class.wait( 5000 ); // wait for up to five seconds
        }

        // Kill the agent process and verify that a new one is started up to replace it
        Process agentProcess = IndexProcessManager.getAgentProcess();

        agentProcess.destroyForcibly();
        agentProcess.waitFor( 5, TimeUnit.SECONDS );

        synchronized (IndexProcessManager.class) {
            IndexProcessManager.class.wait( 5000 ); // wait for up to five seconds
        }
        assertNotNull( IndexProcessManager.getAgentProcess() );
        assertFalse( agentProcess == IndexProcessManager.getAgentProcess() );

        // Gracefully shut down the process manager using the MBean hook
        new IndexingManagerStats().shutdown();
        pmThread.join( 5000 );
    }

    @Test
    public void testRemoteProcessShutdown() throws Exception {
        JMXServiceURL jmxUrl = new JMXServiceURL( "service:jmx:rmi:///jndi/rmi://localhost:12001/jmxrmi" );
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        JMXConnectorServer jmxServer;

        // Start a JMX server that will allow us to call the shutdown hook as a remote service
        LocateRegistry.createRegistry( 12001 );
        jmxServer = JMXConnectorServerFactory.newJMXConnectorServer( jmxUrl, null, mbs );
        jmxServer.start();

        // Launch the Indexing Manager process
        Thread pmThread = new Thread( () -> {
            IndexProcessManager.main( new String[0] );
        } );

        // Start the process manager and wait for everything to be up and running
        IndexProcessManager.debugMode = true;
        pmThread.start();

        synchronized (IndexProcessManager.class) {
            IndexProcessManager.class.wait( 5000 ); // wait for up to five seconds
        }

        // Shutdown the service using the command-line utility that invokes the remote JMX service
        ShutdownIndexingService.main( new String[0] );
        pmThread.join( 5000 );
    }

}

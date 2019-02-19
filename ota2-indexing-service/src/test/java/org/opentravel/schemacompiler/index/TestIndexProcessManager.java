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

import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Verifies the functions of the <code>IndexProcessManager</code> class.
 */
public class TestIndexProcessManager extends AbstractIndexingServiceTest {
    
    @BeforeClass
    public static void setup() throws Exception {
        setupEnvironment();
    }
    
    @Test
    @Ignore
    @SuppressWarnings("squid:S2925")
    public void testStartupAndShutdown() throws Exception {
        ProcessManagerRunner pmRunner = new ProcessManagerRunner();
        Thread pmThread = new Thread( pmRunner );
        
        // Start the process manager and wait for everything to be up and running
        IndexProcessManager.debugMode = true;
        pmThread.start();
        
        for (int i = 0; i < 100; i++) {
            if (pmRunner.isRunning()) {
                break;
            }
        }
        
        // Kill the agent process and verify that a new one is started up to replace it
        Process agentProcess = IndexProcessManager.getAgentProcess();
        
        agentProcess.destroyForcibly();
        agentProcess.waitFor( 5, TimeUnit.SECONDS );
        Thread.sleep( 500 );
        assertNotNull( IndexProcessManager.getAgentProcess() );
        assertFalse( agentProcess == IndexProcessManager.getAgentProcess() );
        
        // Gracefully shut down the process manager
        ShutdownIndexingService.main( new String[0] );
        pmThread.join( 5000 );
    }
    
    /**
     * Used to launch the <code>IndexProcessManager</code> as a background task.
     */
    private static class ProcessManagerRunner implements Runnable {

        @Override
        public void run() {
            IndexProcessManager.main( new String[0] );
        }
        
        public boolean isRunning() {
            return IndexProcessManager.isRunning();
        }
    }
    
}

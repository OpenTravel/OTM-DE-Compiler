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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolutionException;

import java.io.FileNotFoundException;

/**
 * Performs negative test cases for the <code>IndexingAgent</code> class.
 */
public class TestIndexingAgentNegative extends AbstractIndexingServiceTest {

    private static final Object threadLock = new Object();

    @BeforeClass
    public static void setup() throws Exception {
        setupEnvironment();
    }

    @Test(expected = FileNotFoundException.class)
    public void testNullAgentConfig() throws Exception {
        new IndexingAgent( "NULL" );
    }

    @Test(expected = FileNotFoundException.class)
    public void testMissingAgentConfig() throws Exception {
        new IndexingAgent( "src/test/resources/test-config/bad-config-file.xml" );
    }

    @Test(expected = FileNotFoundException.class)
    public void testInvalidRepositoryLocation() throws Exception {
        new IndexingAgent( "src/test/resources/test-config/indexing-agent-invalidrepo.xml" );
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testInvalidSearchIndexLocation() throws Exception {
        new IndexingAgent( "src/test/resources/test-config/indexing-agent-invalidindex.xml" );
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testMissingJmsConfig() throws Exception {
        new IndexingAgent( "src/test/resources/test-config/indexing-agent-nojms.xml" );
    }

    @Test(expected = IndexingRuntimeException.class)
    public void testJmsReceiveError() throws Exception {
        synchronized (threadLock) {
            IndexingAgent agent = new IndexingAgent( "src/test/resources/test-config/indexing-agent-mockjms.xml" );
            try {
                JmsTemplate jmsTemplate = agent.getJmsService();

                when( jmsTemplate.receiveSelected( anyString() ) )
                    .thenThrow( new DestinationResolutionException( "JMS receive error" ) );
                agent.startListening();

            } finally {
                agent.shutdown( false );
                agent.closeIndexWriter();
            }
        }
    }

    @Test(expected = IndexingRuntimeException.class)
    public void testUnknownReceiveError() throws Exception {
        synchronized (threadLock) {
            IndexingAgent agent = new IndexingAgent( "src/test/resources/test-config/indexing-agent-mockjms.xml" );
            try {
                JmsTemplate jmsTemplate = agent.getJmsService();

                when( jmsTemplate.receiveSelected( anyString() ) ).thenThrow( NullPointerException.class );
                agent.startListening();

            } finally {
                agent.shutdown( false );
                agent.closeIndexWriter();
            }
        }
    }

}

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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.activemq.broker.BrokerService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoListType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.opentravel.schemacompiler.util.RepositoryJaxbContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import java.io.StringWriter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * Verifies the functions of the <code>IndexingAgent</code> class.
 */
public class TestIndexingAgent extends AbstractIndexingServiceTest {

    private static FileSystemXmlApplicationContext context;
    private static BrokerService amqBroker;
    private static IndexingAgent indexAgent;
    private static JmsTemplate jmsService;

    @BeforeClass
    public static void setup() throws Exception {
        setupEnvironment();

        String configFileLocation = System.getProperty( IndexProcessManager.MANAGER_CONFIG_SYSPROP );

        context = new FileSystemXmlApplicationContext( configFileLocation );
        amqBroker = (BrokerService) context.getBean( IndexProcessManager.AMQ_BROKER_BEANID );
        amqBroker.start();
        indexAgent = new IndexingAgent();

        new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    indexAgent.startListening();

                } catch (Exception e) {
                    e.printStackTrace( System.out );
                }
            }
        } ).start();

        for (int i = 0; i < 20; i++) {
            if (indexAgent.isRunning()) {
                break;
            }
        }
        if (!indexAgent.isRunning()) {
            fail( "Failed to start indexing agent." );
        }
        jmsService = (JmsTemplate) context.getBean( "indexingJmsService" );
    }

    @AfterClass
    public static void tearDown() throws Exception {
        indexAgent.shutdown();
        indexAgent.closeIndexWriter();
        amqBroker.stop();
        amqBroker.waitUntilStopped();
        context.close();
    }

    @Test
    public void testIndexRepositoryItem() throws Exception {
        RepositoryItem item = repositoryManager.getRepositoryItem(
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", "Version_Test_1_0_0.otm", "1.0.0" );
        JAXBContext jaxbContext = RepositoryJaxbContext.getContext();
        LibraryInfoListType metadataList = new LibraryInfoListType();
        Marshaller m = jaxbContext.createMarshaller();
        StringWriter writer = new StringWriter();

        metadataList.getLibraryInfo().add( RepositoryUtils.createItemMetadata( item ) );
        m.marshal( new ObjectFactory().createLibraryInfoList( metadataList ), writer );
        sendIndexingJob( IndexingConstants.JOB_TYPE_CREATE_INDEX, writer.toString() );
        waitForCommitMessage();
    }

    @Test
    public void testDeleteRepositoryItemIndex() throws Exception {
        RepositoryItem item = repositoryManager.getRepositoryItem(
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", "Version_Test_1_0_0.otm", "1.0.0" );
        JAXBContext jaxbContext = RepositoryJaxbContext.getContext();
        LibraryInfoListType metadataList = new LibraryInfoListType();
        Marshaller m = jaxbContext.createMarshaller();
        StringWriter writer = new StringWriter();

        metadataList.getLibraryInfo().add( RepositoryUtils.createItemMetadata( item ) );
        m.marshal( new ObjectFactory().createLibraryInfoList( metadataList ), writer );
        sendIndexingJob( IndexingConstants.JOB_TYPE_DELETE_INDEX, writer.toString() );
        waitForCommitMessage();
    }

    @Test
    public void testIndexSubscriptionTarget() throws Exception {
        SubscriptionTarget target = new SubscriptionTarget();
        JAXBContext jaxbContext = RepositoryJaxbContext.getExtContext();
        Marshaller m = jaxbContext.createMarshaller();
        StringWriter writer = new StringWriter();

        target.setBaseNamespace( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test" );
        m.marshal(
            new org.opentravel.ns.ota2.repositoryinfoext_v01_00.ObjectFactory().createSubscriptionTarget( target ),
            writer );
        sendIndexingJob( IndexingConstants.JOB_TYPE_SUBSCRIPTION, writer.toString() );
        waitForCommitMessage();
    }

    @Test
    public void testDeleteAll() throws Exception {
        sendIndexingJob( IndexingConstants.JOB_TYPE_DELETE_ALL, null );
        waitForCommitMessage();
    }

    private void sendIndexingJob(final String jobType, final String messageContent) {
        jmsService.send( new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                TextMessage msg = session.createTextMessage();

                msg.setStringProperty( IndexingConstants.MSGPROP_JOB_TYPE, jobType );
                msg.setIntProperty( IndexingConstants.MSGPROP_SELECTOR, IndexingConstants.SELECTOR_VALUE_JOBMSG );
                msg.setText( messageContent );
                return msg;
            }
        } );
    }

    @SuppressWarnings("squid:S2925")
    private static synchronized void waitForCommitMessage() {
        assertNotNull( jmsService.receiveSelected( IndexingConstants.SELECTOR_COMMITMSG ) );
    }

}

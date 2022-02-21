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
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.repocommon.index.IndexingConstants;
import org.opentravel.repocommon.index.builder.IndexBuilder;
import org.opentravel.repocommon.index.builder.IndexBuilderFactory;
import org.opentravel.repocommon.subscription.SubscriptionNavigator;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * Main entry point for the agent that handles indexing requests in response to messages received from the remote
 * repository server.
 * 
 * @author S. Livezey
 */
public class IndexingAgent {

    private static Logger log = LogManager.getLogger( IndexingAgent.class );

    public static final String JMS_TEMPLATE_BEANID = "indexingJmsService";
    public static final String REPOSITORY_LOCATION_BEANID = "repositoryLocation";
    public static final String SEARCH_INDEX_LOCATION_BEANID = "searchIndexLocation";

    private boolean shutdownRequested = false;
    private File searchIndexLocation;
    private RepositoryManager repositoryManager;
    private IndexingJobManager jobManager;
    private Directory indexDirectory;
    private IndexWriterConfig writerConfig;
    private IndexWriter indexWriter;
    private JmsTemplate jmsTemplate;
    private boolean running;

    /**
     * Default constructor.
     * 
     * @throws RepositoryException thrown if the repository manager cannot be initialized
     * @throws IOException thrown if the search index writer cannot be initialized
     */
    public IndexingAgent() throws RepositoryException, IOException {
        this( (String) null );
    }

    /**
     * Constructor that allows the caller to override the value of the system property for the config location (used for
     * testing purposes only). Setting the override to a value of <code>NULL</code> will be interpreted as a null
     * override value.
     * 
     * @param overrideConfigLocation the override location for the agent's spring configuration file
     * @throws RepositoryException thrown if the repository manager cannot be initialized
     * @throws IOException thrown if the search index writer cannot be initialized
     */
    public IndexingAgent(String overrideConfigLocation) throws RepositoryException, IOException {
        initializeContext( overrideConfigLocation );
    }

    /**
     * Constructor that allows the caller to supply a pre-configured Spring application context for the new agent
     * instance.
     * 
     * @param context the Spring application context
     * @throws RepositoryException thrown if the repository manager cannot be initialized
     * @throws IOException thrown if the search index writer cannot be initialized
     */
    public IndexingAgent(ApplicationContext context) throws RepositoryException, IOException {
        initializeContext( context );
    }

    /**
     * Initializes the Spring application context and all of the properties that are obtained from it.
     * 
     * @param overrideConfigLocation the override location for the agent's spring configuration file
     * @throws RepositoryException thrown if errors are detected in the agent configuration settings
     * @throws IOException thrown if the indexing agent configuration file does not exist in the specified location or
     *         the search index cannot be initialized
     */
    @SuppressWarnings("resource")
    private void initializeContext(String overrideConfigLocation) throws RepositoryException, IOException {
        String configFileLocation = overrideConfigLocation;
        File configFile;

        if (configFileLocation == null) {
            configFileLocation = System.getProperty( IndexProcessManager.AGENT_CONFIG_SYSPROP );

        } else if (configFileLocation.equals( "NULL" )) {
            configFileLocation = null;
        }

        // Verify the existence of the configuration file and initialize the context
        if (configFileLocation == null) {
            throw new FileNotFoundException( "The location of the agent configuration file has not be specified "
                + "(use the 'ota2.index.agent.config' system property)." );
        }
        configFile = new File( configFileLocation );

        if (!configFile.exists() || !configFile.isFile()) {
            throw new FileNotFoundException( "Index agent configuration file not found: " + configFileLocation );
        }
        initializeContext( new FileSystemXmlApplicationContext( configFileLocation ) );
    }

    /**
     * Initializes the indexing agent using the Spring application context provided.
     * 
     * @param context the Spring application context
     * @throws RepositoryException thrown if errors are detected in the agent configuration settings
     * @throws IOException thrown if the indexing agent configuration file does not exist in the specified location or
     *         the search index cannot be initialized
     */
    private void initializeContext(ApplicationContext context) throws RepositoryException, IOException {
        // Initialize the agent settings from the application context
        this.jmsTemplate = (JmsTemplate) context.getBean( JMS_TEMPLATE_BEANID );
        String repositoryLocationPath = (String) context.getBean( REPOSITORY_LOCATION_BEANID );
        String searchIndexLocationPath = (String) context.getBean( SEARCH_INDEX_LOCATION_BEANID );

        // Perform some preliminary error checking on the agent's configuration settings
        if (jmsTemplate == null) {
            throw new RepositoryException( "JMS configuration not specified in the agent configuration settings." );
        }
        if (repositoryLocationPath == null) {
            throw new FileNotFoundException(
                "OTM repository location not specified in the agent configuration settings." );
        }
        if (searchIndexLocationPath == null) {
            throw new FileNotFoundException(
                "Search index location not specified in the agent configuration settings." );
        }
        File repositoryLocation = new File( repositoryLocationPath );

        searchIndexLocation = new File( searchIndexLocationPath );

        if (!repositoryLocation.exists() || !repositoryLocation.isDirectory()) {
            throw new FileNotFoundException( "Invalid OTM repository location specified: " + repositoryLocationPath );
        }
        repositoryManager = new RepositoryManager( repositoryLocation );
        jobManager = new IndexingJobManager( repositoryManager, searchIndexLocation );
        running = false;

        // Check to make sure the index was properly closed, and release any lock that might exist
        if (!searchIndexLocation.exists()) {
            searchIndexLocation.mkdirs();
        }
        this.indexDirectory = FSDirectory.open( searchIndexLocation.toPath() );

        // Configure the indexing and search components
        this.writerConfig = new IndexWriterConfig( new StandardAnalyzer() );
        this.writerConfig.setOpenMode( OpenMode.CREATE_OR_APPEND );
        this.indexWriter = new IndexWriter( indexDirectory, writerConfig );

        // Run an empty commit of the index writer; this will initialize the search index directory
        // if it was not already setup prior to launching this agent.
        indexWriter.commit();
    }

    /**
     * Registers the MBeans that enable monitoring for the agent process.
     * 
     * @throws IOException thrown if the JMX service cannot be launched
     */
    private void configureMonitoring() throws IOException {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName( IndexingAgentStats.MBEAN_NAME );

            if (!mbs.isRegistered( name )) {
                IndexingAgentStats agentStats = IndexingAgentStats.getInstance();

                agentStats.setSearchIndexLocation( searchIndexLocation );
                mbs.registerMBean( agentStats, name );
            }

        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException
            | NotCompliantMBeanException e) {
            throw new IOException( e );
        }
    }

    /**
     * Returns the <code>JmsTemplate</code> being used by this service for messaging (intended for testing purposes
     * only).
     * 
     * @return JmsTemplate
     */
    protected JmsTemplate getJmsService() {
        return jmsTemplate;
    }

    /**
     * Permanently closes the index writer for this agent.
     */
    protected void closeIndexWriter() {
        if (indexWriter != null) {
            try {
                indexWriter.close();

            } catch (IOException e) {
                // Ignore error and return

            } finally {
                indexWriter = null;
            }
        }
    }

    /**
     * Starts listening for JMS messages and performs the appropriate indexing action when one is received.
     * 
     * @throws JMSException thrown if a fatal error occurs because the JMS connection is not available
     * @throws IOException thrown if a watch service cannot be created for the local file system
     */
    public void startListening() throws JMSException, IOException {
        Thread jobThread = new Thread( this::watchForIndexingJobs, "Job Indexing Thread" );
        boolean initialStartup = true;

        configureMonitoring();
        checkJmsAvailable();
        jobThread.start();
        running = true;

        log.info( "Indexing agent started for location: " + searchIndexLocation.getAbsolutePath() );
        IndexingAgentStats.getInstance().setAvailable( true );

        while (!shutdownRequested) {
            try {
                Message msg = jmsTemplate.receiveSelected( IndexingConstants.SELECTOR_JOBMSG );

                initialStartup = false;

                if (msg instanceof TextMessage) {
                    TextMessage message = (TextMessage) msg;
                    String messageType = message.getStringProperty( IndexingConstants.MSGPROP_JOB_TYPE );
                    String messageContent = message.getText();

                    if (messageType != null) {
                        jobManager.addIndexingJobs( messageType, messageContent );

                    } else {
                        log.warn( "Job type not specified in indexing request - ignoring." );
                    }
                }

            } catch (Exception e) {
                log.error( "Error receiving indexing job.", e );

                if (initialStartup) {
                    throw new IndexingRuntimeException( "Receive error during indexing agent startup", e );
                }
            }
        }
        IndexingAgentStats.getInstance().setAvailable( false );

        // Wait for up to 30s the background indexing job thread to complete
        try {
            jobThread.join( 30000L );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

        } finally {
            running = false;
        }
    }

    /**
     * Continuously scans for new indexing batch jobs until a shutdown is requested. When a new job has been detected,
     * it is dispatched for processing by the indexing service.
     */
    private void watchForIndexingJobs() {
        long waitDelay = 100L;

        while (!shutdownRequested) {
            // Continue processing batch jobs until no more exist
            while (!shutdownRequested && jobManager.nextIndexingJob()) {
                try {
                    switch (jobManager.currentJobType()) {
                        case CREATE:
                            processIndexingJob( jobManager.currentRepositoryItems(), false );
                            break;
                        case DELETE:
                            processIndexingJob( jobManager.currentRepositoryItems(), true );
                            break;
                        case DELETE_ALL:
                            processDeleteAll();
                            break;
                        case SUBSCRIPTION:
                            processIndexingJob( jobManager.currentSubscriptionTarget() );
                            break;
                    }
                    commitAndNotify();
                    waitDelay = 100L;

                } catch (Exception e) {
                    log.error( "Error processing indexing job", e );
                }
            }

            // Pause briefly before checking for new files
            try {
                if (!shutdownRequested) {
                    Thread.sleep( waitDelay );
                    waitDelay = 1000L;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Verifies that the JMS provider is available by creating and closing a connection.
     * 
     * @throws JMSException thrown if the JMS provider is not available
     */
    private void checkJmsAvailable() throws JMSException {
        Connection jmsConnection = null;

        // Make sure the JMS provider is available; if not, throw a fatal exception before
        // we start listening for messages.
        try {
            jmsConnection = jmsTemplate.getConnectionFactory().createConnection();

        } finally {
            if (jmsConnection != null) {
                jmsConnection.close();
            }
        }
    }

    /**
     * Returns true if the process manager is currently running (used for testing purposes).
     * 
     * @return boolean
     */
    public boolean isRunning() {
        if (!running) {
            try {
                Thread.sleep( 100 );

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return running;
    }

    /**
     * Processes the given indexing job for the given list of items.
     * 
     * @param itemsToIndex the list of repository items to be indexed
     * @param deleteIndex flag indicating whether the index is to be created or deleted
     * @throws IOException thrown if an error occurs while processing the indexing job
     */
    protected void processIndexingJob(List<RepositoryItem> itemsToIndex, boolean deleteIndex) throws IOException {
        IndexBuilderFactory factory = new IndexBuilderFactory( repositoryManager, indexWriter );

        for (RepositoryItem item : itemsToIndex) {
            IndexBuilder<?> indexBuilder =
                deleteIndex ? factory.newDeleteIndexBuilder( item ) : factory.newCreateIndexBuilder( item );

            if (deleteIndex) {
                log.info( "Deleting index for library: " + item.getFilename() );
            } else {
                log.info( "Indexing library: " + item.getFilename() );
            }
            indexBuilder.performIndexingAction();
        }

        if (!deleteIndex) {
            factory.getFacetService().getIndexBuilder().performIndexingAction();
        }
        IndexingAgentStats.getInstance().libraryIndexed();
    }

    /**
     * Processes the given indexing job for the subscription target provided.
     * 
     * @param subscriptionTarget the subscription target to be indexed
     * @throws IOException thrown if an error occurs while processing the indexing job
     */
    protected void processIndexingJob(SubscriptionTarget subscriptionTarget) throws IOException {
        IndexBuilderFactory factory = new IndexBuilderFactory( repositoryManager, indexWriter );
        IndexBuilder<SubscriptionTarget> indexBuilder = factory.newSubscriptionIndexBuilder( subscriptionTarget );

        indexBuilder.performIndexingAction();
    }

    /**
     * Deletes the entire persistent search index. Before committing the index, this routine automatically reindexes all
     * subscription lists in the repository.
     * 
     * @throws IOException thrown if the search index cannot be deleted
     */
    private void processDeleteAll() throws IOException {
        indexWriter.deleteAll();

        try {
            new SubscriptionNavigator( repositoryManager ).navigateSubscriptions( subscriptionList -> {
                try {
                    processIndexingJob( subscriptionList.getSubscriptionTarget() );

                } catch (IOException e) {
                    log.warn( "Error indexing subscription list.", e );
                }
            } );

        } catch (RepositoryException e) {
            log.warn( "Error during reindexing of repository subscriptions.", e );
        }
    }

    /**
     * Commits and refreshes the index writer and sends a JMS message to the OTM repository as notification that an
     * indexing job has been committed.
     * 
     * @throws IOException thrown if the index writer cannot be committed or refreshed
     */
    private void commitAndNotify() throws IOException {
        try (IndexWriter writer = indexWriter) {
            writer.commit();

        } catch (Exception e) {
            log.error( "Error committing index writer", e );

        } finally {
            writerConfig = new IndexWriterConfig( new StandardAnalyzer() );
            writerConfig.setOpenMode( OpenMode.CREATE_OR_APPEND );
            indexWriter = new IndexWriter( indexDirectory, writerConfig );
        }

        jmsTemplate.send( new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                TextMessage msg = session.createTextMessage();

                msg.setIntProperty( IndexingConstants.MSGPROP_SELECTOR, IndexingConstants.SELECTOR_VALUE_COMMITMSG );
                return msg;
            }
        } );
    }

    /**
     * Requests a shutdown of this indexing agent.
     */
    public void shutdown(boolean waitUntilStopped) {
        shutdownRequested = true;

        if (waitUntilStopped) {
            // Wait for up to 30 seconds for the background threads to stop
            for (int i = 0; i < 30; i++) {
                if (running) {
                    try {
                        Thread.sleep( 1000L );

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                } else {
                    break;
                }
            }
        }
    }

    /**
     * Main method invoked from the command-line.
     * 
     * @param args the command-line arguments (ignored)
     */
    public static void main(String[] args) {
        try {
            IndexingAgent agent = new IndexingAgent();

            try {
                agent.startListening();

            } finally {
                agent.closeIndexWriter();
            }

        } catch (Exception e) {
            log.fatal( "Indexing agent encountered a fatal error.", e );
            System.exit( IndexProcessManager.FATAL_EXIT_CODE );
        }
    }

}

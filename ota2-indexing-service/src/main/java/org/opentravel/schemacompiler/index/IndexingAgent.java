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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoListType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionList;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.schemacompiler.index.builder.IndexBuilder;
import org.opentravel.schemacompiler.index.builder.IndexBuilderFactory;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.opentravel.schemacompiler.subscription.SubscriptionNavigator;
import org.opentravel.schemacompiler.subscription.SubscriptionVisitor;
import org.opentravel.schemacompiler.util.RepositoryJaxbContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * Main entry point for the agent that handles indexing requests in response to messages
 * received from the remote repository server.
 * 
 * @author S. Livezey
 */
public class IndexingAgent implements IndexingConstants {
	
    private static Log log = LogFactory.getLog(IndexingAgent.class);
    
	public static final String JMS_TEMPLATE_BEANID          = "indexingJmsService";
	public static final String REPOSITORY_LOCATION_BEANID   = "repositoryLocation";
	public static final String SEARCH_INDEX_LOCATION_BEANID = "searchIndexLocation";
	
    private static boolean shutdownRequested = false;
    
    private File repositoryLocation;
    private File searchIndexLocation;
    private RepositoryManager repositoryManager;
    private Directory indexDirectory;
    private IndexWriterConfig writerConfig;
    private IndexWriter indexWriter;
    private JmsTemplate jmsTemplate;
    
    /**
     * Default constructor.
     * 
     * @throws RepositoryException  thrown if the repository manager cannot be initialized
     * @throws IOException  thrown if the search index writer cannot be initialized
     */
    public IndexingAgent() throws RepositoryException, IOException {
        initializeContext();
        
        // Check to make sure the index was properly closed, and release any lock that might exist
        if (!searchIndexLocation.exists()) searchIndexLocation.mkdirs();
        this.indexDirectory = FSDirectory.open( searchIndexLocation.toPath() );

        // Configure the indexing and search components
        this.writerConfig = new IndexWriterConfig( new StandardAnalyzer() );
        this.writerConfig.setOpenMode( OpenMode.CREATE_OR_APPEND );
        this.indexWriter = new IndexWriter( indexDirectory, writerConfig );
        
        // Run an empty commit of the index writer; this will initialize the search index directory
        // if it was not already setup prior to launching this agent.
        indexWriter.commit();
        
    	repositoryManager = new RepositoryManager( repositoryLocation );
    }
    
	/**
	 * Initializes the Spring application context and all of the properties that
	 * are obtained from it.
	 * 
	 * @throws RepositoryException  thrown if errors are detected in the agent configuration settings
	 * @throws FileNotFoundException  thrown if the indexing agent configuration file does not
	 *								  exist in the specified location
	 */
	@SuppressWarnings("resource")
	private void initializeContext() throws RepositoryException, FileNotFoundException {
		String configFileLocation = System.getProperty( IndexProcessManager.AGENT_CONFIG_SYSPROP );
		File configFile;
		
		// Verify the existence of the configuration file and initialize the context
		if (configFileLocation == null) {
			throw new FileNotFoundException("The location of the agent configuration file has not be specified "
					+ "(use the 'ota2.index.agent.config' system property).");
		}
		configFile = new File( configFileLocation );
		
		if (!configFile.exists() || !configFile.isFile()) {
			throw new FileNotFoundException("Index agent configuration file not found: " + configFileLocation);
		}
		ApplicationContext context = new FileSystemXmlApplicationContext( configFileLocation );
		
		// Initialize the agent settings from the application context
		this.jmsTemplate = (JmsTemplate) context.getBean( JMS_TEMPLATE_BEANID );
		String repositoryLocationPath = (String) context.getBean( REPOSITORY_LOCATION_BEANID );
		String searchIndexLocationPath = (String) context.getBean( SEARCH_INDEX_LOCATION_BEANID );
		
		// Perform some preliminary error checking on the agent's configuration settings
		if (jmsTemplate == null) {
			throw new RepositoryException("JMS configuration not specified in the agent configuration settings.");
		}
		if (repositoryLocationPath == null) {
			throw new FileNotFoundException("OTM repository location not specified in the agent configuration settings.");
		}
		if (searchIndexLocationPath == null) {
			throw new FileNotFoundException("Search index location not specified in the agent configuration settings.");
		}
		repositoryLocation = new File( repositoryLocationPath );
		searchIndexLocation = new File( searchIndexLocationPath );
		
		if (!repositoryLocation.exists() || !repositoryLocation.isDirectory()) {
			throw new FileNotFoundException("Invalid OTM repository location specified: " + repositoryLocationPath);
		}
	}
	
	/**
	 * Starts listening for JMS messages and performs the appropriate indexing action when
	 * one is received.
	 * 
	 * @throws JMSException  thrown if a fatal error occurs because the JMS connection is not available
	 */
	public void startListening() throws JMSException {
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
		
		log.info("Indexing agent started for location: " + searchIndexLocation.getAbsolutePath());
		
		while (!shutdownRequested) {
			try {
				Message msg = jmsTemplate.receiveSelected( SELECTOR_JOBMSG );
				
				if (msg instanceof TextMessage) {
					TextMessage message = (TextMessage) msg;
					String messageType = message.getStringProperty( MSGPROP_JOB_TYPE );
					String messageContent = message.getText();
					
					if (messageType != null) {
						if (messageType.equals( JOB_TYPE_CREATE_INDEX )) {
							processIndexingJob( unmarshallRepositoryItems( messageContent ), false );
							
						} else if (messageType.equals( JOB_TYPE_DELETE_INDEX )) {
							processIndexingJob( unmarshallRepositoryItems( messageContent ), true );
							
						} else if (messageType.equals( JOB_TYPE_SUBSCRIPTION )) {
							processIndexingJob( unmarshallSubscriptionTarget( messageContent ), true );
							
						} else if (messageType.equals( JOB_TYPE_DELETE_ALL )) {
							processDeleteAll();
							
						} else {
							log.warn("Unrecognized indexing job type [" + messageType + "] - ignoring.");
						}
						
					} else {
						log.warn("Job type not specified in indexing request - ignoring.");
					}
				}
				
			} catch (JmsException e) {
				if (isConnectException( e )) {
					throw e; // re-throw connection exceptions as a fatal error
					
				} else {
					log.error("Error receiving indexing job.", e);
				}
			} catch (Throwable t) {
				log.error("Error receiving indexing job.", t);
			}
		}
	}
	
    /**
     * Processes the given indexing job for the given list of items.
     * 
     * @param itemsToIndex  the list of repository items to be indexed
     * @param deleteIndex  flag indicating whether the index is to be created or deleted
     * @throws IOException  thrown if an error occurs while processing the indexing job
     */
    protected void processIndexingJob(List<RepositoryItem> itemsToIndex, boolean deleteIndex) throws IOException {
    	IndexBuilderFactory factory = new IndexBuilderFactory( repositoryManager, indexWriter );
    	
    	for (RepositoryItem item : itemsToIndex) {
        	IndexBuilder<?> indexBuilder = deleteIndex ?
        			factory.newDeleteIndexBuilder( item ) : factory.newCreateIndexBuilder( item );
        	
        	if (deleteIndex) {
            	log.info("Deleting index for library: " + item.getFilename());
        	} else {
            	log.info("Indexing library: " + item.getFilename());
        	}
    		indexBuilder.performIndexingAction();
    	}
		
		if (!deleteIndex) {
			factory.getFacetService().getIndexBuilder().performIndexingAction();
			factory.getValidationService().getIndexBuilder().performIndexingAction();
		}
		indexWriter.commit();
		sendCommitNotifiation();
    }
    
    /**
     * Processes the given indexing job for the subscription target provided.
     * 
     * @param subscriptionTarget  the subscription target to be indexed
     * @param commitUpdates  commits updates to the search index before returning
     * @throws IOException  thrown if an error occurs while processing the indexing job
     */
    protected void processIndexingJob(SubscriptionTarget subscriptionTarget, boolean commitUpdates) throws IOException {
    	IndexBuilderFactory factory = new IndexBuilderFactory( repositoryManager, indexWriter );
    	IndexBuilder<SubscriptionTarget> indexBuilder = factory.newSubscriptionIndexBuilder( subscriptionTarget );
    	
    	indexBuilder.performIndexingAction();
    	
    	if (commitUpdates) {
            indexWriter.commit();
    		sendCommitNotifiation();
    	}
    }
    
	/**
	 * Deletes the entire persistent search index.  Before committing the index, this routine
	 * automatically reindexes all subscription lists in the repository.
	 * 
	 * @throws IOException  thrown if the search index cannot be deleted
	 */
	private void processDeleteAll() throws IOException {
    	log.info("Deleting search index.");
		indexWriter.deleteAll();
		
		try {
			new SubscriptionNavigator( repositoryManager ).navigateSubscriptions( new SubscriptionVisitor() {
				public void visitSubscriptionList(SubscriptionList subscriptionList) {
					try {
						processIndexingJob( subscriptionList.getSubscriptionTarget(), false );
						
					} catch (IOException e) {
						log.warn("Error indexing subscription list.", e);
					}
				}
			});
			
		} catch (RepositoryException e) {
			log.warn("Error during reindexing of repository subscriptions.", e);
		}
		indexWriter.commit();
		sendCommitNotifiation();
	}
	
	/**
	 * Unmarshalls and returns the list of <code>RepositoryItem</code>s from the given message.
	 * 
	 * @param messageContent  the raw message content to unmarshall
	 * @return List<RepositoryItem>
	 * @throws JAXBException  thrown if an error occurs during unmarshalling
	 * @throws IOException  thrown if the message content is unreadable
	 */
	@SuppressWarnings("unchecked")
	private List<RepositoryItem> unmarshallRepositoryItems(String messageContent) throws JAXBException, IOException {
		try (Reader reader = new StringReader( messageContent )) {
			JAXBContext jaxbContext = RepositoryJaxbContext.getContext();
			Unmarshaller u = jaxbContext.createUnmarshaller();
			JAXBElement<LibraryInfoListType> msgElement = (JAXBElement<LibraryInfoListType>) u.unmarshal( reader );
			List<RepositoryItem> itemList = new ArrayList<>();
			
			for (LibraryInfoType msgItem : msgElement.getValue().getLibraryInfo()) {
				itemList.add( RepositoryUtils.createRepositoryItem( repositoryManager, msgItem ) );
			}
			return itemList;
		}
	}
	
	/**
	 * Unmarshalls and returns the <code>SubscriptionTarget</code>s from the given message.
	 * 
	 * @param messageContent  the raw message content to unmarshall
	 * @return SubscriptionTarget
	 * @throws JAXBException  thrown if an error occurs during unmarshalling
	 * @throws IOException  thrown if the message content is unreadable
	 */
	@SuppressWarnings("unchecked")
	private SubscriptionTarget unmarshallSubscriptionTarget(String messageContent) throws JAXBException, IOException {
		try (Reader reader = new StringReader( messageContent )) {
			JAXBContext jaxbContext = RepositoryJaxbContext.getExtContext();
			Unmarshaller u = jaxbContext.createUnmarshaller();
			JAXBElement<SubscriptionTarget> msgElement = (JAXBElement<SubscriptionTarget>) u.unmarshal( reader );
			
			return msgElement.getValue();
		}
	}
	
	/**
	 * Returns true if the given exception or any of its nested caused-by exceptions
	 * is due to a network connection exception (typically because the JMS connection
	 * is down).
	 * 
	 * @param t  the throwable to analyze
	 * @return boolean
	 */
	private boolean isConnectException(Throwable t) {
		boolean isCE = false;
		
		while (!isCE && (t != null)) {
			if (!(isCE = t.getClass().equals( ConnectException.class ))) {
				t = t.getCause();
			}
		}
		return isCE;
	}
	
	/**
	 * Sends a JMS message to the OTM repository as notification that an indexing job has
	 * been committed.
	 */
	private void sendCommitNotifiation() {
		jmsTemplate.send(new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				TextMessage msg = session.createTextMessage();
				
				msg.setIntProperty( MSGPROP_SELECTOR, SELECTOR_VALUE_COMMITMSG );
				return msg;
			}
		});
	}
	
	/**
	 * Requests a shutdown of this indexing agent.
	 */
	public static void shutdown() {
		shutdownRequested = true;
	}
	
	/**
	 * Main method invoked from the command-line.
	 * 
	 * @param args  the command-line arguments (ignored)
	 */
	public static void main(String[] args) {
		try {
			new IndexingAgent().startListening();
			
		} catch (Throwable t) {
			log.fatal("Indexing agent encountered a fatal error.", t);
			System.exit( IndexProcessManager.FATAL_EXIT_CODE );
		}
	}
	
}

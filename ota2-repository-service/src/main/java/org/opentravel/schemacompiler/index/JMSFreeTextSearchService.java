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
import java.io.IOException;
import java.io.StringWriter;
import java.net.ConnectException;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoListType;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.opentravel.schemacompiler.util.RepositoryJaxbContext;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * Implementation of the <code>FreeTextSearchService</code> that submits indexing jobs
 * via JMS to a remote server.
 * 
 * @author S. Livezey
 */
public class JMSFreeTextSearchService extends FreeTextSearchService {
	
	private static final String UNABLE_TO_SUBMIT = "Unable to submit indexing job for processing - service unavailable.";
	private static final long JMS_LISTENER_RETRY_INTERVAL = 10000;
	
    private static Log log = LogFactory.getLog(JMSFreeTextSearchService.class);
    private static org.opentravel.ns.ota2.repositoryinfo_v01_00.ObjectFactory objectFactory =
    		new org.opentravel.ns.ota2.repositoryinfo_v01_00.ObjectFactory();
    private static org.opentravel.ns.ota2.repositoryinfoext_v01_00.ObjectFactory extObjectFactory =
    		new org.opentravel.ns.ota2.repositoryinfoext_v01_00.ObjectFactory();
    
    private boolean shutdownRequested = false;
    private boolean jmsConnectionAvailable = false;
    private Thread commitListenerThread;
    
    /**
     * Constructor that specifies the folder location of the index and the repository
     * manager used to access the content to be indexed and searched for.
     * 
     * @param indexLocation  the folder location of the index directory
     * @param repositoryManager  the repository that owns all content to be indexed
     * @throws IOException  thrown if a low-level error occurs while initializing the search index
     */
	public JMSFreeTextSearchService(File indexLocation, RepositoryManager repositoryManager) throws IOException {
		super(indexLocation, repositoryManager);
	}
	
	/**
	 * @see org.opentravel.schemacompiler.index.FreeTextSearchService#onStartup(org.apache.lucene.store.Directory)
	 */
	@Override
	protected void onStartup(Directory indexDirectory) throws IOException {
		shutdownRequested = false;
		commitListenerThread = new Thread( new IndexCommitListener() );
		commitListenerThread.start();
	}

	/**
	 * @see org.opentravel.schemacompiler.index.FreeTextSearchService#onShutdown()
	 */
	@Override
	protected void onShutdown() throws IOException {
		if ((commitListenerThread != null) && commitListenerThread.isAlive()) {
			JmsTemplate indexingService = RepositoryComponentFactory.getDefault().getIndexingJmsService();
			long timeout = indexingService.getReceiveTimeout();
			
			try {
				if (timeout <= 0) timeout = 1000; // default to 1 second timeout
				shutdownRequested = true;
				commitListenerThread.join( timeout * 2 ); // max wait twice as long as the JMS receive timeout
				
				// If we are using a caching connection factory, make sure all of the sessions and connections
				// get destroyed upon shutdown.  Otherwise, the JVM could hang upon exit when using some JMS
				// providers (e.g. ActiveMQ).
				ConnectionFactory jmsConnectionFactory = indexingService.getConnectionFactory();
				
				if (jmsConnectionFactory instanceof CachingConnectionFactory) {
					((CachingConnectionFactory) jmsConnectionFactory).destroy();
				}
				
			} catch (InterruptedException e) {
				log.warn("Interrupted while waiting for ");
				Thread.currentThread().interrupt();
				
			} finally {
				shutdownRequested = false;
			}
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.index.FreeTextSearchService#isIndexingServiceAvailable()
	 */
	@Override
	public boolean isIndexingServiceAvailable() {
		return isRunning() && jmsConnectionAvailable;
	}

	/**
	 * @see org.opentravel.schemacompiler.index.FreeTextSearchService#newIndexReader(org.apache.lucene.store.Directory)
	 */
	@Override
	protected DirectoryReader newIndexReader(Directory indexDirectory) throws IOException {
		DirectoryReader indexReader = null;
		try {
			indexReader = DirectoryReader.open(indexDirectory);
			
		} catch (IndexNotFoundException e) {
        	// If the search index does not yet exist, open and close an index writer to
			// initialize the directory
			IndexWriterConfig writerConfig = new IndexWriterConfig(new StandardAnalyzer());
	        writerConfig.setOpenMode( OpenMode.CREATE_OR_APPEND );
	        
	        try (IndexWriter indexWriter = new IndexWriter( indexDirectory, writerConfig )) {
	        	indexWriter.commit();
	        }
			indexReader = DirectoryReader.open(indexDirectory);
		}
		return indexReader;
	}

	/**
	 * @see org.opentravel.schemacompiler.index.FreeTextSearchService#submitIndexingJob(java.util.List, boolean)
	 */
	@Override
	protected void submitIndexingJob(List<RepositoryItem> itemsToIndex, boolean deleteIndex) {
		try {
			if (isIndexingServiceAvailable()) {
				JAXBContext jaxbContext = RepositoryJaxbContext.getContext();
				LibraryInfoListType metadataList = new LibraryInfoListType();
				Marshaller m = jaxbContext.createMarshaller();
				StringWriter writer = new StringWriter();
				
				for (RepositoryItem item : itemsToIndex) {
		   	    	metadataList.getLibraryInfo().add( RepositoryUtils.createItemMetadata( item ) );
				}
				m.marshal( objectFactory.createLibraryInfoList( metadataList ), writer );
				sendIndexingJob( deleteIndex ?
						IndexingConstants.JOB_TYPE_DELETE_INDEX : IndexingConstants.JOB_TYPE_CREATE_INDEX, writer.toString() );
				log.info("Submitted processing request for remote indexing job (" + itemsToIndex.size() + " entries).");
				
			} else {
				log.info(UNABLE_TO_SUBMIT);
			}
			
		} catch (JAXBException e) {
			log.error("Error submitting indexing job.", e);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.index.FreeTextSearchService#deleteSearchIndex()
	 */
	@Override
	protected void deleteSearchIndex() {
		if (isIndexingServiceAvailable()) {
			sendIndexingJob( IndexingConstants.JOB_TYPE_DELETE_ALL, null );
	    	log.info("Submitted index deletion job.");
			
		} else {
			log.info(UNABLE_TO_SUBMIT);
		}
	}
	
	/**
	 * @see org.opentravel.schemacompiler.index.FreeTextSearchService#submitIndexingJob(org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget)
	 */
	@Override
	protected void submitIndexingJob(SubscriptionTarget subscriptionTarget) {
		try {
			if (isIndexingServiceAvailable()) {
				JAXBContext jaxbContext = RepositoryJaxbContext.getExtContext();
				Marshaller m = jaxbContext.createMarshaller();
				StringWriter writer = new StringWriter();
				
				m.marshal( extObjectFactory.createSubscriptionTarget( subscriptionTarget ), writer );
				sendIndexingJob( IndexingConstants.JOB_TYPE_SUBSCRIPTION, writer.toString() );
				log.info("Submitted processing request for subscription indexing job.");
				
			} else {
				log.info(UNABLE_TO_SUBMIT);
			}
			
		} catch (JAXBException e) {
			log.error("Error submitting indexing job.", e);
		}
	}

	/**
	 * Sends an indexing job to the remote indexing process.
	 * 
	 * @param jobType  the type of indexing job to send
	 * @param messageContent  the message content that specifies the item to be indexed (may be null)
	 */
	private void sendIndexingJob(final String jobType, final String messageContent) {
		JmsTemplate indexingService = RepositoryComponentFactory.getDefault().getIndexingJmsService();
		
		indexingService.send(new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				TextMessage msg = session.createTextMessage();
				
				msg.setStringProperty( IndexingConstants.MSGPROP_JOB_TYPE, jobType );
				msg.setIntProperty( IndexingConstants.MSGPROP_SELECTOR, IndexingConstants.SELECTOR_VALUE_JOBMSG );
				msg.setText( messageContent );
				return msg;
			}
		});
	}
	
	/**
	 * Background thread listener that will refresh the service's index reader when the
	 * remote indexing service sends a notification that the index directory has been
	 * updated.
	 */
	private class IndexCommitListener implements Runnable {
		
		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (!shutdownRequested) {
				JmsTemplate indexingService = RepositoryComponentFactory.getDefault().getIndexingJmsService();
				
				waitForJmsProvider( indexingService );
				log.info("Indexing commit listener started.");
				
				while (!shutdownRequested && jmsConnectionAvailable) {
					try {
						Message msg = indexingService.receiveSelected( IndexingConstants.SELECTOR_COMMITMSG );
						
						if (msg != null) {
							log.info("Commit notification received from indexing agent.");
							refreshIndexReader();
						}
						
					} catch (Exception e) {
						if (isConnectException( e )) {
							log.warn("Indexing agent connection appears to be down - waiting to reconnect...");
							jmsConnectionAvailable = false;
							
						} else {
							log.error("Error receiving indexing job.", e);
						}
					}
				}
			}
			log.info("Indexing commit listener shut down.");
			shutdownRequested = false;
		}

		/**
		 * Make sure the JMS provider is available; if not, continue to attempt a connection periodically
		 * before we start listening for messages.
		 * 
		 * @param indexingService  the indexing service that will attempt to create the JMS connection
		 */
		private void waitForJmsProvider(JmsTemplate indexingService) {
			Connection jmsConnection = null;
			
			while (!shutdownRequested && !jmsConnectionAvailable) {
				try {
					jmsConnection = indexingService.getConnectionFactory().createConnection();
					jmsConnection.start();
					jmsConnectionAvailable = true;
					
				} catch (Exception e) {
					try {
						log.info("Unable to establish connection to indexing agent - waiting to reconnect...");
						Thread.sleep( JMS_LISTENER_RETRY_INTERVAL );
						
					} catch (Exception e2) {
						// Ignore and continue
					}
				}
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
				isCE = t.getClass().equals( ConnectException.class );
				if (!isCE) t = t.getCause();
			}
			return isCE;
		}
		
	}
	
}

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
import java.io.Reader;
import java.io.StringReader;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
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
import org.opentravel.schemacompiler.index.builder.IndexBuilder;
import org.opentravel.schemacompiler.index.builder.IndexBuilderFactory;
import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.opentravel.schemacompiler.util.RepositoryJaxbContext;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

/**
 * Main entry point for the agent that handles indexing requests in response to messages
 * received from the remote repository server.
 * 
 * @author S. Livezey
 */
public class IndexingAgent implements IndexingConstants {
	
    private static Log log = LogFactory.getLog(IndexingAgent.class);
    
    private static boolean shutdownRequested = false;
    
    private File indexLocation;
    private RepositoryManager repositoryManager;
    private Directory indexDirectory;
    private IndexWriterConfig writerConfig;
    private IndexWriter indexWriter;
    
    /**
     * Default constructor.
     * 
     * @throws IOException  thrown if the search index writer cannot be initialized
     */
    public IndexingAgent() throws IOException {
    	indexLocation = RepositoryComponentFactory.getDefault().getSearchIndexLocation();
        if (!indexLocation.exists()) indexLocation.mkdirs();
        
        // Check to make sure the index was properly closed, and release any lock that might exist
        this.indexDirectory = FSDirectory.open(indexLocation.toPath());

        // Configure the indexing and search components
        this.writerConfig = new IndexWriterConfig(new StandardAnalyzer());
        this.writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
        this.indexWriter = new IndexWriter(indexDirectory, writerConfig);
        
    	repositoryManager = RepositoryComponentFactory.getDefault().getRepositoryManager();
    }
    
	/**
	 * Starts listening for JMS messages and performs the appropriate indexing action when
	 * one is received.
	 * 
	 * @throws JMSException  thrown if a fatal error occurs because the JMS connection is not available
	 */
	public void startListening() throws JMSException {
		JmsTemplate indexingService = RepositoryComponentFactory.getDefault().getIndexingJmsService();
		Connection jmsConnection = null;
		
		try {
			jmsConnection = indexingService.getConnectionFactory().createConnection();
			
		} finally {
			if (jmsConnection != null) {
				jmsConnection.close();
			}
		}
		
		log.info("Indexing agent started for location: " + indexLocation.getAbsolutePath());
		
		while (!shutdownRequested) {
			try {
				Message msg = indexingService.receive();
				
				if (msg instanceof TextMessage) {
					TextMessage message = (TextMessage) msg;
					String messageType = message.getStringProperty( MSGPROP_JOB_TYPE );
					String messageContent = message.getText();
					
					if (messageType != null) {
						if (messageType.equals( JOB_TYPE_CREATE_INDEX )) {
							submitIndexingJob( unmarshallRepositoryItems( messageContent ), false );
							
						} else if (messageType.equals( JOB_TYPE_DELETE_INDEX )) {
							submitIndexingJob( unmarshallRepositoryItems( messageContent ), true );
							
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
     * Submits the given index builder for processing.
     * 
     * @param itemsToIndex  the list of repository items to be indexed
     * @param deleteIndex  flag indicating whether the index is to be created or deleted
     * @throws IOException  thrown if an error occurs while processing the indexing job
     */
    protected void submitIndexingJob(List<RepositoryItem> itemsToIndex, boolean deleteIndex) throws IOException {
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
    }
    
	/**
	 * Deletes the entire persistent search index.
	 * 
	 * @throws IOException  thrown if the search index cannot be deleted
	 */
	private void processDeleteAll() throws IOException {
    	log.info("Deleting search index.");
		indexWriter.deleteAll();
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

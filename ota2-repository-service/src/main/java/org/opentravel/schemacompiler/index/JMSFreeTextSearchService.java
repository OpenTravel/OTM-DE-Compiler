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
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoListType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.ObjectFactory;
import org.opentravel.schemacompiler.providers.JAXBContextResolver;
import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * Implementation of the <code>FreeTextSearchService</code> that submits indexing jobs
 * via JMS to a remote server.
 * 
 * @author S. Livezey
 */
public class JMSFreeTextSearchService extends FreeTextSearchService implements IndexingConstants {
	
    private static Log log = LogFactory.getLog(JMSFreeTextSearchService.class);
    
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
	 * @see org.opentravel.schemacompiler.index.FreeTextSearchService#submitIndexingJob(java.util.List, boolean)
	 */
	@Override
	protected void submitIndexingJob(List<RepositoryItem> itemsToIndex, boolean deleteIndex) {
		try {
			JAXBContext jaxbContext = new JAXBContextResolver().getContext( null );
			LibraryInfoListType metadataList = new LibraryInfoListType();
			Marshaller m = jaxbContext.createMarshaller();
			StringWriter writer = new StringWriter();
			
			for (RepositoryItem item : itemsToIndex) {
	   	    	if (deleteIndex) {
	   	        	log.info("Submitted delete index job for library: " + item.getFilename());
	   	    	} else {
	   	        	log.info("Submitted indexing job for library: " + item.getFilename());
	   	    	}
	   	    	metadataList.getLibraryInfo().add( RepositoryUtils.createItemMetadata( item ) );
			}
			m.marshal( new ObjectFactory().createLibraryInfoList( metadataList ), writer );
			sendIndexingJob( deleteIndex ? JOB_TYPE_DELETE_INDEX : JOB_TYPE_CREATE_INDEX, writer.toString() );
			
		} catch (JAXBException e) {
			log.error("Error submitting indexing job.", e);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.index.FreeTextSearchService#deleteSearchIndex()
	 */
	@Override
	protected void deleteSearchIndex() {
    	log.info("Submitted index deletion job.");
		sendIndexingJob( JOB_TYPE_DELETE_ALL, null );
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
				
				msg.setStringProperty( MSGPROP_JOB_TYPE, jobType );
				msg.setText( messageContent );
				return msg;
			}
		});
	}
	
}

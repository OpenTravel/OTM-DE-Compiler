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

package org.opentravel.schemacompiler.notification;

import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Deque;

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
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.ObjectFactory;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.opentravel.schemacompiler.util.RepositoryJaxbContext;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * Notification service that broadcasts repository events to a JMS topic.
 */
public class JMSNotificationService implements NotificationService, NotificationConstants {
	
    private static Log log = LogFactory.getLog(JMSNotificationService.class);
    private static ObjectFactory objectFactory = new ObjectFactory();
    
    private JmsTemplate jmsService;
    private Deque<NotificationJob> jobQueue = new ArrayDeque<>();
    private boolean running = false;
    private boolean shutdownRequested = false;
    private Thread jobThread;
    
    /**
     * Constructor that specifies the JMS template to use when publishing events.
     * 
     * @param jmsService  the JMS service
     */
    public JMSNotificationService(JmsTemplate jmsService) {
    	this.jmsService = jmsService;
    }
    
	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#startup()
	 */
	@Override
	public void startup() {
		Runnable r = () -> {
			try {
				log.info("Notification service started.");
				running = true;
				
				while (!shutdownRequested) {
					synchronized (jobQueue) {
						jobQueue.wait( 10000L );
						
						if (!jobQueue.isEmpty()) {
							sendNotification( jobQueue.removeLast() );
						}
					}
				}
				log.info("Notification service shut down.");
				
			} catch (Exception e) {
				log.error("Unexpected error caught in notification service - shutting down.", e);
				
			} finally {
				running = false;
				shutdownRequested = false;
			}
		};
		
		jobThread = new Thread( r, getClass().getSimpleName() );
		jobThread.start();
	}

	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#shutdown()
	 */
	@Override
	public void shutdown() {
		// Request shutdown and wait for the job thread to finish
		synchronized (jobQueue) {
			shutdownRequested = true;
			jobQueue.notifyAll();
		}
		
		try {
			jobThread.join();
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		// If we are using a caching connection factory, make sure all of the sessions and connections
		// get destroyed upon shutdown.  Otherwise, the JVM could hang upon exit when using some JMS
		// providers (e.g. ActiveMQ).
		ConnectionFactory jmsConnectionFactory = jmsService.getConnectionFactory();
		
		if (jmsConnectionFactory instanceof CachingConnectionFactory) {
			((CachingConnectionFactory) jmsConnectionFactory).destroy();
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#itemPublished(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void itemPublished(RepositoryItem item) {
		queueNotification( PUBLISH_ACTION_ID, item );
	}

	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#itemModified(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void itemModified(RepositoryItem item) {
		queueNotification( MODIFIED_ACTION_ID, item );
	}

	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#itemLocked(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void itemLocked(RepositoryItem item) {
		queueNotification( LOCKED_ACTION_ID, item );
	}

	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#itemUnlocked(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void itemUnlocked(RepositoryItem item) {
		queueNotification( UNLOCKED_ACTION_ID, item );
	}

	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#itemStatusChanged(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void itemStatusChanged(RepositoryItem item) {
		queueNotification( STATUS_CHANGED_ACTION_ID, item );
	}

	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#itemDeleted(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void itemDeleted(RepositoryItem item) {
		queueNotification( DELETED_ACTION_ID, item );
	}
	
	/**
	 * Broadcasts a JMS event using the information provided.
	 * 
	 * @param actionId  indicates the type of action that was performed on the repository item
	 * @param item  the repository item that was affected by the change
	 */
	private void queueNotification(final String actionId, RepositoryItem item) {
		synchronized (jobQueue) {
			if (running) {
				jobQueue.addFirst( new NotificationJob( actionId, item ) );
				jobQueue.notifyAll();
				
			} else {
				log.warn("Notification service not running - "
						+ "ignoring notification event for " + item.getFilename() );
			}
		}
	}
	
	/**
	 * Broadcasts a JMS event using the information provided.
	 * 
	 * @param job  the notification job to publish
	 */
	private void sendNotification(NotificationJob job) {
		try {
			JAXBContext jaxbContext = RepositoryJaxbContext.getContext();
			LibraryInfoType libraryMetadata = RepositoryUtils.createItemMetadata( job.getAffectedItem() );
			Marshaller m = jaxbContext.createMarshaller();
			final StringWriter writer = new StringWriter();
			
			m.marshal( objectFactory.createLibraryInfo( libraryMetadata ), writer );
			
			jmsService.send(new MessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					TextMessage msg = session.createTextMessage();
					
					msg.setStringProperty( MSGPROP_ACTION, job.getActionId() );
					msg.setText( writer.toString() );
					return msg;
				}
			});
	    	
		} catch (JAXBException e) {
			log.error("Error submitting indexing job.", e);
		}
	}
	
	/**
	 * Encapsulates all information required to process a notification job.
	 */
	private static class NotificationJob {
		
		private String actionId;
		private RepositoryItem affectedItem;
		
		/**
		 * Full constructor.
		 * 
		 * @param actionId  indicates the type of action that was performed on the repository item
		 * @param affectedItem  the repository item that was affected by the change
		 */
		public NotificationJob(String actionId, RepositoryItem affectedItem) {
			this.actionId = actionId;
			this.affectedItem = affectedItem;
		}

		/**
		 * Returns the value of the 'actionId' field.
		 *
		 * @return String
		 */
		public String getActionId() {
			return actionId;
		}

		/**
		 * Returns the value of the 'affectedItem' field.
		 *
		 * @return RepositoryItem
		 */
		public RepositoryItem getAffectedItem() {
			return affectedItem;
		}
		
	}
}

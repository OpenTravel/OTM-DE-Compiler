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

package org.opentravel.schemacompiler.subscription;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.Subscription;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionEventType;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionList;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.schemacompiler.index.FreeTextSearchServiceFactory;
import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.RepositoryNamespaceUtils;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * Provides subscription and email notification services for an OTM repository.
 * 
 * @author S. Livezey
 */
public class SubscriptionManager {
	
	public static final String REMARK_UNLOCK_REVERT = "__%%_UNLOCK__REVERT_%%__";
	
	private static final long DEFAULT_NOTIFICATION_DELAY = 5000; // 5 sec
    private static final String TEMPLATE_LOCATION = "/org/opentravel/notification/templates/";
    private static final String SUBJECT_FORMAT = "{1} - {0}";
    private static final int MAX_RETRIES = 3;
	
    private static Log log = LogFactory.getLog( SubscriptionManager.class );
    private static VelocityEngine velocityEngine;
	public static boolean debugMode = false;
    
	private Map<String,SubscriptionResource> namespaceCache = new HashMap<>();
	private Map<String,SubscriptionResource> allVersionsCache = new HashMap<>();
	private Map<String,SubscriptionResource> singleVersionCache = new HashMap<>();
	
	private SubscriptionFileUtils fileUtils;
	private RepositoryManager manager;
	private String baseRepositoryUrl;
	private SMTPConfig smtpConfig;
	
	private Thread notificationThread;
	private BlockingQueue<NotificationJob> notificationQueue = new DelayQueue<>();
	private RepositoryNotificationListener repositoryListener;
	private long notificationDelay = DEFAULT_NOTIFICATION_DELAY;
	private boolean shutdownRequested = false;
	
	/**
	 * Constructor that specifies the repository manager for which this instance
	 * handles subscriptions and notifications.
	 * 
	 * @param manager  the repository manager instance
	 */
	public SubscriptionManager(RepositoryManager manager) {
		this.fileUtils = new SubscriptionFileUtils( manager );
		this.manager = manager;
	}
	
    /**
     * Creates a new single-version <code>SubscriptionTarget</code> using information
     * from the <code>RepositoryItem</code> provided.
     * 
     * @param item  the repository item from which to return a subscription target
     * @return SubscriptionTarget
     */
    public static SubscriptionTarget getSubscriptionTarget(RepositoryItem item) {
    	return getSubscriptionTarget( item.getBaseNamespace(), item.getLibraryName(), item.getVersion() );
    }
    
    /**
     * Creates a new single-version <code>SubscriptionTarget</code> using information
     * from the <code>RepositoryItem</code> provided.
     * 
	 * @param baseNamespace  the base namespace of the subscription target
	 * @param libraryName  the library name of the subscription target (may be null)
	 * @param version  the library version of the subscription target (may be null)
     * @return SubscriptionTarget
     */
    public static SubscriptionTarget getSubscriptionTarget(String baseNamespace,
    		String libraryName, String version) {
    	SubscriptionTarget sTarget = new SubscriptionTarget();
    	
    	sTarget.setBaseNamespace( baseNamespace );
    	sTarget.setLibraryName( libraryName );
    	sTarget.setVersion( version );
    	return sTarget;
    }
    
	/**
	 * Retrieves the list of events to which the user is subscribed in the given namespace.
	 * 
	 * @param baseNS  the base namespace for which to retrieve the user's subscriptions
	 * @param userId  the ID of the user whose subscriptions are to be retrieved
	 * @return List<SubscriptionEventType>
	 */
	public List<SubscriptionEventType> getNamespaceSubscriptions(String baseNS, String userId)
			throws RepositoryException {
		List<SubscriptionEventType> eventTypes = new ArrayList<>();
		
		if ((userId != null) && !userId.equals( UserPrincipal.ANONYMOUS_USER_ID )) {
			SubscriptionList subscriptions = getNamespaceSubscriptions( baseNS );
			
			for (Subscription subscription : subscriptions.getSubscription()) {
				if (subscription.getUser().contains( userId )) {
					eventTypes.add( subscription.getEventType() );
				}
			}
		}
		return eventTypes;
	}
	
	/**
	 * Retrieves the list of events to which the user is subscribed for all versions of the
	 * specified repository item.
	 * 
	 * @param item  the repository item for which to retrieve the user's all-version subscriptions
	 * @param userId  the ID of the user whose subscriptions are to be retrieved
	 * @return List<SubscriptionEventType>
	 */
	public List<SubscriptionEventType> getAllVersionsSubscriptions(SubscriptionTarget subscriptionTarget,
			String userId) throws RepositoryException {
		List<SubscriptionEventType> eventTypes = new ArrayList<>();
		
		if ((userId != null) && !userId.equals( UserPrincipal.ANONYMOUS_USER_ID )) {
			SubscriptionList subscriptions = getAllVersionsSubscriptions(
					subscriptionTarget.getBaseNamespace(), subscriptionTarget.getLibraryName() );
			
			for (Subscription subscription : subscriptions.getSubscription()) {
				if (subscription.getUser().contains( userId )) {
					eventTypes.add( subscription.getEventType() );
				}
			}
		}
		return eventTypes;
	}
	
	/**
	 * Retrieves the list of events to which the user is subscribed for the specified repository item.
	 * 
	 * @param item  the repository item for which to retrieve the user's single-version subscriptions
	 * @param userId  the ID of the user whose subscriptions are to be retrieved
	 * @return List<SubscriptionEventType>
	 */
	public List<SubscriptionEventType> getSingleVersionSubscriptions(SubscriptionTarget subscriptionTarget,
			String userId) throws RepositoryException {
		List<SubscriptionEventType> eventTypes = new ArrayList<>();
		
		if ((userId != null) && !userId.equals( UserPrincipal.ANONYMOUS_USER_ID )) {
			SubscriptionList subscriptions = getSingleVersionSubscriptions(
					subscriptionTarget.getBaseNamespace(), subscriptionTarget.getLibraryName(),
					subscriptionTarget.getVersion() );
			
			for (Subscription subscription : subscriptions.getSubscription()) {
				if (subscription.getUser().contains( userId )) {
					eventTypes.add( subscription.getEventType() );
				}
			}
		}
		return eventTypes;
	}
	
	/**
	 * Returns the subscription list by performing a lookup using the information provided.
	 * 
	 * @param subscriptionTarget  the target of the subscription list to return
	 * @return SubscriptionList
     * @throws RepositoryException  thrown if an error occurs while retrieving the subscription list
	 */
	public SubscriptionList getSubscriptionList(SubscriptionTarget subscriptionTarget) throws RepositoryException {
		String baseNamespace = subscriptionTarget.getBaseNamespace();
		String libraryName = subscriptionTarget.getLibraryName();
		String version = subscriptionTarget.getVersion();
		SubscriptionList subscriptions;
		
		if (baseNamespace != null) {
			if (libraryName != null) {
				if (version != null) {
					subscriptions = getSingleVersionSubscriptions( baseNamespace, libraryName, version );
					
				} else {
					subscriptions = getAllVersionsSubscriptions( baseNamespace, libraryName );
				}
			} else {
				subscriptions = getNamespaceSubscriptions( baseNamespace );
			}
			
		} else {
			throw new RepositoryException("Insufficient information provided for subscription list lookup.");
		}
		return subscriptions;
	}
	
	/**
	 * Updates the subscription events for the given user within the specified base namespace.
	 * 
	 * @param baseNS  the base namespace for which to update the user's subscriptions
	 * @param userId  the ID of the user whose subscriptions are to be updated
	 * @param eventTypes  the types of events for which the user wishes to be notified
     * @throws RepositoryException  thrown if an error occurs while updating the subscription list
	 */
	public void updateNamespaceSubscriptions(String baseNS, String userId, List<SubscriptionEventType> eventTypes)
			throws RepositoryException {
		updateSubscriptions( getNamespaceSubscriptions( baseNS ), userId, eventTypes );
	}
	
	/**
	 * Updates the subscription events for all versions of the specified repository item.
	 * 
	 * @param item  the repository item for which to update the user's all-version subscriptions
	 * @param userId  the ID of the user whose subscriptions are to be updated
	 * @param eventTypes  the types of events for which the user wishes to be notified
     * @throws RepositoryException  thrown if an error occurs while updating the subscription list
	 */
	public void updateAllVersionsSubscriptions(SubscriptionTarget subscriptionTarget, String userId,
			List<SubscriptionEventType> eventTypes) throws RepositoryException {
		updateSubscriptions( getAllVersionsSubscriptions(
				subscriptionTarget.getBaseNamespace(), subscriptionTarget.getLibraryName() ),
				userId, eventTypes );
	}
	
	/**
	 * Updates the subscription events for the specified repository item.
	 * 
	 * @param item  the repository item for which to update the user's single-version subscriptions
	 * @param userId  the ID of the user whose subscriptions are to be updated
	 * @param eventTypes  the types of events for which the user wishes to be notified
     * @throws RepositoryException  thrown if an error occurs while updating the subscription list
	 */
	public void updateSingleVersionSubscriptions(SubscriptionTarget subscriptionTarget, String userId,
			List<SubscriptionEventType> eventTypes) throws RepositoryException {
		updateSubscriptions( getSingleVersionSubscriptions(
				subscriptionTarget.getBaseNamespace(), subscriptionTarget.getLibraryName(),
				subscriptionTarget.getVersion() ), userId, eventTypes );
	}
	
	/**
	 * Updates and saves the given subscription list using the information provided.
	 * 
	 * @param subscriptions  the subscription list to be updated
	 * @param userId  the ID of the user whose subscriptions are to be updated
	 * @param eventTypes  the types of events for which the user wishes to be notified
     * @throws RepositoryException  thrown if an error occurs while updating the subscription list
	 */
	private void updateSubscriptions(SubscriptionList subscriptions, String userId, List<SubscriptionEventType> eventTypes)
			throws RepositoryException {
		if (eventTypes == null) {
			eventTypes = new ArrayList<>();
		}
		
		for (Subscription subscription : subscriptions.getSubscription()) {
			if (eventTypes.contains( subscription.getEventType() )) {
				if (!subscription.getUser().contains( userId )) {
					subscription.getUser().add( userId );
				}
			} else {
				subscription.getUser().remove( userId );
			}
		}
		try {
			fileUtils.saveSubscriptionList( subscriptions );
			FreeTextSearchServiceFactory.getInstance().indexSubscriptionTarget( subscriptions.getSubscriptionTarget() );
			
		} catch (IOException e) {
			throw new RepositoryException("Error saving updates to subscription list.", e);
		}
	}
	
    /**
     * Constructs a new subscription list for all events that occur within a namespace.
     * 
     * @param baseNS  the URI of the base namespace for which to construct a subscription list
     * @return SubscriptionList
     * @throws RepositoryException  thrown if an error occurs while retrieving the subscription list
     */
    private SubscriptionList getNamespaceSubscriptions(String baseNS) throws RepositoryException {
    	SubscriptionResource sr = namespaceCache.get( baseNS );
    	
    	if (sr == null) {
    		try {
				sr = new SubscriptionResource( fileUtils, baseNS, null, null );
				namespaceCache.put( baseNS, sr );
				
			} catch (IOException e) {
				throw new RepositoryException("Error loading subscription list content.", e);
			}
    	}
    	return sr.getResource();
    }
    
    /**
     * Constructs a new subscription list for events that occur to any version of an OTM library.
     * 
	 * @param baseNamespace  the base namespace of the subscription list target
	 * @param libraryName  the library name of the subscription list target (may be null)
     * @return SubscriptionList
     * @throws RepositoryException  thrown if an error occurs while retrieving the subscription list
     */
    private SubscriptionList getAllVersionsSubscriptions(String baseNamespace, String libraryName) throws RepositoryException {
    	String cacheKey = baseNamespace + "~" + libraryName;
    	SubscriptionResource sr = allVersionsCache.get( cacheKey );
    	
    	if (sr == null) {
    		try {
				sr = new SubscriptionResource( fileUtils, baseNamespace, libraryName, null );
				allVersionsCache.put( cacheKey, sr );
				
			} catch (IOException e) {
				throw new RepositoryException("Error loading subscription list content.", e);
			}
    	}
    	return sr.getResource();
    }
    
    /**
     * Constructs a new subscription list for events that occur to a specific version of an OTM library.
     * 
	 * @param baseNamespace  the base namespace of the subscription list target
	 * @param libraryName  the library name of the subscription list target (may be null)
	 * @param version  the library version of the subscription list target (may be null)
     * @return SubscriptionList
     * @throws RepositoryException  thrown if an error occurs while retrieving the subscription list
     */
    private SubscriptionList getSingleVersionSubscriptions(String baseNamespace, String libraryName, String version) throws RepositoryException {
    	String cacheKey = baseNamespace + "~" + libraryName + "~" + version;
    	SubscriptionResource sr = singleVersionCache.get( cacheKey );
    	
    	if (sr == null) {
    		try {
				sr = new SubscriptionResource( fileUtils, baseNamespace, libraryName, version );
				singleVersionCache.put( cacheKey, sr );
				
			} catch (IOException e) {
				throw new RepositoryException("Error loading subscription list content.", e);
			}
    	}
    	return sr.getResource();
    }
    
	/**
	 * Notifies all subscribed users of an action that was performed on the specified
	 * namespace.  The only valid actions for this method are <code>NS_CREATED</code>
	 * and <code>NS_DELETED</code>.
	 * 
	 * @param item  the item upon which the action was performed
	 * @param action  the action that was performed
	 */
	public void notifySubscribedUsers(String affectedNamespace, RepositoryActionType action) {
		try {
			if ((action != RepositoryActionType.NS_CREATED) && (action != RepositoryActionType.NS_DELETED)) {
				throw new IllegalArgumentException("Only namespace-related actions are allowed for this method.");
			}
			String userId = manager.getFileManager().getCurrentUserId();
			
			if (debugMode) {
				processNotificationJob( new NotificationJob( action, userId, affectedNamespace, null ) );
				
			} else {
				if ((notificationThread != null) && notificationThread.isAlive() && !shutdownRequested) {
					notificationQueue.put( new NotificationJob( action, userId, affectedNamespace, null ) );
					
				} else {
					log.warn("Email notifications not sent because the listener is not running.");
				}
			}
			
		} catch (InterruptedException e) {
			// Ignore - should never happen since the DelayQueue does not have a constrained size limit
		}
	}
	
	/**
	 * Notifies all subscribed users of an action that was performed on the given repository
	 * item.  All actions are considered valid for this method <i>except</i> <code>NS_CREATED</code>
	 * and <code>NS_DELETED</code>.
	 * 
	 * @param item  the item upon which the action was performed
	 * @param action  the action that was performed
	 * @param remarks  free-text remarks that provide additional information about the action
	 */
	public void notifySubscribedUsers(RepositoryItem item, RepositoryActionType action, String remarks) {
		try {
			if ((action == RepositoryActionType.NS_CREATED) || (action == RepositoryActionType.NS_CREATED)) {
				throw new IllegalArgumentException("Namespace-related actions are not allowed for this method.");
			}
			String userId = manager.getFileManager().getCurrentUserId();
			
			if (debugMode) {
				processNotificationJob( new NotificationJob( action, userId, item, remarks ) );
				
			} else {
				if ((notificationThread != null) && notificationThread.isAlive() && !shutdownRequested) {
					notificationQueue.put( new NotificationJob( action, userId, item, remarks ) );
					
				} else {
					log.warn("Email notifications not sent because the listener is not running.");
				}
			}
			
		} catch (InterruptedException e) {
			// Ignore - should never happen since the DelayQueue does not have a constrained size limit
		}
	}
	
	/**
	 * Starts the background thread that listens for notification events.
	 */
	public synchronized void startNotificationListener() {
		if ((notificationThread != null) && notificationThread.isAlive()) {
			throw new IllegalStateException("The notification listener is already running.");
		}
		
		if (smtpConfig != null) {
			notificationThread = new Thread( new Runnable() {
				public void run() {
					listenForNotificationEvents();
				}
			}, "OTM_NotificationListener" );
			
			shutdownRequested = false;
			notificationThread.start();
			repositoryListener = new RepositoryNotificationListener( this, manager );
			manager.addListener( repositoryListener );
			
		} else {
			log.warn("SMTP configuration not initialized - notification listener not started.");
		}
	}
	
	/**
	 * Shuts down the background thread that listens for notification events.
	 */
	public synchronized void shutdownNotificationListener() {
		if ((notificationThread != null) && notificationThread.isAlive()) {
			if (smtpConfig != null) {
				manager.removeListener( repositoryListener );
				repositoryListener = null;
				shutdownRequested = true;
				notificationThread.interrupt();
				
				try {
					notificationThread.join( 1000 ); // wait a full second before giving up
					
				} catch (InterruptedException e) {
					// Ignore and exit
				} finally {
					notificationThread = null;
				}
				
			} else {
				log.warn("SMTP configuration not initialized - skipping notification listener shutdown.");
			}
			
		} else {
			throw new IllegalStateException("The notification listener is not currently running.");
		}
	}
	
    /**
	 * Returns the delay period in milliseconds between when a repository event occurs and
	 * the email notification job is processed.
	 *
	 * @return long
	 */
	public long getNotificationDelay() {
		return notificationDelay;
	}

	/**
	 * Assigns the delay period in milliseconds between when a repository event occurs and
	 * the email notification job is processed.
	 *
	 * @param notificationDelay  the delay period (in milliseconds) to assign
	 */
	public void setNotificationDelay(long notificationDelay) {
		this.notificationDelay = notificationDelay;
	}
	
	/**
	 * Returns the base URL that should be used when creating links to the OTM
	 * repository in email notification messages.
	 *
	 * @return String
	 */
	public String getBaseRepositoryUrl() {
		return baseRepositoryUrl;
	}

	/**
	 * Assigns the base URL that should be used when creating links to the OTM
	 * repository in email notification messages.
	 *
	 * @param baseRepositoryUrl  the URL location to assign
	 */
	public void setBaseRepositoryUrl(String baseRepositoryUrl) {
		this.baseRepositoryUrl = baseRepositoryUrl;
	}

	/**
	 * Returns the SMTP configuration settings.
	 *
	 * @return SMTPConfig
	 */
	public SMTPConfig getSmtpConfig() {
		return smtpConfig;
	}

	/**
	 * Assigns the SMTP configuration settings.
	 *
	 * @param smtpConfig  the configuration settings to assign
	 */
	public void setSmtpConfig(SMTPConfig smtpConfig) {
		this.smtpConfig = smtpConfig;
	}

	/**
	 * Executes the loop that listens for notification events and periodically checks to
	 * see if a listener shutdown has been requested.
	 */
	private void listenForNotificationEvents() {
		while (!shutdownRequested) {
			try {
				NotificationJob job = notificationQueue.poll( 1000, TimeUnit.MILLISECONDS );
				
				if (job != null) {
					processNotificationJob( job );
				}
				
			} catch (InterruptedException e) {
				// Ignore and continue looping until shutdown requested
			}
		}
	}
	
	/**
	 * Processes the given <code></code> by sending an email message to all user accounts which
	 * are configured with a valid email address.
	 * 
	 * @param job  the notification job to process
	 */
	private void processNotificationJob(NotificationJob job) {
		try {
			List<InternetAddress> recipients;
			List<String> userList;
			
			switch (job.getAction()) {
				case NS_CREATED:
				case NS_DELETED:
					userList = getNotificationList( job.getAffectedNamespace(), job.getAction() );
					break;
				default:
					userList = getNotificationList( job.getItem(), job.getAction() );
					break;
			}
			recipients = getEmailAddresses( userList );
			log.info("Processing notification job: " + job.getAction() +
					" (" + recipients.size() + " recipients)");
			
			if (!recipients.isEmpty()) {
				Session mailSession = Session.getInstance( smtpConfig.getSmtpProps() );
				String smtpUser = smtpConfig.getSmtpUser();
				String smtpPassword = smtpConfig.getSmtpPassword();
				InternetAddress fromAddress = new InternetAddress(
						smtpConfig.getSenderAddress(), smtpConfig.getSenderName() );
				Message message = new MimeMessage( mailSession );
				boolean successInd = false;
				int retryCount = 1;
				
				for (InternetAddress recipient : recipients) {
					message.addRecipient( RecipientType.TO, recipient );
				}
				for (String ccAddress : smtpConfig.getCcRecipients()) {
					message.addRecipient( RecipientType.CC, new InternetAddress( ccAddress ) );
				}
				if (smtpConfig.getReplyToAddress() != null) {
					message.setReplyTo( new Address[] {
							new InternetAddress( smtpConfig.getReplyToAddress(), smtpConfig.getReplyToName() ) } );
				}
				message.setFrom( fromAddress );
				message.setSubject( buildMessageSubject( job ) );
				message.setContent( buildMessageBody( job ), "text/html" );
				
				while (!successInd && (retryCount <= MAX_RETRIES)) {
					try {
						
						if ((smtpUser != null) && (smtpPassword != null)) {
							Transport.send( message, smtpUser, smtpPassword );
							
						} else {
							Transport.send( message );
						}
						successInd = true;
						
					} catch (Throwable e) {
						log.error( "Error sending email notification (attempt " + retryCount + ") - " + e.getMessage(), e );
						retryCount++;
					}
				}
			}
			
		} catch (Throwable t) {
			log.error("Error processing notification job.", t);
		}
	}
	
	/**
	 * Returns the list of all users who should be notified of the specified action that was performed
	 * on the given repository item.
	 * 
	 * @param item  the repository item that was the target of the action
	 * @param action  the action that was performed
	 * @return List<String>
	 * @throws RepositoryException  thrown if an error occurs while retrieving the list of users to be notified
	 */
	private List<String> getNotificationList(RepositoryItem item, RepositoryActionType action) throws RepositoryException {
		SubscriptionList singleVersionSubscriptions = getSingleVersionSubscriptions(
				item.getBaseNamespace(), item.getLibraryName(), item.getVersion() );
		SubscriptionList allVersionSubscriptions = getAllVersionsSubscriptions(
				item.getBaseNamespace(), item.getLibraryName() );
		SubscriptionEventType eventType = action.getEventType();
		List<String> userList = new ArrayList<>();
		
		addUsers( userList, singleVersionSubscriptions, eventType );
		addUsers( userList, allVersionSubscriptions, eventType );
		
		for (String userId : getNotificationList( item.getBaseNamespace(), action )) {
			if (!userList.contains( userId )) {
				userList.add( userId );
			}
		}
		return userList;
	}
	
	/**
	 * Returns the list of all users who should be notified of the specified action that was performed
	 * in the specified namespace.
	 * 
	 * @param affectedNamespace  the namespace where the action was performed
	 * @param action  the action that was performed
	 * @return List<String>
	 * @throws RepositoryException  thrown if an error occurs while retrieving the list of users to be notified
	 */
	private List<String> getNotificationList(String affectedNamespace, RepositoryActionType action) throws RepositoryException {
		String baseNS = RepositoryNamespaceUtils.normalizeUri( affectedNamespace );
		SubscriptionEventType eventType = action.getEventType();
		List<String> userList = new ArrayList<>();
		
		while (baseNS != null) {
			addUsers( userList, getNamespaceSubscriptions( baseNS ), eventType );
			
			try {
				baseNS = RepositoryNamespaceUtils.getParentNamespace( baseNS, manager );
				
			} catch (IllegalArgumentException e) {
				// Ignore error and stop searching
				baseNS = null;
			}
		}
		return userList;
	}
	
	/**
	 * Adds users subscribed to the specified event type from the given subscription list.
	 * 
	 * @param userList  the list of users being constructed
	 * @param subscriptionList  the subscription list to process
	 * @param eventType  the event type used to identify subscribed users
	 */
	private void addUsers(List<String> userList, SubscriptionList subscriptionList, SubscriptionEventType eventType) {
		Subscription subscription = null;
		
		for (Subscription s : subscriptionList.getSubscription()) {
			if (s.getEventType() == eventType) {
				subscription = s;
				break;
			}
		}
		
		if (subscription != null) {
			for (String userId : subscription.getUser()) {
				if (!userList.contains( userId )) {
					userList.add( userId );
				}
			}
		}
	}
	
	/**
	 * Returns the list of <code>InternetAddress</code> objects for all users who are configured
	 * with a valid user account.  User's who have not specified an email address will be omitted
	 * from the resulting list.
	 * 
	 * @param userList  the list of users for which to return email addresses
	 * @return List<InternetAddress>
	 */
	private List<InternetAddress> getEmailAddresses(List<String> userList) {
		RepositorySecurityManager securityManager = RepositoryComponentFactory.getDefault().getSecurityManager();
		List<InternetAddress> emailAddresses = new ArrayList<>();
		
		for (String userId : userList) {
			try {
				UserPrincipal user = securityManager.getUser( userId );
				
				if ((user != null) && (user.getEmailAddress() != null) && (user.getEmailAddress().length() > 0)) {
					String fullName = user.getLastName();
					
					if (user.getFirstName() != null) {
						fullName = user.getFirstName() + " " + fullName;
					}
					emailAddresses.add( new InternetAddress( user.getEmailAddress(), fullName ) );
				}
				
			} catch (UnsupportedEncodingException e) {
				// Should never happen; ignore and keep going
			}
		}
		return emailAddresses;
	}
	
	/**
	 * Contstructs the subject line for an email notification using information from the
	 * given job.
	 * 
	 * @param job  the email notification job for which to generate the email message body
	 * @return String
	 */
	private String buildMessageSubject(NotificationJob job) {
		return MessageFormat.format( SUBJECT_FORMAT,
				job.getAction().getDisplayLabel( Locale.getDefault() ),
				manager.getDisplayName() );
	}
	
	/**
	 * Contstructs the message body for an email notification using information from the
	 * given job.
	 * 
	 * @param job  the email notification job for which to generate the email message body
	 * @return String
	 */
	private String buildMessageBody(NotificationJob job) {
		RepositorySecurityManager securityManager = RepositoryComponentFactory.getDefault().getSecurityManager();
		UserPrincipal user = (job.getUserId() == null) ? null : securityManager.getUser( job.getUserId() );
		String templateLocation = new StringBuilder().append( TEMPLATE_LOCATION )
				.append( job.getAction().toString().toLowerCase() ).append( ".vm" ).toString();
		Template template = velocityEngine.getTemplate( templateLocation, "UTF-8" );
		VelocityContext context = new VelocityContext();
		StringWriter writer = new StringWriter();
		
		// Handle some special cases before populating the context
		if (job.getAction() == RepositoryActionType.UNLOCK) {
			if (REMARK_UNLOCK_REVERT.equals( job.getRemarks() )) {
				job.remarks = null;
				context.put( "isUnlockRevert", true );
			}
		}
		if (job.getAction() == RepositoryActionType.LIBRARY_MOVED) {
			if (job.getRemarks() != null) {
				String newBaseNamespace;
				try {
					VersionSchemeFactory factory = VersionSchemeFactory.getInstance();
					VersionScheme vScheme = factory.getVersionScheme( job.getItem().getVersionScheme() );
					
					newBaseNamespace = vScheme.getBaseNamespace( job.getRemarks() );
					
				} catch (VersionSchemeException e) {
					newBaseNamespace = job.getRemarks();
				}
				context.put( "newBaseNamespace", newBaseNamespace );
			}
		}
		
		context.put( "repositoryName", manager.getDisplayName() );
		context.put( "baseRepositoryUrl", baseRepositoryUrl );
		context.put( "action", job.getAction() );
		context.put( "affectedNamespace", job.getAffectedNamespace() );
		context.put( "item", job.getItem() );
		context.put( "remarks", job.getRemarks() );
		context.put( "user", user );
		context.put( "utils", new TemplateUtils() );
		
		template.merge( context, writer );
		return writer.toString();
	}
	
	/**
     * Encapsulates all information required in order to process an email notification job.
     */
    private class NotificationJob implements Delayed {
    	
    	private RepositoryActionType action;
    	private String userId;
    	private String affectedNamespace;
    	private RepositoryItem item;
    	private String remarks;
    	private long delayExpiration;
    	
    	/**
    	 * Constructor used for a job that notifies users of a namespace-related event.
    	 * 
    	 * @param action  the action that was performed
    	 * @param userId  the ID of the user who performed the action
    	 * @param affectedNamespace  the namespace upon which the action was performed
    	 * @param remarks  free-text remarks submitted as part of the notification content
    	 */
    	public NotificationJob(RepositoryActionType action, String userId, String affectedNamespace, String remarks) {
    		this.action = action;
    		this.userId = userId;
    		this.affectedNamespace = affectedNamespace;
    		this.remarks = remarks;
    		this.delayExpiration = System.currentTimeMillis() + notificationDelay;
    	}

    	/**
    	 * Constructor used for a job that notifies users of a library-related event.
    	 * 
    	 * @param action  the action that was performed
    	 * @param userId  the ID of the user who performed the action
    	 * @param affectedNamespace  the repository item (library) upon which the action was performed
    	 * @param remarks  free-text remarks submitted as part of the notification content
    	 */
    	public NotificationJob(RepositoryActionType action, String userId, RepositoryItem item, String remarks) {
    		this.action = action;
    		this.userId = userId;
    		this.item = item;
    		this.remarks = remarks;
    		this.delayExpiration = System.currentTimeMillis() + notificationDelay;
    	}

		/**
		 * Returns the the action that was performed.
		 *
		 * @return RepositoryActionType
		 */
		public RepositoryActionType getAction() {
			return action;
		}

		/**
		 * Returns the ID of the user who performed the action.
		 *
		 * @return String
		 */
		public String getUserId() {
			return userId;
		}

		/**
		 * Returns the namespace upon which the action was performed.
		 *
		 * @return String
		 */
		public String getAffectedNamespace() {
			return affectedNamespace;
		}

		/**
		 * Returns the repository item (library) upon which the action was performed.
		 *
		 * @return RepositoryItem
		 */
		public RepositoryItem getItem() {
			return item;
		}

		/**
		 * Returns the free-text remarks that were submitted as part of the notification
		 * content.
		 *
		 * @return String
		 */
		public String getRemarks() {
			return remarks;
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Delayed obj) {
			NotificationJob other = (NotificationJob) obj;
			int result;
			
			if (this.delayExpiration == other.delayExpiration) {
				result = 0;
			} else {
				result = (this.delayExpiration < other.delayExpiration) ? -1 : 1;
			}
			return result;
		}

		/**
		 * @see java.util.concurrent.Delayed#getDelay(java.util.concurrent.TimeUnit)
		 */
		@Override
		public long getDelay(TimeUnit unit) {
			return delayExpiration - System.currentTimeMillis();
		}
    	
    }
    
	/**
	 * Initializes the Velocity template processing engine.
	 */
	static {
		try {
			VelocityEngine ve = new VelocityEngine();
			
			ve.setProperty( RuntimeConstants.RESOURCE_LOADER, "classpath" );
			ve.setProperty( "classpath.resource.loader.class", ClasspathResourceLoader.class.getName() );
			velocityEngine = ve;
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError( t );
		}
	}
	
}

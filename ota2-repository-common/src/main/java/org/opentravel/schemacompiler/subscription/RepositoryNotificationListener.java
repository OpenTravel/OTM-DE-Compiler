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

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryListener;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * Forwards notifications to the <code>SubscriptionManager</code> when actionable events
 * occur in an OTM repository.
 */
public class RepositoryNotificationListener implements RepositoryListener {
	
    private static Log log = LogFactory.getLog( RepositoryNotificationListener.class );
    
	private SubscriptionManager subscriptionManager;
	private RepositoryManager repositoryManager;
	
	/**
	 * Constructor that initializes the repository and subscription manager componets
	 * for this listener.
	 * 
	 * @param subscriptionManager  the subscription manager to be notified
	 * @param repositoryManager  the repository manager from which to forward events
	 */
	public RepositoryNotificationListener(SubscriptionManager subscriptionManager,
			RepositoryManager repositoryManager) {
		this.subscriptionManager = subscriptionManager;
		this.repositoryManager = repositoryManager;
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onCreateRootNamespace(java.lang.String)
	 */
	@Override
	public void onCreateRootNamespace(String rootNamespace) {
		// No action required
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onDeleteRootNamespace(java.lang.String)
	 */
	@Override
	public void onDeleteRootNamespace(String rootNamespace) {
		// No action required
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onCreateNamespace(java.lang.String)
	 */
	@Override
	public void onCreateNamespace(String baseNamespace) {
		try {
			subscriptionManager.notifySubscribedUsers( baseNamespace, RepositoryActionType.NS_CREATED );
			
		} catch (Throwable t) {
			log.warn("Error during subscriber notification.", t);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onDeleteNamespace(java.lang.String)
	 */
	@Override
	public void onDeleteNamespace(String baseNamespace) {
		try {
			subscriptionManager.notifySubscribedUsers( baseNamespace, RepositoryActionType.NS_DELETED );
			
		} catch (Throwable t) {
			log.warn("Error during subscriber notification.", t);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onPublish(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void onPublish(RepositoryItem item) {
		try {
			List<RepositoryItem> nsItems = repositoryManager.listItems( item.getBaseNamespace(), null, false );
			Iterator<RepositoryItem> iterator = nsItems.iterator();
			
			// Filter out all of the libraries in this base namespace that do not share the same
			// library name as the item we just published.  If the list size is one (1), it is a
			// completely new library; if not, it is a new version of an existing library.
			while (iterator.hasNext()) {
				RepositoryItem nsItem = iterator.next();
				
				if (!nsItem.getLibraryName().equals( item.getLibraryName() )) {
					iterator.remove();
				}
			}
			subscriptionManager.notifySubscribedUsers( item, (nsItems.size() == 1) ?
						RepositoryActionType.PUBLISH : RepositoryActionType.NEW_VERSION, null );
			
		} catch (Throwable t) {
			log.warn("Error during subscriber notification.", t);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onCommit(org.opentravel.schemacompiler.repository.RepositoryItem, java.lang.String)
	 */
	@Override
	public void onCommit(RepositoryItem item, String remarks) {
		try {
			subscriptionManager.notifySubscribedUsers( item, RepositoryActionType.COMMIT, remarks );
			
		} catch (Throwable t) {
			log.warn("Error during subscriber notification.", t);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onLock(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void onLock(RepositoryItem item) {
		try {
			subscriptionManager.notifySubscribedUsers( item, RepositoryActionType.LOCK, null );
			
		} catch (Throwable t) {
			log.warn("Error during subscriber notification.", t);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onUnlock(org.opentravel.schemacompiler.repository.RepositoryItem, boolean, java.lang.String)
	 */
	@Override
	public void onUnlock(RepositoryItem item, boolean committedWIP, String remarks) {
		try {
			if (!committedWIP) {
				remarks = SubscriptionManager.REMARK_UNLOCK_REVERT;
			}
			subscriptionManager.notifySubscribedUsers( item, RepositoryActionType.UNLOCK, remarks );
			
		} catch (Throwable t) {
			log.warn("Error during subscriber notification.", t);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onPromote(org.opentravel.schemacompiler.repository.RepositoryItem, org.opentravel.schemacompiler.model.TLLibraryStatus)
	 */
	@Override
	public void onPromote(RepositoryItem item, TLLibraryStatus originalStatus) {
		try {
			subscriptionManager.notifySubscribedUsers(
					item, RepositoryActionType.PROMOTE, originalStatus.toString() );
			
		} catch (Throwable t) {
			log.warn("Error during subscriber notification.", t);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onDemote(org.opentravel.schemacompiler.repository.RepositoryItem, org.opentravel.schemacompiler.model.TLLibraryStatus)
	 */
	@Override
	public void onDemote(RepositoryItem item, TLLibraryStatus originalStatus) {
		try {
			subscriptionManager.notifySubscribedUsers(
					item, RepositoryActionType.DEMOTE, originalStatus.toString() );
			
		} catch (Throwable t) {
			log.warn("Error during subscriber notification.", t);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onUpdateStatus(org.opentravel.schemacompiler.repository.RepositoryItem, org.opentravel.schemacompiler.model.TLLibraryStatus)
	 */
	@Override
	public void onUpdateStatus(RepositoryItem item, TLLibraryStatus originalStatus) {
		try {
			TLLibraryStatus newStatus = item.getStatus();
			
			if ((originalStatus != null) && (newStatus != null)) {
				boolean isPromote = originalStatus.getRank() < newStatus.getRank();
				
				subscriptionManager.notifySubscribedUsers( item, isPromote ?
						RepositoryActionType.PROMOTE : RepositoryActionType.DEMOTE,
						newStatus.toString() );
			}
			
		} catch (Throwable t) {
			log.warn("Error during subscriber notification.", t);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onRecalculateCrc(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void onRecalculateCrc(RepositoryItem item) {
		try {
			subscriptionManager.notifySubscribedUsers( item, RepositoryActionType.CRC_UPDATED, null );
			
		} catch (Throwable t) {
			log.warn("Error during subscriber notification.", t);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onDelete(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void onDelete(RepositoryItem item) {
		try {
			subscriptionManager.notifySubscribedUsers( item, RepositoryActionType.DELETE, null );
			
		} catch (Throwable t) {
			log.warn("Error during subscriber notification.", t);
		}
	}
	
}

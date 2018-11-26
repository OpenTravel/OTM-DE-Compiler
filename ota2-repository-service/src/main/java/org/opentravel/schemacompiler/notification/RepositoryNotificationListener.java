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

import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryListener;

/**
 * Repository listener that broadcasts notifications when content modification
 * events occur.
 */
public class RepositoryNotificationListener implements RepositoryListener {
	
	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onPublish(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void onPublish(RepositoryItem item) {
		getService().itemPublished( item );
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onCommit(org.opentravel.schemacompiler.repository.RepositoryItem, java.lang.String)
	 */
	@Override
	public void onCommit(RepositoryItem item, String remarks) {
		getService().itemModified( item );
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onLock(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void onLock(RepositoryItem item) {
		getService().itemLocked( item );
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onUnlock(org.opentravel.schemacompiler.repository.RepositoryItem, boolean, java.lang.String)
	 */
	@Override
	public void onUnlock(RepositoryItem item, boolean committedWIP, String remarks) {
		getService().itemUnlocked( item );
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onPromote(org.opentravel.schemacompiler.repository.RepositoryItem, org.opentravel.schemacompiler.model.TLLibraryStatus)
	 */
	@Override
	public void onPromote(RepositoryItem item, TLLibraryStatus originalStatus) {
		getService().itemStatusChanged( item );
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onDemote(org.opentravel.schemacompiler.repository.RepositoryItem, org.opentravel.schemacompiler.model.TLLibraryStatus)
	 */
	@Override
	public void onDemote(RepositoryItem item, TLLibraryStatus originalStatus) {
		getService().itemStatusChanged( item );
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onUpdateStatus(org.opentravel.schemacompiler.repository.RepositoryItem, org.opentravel.schemacompiler.model.TLLibraryStatus)
	 */
	@Override
	public void onUpdateStatus(RepositoryItem item, TLLibraryStatus originalStatus) {
		getService().itemStatusChanged( item );
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onRecalculateCrc(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void onRecalculateCrc(RepositoryItem item) {
		getService().itemModified( item );
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onDelete(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void onDelete(RepositoryItem item) {
		getService().itemDeleted( item );
	}
	
	/**
	 * Returns a handle to the <code>NotificationService</code>.
	 * 
	 * @return NotificationService
	 */
	private NotificationService getService() {
		return NotificationServiceFactory.getInstance().getService();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onCreateRootNamespace(java.lang.String)
	 */
	@Override
	public void onCreateRootNamespace(String rootNamespace) {}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onDeleteRootNamespace(java.lang.String)
	 */
	@Override
	public void onDeleteRootNamespace(String rootNamespace) {}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onCreateNamespace(java.lang.String)
	 */
	@Override
	public void onCreateNamespace(String baseNamespace) {}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryListener#onDeleteNamespace(java.lang.String)
	 */
	@Override
	public void onDeleteNamespace(String baseNamespace) {}

}

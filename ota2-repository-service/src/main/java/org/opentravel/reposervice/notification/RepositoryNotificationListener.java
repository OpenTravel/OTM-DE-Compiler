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

package org.opentravel.reposervice.notification;

import org.opentravel.repocommon.notification.NotificationService;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryListener;

/**
 * Repository listener that broadcasts notifications when content modification events occur.
 */
public class RepositoryNotificationListener implements RepositoryListener {

    /**
     * @see org.opentravel.schemacompiler.resource.RepositoryListener#onPublish(org.opentravel.schemacompiler.resource.RepositoryItem)
     */
    @Override
    public void onPublish(RepositoryItem item) {
        getService().itemPublished( item );
    }

    /**
     * @see org.opentravel.schemacompiler.resource.RepositoryListener#onCommit(org.opentravel.schemacompiler.resource.RepositoryItem,
     *      java.lang.String)
     */
    @Override
    public void onCommit(RepositoryItem item, String remarks) {
        getService().itemModified( item );
    }

    /**
     * @see org.opentravel.schemacompiler.resource.RepositoryListener#onLock(org.opentravel.schemacompiler.resource.RepositoryItem)
     */
    @Override
    public void onLock(RepositoryItem item) {
        getService().itemLocked( item );
    }

    /**
     * @see org.opentravel.schemacompiler.resource.RepositoryListener#onUnlock(org.opentravel.schemacompiler.resource.RepositoryItem,
     *      boolean, java.lang.String)
     */
    @Override
    public void onUnlock(RepositoryItem item, boolean committedWIP, String remarks) {
        getService().itemUnlocked( item );
    }

    /**
     * @see org.opentravel.schemacompiler.resource.RepositoryListener#onPromote(org.opentravel.schemacompiler.resource.RepositoryItem,
     *      org.opentravel.schemacompiler.model.TLLibraryStatus)
     */
    @Override
    public void onPromote(RepositoryItem item, TLLibraryStatus originalStatus) {
        getService().itemStatusChanged( item );
    }

    /**
     * @see org.opentravel.schemacompiler.resource.RepositoryListener#onDemote(org.opentravel.schemacompiler.resource.RepositoryItem,
     *      org.opentravel.schemacompiler.model.TLLibraryStatus)
     */
    @Override
    public void onDemote(RepositoryItem item, TLLibraryStatus originalStatus) {
        getService().itemStatusChanged( item );
    }

    /**
     * @see org.opentravel.schemacompiler.resource.RepositoryListener#onUpdateStatus(org.opentravel.schemacompiler.resource.RepositoryItem,
     *      org.opentravel.schemacompiler.model.TLLibraryStatus)
     */
    @Override
    public void onUpdateStatus(RepositoryItem item, TLLibraryStatus originalStatus) {
        getService().itemStatusChanged( item );
    }

    /**
     * @see org.opentravel.schemacompiler.resource.RepositoryListener#onRecalculateCrc(org.opentravel.schemacompiler.resource.RepositoryItem)
     */
    @Override
    public void onRecalculateCrc(RepositoryItem item) {
        getService().itemModified( item );
    }

    /**
     * @see org.opentravel.schemacompiler.resource.RepositoryListener#onDelete(org.opentravel.schemacompiler.resource.RepositoryItem)
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
     * @see org.opentravel.schemacompiler.resource.RepositoryListener#onCreateRootNamespace(java.lang.String)
     */
    @Override
    public void onCreateRootNamespace(String rootNamespace) {
        // No action required for namespace actions
    }

    /**
     * @see org.opentravel.schemacompiler.resource.RepositoryListener#onDeleteRootNamespace(java.lang.String)
     */
    @Override
    public void onDeleteRootNamespace(String rootNamespace) {
        // No action required for namespace actions
    }

    /**
     * @see org.opentravel.schemacompiler.resource.RepositoryListener#onCreateNamespace(java.lang.String)
     */
    @Override
    public void onCreateNamespace(String baseNamespace) {
        // No action required for namespace actions
    }

    /**
     * @see org.opentravel.schemacompiler.resource.RepositoryListener#onDeleteNamespace(java.lang.String)
     */
    @Override
    public void onDeleteNamespace(String baseNamespace) {
        // No action required for namespace actions
    }

}

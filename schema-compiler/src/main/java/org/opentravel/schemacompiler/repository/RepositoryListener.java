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

package org.opentravel.schemacompiler.repository;

import org.opentravel.schemacompiler.model.TLLibraryStatus;

/**
 * Interface to be implemented by any component wishing to be notified of local events performed by a
 * <code>RepositoryManager</code> instance. The methods of this interface will only be invoked if the corresponding
 * repository action was successful.
 */
public interface RepositoryListener {

    /**
     * Called when a root namespace is created in the repository.
     * 
     * @param rootNamespace the root namespace that was created
     */
    public void onCreateRootNamespace(String rootNamespace);

    /**
     * Called when a root namespace is deleted from the repository.
     * 
     * @param rootNamespace the root namespace that was deleted
     */
    public void onDeleteRootNamespace(String rootNamespace);

    /**
     * Called when a namespace is created in the repository.
     * 
     * @param baseNamespace the base namespace that was created
     */
    public void onCreateNamespace(String baseNamespace);

    /**
     * Called when a namespace is deleted from the repository.
     * 
     * @param baseNamespace the base namespace that was deleted
     */
    public void onDeleteNamespace(String baseNamespace);

    /**
     * Called when a new item is published for the first time to this repository.
     * 
     * @param item the repository item that was published
     */
    public void onPublish(RepositoryItem item);

    /**
     * Called when changes have been committed to the given repository item.
     * 
     * @param item the repository item for which changes were committed
     * @param remarks free-text remarks that describe the nature of the change
     */
    public void onCommit(RepositoryItem item, String remarks);

    /**
     * Called when the specified repository item has been locked by a user.
     * 
     * @param item the repository item that was locked
     */
    public void onLock(RepositoryItem item);

    /**
     * Called when the specified repository item has been unlocked by a user.
     * 
     * @param item the repository item to lock
     * @param committedWip flag indicating that WIP changes were committed prior to releasing the lock
     * @param remarks free-text remarks that describe the nature of the change (ignored if 'committedWIP' is false)
     */
    public void onUnlock(RepositoryItem item, boolean committedWip, String remarks);

    /**
     * Called when a repository item has been promoted from its current lifecycle status to the next available one.
     * 
     * @param item the repository item that was promoted
     * @param originalStatus the original status of the repository item
     */
    public void onPromote(RepositoryItem item, TLLibraryStatus originalStatus);

    /**
     * Called when a repository item has been demoted from its current lifecycle status to the previous available one.
     * 
     * @param item the repository item that was demoted
     * @param originalStatus the original status of the repository item
     */
    public void onDemote(RepositoryItem item, TLLibraryStatus originalStatus);

    /**
     * Called when the status of a repository item has been modified.
     * 
     * @param item the repository item whose status is to be updated
     * @param originalStatus the original status of the repository item
     */
    public void onUpdateStatus(RepositoryItem item, TLLibraryStatus originalStatus);

    /**
     * Called when the CRC of a repository item has been recalculated.
     * 
     * @param item the repository item whose CRC was recalculated
     */
    public void onRecalculateCrc(RepositoryItem item);

    /**
     * Called when a repository item has been deleted from the repository.
     * 
     * @param item the repository item that was deleted
     */
    public void onDelete(RepositoryItem item);

}

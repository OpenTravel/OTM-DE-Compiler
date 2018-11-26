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

import org.opentravel.schemacompiler.repository.RepositoryItem;

/**
 * Interface used for broadcasting notifications of events that modify
 * content within the OTM repository.
 */
public interface NotificationService {
	
	/**
	 * Permanently shuts down the service and frees any system resources being held.
	 */
	public void shutdown();
	
	/**
	 * Notifies remote observers that the given repository item was published.
	 * 
	 * @param item  the affected repository item
	 */
	public void itemPublished(RepositoryItem item);
	
	/**
	 * Notifies remote observers that the given repository item was modified.
	 * 
	 * @param item  the affected repository item
	 */
	public void itemModified(RepositoryItem item);
	
	/**
	 * Notifies remote observers that the given repository item was locked.
	 * 
	 * @param item  the affected repository item
	 */
	public void itemLocked(RepositoryItem item);
	
	/**
	 * Notifies remote observers that the given repository item was unlocked.
	 * 
	 * @param item  the affected repository item
	 */
	public void itemUnlocked(RepositoryItem item);
	
	/**
	 * Notifies remote observers that the status of the given repository item
	 * was changed.
	 * 
	 * @param item  the affected repository item
	 */
	public void itemStatusChanged(RepositoryItem item);
	
	/**
	 * Notifies remote observers that the given repository item was deleted.
	 * 
	 * @param item  the affected repository item
	 */
	public void itemDeleted(RepositoryItem item);
	
}

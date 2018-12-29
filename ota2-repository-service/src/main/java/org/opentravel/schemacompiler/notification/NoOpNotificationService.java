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
 * Default implementation of the <code>NotificationService</code> that does
 * nothing.
 */
public class NoOpNotificationService implements NotificationService {

	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#startup()
	 */
	@Override
	public void startup() {
		// No action needed for the no-op service implementation
	}

	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#shutdown()
	 */
	@Override
	public void shutdown() {
		// No action needed for the no-op service implementation
	}

	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#itemPublished(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void itemPublished(RepositoryItem item) {
		// No action needed for the no-op service implementation
	}

	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#itemModified(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void itemModified(RepositoryItem item) {
		// No action needed for the no-op service implementation
	}

	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#itemLocked(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void itemLocked(RepositoryItem item) {
		// No action needed for the no-op service implementation
	}

	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#itemUnlocked(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void itemUnlocked(RepositoryItem item) {
		// No action needed for the no-op service implementation
	}

	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#itemStatusChanged(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void itemStatusChanged(RepositoryItem item) {
		// No action needed for the no-op service implementation
	}

	/**
	 * @see org.opentravel.schemacompiler.notification.NotificationService#itemDeleted(org.opentravel.schemacompiler.repository.RepositoryItem)
	 */
	@Override
	public void itemDeleted(RepositoryItem item) {
		// No action needed for the no-op service implementation
	}
	
}

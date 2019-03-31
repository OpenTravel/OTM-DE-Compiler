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

import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * Factory used to obtain a working instance of the <code>NotificationService</code>. If no service is configured in the
 * repository's configuration file, the no-op service implementation will be returned.
 */
public class NotificationServiceFactory {

    private static final RepositoryNotificationListener repoListener = new RepositoryNotificationListener();

    private static NotificationServiceFactory instance = null;

    private NotificationService service;

    /**
     * Private constructor that initializes the <code>NotificationService</code> instance.
     */
    private NotificationServiceFactory() {
        service = RepositoryComponentFactory.getDefault().getNotificationService();

        if (service == null) {
            service = new NoOpNotificationService();
        }
    }

    /**
     * Initializes the singleton instance of the factory and allocates any system resources required by the
     * <code>NotificationService</code>.
     * 
     * @return NotificationServiceFactory
     */
    public static synchronized NotificationServiceFactory getInstance() {
        if (instance == null) {
            instance = new NotificationServiceFactory();
        }
        return instance;
    }

    /**
     * Starts the service and allocates any system resources that are required for the implementation.
     */
    public static synchronized void startup() {
        RepositoryManager manager = RepositoryComponentFactory.getDefault().getRepositoryManager();

        getInstance().getService().startup();
        manager.addListener( repoListener );
    }

    /**
     * Shuts down the factory and releases any system resources that are being held.
     */
    public static synchronized void shutdown() {
        if (instance != null) {
            RepositoryManager manager = RepositoryComponentFactory.getDefault().getRepositoryManager();

            instance.getService().shutdown();
            manager.removeListener( repoListener );
            instance = null;
        }
    }

    /**
     * Returns the notification service instance.
     * 
     * @return NotificationService
     */
    public NotificationService getService() {
        return service;
    }

}

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

import org.opentravel.schemacompiler.notification.NotificationService;
import org.opentravel.schemacompiler.security.AuthenticationProvider;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.subscription.SubscriptionManager;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import java.io.File;

/**
 * Handles the creation of key repository components using a Spring application context file.
 * 
 * @author S. Livezey
 */
public class RepositoryComponentFactory {

    public static final String SERVICE_CONFIGURATION_FILENAME = "ota2-repository-config.xml";

    private static final String REPOSITORY_LOCATION_KEY = "repositoryLocation";
    private static final String SEARCH_INDEX_LOCATION_KEY = "searchIndexLocation";
    private static final String REPOSITORY_MANAGER_KEY = "repositoryManager";
    private static final String SECURITY_MANAGER_KEY = "securityManager";
    private static final String AUTHENTICATION_PROVIDER_KEY = "authenticationProvider";
    private static final String SUBSCRIPTION_MANAGER_KEY = "subscriptionManager";
    private static final String NOTIFICATION_SERVICE_KEY = "notificationService";
    private static final String INDEXING_JMS_SERVICE_KEY = "indexingJmsService";

    private static RepositoryComponentFactory defaultInstance;
    private static final Object defaultInstanceLock = new Object();

    private ApplicationContext appContext;

    /**
     * Constructor that specifies the Spring application context file that contains the service configuration settings.
     * 
     * @param serviceConfigurationFile the location of the service configuration file
     */
    public RepositoryComponentFactory(File serviceConfigurationFile) {
        this( serviceConfigurationFile.getAbsolutePath() );
    }

    /**
     * Constructor that specifies the Spring application context file that contains the service configuration settings.
     * 
     * @param serviceConfigurationFile the location of the service configuration file
     */
    public RepositoryComponentFactory(String serviceConfigurationFile) {
        if (serviceConfigurationFile.startsWith( "classpath:" )) {
            appContext = new ClassPathXmlApplicationContext( serviceConfigurationFile );

        } else {
            // For non-windows file systems, we need to add the "file://" prefix to avoid
            // spring's default behavior that interprets the filename as a relative path
            if (!System.getProperty( "os.name" ).toLowerCase().startsWith( "windows" )) {
                serviceConfigurationFile = "file://" + serviceConfigurationFile;
            }
            appContext = new FileSystemXmlApplicationContext( serviceConfigurationFile );
        }
    }

    /**
     * Returns the factory that is configured using the default application context file. The default context XML is
     * identified by looking in the following locations:
     * <ol>
     * <li>If running in a Tomcat container environment, the file location "
     * <code>${catalina.base}/conf/ota2-repository-config.xml</code>" is checked first</li>
     * <li>Next, the location identified by the system property "<code>ota2.repository.config</code> " is checked</li>
     * <li>If neither of the previous locations contains a valid application context file, the user's current working
     * directory is checked for a file named " <code>ota2-repository-config.xml</code>"</li>
     * </ol>
     * 
     * @return RepositoryComponentFactory
     */
    public static RepositoryComponentFactory getDefault() {
        synchronized (defaultInstanceLock) {
            if (defaultInstance == null) {
                defaultInstance = new RepositoryComponentFactory( findConfigurationFile() );
            }
            return defaultInstance;
        }
    }

    /**
     * Resets the default singleton instance of this factory, allowing the JVM to reconfigure the location of the
     * configuration file.
     * 
     * <p>
     * NOTE: This method is only intended for testing purposes and may produce unpredictable results in a production
     * environment.
     */
    public static void resetDefault() {
        synchronized (defaultInstanceLock) {
            defaultInstance = null;
        }
    }

    /**
     * Returns the root folder location of the OTA2.0 repository as defined in the service configuration file.
     * 
     * @return File
     */
    public File getRepositoryLocation() {
        return (File) appContext.getBean( REPOSITORY_LOCATION_KEY );
    }

    /**
     * Returns the root folder location of the Lucene index for the OTA2.0 repository.
     * 
     * @return File
     */
    public File getSearchIndexLocation() {
        return (File) appContext.getBean( SEARCH_INDEX_LOCATION_KEY );
    }

    /**
     * Returns the <code>RepositoryManager</code> as defined in the service configuration file.
     * 
     * @return RepositoryManager
     */
    public RepositoryManager getRepositoryManager() {
        return (RepositoryManager) appContext.getBean( REPOSITORY_MANAGER_KEY );
    }

    /**
     * Returns the <code>RepositorySecurityManager</code> as defined in the service configuration file.
     * 
     * @return RepositorySecurityManager
     */
    public RepositorySecurityManager getSecurityManager() {
        return (RepositorySecurityManager) appContext.getBean( SECURITY_MANAGER_KEY );
    }

    /**
     * Returns the <code>AuthenticationProvider</code> as defined in the service configuration file.
     * 
     * @return AuthenticationProvider
     */
    public AuthenticationProvider getAuthenticationProvider() {
        return (AuthenticationProvider) appContext.getBean( AUTHENTICATION_PROVIDER_KEY );
    }

    /**
     * Returns the <code>SubscriptionManager</code> as defined in the service configuration file. If no subscription
     * manager has been configured, this method will return null.
     * 
     * @return SubscriptionManager
     */
    public SubscriptionManager getSubscriptionManager() {
        SubscriptionManager manager = null;

        try {
            manager = (SubscriptionManager) appContext.getBean( SUBSCRIPTION_MANAGER_KEY );

        } catch (NoSuchBeanDefinitionException e) {
            // Ignore - subscription manager is an optional component
        }
        return manager;
    }

    /**
     * Returns the <code>JmsTemplate</code> that will serve as the indexing service to publish indexing jobs to a remote
     * server. If no notification JMS service has been configured, this method will return null.
     * 
     * @return JmsTemplate
     */
    public JmsTemplate getIndexingJmsService() {
        JmsTemplate service = null;

        try {
            service = (JmsTemplate) appContext.getBean( INDEXING_JMS_SERVICE_KEY );

        } catch (NoSuchBeanDefinitionException e) {
            // Ignore - subscription manager is an optional component
        }
        return service;
    }

    /**
     * Returns the <code>NotificationService</code> as defined in the service configuration file. If no notification
     * service has been configured, this method will return null.
     * 
     * @return NotificationService
     */
    public NotificationService getNotificationService() {
        NotificationService manager = null;

        try {
            manager = (NotificationService) appContext.getBean( NOTIFICATION_SERVICE_KEY );

        } catch (NoSuchBeanDefinitionException e) {
            // Ignore - notification service is an optional component
        }
        return manager;
    }

    /**
     * Returns the location of the repository configuration file.
     * 
     * @return File
     */
    private static File findConfigurationFile() {
        File configFile = null;

        if (System.getProperties().containsKey( "catalina.base" )) {
            configFile = new File( System.getProperty( "catalina.base" ), "/conf/" + SERVICE_CONFIGURATION_FILENAME );

            if (!configFile.exists()) {
                configFile = null;
            }
        }
        if ((configFile == null) && System.getProperties().containsKey( "ota2.repository.config" )) {
            configFile = new File( System.getProperty( "ota2.repository.config" ) );

            if (!configFile.exists()) {
                configFile = null;
            }
        }
        if (configFile == null) {
            configFile = new File( System.getProperty( "user.dir" ), SERVICE_CONFIGURATION_FILENAME );
        }
        return configFile;
    }

}

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

package org.opentravel.schemacompiler.index;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * Factory that provides access to a singleton instance of the <code>FreeTextSearchService</code>.
 * 
 * @author S. Livezey
 */
public class FreeTextSearchServiceFactory {
	
    private static FreeTextSearchService defaultInstance;
    private static Set<Object> serviceOwners = new HashSet<Object>();
    private static boolean realTimeIndexing = false;
    
    /**
     * Returns the singleton instance of the service. This method only returns a service instance
     * after the initializeInstance() has been called successfully.
     * 
     * @return FreeTextSearchService
     */
    public static FreeTextSearchService getInstance() {
        return defaultInstance;
    }

    /**
     * Constructs a singleton instance of the service and starts it using the information provided.
     * If a singleton instance has already been initialized, this method takes no action.
     * 
     * @param indexLocation
     *            the folder location of the index directory
     * @param repositoryManager
     *            the repository that owns all content to be indexed
     * @throws IOException
     *             thrown if a low-level error occurs while initializing the search index
     */
    public static synchronized void initializeSingleton(File indexLocation,
            RepositoryManager repositoryManager) throws IOException {
        if (defaultInstance == null) {
        	if (realTimeIndexing) {
                defaultInstance = new RealTimeFreeTextSearchService(indexLocation, repositoryManager);
        	} else {
                defaultInstance = new JMSFreeTextSearchService(indexLocation, repositoryManager);
        	}
            defaultInstance.startService();
        }
    }

    /**
     * Shuts down the running service and nulls the singleton if two conditions are met. First, a
     * singleton must already exist. Second, the collection of service owners must be empty. If
     * either of these two conditions is not met, this method will take no action.
     * 
     * @throws IOException
     *             thrown if a low-level error occurs while closing the service's index reader or
     *             writer
     */
    public static synchronized void destroySingleton() throws IOException {
        if ((defaultInstance != null) && serviceOwners.isEmpty()) {
            try {
                defaultInstance.stopService();

            } finally {
                defaultInstance = null;
            }
        }
    }
    
    /**
     * Registers the given component as an owner of the singleton instance of this service.
     * 
     * <p>
     * NOTE: The singleton instance of the service DOES NOT have to be initialized to register a
     * service owner.
     * 
     * @param owner
     *            the component to be registered as a service owner
     */
    public static synchronized void registerServiceOwner(Object owner) {
        if (owner != null) {
            serviceOwners.add(owner);
        }
    }

    /**
     * Un-registers the given component as an owner of the singleton instance of this service.
     * 
     * @param owner
     *            the component to be removed from the list of registered service owners
     */
    public static synchronized void unregisterServiceOwner(Object owner) {
        serviceOwners.remove(owner);
    }
    
    /**
     * Returns true if real-time indexing has been enabled. By default, this value is false to
     * enable background processing of indexing tasks.
     * 
     * <p>
     * NOTE: Real-time indexing is intended for testing purposes only, and should not be utilized
     * for production repository deployments.
     * 
     * @return boolean
     */
    public static boolean isRealTimeIndexing() {
        return realTimeIndexing;
    }

    /**
     * Assigns the flag value that indicates whether real-time indexing has been enabled. By
     * default, this value is false to enable background processing of indexing tasks.
     * 
     * <p>
     * NOTE: Real-time indexing is intended for testing purposes only, and should not be utilized
     * for production repository deployments.
     * 
     * @param realTimeIndexing
     *            the flag value to assign
     */
    public static void setRealTimeIndexing(boolean realTimeIndexing) {
    	FreeTextSearchServiceFactory.realTimeIndexing = realTimeIndexing;
    }

    /**
     * Initializes the flag indicating whether real-time indexing has been requested.
     */
    static {
    	try {
            realTimeIndexing = System.getProperty("ota2.repository.realTimeIndexing", "false")
                    .equalsIgnoreCase("true");
    		
    	} catch (Throwable t) {
    		throw new ExceptionInInitializerError(t);
    	}
    }
    
}

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

package org.opentravel.repocommon.subscription;

import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionList;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Navigator that traverses all of the library and namespace subscriptions defined within an OTM repository.
 */
public class SubscriptionNavigator {

    private RepositoryManager manager;
    private SubscriptionFileUtils fileUtils;

    /**
     * Constructor that specifies the <code>RepositoryManager</code> to use when navigating the contents of the
     * repository.
     * 
     * @param manager the repository manager instance
     */
    public SubscriptionNavigator(RepositoryManager manager) {
        this.manager = manager;
        this.fileUtils = new SubscriptionFileUtils( manager );
    }

    /**
     * Recursively navigates all of the <code>SubscriptionList</code>s in the repository.
     * 
     * @param visitor the visitor to be called when a subscription list is discovered
     * @throws RepositoryException thrown if an error occurs while searching the repository
     */
    public void navigateSubscriptions(SubscriptionVisitor visitor) throws RepositoryException {
        for (String baseNS : manager.listBaseNamespaces()) {
            List<RepositoryItem> itemList = manager.listItems( baseNS, false, true );

            processSubscriptionList( baseNS, null, null, visitor );

            for (RepositoryItem item : itemList) {
                processSubscriptionList( baseNS, item.getLibraryName(), null, visitor );
                processSubscriptionList( baseNS, item.getLibraryName(), item.getVersion(), visitor );
            }
        }
    }

    /**
     * If a subscription list with the given characteristics exists, load its contents and call the visitor provided.
     * 
     * @param baseNS the base namespace of the subscription list target
     * @param libraryName the library name of the subscription list target (may be null)
     * @param version the library version of the subscription list target (may be null)
     * @param visitor the visitor to call if a valid subscription list file exists
     * @throws RepositoryException thrown if the subscription list file cannot be identified or is unreadable
     */
    private void processSubscriptionList(String baseNS, String libraryName, String version, SubscriptionVisitor visitor)
        throws RepositoryException {
        try {
            File subscriptionFile = fileUtils.getSubscriptionListFile( baseNS, libraryName, version );

            if (subscriptionFile.exists()) {
                SubscriptionList subscriptionList = fileUtils.loadFile( subscriptionFile );
                visitor.visitSubscriptionList( subscriptionList );
            }

        } catch (IOException e) {
            throw new RepositoryException( "Error processing subscription list.", e );
        }
    }

}

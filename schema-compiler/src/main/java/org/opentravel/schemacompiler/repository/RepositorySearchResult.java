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

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;

/**
 * Base class for items returned from a search in a remote repository.
 */
public abstract class RepositorySearchResult {

    private RepositoryItem repositoryItem;

    /**
     * Initializes the repository item for this search result.
     *
     * @param repositoryItem the repository item associated with this search result
     */
    public RepositorySearchResult(RepositoryItem repositoryItem) {
        this.repositoryItem = repositoryItem;
    }

    /**
     * Returns the repository item associated with the library or entity for this search result.
     *
     * @return RepositoryItem
     */
    public RepositoryItem getRepositoryItem() {
        return repositoryItem;
    }

    /**
     * Returns the library associated with this search result item from the given model. If no matching library exists
     * in the model provided, this method will return null.
     * 
     * @param model the model from which to retrieve the matching library
     * @return AbstractLibrary
     */
    public AbstractLibrary findLibrary(TLModel model) {
        return model.getLibrary( repositoryItem.getNamespace(), repositoryItem.getLibraryName() );
    }

    /**
     * If the associated repository item is locked by the local user, change its state to WIP.
     * 
     * @param manager the repository manager for the local environment
     */
    void checkItemState(RepositoryManager manager) {
        try {
            RepositoryUtils.checkItemState( (RepositoryItemImpl) getRepositoryItem(), manager );

        } catch (RepositoryException e) {
            // Ignore and retain the given item state
        }
    }

}

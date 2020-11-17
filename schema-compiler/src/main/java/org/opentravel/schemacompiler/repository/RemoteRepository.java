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

import org.opentravel.ns.ota2.repositoryinfo_v01_00.RefreshPolicy;
import org.opentravel.schemacompiler.model.NamedEntity;

import java.util.List;

/**
 * Interface that defines the interaction model between a local OTA2.0 project and a single remote repository.
 * 
 * @author S. Livezey
 */
public interface RemoteRepository extends Repository {

    /**
     * Contacts the remote web service to update all of the server-provided meta-data.
     * 
     * @throws RepositoryException thrown if the remote repository cannot be accessed
     */
    public void refreshRepositoryMetadata() throws RepositoryException;

    /**
     * Returns the web service endpoint URL of the remote repository.
     * 
     * @return String
     */
    public String getEndpointUrl();

    /**
     * Returns the policy value that indicates how often items that are cached in the local repository should be checked
     * for updates.
     * 
     * @return RefreshPolicy
     */
    public RefreshPolicy getRefreshPolicy();

    /**
     * Returns the list of repository items that reference the one provided.
     * 
     * @param item the repository item for which to retrieve the where-used items
     * @param includeIndirect flag indicating whether to include indirect where-used references; if false, only
     *        repository items that directly reference the given one will be returned
     * @return List&lt;RepositoryItem&gt;
     * @throws RepositoryException thrown if the remote repository cannot be accessed
     */
    public List<RepositoryItem> getItemWhereUsed(RepositoryItem item, boolean includeIndirect)
        throws RepositoryException;

    /**
     * Returns the list of entities that reference the given one. References may come in the form of field-level type
     * references, extensions, etc. If the given entity is not defined within this remote repository, this method will
     * return an empty list.
     * 
     * @param entity the entity for which to return where-used entity references
     * @param includeIndirect flag indicating whether to include indirect where-used references; if false, only entities
     *        that directly reference the given one will be returned
     * @return List&lt;EntitySearchResult&gt;
     * @throws RepositoryException thrown if the remote repository cannot be accessed
     */
    public List<EntitySearchResult> getEntityWhereUsed(NamedEntity entity, boolean includeIndirect)
        throws RepositoryException;

    /**
     * Returns the list of entities that extend the given one.
     * 
     * @param entity the entity for which to return the list of extending entities
     * @return List&lt;EntitySearchResult&gt;
     * @throws RepositoryException thrown if the remote repository cannot be accessed
     */
    public List<EntitySearchResult> getEntityWhereExtended(NamedEntity entity) throws RepositoryException;

    /**
     * Downloads the specified content (and its associated meta-data) from the remote repository into the local
     * instance. If the refresh policy for this repository does not require an update (or the remote repository is not
     * accessible), the locally cached copy of the content will be used.
     * 
     * <p>
     * This method will return true if the local copy was replaced by newer content from the remote repository. False
     * will be returned if the local copy was up-to-date, even if a refresh was forced by the caller or the update
     * policy.
     * 
     * @param item the reposited item whose content is to be downloaded into the local cache
     * @param forceUpdate disregards the repository's update policy and forces the remote content to be downloaded
     * @return boolean
     * @throws RepositoryException thrown if the remote repository cannot be accessed
     */
    public boolean downloadContent(RepositoryItem item, boolean forceUpdate) throws RepositoryException;

    /**
     * Returns true if the current user has administrator permissions for the remote repository.
     * 
     * @return boolean
     * @throws RepositoryException thrown if the remote repository cannot be accessed
     */
    public boolean isAdministrator() throws RepositoryException;

}

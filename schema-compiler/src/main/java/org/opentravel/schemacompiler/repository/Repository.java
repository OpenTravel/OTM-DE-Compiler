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

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.model.TLLibraryStatus;

/**
 * Interface that defines the interaction model between a local OTA2.0 project and a single
 * repository instance.
 * 
 * @author S. Livezey
 */
public interface Repository {

    /**
     * Returns the <code>RepositoryManager</code> that owns this <code>Repository</code> instance.
     * 
     * @return RepositoryManager
     */
    public RepositoryManager getManager();

    /**
     * Returns the local ID of the repository.
     * 
     * @return String
     */
    public String getId();

    /**
     * Returns the display name of the repository.
     * 
     * @return String
     */
    public String getDisplayName();

    /**
     * Returns a list of all root namespaces for the libraries stored and managed in the remote
     * repository. All items that are published to a repository must be assigned to a root namespace
     * or one of its child URI's.
     * 
     * @return List<String>
     * @throws RepositoryException
     *             thrown if the remote repository cannot be accessed
     */
    public List<String> listRootNamespaces() throws RepositoryException;

    /**
     * Returns the list of namespace paths from the repository that exist immediately under the
     * given namespace. Only base (non-versioned) namespace URI paths are returned by this method.
     * 
     * NOTE: The resulting list of strings do not contain full namespace URI paths. Instead, only
     * the path names of the immediate children are returned.
     * 
     * @param baseNamespace
     *            the base namespace for which to return child paths
     * @return List<String>
     * @throws RepositoryException
     *             thrown if the remote repository cannot be accessed
     */
    public List<String> listNamespaceChildren(String baseNamespace) throws RepositoryException;

    /**
     * Returns a list of all base namespaces for the libraries stored and managed in the remote
     * repository. Base namespaces do not include the trailing version component of the URI path.
     * 
     * @return List<String>
     * @throws RepositoryException
     *             thrown if the remote repository cannot be accessed
     */
    public List<String> listBaseNamespaces() throws RepositoryException;

    /**
     * Returns a list of all namespaces for the libraries stored and managed in the remote
     * repository. Each of the namespaces that are returned include the trailing version component
     * of the URI path.
     * 
     * @return List<String>
     * @throws RepositoryException
     *             thrown if the remote repository cannot be accessed
     */
    public List<String> listAllNamespaces() throws RepositoryException;

    /**
     * Returns a list of each <code>RepositoryItem</code> assigned to the specified base namespace.
     * If multiple versions of a <code>RepositoryItem</code> are present, only the latest version
     * will be returned when the 'latestVersionsOnly' flag is true. If the 'includeDraftVersions'
     * flag is false, only <code>FINAL</code> versions will be considered durng the search.
     * 
     * @param baseNamespace
     *            the base namespace that does not include the trailing version component of the URI
     *            path
     * @param latestVersionsOnly
     *            flag indicating whether the results should include all matching versions or just
     *            the latest version of each library
     * @param includeDraftVersions
     *            flag indicating whether items in <code>DRAFT</code> status should be included in
     *            the resulting list
     * @return List<RepositoryItem>
     * @throws RepositoryException
     *             thrown if the remote repository cannot be accessed
     * @deprecated  Use the {@link Repository#listItems(String, TLLibraryStatus, boolean)} method instead
     */
    @Deprecated
    public List<RepositoryItem> listItems(String baseNamespace, boolean latestVersionsOnly,
            boolean includeDraftVersions) throws RepositoryException;

    /**
     * Returns a list of each <code>RepositoryItem</code> assigned to the specified base namespace.
     * If multiple versions of a <code>RepositoryItem</code> are present, only the latest version
     * will be returned when the 'latestVersionsOnly' flag is true.  If the 'includeStatus' is
     * non-null, only versions with the specified status or later will be considered durng the
     * search.
     * 
     * @param baseNamespace
     *            the base namespace that does not include the trailing version component of the URI
     *            path
     * @param includeStatus
     *            indicates the latest library status to include in the results (null = all statuses)
     * @param latestVersionsOnly
     *            flag indicating whether the results should include all matching versions or just
     *            the latest version of each library
     * @return List<RepositoryItem>
     * @throws RepositoryException
     *             thrown if the remote repository cannot be accessed
     */
    public List<RepositoryItem> listItems(String baseNamespace, TLLibraryStatus includeStatus,
    		boolean latestVersionsOnly) throws RepositoryException;

    /**
     * Searches the contents of the repository using the free-text keywords provided. If multiple
     * versions of a <code>RepositoryItem</code> match the query, only the latest version will be
     * returned when the 'latestVersionsOnly' flag is true. If the 'includeDraftVersions' flag is
     * false, only <code>FINAL</code> versions will be considered durng the search.
     * 
     * @param freeTextQuery
     *            the string containing space-separated keywords for the free-text search
     * @param latestVersionsOnly
     *            flag indicating whether the results should include all matching versions or just
     *            the latest version of each library
     * @param includeDraftVersions
     *            flag indicating whether versions in <code>DRAFT</code> status should be considered
     *            by the search
     * @return List<RepositoryItem>
     * @throws RepositoryException
     *             thrown if the remote repository cannot be accessed
     * @deprecated  Use the {@link Repository#search(String, TLLibraryStatus, boolean)} method instead
     */
    @Deprecated
    public List<RepositoryItem> search(String freeTextQuery, boolean latestVersionsOnly,
            boolean includeDraftVersions) throws RepositoryException;

    /**
     * Searches the contents of the repository using the free-text keywords provided. If multiple
     * versions of a <code>RepositoryItem</code> match the query, only the latest version will be
     * returned when the 'latestVersionsOnly' flag is true. If the 'includeStatus' is non-null, only
     * versions with the specified status or later will be considered durng the search.
     * 
     * @param freeTextQuery
     *            the string containing space-separated keywords for the free-text search
     * @param includeStatus
     *            indicates the latest library status to include in the results (null = all statuses)
     * @param latestVersionsOnly
     *            flag indicating whether the results should include all matching versions or just
     *            the latest version of each library
     * @return List<RepositorySearchResult>
     * @throws RepositoryException
     *             thrown if the remote repository cannot be accessed
     */
    public List<RepositorySearchResult> search(String freeTextQuery, TLLibraryStatus includeStatus,
    		boolean latestVersionsOnly) throws RepositoryException;

    /**
     * Returns a list containing all versions of the given <code>RepositoryItem</code>.
     * 
     * @param item
     *            the repository item for which to retrieve the version history
     * @return List<RepositoryItem>
     * @throws RepositoryException
     *             thrown if the remote repository cannot be accessed
     */
    public List<RepositoryItem> getVersionHistory(RepositoryItem item) throws RepositoryException;

    /**
     * Returns the meta-data record for the repository item with the specified namespace, filename,
     * and version.
     * 
     * @param baseNamespace
     *            the base namespace of the repository item
     * @param filename
     *            the filename of the repository item
     * @param versionIdentifier
     *            the version identifier of the repository item
     * @return RepositoryItem
     * @throws RepositoryException
     *             thrown if the meta-data record does not exist or cannot be retrieved
     */
    public RepositoryItem getRepositoryItem(String baseNamespace, String filename,
            String versionIdentifier) throws RepositoryException;

    /**
     * Returns the meta-data record for a repository item using the URI provided. This method
     * requires that the URI contains the encoded namespace value for the repository item (normally
     * optional per the OTM repository URI specification).
     * 
     * @param itemUri
     *            the fully-qualified URI of the repository item (encoded namespace value is
     *            required)
     * @return RepositoryItem
     * @throws RepositoryException
     *             thrown if the meta-data record does not exist or cannot be retrieved
     * @throws URISyntaxException
     *             thrown if the given URI is not a valid OTM repository URI (including the
     *             namespace)
     */
    public RepositoryItem getRepositoryItem(String itemUri) throws RepositoryException,
            URISyntaxException;

    /**
     * Returns the meta-data record for a repository item using the URI and namespace provided. If
     * the item URI specifies a namespace value (optional per the OTM repository URI specification),
     * that namespace will be ignored in favor of the 'itemNamespace' value provided.
     * 
     * <p>
     * The format of the 'itemUri' is as follows:
     * 
     * <pre>
     * otm://&lt;repository-id&gt;[/&lt;item-namespace&gt;]/&lt;otm-filename&gt;[#&lt;version-scheme-id&gt;]
     * </pre>
     * 
     * <ul>
     * <li><u>repository-id</u> - the ID of the repository to which the item is published (required)
     * </li>
     * <li><u>item-namespace</u> - the repository-managed namespace to which the item is assigned
     * (optional); since this component is a URI, this value must be URL encoded to include it
     * within the repository URI path</li>
     * <li><u>otm-filename</u> - the filename assigned to the item in the OTM repository</li>
     * <li><u>version-scheme-id</u> - the version scheme assigned to the item; if the default value
     * of "OTA2" is assigned, this component of the URI path is optional</li>
     * </ul>
     * 
     * @param itemUri
     *            the fully-qualified URI of the repository item (encoded namespace value is
     *            required)
     * @param itemNamespace
     *            the assigned namespace of the OTM repository item
     * @return RepositoryItem
     * @throws RepositoryException
     *             thrown if the meta-data record does not exist or cannot be retrieved
     * @throws URISyntaxException
     *             thrown if the given URI is not a valid OTM repository URI
     */
    public RepositoryItem getRepositoryItem(String itemUri, String itemNamespace)
            throws RepositoryException, URISyntaxException;

    /**
     * Returns the permission that the registered user is authorized to perform on the specified
     * namespace.
     * 
     * <p>
     * The format of the 'itemUri' is as follows:
     * 
     * <pre>
     * otm://&lt;repository-id&gt;[/&lt;item-namespace&gt;]/&lt;otm-filename&gt;[#&lt;version-scheme-id&gt;]
     * </pre>
     * 
     * <ul>
     * <li><u>repository-id</u> - the ID of the repository to which the item is published (required)
     * </li>
     * <li><u>item-namespace</u> - the repository-managed namespace to which the item is assigned
     * (optional); since this component is a URI, this value must be URL encoded to include it
     * within the repository URI path</li>
     * <li><u>otm-filename</u> - the filename assigned to the item in the OTM repository</li>
     * <li><u>version-scheme-id</u> - the version scheme assigned to the item; if the default value
     * of "OTA2" is assigned, this component of the URI path is optional</li>
     * </ul>
     * 
     * @param baseNamespace
     *            the namespace for which permissions should be returned
     * @return RepositoryPermission
     * @throws RepositoryException
     *             thrown if the remote repository cannot be accessed
     */
    public RepositoryPermission getUserAuthorization(String baseNamespace)
            throws RepositoryException;
    
    /**
     * Returns the list of repository items that are locked by the current user of this
     * repository.  If access to this repository is based on anonymous credentials, this
     * method will always return an empty list.
     * 
     * <p>If this repository is the local <code>RepositoryManager</code>, this method will
     * only return locked items from the user's local repository.  Use the <code>RemoteRepository</code>
     * handles to retrieve locked items from remote repositories.
     * 
     * @return List<RepositoryItem>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<RepositoryItem> getLockedItems() throws RepositoryException;
    
    /**
     * Creates the given root namespace within the repository. If the namespace URI provided is
     * nested under an existing root namespace, an exception will be thrown.
     * 
     * @param rootNamespace
     *            the base namespace to be created
     * @throws RepositoryException
     *             thrown if the repository cannot be accessed or the namespace is is nested under
     *             an existing root namespace
     */
    public void createRootNamespace(String rootNamespace) throws RepositoryException;

    /**
     * Deletes the specified root namespace from the repository. This method will only succeed if
     * the root namespace is empty.
     * 
     * @param rootNamespace
     *            the root namespace to be deleted
     * @throws RepositoryException
     *             thrown if the repository cannot be accessed or the root namespace cannot be
     *             deleted
     */
    public void deleteRootNamespace(String rootNamespace) throws RepositoryException;

    /**
     * Creates the given base namespace within the repository. If the base namespace is not part of
     * one of the repository's root namespaces, an exception will be thrown.
     * 
     * @param baseNamespace
     *            the base namespace to be created
     * @throws RepositoryException
     *             thrown if the repository cannot be accessed or the namespace is not the child of
     *             an existing root namespace
     */
    public void createNamespace(String baseNamespace) throws RepositoryException;

    /**
     * Deletes the last component of the namespace's path from the repository. This method will only
     * succeed if all of the following conditions are met:
     * <ul>
     * <li>The namespace currently exists in the repository</li>
     * <li>The namespace does not have any child namespaces defined</li>
     * <li>The namespace does not have any OTM library or schema items defined</li>
     * </ul>
     * 
     * @param baseNamespace
     *            the base namespace to be deleted
     * @throws RepositoryException
     *             thrown if the repository cannot be accessed or the namespace cannot be deleted
     */
    public void deleteNamespace(String baseNamespace) throws RepositoryException;

    /**
     * Publishes a file for the first time to this repository. Once published, this repository will
     * be considered as the owner and manager of the file's content.
     * 
     * @param unmanagedContent
     *            input stream for the unmanaged content to publish
     * @param filename
     *            the filename of the unmanaged content to publish
     * @param libraryName
     *            the name of the unmanaged library to publish
     * @paramnamespace the full namespace (including version-specific path information) with which
     *                 the file content is to be associated
     * @param versionIdentifier
     *            the version identifier of the repository item
     * @param versionScheme
     *            the version scheme of the item to be published (may be null)
     * @param initialStatus
     *            the initial status of the item in the repository
     * @return RepositoryItem
     * @throws RepositoryException
     *             thrown if the publication of the file fails
     */
    public RepositoryItem publish(InputStream unmanagedContent, String filename,
            String libraryName, String namespace, String versionIdentifier, String versionScheme,
            TLLibraryStatus initialStatus) throws RepositoryException;

    /**
     * Commits the content of the specified <code>RepositoryItem</code> by updating its repository
     * contents to match the data obtained from the input stream for the work-in-process content.
     * 
     * @param item
     *            the repository item to commit
     * @throws RepositoryException
     *             thrown if the file content cannot be committed or is not yet locked by the
     *             current user
     */
    public void commit(RepositoryItem item) throws RepositoryException;

    /**
     * Locks the specified repository item using the credentials for its owning repository.
     * 
     * @param item
     *            the repository item to lock
     * @throws RepositoryException
     *             thrown if the file content cannot be locked by the current user
     */
    public void lock(RepositoryItem item) throws RepositoryException;

    /**
     * Locks the specified repository item using the credentials for its owning repository. If the
     * 'commitWIP' parameter is true, the work-in-process conent of the will be committed to the
     * remote repository before the existing lock is released. If the flag is false, any changes in
     * the item's WIP content will be discarded.
     * 
     * @param item
     *            the repository item to lock
     * @param commitWIP
     *            flag indicating that the current WIP content changes should be committed prior to
     *            releasing the lock
     * @throws RepositoryException
     *             thrown if the file content cannot be unlocked or is not yet locked by the current
     *             user
     */
    public void unlock(RepositoryItem item, boolean commitWIP) throws RepositoryException;

    /**
     * Promotes a <code>RepositoryItem</code> from <code>DRAFT</code> status to <code>FINAL</code>.
     * 
     * @param item
     *            the repository item to promote
     * @throws RepositoryException
     */
    public void promote(RepositoryItem item) throws RepositoryException;

    /**
     * Demotes a <code>RepositoryItem</code> from <code>FINAL</code> status to <code>DRAFT</code>.
     * This operation can only be performed if the local user has administrative permissions to
     * modify the requested item.
     * 
     * @param item
     *            the repository item to demote
     * @throws RepositoryException
     */
    public void demote(RepositoryItem item) throws RepositoryException;

    /**
     * Recalculates the CRC of a <code>RepositoryItem</code> that is in the <code>FINAL</code>
     * status.
     * 
     * @param item
     *            the repository item whose CRC is to be recalculated
     * @throws RepositoryException
     */
    public void recalculateCrc(RepositoryItem item) throws RepositoryException;

    /**
     * Deletes a <code>RepositoryItem</code> from the repository. This operation can only be
     * performed if the local user has administrative permissions to modify the requested item.
     * 
     * @param item
     *            the repository item to delete
     * @throws RepositoryException
     */
    public void delete(RepositoryItem item) throws RepositoryException;

}

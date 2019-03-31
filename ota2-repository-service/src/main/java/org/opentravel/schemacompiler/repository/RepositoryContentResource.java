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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.ResourceConfig;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.EntityInfoListType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.EntityInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryHistoryType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoListType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.ListItems2RQType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.ListItemsRQType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.NamespaceListType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryItemIdentityType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermissionType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.SearchResultsListType;
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.index.EntitySearchResult;
import org.opentravel.schemacompiler.index.FreeTextSearchService;
import org.opentravel.schemacompiler.index.FreeTextSearchServiceFactory;
import org.opentravel.schemacompiler.index.LibrarySearchResult;
import org.opentravel.schemacompiler.index.ReleaseSearchResult;
import org.opentravel.schemacompiler.index.SearchResult;
import org.opentravel.schemacompiler.lock.LockableResource;
import org.opentravel.schemacompiler.lock.RepositoryLockManager;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBElement;

/**
 * JAX-RS endpoint used for publishing and downloading content from the OTA2.0 repository.
 * 
 * @author S. Livezey
 */
@Path("/")
public class RepositoryContentResource {

    private static final String ADMIN_ACCESS_REQUIRED = " - administration access required.";
    private static final String USER_NOT_AUTHORIZED =
        "The user does not have permission to access the requested resource.";

    private static ObjectFactory objectFactory = new ObjectFactory();
    static Log log = LogFactory.getLog( RepositoryContentResource.class );

    private RepositoryMetadataResource repositoryMetadataResource;
    private RepositorySecurityManager securityManager;
    private RepositoryManager repositoryManager;
    @Context
    ResourceConfig rc;

    /**
     * Default constructor.
     * 
     * @throws RepositoryException thrown if the local repository manager cannot be initialized
     */
    public RepositoryContentResource() throws RepositoryException {
        RepositoryComponentFactory componentFactory = RepositoryComponentFactory.getDefault();

        this.repositoryManager = componentFactory.getRepositoryManager();
        this.repositoryMetadataResource = new RepositoryMetadataResource( componentFactory.getRepositoryLocation() );
        this.securityManager = componentFactory.getSecurityManager();

        try {
            FreeTextSearchServiceFactory.initializeSingleton( repositoryManager );

        } catch (IOException e) {
            throw new RepositoryException( "Error initializing the free-text search service.", e );
        }
    }

    /**
     * Returns the meta-data information for the OTA2.0 repository being hosted by this web service.
     * 
     * @return JAXBElement&lt;RepositoryInfoType&gt;
     */
    @GET
    @Path("repository-metadata")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<RepositoryInfoType> getRepositoryMetadata() {
        return objectFactory.createRepositoryInfo( repositoryMetadataResource.getResource() );
    }

    /**
     * Returns a list of all root namespaces for the libraries stored and managed in the remote repository. All items
     * that are published to a repository must be assigned to a root namespace or one of its child URI's.
     * 
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;NamespaceListType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @GET
    @Path("all-namespaces")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<NamespaceListType> listAllNamespaces(@HeaderParam("Authorization") String authorizationHeader)
        throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        RepositoryInfoType repositoryMetadata = repositoryMetadataResource.getResource();
        List<String> nsList =
            repositoryManager.getFileManager().findAllNamespaces( repositoryMetadata.getRootNamespace() );
        NamespaceListType namespaceList = new NamespaceListType();

        for (String ns : nsList) {
            if (!namespaceList.getNamespace().contains( ns )
                && securityManager.isAuthorized( user, ns, RepositoryPermission.READ_FINAL )) {
                namespaceList.getNamespace().add( ns );
            }
        }
        Collections.sort( namespaceList.getNamespace() );
        return objectFactory.createNamespaceList( namespaceList );
    }

    /**
     * Returns a list of all base namespaces for the libraries stored and managed in the remote repository. Base
     * namespaces do not include the trailing version component of the URI path.
     * 
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;NamespaceListType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @GET
    @Path("base-namespaces")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<NamespaceListType> listBaseNamespaces(@HeaderParam("Authorization") String authorizationHeader)
        throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        RepositoryInfoType repositoryMetadata = repositoryMetadataResource.getResource();
        List<String> nsList =
            repositoryManager.getFileManager().findAllBaseNamespaces( repositoryMetadata.getRootNamespace() );
        NamespaceListType namespaceList = new NamespaceListType();

        for (String ns : nsList) {
            if (!namespaceList.getNamespace().contains( ns )
                && securityManager.isAuthorized( user, ns, RepositoryPermission.READ_FINAL )) {
                namespaceList.getNamespace().add( ns );
            }
        }
        Collections.sort( namespaceList.getNamespace() );
        return objectFactory.createNamespaceList( namespaceList );
    }

    /**
     * Returns the list of namespace paths from the repository that exist immediately under the given namespace. Only
     * base (non-versioned) namespace URI paths are returned by this method.
     * 
     * @param baseNamespace the base namespace for which to return child paths
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;NamespaceListType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @GET
    @Path("namespace-children")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<NamespaceListType> listNamespaceChildren(@QueryParam("baseNamespace") String baseNamespace,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        List<String> childPaths = repositoryManager.listNamespaceChildren( baseNamespace );
        NamespaceListType namespaceList = new NamespaceListType();

        for (String childPath : childPaths) {
            String childNS = RepositoryNamespaceUtils.appendChildPath( baseNamespace, childPath );

            if (!namespaceList.getNamespace().contains( childNS )
                && securityManager.isAuthorized( user, childNS, RepositoryPermission.READ_FINAL )) {
                namespaceList.getNamespace().add( childPath );
            }
        }
        Collections.sort( namespaceList.getNamespace() );
        return objectFactory.createNamespaceList( namespaceList );
    }

    /**
     * Returns a list of the latest version of each <code>RepositoryItem</code>'s meta-data record in the repository.
     * 
     * @param listItemsRQ the request object that specifies which base namespace and items are to be returned
     * @param authorizationHeader the value of the HTTP "Authorization" header
     * @return JAXBElement&lt;LibraryInfoListType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("list-items")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoListType> listItemsForNamespace(JAXBElement<ListItemsRQType> listItemsRQ,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        Map<String,Map<TLLibraryStatus,Boolean>> accessibleItemCache = new HashMap<>();
        List<RepositoryItem> namespaceItems = repositoryManager.listItems( listItemsRQ.getValue().getNamespace(),
            listItemsRQ.getValue().isLatestVersionOnly(), listItemsRQ.getValue().isIncludeDraft() );
        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        LibraryInfoListType metadataList = new LibraryInfoListType();

        for (RepositoryItem item : namespaceItems) {
            if (isReadable( item, user, accessibleItemCache )) {
                metadataList.getLibraryInfo().add( RepositoryUtils.createItemMetadata( item ) );
            }
        }
        return objectFactory.createLibraryInfoList( metadataList );
    }

    /**
     * Returns a list of the latest version of each <code>RepositoryItem</code>'s meta-data record in the repository.
     * 
     * @param listItemsRQ the request object that specifies which base namespace and items are to be returned
     * @param itemTypeStr specifies the type of repository item to which the search should be restricted
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;LibraryInfoListType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("list-items2")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoListType> listItemsForNamespace2(JAXBElement<ListItems2RQType> listItemsRQ,
        @QueryParam("itemType") String itemTypeStr, @HeaderParam("Authorization") String authorizationHeader)
        throws RepositoryException {

        Map<String,Map<TLLibraryStatus,Boolean>> accessibleItemCache = new HashMap<>();
        RepositoryItemType itemType = getItemType( itemTypeStr );
        List<RepositoryItem> namespaceItems = repositoryManager.listItems( listItemsRQ.getValue().getNamespace(),
            getStatus( listItemsRQ.getValue().getIncludeStatus().toString() ),
            listItemsRQ.getValue().isLatestVersionOnly(), itemType );
        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        LibraryInfoListType metadataList = new LibraryInfoListType();

        for (RepositoryItem item : namespaceItems) {
            if (isReadable( item, user, accessibleItemCache )) {
                metadataList.getLibraryInfo().add( RepositoryUtils.createItemMetadata( item ) );
            }
        }
        return objectFactory.createLibraryInfoList( metadataList );
    }

    /**
     * Returns a list of the latest version of each <code>RepositoryItem</code>'s that match the free-text search
     * criteria provided.
     * 
     * @param freeTextQuery the string containing space-separated keywords for the free-text search
     * @param latestVersionsOnly flag indicating whether the results should include all matching versions or just the
     *        latest version of each library
     * @param includeDraftVersions flag indicating whether items in <code>DRAFT</code> status should be included in the
     *        resulting list
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;LibraryInfoListType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @GET
    @Path("search")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoListType> search(@QueryParam("query") String freeTextQuery,
        @QueryParam("latestVersion") boolean latestVersionsOnly,
        @QueryParam("includeDraft") boolean includeDraftVersions,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        Map<String,Map<TLLibraryStatus,Boolean>> accessibleItemCache = new HashMap<>();
        TLLibraryStatus searchStatus = includeDraftVersions ? null : TLLibraryStatus.FINAL;
        List<SearchResult<Object>> searchResults =
            FreeTextSearchServiceFactory.getInstance().search( freeTextQuery, searchStatus, latestVersionsOnly, false );
        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        LibraryInfoListType metadataList = new LibraryInfoListType();

        for (SearchResult<?> result : searchResults) {
            if (result instanceof LibrarySearchResult) {
                RepositoryItem item = ((LibrarySearchResult) result).getRepositoryItem();

                if (isReadable( item, user, accessibleItemCache )) {
                    metadataList.getLibraryInfo().add( RepositoryUtils.createItemMetadata( item ) );
                }
            }
        }
        return objectFactory.createLibraryInfoList( metadataList );
    }

    /**
     * Returns a list of the latest version of each <code>RepositoryItem</code>'s that match the free-text search
     * criteria provided.
     * 
     * @param freeTextQuery the string containing space-separated keywords for the free-text search
     * @param latestVersionsOnly flag indicating whether the results should include all matching versions or just the
     *        latest version of each library
     * @param includeStatusStr string representation of the library status that indicating the latest status to include
     *        in the results (null = all statuses)
     * @param itemTypeStr specifies the type of repository item to which the search should be restricted
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;LibraryInfoListType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @GET
    @Path("search2")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<SearchResultsListType> search(@QueryParam("query") String freeTextQuery,
        @QueryParam("latestVersion") boolean latestVersionsOnly, @QueryParam("includeStatus") String includeStatusStr,
        @QueryParam("itemType") String itemTypeStr, @HeaderParam("Authorization") String authorizationHeader)
        throws RepositoryException {

        Map<String,Map<TLLibraryStatus,Boolean>> accessibleItemCache = new HashMap<>();
        TLLibraryStatus searchStatus = getStatus( includeStatusStr );
        RepositoryItemType itemType = getItemType( itemTypeStr );
        List<SearchResult<Object>> searchResults =
            FreeTextSearchServiceFactory.getInstance().search( freeTextQuery, searchStatus, latestVersionsOnly, false );
        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        SearchResultsListType resultsList = new SearchResultsListType();
        Set<String> referencedLibraryIds = new HashSet<>();

        // First pass to build the list of referenced libraries
        for (SearchResult<?> result : searchResults) {
            if (result instanceof EntitySearchResult) {
                EntitySearchResult entityResult = (EntitySearchResult) result;

                if (entityResult.getOwningLibraryId() != null) {
                    referencedLibraryIds.add( entityResult.getOwningLibraryId() );
                }
            }
        }

        // Build a map of all referenced libraries
        List<LibrarySearchResult> referencedLibraries =
            FreeTextSearchServiceFactory.getInstance().getLibraries( referencedLibraryIds, false );
        Map<String,LibrarySearchResult> referencedLibrariesById = new HashMap<>();

        for (LibrarySearchResult referencedLib : referencedLibraries) {
            referencedLibrariesById.put( referencedLib.getSearchIndexId(), referencedLib );
        }

        // Second pass to build the search results
        for (SearchResult<?> result : searchResults) {
            if (result instanceof ReleaseSearchResult) {
                addReleaseSearchResult( (ReleaseSearchResult) result, resultsList, itemType, user,
                    accessibleItemCache );

            } else if (result instanceof LibrarySearchResult) {
                addLibrarySearchResult( (LibrarySearchResult) result, resultsList, itemType, accessibleItemCache,
                    user );

            } else if ((result instanceof EntitySearchResult)
                && ((itemType == null) || (itemType == RepositoryItemType.LIBRARY))) {
                addEntitySearchResult( (EntitySearchResult) result, resultsList, referencedLibrariesById, user,
                    accessibleItemCache );
            }
        }
        return objectFactory.createSearchResultsList( resultsList );
    }

    /**
     * Adds a release search result to the results list provided if the specified user has read access to the item.
     * 
     * @param releaseResult the release search result to be added
     * @param resultsList the results list to which the new result will be added
     * @param itemType the repository item type to which the search is restricted
     * @param user the user who initiated the search
     * @param accessibleItemCache cache of which namespaces the user has access to
     * @throws RepositoryException thrown if an error occurs while accessing the remote repository
     */
    private void addReleaseSearchResult(ReleaseSearchResult releaseResult, SearchResultsListType resultsList,
        RepositoryItemType itemType, UserPrincipal user, Map<String,Map<TLLibraryStatus,Boolean>> accessibleItemCache)
        throws RepositoryException {
        if ((itemType == null) || (itemType == RepositoryItemType.RELEASE)) {
            RepositoryItem item = repositoryManager.getRepositoryItem( releaseResult.getBaseNamespace(),
                releaseResult.getFilename(), releaseResult.getVersion() );

            if (isReadable( item, user, accessibleItemCache )) {
                resultsList.getSearchResult()
                    .add( objectFactory.createLibrarySearchResult( RepositoryUtils.createItemMetadata( item ) ) );
            }
        }
    }

    /**
     * Adds a library search result to the results list provided if the specified user has read access to the item.
     * 
     * @param libraryResult the library search result to be added
     * @param resultsList the results list to which the new result will be added
     * @param itemType the repository item type to which the search is restricted
     * @param user the user who initiated the search
     * @param accessibleItemCache cache of which namespaces the user has access to
     * @throws RepositorySecurityException thrown if an error occurs while determining the user's access
     */
    private void addLibrarySearchResult(LibrarySearchResult libraryResult, SearchResultsListType resultsList,
        RepositoryItemType itemType, Map<String,Map<TLLibraryStatus,Boolean>> accessibleItemCache, UserPrincipal user)
        throws RepositorySecurityException {
        if ((itemType == null) || (itemType == RepositoryItemType.LIBRARY)) {
            RepositoryItem item = libraryResult.getRepositoryItem();

            if (isReadable( item, user, accessibleItemCache )) {
                resultsList.getSearchResult()
                    .add( objectFactory.createLibrarySearchResult( RepositoryUtils.createItemMetadata( item ) ) );
            }
        }
    }

    /**
     * Adds an entity search result to the results list provided if the specified user has read access to the item.
     * 
     * @param entityResult the entity search result to be added
     * @param resultsList the results list to which the new result will be added
     * @param referencedLibrariesById libraries collated by search index ID
     * @param user the user who initiated the search
     * @param accessibleItemCache cache of which namespaces the user has access to
     * @throws RepositorySecurityException thrown if an error occurs while determining the user's access
     */
    private void addEntitySearchResult(EntitySearchResult entityResult, SearchResultsListType resultsList,
        Map<String,LibrarySearchResult> referencedLibrariesById, UserPrincipal user,
        Map<String,Map<TLLibraryStatus,Boolean>> accessibleItemCache) throws RepositorySecurityException {
        LibrarySearchResult referencedLib = referencedLibrariesById.get( entityResult.getOwningLibraryId() );

        if (referencedLib != null) {
            RepositoryItem item = referencedLib.getRepositoryItem();

            if (isReadable( item, user, accessibleItemCache )) {
                resultsList.getSearchResult()
                    .add( objectFactory.createLibrarySearchResult( createEntityMetadata( entityResult, item ) ) );
            }
        }
    }

    /**
     * Called by remote clients to retrieve the version history for an item in the OTA2.0 repository.
     * 
     * @param identityElement the XML element that identifies the repository item to be locked
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;LibraryInfoListType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("version-history")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoListType> getVersionHistory(JAXBElement<RepositoryItemIdentityType> identityElement,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        Map<String,Map<TLLibraryStatus,Boolean>> accessibleItemCache = new HashMap<>();
        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
            itemIdentity.getBaseNamespace(), itemIdentity.getFilename(), itemIdentity.getVersion() );
        RepositoryItemImpl requestItem = RepositoryUtils.createRepositoryItem( repositoryManager, itemMetadata );
        List<RepositoryItem> historyItems = repositoryManager.getVersionHistory( requestItem );
        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        LibraryInfoListType metadataList = new LibraryInfoListType();

        for (RepositoryItem item : historyItems) {
            if (isReadable( item, user, accessibleItemCache )) {
                metadataList.getLibraryInfo().add( RepositoryUtils.createItemMetadata( item ) );
            }
        }
        return objectFactory.createLibraryInfoList( metadataList );
    }

    /**
     * Returns the commit history for the specified repository item.
     * 
     * @param itemMetadata meta-data for the repository item for which to return the commit history
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;LibraryInfoListType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("history")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryHistoryType> getHistory(JAXBElement<LibraryInfoType> itemMetadata,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        Map<String,Map<TLLibraryStatus,Boolean>> accessibleItemCache = new HashMap<>();
        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        RepositoryItem item = RepositoryUtils.createRepositoryItem( repositoryManager, itemMetadata.getValue() );

        if (isReadable( item, user, accessibleItemCache )) {
            LibraryHistoryType libraryHistory = repositoryManager.getHistoryManager().getHistory( item );

            if (libraryHistory == null) {
                throw new RepositoryException( "Commit history not found for " + item.getFilename() );
            }
            return objectFactory.createLibraryHistory( libraryHistory );

        } else {
            throw new RepositorySecurityException( USER_NOT_AUTHORIZED );
        }
    }

    /**
     * Returns a list of meta-data records for the repository items which reference the item specified in the request
     * payload.
     * 
     * @param itemMetadata meta-data for the repository item for which to return where-used items
     * @param includeIndirectStr string representation of the flag indicating whether to include indirect where-used
     *        references
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;LibraryInfoListType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("item-where-used")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoListType> getItemWhereUsed(JAXBElement<LibraryInfoType> itemMetadata,
        @QueryParam("includeIndirect") String includeIndirectStr,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        Map<String,Map<TLLibraryStatus,Boolean>> accessibleItemCache = new HashMap<>();
        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
        RepositoryItem searchItem = RepositoryUtils.createRepositoryItem( repositoryManager, itemMetadata.getValue() );
        LibrarySearchResult library = searchService.getLibrary( searchItem, false );
        LibraryInfoListType metadataList = new LibraryInfoListType();

        if ((library != null) && isReadable( searchItem, user, accessibleItemCache )) {
            List<LibrarySearchResult> searchResults =
                searchService.getLibraryWhereUsed( library, Boolean.parseBoolean( includeIndirectStr ), false );

            for (LibrarySearchResult searchResult : searchResults) {
                RepositoryItem item = searchResult.getRepositoryItem();

                if (isReadable( item, user, accessibleItemCache )) {
                    metadataList.getLibraryInfo().add( RepositoryUtils.createItemMetadata( item ) );
                }
            }
        }
        return objectFactory.createLibraryInfoList( metadataList );
    }

    /**
     * Returns a list of meta-data records for the entities which reference the entity specified in the request payload.
     * 
     * @param entityMetadataElement meta-data for the entity for which to return where-used entities
     * @param includeIndirectStr string representation of the flag indicating whether to include indirect where-used
     *        references
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;EntityInfoListType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("entity-where-used")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<EntityInfoListType> getEntityWhereUsed(JAXBElement<EntityInfoType> entityMetadataElement,
        @QueryParam("includeIndirect") String includeIndirectStr,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        EntityInfoType entityMetadata = entityMetadataElement.getValue();
        Map<String,Map<TLLibraryStatus,Boolean>> accessibleItemCache = new HashMap<>();
        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
        RepositoryItem searchItem = RepositoryUtils.createRepositoryItem( repositoryManager, entityMetadata );
        LibrarySearchResult library = searchService.getLibrary( searchItem, false );
        EntityInfoListType metadataList = new EntityInfoListType();

        if ((library != null) && isReadable( searchItem, user, accessibleItemCache )) {
            EntitySearchResult entity =
                searchService.getEntity( library.getSearchIndexId(), entityMetadata.getEntityName(), false );
            List<EntitySearchResult> searchResults =
                searchService.getEntityWhereUsed( entity, Boolean.parseBoolean( includeIndirectStr ), false );
            Map<String,LibrarySearchResult> libraryCache = new HashMap<>();
            Map<String,Boolean> libraryAccessCache = new HashMap<>();

            for (EntitySearchResult searchResult : searchResults) {
                Boolean isReadable = libraryAccessCache.get( searchResult.getOwningLibraryId() );

                if (isReadable == null) {
                    LibrarySearchResult srLibrary =
                        searchService.getLibrary( searchResult.getOwningLibraryId(), false );

                    if (srLibrary != null) {
                        isReadable = isReadable( srLibrary.getRepositoryItem(), user, accessibleItemCache );
                        libraryCache.put( srLibrary.getSearchIndexId(), srLibrary );
                    }
                }

                if (isReadable) {
                    LibrarySearchResult srLibrary = libraryCache.get( searchResult.getOwningLibraryId() );
                    EntityInfoType srEntity = new EntityInfoType();

                    RepositoryUtils.populateMetadata( srLibrary.getRepositoryItem(), srEntity );
                    srEntity.setEntityName( searchResult.getItemName() );
                    srEntity.setEntityType( searchResult.getEntityType().getName() );
                    metadataList.getEntityInfo().add( srEntity );
                }
            }
        }
        return objectFactory.createEntityInfoList( metadataList );
    }

    /**
     * Returns a list of meta-data records for the entities which extend the entity specified in the request payload.
     * 
     * @param entityMetadataElement meta-data for the entity for which to return extended entities
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;EntityInfoListType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("entity-where-extended")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<EntityInfoListType> getEntityWhereExtended(JAXBElement<EntityInfoType> entityMetadataElement,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        EntityInfoType entityMetadata = entityMetadataElement.getValue();
        Map<String,Map<TLLibraryStatus,Boolean>> accessibleItemCache = new HashMap<>();
        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
        RepositoryItem searchItem = RepositoryUtils.createRepositoryItem( repositoryManager, entityMetadata );
        LibrarySearchResult library = searchService.getLibrary( searchItem, false );
        EntityInfoListType metadataList = new EntityInfoListType();

        if ((library != null) && isReadable( searchItem, user, accessibleItemCache )) {
            EntitySearchResult entity =
                searchService.getEntity( library.getSearchIndexId(), entityMetadata.getEntityName(), false );
            List<EntitySearchResult> searchResults = searchService.getExtendedByEntities( entity, false );
            Map<String,LibrarySearchResult> libraryCache = new HashMap<>();
            Map<String,Boolean> libraryAccessCache = new HashMap<>();

            for (EntitySearchResult searchResult : searchResults) {
                Boolean isReadable = libraryAccessCache.get( searchResult.getOwningLibraryId() );

                if (isReadable == null) {
                    LibrarySearchResult srLibrary =
                        searchService.getLibrary( searchResult.getOwningLibraryId(), false );

                    if (srLibrary != null) {
                        isReadable = isReadable( srLibrary.getRepositoryItem(), user, accessibleItemCache );
                        libraryCache.put( srLibrary.getSearchIndexId(), srLibrary );
                    }
                }

                if (isReadable) {
                    LibrarySearchResult srLibrary = libraryCache.get( searchResult.getOwningLibraryId() );
                    EntityInfoType srEntity = new EntityInfoType();

                    RepositoryUtils.populateMetadata( srLibrary.getRepositoryItem(), srEntity );
                    srEntity.setEntityName( searchResult.getItemName() );
                    srEntity.setEntityType( searchResult.getEntityType().getName() );
                    metadataList.getEntityInfo().add( srEntity );
                }
            }
        }
        return objectFactory.createEntityInfoList( metadataList );
    }

    /**
     * Creates the given root namespace within the repository. If the namespace URI provided is nested under an existing
     * root namespace, an exception will be thrown.
     * 
     * @param rootNamespace the root namespace to be created
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return Response
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @GET
    @Path("create-root-namespace")
    public Response createRootNamespace(@QueryParam("rootNamespace") String rootNamespace,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );

        if (securityManager.isAdministrator( user )) {
            repositoryManager.createRootNamespace( rootNamespace );
            return Response.status( Response.Status.OK ).build();

        } else {
            throw new RepositorySecurityException(
                "The user does not have permission to create the requested namespace." );
        }
    }

    /**
     * Deletes the specified root namespace from the repository. This method will only succeed if the root namespace is
     * empty.
     * 
     * @param rootNamespace the root namespace to be deleted
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return Response
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @GET
    @Path("delete-root-namespace")
    public Response deleteRootNamespace(@QueryParam("rootNamespace") String rootNamespace,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );

        if (securityManager.isAdministrator( user )) {
            repositoryManager.deleteRootNamespace( rootNamespace );
            return Response.status( Response.Status.OK ).build();

        } else {
            throw new RepositorySecurityException(
                "The user does not have permission to delete the requested namespace." );
        }
    }

    /**
     * Creates the given base namespace within the repository. If the base namespace is not part of one of the
     * repository's root namespaces, an exception will be thrown.
     * 
     * @param baseNamespace the base namespace to be created
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return Response
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @GET
    @Path("create-namespace")
    public Response createNamespace(@QueryParam("baseNamespace") String baseNamespace,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        String parentNS = RepositoryNamespaceUtils.getParentNamespace( baseNamespace, repositoryManager );

        if (securityManager.isAuthorized( user, parentNS, RepositoryPermission.WRITE )) {
            repositoryManager.createNamespace( baseNamespace );
            return Response.status( Response.Status.OK ).build();

        } else {
            throw new RepositorySecurityException(
                "The user does not have permission to create the requested namespace." );
        }
    }

    /**
     * Deletes the last component of the namespace's path from the repository. This method will only succeed if all of
     * the following conditions are met:
     * <ul>
     * <li>The namespace currently exists in the repository</li>
     * <li>The namespace does not have any child namespaces defined</li>
     * <li>The namespace does not have any OTM library or schema items defined</li>
     * </ul>
     * 
     * @param baseNamespace the base namespace to be deleted
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return Response
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @GET
    @Path("delete-namespace")
    public Response deleteNamespace(@QueryParam("baseNamespace") String baseNamespace,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        String parentNS = RepositoryNamespaceUtils.getParentNamespace( baseNamespace, repositoryManager );

        if (securityManager.isAuthorized( user, parentNS, RepositoryPermission.WRITE )) {
            repositoryManager.deleteNamespace( baseNamespace );
            return Response.status( Response.Status.OK ).build();

        } else {
            throw new RepositorySecurityException(
                "The user does not have permission to delete the requested namespace." );
        }
    }

    /**
     * Called by remote clients to publish new library content to the OTA2.0 repository.
     * 
     * @param contentStream provides access to the raw content of the file being published
     * @param contentDetail detailed information about the file (e.g. filename, etc.)
     * @param namespace the full namespace (with version-specific path information) to which the library content will be
     *        published
     * @param libraryName the name of the unmanaged library to publish
     * @param versionIdentifier the version identifier of the library being published
     * @param versionScheme the version scheme of the item to be published (may be null)
     * @param initialStatus the initial status (e.g. draft/final) of the library being published
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return Response
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("publish")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @SuppressWarnings("squid:S00107")
    public Response publishContent(@FormDataParam("fileContent") InputStream contentStream,
        @FormDataParam("fileContent") FormDataContentDisposition contentDetail,
        @FormDataParam("namespace") String namespace, @FormDataParam("libraryName") String libraryName,
        @FormDataParam("version") String versionIdentifier, @FormDataParam("versionScheme") String versionScheme,
        @FormDataParam("status") String initialStatus, @HeaderParam("Authorization") String authorizationHeader)
        throws RepositoryException {

        // Attempt to obtain a lock on the resource we are about to create. If we cannot calculate
        // the base namespace
        // for the item, proceed without the lock. In those cases, the attempt will most likely
        // result in an error anyway.
        LockableResource lockedResource = null;
        try {
            VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme( versionScheme );
            String baseNamespace = vScheme.getBaseNamespace( namespace );

            lockedResource =
                RepositoryLockManager.getInstance().acquireWriteLock( baseNamespace, contentDetail.getFileName() );

        } catch (VersionSchemeException e) {
            // Ignore and proceed (see inline comments above)
        }

        try {
            UserPrincipal user = securityManager.authenticateUser( authorizationHeader );

            if (securityManager.isAuthorized( user, namespace, RepositoryPermission.WRITE )) {
                repositoryManager.getFileManager().setCurrentUserId( user.getUserId() );
                RepositoryItem publishedItem =
                    repositoryManager.publish( contentStream, contentDetail.getFileName(), libraryName, namespace,
                        versionIdentifier, versionScheme, TLLibraryStatus.valueOf( initialStatus.toUpperCase() ) );
                Response response = Response.status( Response.Status.OK ).build();

                indexRepositoryItem( publishedItem );
                return response;

            } else {
                throw new RepositorySecurityException( "The user does not have permission to publish the item." );
            }

        } finally {
            if (lockedResource != null) {
                RepositoryLockManager.getInstance().releaseWriteLock( lockedResource );
            }
        }
    }

    /**
     * Called by remote clients to update existing library content in the OTA2.0 repository.
     * 
     * @param identityElement the XML element that identifies the repository item to be committed
     * @param contentStream provides access to the raw content of the file being committed
     * @param remarks the commit remarks for the updated library content
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return Response
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("commit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response commitContent(@FormDataParam("item") JAXBElement<RepositoryItemIdentityType> identityElement,
        @FormDataParam("fileContent") InputStream contentStream, @FormDataParam("remarks") String remarks,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance()
            .acquireWriteLock( itemIdentity.getBaseNamespace(), itemIdentity.getFilename() );
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename(), itemIdentity.getVersion() );
            RepositoryItemImpl item = RepositoryUtils.createRepositoryItem( repositoryManager, itemMetadata );
            UserPrincipal user = securityManager.authenticateUser( authorizationHeader );

            if (securityManager.isWriteAuthorized( user, item )) {
                repositoryManager.getFileManager().setCurrentUserId( user.getUserId() );

                repositoryManager.commit( item, contentStream, remarks );
                indexRepositoryItem( item );
                return Response.status( 200 ).build();

            } else {
                throw new RepositorySecurityException( USER_NOT_AUTHORIZED );
            }

        } finally {
            RepositoryLockManager.getInstance().releaseWriteLock( lockedResource );
        }
    }

    /**
     * Called by remote clients to obtain a lock for an item in the OTA2.0 repository.
     * 
     * @param identityElement the XML element that identifies the repository item to be locked
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;LibraryInfoType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("lock")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoType> lockRepositoryItem(JAXBElement<RepositoryItemIdentityType> identityElement,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance()
            .acquireWriteLock( itemIdentity.getBaseNamespace(), itemIdentity.getFilename() );
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename(), itemIdentity.getVersion() );
            RepositoryItemImpl item = RepositoryUtils.createRepositoryItem( repositoryManager, itemMetadata );
            UserPrincipal user = securityManager.authenticateUser( authorizationHeader );

            if (securityManager.isWriteAuthorized( user, item )) {
                repositoryManager.getFileManager().setCurrentUserId( user.getUserId() );

                // Obtain the lock in the local repository
                item.setLockedByUser( user.getUserId() );
                repositoryManager.lock( item );
                indexRepositoryItem( item );

                // Refresh the item's meta-data and return it to the caller
                itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata( itemIdentity.getBaseNamespace(),
                    itemIdentity.getFilename(), itemIdentity.getVersion() );
                return objectFactory.createLibraryInfo( itemMetadata );

            } else {
                throw new RepositorySecurityException( USER_NOT_AUTHORIZED );
            }

        } finally {
            RepositoryLockManager.getInstance().releaseWriteLock( lockedResource );
        }
    }

    /**
     * Called by remote clients to release an existing lock on an item in the OTA2.0 repository.
     * 
     * @param identityElement the XML element that identifies the repository item to be unlocked
     * @param contentStream provides access to the raw content of the file being unlocked (may be null)
     * @param remarks the commit remarks for the updated library content
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;LibraryInfoType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("unlock")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoType> unlockRepositoryItem(
        @FormDataParam("item") JAXBElement<RepositoryItemIdentityType> identityElement,
        @FormDataParam("fileContent") InputStream contentStream, @FormDataParam("remarks") String remarks,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance()
            .acquireWriteLock( itemIdentity.getBaseNamespace(), itemIdentity.getFilename() );
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename(), itemIdentity.getVersion() );
            RepositoryItemImpl item = RepositoryUtils.createRepositoryItem( repositoryManager, itemMetadata );
            UserPrincipal user = securityManager.authenticateUser( authorizationHeader );

            if (securityManager.isWriteAuthorized( user, item )) {
                repositoryManager.getFileManager().setCurrentUserId( user.getUserId() );

                // Release the lock in the local repository
                item.setLockedByUser( user.getUserId() );
                repositoryManager.unlock( item, contentStream, remarks );
                indexRepositoryItem( item );

                // Refresh the item's meta-data and return it to the caller
                itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata( itemIdentity.getBaseNamespace(),
                    itemIdentity.getFilename(), itemIdentity.getVersion() );
                return objectFactory.createLibraryInfo( itemMetadata );

            } else {
                throw new RepositorySecurityException( USER_NOT_AUTHORIZED );
            }

        } finally {
            RepositoryLockManager.getInstance().releaseWriteLock( lockedResource );
        }
    }

    /**
     * Called by remote clients to promote an item in the OTA2.0 repository.
     * 
     * @param identityElement the XML element that identifies the repository item to be promoted
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;LibraryInfoType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("promote")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoType> promoteRepositoryItem(JAXBElement<RepositoryItemIdentityType> identityElement,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance()
            .acquireWriteLock( itemIdentity.getBaseNamespace(), itemIdentity.getFilename() );
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename(), itemIdentity.getVersion() );
            RepositoryItemImpl item = RepositoryUtils.createRepositoryItem( repositoryManager, itemMetadata );
            UserPrincipal user = securityManager.authenticateUser( authorizationHeader );

            if (securityManager.isPromoteAuthorized( user, item )) {
                repositoryManager.getFileManager().setCurrentUserId( user.getUserId() );

                // Promote the item in the local repository
                repositoryManager.promote( item );
                indexRepositoryItem( item );

                // Refresh the item's meta-data and return it to the caller
                itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata( itemIdentity.getBaseNamespace(),
                    itemIdentity.getFilename(), itemIdentity.getVersion() );
                return objectFactory.createLibraryInfo( itemMetadata );

            } else {
                throw new RepositorySecurityException( USER_NOT_AUTHORIZED );
            }

        } finally {
            RepositoryLockManager.getInstance().releaseWriteLock( lockedResource );
        }
    }

    /**
     * Called by remote clients to demote an item in the OTA2.0 repository.
     * 
     * @param identityElement the XML element that identifies the repository item to be demoted
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;LibraryInfoType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("demote")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoType> demoteRepositoryItem(JAXBElement<RepositoryItemIdentityType> identityElement,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance()
            .acquireWriteLock( itemIdentity.getBaseNamespace(), itemIdentity.getFilename() );
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename(), itemIdentity.getVersion() );
            UserPrincipal user = securityManager.authenticateUser( authorizationHeader );

            if (securityManager.isAdministrator( user )) {
                repositoryManager.getFileManager().setCurrentUserId( user.getUserId() );
                RepositoryItemImpl item = RepositoryUtils.createRepositoryItem( repositoryManager, itemMetadata );

                // Promote the item in the local repository
                repositoryManager.demote( item );
                indexRepositoryItem( item );

                // Refresh the item's meta-data and return it to the caller
                itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata( itemIdentity.getBaseNamespace(),
                    itemIdentity.getFilename(), itemIdentity.getVersion() );
                return objectFactory.createLibraryInfo( itemMetadata );

            } else {
                throw new RepositorySecurityException(
                    "The user does not have permission to demote an item in the repository" + ADMIN_ACCESS_REQUIRED );
            }

        } finally {
            RepositoryLockManager.getInstance().releaseWriteLock( lockedResource );
        }
    }

    /**
     * Called by remote clients to update the status of an item in the OTA2.0 repository.
     * 
     * @param identityElement the XML element that identifies the repository item to be demoted
     * @param newStatusStr the string representation of the new status to assign
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;LibraryInfoType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("update-status")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoType> updateRepositoryItemStatus(
        JAXBElement<RepositoryItemIdentityType> identityElement, @QueryParam("newStatus") String newStatusStr,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance()
            .acquireWriteLock( itemIdentity.getBaseNamespace(), itemIdentity.getFilename() );
        TLLibraryStatus newStatus = getStatus( newStatusStr );
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename(), itemIdentity.getVersion() );
            TLLibraryStatus currentStatus = TLLibraryStatus.fromRepositoryStatus( itemMetadata.getStatus() );
            UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
            boolean hasAccess =
                (currentStatus.getRank() <= newStatus.getRank()) || securityManager.isAdministrator( user );

            if (hasAccess) {
                repositoryManager.getFileManager().setCurrentUserId( user.getUserId() );
                RepositoryItemImpl item = RepositoryUtils.createRepositoryItem( repositoryManager, itemMetadata );

                // Update the item's status in the local repository
                repositoryManager.updateStatus( item, newStatus );
                indexRepositoryItem( item );

                // Refresh the item's meta-data and return it to the caller
                itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata( itemIdentity.getBaseNamespace(),
                    itemIdentity.getFilename(), itemIdentity.getVersion() );
                return objectFactory.createLibraryInfo( itemMetadata );

            } else {
                throw new RepositorySecurityException(
                    "The user does not have permission to demote an item in the repository" + ADMIN_ACCESS_REQUIRED );
            }

        } finally {
            RepositoryLockManager.getInstance().releaseWriteLock( lockedResource );
        }
    }

    /**
     * Called by remote clients to recalculate the CRC value for an item in the OTA2.0 repository.
     * 
     * @param identityElement the XML element that identifies the repository item whose CRC is to be recalculated
     * @param authorizationHeader the value of the HTTP "Authorization" header
     * @return JAXBElement&lt;LibraryInfoType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("recalculate-crc")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoType> recalculateRepositoryItemCrc(
        JAXBElement<RepositoryItemIdentityType> identityElement,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance()
            .acquireWriteLock( itemIdentity.getBaseNamespace(), itemIdentity.getFilename() );
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename(), itemIdentity.getVersion() );
            UserPrincipal user = securityManager.authenticateUser( authorizationHeader );

            if (securityManager.isAdministrator( user )) {
                repositoryManager.getFileManager().setCurrentUserId( user.getUserId() );
                RepositoryItemImpl item = RepositoryUtils.createRepositoryItem( repositoryManager, itemMetadata );

                // Recalculate the item's CRC in the local repository
                repositoryManager.recalculateCrc( item );
                indexRepositoryItem( item );

                // Refresh the item's meta-data and return it to the caller
                itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata( itemIdentity.getBaseNamespace(),
                    itemIdentity.getFilename(), itemIdentity.getVersion() );
                return objectFactory.createLibraryInfo( itemMetadata );

            } else {
                throw new RepositorySecurityException(
                    "The user does not have permission to recalculate the CRC of an item in the repository"
                        + ADMIN_ACCESS_REQUIRED );
            }

        } finally {
            RepositoryLockManager.getInstance().releaseWriteLock( lockedResource );
        }
    }

    /**
     * Called by remote clients to delete an item from the OTA2.0 repository.
     * 
     * @param identityElement the XML element that identifies the repository item to be deleted
     * @param authorizationHeader the value of the HTTP "Authorization" header
     * @return Response
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("delete")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public Response deleteRepositoryItem(JAXBElement<RepositoryItemIdentityType> identityElement,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance()
            .acquireWriteLock( itemIdentity.getBaseNamespace(), itemIdentity.getFilename() );
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename(), itemIdentity.getVersion() );
            UserPrincipal user = securityManager.authenticateUser( authorizationHeader );

            if (securityManager.isAdministrator( user )) {
                repositoryManager.getFileManager().setCurrentUserId( user.getUserId() );
                RepositoryItemImpl item = RepositoryUtils.createRepositoryItem( repositoryManager, itemMetadata );

                if (isReleaseMember( item )) {
                    throw new RepositoryException(
                        "The library cannot be deleted because it is part of a managed release." );
                }

                // Delete the item in the local repository
                repositoryManager.delete( item );

                // Delete the item from the free-text search index
                indexRepositoryItem( item, true );

                return Response.status( Response.Status.OK ).build();

            } else {
                throw new RepositorySecurityException( USER_NOT_AUTHORIZED );
            }

        } finally {
            RepositoryLockManager.getInstance().releaseWriteLock( lockedResource );
        }
    }

    /**
     * Returns the meta-data information for a single item in the OTA2.0 repository.
     * 
     * @param identityElement the XML element that identifies the repository item to download
     * @param authorizationHeader the value of the HTTP "Authorization" header
     * @return JAXBElement&lt;RepositoryInfoType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("metadata")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoType> getRepositoryItemMetadata(
        JAXBElement<RepositoryItemIdentityType> identityElement,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance()
            .acquireReadLock( itemIdentity.getBaseNamespace(), itemIdentity.getFilename() );
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename(), itemIdentity.getVersion() );
            RepositoryItemImpl item = RepositoryUtils.createRepositoryItem( repositoryManager, itemMetadata );
            UserPrincipal user = securityManager.authenticateUser( authorizationHeader );

            if (securityManager.isReadAuthorized( user, item )) {
                return objectFactory.createLibraryInfo( itemMetadata );

            } else {
                throw new RepositorySecurityException( USER_NOT_AUTHORIZED );
            }

        } finally {
            RepositoryLockManager.getInstance().releaseReadLock( lockedResource );
        }
    }

    /**
     * Called by remote clients to download library/schema content from the OTA2.0 repository.
     * 
     * @param identityElement the XML element that identifies the repository item to download
     * @param authorizationHeader the value of the HTTP "Authorization" header
     * @return Response
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @POST
    @Path("content")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public Response downloadContent(JAXBElement<RepositoryItemIdentityType> identityElement,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance()
            .acquireReadLock( itemIdentity.getBaseNamespace(), itemIdentity.getFilename() );
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename(), itemIdentity.getVersion() );
            RepositoryItemImpl item = RepositoryUtils.createRepositoryItem( repositoryManager, itemMetadata );
            UserPrincipal user = securityManager.authenticateUser( authorizationHeader );

            if (securityManager.isReadAuthorized( user, item )) {
                File contentFile = repositoryManager.getFileManager().getLibraryContentLocation(
                    itemIdentity.getBaseNamespace(), itemIdentity.getFilename(), itemIdentity.getVersion() );
                ResponseBuilder response = Response.ok( contentFile );

                response.header( "Content-Disposition", "attachment; filename=" + contentFile.getName() );
                return response.build();

            } else {
                throw new RepositorySecurityException( USER_NOT_AUTHORIZED );
            }

        } finally {
            RepositoryLockManager.getInstance().releaseReadLock( lockedResource );
        }
    }

    /**
     * Called by remote clients to download historical library/schema content from the OTA2.0 repository.
     * 
     * @param baseNS the base namespace of the repository item to download
     * @param filename the filename of the repository item to download
     * @param version the version of the repository item to download
     * @param commitNumber the commit number of the repository item to download (may be null)
     * @param authorizationHeader the value of the HTTP "Authorization" header
     * @return Response
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @GET
    @Path("historical-content")
    public Response downloadHistoricalContent(@QueryParam("basens") String baseNS,
        @QueryParam("filename") String filename, @QueryParam("version") String version,
        @QueryParam("commit") Integer commitNumber, @HeaderParam("Authorization") String authorizationHeader)
        throws RepositoryException {

        LibraryInfoType itemMetadata =
            repositoryManager.getFileManager().loadLibraryMetadata( baseNS, filename, version );
        RepositoryItemImpl item = RepositoryUtils.createRepositoryItem( repositoryManager, itemMetadata );
        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );

        if (securityManager.isReadAuthorized( user, item )) {
            ResponseBuilder response;
            File contentFile = null;

            if (commitNumber != null) {
                contentFile = repositoryManager.getHistoryManager().getHistoricalContent( item, commitNumber );
            }
            if (contentFile == null) {
                contentFile = repositoryManager.getFileManager().getLibraryContentLocation( baseNS, filename, version );
            }

            response = Response.ok( contentFile );
            response.header( "Content-Disposition", "attachment; filename=" + item.getFilename() );
            return response.build();

        } else {
            throw new RepositorySecurityException( USER_NOT_AUTHORIZED );
        }
    }

    /**
     * Returns the permission that the registered user is authorized to perform on the specified namespace.
     * 
     * @param baseNamespace the namespace for which permissions should be returned
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;RepositoryPermissionType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @GET
    @Path("user-authorization")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<RepositoryPermissionType> getUserAuthorization(
        @DefaultValue("") @QueryParam("baseNamespace") String baseNamespace,
        @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        RepositoryPermission securityAuthorization =
            (user == null) ? null : securityManager.getAuthorization( user, baseNamespace );
        org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission repositoryAuthorization = null;
        RepositoryPermissionType result = new RepositoryPermissionType();

        if (securityAuthorization != null) {
            repositoryAuthorization = org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission
                .fromValue( securityAuthorization.value() );
        } else {
            repositoryAuthorization = org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission.NONE;
        }
        result.setRepositoryPermission( repositoryAuthorization );
        return objectFactory.createRepositoryPermission( result );
    }

    /**
     * Returns the list of items locked by the calling user. If the request is anonymous, this method will always return
     * an empty list.
     * 
     * @param authorizationHeader the header value that contains the encoded Basic Auth credentials
     * @return JAXBElement&lt;LibraryInfoListType&gt;
     * @throws RepositoryException thrown if the request cannot be processed
     */
    @GET
    @Path("locked-items")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoListType> getLockedItems(@HeaderParam("Authorization") String authorizationHeader)
        throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser( authorizationHeader );
        LibraryInfoListType lockedItems = new LibraryInfoListType();

        if (user != UserPrincipal.ANONYMOUS_USER) {
            Map<String,Map<TLLibraryStatus,Boolean>> accessibleItemCache = new HashMap<>();
            List<LibrarySearchResult> lockedLibraries =
                FreeTextSearchServiceFactory.getInstance().getLockedLibraries( user.getUserId(), false );

            for (LibrarySearchResult searchItem : lockedLibraries) {
                RepositoryItem item = searchItem.getRepositoryItem();

                // The user has this item locked, but we will still check for read permission
                // just in case
                if (isReadable( item, user, accessibleItemCache )) {
                    lockedItems.getLibraryInfo().add( RepositoryUtils.createItemMetadata( item ) );
                }
            }
        }
        return objectFactory.createLibraryInfoList( lockedItems );
    }

    /**
     * Returns true if the specified user should be allowed read access to the given repository item
     * 
     * @param item the repository item to be checked
     * @param user the user who is requesting access to the item
     * @param accessibleItemCache cache of permissions that have already been checked during this request/response cycle
     * @return boolean
     * @throws RepositorySecurityException thrown if the user's authorizations cannot be resolved
     */
    protected boolean isReadable(RepositoryItem item, UserPrincipal user,
        Map<String,Map<TLLibraryStatus,Boolean>> accessibleItemCache) throws RepositorySecurityException {
        Map<TLLibraryStatus,Boolean> cacheRecord = accessibleItemCache.get( item.getBaseNamespace() );

        if (cacheRecord == null) {
            cacheRecord = new EnumMap<>( TLLibraryStatus.class );
            accessibleItemCache.put( item.getBaseNamespace(), cacheRecord );
        }
        Boolean hasAccess = cacheRecord.get( item.getStatus() );

        if (hasAccess == null) {
            hasAccess = securityManager.isReadAuthorized( user, item );
            cacheRecord.put( item.getStatus(), hasAccess );
        }
        return hasAccess;
    }

    /**
     * Returns true if the given item represents an OTM library that is part of a managed release.
     * 
     * @param item the repository item to check
     * @return boolean
     * @throws RepositoryException thrown if the repository search service cannot be accessed
     */
    protected boolean isReleaseMember(RepositoryItem item) throws RepositoryException {
        FreeTextSearchService service = FreeTextSearchServiceFactory.getInstance();
        LibrarySearchResult library = service.getLibrary( item, false );
        boolean result = false;

        if (library != null) {
            List<ReleaseSearchResult> releaseList = service.getLibraryReleases( library, false );
            result = !releaseList.isEmpty();
        }
        return result;
    }

    /**
     * Returns the status enumeration from the given string value, or null if the string is null or empty.
     * 
     * @param statusStr the status string for which to return the enumeration value
     * @return TLLibraryStatus
     * @throws RepositoryException thrown if the status string value is not valid
     */
    private TLLibraryStatus getStatus(String statusStr) throws RepositoryException {
        try {
            if (statusStr != null) {
                return TLLibraryStatus.valueOf( statusStr );

            } else {
                throw new RepositoryException( "Library status cannot be null." );
            }

        } catch (IllegalArgumentException e) {
            throw new RepositoryException( "Unknown library status: " + statusStr );
        }
    }

    /**
     * Returns the item type enumeration from the given string value, or null if the string is null or empty.
     * 
     * @param itemTypeStr the item type string for which to return the enumeration value
     * @return RepositoryItemType
     * @throws RepositoryException thrown if the item type string value is not valid
     */
    private RepositoryItemType getItemType(String itemTypeStr) throws RepositoryException {
        try {
            return ((itemTypeStr == null) || (itemTypeStr.length() == 0)) ? null
                : RepositoryItemType.valueOf( itemTypeStr );

        } catch (IllegalArgumentException e) {
            throw new RepositoryException( "Unknown repository item type: " + itemTypeStr );
        }
    }

    /**
     * Creates a new meta-data record using information from the given entity search result.
     * 
     * @param searchResult the search result instance for which to create a meta-data record
     * @param item the repository item associated with the entity's owning library
     * @return EntityInfoType
     */
    public static EntityInfoType createEntityMetadata(EntitySearchResult searchResult, RepositoryItem item) {
        EntityInfoType entityMetadata = new EntityInfoType();

        entityMetadata.setEntityName( searchResult.getItemName() );
        entityMetadata.setEntityType( searchResult.getEntityType().getName() );
        RepositoryUtils.populateMetadata( item, entityMetadata );
        return entityMetadata;
    }

    /**
     * Submits the given repository item for indexing by the <code>FreeTextSearchService</code>.
     * 
     * @param item the repository item to be indexed
     */
    private void indexRepositoryItem(RepositoryItem item) {
        indexRepositoryItem( item, false );
    }

    /**
     * Submits the given repository item for indexing by the <code>FreeTextSearchService</code>.
     * 
     * @param item the repository item to be indexed
     * @param deleteIndex flag indicating whether the item's search index is to be deleted
     */
    private void indexRepositoryItem(RepositoryItem item, boolean deleteIndex) {
        FreeTextSearchService service = FreeTextSearchServiceFactory.getInstance();

        if ((service != null) && (item != null)) {
            try {
                if (!deleteIndex) {
                    service.indexRepositoryItem( item );

                } else {
                    service.deleteRepositoryItemIndex( item );
                }

            } catch (Exception e) {
                log.warn( "Error submitting repository item for indexing: " + item.getFilename() + " ["
                    + item.getNamespace() + "]", e );
            }
        }
    }

}

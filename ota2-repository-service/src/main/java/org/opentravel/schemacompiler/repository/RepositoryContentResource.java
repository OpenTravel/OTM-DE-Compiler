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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.ResourceConfig;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.EntityInfoListType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.EntityInfoType;
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
import org.opentravel.schemacompiler.index.LibrarySearchResult;
import org.opentravel.schemacompiler.index.SearchResult;
import org.opentravel.schemacompiler.lock.LockableResource;
import org.opentravel.schemacompiler.lock.RepositoryLockManager;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.opentravel.schemacompiler.security.RepositorySecurityException;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * JAX-RS endpoint used for publishing and downloading content from the OTA2.0 repository.
 * 
 * @author S. Livezey
 */
@Path("/")
public class RepositoryContentResource {

    private static ObjectFactory objectFactory = new ObjectFactory();
    static Log log = LogFactory.getLog(RepositoryContentResource.class);

    private RepositoryMetadataResource repositoryMetadataResource;
    private RepositorySecurityManager securityManager;
    private RepositoryManager repositoryManager;
    @Context
    ResourceConfig rc;

    /**
     * Default constructor.
     * 
     * @throws RepositoryException
     *             thrown if the local repository manager cannot be initialized
     */
    public RepositoryContentResource() throws RepositoryException {
        RepositoryComponentFactory componentFactory = RepositoryComponentFactory.getDefault();

        this.repositoryManager = componentFactory.getRepositoryManager();
        this.repositoryMetadataResource = new RepositoryMetadataResource(
                componentFactory.getRepositoryLocation());
        this.securityManager = componentFactory.getSecurityManager();

        try {
            FreeTextSearchService.initializeSingleton(componentFactory.getSearchIndexLocation(),
                    repositoryManager);

        } catch (IOException e) {
            throw new RepositoryException("Error initializing the free-text search service.", e);
        }
    }

    /**
     * Returns the meta-data information for the OTA2.0 repository being hosted by this web service.
     * 
     * @return JAXBElement<RepositoryInfoType>
     */
    @GET
    @Path("repository-metadata")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<RepositoryInfoType> getRepositoryMetadata() throws RepositoryException {
        return objectFactory.createRepositoryInfo(repositoryMetadataResource.getResource());
    }

    /**
     * Returns a list of all root namespaces for the libraries stored and managed in the remote
     * repository. All items that are published to a repository must be assigned to a root namespace
     * or one of its child URI's.
     * 
     * @return JAXBElement<NamespaceListType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @GET
    @Path("all-namespaces")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<NamespaceListType> listAllNamespaces(
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);
        RepositoryInfoType repositoryMetadata = repositoryMetadataResource.getResource();
        List<String> nsList = repositoryManager.getFileManager().findAllNamespaces(
                repositoryMetadata.getRootNamespace());
        NamespaceListType namespaceList = new NamespaceListType();

        for (String ns : nsList) {
            if (!namespaceList.getNamespace().contains(ns)
                    && securityManager.isAuthorized(user, ns, RepositoryPermission.READ_FINAL)) {
                namespaceList.getNamespace().add(ns);
            }
        }
        Collections.sort(namespaceList.getNamespace());
        return objectFactory.createNamespaceList(namespaceList);
    }

    /**
     * Returns a list of all base namespaces for the libraries stored and managed in the remote
     * repository. Base namespaces do not include the trailing version component of the URI path.
     * 
     * @return JAXBElement<NamespaceListType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @GET
    @Path("base-namespaces")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<NamespaceListType> listBaseNamespaces(
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);
        RepositoryInfoType repositoryMetadata = repositoryMetadataResource.getResource();
        List<String> nsList = repositoryManager.getFileManager().findAllBaseNamespaces(
                repositoryMetadata.getRootNamespace());
        NamespaceListType namespaceList = new NamespaceListType();

        for (String ns : nsList) {
            if (!namespaceList.getNamespace().contains(ns)
                    && securityManager.isAuthorized(user, ns, RepositoryPermission.READ_FINAL)) {
                namespaceList.getNamespace().add(ns);
            }
        }
        Collections.sort(namespaceList.getNamespace());
        return objectFactory.createNamespaceList(namespaceList);
    }

    /**
     * Returns the list of namespace paths from the repository that exist immediately under the
     * given namespace. Only base (non-versioned) namespace URI paths are returned by this method.
     * 
     * @param baseNamespace
     *            the base namespace for which to return child paths
     * @return JAXBElement<NamespaceListType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @GET
    @Path("namespace-children")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<NamespaceListType> listNamespaceChildren(
            @QueryParam("baseNamespace") String baseNamespace,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);
        List<String> childPaths = repositoryManager.listNamespaceChildren(baseNamespace);
        NamespaceListType namespaceList = new NamespaceListType();

        for (String childPath : childPaths) {
            String childNS = RepositoryNamespaceUtils.appendChildPath(baseNamespace, childPath);

            if (!namespaceList.getNamespace().contains(childNS)
                    && securityManager.isAuthorized(user, childNS, RepositoryPermission.READ_FINAL)) {
                namespaceList.getNamespace().add(childPath);
            }
        }
        Collections.sort(namespaceList.getNamespace());
        return objectFactory.createNamespaceList(namespaceList);
    }

    /**
     * Returns a list of the latest version of each <code>RepositoryItem</code>'s meta-data record
     * in the repository.
     * 
     * @param listItemsRQ
     *            the request object that specifies which base namespace and items are to be
     *            returned
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return JAXBElement<LibraryInfoListType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("list-items")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoListType> listItemsForNamespace(
            JAXBElement<ListItemsRQType> listItemsRQ,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        Map<String, Map<TLLibraryStatus, Boolean>> accessibleItemCache = new HashMap<String, Map<TLLibraryStatus, Boolean>>();
        List<RepositoryItem> namespaceItems = repositoryManager.listItems(listItemsRQ.getValue()
                .getNamespace(), listItemsRQ.getValue().isLatestVersionOnly(), listItemsRQ
                .getValue().isIncludeDraft());
        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);
        LibraryInfoListType metadataList = new LibraryInfoListType();

        for (RepositoryItem item : namespaceItems) {
            if (isReadable(item, user, accessibleItemCache)) {
                metadataList.getLibraryInfo().add(RepositoryUtils.createItemMetadata(item));
            }
        }
        return objectFactory.createLibraryInfoList(metadataList);
    }

    /**
     * Returns a list of the latest version of each <code>RepositoryItem</code>'s meta-data record
     * in the repository.
     * 
     * @param listItemsRQ
     *            the request object that specifies which base namespace and items are to be
     *            returned
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return JAXBElement<LibraryInfoListType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("list-items2")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    // TODO: Test the updated 'listItems()' API operation
    public JAXBElement<LibraryInfoListType> listItemsForNamespace2(
            JAXBElement<ListItems2RQType> listItemsRQ,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        Map<String, Map<TLLibraryStatus, Boolean>> accessibleItemCache = new HashMap<String, Map<TLLibraryStatus, Boolean>>();
        List<RepositoryItem> namespaceItems = repositoryManager.listItems(
        		listItemsRQ.getValue().getNamespace(), getStatus( listItemsRQ.getValue().getIncludeStatus().toString() ),
        		listItemsRQ.getValue().isLatestVersionOnly());
        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);
        LibraryInfoListType metadataList = new LibraryInfoListType();

        for (RepositoryItem item : namespaceItems) {
            if (isReadable(item, user, accessibleItemCache)) {
                metadataList.getLibraryInfo().add(RepositoryUtils.createItemMetadata(item));
            }
        }
        return objectFactory.createLibraryInfoList(metadataList);
    }

    /**
     * Returns a list of the latest version of each <code>RepositoryItem</code>'s that match the
     * free-text search criteria provided.
     * 
     * @param freeTextQuery
     *            the string containing space-separated keywords for the free-text search
     * @param latestVersionsOnly
     *            flag indicating whether the results should include all matching versions or just
     *            the latest version of each library
     * @param includeDraftVersions
     *            flag indicating whether items in <code>DRAFT</code> status should be included in
     *            the resulting list
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return JAXBElement<LibraryInfoListType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @GET
    @Path("search")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoListType> search(@QueryParam("query") String freeTextQuery,
            @QueryParam("latestVersion") boolean latestVersionsOnly,
            @QueryParam("includeDraft") boolean includeDraftVersions,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        Map<String, Map<TLLibraryStatus, Boolean>> accessibleItemCache = new HashMap<String, Map<TLLibraryStatus, Boolean>>();
        TLLibraryStatus searchStatus = includeDraftVersions ? null : TLLibraryStatus.FINAL;
        List<SearchResult<?>> searchResults = FreeTextSearchService.getInstance().search(
                freeTextQuery, searchStatus, latestVersionsOnly, false );
        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);
        LibraryInfoListType metadataList = new LibraryInfoListType();

        for (SearchResult<?> result : searchResults) {
        	if (result instanceof LibrarySearchResult) {
        		RepositoryItem item = ((LibrarySearchResult) result).getRepositoryItem();
        		
                if (isReadable(item, user, accessibleItemCache)) {
                    metadataList.getLibraryInfo().add(RepositoryUtils.createItemMetadata(item));
                }
        	}
        }
        return objectFactory.createLibraryInfoList(metadataList);
    }

    /**
     * Returns a list of the latest version of each <code>RepositoryItem</code>'s that match the
     * free-text search criteria provided.
     * 
     * @param freeTextQuery
     *            the string containing space-separated keywords for the free-text search
     * @param latestVersionsOnly
     *            flag indicating whether the results should include all matching versions or just
     *            the latest version of each library
     * @param includeStatusStr
     *            string representation of the library status that indicating the latest status to
     *            include in the results (null = all statuses)
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return JAXBElement<LibraryInfoListType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @GET
    @Path("search2")
    @Produces(MediaType.TEXT_XML)
    // TODO: Test the updated 'search()' API operation
    public JAXBElement<SearchResultsListType> search(@QueryParam("query") String freeTextQuery,
            @QueryParam("latestVersion") boolean latestVersionsOnly,
            @QueryParam("includeStatus") String includeStatusStr,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        Map<String, Map<TLLibraryStatus, Boolean>> accessibleItemCache = new HashMap<String, Map<TLLibraryStatus, Boolean>>();
        TLLibraryStatus searchStatus = getStatus( includeStatusStr );
        List<SearchResult<?>> searchResults = FreeTextSearchService.getInstance().search(
                freeTextQuery, searchStatus, latestVersionsOnly, false );
        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);
        SearchResultsListType resultsList = new SearchResultsListType();
        Set<String> referencedLibraryIds = new HashSet<>();
        
        // First pass to build the list of referenced libraries
        for (SearchResult<?> result : searchResults) {
        	if (result instanceof EntitySearchResult) {
        		referencedLibraryIds.add( ((EntitySearchResult) result).getOwningLibraryId() );
        	}
        }
        
        // Build a map of all referenced libraries
        List<LibrarySearchResult> referencedLibraries = FreeTextSearchService.getInstance()
        		.getLibraries( referencedLibraryIds, false );
        Map<String,LibrarySearchResult> referencedLibrariesById = new HashMap<>();
        
        for (LibrarySearchResult referencedLib : referencedLibraries) {
        	referencedLibrariesById.put( referencedLib.getSearchIndexId(), referencedLib );
        }
        
        // Second pass to build the search results
        for (SearchResult<?> result : searchResults) {
        	if (result instanceof LibrarySearchResult) {
        		RepositoryItem item = ((LibrarySearchResult) result).getRepositoryItem();
        		
                if (isReadable(item, user, accessibleItemCache)) {
                	resultsList.getSearchResult().add(
                			objectFactory.createLibrarySearchResult(
                					RepositoryUtils.createItemMetadata( item ) ) );
                }
                
        	} else if (result instanceof EntitySearchResult) {
        		EntitySearchResult entityResult = (EntitySearchResult) result;
        		LibrarySearchResult referencedLib = referencedLibrariesById.get( entityResult.getOwningLibraryId() );
        		
        		if (referencedLib != null) {
        			RepositoryItem item = referencedLib.getRepositoryItem();
        			
                    if (isReadable(item, user, accessibleItemCache)) {
                    	resultsList.getSearchResult().add(
                    			objectFactory.createLibrarySearchResult(
                    					createEntityMetadata( entityResult, item ) ) );
                    }
        		}
        	}
        }
        return objectFactory.createSearchResultsList( resultsList );
    }
    
    /**
     * Called by remote clients to retrieve the version history for an item in the OTA2.0
     * repository.
     * 
     * @param identityElement
     *            the XML element that identifies the repository item to be locked
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return JAXBElement<LibraryInfoListType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("version-history")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoListType> getVersionHistory(
            JAXBElement<RepositoryItemIdentityType> identityElement,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        Map<String, Map<TLLibraryStatus, Boolean>> accessibleItemCache = new HashMap<String, Map<TLLibraryStatus, Boolean>>();
        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                itemIdentity.getVersion());
        RepositoryItemImpl requestItem = RepositoryUtils.createRepositoryItem(repositoryManager,
                itemMetadata);
        List<RepositoryItem> historyItems = repositoryManager.getVersionHistory(requestItem);
        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);
        LibraryInfoListType metadataList = new LibraryInfoListType();

        for (RepositoryItem item : historyItems) {
            if (isReadable(item, user, accessibleItemCache)) {
                metadataList.getLibraryInfo().add(RepositoryUtils.createItemMetadata(item));
            }
        }
        return objectFactory.createLibraryInfoList(metadataList);
    }

    /**
     * Returns a list of meta-data records for the repository items which reference the item
     * specified in the request payload.
     * 
     * @param itemMetadata
     *            meta-data for the repository item for which to return where-used items
     * @param includeIndirectStr
     *            string representation of the flag indicating whether to include indirect
     *            where-used references
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return JAXBElement<LibraryInfoListType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("item-where-used")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    // TODO: Test the 'getItemWhereUsed()' API operation
    public JAXBElement<LibraryInfoListType> getItemWhereUsed(
            JAXBElement<LibraryInfoType> itemMetadata,
            @QueryParam("includeIndirect") String includeIndirectStr,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {
    	
        Map<String, Map<TLLibraryStatus, Boolean>> accessibleItemCache = new HashMap<String, Map<TLLibraryStatus, Boolean>>();
        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);
    	FreeTextSearchService searchService = FreeTextSearchService.getInstance();
    	RepositoryItem searchItem = RepositoryUtils.createRepositoryItem(repositoryManager, itemMetadata.getValue());
    	LibrarySearchResult library = searchService.getLibrary(searchItem, false);
        LibraryInfoListType metadataList = new LibraryInfoListType();
    	
    	if ((library != null) && isReadable( searchItem, user, accessibleItemCache)) {
            List<LibrarySearchResult> searchResults = searchService.getLibraryWhereUsed(
            		library, Boolean.parseBoolean( includeIndirectStr ), false );
    		
            for (LibrarySearchResult searchResult : searchResults) {
            	RepositoryItem item = searchResult.getRepositoryItem();
            	
                if (isReadable(item, user, accessibleItemCache)) {
                    metadataList.getLibraryInfo().add(RepositoryUtils.createItemMetadata(item));
                }
            }
    	}
        return objectFactory.createLibraryInfoList(metadataList);
    }

    /**
     * Returns a list of meta-data records for the entities which reference the entity
     * specified in the request payload.
     * 
     * @param entityMetadataElement
     *            meta-data for the entity for which to return where-used entities
     * @param includeIndirectStr
     *            string representation of the flag indicating whether to include indirect
     *            where-used references
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return JAXBElement<EntityInfoListType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("entity-where-used")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    // TODO: Test the 'getEntityWhereUsed()' API operation
    public JAXBElement<EntityInfoListType> getEntityWhereUsed(
            JAXBElement<EntityInfoType> entityMetadataElement,
            @QueryParam("includeIndirect") String includeIndirectStr,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {
    	
        EntityInfoType entityMetadata = entityMetadataElement.getValue();
        Map<String, Map<TLLibraryStatus, Boolean>> accessibleItemCache = new HashMap<String, Map<TLLibraryStatus, Boolean>>();
        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);
    	FreeTextSearchService searchService = FreeTextSearchService.getInstance();
    	RepositoryItem searchItem = RepositoryUtils.createRepositoryItem(repositoryManager, entityMetadata);
    	LibrarySearchResult library = searchService.getLibrary(searchItem, false);
        EntityInfoListType metadataList = new EntityInfoListType();
    	
    	if ((library != null) && isReadable( searchItem, user, accessibleItemCache)) {
    		EntitySearchResult entity = searchService.getEntity( library.getSearchIndexId(),
    				entityMetadata.getEntityName(), false );
            List<EntitySearchResult> searchResults = searchService.getEntityWhereUsed(
            		entity, Boolean.parseBoolean( includeIndirectStr ), false );
    		Map<String,LibrarySearchResult> libraryCache = new HashMap<>();
    		Map<String,Boolean> libraryAccessCache = new HashMap<>();
    		
            for (EntitySearchResult searchResult : searchResults) {
            	Boolean isReadable = libraryAccessCache.get( searchResult.getOwningLibraryId() );
            	
            	if (isReadable == null) {
                	LibrarySearchResult srLibrary = searchService.getLibrary( searchResult.getOwningLibraryId(), false );
                	
            		isReadable = (srLibrary != null) && isReadable(srLibrary.getRepositoryItem(), user, accessibleItemCache);
            		libraryCache.put( srLibrary.getSearchIndexId(), srLibrary );
            	}
            	
                if (isReadable) {
                	LibrarySearchResult srLibrary = libraryCache.get( searchResult.getOwningLibraryId() );
                	EntityInfoType srEntity = new EntityInfoType();
                	
                	RepositoryUtils.populateMetadata( srLibrary.getRepositoryItem(), srEntity );
                	srEntity.setEntityName( searchResult.getItemName() );
                	srEntity.setEntityName( searchResult.getEntityType().getName() );
                    metadataList.getEntityInfo().add( srEntity );
                }
            }
    	}
        return objectFactory.createEntityInfoList(metadataList);
    }

    /**
     * Returns a list of meta-data records for the entities which extend the entity
     * specified in the request payload.
     * 
     * @param entityMetadataElement
     *            meta-data for the entity for which to return extended entities
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return JAXBElement<EntityInfoListType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("entity-where-extended")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    // TODO: Test the 'getEntityWhereExtended()' API operation
    public JAXBElement<EntityInfoListType> getEntityWhereExtended(
            JAXBElement<EntityInfoType> entityMetadataElement,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {
    	
        EntityInfoType entityMetadata = entityMetadataElement.getValue();
        Map<String, Map<TLLibraryStatus, Boolean>> accessibleItemCache = new HashMap<String, Map<TLLibraryStatus, Boolean>>();
        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);
    	FreeTextSearchService searchService = FreeTextSearchService.getInstance();
    	RepositoryItem searchItem = RepositoryUtils.createRepositoryItem(repositoryManager, entityMetadata);
    	LibrarySearchResult library = searchService.getLibrary(searchItem, false);
        EntityInfoListType metadataList = new EntityInfoListType();
    	
    	if ((library != null) && isReadable( searchItem, user, accessibleItemCache)) {
    		EntitySearchResult entity = searchService.getEntity( library.getSearchIndexId(),
    				entityMetadata.getEntityName(), false );
            List<EntitySearchResult> searchResults = searchService.getExtendedByEntities( entity, false );
    		Map<String,LibrarySearchResult> libraryCache = new HashMap<>();
    		Map<String,Boolean> libraryAccessCache = new HashMap<>();
    		
            for (EntitySearchResult searchResult : searchResults) {
            	Boolean isReadable = libraryAccessCache.get( searchResult.getOwningLibraryId() );
            	
            	if (isReadable == null) {
                	LibrarySearchResult srLibrary = searchService.getLibrary( searchResult.getOwningLibraryId(), false );
                	
            		isReadable = (srLibrary != null) && isReadable(srLibrary.getRepositoryItem(), user, accessibleItemCache);
            		libraryCache.put( srLibrary.getSearchIndexId(), srLibrary );
            	}
            	
                if (isReadable) {
                	LibrarySearchResult srLibrary = libraryCache.get( searchResult.getOwningLibraryId() );
                	EntityInfoType srEntity = new EntityInfoType();
                	
                	RepositoryUtils.populateMetadata( srLibrary.getRepositoryItem(), srEntity );
                	srEntity.setEntityName( searchResult.getItemName() );
                	srEntity.setEntityName( searchResult.getEntityType().getName() );
                    metadataList.getEntityInfo().add( srEntity );
                }
            }
    	}
        return objectFactory.createEntityInfoList(metadataList);
    }

    /**
     * Creates the given root namespace within the repository. If the namespace URI provided is
     * nested under an existing root namespace, an exception will be thrown.
     * 
     * @param rootNamespace
     *            the root namespace to be created
     * @return Response
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @GET
    @Path("create-root-namespace")
    public Response createRootNamespace(@QueryParam("rootNamespace") String rootNamespace,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);

        if (securityManager.isAdministrator(user)) {
            repositoryManager.createRootNamespace(rootNamespace);
            return Response.status(Response.Status.OK).build();

        } else {
            throw new RepositorySecurityException(
                    "The user does not have permission to create the requested namespace.");
        }
    }

    /**
     * Deletes the specified root namespace from the repository. This method will only succeed if
     * the root namespace is empty.
     * 
     * @param rootNamespace
     *            the root namespace to be deleted
     * @return Response
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @GET
    @Path("delete-root-namespace")
    public Response deleteRootNamespace(@QueryParam("rootNamespace") String rootNamespace,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);

        if (securityManager.isAdministrator(user)) {
            repositoryManager.deleteRootNamespace(rootNamespace);
            return Response.status(Response.Status.OK).build();

        } else {
            throw new RepositorySecurityException(
                    "The user does not have permission to delete the requested namespace.");
        }
    }

    /**
     * Creates the given base namespace within the repository. If the base namespace is not part of
     * one of the repository's root namespaces, an exception will be thrown.
     * 
     * @param baseNamespace
     *            the base namespace to be created
     * @return Response
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @GET
    @Path("create-namespace")
    public Response createNamespace(@QueryParam("baseNamespace") String baseNamespace,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);
        String parentNS = RepositoryNamespaceUtils.getParentNamespace(baseNamespace,
                repositoryManager);

        if (securityManager.isAuthorized(user, parentNS, RepositoryPermission.WRITE)) {
            repositoryManager.createNamespace(baseNamespace);
            return Response.status(Response.Status.OK).build();

        } else {
            throw new RepositorySecurityException(
                    "The user does not have permission to create the requested namespace.");
        }
    }

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
     * @return Response
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @GET
    @Path("delete-namespace")
    public Response deleteNamespace(@QueryParam("baseNamespace") String baseNamespace,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);
        String parentNS = RepositoryNamespaceUtils.getParentNamespace(baseNamespace,
                repositoryManager);

        if (securityManager.isAuthorized(user, parentNS, RepositoryPermission.WRITE)) {
            repositoryManager.deleteNamespace(baseNamespace);
            return Response.status(Response.Status.OK).build();

        } else {
            throw new RepositorySecurityException(
                    "The user does not have permission to delete the requested namespace.");
        }
    }

    /**
     * Called by remote clients to publish new library content to the OTA2.0 repository.
     * 
     * @param contentStream
     *            provides access to the raw content of the file being published
     * @param contentDetail
     *            detailed information about the file (e.g. filename, etc.)
     * @param namespace
     *            the full namespace (with version-specific path information) to which the library
     *            content will be published
     * @param libraryName
     *            the name of the unmanaged library to publish
     * @param versionIdentifier
     *            the version identifier of the library being published
     * @param versionScheme
     *            the version scheme of the item to be published (may be null)
     * @param initialStatus
     *            the initial status (e.g. draft/final) of the library being published
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return Response
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("publish")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response publishContent(@FormDataParam("fileContent") InputStream contentStream,
    		@FormDataParam("fileContent") FormDataContentDisposition contentDetail,
    		@FormDataParam("namespace") String namespace,
    		@FormDataParam("libraryName") String libraryName,
    		@FormDataParam("version") String versionIdentifier,
    		@FormDataParam("versionScheme") String versionScheme,
    		@FormDataParam("status") String initialStatus,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        // Attempt to obtain a lock on the resource we are about to create. If we cannot calculate
        // the base namespace
        // for the item, proceed without the lock. In those cases, the attempt will most likely
        // result in an error anyway.
        LockableResource lockedResource = null;
        try {
            VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme(
                    versionScheme);
            String baseNamespace = vScheme.getBaseNamespace(namespace);

            lockedResource = RepositoryLockManager.getInstance().acquireWriteLock(baseNamespace,
                    contentDetail.getFileName());

        } catch (VersionSchemeException e) {
            // Ignore and proceed (see inline comments above)
        }

        try {
            UserPrincipal user = securityManager.authenticateUser(authorizationHeader);

            if (securityManager.isAuthorized(user, namespace, RepositoryPermission.WRITE)) {
                repositoryManager.getFileManager().setCurrentUserId(user.getUserId());
                RepositoryItem publishedItem = repositoryManager.publish(contentStream,
                        contentDetail.getFileName(), libraryName, namespace, versionIdentifier,
                        versionScheme, TLLibraryStatus.valueOf(initialStatus.toUpperCase()));
                Response response = Response.status(Response.Status.OK).build();

                indexRepositoryItem(publishedItem);
                return response;

            } else {
                throw new RepositorySecurityException(
                        "The user does not have permission to publish the item.");
            }

        } finally {
            if (lockedResource != null) {
                RepositoryLockManager.getInstance().releaseWriteLock(lockedResource);
            }
        }
    }

    /**
     * Called by remote clients to update existing library content in the OTA2.0 repository.
     * 
     * @param identityElement
     *            the XML element that identifies the repository item to be committed
     * @param contentStream
     *            provides access to the raw content of the file being committed
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return Response
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("commit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response commitContent(
    		@FormDataParam("item") JAXBElement<RepositoryItemIdentityType> identityElement,
    		@FormDataParam("fileContent") InputStream contentStream,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance().acquireWriteLock(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename());
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                    itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                    itemIdentity.getVersion());
            UserPrincipal user = securityManager.authenticateUser(authorizationHeader);

            if (securityManager.isAuthorized(user, itemMetadata.getBaseNamespace(),
                    RepositoryPermission.WRITE)) {
                repositoryManager.getFileManager().setCurrentUserId(user.getUserId());
                RepositoryItemImpl item = RepositoryUtils.createRepositoryItem(repositoryManager,
                        itemMetadata);

                repositoryManager.commit(item, contentStream);
                indexRepositoryItem(item);
                return Response.status(200).build();

            } else {
                throw new RepositorySecurityException(
                        "The user does not have permission to access the requested resource.");
            }

        } finally {
            RepositoryLockManager.getInstance().releaseWriteLock(lockedResource);
        }
    }

    /**
     * Called by remote clients to obtain a lock for an item in the OTA2.0 repository.
     * 
     * @param identityElement
     *            the XML element that identifies the repository item to be locked
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return JAXBElement<LibraryInfoType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("lock")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoType> lockRepositoryItem(
            JAXBElement<RepositoryItemIdentityType> identityElement,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance().acquireWriteLock(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename());
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                    itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                    itemIdentity.getVersion());
            UserPrincipal user = securityManager.authenticateUser(authorizationHeader);

            if (securityManager.isAuthorized(user, itemMetadata.getBaseNamespace(),
                    RepositoryPermission.WRITE)) {
                repositoryManager.getFileManager().setCurrentUserId(user.getUserId());
                RepositoryItemImpl item = RepositoryUtils.createRepositoryItem(repositoryManager,
                        itemMetadata);

                // Obtain the lock in the local repository
                item.setLockedByUser(user.getUserId());
                repositoryManager.lock(item);
                indexRepositoryItem(item);

                // Refresh the item's meta-data and return it to the caller
                itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                        itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                        itemIdentity.getVersion());
                return objectFactory.createLibraryInfo(itemMetadata);

            } else {
                throw new RepositorySecurityException(
                        "The user does not have permission to access the requested resource.");
            }

        } finally {
            RepositoryLockManager.getInstance().releaseWriteLock(lockedResource);
        }
    }

    /**
     * Called by remote clients to release an existing lock on an item in the OTA2.0 repository.
     * 
     * @param identityElement
     *            the XML element that identifies the repository item to be unlocked
     * @param contentStream
     *            provides access to the raw content of the file being unlocked (may be null)
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return JAXBElement<LibraryInfoType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("unlock")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoType> unlockRepositoryItem(
    		@FormDataParam("item") JAXBElement<RepositoryItemIdentityType> identityElement,
    		@FormDataParam("fileContent") InputStream contentStream,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance().acquireWriteLock(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename());
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                    itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                    itemIdentity.getVersion());
            UserPrincipal user = securityManager.authenticateUser(authorizationHeader);

            if (securityManager.isAuthorized(user, itemMetadata.getBaseNamespace(),
                    RepositoryPermission.WRITE)) {
                repositoryManager.getFileManager().setCurrentUserId(user.getUserId());
                RepositoryItemImpl item = RepositoryUtils.createRepositoryItem(repositoryManager,
                        itemMetadata);

                // Release the lock in the local repository
                item.setLockedByUser(user.getUserId());
                repositoryManager.unlock(item, contentStream);
                indexRepositoryItem(item);

                // Refresh the item's meta-data and return it to the caller
                itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                        itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                        itemIdentity.getVersion());
                return objectFactory.createLibraryInfo(itemMetadata);

            } else {
                throw new RepositorySecurityException(
                        "The user does not have permission to access the requested resource.");
            }

        } finally {
            RepositoryLockManager.getInstance().releaseWriteLock(lockedResource);
        }
    }

    /**
     * Called by remote clients to promote an item in the OTA2.0 repository from DRAFT to FINAL.
     * 
     * @param identityElement
     *            the XML element that identifies the repository item to be promoted
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return JAXBElement<LibraryInfoType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("promote")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoType> promoteRepositoryItem(
            JAXBElement<RepositoryItemIdentityType> identityElement,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance().acquireWriteLock(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename());
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                    itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                    itemIdentity.getVersion());
            UserPrincipal user = securityManager.authenticateUser(authorizationHeader);

            if (securityManager.isAuthorized(user, itemMetadata.getBaseNamespace(),
                    RepositoryPermission.WRITE)) {
                repositoryManager.getFileManager().setCurrentUserId(user.getUserId());
                RepositoryItemImpl item = RepositoryUtils.createRepositoryItem(repositoryManager,
                        itemMetadata);

                // Promote the item in the local repository
                repositoryManager.promote(item);
                indexRepositoryItem(item);

                // Refresh the item's meta-data and return it to the caller
                itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                        itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                        itemIdentity.getVersion());
                return objectFactory.createLibraryInfo(itemMetadata);

            } else {
                throw new RepositorySecurityException(
                        "The user does not have permission to access the requested resource.");
            }

        } finally {
            RepositoryLockManager.getInstance().releaseWriteLock(lockedResource);
        }
    }

    /**
     * Called by remote clients to demote an item in the OTA2.0 repository from FINAL to DRAFT.
     * 
     * @param identityElement
     *            the XML element that identifies the repository item to be demoted
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return JAXBElement<LibraryInfoType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("demote")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoType> demoteRepositoryItem(
            JAXBElement<RepositoryItemIdentityType> identityElement,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance().acquireWriteLock(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename());
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                    itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                    itemIdentity.getVersion());
            UserPrincipal user = securityManager.authenticateUser(authorizationHeader);

            if (securityManager.isAdministrator(user)) {
                repositoryManager.getFileManager().setCurrentUserId(user.getUserId());
                RepositoryItemImpl item = RepositoryUtils.createRepositoryItem(repositoryManager,
                        itemMetadata);

                // Promote the item in the local repository
                repositoryManager.demote(item);
                indexRepositoryItem(item);

                // Refresh the item's meta-data and return it to the caller
                itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                        itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                        itemIdentity.getVersion());
                return objectFactory.createLibraryInfo(itemMetadata);

            } else {
                throw new RepositorySecurityException(
                        "The user does not have permission to demote an item in the repository - administration access required.");
            }

        } finally {
            RepositoryLockManager.getInstance().releaseWriteLock(lockedResource);
        }
    }

    /**
     * Called by remote clients to recalculate the CRC value for an item in the OTA2.0 repository.
     * 
     * @param identityElement
     *            the XML element that identifies the repository item whose CRC is to be
     *            recalculated
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return JAXBElement<LibraryInfoType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("recalculate-crc")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoType> recalculateRepositoryItemCrc(
            JAXBElement<RepositoryItemIdentityType> identityElement,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance().acquireWriteLock(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename());
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                    itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                    itemIdentity.getVersion());
            UserPrincipal user = securityManager.authenticateUser(authorizationHeader);

            if (securityManager.isAdministrator(user)) {
                repositoryManager.getFileManager().setCurrentUserId(user.getUserId());
                RepositoryItemImpl item = RepositoryUtils.createRepositoryItem(repositoryManager,
                        itemMetadata);

                // Recalculate the item's CRC in the local repository
                repositoryManager.recalculateCrc(item);
                indexRepositoryItem(item);

                // Refresh the item's meta-data and return it to the caller
                itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                        itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                        itemIdentity.getVersion());
                return objectFactory.createLibraryInfo(itemMetadata);

            } else {
                throw new RepositorySecurityException(
                        "The user does not have permission to recalculate the CRC of an item in the repository - administration access required.");
            }

        } finally {
            RepositoryLockManager.getInstance().releaseWriteLock(lockedResource);
        }
    }

    /**
     * Called by remote clients to delete an item from the OTA2.0 repository.
     * 
     * @param identityElement
     *            the XML element that identifies the repository item to be deleted
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return Response
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("delete")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public Response deleteRepositoryItem(JAXBElement<RepositoryItemIdentityType> identityElement,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance().acquireWriteLock(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename());
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                    itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                    itemIdentity.getVersion());
            UserPrincipal user = securityManager.authenticateUser(authorizationHeader);

            if (securityManager.isAdministrator(user)) {
                repositoryManager.getFileManager().setCurrentUserId(user.getUserId());
                RepositoryItemImpl item = RepositoryUtils.createRepositoryItem(repositoryManager,
                        itemMetadata);

                // Delete the item in the local repository
                repositoryManager.delete(item);

                // Delete the item from the free-text search index
                indexRepositoryItem(item, true);

                return Response.status(Response.Status.OK).build();

            } else {
                throw new RepositorySecurityException(
                        "The user does not have permission to access the requested resource.");
            }

        } finally {
            RepositoryLockManager.getInstance().releaseWriteLock(lockedResource);
        }
    }

    /**
     * Returns the meta-data information for a single item in the OTA2.0 repository.
     * 
     * @param identityElement
     *            the XML element that identifies the repository item to download
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return JAXBElement<RepositoryInfoType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("metadata")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<LibraryInfoType> getRepositoryItemMetadata(
            JAXBElement<RepositoryItemIdentityType> identityElement,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance().acquireReadLock(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename());
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                    itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                    itemIdentity.getVersion());
            UserPrincipal user = securityManager.authenticateUser(authorizationHeader);

            if (securityManager.isAuthorized(user, itemMetadata.getBaseNamespace(),
                    getMinimumReadPermission(itemMetadata))) {
                return objectFactory.createLibraryInfo(itemMetadata);

            } else {
                throw new RepositorySecurityException(
                        "The user does not have permission to access the requested resource.");
            }

        } finally {
            RepositoryLockManager.getInstance().releaseReadLock(lockedResource);
        }
    }

    /**
     * Called by remote clients to download library/schema content from the OTA2.0 repository.
     * 
     * @param identityElement
     *            the XML element that identifies the repository item to download
     * @param authorizationHeader
     *            the value of the HTTP "Authorization" header
     * @return Response
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @POST
    @Path("content")
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public Response downloadContent(JAXBElement<RepositoryItemIdentityType> identityElement,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        RepositoryItemIdentityType itemIdentity = identityElement.getValue();
        LockableResource lockedResource = RepositoryLockManager.getInstance().acquireReadLock(
                itemIdentity.getBaseNamespace(), itemIdentity.getFilename());
        try {
            LibraryInfoType itemMetadata = repositoryManager.getFileManager().loadLibraryMetadata(
                    itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                    itemIdentity.getVersion());
            UserPrincipal user = securityManager.authenticateUser(authorizationHeader);

            if (securityManager.isAuthorized(user, itemMetadata.getBaseNamespace(),
                    getMinimumReadPermission(itemMetadata))) {
                File contentFile = repositoryManager.getFileManager().getLibraryContentLocation(
                        itemIdentity.getBaseNamespace(), itemIdentity.getFilename(),
                        itemIdentity.getVersion());
                ResponseBuilder response = Response.ok(contentFile);

                response.header("Content-Disposition",
                        "attachment; filename=" + contentFile.getName());
                return response.build();

            } else {
                throw new RepositorySecurityException(
                        "The user does not have permission to access the requested resource.");
            }

        } finally {
            RepositoryLockManager.getInstance().releaseReadLock(lockedResource);
        }
    }

    /**
     * Returns the permission that the registered user is authorized to perform on the specified
     * namespace.
     * 
     * @param baseNamespace
     *            the namespace for which permissions should be returned
     * @return JAXBElement<RepositoryPermissionType>
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @GET
    @Path("user-authorization")
    @Produces(MediaType.TEXT_XML)
    public JAXBElement<RepositoryPermissionType> getUserAuthorization(
            @DefaultValue("") @QueryParam("baseNamespace") String baseNamespace,
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {

        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);
        RepositoryPermission securityAuthorization =
        		(user == null) ? null : securityManager.getAuthorization(user, baseNamespace);
        org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission repositoryAuthorization = null;
        RepositoryPermissionType result = new RepositoryPermissionType();

        if (securityAuthorization != null) {
            repositoryAuthorization = org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission
                    .fromValue(securityAuthorization.value());
        } else {
            repositoryAuthorization = org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission.NONE;
        }
        result.setRepositoryPermission(repositoryAuthorization);
        return objectFactory.createRepositoryPermission(result);
    }

    /**
     * Returns the list of items locked by the calling user.  If the request is anonymous, this method
     * will always return an empty list.
     * 
     * @param authorizationHeader
     *            the authorization header for the request
     * @return JAXBElement<LibraryInfoListType> 
     * @throws RepositoryException
     *             thrown if the request cannot be processed
     */
    @GET
    @Path("locked-items")
    @Produces(MediaType.TEXT_XML)
    // TODO: Test the 'getLockedItems()' API operation
    public JAXBElement<LibraryInfoListType>  getLockedItems(
            @HeaderParam("Authorization") String authorizationHeader) throws RepositoryException {
    	
        UserPrincipal user = securityManager.authenticateUser(authorizationHeader);
        LibraryInfoListType lockedItems = new LibraryInfoListType();
        
        if (user != UserPrincipal.ANONYMOUS_USER) {
            Map<String, Map<TLLibraryStatus, Boolean>> accessibleItemCache =
            		new HashMap<String, Map<TLLibraryStatus, Boolean>>();
        	List<LibrarySearchResult> lockedLibraries =
        			FreeTextSearchService.getInstance().getLockedLibraries( user.getUserId(), false );
        	
        	for (LibrarySearchResult searchItem : lockedLibraries) {
        		RepositoryItem item = searchItem.getRepositoryItem();
        		
        		// The user has this item locked, but we will still check for read permission
        		// just in case
                if (isReadable(item, user, accessibleItemCache)) {
                	lockedItems.getLibraryInfo().add( RepositoryUtils.createItemMetadata( item ) );
                }
        	}
        }
        return objectFactory.createLibraryInfoList( lockedItems );
    }
    
    /**
     * Returns true if the specified user should be allowed read access to the given repository item
     * 
     * @param item
     *            the repository item to be checked
     * @param user
     *            the user who is requesting access to the item
     * @param accessibleItemCache
     *            cache of permissions that have already been checked during this request/response
     *            cycle
     * @return boolean
     * @throws RepositorySecurityException
     *             thrown if the user's authorizations cannot be resolved
     */
    protected boolean isReadable(RepositoryItem item, UserPrincipal user,
            Map<String, Map<TLLibraryStatus, Boolean>> accessibleItemCache)
            throws RepositorySecurityException {
        Map<TLLibraryStatus, Boolean> cacheRecord = accessibleItemCache
                .get(item.getBaseNamespace());

        if (cacheRecord == null) {
            cacheRecord = new HashMap<TLLibraryStatus, Boolean>();
            accessibleItemCache.put(item.getBaseNamespace(), cacheRecord);
        }
        Boolean hasAccess = cacheRecord.get(item.getStatus());

        if (hasAccess == null) {
            RepositoryPermission permission;

            if (item.getStatus() == TLLibraryStatus.DRAFT) {
                permission = RepositoryPermission.READ_DRAFT;

            } else { // status is FINAL
                permission = RepositoryPermission.READ_FINAL;
            }
            hasAccess = securityManager.isAuthorized(user, item.getBaseNamespace(), permission);
            cacheRecord.put(item.getStatus(), hasAccess);
        }
        return hasAccess;
    }

    /**
     * Returns the permission level that is required to read the meta-data or content of the
     * indicated repository item.
     * 
     * @param itemMetadata
     *            the meta-data for the repository item to be accessed
     * @return RepositoryPermission
     */
    private RepositoryPermission getMinimumReadPermission(LibraryInfoType itemMetadata) {
        RepositoryPermission permission;

        switch (itemMetadata.getStatus()) {
            case DRAFT:
            case UNDER_REVIEW:
                permission = RepositoryPermission.READ_DRAFT;
                break;
            default:
                permission = RepositoryPermission.READ_FINAL;
        }
        return permission;
    }

    /**
     * Returns the status enumeration from the given string value, or null if the string
     * is null or empty.
     * 
     * @param statusStr  the status string for which to return the enumeration value
     * @return TLLibraryStatus
     * @throws RepositoryException  thrown if the status string value is not valid
     */
    private TLLibraryStatus getStatus(String statusStr) throws RepositoryException {
    	try {
    		return ((statusStr == null) || (statusStr.length() == 0)) ?
    				null : TLLibraryStatus.valueOf( statusStr );
    		
    	} catch (IllegalArgumentException e) {
    		throw new RepositoryException("Unknown library status: " + statusStr);
    	}
    }
    
    /**
     * Creates a new meta-data record using information from the given entity search result.
     * 
     * @param searchResult  the search result instance for which to create a meta-data record
     * @param item  the repository item associated with the entity's owning library
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
     * @param item
     *            the repository item to be indexed
     */
    private void indexRepositoryItem(RepositoryItem item) {
        indexRepositoryItem(item, false);
    }

    /**
     * Submits the given repository item for indexing by the <code>FreeTextSearchService</code>.
     * 
     * @param item
     *            the repository item to be indexed
     * @param deleteIndex
     *            flag indicating whether the item's search index is to be deleted
     */
    private void indexRepositoryItem(RepositoryItem item, boolean deleteIndex) {
        FreeTextSearchService service = FreeTextSearchService.getInstance();

        if ((service != null) && (item != null)) {
            try {
                if (!deleteIndex) {
                    service.indexRepositoryItem(item);

                } else {
                    service.deleteRepositoryItemIndex(item);
                }

            } catch (Throwable t) {
                log.warn("Error submitting repository item for indexing: " + item.getFilename()
                        + " [" + item.getNamespace() + "]", t);
            }
        }
    }

}

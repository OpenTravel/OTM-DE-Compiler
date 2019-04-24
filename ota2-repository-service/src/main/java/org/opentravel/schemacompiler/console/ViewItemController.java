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

package org.opentravel.schemacompiler.console;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.schemacompiler.index.AssemblySearchResult;
import org.opentravel.schemacompiler.index.EntitySearchResult;
import org.opentravel.schemacompiler.index.FreeTextSearchService;
import org.opentravel.schemacompiler.index.FreeTextSearchServiceFactory;
import org.opentravel.schemacompiler.index.IndexingUtils;
import org.opentravel.schemacompiler.index.LibrarySearchResult;
import org.opentravel.schemacompiler.index.ReleaseSearchResult;
import org.opentravel.schemacompiler.index.SearchResult;
import org.opentravel.schemacompiler.index.ValidationResult;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.repository.ReleaseMember;
import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemCommit;
import org.opentravel.schemacompiler.repository.RepositoryItemHistory;
import org.opentravel.schemacompiler.repository.RepositoryItemType;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.opentravel.schemacompiler.subscription.SubscriptionManager;
import org.opentravel.schemacompiler.util.DocumentationHelper;
import org.opentravel.schemacompiler.util.FacetIdentityWrapper;
import org.opentravel.schemacompiler.util.PageUtils;
import org.opentravel.schemacompiler.util.ReferenceFinder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

/**
 * Controller that handles interactions with the view item page(s) of the OTA2.0 repository console.
 * 
 * @author S. Livezey
 */
@Controller
public class ViewItemController extends BaseController {

    private static final String FINDINGS = "findings";
    private static final String INDIRECT_WHERE_USED = "indirectWhereUsed";
    private static final String DIRECT_WHERE_USED = "directWhereUsed";
    private static final String IMAGE_RESOLVER = "imageResolver";
    private static final String DOC_HELPER = "docHelper";
    private static final String ENTITIES_BY_REFERENCE = "entitiesByReference";
    private static final String ENTITY_FACETS = "entityFacets";
    private static final String PAGE_UTILS = "pageUtils";
    private static final String LIBRARY = "library";
    private static final String ENTITY = "entity";
    public static final String LIBRARY_NOT_AUTHORIZED = "You are not authorized to view the requested library.";
    public static final String RELEASE_NOT_AUTHORIZED = "You are not authorized to view the requested release.";
    public static final String ASSEMBLY_NOT_AUTHORIZED = "You are not authorized to view the requested assembly.";
    public static final String ENTITY_NOT_AUTHORIZED = "You are not authorized to view the requested entity.";
    public static final String ENTITY_NOT_FOUND = "The requested entity could not be found.";
    public static final String ERROR_DISPLAYING_LIBRARY = "An error occured while displaying the library.";
    public static final String ERROR_DISPLAYING_LIBRARY2 =
        "An error occured while displaying the library (see server log for details).";

    private static Log log = LogFactory.getLog( BrowseController.class );

    /**
     * Called by the Spring MVC controller to display the release view page.
     * 
     * @param baseNamespace the base namespace of the selected release
     * @param filename the filename of the selected release to view
     * @param version the version of the selected release to view
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({"/releaseView.html", "/releaseView.htm"})
    public String viewRelease(@RequestParam(value = "baseNamespace") String baseNamespace,
        @RequestParam(value = "filename") String filename, @RequestParam(value = "version") String version,
        HttpSession session, Model model) {
        String targetPage = null;
        try {
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser( session );
            RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );

            checkItemType( item, RepositoryItemType.RELEASE );

            if (securityManager.isReadAuthorized( user, item )) {
                FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
                String releaseIndexId = IndexingUtils.getIdentityKey( item );
                ReleaseSearchResult release = searchService.getRelease( releaseIndexId, true );

                if (release == null) {
                    throw new RepositoryException( "The requested release cannot be displayed." );
                }
                List<ReleaseMemberItem> principalLibraries =
                    getReleaseMembers( release.getItemContent().getPrincipalMembers(), searchService );
                List<ReleaseMemberItem> referencedLibraries =
                    getReleaseMembers( release.getItemContent().getReferencedMembers(), searchService );

                Collections.sort( principalLibraries, (ReleaseMemberItem lib1, ReleaseMemberItem lib2) -> lib1
                    .getLibrary().getItemName().compareTo( lib2.getLibrary().getItemName() ) );
                Collections.sort( referencedLibraries, (ReleaseMemberItem lib1, ReleaseMemberItem lib2) -> lib1
                    .getLibrary().getItemName().compareTo( lib2.getLibrary().getItemName() ) );

                model.addAttribute( IMAGE_RESOLVER, new SearchResultImageResolver() );
                model.addAttribute( PAGE_UTILS, new PageUtils() );
                model.addAttribute( "item", item );
                model.addAttribute( "release", release );
                model.addAttribute( "principalLibraries", principalLibraries );
                model.addAttribute( "referencedLibraries", referencedLibraries );
                model.addAttribute( "externalPrincipals", release.getExternalPrincipals() );
                model.addAttribute( "externalReferences", release.getExternalReferences() );

            } else {
                setErrorMessage( RELEASE_NOT_AUTHORIZED, model );
                targetPage = new SearchController().defaultSearchPage( session, model );
            }

        } catch (Exception e) {
            log.error( "An error occured while displaying the release.", e );
            setErrorMessage( "An error occured while displaying the release (see server log for details).", model );
            targetPage = new SearchController().defaultSearchPage( session, model );
        }

        if (targetPage == null) {
            targetPage = applyCommonValues( model, "releaseView" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the assemblies with which a release is associated.
     * 
     * @param baseNamespace the base namespace of the selected release
     * @param filename the filename of the selected release to view
     * @param version the version of the selected release to view
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({"/releaseAssemblies.html", "/releaseAssemblies.htm"})
    public String releaseAssemblies(@RequestParam(value = "baseNamespace") String baseNamespace,
        @RequestParam(value = "filename") String filename, @RequestParam(value = "version") String version,
        HttpSession session, Model model) {
        String targetPage = null;
        try {
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser( session );
            RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );

            checkItemType( item, RepositoryItemType.RELEASE );

            if (securityManager.isReadAuthorized( user, item )) {
                FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
                String releaseIndexId = IndexingUtils.getIdentityKey( item );
                ReleaseSearchResult release = searchService.getRelease( releaseIndexId, true );
                List<AssemblySearchResult> releaseAssemblies = searchService.getReleaseAssemblies( release, true );

                model.addAttribute( IMAGE_RESOLVER, new SearchResultImageResolver() );
                model.addAttribute( PAGE_UTILS, new PageUtils() );
                model.addAttribute( "item", item );
                model.addAttribute( "release", release );
                model.addAttribute( "releaseAssemblies", releaseAssemblies );

            } else {
                setErrorMessage( RELEASE_NOT_AUTHORIZED, model );
                targetPage = new SearchController().defaultSearchPage( session, model );
            }

        } catch (Exception e) {
            log.error( "An error occured while displaying the release.", e );
            setErrorMessage( "An error occured while displaying the release (see server log for details).", model );
            targetPage = new SearchController().defaultSearchPage( session, model );
        }

        if (targetPage == null) {
            targetPage = applyCommonValues( model, "releaseAssemblies" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the service assembly view page.
     * 
     * @param baseNamespace the base namespace of the selected assembly
     * @param filename the filename of the selected assembly to view
     * @param version the version of the selected assembly to view
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({"/assemblyView.html", "/assemblyView.htm"})
    public String viewAssembly(@RequestParam(value = "baseNamespace") String baseNamespace,
        @RequestParam(value = "filename") String filename, @RequestParam(value = "version") String version,
        HttpSession session, Model model) {
        String targetPage = null;
        try {
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser( session );
            RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );

            checkItemType( item, RepositoryItemType.ASSEMBLY );

            if (securityManager.isReadAuthorized( user, item )) {
                FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
                String assemblyIndexId = IndexingUtils.getIdentityKey( item );
                AssemblySearchResult assembly = searchService.getAssembly( assemblyIndexId, true );

                if (assembly == null) {
                    throw new RepositoryException( "The requested release cannot be displayed." );
                }
                List<ReleaseSearchResult> providerReleases =
                    searchService.getReleases( assembly.getReferencedProviderIds(), true );
                List<ReleaseSearchResult> consumerReleases =
                    searchService.getReleases( assembly.getReferencedConsumerIds(), true );

                Collections.sort( providerReleases, (ReleaseSearchResult r1, ReleaseSearchResult r2) -> r1
                    .getReleaseName().compareTo( r2.getReleaseName() ) );
                Collections.sort( consumerReleases, (ReleaseSearchResult r1, ReleaseSearchResult r2) -> r1
                    .getReleaseName().compareTo( r2.getReleaseName() ) );

                model.addAttribute( IMAGE_RESOLVER, new SearchResultImageResolver() );
                model.addAttribute( PAGE_UTILS, new PageUtils() );
                model.addAttribute( "item", item );
                model.addAttribute( "assembly", assembly );
                model.addAttribute( "providerReleases", providerReleases );
                model.addAttribute( "consumerReleases", consumerReleases );
                model.addAttribute( "externalProviders", assembly.getExternalProviders() );
                model.addAttribute( "externalConsumers", assembly.getExternalConsumers() );

            } else {
                setErrorMessage( ASSEMBLY_NOT_AUTHORIZED, model );
                targetPage = new SearchController().defaultSearchPage( session, model );
            }

        } catch (Exception e) {
            log.error( "An error occured while displaying the assembly.", e );
            setErrorMessage( "An error occured while displaying the assembly (see server log for details).", model );
            targetPage = new SearchController().defaultSearchPage( session, model );
        }

        if (targetPage == null) {
            targetPage = applyCommonValues( model, "assemblyView" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the general-information library page.
     * 
     * @param baseNamespace the base namespace of the selected library
     * @param filename the filename of the selected library to view
     * @param version the version of the selected library to view
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({"/libraryDictionary.html", "/libraryDictionary.htm"})
    public String libraryDictionary(@RequestParam(value = "baseNamespace") String baseNamespace,
        @RequestParam(value = "filename") String filename, @RequestParam(value = "version") String version,
        HttpSession session, Model model) {
        String targetPage = null;
        try {
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser( session );
            RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );

            checkItemType( item, RepositoryItemType.LIBRARY );

            if (securityManager.isReadAuthorized( user, item )) {
                FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
                String libraryIndexId = IndexingUtils.getIdentityKey( item );
                List<EntitySearchResult> entityList = searchService.getEntities( libraryIndexId, false );

                Collections.sort( entityList, (EntitySearchResult entity1, EntitySearchResult entity2) -> entity1
                    .getItemName().compareTo( entity2.getItemName() ) );
                model.addAttribute( IMAGE_RESOLVER, new SearchResultImageResolver() );
                model.addAttribute( PAGE_UTILS, new PageUtils() );
                model.addAttribute( "entityList", entityList );
                model.addAttribute( "item", item );

            } else {
                setErrorMessage( LIBRARY_NOT_AUTHORIZED, model );
                targetPage = new SearchController().defaultSearchPage( session, model );
            }

        } catch (Exception e) {
            log.error( ERROR_DISPLAYING_LIBRARY, e );
            setErrorMessage( ERROR_DISPLAYING_LIBRARY2, model );
            targetPage = new SearchController().defaultSearchPage( session, model );
        }

        if (targetPage == null) {
            targetPage = applyCommonValues( model, "libraryDictionary" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the general-information library page.
     * 
     * @param baseNamespace the base namespace of the selected library
     * @param filename the filename of the selected library to view
     * @param version the version of the selected library to view
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({"/libraryUsage.html", "/libraryUsage.htm"})
    public String libraryUsage(@RequestParam(value = "baseNamespace") String baseNamespace,
        @RequestParam(value = "filename") String filename, @RequestParam(value = "version") String version,
        HttpSession session, Model model) {
        String targetPage = null;
        try {
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser( session );
            RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );

            checkItemType( item, RepositoryItemType.LIBRARY );

            if (securityManager.isReadAuthorized( user, item )) {
                FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
                String indexItemId = IndexingUtils.getIdentityKey( item );
                LibrarySearchResult indexItem = searchService.getLibrary( indexItemId, false );
                List<LibrarySearchResult> usesLibraries =
                    searchService.getLibraries( indexItem.getReferencedLibraryIds(), false );
                List<LibrarySearchResult> directWhereUsed =
                    searchService.getLibraryWhereUsed( indexItem, false, false );
                List<LibrarySearchResult> indirectWhereUsed =
                    searchService.getLibraryWhereUsed( indexItem, true, false );

                purgeDirectWhereUsed( indirectWhereUsed, directWhereUsed );

                model.addAttribute( "usesLibraries", usesLibraries );
                model.addAttribute( DIRECT_WHERE_USED, directWhereUsed );
                model.addAttribute( INDIRECT_WHERE_USED, indirectWhereUsed );
                model.addAttribute( "item", item );

            } else {
                setErrorMessage( LIBRARY_NOT_AUTHORIZED, model );
                targetPage = new SearchController().defaultSearchPage( session, model );
            }

        } catch (Exception e) {
            log.error( ERROR_DISPLAYING_LIBRARY, e );
            setErrorMessage( ERROR_DISPLAYING_LIBRARY2, model );
            targetPage = new SearchController().defaultSearchPage( session, model );
        }

        if (targetPage == null) {
            targetPage = applyCommonValues( model, "libraryUsage" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the general-information library page.
     * 
     * @param baseNamespace the base namespace of the selected library
     * @param filename the filename of the selected library to view
     * @param version the version of the selected library to view
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({"/libraryValidation.html", "/libraryValidation.htm"})
    public String libraryValidation(@RequestParam(value = "baseNamespace") String baseNamespace,
        @RequestParam(value = "filename") String filename, @RequestParam(value = "version") String version,
        HttpSession session, Model model) {
        String targetPage = null;
        try {
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser( session );
            RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );

            checkItemType( item, RepositoryItemType.LIBRARY );

            if (securityManager.isReadAuthorized( user, item )) {
                FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
                String indexItemId = IndexingUtils.getIdentityKey( item );
                List<ValidationResult> findings = searchService.getLibraryFindings( indexItemId );

                model.addAttribute( FINDINGS, findings );
                model.addAttribute( "item", item );

            } else {
                setErrorMessage( LIBRARY_NOT_AUTHORIZED, model );
                targetPage = new SearchController().defaultSearchPage( session, model );
            }

        } catch (Exception e) {
            log.error( ERROR_DISPLAYING_LIBRARY, e );
            setErrorMessage( ERROR_DISPLAYING_LIBRARY2, model );
            targetPage = new SearchController().defaultSearchPage( session, model );
        }

        if (targetPage == null) {
            targetPage = applyCommonValues( model, "libraryValidation" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the library releases page.
     * 
     * @param baseNamespace the base namespace of the selected library
     * @param filename the filename of the selected library to view
     * @param version the version of the selected library to view
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({"/libraryReleases.html", "/libraryReleases.htm"})
    public String libraryReleases(@RequestParam(value = "baseNamespace") String baseNamespace,
        @RequestParam(value = "filename") String filename, @RequestParam(value = "version") String version,
        HttpSession session, Model model) {
        String targetPage = null;
        try {
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser( session );
            RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );

            checkItemType( item, RepositoryItemType.LIBRARY );

            if (securityManager.isReadAuthorized( user, item )) {
                FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
                LibrarySearchResult library = searchService.getLibrary( item, false );
                List<ReleaseSearchResult> releaseList = searchService.getLibraryReleases( library, false );

                model.addAttribute( "releaseList", releaseList );
                model.addAttribute( PAGE_UTILS, new PageUtils() );
                model.addAttribute( "item", item );

            } else {
                setErrorMessage( LIBRARY_NOT_AUTHORIZED, model );
                targetPage = new SearchController().defaultSearchPage( session, model );
            }

        } catch (Exception e) {
            log.error( ERROR_DISPLAYING_LIBRARY, e );
            setErrorMessage( ERROR_DISPLAYING_LIBRARY2, model );
            targetPage = new SearchController().defaultSearchPage( session, model );
        }

        if (targetPage == null) {
            targetPage = applyCommonValues( model, "libraryReleases" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the library releases page.
     * 
     * @param baseNamespace the base namespace of the selected library
     * @param filename the filename of the selected library to view
     * @param version the version of the selected library to view
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({"/libraryAssemblies.html", "/libraryAssemblies.htm"})
    public String libraryAssemblies(@RequestParam(value = "baseNamespace") String baseNamespace,
        @RequestParam(value = "filename") String filename, @RequestParam(value = "version") String version,
        HttpSession session, Model model) {
        String targetPage = null;
        try {
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser( session );
            RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );

            checkItemType( item, RepositoryItemType.LIBRARY );

            if (securityManager.isReadAuthorized( user, item )) {
                FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
                LibrarySearchResult library = searchService.getLibrary( item, false );
                List<ReleaseSearchResult> releaseList = searchService.getLibraryReleases( library, false );
                List<AssemblySearchResult> libraryAssemblies = new ArrayList<>();
                Set<String> assemblyIds = new HashSet<>();

                for (ReleaseSearchResult release : releaseList) {
                    List<AssemblySearchResult> releaseAssemblies = searchService.getReleaseAssemblies( release, true );

                    for (AssemblySearchResult assembly : releaseAssemblies) {
                        if (!assemblyIds.contains( assembly.getSearchIndexId() )) {
                            libraryAssemblies.add( assembly );
                            assemblyIds.add( assembly.getSearchIndexId() );
                        }
                    }
                }

                model.addAttribute( PAGE_UTILS, new PageUtils() );
                model.addAttribute( "item", item );
                model.addAttribute( "libraryAssemblies", libraryAssemblies );

            } else {
                setErrorMessage( LIBRARY_NOT_AUTHORIZED, model );
                targetPage = new SearchController().defaultSearchPage( session, model );
            }

        } catch (Exception e) {
            log.error( ERROR_DISPLAYING_LIBRARY, e );
            setErrorMessage( ERROR_DISPLAYING_LIBRARY2, model );
            targetPage = new SearchController().defaultSearchPage( session, model );
        }

        if (targetPage == null) {
            targetPage = applyCommonValues( model, "libraryAssemblies" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the commit-history library page.
     * 
     * @param baseNamespace the base namespace of the selected library
     * @param filename the filename of the selected library to view
     * @param version the version of the selected library to view
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({"/libraryHistory.html", "/libraryHistory.htm"})
    public String libraryHistory(@RequestParam(value = "baseNamespace") String baseNamespace,
        @RequestParam(value = "filename") String filename, @RequestParam(value = "version") String version,
        HttpSession session, Model model) {
        String targetPage = null;
        try {
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser( session );
            RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );

            checkItemType( item, RepositoryItemType.LIBRARY );

            if (securityManager.isReadAuthorized( user, item )) {
                Map<String,UserPrincipal> commitUsers = new HashMap<>();
                RepositoryItemHistory history = getItemHistory( item, commitUsers, securityManager );

                model.addAttribute( "history", history );
                model.addAttribute( "commitUsers", commitUsers );
                model.addAttribute( PAGE_UTILS, new PageUtils() );
                model.addAttribute( "item", item );

            } else {
                setErrorMessage( LIBRARY_NOT_AUTHORIZED, model );
                targetPage = new SearchController().defaultSearchPage( session, model );
            }

        } catch (Exception e) {
            log.error( ERROR_DISPLAYING_LIBRARY, e );
            setErrorMessage( ERROR_DISPLAYING_LIBRARY2, model );
            targetPage = new SearchController().defaultSearchPage( session, model );
        }

        if (targetPage == null) {
            targetPage = applyCommonValues( model, "libraryHistory" );
        }
        return targetPage;
    }

    /**
     * Returns the commit history of the given repository item and collects the user accounts of each user that
     * committed to it.
     * 
     * @param item the repository item for which to return the commit history
     * @param commitUsers the map of users who performed commits
     * @param securityManager the security manager used to resolved user ID's
     * @return RepositoryItemHistory
     */
    private RepositoryItemHistory getItemHistory(RepositoryItem item, Map<String,UserPrincipal> commitUsers,
        RepositorySecurityManager securityManager) {
        RepositoryItemHistory history = null;

        try {
            history = getRepositoryManager().getHistory( item );

            // Build a map of all users that have contributed commits to this repository item
            for (RepositoryItemCommit commitItem : history.getCommitHistory()) {
                String userId = commitItem.getUser();

                if (!commitUsers.containsKey( userId )) {
                    UserPrincipal commitUser = securityManager.getUser( userId );

                    if (commitUser != null) {
                        commitUsers.put( userId, commitUser );
                    }
                }
            }

        } catch (RepositoryException e) {
            // Ignore exception if history does not yet exist
        }
        return history;
    }

    /**
     * Called by the Spring MVC controller to display the general-information library page.
     * 
     * @param baseNamespace the base namespace of the selected library
     * @param filename the filename of the selected library to view
     * @param version the version of the selected library to view
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({"/libraryInfo.html", "/libraryInfo.htm"})
    public String libraryInfo(@RequestParam(value = "baseNamespace") String baseNamespace,
        @RequestParam(value = "filename") String filename, @RequestParam(value = "version") String version,
        HttpSession session, Model model) {
        String targetPage = null;
        try {
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser( session );
            RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );
            SubscriptionTarget sTarget = SubscriptionManager.getSubscriptionTarget( item );

            checkItemType( item, RepositoryItemType.LIBRARY );

            if (securityManager.isReadAuthorized( user, item )) {
                SubscriptionManager subscriptionManager =
                    RepositoryComponentFactory.getDefault().getSubscriptionManager();
                FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
                boolean otm16Enabled = RepositoryUtils.isOTM16Library( item, getRepositoryManager() );
                boolean hasAllVersionsSubscription = (subscriptionManager != null)
                    && !subscriptionManager.getAllVersionsSubscriptions( sTarget, user.getUserId() ).isEmpty();
                boolean hasSingleVersionSubscription = (subscriptionManager != null)
                    && !subscriptionManager.getSingleVersionSubscriptions( sTarget, user.getUserId() ).isEmpty();
                String indexItemId = IndexingUtils.getIdentityKey( item );
                LibrarySearchResult indexItem = searchService.getLibrary( indexItemId, false );
                UserPrincipal lockedByUser = null;

                if (item.getLockedByUser() != null) {
                    lockedByUser = securityManager.getUser( item.getLockedByUser() );
                }
                model.addAttribute( "otm16Enabled", otm16Enabled );
                model.addAttribute( "lockedByUser", lockedByUser );
                model.addAttribute( "indexItem", indexItem );
                model.addAttribute( "canEditSubscription",
                    (subscriptionManager != null) && (user != UserPrincipal.ANONYMOUS_USER) );
                model.addAttribute( "hasAllVersionsSubscription", hasAllVersionsSubscription );
                model.addAttribute( "hasSingleVersionSubscription", hasSingleVersionSubscription );
                model.addAttribute( "item", item );

            } else {
                setErrorMessage( LIBRARY_NOT_AUTHORIZED, model );
                targetPage = new SearchController().defaultSearchPage( session, model );
            }

        } catch (Exception e) {
            log.error( ERROR_DISPLAYING_LIBRARY, e );
            setErrorMessage( ERROR_DISPLAYING_LIBRARY2, model );
            targetPage = new SearchController().defaultSearchPage( session, model );
        }

        if (targetPage == null) {
            targetPage = applyCommonValues( model, "libraryInfo" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the raw-content library page.
     * 
     * @param baseNamespace the base namespace of the selected library
     * @param filename the filename of the selected library to view
     * @param version the version of the selected library to view
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({"/libraryRawContent.html", "/libraryRawContent.htm"})
    public String libraryRawContent(@RequestParam(value = "baseNamespace") String baseNamespace,
        @RequestParam(value = "filename") String filename, @RequestParam(value = "version") String version,
        HttpSession session, Model model) {
        String targetPage = null;
        try {
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser( session );

            // User must be an administrator to view raw OTM content
            if (securityManager.isAdministrator( user )) {
                RepositoryManager repositoryManager = getRepositoryManager();
                RepositoryItem item = repositoryManager.getRepositoryItem( baseNamespace, filename, version );
                File libraryFile = repositoryManager.getFileManager()
                    .getLibraryContentLocation( item.getBaseNamespace(), item.getFilename(), item.getVersion() );
                String libraryContent;

                // Load the raw library content from the OTM file
                try (StringWriter out = new StringWriter()) {
                    try (Reader in = new FileReader( libraryFile )) {
                        char[] buffer = new char[1024];
                        int charsRead;

                        while ((charsRead = in.read( buffer, 0, buffer.length )) >= 0) {
                            out.write( buffer, 0, charsRead );
                        }
                    }
                    libraryContent = StringEscapeUtils.escapeXml( out.toString() );
                }
                model.addAttribute( "libraryContent", libraryContent );
                model.addAttribute( "item", item );

            } else {
                setErrorMessage( LIBRARY_NOT_AUTHORIZED, model );
                targetPage = new SearchController().defaultSearchPage( session, model );
            }

        } catch (Exception e) {
            log.error( ERROR_DISPLAYING_LIBRARY, e );
            setErrorMessage( ERROR_DISPLAYING_LIBRARY2, model );
            targetPage = new SearchController().defaultSearchPage( session, model );
        }

        if (targetPage == null) {
            targetPage = applyCommonValues( model, "libraryRawContent" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the general-information library page.
     * 
     * @param namespace the namespace of the selected entity
     * @param localName the local name of the selected entity
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({"/entityDictionary.html", "/entityDictionary.htm"})
    public String entityDictionary(@RequestParam(value = "namespace") String namespace,
        @RequestParam(value = "localName") String localName, HttpSession session, Model model) {
        String targetPage = null;
        try {
            FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
            RepositorySecurityManager securityManager = getSecurityManager();
            String identityKey = IndexingUtils.getIdentityKey( namespace, localName, true );
            EntitySearchResult indexEntity = searchService.getEntity( identityKey, true );
            LibrarySearchResult indexLibrary =
                (indexEntity == null) ? null : searchService.getLibrary( indexEntity.getOwningLibraryId(), false );

            if (indexLibrary != null) {
                UserPrincipal user = getCurrentUser( session );

                if (securityManager.isReadAuthorized( user, indexLibrary.getRepositoryItem() )) {
                    ReferenceFinder refFinder = new ReferenceFinder( indexEntity.getItemContent(), indexLibrary );
                    Map<String,EntitySearchResult> entitiesByReference =
                        refFinder.buildEntityReferenceMap( searchService );
                    List<FacetIdentityWrapper> entityFacets = buildFacetList( indexEntity.getItemContent() );

                    model.addAttribute( ENTITY, indexEntity );
                    model.addAttribute( LIBRARY, indexLibrary );
                    model.addAttribute( PAGE_UTILS, new PageUtils() );
                    model.addAttribute( ENTITY_FACETS, entityFacets );
                    model.addAttribute( ENTITIES_BY_REFERENCE, entitiesByReference );
                    model.addAttribute( DOC_HELPER, new DocumentationHelper( indexEntity ) );
                    model.addAttribute( IMAGE_RESOLVER, new SearchResultImageResolver() );

                } else {
                    setErrorMessage( ENTITY_NOT_AUTHORIZED, model );
                    targetPage = new SearchController().defaultSearchPage( session, model );
                }
            } else {
                setErrorMessage( ENTITY_NOT_FOUND, model );
                targetPage = new SearchController().defaultSearchPage( session, model );
            }

        } catch (Exception e) {
            log.error( ERROR_DISPLAYING_LIBRARY, e );
            setErrorMessage( ERROR_DISPLAYING_LIBRARY2, model );
        }

        if (targetPage == null) {
            targetPage = applyCommonValues( model, "entityDictionary" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the general-information library page.
     * 
     * @param namespace the namespace of the selected entity
     * @param localName the local name of the selected entity
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({"/entityUsage.html", "/entityUsage.htm"})
    public String entityUsage(@RequestParam(value = "namespace") String namespace,
        @RequestParam(value = "localName") String localName, HttpSession session, Model model) {
        String targetPage = null;
        try {
            FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
            RepositorySecurityManager securityManager = getSecurityManager();
            String identityKey = IndexingUtils.getIdentityKey( namespace, localName, true );
            EntitySearchResult indexEntity = searchService.getEntity( identityKey, true );
            LibrarySearchResult indexLibrary =
                (indexEntity == null) ? null : searchService.getLibrary( indexEntity.getOwningLibraryId(), false );

            if (indexLibrary != null) {
                UserPrincipal user = getCurrentUser( session );

                if (securityManager.isReadAuthorized( user, indexLibrary.getRepositoryItem() )) {
                    List<EntitySearchResult> directWhereUsed =
                        searchService.getEntityWhereUsed( indexEntity, false, false );
                    List<EntitySearchResult> indirectWhereUsed =
                        searchService.getEntityWhereUsed( indexEntity, true, false );

                    purgeDirectWhereUsed( indirectWhereUsed, directWhereUsed );

                    model.addAttribute( ENTITY, indexEntity );
                    model.addAttribute( LIBRARY, indexLibrary );
                    model.addAttribute( DIRECT_WHERE_USED, directWhereUsed );
                    model.addAttribute( INDIRECT_WHERE_USED, indirectWhereUsed );
                    model.addAttribute( IMAGE_RESOLVER, new SearchResultImageResolver() );

                } else {
                    setErrorMessage( ENTITY_NOT_AUTHORIZED, model );
                    targetPage = new SearchController().defaultSearchPage( session, model );
                }
            } else {
                setErrorMessage( ENTITY_NOT_FOUND, model );
                targetPage = new SearchController().defaultSearchPage( session, model );
            }

        } catch (Exception e) {
            log.error( ERROR_DISPLAYING_LIBRARY, e );
            setErrorMessage( ERROR_DISPLAYING_LIBRARY2, model );
        }

        if (targetPage == null) {
            targetPage = applyCommonValues( model, "entityUsage" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the general-information library page.
     * 
     * @param namespace the namespace of the selected entity
     * @param localName the local name of the selected entity
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({"/entityValidation.html", "/entityValidation.htm"})
    public String entityValidation(@RequestParam(value = "namespace") String namespace,
        @RequestParam(value = "localName") String localName, HttpSession session, Model model) {
        String targetPage = null;
        try {
            FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
            RepositorySecurityManager securityManager = getSecurityManager();
            String identityKey = IndexingUtils.getIdentityKey( namespace, localName, true );
            EntitySearchResult indexEntity = searchService.getEntity( identityKey, true );
            LibrarySearchResult indexLibrary =
                (indexEntity == null) ? null : searchService.getLibrary( indexEntity.getOwningLibraryId(), false );

            if (indexLibrary != null) {
                UserPrincipal user = getCurrentUser( session );

                if (securityManager.isReadAuthorized( user, indexLibrary.getRepositoryItem() )) {
                    List<ValidationResult> findings = searchService.getEntityFindings( identityKey );

                    model.addAttribute( FINDINGS, findings );
                    model.addAttribute( ENTITY, indexEntity );
                    model.addAttribute( LIBRARY, indexLibrary );

                } else {
                    setErrorMessage( ENTITY_NOT_AUTHORIZED, model );
                    targetPage = new SearchController().defaultSearchPage( session, model );
                }
            } else {
                setErrorMessage( ENTITY_NOT_FOUND, model );
                targetPage = new SearchController().defaultSearchPage( session, model );
            }

        } catch (Exception e) {
            log.error( ERROR_DISPLAYING_LIBRARY, e );
            setErrorMessage( ERROR_DISPLAYING_LIBRARY2, model );
        }

        if (targetPage == null) {
            targetPage = applyCommonValues( model, "entityValidation" );
        }
        return targetPage;
    }

    /**
     * Iterates through the indirect where-used list of libraries and removes all entries that are members of the direct
     * where-used list.
     * 
     * @param indirectWhereUsed the list of indirect where-used libraries to process
     * @param directWhereUsed the list of direct where-used libraries
     */
    private void purgeDirectWhereUsed(List<? extends SearchResult<?>> indirectWhereUsed,
        List<? extends SearchResult<?>> directWhereUsed) {
        Iterator<? extends SearchResult<?>> iterator = indirectWhereUsed.iterator();
        Set<String> directWhereUsedIds = new HashSet<>();

        for (SearchResult<?> sr : directWhereUsed) {
            directWhereUsedIds.add( sr.getSearchIndexId() );
        }
        while (iterator.hasNext()) {
            SearchResult<?> sr = iterator.next();

            if (directWhereUsedIds.contains( sr.getSearchIndexId() )) {
                iterator.remove();
            }
        }
    }

    /**
     * Constructs the list of facets for the given <code>NamedEntity</code>. If the entity is not a facet owner, an
     * empty list will be returned.
     * 
     * @param entity the entity from which to construct the facet list
     * @return List&lt;FacetIdentityWrapper&gt;
     */
    private List<FacetIdentityWrapper> buildFacetList(NamedEntity entity) {
        List<FacetIdentityWrapper> facetList = new ArrayList<>();

        if (entity instanceof TLBusinessObject) {
            TLBusinessObject facetOwner = (TLBusinessObject) entity;

            facetList.add( new FacetIdentityWrapper( facetOwner.getIdFacet() ) );
            facetList.add( new FacetIdentityWrapper( facetOwner.getSummaryFacet() ) );
            facetList.add( new FacetIdentityWrapper( facetOwner.getDetailFacet() ) );
            addContextualFacets( facetOwner.getCustomFacets(), facetList );
            addContextualFacets( facetOwner.getQueryFacets(), facetList );
            addContextualFacets( facetOwner.getUpdateFacets(), facetList );

        } else if (entity instanceof TLChoiceObject) {
            TLChoiceObject facetOwner = (TLChoiceObject) entity;

            facetList.add( new FacetIdentityWrapper( facetOwner.getSharedFacet() ) );
            addContextualFacets( facetOwner.getChoiceFacets(), facetList );

        } else if (entity instanceof TLCoreObject) {
            TLCoreObject facetOwner = (TLCoreObject) entity;

            facetList.add( new FacetIdentityWrapper( facetOwner.getSummaryFacet() ) );
            facetList.add( new FacetIdentityWrapper( facetOwner.getDetailFacet() ) );

        } else if (entity instanceof TLOperation) {
            TLOperation facetOwner = (TLOperation) entity;

            facetList.add( new FacetIdentityWrapper( facetOwner.getRequest() ) );
            facetList.add( new FacetIdentityWrapper( facetOwner.getResponse() ) );
            facetList.add( new FacetIdentityWrapper( facetOwner.getNotification() ) );
        }
        return facetList;
    }

    /**
     * Recursive method that adds all contextual facets and their children to the list provided.
     * 
     * @param contextualFacets the list of contextual facets to add
     * @param facetList the facet list being constructed
     */
    private void addContextualFacets(List<TLContextualFacet> contextualFacets, List<FacetIdentityWrapper> facetList) {
        for (TLContextualFacet facet : contextualFacets) {
            facetList.add( new FacetIdentityWrapper( facet ) );
            addContextualFacets( facet.getChildFacets(), facetList );
        }
    }

    /**
     * Returns the list of <code>ReleaseMemberItem</code> instances that correspond to the given list of release
     * members.
     * 
     * @param memberList the list of release members
     * @param service the search service to use when returning the search index results
     * @return List&lt;ReleaseMemberItem&gt;
     * @throws RepositoryException thrown if the search index cannnot be accessed
     */
    private List<ReleaseMemberItem> getReleaseMembers(List<ReleaseMember> memberList, FreeTextSearchService service)
        throws RepositoryException {
        List<ReleaseMemberItem> memberItems = new ArrayList<>();

        for (ReleaseMember member : memberList) {
            String memberKey = IndexingUtils.getIdentityKey( member.getRepositoryItem() );
            LibrarySearchResult library = service.getLibrary( memberKey, false );

            if (library != null) {
                memberItems.add( new ReleaseMemberItem( library, member.getEffectiveDate() ) );
            }
        }
        return memberItems;
    }

    /**
     * Verifies that the given repository item matches the specified item type and throws an exception if it is not.
     * 
     * @param item the repository item to check
     * @param expectedType the expected type of the repository item
     * @throws RepositoryException thrown if the repository item is not an OTM library
     */
    private void checkItemType(RepositoryItem item, RepositoryItemType expectedType) throws RepositoryException {
        if (!expectedType.isItemType( item.getFilename() )) {
            throw new RepositoryException( "The specified repository item is not an OTM library." );
        }
    }

}

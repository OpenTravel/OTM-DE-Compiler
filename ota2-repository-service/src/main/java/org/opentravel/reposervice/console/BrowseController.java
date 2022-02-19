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

package org.opentravel.reposervice.console;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionEventType;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.opentravel.repocommon.index.FreeTextSearchService;
import org.opentravel.repocommon.index.FreeTextSearchServiceFactory;
import org.opentravel.repocommon.index.IndexingUtils;
import org.opentravel.repocommon.index.LibrarySearchResult;
import org.opentravel.repocommon.index.SubscriptionSearchResult;
import org.opentravel.repocommon.repository.RepositoryComponentFactory;
import org.opentravel.repocommon.security.RepositorySecurityManager;
import org.opentravel.repocommon.security.UserPrincipal;
import org.opentravel.repocommon.subscription.SubscriptionManager;
import org.opentravel.reposervice.util.PageUtils;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.RepositoryNamespaceUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

/**
 * Controller that handles interactions with the browse page of the OTA2.0 repository console.
 * 
 * @author S. Livezey
 */
@Controller
public class BrowseController extends BaseController {

    private static final String FILENAME = "filename";
    private static final String BASE_NAMESPACE = "baseNamespace";
    private static final String PAGE_UTILS = "pageUtils";
    private static final String IMAGE_RESOLVER = "imageResolver";
    private static final String BROWSE_ITEMS = "browseItems";
    private static final String ERROR_DISPLAYING_PAGE =
        "An error occured while displaying the page (see server log for details).";
    private static final String REDIRECT_SUBSCRIPTIONS = "redirect:/console/subscriptions.html";
    private static final String REDIRECT_INDEX = "redirect:/console/index.html";
    private static final String REDIRECT_BROWSE = "redirect:/console/browse.html";

    private static Logger log = LogManager.getLogger( BrowseController.class );

    /**
     * Called by the Spring MVC controller to display the application browse page.
     * 
     * @param baseNamespace the root namespace parameter from the URL
     * @param filename the filename of the selected library
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({"/browse.html", "/browse.htm"})
    public String browsePage(@RequestParam(value = BASE_NAMESPACE, required = false) String baseNamespace,
        @RequestParam(value = FILENAME, required = false) String filename, HttpSession session, Model model) {
        try {
            RepositoryManager repositoryManager = getRepositoryManager();
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser( session );
            List<NamespaceItem> browseItems;

            if (baseNamespace == null) { // display root namespaces
                browseItems = getRootNSBrowseItems( user );

            } else if (filename != null) { // display all versions of the selected library or release
                browseItems = getDetailBrowseItems( baseNamespace, filename, model, user );

            } else { // display sub-namespaces and latest version of each library
                browseItems = getNamespaceBrowseItems( baseNamespace, user );
            }

            if (baseNamespace != null) {
                SubscriptionManager subscriptionManager =
                    RepositoryComponentFactory.getDefault().getSubscriptionManager();

                model.addAttribute( "parentItems", getParentNamespaceItems( baseNamespace ) );
                model.addAttribute( "canCreateNamespaceExtension",
                    securityManager.isAuthorized( user, baseNamespace, RepositoryPermission.WRITE ) );
                model.addAttribute( "canEditSubscription",
                    (subscriptionManager != null) && (user != UserPrincipal.ANONYMOUS_USER) );
                model.addAttribute( "hasSubscription", (subscriptionManager != null)
                    && !subscriptionManager.getNamespaceSubscriptions( baseNamespace, user.getUserId() ).isEmpty() );

                if (!repositoryManager.listRootNamespaces().contains( baseNamespace )
                    && repositoryManager.listNamespaceChildren( baseNamespace ).isEmpty()
                    && repositoryManager.listItems( baseNamespace, false, false ).isEmpty()) {
                    String parentNS = RepositoryNamespaceUtils.getParentNamespace( baseNamespace, repositoryManager );

                    model.addAttribute( "canDeleteNamespace",
                        securityManager.isAuthorized( user, parentNS, RepositoryPermission.WRITE ) );
                }
            }
            model.addAttribute( IMAGE_RESOLVER, new SearchResultImageResolver() );
            model.addAttribute( PAGE_UTILS, new PageUtils() );
            model.addAttribute( BASE_NAMESPACE, baseNamespace );
            model.addAttribute( FILENAME, filename );
            model.addAttribute( BROWSE_ITEMS, browseItems );

        } catch (Exception e) {
            log.error( "An error occured while displaying the browse page.", e );
            setErrorMessage( ERROR_DISPLAYING_PAGE, model );
        }
        return applyCommonValues( model, "browse" );
    }

    /**
     * Returns the browse items for the specified base namespace to which the user has access. The returned items
     * represent the root namespaces of the repository.
     * 
     * @param user the user requesting access to the browse items
     * @return List&lt;NamespaceItem&gt;
     * @throws RepositoryException thrown if an exception occurs while accessing the local repository
     */
    private List<NamespaceItem> getRootNSBrowseItems(UserPrincipal user) throws RepositoryException {
        RepositorySecurityManager securityManager = getSecurityManager();
        List<NamespaceItem> browseItems = new ArrayList<>();

        for (String rootNS : getRepositoryManager().listRootNamespaces()) {
            try {
                if (securityManager.isAuthorized( user, rootNS, RepositoryPermission.READ_FINAL )) {
                    browseItems.add( new NamespaceItem( rootNS ) );
                }

            } catch (Exception e) {
                log.warn( "Error determining user access to root namespace: " + rootNS );
            }
        }
        return browseItems;
    }

    /**
     * Returns the browse items for the specified base namespace to which the user has access. The returned items
     * include all versions of the library/release specified by the base namespace and filename.
     * 
     * @param baseNamespace the base namespace for which to return browse items
     * @param filename the repository item filename for the browse items to return
     * @param model the model context to be used when rendering the page view
     * @param user the user requesting access to the browse items
     * @return List&lt;NamespaceItem&gt;
     * @throws RepositoryException thrown if an exception occurs while accessing the local repository
     */
    private List<NamespaceItem> getDetailBrowseItems(String baseNamespace, String filename, Model model,
        UserPrincipal user) throws RepositoryException {
        RepositoryManager repositoryManager = getRepositoryManager();
        RepositorySecurityManager securityManager = getSecurityManager();
        List<RepositoryItem> allItems =
            repositoryManager.listItems( baseNamespace, TLLibraryStatus.DRAFT, false, null );
        List<NamespaceItem> browseItems = new ArrayList<>();

        for (RepositoryItem item : allItems) {
            if (item.getFilename().equals( filename )) {
                List<RepositoryItem> versionHistory = repositoryManager.getVersionHistory( item );

                for (RepositoryItem itemVersion : versionHistory) {
                    if (securityManager.isReadAuthorized( user, itemVersion )) {
                        browseItems.add( createNamespaceItem( itemVersion ) );
                    }
                }
                model.addAttribute( "libraryName", item.getLibraryName() );
            }
        }
        return browseItems;
    }

    /**
     * Returns the browse items for the specified base namespace to which the user has access. The returned items
     * include sub-namespaces and the latest version of each repository item.
     * 
     * @param baseNamespace the base namespace for which to return browse items
     * @param user the user requesting access to the browse items
     * @return List&lt;NamespaceItem&gt;
     * @throws RepositoryException thrown if an exception occurs while accessing the local repository
     */
    private List<NamespaceItem> getNamespaceBrowseItems(String baseNamespace, UserPrincipal user)
        throws RepositoryException {
        RepositoryManager repositoryManager = getRepositoryManager();
        RepositorySecurityManager securityManager = getSecurityManager();
        List<String> nsChildren = repositoryManager.listNamespaceChildren( baseNamespace );
        List<RepositoryItem> itemList = repositoryManager.listItems( baseNamespace, TLLibraryStatus.DRAFT, true, null );
        List<NamespaceItem> browseItems = new ArrayList<>();

        for (String childPath : nsChildren) {
            String childNS = RepositoryNamespaceUtils.appendChildPath( baseNamespace, childPath );

            if (securityManager.isAuthorized( user, childNS, RepositoryPermission.READ_FINAL )) {
                browseItems.add( new NamespaceItem( childNS, childPath ) );
            }
        }

        for (RepositoryItem item : itemList) {
            if (securityManager.isReadAuthorized( user, item )) {
                browseItems.add( createNamespaceItem( item ) );
            }
        }
        return browseItems;
    }

    /**
     * Called by the Spring MVC controller to create a new child namespace URI for the repository.
     * 
     * @param baseNamespace the base namespace from which the extension should be created
     * @param nsExtension the child namespace extension to create from the base
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping({"/createNamespace.html", "/createNamespace.htm"})
    public String createNamespace(@RequestParam(value = BASE_NAMESPACE, required = true) String baseNamespace,
        @RequestParam(value = "nsExtension", required = false) String nsExtension, HttpSession session, Model model,
        RedirectAttributes redirectAttrs) {
        String targetPage = null;

        if ((nsExtension != null) && (nsExtension.length() > 0)) {
            String newNS = RepositoryNamespaceUtils.appendChildPath( baseNamespace, nsExtension );
            try {
                getRepositoryManager().createNamespace( newNS );
                model.asMap().clear();
                setStatusMessage( "Namespace created successfully.", redirectAttrs );
                redirectAttrs.addAttribute( BASE_NAMESPACE, baseNamespace );
                targetPage = REDIRECT_BROWSE;

            } catch (RepositoryException e) {
                log.error( "Error creating namespace: " + newNS, e );
                setErrorMessage( e.getMessage(), model );
            }
        }

        if (targetPage == null) {
            model.addAttribute( BASE_NAMESPACE, baseNamespace );
            model.addAttribute( "nsExtension", nsExtension );
            targetPage = applyCommonValues( model, "createNamespace" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to create a new child namespace URI for the repository.
     * 
     * @param baseNamespace the base namespace from which the extension should be created
     * @param confirmDelete flag indicating whether the user has confirmed the deletion
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping({"/deleteNamespace.html", "/deleteNamespace.htm"})
    public String deleteNamespace(@RequestParam(value = BASE_NAMESPACE, required = true) String baseNamespace,
        @RequestParam(value = "confirmDelete", required = false) boolean confirmDelete, HttpSession session,
        Model model, RedirectAttributes redirectAttrs) {
        String targetPage = null;

        if (confirmDelete) {
            try {
                String parentNS = RepositoryNamespaceUtils.getParentNamespace( baseNamespace, getRepositoryManager() );

                getRepositoryManager().deleteNamespace( baseNamespace );
                model.asMap().clear();
                setStatusMessage( "Namespace deleted successfully.", redirectAttrs );
                redirectAttrs.addAttribute( BASE_NAMESPACE, parentNS );
                targetPage = REDIRECT_BROWSE;

            } catch (RepositoryException e) {
                log.error( "Error deleting namespace: " + baseNamespace, e );
                setErrorMessage( e.getMessage(), model );
            }
        }

        if (targetPage == null) {
            model.addAttribute( BASE_NAMESPACE, baseNamespace );
            targetPage = applyCommonValues( model, "deleteNamespace" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the application browse page.
     * 
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping({"/lockedLibraries.html", "/lockedLibraries.htm"})
    public String lockedLibraries(HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        try {
            UserPrincipal user = getCurrentUser( session );
            List<LibrarySearchResult> lockedLibraries;

            if ((user != null) && (user != UserPrincipal.ANONYMOUS_USER)) {
                FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
                lockedLibraries = searchService.getLockedLibraries( user.getUserId(), false );

            } else {
                setErrorMessage( "You must login in order to view your locked libraries.", redirectAttrs );
                return REDIRECT_INDEX;
            }
            model.addAttribute( "lockedLibraries", lockedLibraries );

        } catch (Exception e) {
            log.error( "An error occured while displaying the locked libraries page.", e );
            setErrorMessage( ERROR_DISPLAYING_PAGE, model );
        }
        return applyCommonValues( model, "lockedLibraries" );
    }

    /**
     * Called by the Spring MVC controller to display a consolidated list of subscriptions for the current user.
     * 
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping({"/subscriptions.html", "/subscriptions.htm"})
    public String viewSubscriptions(HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        try {
            UserPrincipal user = getCurrentUser( session );
            List<SubscriptionSearchResult> subscriptions;

            if ((user != null) && (user != UserPrincipal.ANONYMOUS_USER)) {
                FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
                subscriptions = searchService.getSubscriptions( user.getUserId() );
            } else {
                setErrorMessage( "You must login in order to view your subscriptions.", redirectAttrs );
                return REDIRECT_INDEX;
            }
            model.addAttribute( "user", user );
            model.addAttribute( "subscriptions", subscriptions );

        } catch (Exception e) {
            log.error( "An error occured while displaying the subscriptions page.", e );
            setErrorMessage( ERROR_DISPLAYING_PAGE, model );
        }
        return applyCommonValues( model, "subscriptions" );
    }

    /**
     * Called by the Spring MVC controller to display the page used to edit a user's subscription to namespace events.
     * 
     * @param baseNamespace the base namespace for which to edit the user's subscription
     * @param cancelToSubscriptionPage flag indicating if a cancel should be directed to the user's subscription page
     * @param etLibraryPublish flag indicating if the user has subscribed to the publish event
     * @param etLibraryNewVersion flag indicating if the user has subscribed to the new-version event
     * @param etLibraryStatusChange flag indicating if the user has subscribed to the status-change event
     * @param etLibraryStateChange flag indicating if the user has subscribed to the state-change event
     * @param etLibraryCommit flag indicating if the user has subscribed to the commit event
     * @param etLibraryMoveOrRename flag indicating if the user has subscribed to the move or rename event
     * @param etNamespaceAction flag indicating if the user has subscribed to the namespace-action event
     * @param updateSubscription flag indicating if the subscription updates should be applied
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping(value = {"/namespaceSubscription.html", "/namespaceSubscription.htm"})
    public String namespaceSubscription(@RequestParam(value = BASE_NAMESPACE, required = false) String baseNamespace,
        @RequestParam(value = "cts", required = false) boolean cancelToSubscriptionPage,
        @RequestParam(value = "etLibraryPublish", required = false) boolean etLibraryPublish,
        @RequestParam(value = "etLibraryNewVersion", required = false) boolean etLibraryNewVersion,
        @RequestParam(value = "etLibraryStatusChange", required = false) boolean etLibraryStatusChange,
        @RequestParam(value = "etLibraryStateChange", required = false) boolean etLibraryStateChange,
        @RequestParam(value = "etLibraryCommit", required = false) boolean etLibraryCommit,
        @RequestParam(value = "etLibraryMoveOrRename", required = false) boolean etLibraryMoveOrRename,
        @RequestParam(value = "etNamespaceAction", required = false) boolean etNamespaceAction,
        @RequestParam(value = "updateSubscription", required = false) boolean updateSubscription, HttpSession session,
        Model model, RedirectAttributes redirectAttrs) {
        String targetPage = "namespaceSubscription";
        boolean success = false;
        try {
            SubscriptionManager subscriptionManager = RepositoryComponentFactory.getDefault().getSubscriptionManager();
            UserPrincipal currentUser = (UserPrincipal) session.getAttribute( "user" );

            baseNamespace = nullifyBlank( baseNamespace );

            if (currentUser == null) {
                setErrorMessage( "You must be logged in to edit your subscription settings.", model );

            } else if (baseNamespace == null) {
                setErrorMessage( "Unable to edit subscription settings - namespace not specified.", model );
                targetPage = new BrowseController().browsePage( null, null, session, model );

            } else if (updateSubscription) {
                List<SubscriptionEventType> eventTypes = new ArrayList<>();

                addIfSelected( etLibraryPublish, SubscriptionEventType.LIBRARY_PUBLISH, eventTypes );
                addIfSelected( etLibraryNewVersion, SubscriptionEventType.LIBRARY_NEW_VERSION, eventTypes );
                addIfSelected( etLibraryStatusChange, SubscriptionEventType.LIBRARY_STATUS_CHANGE, eventTypes );
                addIfSelected( etLibraryStateChange, SubscriptionEventType.LIBRARY_STATE_CHANGE, eventTypes );
                addIfSelected( etLibraryCommit, SubscriptionEventType.LIBRARY_COMMIT, eventTypes );
                addIfSelected( etLibraryMoveOrRename, SubscriptionEventType.LIBRARY_MOVE_OR_RENAME, eventTypes );
                addIfSelected( etNamespaceAction, SubscriptionEventType.NAMESPACE_ACTION, eventTypes );
                subscriptionManager.updateNamespaceSubscriptions( baseNamespace, currentUser.getUserId(), eventTypes );
                success = true;

            } else {
                List<SubscriptionEventType> eventTypes =
                    subscriptionManager.getNamespaceSubscriptions( baseNamespace, currentUser.getUserId() );

                etLibraryPublish = eventTypes.contains( SubscriptionEventType.LIBRARY_PUBLISH );
                etLibraryNewVersion = eventTypes.contains( SubscriptionEventType.LIBRARY_NEW_VERSION );
                etLibraryStatusChange = eventTypes.contains( SubscriptionEventType.LIBRARY_STATUS_CHANGE );
                etLibraryStateChange = eventTypes.contains( SubscriptionEventType.LIBRARY_STATE_CHANGE );
                etLibraryCommit = eventTypes.contains( SubscriptionEventType.LIBRARY_COMMIT );
                etLibraryMoveOrRename = eventTypes.contains( SubscriptionEventType.LIBRARY_MOVE_OR_RENAME );
                etNamespaceAction = eventTypes.contains( SubscriptionEventType.NAMESPACE_ACTION );
            }
            model.addAttribute( BASE_NAMESPACE, baseNamespace );
            model.addAttribute( "cts", cancelToSubscriptionPage );
            model.addAttribute( "etLibraryPublish", etLibraryPublish );
            model.addAttribute( "etLibraryNewVersion", etLibraryNewVersion );
            model.addAttribute( "etLibraryStatusChange", etLibraryStatusChange );
            model.addAttribute( "etLibraryStateChange", etLibraryStateChange );
            model.addAttribute( "etLibraryCommit", etLibraryCommit );
            model.addAttribute( "etLibraryMoveOrRename", etLibraryMoveOrRename );
            model.addAttribute( "etNamespaceAction", etNamespaceAction );

        } catch (RepositoryException e) {
            setErrorMessage(
                "Error updating namespace subscription settings - please contact your system administrator.", model );
            log.error( "Error updating namespace subscription settings.", e );
        }
        if (success) {
            setStatusMessage( "Subscription settings updated successfully.", redirectAttrs );

            if (cancelToSubscriptionPage) {
                targetPage = redirectToSubscriptions();

            } else {
                redirectAttrs.addAttribute( BASE_NAMESPACE, baseNamespace );
                targetPage = REDIRECT_BROWSE;
            }

        } else {
            targetPage = applyCommonValues( model, targetPage );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the page used to edit a user's subscription to namespace events.
     * 
     * @param baseNamespace the base namespace for which to edit the user's subscription
     * @param libraryName the name of the library for which to edit subscriptions
     * @param version the version of the library for which to edit subscriptions
     * @param filename the name of the repository item file for which to edit subscriptions
     * @param allVersions flag indicating if the subscriptions for all library versions is being updated
     * @param etLibraryPublish flag indicating if the user has subscribed to the publish event
     * @param etLibraryNewVersion flag indicating if the user has subscribed to the new-version event
     * @param etLibraryStatusChange flag indicating if the user has subscribed to the status-change event
     * @param etLibraryStateChange flag indicating if the user has subscribed to the state-change event
     * @param etLibraryCommit flag indicating if the user has subscribed to the commit event
     * @param etLibraryMoveOrRename flag indicating if the user has subscribed to the move or rename event
     * @param updateSubscription flag indicating if the subscription updates should be applied
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping(value = {"/librarySubscription.html", "/librarySubscription.htm"})
    public String librarySubscription(@RequestParam(value = BASE_NAMESPACE, required = false) String baseNamespace,
        @RequestParam(value = "libraryName", required = false) String libraryName,
        @RequestParam(value = "version", required = false) String version,
        @RequestParam(value = FILENAME, required = false) String filename,
        @RequestParam(value = "allVersions", required = false) boolean allVersions,
        @RequestParam(value = "etLibraryPublish", required = false) boolean etLibraryPublish,
        @RequestParam(value = "etLibraryNewVersion", required = false) boolean etLibraryNewVersion,
        @RequestParam(value = "etLibraryStatusChange", required = false) boolean etLibraryStatusChange,
        @RequestParam(value = "etLibraryStateChange", required = false) boolean etLibraryStateChange,
        @RequestParam(value = "etLibraryCommit", required = false) boolean etLibraryCommit,
        @RequestParam(value = "etLibraryMoveOrRename", required = false) boolean etLibraryMoveOrRename,
        @RequestParam(value = "updateSubscription", required = false) boolean updateSubscription, HttpSession session,
        Model model, RedirectAttributes redirectAttrs) {
        String targetPage = "librarySubscription";
        boolean success = false;
        try {
            SubscriptionManager subscriptionManager = RepositoryComponentFactory.getDefault().getSubscriptionManager();
            SubscriptionTarget sTarget =
                SubscriptionManager.getSubscriptionTarget( baseNamespace, libraryName, version );
            UserPrincipal currentUser = (UserPrincipal) session.getAttribute( "user" );
            RepositoryItem item;

            baseNamespace = nullifyBlank( baseNamespace );
            libraryName = nullifyBlank( libraryName );
            filename = nullifyBlank( filename );
            version = nullifyBlank( version );
            item = findLibrary( baseNamespace, libraryName, version );

            sTarget.setBaseNamespace( baseNamespace );
            sTarget.setLibraryName( libraryName );
            sTarget.setVersion( version );

            String errorTarget =
                checkSubscriptionAccess( baseNamespace, libraryName, item, redirectAttrs, currentUser );

            if (errorTarget != null) {
                return errorTarget;
            }

            if (updateSubscription) {
                List<SubscriptionEventType> eventTypes = new ArrayList<>();

                addIfSelected( etLibraryPublish, SubscriptionEventType.LIBRARY_PUBLISH, eventTypes );
                addIfSelected( etLibraryNewVersion, SubscriptionEventType.LIBRARY_NEW_VERSION, eventTypes );
                addIfSelected( etLibraryStatusChange, SubscriptionEventType.LIBRARY_STATUS_CHANGE, eventTypes );
                addIfSelected( etLibraryStateChange, SubscriptionEventType.LIBRARY_STATE_CHANGE, eventTypes );
                addIfSelected( etLibraryCommit, SubscriptionEventType.LIBRARY_COMMIT, eventTypes );
                addIfSelected( etLibraryMoveOrRename, SubscriptionEventType.LIBRARY_MOVE_OR_RENAME, eventTypes );

                if (allVersions) {
                    subscriptionManager.updateAllVersionsSubscriptions( sTarget, currentUser.getUserId(), eventTypes );

                } else {
                    subscriptionManager.updateSingleVersionSubscriptions( sTarget, currentUser.getUserId(),
                        eventTypes );
                }
                success = true;

            } else {
                List<SubscriptionEventType> eventTypes;

                if (allVersions) {
                    eventTypes = subscriptionManager.getAllVersionsSubscriptions( sTarget, currentUser.getUserId() );

                } else {
                    eventTypes = subscriptionManager.getSingleVersionSubscriptions( sTarget, currentUser.getUserId() );
                }

                etLibraryPublish = eventTypes.contains( SubscriptionEventType.LIBRARY_PUBLISH );
                etLibraryNewVersion = eventTypes.contains( SubscriptionEventType.LIBRARY_NEW_VERSION );
                etLibraryStatusChange = eventTypes.contains( SubscriptionEventType.LIBRARY_STATUS_CHANGE );
                etLibraryStateChange = eventTypes.contains( SubscriptionEventType.LIBRARY_STATE_CHANGE );
                etLibraryCommit = eventTypes.contains( SubscriptionEventType.LIBRARY_COMMIT );
                etLibraryMoveOrRename = eventTypes.contains( SubscriptionEventType.LIBRARY_MOVE_OR_RENAME );
            }
            model.addAttribute( "item", item );
            model.addAttribute( BASE_NAMESPACE, baseNamespace );
            model.addAttribute( "libraryName", libraryName );
            model.addAttribute( "version", version );
            model.addAttribute( FILENAME, filename );
            model.addAttribute( "allVersions", allVersions );
            model.addAttribute( "etLibraryPublish", etLibraryPublish );
            model.addAttribute( "etLibraryNewVersion", etLibraryNewVersion );
            model.addAttribute( "etLibraryStatusChange", etLibraryStatusChange );
            model.addAttribute( "etLibraryStateChange", etLibraryStateChange );
            model.addAttribute( "etLibraryCommit", etLibraryCommit );
            model.addAttribute( "etLibraryMoveOrRename", etLibraryMoveOrRename );

        } catch (RepositoryException e) {
            setErrorMessage(
                "Error updating namespace subscription settings - please contact your system administrator.", model );
            log.error( "Error updating namespace subscription settings.", e );
        }
        if (success) {
            setStatusMessage( "Subscription settings updated successfully.", redirectAttrs );

            if (filename == null) { // redirect to subscriptions page
                targetPage = redirectToSubscriptions();

            } else {
                redirectAttrs.addAttribute( BASE_NAMESPACE, baseNamespace );
                redirectAttrs.addAttribute( FILENAME, filename );
                redirectAttrs.addAttribute( "version", version );
                targetPage = "redirect:/console/libraryInfo.html";
            }

        } else {
            targetPage = applyCommonValues( model, targetPage );
        }
        return targetPage;
    }

    /**
     * Checks the user's access to edit the subscription information.
     * 
     * @param baseNamespace the base namespace of the library to which the user is requesting subscription access (may
     *        be null)
     * @param libraryName the name of the library to which the user is requesting subscription access (may be null)
     * @param item the repository item to which subscription access is being requested (may be null)
     * @param redirectAttrs redirect attributes for the current request
     * @param currentUser the current user who is requesting access
     * @return String
     */
    private String checkSubscriptionAccess(String baseNamespace, String libraryName, RepositoryItem item,
        RedirectAttributes redirectAttrs, UserPrincipal currentUser) {
        String errorTarget = null;

        if ((currentUser == null) || (currentUser == UserPrincipal.ANONYMOUS_USER)) {
            setErrorMessage( "You must be logged in to edit your subscription settings.", redirectAttrs );
            errorTarget = REDIRECT_INDEX;

        } else if ((baseNamespace == null) || (libraryName == null)) {
            setErrorMessage( "Unable to edit subscription settings - library information not specified.",
                redirectAttrs );
            errorTarget = REDIRECT_INDEX;

        } else if (item == null) {
            setErrorMessage( "The library associated with this subscription does not exist", redirectAttrs );
            errorTarget = REDIRECT_SUBSCRIPTIONS;
        }
        return errorTarget;
    }

    /**
     * Adds the given event type to the list if it was selected by the user.
     * 
     * @param isSelected flag indicating whether the user selected the event
     * @param eventType the event type to add
     * @param eventTypes the list of event types being constructed
     */
    private void addIfSelected(boolean isSelected, SubscriptionEventType eventType,
        List<SubscriptionEventType> eventTypes) {
        if (isSelected) {
            eventTypes.add( eventType );
        }
    }

    /**
     * Wait for changes to propagage through the indexing process before redisplaying the subscriptions page. This is a
     * hack, but good enough for now.
     * 
     * @return String
     */
    private String redirectToSubscriptions() {
        try {
            Thread.sleep( 1000 );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return REDIRECT_SUBSCRIPTIONS;
    }

    /**
     * Returns the list of namespace items that represent the relative paths above the one specified. The list of items
     * is sorted from the root namespace to the lowest level child above the one provided.
     * 
     * @param ns the namespace URI for which to construct parent namespace items
     * @return List&lt;NamespaceItem&gt;
     * @throws RepositoryException thrown if the repository's root namespaces cannot be accessed or the given namespace
     *         URI is not part of the base namespace hierarchy from the associated repository
     */
    private List<NamespaceItem> getParentNamespaceItems(String ns) throws RepositoryException {
        try {
            String rootNS = RepositoryNamespaceUtils.getRootNamespace( ns, getRepositoryManager() );
            String pathFromRoot = rootNS.equals( ns ) ? null : ns.substring( rootNS.length() + 1 );
            List<NamespaceItem> parentItems = new ArrayList<>();
            StringBuilder nsPath = new StringBuilder( rootNS );

            parentItems.add( new NamespaceItem( rootNS ) );

            if ((pathFromRoot != null) && (pathFromRoot.length() > 0)) {
                for (String pathPart : pathFromRoot.split( "/" )) {
                    if (pathPart.length() == 0) {
                        continue;
                    }
                    nsPath.append( "/" ).append( pathPart );
                    parentItems.add( new NamespaceItem( nsPath.toString(), pathPart ) );
                }
            }
            return parentItems;

        } catch (IllegalArgumentException e) {
            throw new RepositoryException( e.getMessage(), e );
        }
    }

    /**
     * Attempts to create a <code>NamespaceItem</code> using an index document returned from the free-text search
     * service. If no search index document can be located, the item will be constructed directly from the
     * <code>RepositoryItem</code> provided.
     * 
     * @param item the repository item from which to construct the namespace item
     * @return NamespaceItem
     */
    private NamespaceItem createNamespaceItem(RepositoryItem item) throws RepositoryException {
        FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
        String itemIndexId = IndexingUtils.getIdentityKey( item );
        LibrarySearchResult indexItem = searchService.getLibrary( itemIndexId, false );
        NamespaceItem nsItem;

        if (indexItem != null) {
            nsItem = new NamespaceItem( indexItem );
        } else {
            // fallback if not returned from the search index
            nsItem = new NamespaceItem( item );
        }
        return nsItem;
    }

    /**
     * Returns null if the given string is null or empty.
     * 
     * @param str the string to be nullified in case of blank
     * @return String
     */
    private String nullifyBlank(String str) {
        return ((str != null) && (str.length() == 0)) ? null : str;
    }

    /**
     * Retrieves the <code>RepositoryItem</code> for the library with the given base namespace, library name, and
     * version. If the version parameter is null, the latest version of the library will be returned. If no matching
     * library exists, this method will return null.
     * 
     * @param baseNamespace the base namespace of the library to retrieve
     * @param libraryName the name of the library to retrieve
     * @param version the version of the library to retrieve (null for latest version)
     * @return RepositoryItem
     * @throws RepositoryException thrown if an error occurs while retrieving the library
     */
    private RepositoryItem findLibrary(String baseNamespace, String libraryName, String version)
        throws RepositoryException {
        RepositoryManager repositoryManager = getRepositoryManager();
        RepositoryItem itemVersion = null;

        if (version != null) {
            List<RepositoryItem> itemList = repositoryManager.listItems( baseNamespace, null, false );

            for (RepositoryItem item : itemList) {
                if (item.getLibraryName().equals( libraryName ) && item.getVersion().equals( version )) {
                    itemVersion = item;
                    break;
                }
            }

        } else {
            List<RepositoryItem> itemList = repositoryManager.listItems( baseNamespace, null, true );

            for (RepositoryItem item : itemList) {
                if (item.getLibraryName().equals( libraryName )) {
                    itemVersion = item;
                    break;
                }
            }
        }
        return itemVersion;
    }

}

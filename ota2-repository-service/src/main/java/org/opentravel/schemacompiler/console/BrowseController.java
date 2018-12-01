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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionEventType;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.index.FreeTextSearchService;
import org.opentravel.schemacompiler.index.FreeTextSearchServiceFactory;
import org.opentravel.schemacompiler.index.IndexingUtils;
import org.opentravel.schemacompiler.index.LibrarySearchResult;
import org.opentravel.schemacompiler.index.SubscriptionSearchResult;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.RepositoryNamespaceUtils;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.opentravel.schemacompiler.subscription.SubscriptionManager;
import org.opentravel.schemacompiler.util.PageUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller that handles interactions with the browse page of the OTA2.0 repository console.
 * 
 * @author S. Livezey
 */
@Controller
public class BrowseController extends BaseController {

    private static Log log = LogFactory.getLog(BrowseController.class);
    
    /**
     * Called by the Spring MVC controller to display the application browse page.
     * 
     * @param baseNamespace  the root namespace parameter from the URL
     * @param path  the sub-namespace path relative to the base namespace
     * @param filename  the filename of the selected library
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({ "/browse.html", "/browse.htm" })
    public String browsePage(
            @RequestParam(value = "baseNamespace", required = false) String baseNamespace,
            @RequestParam(value = "filename", required = false) String filename,
            HttpSession session, Model model) {
        try {
            RepositoryManager repositoryManager = getRepositoryManager();
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser(session);
            List<NamespaceItem> browseItems = new ArrayList<NamespaceItem>();

            if (baseNamespace == null) { // display root namespaces
                for (String rootNS : repositoryManager.listRootNamespaces()) {
                    try {
                        if (securityManager.isAuthorized(user, rootNS,
                                RepositoryPermission.READ_FINAL)) {
                            browseItems.add(new NamespaceItem(rootNS));
                        }

                    } catch (Throwable t) {
                        log.warn("Error determining user access to root namespace: " + rootNS);
                    }
                }
            } else if (filename != null) { // display all versions of the selected library or release
                List<RepositoryItem> allItems = repositoryManager.listItems(baseNamespace, TLLibraryStatus.DRAFT, false, null);

                for (RepositoryItem item : allItems) {
                    if (item.getFilename().equals(filename)) {
                        List<RepositoryItem> versionHistory = repositoryManager.getVersionHistory(item);

                        for (RepositoryItem itemVersion : versionHistory) {
                            if (securityManager.isReadAuthorized(user, itemVersion)) {
                            	browseItems.add( createNamespaceItem( itemVersion ) );
                            }
                        }
                        model.addAttribute("libraryName", item.getLibraryName());
                    }
                }

            } else { // display sub-namespaces and latest version of each library
                List<String> nsChildren = repositoryManager.listNamespaceChildren(baseNamespace);
                List<RepositoryItem> itemList = repositoryManager.listItems(baseNamespace, TLLibraryStatus.DRAFT, true, null);

                for (String childPath : nsChildren) {
                    String childNS = RepositoryNamespaceUtils.appendChildPath(baseNamespace,
                            childPath);

                    if (securityManager.isAuthorized(user, childNS, RepositoryPermission.READ_FINAL)) {
                        browseItems.add(new NamespaceItem(childNS, childPath));
                    }
                }

                for (RepositoryItem item : itemList) {
                    if (securityManager.isReadAuthorized(user, item)) {
                    	browseItems.add( createNamespaceItem( item ) );
                    }
                }
            }

            if (baseNamespace != null) {
            	SubscriptionManager subscriptionManager = RepositoryComponentFactory.getDefault().getSubscriptionManager();
            	
                model.addAttribute("parentItems", getParentNamespaceItems(baseNamespace));
                model.addAttribute("canCreateNamespaceExtension",
                		securityManager.isAuthorized( user, baseNamespace, RepositoryPermission.WRITE));
                model.addAttribute("canEditSubscription", (user != UserPrincipal.ANONYMOUS_USER));
                model.addAttribute("hasSubscription",
                		!subscriptionManager.getNamespaceSubscriptions(baseNamespace, user.getUserId()).isEmpty() );

                if (!repositoryManager.listRootNamespaces().contains(baseNamespace)
                        && repositoryManager.listNamespaceChildren(baseNamespace).isEmpty()
                        && repositoryManager.listItems(baseNamespace, false, false).isEmpty()) {
                    String parentNS = RepositoryNamespaceUtils.getParentNamespace(baseNamespace, repositoryManager);

                    model.addAttribute("canDeleteNamespace",
                    		securityManager.isAuthorized(user, parentNS, RepositoryPermission.WRITE));
                }
            }
            model.addAttribute("imageResolver", new SearchResultImageResolver());
            model.addAttribute("pageUtils", new PageUtils());
            model.addAttribute("baseNamespace", baseNamespace);
            model.addAttribute("filename", filename);
            model.addAttribute("browseItems", browseItems);

        } catch (Throwable t) {
            log.error("An error occured while displaying the browse page.", t);
            setErrorMessage("An error occured while displaying the page (see server log for details).", model);
        }
        return applyCommonValues(model, "browse");
    }

    /**
     * Called by the Spring MVC controller to create a new child namespace URI for the repository.
     * 
     * @param baseNamespace  the base namespace from which the extension should be created
     * @param nsExtension  the child namespace extension to create from the base
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @param redirectAttrs  request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping({ "/createNamespace.html", "/createNamespace.htm" })
    public String createNamespace(
            @RequestParam(value = "baseNamespace", required = true) String baseNamespace,
            @RequestParam(value = "nsExtension", required = false) String nsExtension,
            HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        String targetPage = null;

        if ((nsExtension != null) && (nsExtension.length() > 0)) {
            String newNS = RepositoryNamespaceUtils.appendChildPath(baseNamespace, nsExtension);
            try {
                getRepositoryManager().createNamespace(newNS);
				model.asMap().clear();
                setStatusMessage("Namespace created successfully.", redirectAttrs);
				redirectAttrs.addAttribute( "baseNamespace", baseNamespace );
                targetPage = "redirect:/console/browse.html";

            } catch (RepositoryException e) {
                log.error("Error creating namespace: " + newNS, e);
                setErrorMessage(e.getMessage(), model);
            }
        }

        if (targetPage == null) {
            model.addAttribute("baseNamespace", baseNamespace);
            model.addAttribute("nsExtension", nsExtension);
            targetPage = applyCommonValues(model, "createNamespace");
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to create a new child namespace URI for the repository.
     * 
     * @param baseNamespace  the base namespace from which the extension should be created
     * @param nsExtension  the child namespace extension to create from the base
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @param redirectAttrs  request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping({ "/deleteNamespace.html", "/deleteNamespace.htm" })
    public String deleteNamespace(
            @RequestParam(value = "baseNamespace", required = true) String baseNamespace,
            @RequestParam(value = "confirmDelete", required = false) boolean confirmDelete,
            HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        String targetPage = null;

        if (confirmDelete) {
            try {
                String parentNS = RepositoryNamespaceUtils.getParentNamespace(baseNamespace,
                        getRepositoryManager());

                getRepositoryManager().deleteNamespace(baseNamespace);
				model.asMap().clear();
                setStatusMessage("Namespace deleted successfully.", redirectAttrs);
				redirectAttrs.addAttribute( "baseNamespace", parentNS );
                targetPage = "redirect:/console/browse.html";

            } catch (RepositoryException e) {
                log.error("Error deleting namespace: " + baseNamespace, e);
                setErrorMessage(e.getMessage(), model);
            }
        }

        if (targetPage == null) {
            model.addAttribute("baseNamespace", baseNamespace);
            targetPage = applyCommonValues(model, "deleteNamespace");
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the application browse page.
     * 
     * @param baseNamespace  the root namespace parameter from the URL
     * @param path  the sub-namespace path relative to the base namespace
     * @param filename  the filename of the selected library
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @param redirectAttrs  request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping({ "/lockedLibraries.html", "/lockedLibraries.htm" })
    public String lockedLibraries(HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        try {
            UserPrincipal user = getCurrentUser(session);
        	List<LibrarySearchResult> lockedLibraries;
        	
            if ((user != null) && (user != UserPrincipal.ANONYMOUS_USER)) {
            	FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
            	lockedLibraries = searchService.getLockedLibraries( user.getUserId(), false );
            	
            } else {
            	setErrorMessage( "You must login in order to view your locked libraries.", redirectAttrs );
            	return "redirect:/console/index.html";
            }
            model.addAttribute("lockedLibraries", lockedLibraries);
            
        } catch (Throwable t) {
            log.error("An error occured while displaying the locked libraries page.", t);
            setErrorMessage("An error occured while displaying the page (see server log for details).", model);
        }
        return applyCommonValues(model, "lockedLibraries");
    }

    /**
     * Called by the Spring MVC controller to display a consolidated list of subscriptions
     * for the current user.
     * 
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @param redirectAttrs  request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping({ "/subscriptions.html", "/subscriptions.htm" })
    public String viewSubscriptions(HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        try {
            UserPrincipal user = getCurrentUser(session);
        	List<SubscriptionSearchResult> subscriptions;
        	
            if ((user != null) && (user != UserPrincipal.ANONYMOUS_USER)) {
            	FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
            	subscriptions = searchService.getSubscriptions( user.getUserId() );
            } else {
            	setErrorMessage( "You must login in order to view your subscriptions.", redirectAttrs );
            	return "redirect:/console/index.html";
            }
            model.addAttribute("user", user);
            model.addAttribute("subscriptions", subscriptions);
            
        } catch (Throwable t) {
            log.error("An error occured while displaying the subscriptions page.", t);
            setErrorMessage("An error occured while displaying the page (see server log for details).", model);
        }
        return applyCommonValues(model, "subscriptions");
    }

    /**
     * Called by the Spring MVC controller to display the page used to edit a user's subscription
     * to namespace events.
     * 
     * @param ns  the namespace for which to edit the user's subscription
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @param redirectAttrs  request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping(value = { "/namespaceSubscription.html", "/namespaceSubscription.htm" })
    public String namespaceSubscription(
            @RequestParam(value = "baseNamespace", required = false) String baseNamespace,
            @RequestParam(value = "cts", required = false) boolean cancelToSubscriptionPage,
            @RequestParam(value = "etLibraryPublish", required = false) boolean etLibraryPublish,
            @RequestParam(value = "etLibraryNewVersion", required = false) boolean etLibraryNewVersion,
            @RequestParam(value = "etLibraryStatusChange", required = false) boolean etLibraryStatusChange,
            @RequestParam(value = "etLibraryStateChange", required = false) boolean etLibraryStateChange,
            @RequestParam(value = "etLibraryCommit", required = false) boolean etLibraryCommit,
            @RequestParam(value = "etLibraryMoveOrRename", required = false) boolean etLibraryMoveOrRename,
            @RequestParam(value = "etNamespaceAction", required = false) boolean etNamespaceAction,
            @RequestParam(value = "updateSubscription", required = false) boolean updateSubscription,
            HttpSession session, Model model, RedirectAttributes redirectAttrs) {
    	String targetPage = "namespaceSubscription";
        boolean success = false;
        try {
        	SubscriptionManager subscriptionManager = RepositoryComponentFactory.getDefault().getSubscriptionManager();
            UserPrincipal currentUser = (UserPrincipal) session.getAttribute("user");
            
        	baseNamespace = nullifyBlank( baseNamespace );
        	
            if (currentUser == null) {
                setErrorMessage("You must be logged in to edit your subscription settings.", model);
                
            } else if (baseNamespace == null) {
                setErrorMessage("Unable to edit subscription settings - namespace not specified.", model);
                targetPage = new BrowseController().browsePage(null, null, session, model);
                
            } else if (updateSubscription) {
            	List<SubscriptionEventType> eventTypes = new ArrayList<>();
            	
                if (etLibraryPublish) eventTypes.add( SubscriptionEventType.LIBRARY_PUBLISH );
                if (etLibraryNewVersion) eventTypes.add( SubscriptionEventType.LIBRARY_NEW_VERSION );
                if (etLibraryStatusChange) eventTypes.add( SubscriptionEventType.LIBRARY_STATUS_CHANGE );
                if (etLibraryStateChange) eventTypes.add( SubscriptionEventType.LIBRARY_STATE_CHANGE );
                if (etLibraryCommit) eventTypes.add( SubscriptionEventType.LIBRARY_COMMIT );
                if (etLibraryMoveOrRename) eventTypes.add( SubscriptionEventType.LIBRARY_MOVE_OR_RENAME );
                if (etNamespaceAction) eventTypes.add( SubscriptionEventType.NAMESPACE_ACTION );
                subscriptionManager.updateNamespaceSubscriptions(
                		baseNamespace, currentUser.getUserId(), eventTypes );
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
            model.addAttribute("baseNamespace", baseNamespace);
            model.addAttribute("cts", cancelToSubscriptionPage);
            model.addAttribute("etLibraryPublish", etLibraryPublish);
            model.addAttribute("etLibraryNewVersion", etLibraryNewVersion);
            model.addAttribute("etLibraryStatusChange", etLibraryStatusChange);
            model.addAttribute("etLibraryStateChange", etLibraryStateChange);
            model.addAttribute("etLibraryCommit", etLibraryCommit);
            model.addAttribute("etLibraryMoveOrRename", etLibraryMoveOrRename);
            model.addAttribute("etNamespaceAction", etNamespaceAction);
            
        } catch (RepositoryException e) {
            setErrorMessage("Error updating namespace subscription settings - please contact your system administrator.", model);
            log.error("Error updating namespace subscription settings.", e);
        }
        if (success) {
        	setStatusMessage("Subscription settings updated successfully.", redirectAttrs);
        	
        	if (cancelToSubscriptionPage) {
        		// Wait for changes to propagage through the indexing process before redisplaying
        		// the subscriptions page.  This is a hack, but good enough for now.
        		try {
        			Thread.sleep(1000);
        			
        		} catch (InterruptedException e) {
        			Thread.currentThread().interrupt();
        		}
                targetPage = "redirect:/console/subscriptions.html";
                
        	} else {
        		redirectAttrs.addAttribute("baseNamespace", baseNamespace);
        		targetPage = "redirect:/console/browse.html";
        	}
        	
        } else {
        	targetPage = applyCommonValues(model, targetPage);
        }
        return targetPage;
    }
    
    /**
     * Called by the Spring MVC controller to display the page used to edit a user's subscription
     * to namespace events.
     * 
     * @param ns  the namespace for which to edit the user's subscription
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @param redirectAttrs  request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping(value = { "/librarySubscription.html", "/librarySubscription.htm" })
    public String librarySubscription(
            @RequestParam(value = "baseNamespace", required = false) String baseNamespace,
            @RequestParam(value = "libraryName", required = false) String libraryName,
            @RequestParam(value = "version", required = false) String version,
            @RequestParam(value = "filename", required = false) String filename,
            @RequestParam(value = "allVersions", required = false) boolean allVersions,
            @RequestParam(value = "etLibraryPublish", required = false) boolean etLibraryPublish,
            @RequestParam(value = "etLibraryNewVersion", required = false) boolean etLibraryNewVersion,
            @RequestParam(value = "etLibraryStatusChange", required = false) boolean etLibraryStatusChange,
            @RequestParam(value = "etLibraryStateChange", required = false) boolean etLibraryStateChange,
            @RequestParam(value = "etLibraryCommit", required = false) boolean etLibraryCommit,
            @RequestParam(value = "etLibraryMoveOrRename", required = false) boolean etLibraryMoveOrRename,
            @RequestParam(value = "updateSubscription", required = false) boolean updateSubscription,
            HttpSession session, Model model, RedirectAttributes redirectAttrs) {
    	String targetPage = "librarySubscription";
        boolean success = false;
        try {
        	SubscriptionManager subscriptionManager = RepositoryComponentFactory.getDefault().getSubscriptionManager();
        	SubscriptionTarget sTarget = SubscriptionManager.getSubscriptionTarget( baseNamespace, libraryName, version );
            UserPrincipal currentUser = (UserPrincipal) session.getAttribute("user");
        	RepositoryItem item;
        	
        	baseNamespace = nullifyBlank( baseNamespace );
        	libraryName = nullifyBlank( libraryName );
        	filename = nullifyBlank( filename );
        	version = nullifyBlank( version );
        	item = findLibrary( baseNamespace, libraryName, version );
        	
        	sTarget.setBaseNamespace( baseNamespace );
        	sTarget.setLibraryName( libraryName );
        	sTarget.setVersion( version );
            
        	if ((currentUser == null) || (currentUser == UserPrincipal.ANONYMOUS_USER)) {
                setErrorMessage("You must be logged in to edit your subscription settings.", redirectAttrs);
                return "redirect:/console/index.html";
                
            } else if ((baseNamespace == null) || (libraryName == null)) {
                setErrorMessage("Unable to edit subscription settings - library information not specified.", redirectAttrs);
                return "redirect:/console/index.html";
                
            } else if (item == null) {
                setErrorMessage("The library associated with this subscription does not exist", redirectAttrs);
                return "redirect:/console/subscriptions.html";
            	
            } else if (updateSubscription) {
            	List<SubscriptionEventType> eventTypes = new ArrayList<>();
            	
                if (etLibraryPublish) eventTypes.add( SubscriptionEventType.LIBRARY_PUBLISH );
                if (etLibraryNewVersion) eventTypes.add( SubscriptionEventType.LIBRARY_NEW_VERSION );
                if (etLibraryStatusChange) eventTypes.add( SubscriptionEventType.LIBRARY_STATUS_CHANGE );
                if (etLibraryStateChange) eventTypes.add( SubscriptionEventType.LIBRARY_STATE_CHANGE );
                if (etLibraryCommit) eventTypes.add( SubscriptionEventType.LIBRARY_COMMIT );
                if (etLibraryMoveOrRename) eventTypes.add( SubscriptionEventType.LIBRARY_MOVE_OR_RENAME );
                
                if (allVersions) {
                    subscriptionManager.updateAllVersionsSubscriptions( sTarget, currentUser.getUserId(), eventTypes );
                	
                } else {
                    subscriptionManager.updateSingleVersionSubscriptions( sTarget, currentUser.getUserId(), eventTypes );
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
            model.addAttribute("item", item);
            model.addAttribute("baseNamespace", baseNamespace);
            model.addAttribute("libraryName", libraryName);
            model.addAttribute("version", version);
            model.addAttribute("filename", filename);
            model.addAttribute("allVersions", allVersions);
            model.addAttribute("etLibraryPublish", etLibraryPublish);
            model.addAttribute("etLibraryNewVersion", etLibraryNewVersion);
            model.addAttribute("etLibraryStatusChange", etLibraryStatusChange);
            model.addAttribute("etLibraryStateChange", etLibraryStateChange);
            model.addAttribute("etLibraryCommit", etLibraryCommit);
            model.addAttribute("etLibraryMoveOrRename", etLibraryMoveOrRename);
            
        } catch (RepositoryException e) {
            setErrorMessage("Error updating namespace subscription settings - please contact your system administrator.", model);
            log.error("Error updating namespace subscription settings.", e);
        }
        if (success) {
        	setStatusMessage("Subscription settings updated successfully.", redirectAttrs);
        	
        	if (filename == null) { // redirect to subscriptions page
        		// Wait for changes to propagage through the indexing process before redisplaying
        		// the subscriptions page.  This is a hack, but good enough for now.
        		try {
        			Thread.sleep(1000);
        			
        		} catch (InterruptedException e) {
        			Thread.currentThread().interrupt();
        		}
                targetPage = "redirect:/console/subscriptions.html";
        		
        	} else {
            	redirectAttrs.addAttribute("baseNamespace", baseNamespace);
            	redirectAttrs.addAttribute("filename", filename);
            	redirectAttrs.addAttribute("version", version);
            	targetPage = "redirect:/console/libraryInfo.html";
        	}
        	
        } else {
        	targetPage = applyCommonValues(model, targetPage);
        }
        return targetPage;
    }
    
    /**
     * Returns the list of namespace items that represent the relative paths above the one
     * specified. The list of items is sorted from the root namespace to the lowest level child
     * above the one provided.
     * 
     * @param ns  the namespace URI for which to construct parent namespace items
     * @return List<NamespaceItem>
     * @throws RepositoryException
     *             thrown if the repository's root namespaces cannot be accessed or the given
     *             namespace URI is not part of the base namespace hierarchy from the associated
     *             repository
     */
    private List<NamespaceItem> getParentNamespaceItems(String ns) throws RepositoryException {
        try {
            String rootNS = RepositoryNamespaceUtils.getRootNamespace(ns, getRepositoryManager());
            String pathFromRoot = rootNS.equals(ns) ? null : ns.substring(rootNS.length() + 1);
            List<NamespaceItem> parentItems = new ArrayList<NamespaceItem>();
            StringBuilder nsPath = new StringBuilder(rootNS);

            parentItems.add(new NamespaceItem(rootNS));

            if ((pathFromRoot != null) && (pathFromRoot.length() > 0)) {
                for (String pathPart : pathFromRoot.split("/")) {
                    if (pathPart.length() == 0)
                        continue;
                    nsPath.append("/").append(pathPart);
                    parentItems.add(new NamespaceItem(nsPath.toString(), pathPart));
                }
            }
            return parentItems;

        } catch (IllegalArgumentException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }
    
    /**
     * Attempts to create a <code>NamespaceItem</code> using an index document returned from the
     * free-text search service.  If no search index document can be located, the item will be constructed
     * directly from the <code>RepositoryItem</code> provided.
     * 
     * @param item  the repository item from which to construct the namespace item
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
     * @param str  the string to be nullified in case of blank
     * @return String
     */
    private String nullifyBlank(String str) {
    	return ((str != null) && (str.length() == 0)) ? null : str;
    }
    
    /**
     * Retrieves the <code>RepositoryItem</code> for the library with the given base namespace,
     * library name, and version.  If the version parameter is null, the latest version of the
     * library will be returned.  If no matching library exists, this method will return null.
     * 
     * @param baseNamespace  the base namespace of the library to retrieve
     * @param libraryName  the name of the library to retrieve
     * @param version  the version of the library to retrieve (null for latest version)
     * @return RepositoryItem
     * @throws RepositoryException  thrown if an error occurs while retrieving the library
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

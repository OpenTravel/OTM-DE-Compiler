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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.schemacompiler.index.EntitySearchResult;
import org.opentravel.schemacompiler.index.FreeTextSearchService;
import org.opentravel.schemacompiler.index.FreeTextSearchServiceFactory;
import org.opentravel.schemacompiler.index.IndexingUtils;
import org.opentravel.schemacompiler.index.LibrarySearchResult;
import org.opentravel.schemacompiler.index.ValidationResult;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller that handles interactions with the view item page(s) of the OTA2.0 repository console.
 * 
 * @author S. Livezey
 */
@Controller
public class ViewItemController extends BaseController {

    private static Log log = LogFactory.getLog(BrowseController.class);

    /**
     * Called by the Spring MVC controller to display the general-information library
     * page.
     * 
     * @param rootNamespace  the root namespace of the selected library
     * @param path  the sub-namespace path relative to the base namespace
     * @param filename  the filename of the selected library to view
     * @param version  the version of the selected library to view
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({ "/libraryDictionary.html", "/libraryDictionary.htm" })
    public String libraryDictionary(@RequestParam(value = "baseNamespace") String baseNamespace,
            @RequestParam(value = "filename") String filename,
            @RequestParam(value = "version") String version, HttpSession session, Model model) {
        String targetPage = null;
        try {
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser(session);
            RepositoryItem item = getRepositoryManager().getRepositoryItem(baseNamespace, filename, version);

            if (securityManager.isReadAuthorized(user, item)) {
            	FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
            	String libraryIndexId = IndexingUtils.getIdentityKey( item );
            	List<EntitySearchResult> entityList = searchService.getEntities( libraryIndexId, false );
            	
            	Collections.sort( entityList, new Comparator<EntitySearchResult>() {
					public int compare(EntitySearchResult entity1, EntitySearchResult entity2) {
						return entity1.getItemName().compareTo( entity2.getItemName() );
					}
            	});
                model.addAttribute("imageResolver", new SearchResultImageResolver());
                model.addAttribute("entityList", entityList);
                model.addAttribute("item", item);
            	
            	
            } else {
                setErrorMessage("You are not authorized to view the requested library.", model);
                targetPage = new SearchController().searchPage(null, false, false, session, model);
            }

        } catch (Throwable t) {
            log.error("An error occured while displaying the library.", t);
            setErrorMessage(
                    "An error occured while displaying the library (see server log for details).",
                    model);
        }

        if (targetPage == null) {
            targetPage = applyCommonValues(model, "libraryDictionary");
        }
        return targetPage;
    }
    
    /**
     * Called by the Spring MVC controller to display the general-information library
     * page.
     * 
     * @param rootNamespace  the root namespace of the selected library
     * @param path  the sub-namespace path relative to the base namespace
     * @param filename  the filename of the selected library to view
     * @param version  the version of the selected library to view
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({ "/libraryUsage.html", "/libraryUsage.htm" })
    public String libraryUsage(@RequestParam(value = "baseNamespace") String baseNamespace,
            @RequestParam(value = "filename") String filename,
            @RequestParam(value = "version") String version, HttpSession session, Model model) {
        String targetPage = null;
        try {
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser(session);
            RepositoryItem item = getRepositoryManager().getRepositoryItem(baseNamespace, filename, version);

            if (securityManager.isReadAuthorized(user, item)) {
                FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
                String indexItemId = IndexingUtils.getIdentityKey( item );
                LibrarySearchResult indexItem = searchService.getLibrary( indexItemId, false );
                List<LibrarySearchResult> usesLibraries = searchService.getLibraries( indexItem.getReferencedLibraryIds(), false );
                List<LibrarySearchResult> directWhereUsed = searchService.getLibraryWhereUsed( indexItem, false, false );
                List<LibrarySearchResult> indirectWhereUsed = searchService.getLibraryWhereUsed( indexItem, true, false );
                
                purgeDirectWhereUsed( indirectWhereUsed, directWhereUsed );
                
                model.addAttribute("usesLibraries", usesLibraries);
                model.addAttribute("directWhereUsed", directWhereUsed);
                model.addAttribute("indirectWhereUsed", indirectWhereUsed);
                model.addAttribute("item", item);
            	
            } else {
                setErrorMessage("You are not authorized to view the requested library.", model);
                targetPage = new SearchController().searchPage(null, false, false, session, model);
            }

        } catch (Throwable t) {
            log.error("An error occured while displaying the library.", t);
            setErrorMessage(
                    "An error occured while displaying the library (see server log for details).",
                    model);
        }

        if (targetPage == null) {
            targetPage = applyCommonValues(model, "libraryUsage");
        }
        return targetPage;
    }
    
    /**
     * Called by the Spring MVC controller to display the general-information library
     * page.
     * 
     * @param rootNamespace  the root namespace of the selected library
     * @param path  the sub-namespace path relative to the base namespace
     * @param filename  the filename of the selected library to view
     * @param version  the version of the selected library to view
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({ "/libraryValidation.html", "/libraryValidation.htm" })
    public String libraryValidation(@RequestParam(value = "baseNamespace") String baseNamespace,
            @RequestParam(value = "filename") String filename,
            @RequestParam(value = "version") String version, HttpSession session, Model model) {
        String targetPage = null;
        try {
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser(session);
            RepositoryItem item = getRepositoryManager().getRepositoryItem(baseNamespace, filename, version);

            if (securityManager.isReadAuthorized(user, item)) {
                FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
                String indexItemId = IndexingUtils.getIdentityKey( item );
                List<ValidationResult> findings = searchService.getLibraryFindings( indexItemId );
                
                model.addAttribute("findings", findings);
                model.addAttribute("item", item);
            	
            } else {
                setErrorMessage("You are not authorized to view the requested library.", model);
                targetPage = new SearchController().searchPage(null, false, false, session, model);
            }

        } catch (Throwable t) {
            log.error("An error occured while displaying the library.", t);
            setErrorMessage(
                    "An error occured while displaying the library (see server log for details).",
                    model);
        }

        if (targetPage == null) {
            targetPage = applyCommonValues(model, "libraryValidation");
        }
        return targetPage;
    }
    
    /**
     * Called by the Spring MVC controller to display the general-information library
     * page.
     * 
     * @param rootNamespace  the root namespace of the selected library
     * @param path  the sub-namespace path relative to the base namespace
     * @param filename  the filename of the selected library to view
     * @param version  the version of the selected library to view
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({ "/libraryInfo.html", "/libraryInfo.htm" })
    public String libraryInfo(@RequestParam(value = "baseNamespace") String baseNamespace,
            @RequestParam(value = "filename") String filename,
            @RequestParam(value = "version") String version, HttpSession session, Model model) {
        String targetPage = null;
        try {
            RepositorySecurityManager securityManager = getSecurityManager();
            UserPrincipal user = getCurrentUser(session);
            RepositoryItem item = getRepositoryManager().getRepositoryItem(baseNamespace, filename, version);

            if (securityManager.isReadAuthorized(user, item)) {
                boolean otm16Enabled = RepositoryUtils.isOTM16LifecycleEnabled( item.getStatus().toRepositoryStatus() );
                FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
                String indexItemId = IndexingUtils.getIdentityKey( item );
                LibrarySearchResult indexItem = searchService.getLibrary( indexItemId, false );
                UserPrincipal lockedByUser = null;
                
                if (item.getLockedByUser() != null) {
                    lockedByUser = securityManager.getUser( item.getLockedByUser() );
                }
                model.addAttribute("otm16Enabled", otm16Enabled);
                model.addAttribute("lockedByUser", lockedByUser);
                model.addAttribute("indexItem", indexItem);
                model.addAttribute("item", item);
                
            } else {
                setErrorMessage("You are not authorized to view the requested library.", model);
                targetPage = new SearchController().searchPage(null, false, false, session, model);
            }

        } catch (Throwable t) {
            log.error("An error occured while displaying the library.", t);
            setErrorMessage(
                    "An error occured while displaying the library (see server log for details).",
                    model);
        }

        if (targetPage == null) {
            targetPage = applyCommonValues(model, "libraryInfo");
        }
        return targetPage;
    }

    /**
     * Iterates through the indirect where-used list of libraries and removes all entries
     * that are members of the direct where-used list.
     * 
     * @param indirectWhereUsed  the list of indirect where-used libraries to process
     * @param directWhereUsed  the list of direct where-used libraries
     */
    private void purgeDirectWhereUsed(List<LibrarySearchResult> indirectWhereUsed, List<LibrarySearchResult> directWhereUsed) {
    	Iterator<LibrarySearchResult> iterator = indirectWhereUsed.iterator();
    	Set<String> directWhereUsedIds = new HashSet<>();
    	
    	for (LibrarySearchResult lsr : directWhereUsed) {
    		directWhereUsedIds.add( lsr.getSearchIndexId() );
    	}
    	while (iterator.hasNext()) {
    		LibrarySearchResult lsr = iterator.next();
    		
    		if (directWhereUsedIds.contains( lsr.getSearchIndexId() )) {
    			iterator.remove();
    		}
    	}
    }
    
}

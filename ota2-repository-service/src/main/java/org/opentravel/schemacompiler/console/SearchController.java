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
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.index.IndexingSearchService;
import org.opentravel.schemacompiler.index.LibrarySearchResult;
import org.opentravel.schemacompiler.index.SearchResult;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller that handles interactions with the search page of the OTA2.0 repository console.
 * 
 * @author S. Livezey
 */
@Controller
public class SearchController extends BaseController {

    private static Log log = LogFactory.getLog(SearchController.class);

    private IndexingSearchService searchService;

    /**
     * Default constructor.
     */
    public SearchController() {
        try {
        	IndexingSearchService.initializeSingleton(RepositoryComponentFactory.getDefault()
                    .getSearchIndexLocation(), getRepositoryManager());
            searchService = IndexingSearchService.getInstance();

        } catch (Throwable t) {
            log.error("Error initializing the free-text search service.", t);
        }
    }

    /**
     * Called by the Spring MVC controller to display the application search page.
     * 
     * @param keywords
     *            the keywords for the free-text search query
     * @param latestVersions
     *            flag indicating whether only latest versions should be returned in the search
     *            results
     * @param finalVersions
     *            flag indicating whether only final (non-draft) versions should be returned in the
     *            search results
     * @param model
     *            the model context to be used when rendering the page view
     * @param session
     *            the HTTP session that contains information about an authenticated user
     * @return String
     */
    @RequestMapping({ "/", "/index.html", "/index.htm", "/search.html", "/search.htm" })
    public String searchPage(@RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "latestVersions", required = false) boolean latestVersions,
            @RequestParam(value = "finalVersions", required = false) boolean finalVersions,
            HttpSession session, Model model) {
        if ((keywords != null) && (keywords.length() > 0)) {
            List<NamespaceItem> searchResults = new ArrayList<NamespaceItem>();

            if (searchService != null) {
                try {
                	TLLibraryStatus searchStatus = finalVersions ? TLLibraryStatus.FINAL : null;
                    List<SearchResult<?>> results = searchService.search(keywords, searchStatus, latestVersions, false );
                    RepositorySecurityManager securityManager = getSecurityManager();
                    UserPrincipal user = getCurrentUser(session);

                    for (SearchResult<?> result : results) {
                    	if (result instanceof LibrarySearchResult) {
                    		RepositoryItem item = ((LibrarySearchResult) result).getRepositoryItem();
                            RepositoryPermission requiredPermission =
                            		(item.getStatus() == TLLibraryStatus.DRAFT) ?
                            				RepositoryPermission.READ_DRAFT : RepositoryPermission.READ_FINAL;

                            if (securityManager.isAuthorized(user, item.getNamespace(),
                                    requiredPermission)) {
                                searchResults.add(new NamespaceItem(item));
                            }
                    	}
                    }
                } catch (RepositoryException e) {
                    log.error("An error occured while performing the requested search.", e);
                    setErrorMessage(
                            "An error occured while performing the requested search (see server log for details).",
                            model);
                }
            } else {
                setErrorMessage(
                        "The repository's search service is not available (please contact your system administrator).",
                        model);
            }
            model.addAttribute("keywords", keywords);
            model.addAttribute("searchResults", searchResults);
        }
        return applyCommonValues(model, "search");
    }

}

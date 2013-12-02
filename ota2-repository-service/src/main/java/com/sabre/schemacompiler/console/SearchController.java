/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.console;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sabre.schemacompiler.index.FreeTextSearchService;
import com.sabre.schemacompiler.model.TLLibraryStatus;
import com.sabre.schemacompiler.repository.RepositoryComponentFactory;
import com.sabre.schemacompiler.repository.RepositoryException;
import com.sabre.schemacompiler.repository.RepositoryItem;
import com.sabre.schemacompiler.security.RepositorySecurityManager;
import com.sabre.schemacompiler.security.UserPrincipal;

/**
 * Controller that handles interactions with the search page of the OTA2.0 repository console.
 * 
 * @author S. Livezey
 */
@Controller
public class SearchController extends BaseController {
	
	private static Log log = LogFactory.getLog(SearchController.class);
	
	private FreeTextSearchService searchService;
	
	/**
	 * Default constructor.
	 */
	public SearchController() {
		try {
			FreeTextSearchService.initializeSingleton(
					RepositoryComponentFactory.getDefault().getSearchIndexLocation(), getRepositoryManager() );
			searchService = FreeTextSearchService.getInstance();
			
		} catch (Throwable t) {
			log.error("Error initializing the free-text search service.", t);
		}
	}
	
	/**
	 * Called by the Spring MVC controller to display the application search page.
	 * 
	 * @param keywords  the keywords for the free-text search query
	 * @param latestVersions  flag indicating whether only latest versions should be returned in the search results
	 * @param finalVersions  flag indicating whether only final (non-draft) versions should be returned in the search results
	 * @param model  the model context to be used when rendering the page view
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @return String
	 */
	@RequestMapping( { "/", "/index.html", "/index.htm", "/search.html", "/search.htm" } )
	public String searchPage(@RequestParam(value="keywords", required=false) String keywords,
			@RequestParam(value="latestVersions", required=false) boolean latestVersions,
			@RequestParam(value="finalVersions", required=false) boolean finalVersions,
			HttpSession session, Model model) {
		if ((keywords != null) && (keywords.length() > 0)) {
			List<NamespaceItem> searchResults = new ArrayList<NamespaceItem>();
			
			if (searchService != null) {
				try {
					List<RepositoryItem> results = searchService.query(keywords, latestVersions, !finalVersions);
					RepositorySecurityManager securityManager = getSecurityManager();
					UserPrincipal user = getCurrentUser( session );
					
					for (RepositoryItem item : results) {
						RepositoryPermission requiredPermission = (item.getStatus() == TLLibraryStatus.DRAFT)
								? RepositoryPermission.READ_DRAFT : RepositoryPermission.READ_FINAL;
							
						if (securityManager.isAuthorized(user, item.getNamespace(), requiredPermission)) {
							searchResults.add( new NamespaceItem(item) );
						}
					}
				} catch (RepositoryException e) {
					log.error("An error occured while performing the requested search.", e);
					setErrorMessage( "An error occured while performing the requested search (see server log for details).", model );
				}
			} else {
				setErrorMessage( "The repository's search service is not available (please contact your system administrator).", model );
			}
			model.addAttribute("keywords", keywords);
			model.addAttribute("searchResults", searchResults);
		}
		return applyCommonValues( model, "search" );
	}
	
}

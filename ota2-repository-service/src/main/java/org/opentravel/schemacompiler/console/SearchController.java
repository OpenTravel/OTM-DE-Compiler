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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.index.EntitySearchResult;
import org.opentravel.schemacompiler.index.FreeTextSearchService;
import org.opentravel.schemacompiler.index.FreeTextSearchServiceFactory;
import org.opentravel.schemacompiler.index.LibrarySearchResult;
import org.opentravel.schemacompiler.index.ReleaseSearchResult;
import org.opentravel.schemacompiler.index.SearchResult;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.repository.Release;
import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.opentravel.schemacompiler.util.MessageFormatter;
import org.opentravel.schemacompiler.util.PageUtils;
import org.opentravel.schemacompiler.util.SelectOption;
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
	private static MessageFormatter messageFormatter = new MessageFormatter();
	private static List<Class<?>> entityFilterTypes = Arrays.asList(
			TLLibrary.class, Release.class, TLBusinessObject.class, TLChoiceObject.class,
			TLCoreObject.class, TLValueWithAttributes.class, TLAbstractEnumeration.class, TLSimple.class
		);
	private static Map<String,Class<?>> entityFilterMap;
	
    private FreeTextSearchService searchService;

    /**
     * Default constructor.
     */
    public SearchController() {
        try {
        	FreeTextSearchServiceFactory.initializeSingleton(RepositoryComponentFactory.getDefault()
                    .getSearchIndexLocation(), getRepositoryManager());
            searchService = FreeTextSearchServiceFactory.getInstance();

        } catch (Exception e) {
            log.error("Error initializing the free-text search service.", e);
        }
    }

    /**
     * Called by the Spring MVC controller to display the application search page.
     * 
     * @param keywords  the keywords for the free-text search query
     * @param latestVersions  flag indicating whether only latest versions should be returned
     *						  in the search results
     * @param finalVersions  flag indicating whether only final (non-draft) versions should be
     *						 returned in the search results
     * @param model  the model context to be used when rendering the page view
     * @param session  the HTTP session that contains information about an authenticated user
     * @return String
     */
    @RequestMapping({ "/", "/index.html", "/index.htm", "/search.html", "/search.htm" })
    public String searchPage(@RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "latestVersions", required = false) boolean latestVersions,
            @RequestParam(value = "minStatus", required = false) String minStatus,
            @RequestParam(value = "nsFilter", required = false) String nsFilter,
            @RequestParam(value = "entityType", required = false) String entityTypeStr,
			HttpSession session, Model model) {
		TLLibraryStatus searchStatus = getEnumValue( minStatus, TLLibraryStatus.class );
		Class<?> entityType = entityFilterMap.get( entityTypeStr );
		
		model.addAttribute( "statusOptions", getStatusOptions( searchStatus ) );
		model.addAttribute( "entityTypeOptions", getEntityTypeOptions( entityType ) );
		model.addAttribute( "nsOptions", getNamespaceOptions( nsFilter ) );
		
		if ((keywords != null) && (keywords.length() > 0)) {
			List<SearchResult<?>> searchResults = new ArrayList<>();
			
			if (searchService != null) {
				try {
					List<SearchResult<Object>> results = searchService.search(keywords, searchStatus, latestVersions, false);
					RepositorySecurityManager securityManager = getSecurityManager();
					UserPrincipal user = getCurrentUser(session);
					
					for (SearchResult<?> result : results) {
						if (!isFilterMatch( result, nsFilter, entityType )) {
							continue;
						}
						
						if (result instanceof ReleaseSearchResult) {
							ReleaseSearchResult release = (ReleaseSearchResult) result;
							RepositoryItem releaseItem = RepositoryManager.getDefault().getRepositoryItem(
									release.getBaseNamespace(), release.getFilename(), release.getVersion());
							
							if (securityManager.isReadAuthorized(user, releaseItem)) {
								searchResults.add(result);
							}
							
						} else if (result instanceof LibrarySearchResult) {
							RepositoryItem item = ((LibrarySearchResult) result).getRepositoryItem();
							
							if (securityManager.isReadAuthorized(user, item)) {
								searchResults.add(result);
							}
							
						} else if (result instanceof EntitySearchResult) {
							EntitySearchResult indexEntity = (EntitySearchResult) result;
							RepositoryPermission checkPermission = ((indexEntity.getStatus() == TLLibraryStatus.FINAL)
									|| (indexEntity.getStatus() == TLLibraryStatus.OBSOLETE))
											? RepositoryPermission.READ_FINAL
											: RepositoryPermission.READ_DRAFT;
							
							if (securityManager.isAuthorized(user, indexEntity.getItemNamespace(), checkPermission)) {
								searchResults.add(result);
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
			model.addAttribute("pageUtils", new PageUtils());
			model.addAttribute("imageResolver", new SearchResultImageResolver());
		}
		return applyCommonValues(model, "search");
    }
    
    /**
     * Returns a default search page with no parameters specified by the user.
     * 
     * @param model  the model context to be used when rendering the page view
     * @param session  the HTTP session that contains information about an authenticated user
     * @return String
     */
    public String defaultSearchPage(HttpSession session, Model model) {
    		return searchPage( null, false, null, null, null, session, model );
    }
    
    /**
     * Returns true if the given search result matches with the namespace and entity
     * type filters provided.
     * 
     * @param searchResult  the search result to verify
     * @param nsFilter  the namespace filter for the search
     * @param entityTypeFilter  the entity type filter for the search
     * @return boolean
     */
    private boolean isFilterMatch(SearchResult<?> searchResult, String nsFilter, Class<?> entityTypeFilter) {
    		boolean isMatch = true;
    		
    		if ((entityTypeFilter != null) && !entityTypeFilter.isAssignableFrom( searchResult.getEntityType() )) {
    			isMatch = false;
    		}
    		if (isMatch && (nsFilter != null) && (nsFilter.length() > 0)
    				&& !searchResult.getItemNamespace().startsWith( nsFilter )) {
    			isMatch = false;
    		}
    		return isMatch;
    }
    
    /**
     * Returns the corresponding enum value or null if the value string is null or invalid.
     * 
     * @param valueStr  the string representation of the enum value
     * @param enumType  the enumeration type from which to obtain the value
     * @return T
     */
    private <T extends Enum<T>> T getEnumValue(String valueStr, Class<T> enumType) {
    		T enumValue = null;
    		
    		try {
    			if (valueStr != null) {
        			enumValue = Enum.valueOf( enumType, valueStr );
    			}
    			
    		} catch (IllegalArgumentException e) {
    			// No action - return null as enum value
    		}
    		return enumValue;
    }
    
    /**
     * Returns the minimum-status options for the search filters.
     * 
     * @param selectedStatus  the minimum-status that is currently selected
     * @return List<SelectOption>
     */
    private List<SelectOption> getStatusOptions(TLLibraryStatus selectedStatus) {
		String selectedStatusStr = (selectedStatus == null) ? null : selectedStatus.toString();
    		List<SelectOption> options = new ArrayList<>();
    		
    		for (TLLibraryStatus status : TLLibraryStatus.values()) {
    			options.add( new SelectOption( status.toString(),
    					messageFormatter.getLibraryStatusDisplayName( status ) ) );
    		}
    		SelectOption.setSelectedValue( options, selectedStatusStr );
    		return options;
    }
    
    /**
     * Returns the entity-type options for the search filters.
     * 
     * @param selectedEntityType  the entity-type that is currently selected
     * @return List<SelectOption>
     */
    private List<SelectOption> getEntityTypeOptions(Class<?> selectedEntityType) {
		String selectedEntityTypeStr = (selectedEntityType == null) ? null : selectedEntityType.getSimpleName();
    		List<SelectOption> options = new ArrayList<>();
    		
    		options.add( new SelectOption( "", "Any" ) );
    		
    		for (Class<?> entityType : entityFilterTypes) {
    			options.add( new SelectOption( entityType.getSimpleName(),
    					messageFormatter.getEntityTypeDisplayName( entityType ) ) );
    		}
    		SelectOption.setSelectedValue( options, selectedEntityTypeStr );
    		return options;
    }
    
    /**
     * Returns the namespace options for the search filters.
     * 
     * @param selectedNamespace  the namespace that is currently selected
     * @return List<SelectOption>
     */
    private List<SelectOption> getNamespaceOptions(String selectedNamespace) {
    		List<String> filterNamespaces = new ArrayList<>();
		List<SelectOption> options = new ArrayList<>();
    		
		options.add( new SelectOption( "", "Any" ) );
		getFilterNamespaces( null, 0, filterNamespaces );
		
		for (String filterNS : filterNamespaces) {
			options.add( new SelectOption( filterNS ) );
		}
		SelectOption.setSelectedValue( options, selectedNamespace );
		return options;
    }
    
    /**
     * Returns the list of repository namespaces that should be included in the filter options.
     * 
     * @param parentNS  the parent namespace for which to identify children
     * @param depth  the current depth of the search
     * @param nsList  the list of namespaces being constructed
     */
    private void getFilterNamespaces(String parentNS, int depth, List<String> nsList) {
    		if (depth < 3) {
    			try {
            		RepositoryManager manager = getRepositoryManager();
        			List<String> childNamespaces;
        			String baseNS;
        			
            		if (parentNS == null) {
            			childNamespaces = manager.listRootNamespaces();
            			baseNS = "";
            			
            		} else {
            			childNamespaces = manager.listNamespaceChildren( parentNS );
            			baseNS = parentNS + "/";
            		}
            		
            		for (String childNS : childNamespaces) {
            			String ns = baseNS + childNS;
            			
        				nsList.add( ns );
        				getFilterNamespaces( ns, depth + 1, nsList );
            		}
            		
    			} catch (RepositoryException e) {
    				// Ignore and return an empty list
    			}
    		}
    }
    
	/**
	 * Initializes the mapping of entity type names to entity classes.
	 */
	static {
		try {
			entityFilterMap = new HashMap<>();
			
			for (Class<?> entityType : entityFilterTypes) {
				entityFilterMap.put( entityType.getSimpleName(), entityType );
			}
			
		} catch (Exception e) {
			throw new ExceptionInInitializerError( e );
		}
	}
	
}

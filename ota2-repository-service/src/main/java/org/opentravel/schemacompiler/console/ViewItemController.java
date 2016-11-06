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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.schemacompiler.index.EntitySearchResult;
import org.opentravel.schemacompiler.index.FreeTextSearchService;
import org.opentravel.schemacompiler.index.FreeTextSearchServiceFactory;
import org.opentravel.schemacompiler.index.IndexingUtils;
import org.opentravel.schemacompiler.index.LibrarySearchResult;
import org.opentravel.schemacompiler.index.SearchResult;
import org.opentravel.schemacompiler.index.ValidationResult;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.opentravel.schemacompiler.util.DocumentationHelper;
import org.opentravel.schemacompiler.util.FacetIdentityWrapper;
import org.opentravel.schemacompiler.util.PageUtils;
import org.opentravel.schemacompiler.util.ReferenceFinder;
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
                model.addAttribute("pageUtils", new PageUtils());
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
    @RequestMapping({ "/entityDictionary.html", "/entityDictionary.htm" })
    public String entityDictionary(@RequestParam(value = "namespace") String namespace,
            @RequestParam(value = "localName") String localName, HttpSession session, Model model) {
        String targetPage = null;
        try {
        	FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
            RepositorySecurityManager securityManager = getSecurityManager();
            String identityKey = IndexingUtils.getIdentityKey( namespace, localName, true );
            EntitySearchResult indexEntity = searchService.getEntity( identityKey, true );
        	LibrarySearchResult indexLibrary = (indexEntity == null) ? null :
        			searchService.getLibrary( indexEntity.getOwningLibraryId(), false );
            
            if (indexLibrary != null) {
                UserPrincipal user = getCurrentUser(session);
            	
                if (securityManager.isReadAuthorized( user, indexLibrary.getRepositoryItem() )) {
                	ReferenceFinder refFinder = new ReferenceFinder( indexEntity.getItemContent(), indexLibrary );
                	Map<String,EntitySearchResult> entitiesByReference = refFinder.buildEntityReferenceMap( searchService );
                	List<FacetIdentityWrapper> entityFacets = buildFacetList( indexEntity.getItemContent() );
                	
                	model.addAttribute( "entity", indexEntity );
                	model.addAttribute( "library", indexLibrary );
                	model.addAttribute( "pageUtils", new PageUtils() );
                	model.addAttribute( "entityFacets", entityFacets );
                	model.addAttribute( "entitiesByReference", entitiesByReference );
                	model.addAttribute( "docHelper", new DocumentationHelper( indexEntity ) );
                    model.addAttribute( "imageResolver", new SearchResultImageResolver() );
                	
                } else {
                    setErrorMessage("You are not authorized to view the requested entity.", model);
                    targetPage = new SearchController().searchPage(null, false, false, session, model);
                }
            } else {
                setErrorMessage("The requested entity could not be found.", model);
                targetPage = new SearchController().searchPage(null, false, false, session, model);
            }
            
        } catch (Throwable t) {
            log.error("An error occured while displaying the library.", t);
            setErrorMessage(
                    "An error occured while displaying the library (see server log for details).",
                    model);
        }

        if (targetPage == null) {
            targetPage = applyCommonValues(model, "entityDictionary");
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
    @RequestMapping({ "/entityUsage.html", "/entityUsage.htm" })
    public String entityUsage(@RequestParam(value = "namespace") String namespace,
            @RequestParam(value = "localName") String localName, HttpSession session, Model model) {
        String targetPage = null;
        try {
        	FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
            RepositorySecurityManager securityManager = getSecurityManager();
            String identityKey = IndexingUtils.getIdentityKey( namespace, localName, true );
            EntitySearchResult indexEntity = searchService.getEntity( identityKey, true );
        	LibrarySearchResult indexLibrary = (indexEntity == null) ? null :
        			searchService.getLibrary( indexEntity.getOwningLibraryId(), false );
            
            if (indexLibrary != null) {
                UserPrincipal user = getCurrentUser(session);
            	
                if (securityManager.isReadAuthorized( user, indexLibrary.getRepositoryItem() )) {
                    List<EntitySearchResult> directWhereUsed = searchService.getEntityWhereUsed( indexEntity, false, false );
                    List<EntitySearchResult> indirectWhereUsed = searchService.getEntityWhereUsed( indexEntity, true, false );
                    
                    purgeDirectWhereUsed( indirectWhereUsed, directWhereUsed );
                    
                	model.addAttribute( "entity", indexEntity );
                	model.addAttribute( "library", indexLibrary );
                    model.addAttribute("directWhereUsed", directWhereUsed);
                    model.addAttribute("indirectWhereUsed", indirectWhereUsed);
                    model.addAttribute("imageResolver", new SearchResultImageResolver());
                	
                } else {
                    setErrorMessage("You are not authorized to view the requested entity.", model);
                    targetPage = new SearchController().searchPage(null, false, false, session, model);
                }
            } else {
                setErrorMessage("The requested entity could not be found.", model);
                targetPage = new SearchController().searchPage(null, false, false, session, model);
            }
            
        } catch (Throwable t) {
            log.error("An error occured while displaying the library.", t);
            setErrorMessage(
                    "An error occured while displaying the library (see server log for details).",
                    model);
        }

        if (targetPage == null) {
            targetPage = applyCommonValues(model, "entityUsage");
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
    @RequestMapping({ "/entityValidation.html", "/entityValidation.htm" })
    public String entityValidation(@RequestParam(value = "namespace") String namespace,
            @RequestParam(value = "localName") String localName, HttpSession session, Model model) {
        String targetPage = null;
        try {
        	FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
            RepositorySecurityManager securityManager = getSecurityManager();
            String identityKey = IndexingUtils.getIdentityKey( namespace, localName, true );
            EntitySearchResult indexEntity = searchService.getEntity( identityKey, true );
        	LibrarySearchResult indexLibrary = (indexEntity == null) ? null :
        			searchService.getLibrary( indexEntity.getOwningLibraryId(), false );
            
            if (indexLibrary != null) {
                UserPrincipal user = getCurrentUser(session);
            	
                if (securityManager.isReadAuthorized( user, indexLibrary.getRepositoryItem() )) {
                    List<ValidationResult> findings = searchService.getEntityFindings( identityKey );
                    
                    model.addAttribute( "findings", findings );
                	model.addAttribute( "entity", indexEntity );
                	model.addAttribute( "library", indexLibrary );
                	
                } else {
                    setErrorMessage("You are not authorized to view the requested entity.", model);
                    targetPage = new SearchController().searchPage(null, false, false, session, model);
                }
            } else {
                setErrorMessage("The requested entity could not be found.", model);
                targetPage = new SearchController().searchPage(null, false, false, session, model);
            }
            
        } catch (Throwable t) {
            log.error("An error occured while displaying the library.", t);
            setErrorMessage(
                    "An error occured while displaying the library (see server log for details).",
                    model);
        }

        if (targetPage == null) {
            targetPage = applyCommonValues(model, "entityValidation");
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
     * Constructs the list of facets for the given <code>NamedEntity</code>.  If the
     * entity is not a facet owner, an empty list will be returned.
     * 
     * @param entity  the entity from which to construct the facet list
     * @return List<FacetIdentityWrapper>
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
     * Recursive method that adds all contextual facets and their children to the list
     * provided.
     * 
     * @param contextualFacets  the list of contextual facets to add
     * @param facetList  the facet list being constructed
     */
    private void addContextualFacets(List<TLContextualFacet> contextualFacets, List<FacetIdentityWrapper> facetList) {
    	for (TLContextualFacet facet : contextualFacets) {
    		facetList.add( new FacetIdentityWrapper( facet ) );
    		addContextualFacets( facet.getChildFacets(), facetList );
    	}
    }
    
}

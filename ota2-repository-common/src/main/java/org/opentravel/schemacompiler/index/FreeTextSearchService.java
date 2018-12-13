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
package org.opentravel.schemacompiler.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.Subscription;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionEventType;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Release;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemType;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * Repository service that uses Apache Lucene for indexing of reposited artifacts and free-text
 * searching.
 * 
 * @author S. Livezey
 */
public abstract class FreeTextSearchService implements IndexingTerms {
	
	private static final String ERROR_RELEASING_INDEX_SEARCHER = "Error releasing index searcher.";
	private static final String WHERE_USED_QUERY_ERROR = "Error executing where-used query for entity: ";
	
	private static final Set<String> nonContentAttrs = new HashSet<>( Arrays.asList(
			IDENTITY_FIELD, ENTITY_TYPE_FIELD, ENTITY_NAME_FIELD, ENTITY_NAMESPACE_FIELD,
			BASE_NAMESPACE_FIELD, FILENAME_FIELD, VERSION_FIELD, VERSION_SCHEME_FIELD,
			ENTITY_DESCRIPTION_FIELD, STATUS_FIELD, RELEASE_STATUS_FIELD, LOCKED_BY_USER_FIELD,
			REFERENCED_LIBRARY_FIELD, PREFIX_MAPPING_FIELD, OWNING_LIBRARY_FIELD,
			EXTENDS_ENTITY_FIELD, REFERENCE_IDENTITY_FIELD, FACET_OWNER_FIELD
		) );
	private static final Set<String> contentAttr = new HashSet<>( Arrays.asList( CONTENT_DATA_FIELD ) );
	
    private static Log log = LogFactory.getLog(FreeTextSearchService.class);
    
    protected static Object indexingLock = new Object();

    private File indexLocation;
    private RepositoryManager repositoryManager;
    private boolean isRunning = false;
    private Directory indexDirectory;
    private DirectoryReader indexReader;
    private SearcherManager searchManager;
    private ReadWriteLock searchLock = new ReentrantReadWriteLock();

    /**
     * Constructor that specifies the folder location of the index and the repository
     * manager used to access the content to be indexed and searched for.
     * 
     * @param indexLocation  the folder location of the index directory
     * @param repositoryManager  the repository that owns all content to be indexed
     * @throws IOException  thrown if a low-level error occurs while initializing the search index
     */
    public FreeTextSearchService(File indexLocation, RepositoryManager repositoryManager)
            throws IOException {
        if (!indexLocation.exists()) {
            indexLocation.mkdirs();
        }
        this.repositoryManager = repositoryManager;
        this.indexLocation = indexLocation;
    }

    /**
     * Starts the indexing service.
     * 
     * @throws IllegalStateException
     *             thrown if the service is already running
     * @throws IOException
     *             thrown if a low-level error occurs while initializing the index reader or writer
     */
    @SuppressWarnings("squid:S2093") // Try with resource cannot be used since directory must be left open
    public synchronized void startService() throws IOException {
        if (isRunning) {
            throw new IllegalStateException(
                    "Unable to start - the indexing service is already running.");
        }
        try {
        		searchLock.writeLock().lock();
            this.indexDirectory = FSDirectory.open(indexLocation.toPath());
            onStartup(this.indexDirectory);
            
            this.indexReader = newIndexReader(this.indexDirectory);
            this.searchManager = new SearcherManager(indexReader, new SearcherFactory());
            this.isRunning = true;
        	
        } finally {
        		searchLock.writeLock().unlock();
        }
    }
    
    /**
     * Called during service startup to facilitate any initialization functions required for
     * sub-classes.  Unless overridden, this method returns with no action.
     * 
     * @param indexDirectory  the search index directory
     * @throws IOException  thrown if an error occurs during service initialization
     */
    protected void onStartup(Directory indexDirectory) throws IOException {}

    /**
     * Stops the indexing service. This method halts processing of the indexing tasks that have been
     * submitted to this service, but does not clear the queue of pending tasks.
     * 
     * @throws IllegalStateException
     *             thrown if the service is already running
     * @throws IOException
     *             thrown if a low-level error occurs while closing the index reader or writer
     */
    public synchronized void stopService() throws IOException {
        if (!isRunning) {
            throw new IllegalStateException(
                    "Unable to stop - the indexing service is not currently running.");
        }

        // Close all of the indexing resources that were allocated during service startup
        try {
        	searchLock.writeLock().lock();
            searchManager.close();
            indexReader.close();
        	onShutdown();
            indexDirectory.close();

        } finally {
            searchManager = null;
            indexReader = null;
            indexDirectory = null;
            isRunning = false;
        	searchLock.writeLock().unlock();
        }
    }
    
    /**
     * Called during service shutdown to facilitate any functions required for sub-classes.
     * Unless overridden, this method returns with no action.
     * 
     * @throws IOException  thrown if an error occurs during service shutdown
     */
    protected void onShutdown() throws IOException {}
    
    /**
     * Returns true if the service is running.
     * 
     * @return boolean
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Returns true if indexing jobs can be processed by this service.  Even if the service
     * is running, the processing of indexing jobs (as opposed to searches) may be impaired
     * in some situations.
     * 
     * @return boolean
     */
    public abstract boolean isIndexingServiceAvailable();

    /**
     * Constructs a new <code>DirectoryReader</code> for handling read-only queries to the
     * search index.
     * 
     * @param indexDirectory  the search index directory
     * @return DirectoryReader
     * @throws IOException  thrown if the index reader cannot be created
     */
    protected abstract DirectoryReader newIndexReader(Directory indexDirectory) throws IOException;
    
    /**
     * Constructs a new <code>DirectoryReader</code> instance and closes the old one.
     * 
     * @throws IOException  thrown if the new index reader instance cannot be initialized
     */
    protected void refreshIndexReader() throws IOException {
    	try (DirectoryReader oldReader = indexReader) {
    		searchLock.writeLock().lock();
    		this.indexReader = newIndexReader(indexDirectory);
            this.searchManager = new SearcherManager(indexReader, new SearcherFactory());
    		
    	} finally {
    		searchLock.writeLock().unlock();
    	}
    }
    
	/**
	 * Returns the manager instance used to access the repository content.
	 *
	 * @return RepositoryManager
	 */
	public RepositoryManager getRepositoryManager() {
		return repositoryManager;
	}

	/**
     * Re-indexes (or indexes for the first time) all items in the repository.
     * 
     * @throws RepositoryException  thrown if an error occurs during the indexing operation
     */
    public void indexAllRepositoryItems() throws RepositoryException {
        if (!isRunning) {
            throw new IllegalStateException(
                    "Unable to perform indexing task - the service is not currently running.");
        }
        
        if (!isIndexingServiceAvailable()) {
        	throw new RepositoryException("Indexing service unavailable - unable to perform repository re-indexing.");
        }

        // First, clear the contents of the entire index
        deleteSearchIndex();

        // Retrieve and index all items in the repository
    	List<RepositoryItem> itemsToIndex = new ArrayList<>();
    	
        for (String baseNamespace : repositoryManager.listBaseNamespaces()) {
            for (RepositoryItem item :
            		repositoryManager.listItems(baseNamespace, TLLibraryStatus.DRAFT, false, null)) {
        		itemsToIndex.add( item );
            }
        }
    	submitIndexingJob( itemsToIndex, false );
    }

    /**
     * Submits the given repository item for indexing. To ensure accurate searching across versions,
     * all versions of the library will be re-indexed by this method.
     * 
     * @param item  the repository item to be indexed
     * @throws RepositoryException  thrown if an error occurs during the indexing operation
     */
    public void indexRepositoryItem(RepositoryItem item) throws RepositoryException {
    	List<RepositoryItem> itemsToIndex = new ArrayList<>();
    	String baseNamespace = item.getBaseNamespace();
    	String libraryName = item.getLibraryName();
    	
        // Find all versions of this library and re-index all of them
        for (RepositoryItem candidateItem :
        		repositoryManager.listItems(baseNamespace, TLLibraryStatus.DRAFT, false, null)) {
            if (libraryName.equals( candidateItem.getLibraryName() )) {
        		itemsToIndex.add( candidateItem );
            }
        }
    	submitIndexingJob( itemsToIndex, false );
        
        // Re-index all libraries that directly reference this repository item to ensure
        // that the validation results are accurate
        indexWhereUsedLibraries( item );
    }
    
    /**
     * Removes the index for the specified repository item. If other versions of the item still
     * exist, the version chain will be re-indexed. If the item is the last existing version, the
     * index will be deleted outright.
     * 
     * @param item  the repository item whose index is to be deleted
     * @throws RepositoryException  thrown if an error occurs during the indexing operation
     */
    public void deleteRepositoryItemIndex(RepositoryItem item) throws RepositoryException {
    	List<RepositoryItem> itemsToIndex = new ArrayList<>();
    	String baseNamespace = item.getBaseNamespace();
    	String libraryName = item.getLibraryName();
    	String version = item.getVersion();
    	
    	submitIndexingJob( Arrays.asList( item ), true ); // delete the index for the target version
    	
        // Re-index all of the remaining versions
    	
        for (RepositoryItem candidateItem : repositoryManager.listItems(baseNamespace, false, true)) {
            if (libraryName.equals( candidateItem.getLibraryName() )
            		&& !version.equals( candidateItem.getVersion() )) {
        		itemsToIndex.add( candidateItem );
            }
        }
    	submitIndexingJob( itemsToIndex, false );
        
        // Re-index all libraries that directly reference this repository item to ensure
        // that the validation results are accurate
        indexWhereUsedLibraries( item );
    }
    
    /**
     * Submits the given subscription target for indexing.
     * 
     * @param subscriptionTarget  the subscription target to be indexed
     * @throws RepositoryException  thrown if an error occurs during the indexing operation
     */
    public void indexSubscriptionTarget(SubscriptionTarget subscriptionTarget) throws RepositoryException {
    	submitIndexingJob( subscriptionTarget );
    }
    
    /**
     * Updates the indexes for all libraries that directly reference the given repository
     * item.
     * 
     * @param item  the repository item for which to re-index all where-used libraries
     * @throws RepositoryException  thrown if an error occurs during the indexing operation
     */
    private void indexWhereUsedLibraries(RepositoryItem item) throws RepositoryException {
        String libraryIndexId = IndexingUtils.getIdentityKey( item );
        LibrarySearchResult libraryIndex = getLibrary( libraryIndexId, false );
        
        if (libraryIndex != null) {
        	List<LibrarySearchResult> whereUsedLibraries = getLibraryWhereUsed( libraryIndex, false, false );
        	List<RepositoryItem> itemsToIndex = new ArrayList<>();
        	
        	for (LibrarySearchResult whereUsed : whereUsedLibraries) {
        		itemsToIndex.add( whereUsed.getRepositoryItem() );
        	}
        	submitIndexingJob( itemsToIndex, false );
        }
    }
    
    /**
     * Submits the given the list of repository items for processing.
     * 
     * @param itemsToIndex  the list of repository items to be indexed
     * @param deleteIndex  flag indicating whether the item's index is to be created or deleted
     */
    protected abstract void submitIndexingJob(List<RepositoryItem> itemsToIndex, boolean deleteIndex);

    /**
     * Submits the given subscription target for processing.
     * 
     * @param itemsToIndex  the list of repository items to be indexed
     */
    protected abstract void submitIndexingJob(SubscriptionTarget subscriptionTarget);

    /**
     * Deletes the contents of the entire search index.
     */
    protected abstract void deleteSearchIndex();

    /**
     * Performs a free-text search of the repository index.  Results may include OTM libraries and/or
     * individual entity records.
     * 
     * @param freeText  the free-text search criteria / keywords
     * @param includeStatus  indicates the latest library status to include in the results (null = all statuses)
     * @param latestVersionsOnly  flag indicating whether only the latest versions of a library or entity should
     *							  be returned in the search results
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<SearchResult<?>>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<SearchResult<?>> search(String freeText, TLLibraryStatus includeStatus,
    		boolean latestVersionsOnly, boolean resolveContent) throws RepositoryException {
    	try {
			Query keywordQuery = new QueryParser( KEYWORDS_FIELD, new StandardAnalyzer()).parse( freeText + "~" );
			BooleanQuery statusQuery = null;
			Query latestVersionQuery = null;
			
			// Construct the search query...
			if (!latestVersionsOnly && (includeStatus != null) && (includeStatus != TLLibraryStatus.DRAFT)) {
				statusQuery = new BooleanQuery();
				
				switch (includeStatus) {
					case UNDER_REVIEW:
						statusQuery.add( new BooleanClause( new TermQuery(
								new Term( STATUS_FIELD, TLLibraryStatus.UNDER_REVIEW.toString() ) ), Occur.SHOULD ));
						statusQuery.add( new BooleanClause( new TermQuery(
								new Term( STATUS_FIELD, TLLibraryStatus.FINAL.toString() ) ), Occur.SHOULD ));
						statusQuery.add( new BooleanClause( new TermQuery(
								new Term( STATUS_FIELD, TLLibraryStatus.OBSOLETE.toString() ) ), Occur.SHOULD ));
						break;
					case FINAL:
						statusQuery.add( new BooleanClause( new TermQuery(
								new Term( STATUS_FIELD, TLLibraryStatus.FINAL.toString() ) ), Occur.SHOULD ));
						statusQuery.add( new BooleanClause( new TermQuery(
								new Term( STATUS_FIELD, TLLibraryStatus.OBSOLETE.toString() ) ), Occur.SHOULD ));
						break;
					case OBSOLETE:
						statusQuery.add( new BooleanClause( new TermQuery(
								new Term( STATUS_FIELD, TLLibraryStatus.OBSOLETE.toString() ) ), Occur.SHOULD ));
						break;
					default:
						break;
				}
			}
			if (latestVersionsOnly) {
				String fieldName;
				
				if (includeStatus != null) {
					switch (includeStatus) {
						case UNDER_REVIEW:
							fieldName = LATEST_VERSION_AT_UNDER_REVIEW_FIELD;
							break;
						case FINAL:
							fieldName = LATEST_VERSION_AT_FINAL_FIELD;
							break;
						case OBSOLETE:
							fieldName = LATEST_VERSION_AT_OBSOLETE_FIELD;
							break;
						default:
							fieldName = LATEST_VERSION_FIELD;
							break;
					}
				} else {
					fieldName = LATEST_VERSION_FIELD;
				}
				latestVersionQuery = new TermQuery( new Term( fieldName, "true" ) );
			}
			
			// Assemble the master search query
			BooleanQuery masterQuery = newSearchIndexQuery();
			
			masterQuery.add( new BooleanClause( keywordQuery, Occur.MUST ) );
			
			if (statusQuery != null) {
				masterQuery.add( new BooleanClause( statusQuery, Occur.MUST ) );
			}
			if (latestVersionQuery != null) {
				masterQuery.add( new BooleanClause( latestVersionQuery, Occur.MUST ) );
			}
			
			// Execute the query and assemble the search results
			List<SearchResult<?>> searchResults = new ArrayList<>();
			List<Document> queryResults = executeQuery( masterQuery, resolveContent ? null : nonContentAttrs );
			
			for (Document doc : queryResults) {
				if (TLLibrary.class.getName().equals( doc.get( ENTITY_TYPE_FIELD ) )) {
					searchResults.add( new LibrarySearchResult( doc, repositoryManager, this ) );
					
				} else if (Release.class.getName().equals( doc.get( ENTITY_TYPE_FIELD ) )) {
					searchResults.add( new ReleaseSearchResult( doc, this ) );
					
				} else {
					searchResults.add( new EntitySearchResult( doc, this ) );
				}
			}
	    		return searchResults;
	    	
		} catch (ParseException e) {
			throw new RepositoryException("Error in free-text search query.", e);
		}
    }
    
    /**
     * Returns the library with the specified search index ID.
     * 
     * @param searchIndexId  the search index ID of the library to retrieve
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return LibrarySearchResult
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public LibrarySearchResult getLibrary(String searchIndexId, boolean resolveContent) throws RepositoryException {
    	List<LibrarySearchResult> results = getLibraries( Arrays.asList( searchIndexId ), resolveContent );
    	return results.isEmpty() ? null : results.get( 0 );
    }
    
    /**
     * Returns the library from the search index that is associated with the given repository item.
     * 
     * @param repositoryItem  the repository item for which to return the library from the search index
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return LibrarySearchResult
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public LibrarySearchResult getLibrary(RepositoryItem item, boolean resolveContent) throws RepositoryException {
    	return (LibrarySearchResult) getLibraryOrRelease( item, resolveContent, RepositoryItemType.LIBRARY );
    }
    
    /**
     * Returns a list of libraries that correspond to the search index ID's provided.
     * 
     * @param searchIndexIds  the search index ID's for the libraries to retrieve
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<LibrarySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<LibrarySearchResult> getLibraries(Collection<String> searchIndexIds, boolean resolveContent)
    		throws RepositoryException {
    	List<SearchResult<?>> rawResults = getLibrariesOrReleases(
    			searchIndexIds, resolveContent, RepositoryItemType.LIBRARY );
    	List<LibrarySearchResult> results = new ArrayList<>();
    	
    	for (SearchResult<?> r : rawResults) {
    		results.add( (LibrarySearchResult) r );
    	}
    	return results;
    }
    
    /**
     * Returns the release with the specified search index ID.
     * 
     * @param searchIndexId  the search index ID of the release to retrieve
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return ReleaseSearchResult
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public ReleaseSearchResult getRelease(String searchIndexId, boolean resolveContent) throws RepositoryException {
    	List<ReleaseSearchResult> results = getReleases( Arrays.asList( searchIndexId ), resolveContent );
    	return results.isEmpty() ? null : results.get( 0 );
    }
    
    /**
     * Returns the release from the search index that is associated with the given repository item.
     * 
     * @param repositoryItem  the repository item for which to return the release from the search index
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return ReleaseSearchResult
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public ReleaseSearchResult getRelease(RepositoryItem item, boolean resolveContent) throws RepositoryException {
    	return (ReleaseSearchResult) getLibraryOrRelease( item, resolveContent, RepositoryItemType.RELEASE );
    }
    
    /**
     * Returns a list of releases that correspond to the search index ID's provided.
     * 
     * @param searchIndexIds  the search index ID's for the releases to retrieve
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<ReleaseSearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<ReleaseSearchResult> getReleases(Collection<String> searchIndexIds, boolean resolveContent)
    		throws RepositoryException {
    	List<SearchResult<?>> rawResults = getLibrariesOrReleases(
    			searchIndexIds, resolveContent, RepositoryItemType.RELEASE );
    	List<ReleaseSearchResult> results = new ArrayList<>();
    	
    	for (SearchResult<?> r : rawResults) {
    		results.add( (ReleaseSearchResult) r );
    	}
    	return results;
    }
    
    /**
     * Returns the library or release from the search index that is associated with the given repository item.
     * 
     * @param repositoryItem  the repository item for which to return the library or release from the search index
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @param itemType  the type of repository item (library or release) to search for
     * @return SearchResult<?>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    private SearchResult<?> getLibraryOrRelease(RepositoryItem item, boolean resolveContent, RepositoryItemType itemType)
    		throws RepositoryException {
    	SearchResult<?> searchResult = null;
    	BooleanQuery query = newSearchIndexQuery();
    	
		if (itemType == RepositoryItemType.RELEASE) {
			query.add( new BooleanClause( new TermQuery(
					new Term( ENTITY_TYPE_FIELD, Release.class.getName() ) ), Occur.MUST ));
			
		} else {
			query.add( new BooleanClause( new TermQuery(
					new Term( ENTITY_TYPE_FIELD, TLLibrary.class.getName() ) ), Occur.MUST ));
		}
    	
		query.add( new BooleanClause( new TermQuery(
				new Term( BASE_NAMESPACE_FIELD, item.getBaseNamespace() ) ), Occur.MUST ));
		query.add( new BooleanClause( new TermQuery(
				new Term( FILENAME_FIELD, item.getFilename() ) ), Occur.MUST ));
		query.add( new BooleanClause( new TermQuery(
				new Term( VERSION_FIELD, item.getVersion() ) ), Occur.MUST ));
    	List<Document> queryResults = executeQuery( query, resolveContent ? null : nonContentAttrs );
    	
    	if (!queryResults.isEmpty()) {
    		if (itemType == RepositoryItemType.RELEASE) {
        		searchResult = new ReleaseSearchResult( queryResults.get( 0 ), this );
    			
    		} else {
        		searchResult = new LibrarySearchResult( queryResults.get( 0 ), repositoryManager, this );
    		}
    	}
    	return searchResult;
    }
    
    /**
     * Returns a list of libraries or releases that correspond to the search index ID's provided.
     * 
     * @param searchIndexIds  the search index ID's for the libraries or releases to retrieve
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @param itemType  the type of repository item (library or release) to search for
     * @return List<LibrarySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    private List<SearchResult<?>> getLibrariesOrReleases(Collection<String> searchIndexIds,
    		boolean resolveContent, RepositoryItemType itemType) throws RepositoryException {
    	List<SearchResult<?>> searchResults = new ArrayList<>();
    	
    	if ((searchIndexIds != null) && !searchIndexIds.isEmpty()) {
        	BooleanQuery identityQuery = new BooleanQuery();
        	BooleanQuery masterQuery = newSearchIndexQuery();
        	List<Document> queryResults;
        	
        	for (String searchIndexId : searchIndexIds) {
        		identityQuery.add( new BooleanClause( new TermQuery(
        				new Term( IDENTITY_FIELD, searchIndexId ) ), Occur.SHOULD ));
        	}
    		if (itemType == RepositoryItemType.RELEASE) {
    			masterQuery.add( new BooleanClause( new TermQuery(
    					new Term( ENTITY_TYPE_FIELD, Release.class.getName() ) ), Occur.MUST ));
    			
    		} else {
    			masterQuery.add( new BooleanClause( new TermQuery(
    					new Term( ENTITY_TYPE_FIELD, TLLibrary.class.getName() ) ), Occur.MUST ));
    		}
        	masterQuery.add( identityQuery, Occur.MUST );
        	queryResults = executeQuery( masterQuery, resolveContent ? null : nonContentAttrs );
        	
        	for (Document doc : queryResults) {
        		if (itemType == RepositoryItemType.RELEASE) {
            		searchResults.add( new ReleaseSearchResult( doc, this ) );
        			
        		} else {
            		searchResults.add( new LibrarySearchResult( doc, repositoryManager, this ) );
        		}
        	}
    	}
    	return searchResults;
    }
    
    /**
     * Returns the list of libraries that are locked by the specified user.  If the user ID provided
     * is null, this method will return a list of all repository items that are locked by any user.
     * 
     * @param userId  the ID if the user for which to return locked libraries (null for all users)
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<LibrarySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<LibrarySearchResult> getLockedLibraries(String userId, boolean resolveContent) throws RepositoryException {
    	List<LibrarySearchResult> searchResults = new ArrayList<>();
    	BooleanQuery query = newSearchIndexQuery();
    	List<Document> queryResults;
    	
    	if ((userId == null) || (userId.length() == 0)) {
        	query.add( new BooleanClause( new RegexpQuery(
    				new Term( LOCKED_BY_USER_FIELD, ".+" ) ), Occur.MUST ));
        	
    	} else {
        	query.add( new BooleanClause( new TermQuery(
    				new Term( LOCKED_BY_USER_FIELD, userId ) ), Occur.MUST ));
    	}
    	query.add( new BooleanClause( new TermQuery(
				new Term( ENTITY_TYPE_FIELD, TLLibrary.class.getName() ) ), Occur.MUST ));
    	queryResults = executeQuery( query, resolveContent ? null : nonContentAttrs );
    	
    	for (Document doc : queryResults) {
    		searchResults.add( new LibrarySearchResult( doc, repositoryManager, this ) );
    	}
    	return searchResults;
    }
    
    /**
     * Returns the list of libraries that directly (and possibly indirectly) reference the library with the
     * specified search index ID.
     * 
     * @param libraryIndex  the index search result for the library to use as the target of the where-used search
     * @param includeIndirectReferences  flag indicating whether indirect where-used references should be included
     *									 in the search results
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<LibrarySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<LibrarySearchResult> getLibraryWhereUsed(LibrarySearchResult libraryIndex, boolean includeIndirectReferences,
    		boolean resolveContent) throws RepositoryException {
        IndexSearcher searcher = null;
        try {
        	searchLock.readLock().lock();
        	List<LibrarySearchResult> searchResults = new ArrayList<>();
        	
            searcher = searchManager.acquire();
            
            if (includeIndirectReferences) {
            	getIndirectLibraryWhereUsed( searcher, libraryIndex, new HashSet<String>(), searchResults, resolveContent );
            	
            } else {
            	searchResults.addAll( getDirectLibraryWhereUsed( searcher, libraryIndex, resolveContent ) );
            }
            return searchResults;
            
        } catch (Exception e) {
            throw new RepositoryException(WHERE_USED_QUERY_ERROR + libraryIndex.getSearchIndexId(), e);

        } finally {
            try {
                if (searcher != null) searchManager.release(searcher);

            } catch (Exception e) {
                log.error(ERROR_RELEASING_INDEX_SEARCHER, e);
            }
        	searchLock.readLock().unlock();
        }
    }
    
    /**
     * Performs a recursive where-used search to identify all entities that reference the given entity
     * via both direct and indirect references.
     * 
     * @param searcher  the searcher to use for the query operation(s)
     * @param libraryIndex  the index search result for the library to use as the target of the where-used search
     * @param foundIndexIds  cumulative list of entity search index ID's found during previous queries
     * @param searchResults  cumulative list of where-used search results
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    private void getIndirectLibraryWhereUsed(IndexSearcher searcher, LibrarySearchResult libraryIndex,
    		Set<String> foundIndexIds, List<LibrarySearchResult> searchResults, boolean resolveContent)
    				throws RepositoryException {
    	List<LibrarySearchResult> directWhereUsed = getDirectLibraryWhereUsed( searcher, libraryIndex, resolveContent );
    	
    	for (LibrarySearchResult dwuEntity : directWhereUsed) {
    		String dwuIndexId = dwuEntity.getSearchIndexId();
    		
    		if (!foundIndexIds.contains( dwuIndexId )) {
    			searchResults.add( dwuEntity );
    			foundIndexIds.add( dwuIndexId );
    			getIndirectLibraryWhereUsed( searcher, dwuEntity, foundIndexIds, searchResults, resolveContent );
    		}
    	}
    }
    
    /**
     * Executes a single query to identify the entities that directly reference the given one.
     * 
     * @param searcher  the searcher to use for the query operation
     * @param libraryIndex  the index search result for the library to use as the target of the where-used search
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<LibrarySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    private List<LibrarySearchResult> getDirectLibraryWhereUsed(IndexSearcher searcher, LibrarySearchResult libraryIndex,
    		boolean resolveContent) throws RepositoryException {
    	List<LibrarySearchResult> searchResults = new ArrayList<>();
    	BooleanQuery query = newSearchIndexQuery();
    	List<Document> queryResults;
    	
    	query.add( new BooleanClause( new TermQuery(
				new Term( ENTITY_TYPE_FIELD, TLLibrary.class.getName() ) ), Occur.MUST ));
    	query.add( new BooleanClause( new TermQuery(
				new Term( REFERENCED_LIBRARY_FIELD, libraryIndex.getSearchIndexId() ) ), Occur.MUST ));
    	
    	queryResults = executeQuery( searcher, query, resolveContent ? null : nonContentAttrs );
    	
    	for (Document doc : queryResults) {
    		searchResults.add( new LibrarySearchResult( doc, repositoryManager, this ) );
    	}
    	return searchResults;
    }
    
    /**
     * Returns the list of OTM releases that reference the library with the specified search index ID.
     * 
     * @param libraryIndex  the index search result for the library to use as the target of the where-used search
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<ReleaseSearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<ReleaseSearchResult> getLibraryReleases(LibrarySearchResult libraryIndex, boolean resolveContent)
    		throws RepositoryException {
        IndexSearcher searcher = null;
        try {
        	searchLock.readLock().lock();
        	List<ReleaseSearchResult> searchResults = new ArrayList<>();
        	BooleanQuery query = newSearchIndexQuery();
        	List<Document> queryResults;
        	
            searcher = searchManager.acquire();
            
        	query.add( new BooleanClause( new TermQuery(
    				new Term( ENTITY_TYPE_FIELD, Release.class.getName() ) ), Occur.MUST ));
        	query.add( new BooleanClause( new TermQuery(
    				new Term( REFERENCED_LIBRARY_FIELD, libraryIndex.getSearchIndexId() ) ), Occur.MUST ));
        	
        	queryResults = executeQuery( searcher, query, resolveContent ? null : nonContentAttrs );
        	
        	for (Document doc : queryResults) {
        		searchResults.add( new ReleaseSearchResult( doc, this ) );
        	}
        	return searchResults;
        	
        } catch (Exception e) {
            throw new RepositoryException(
            		WHERE_USED_QUERY_ERROR + libraryIndex.getSearchIndexId(), e);

        } finally {
            try {
                if (searcher != null) searchManager.release(searcher);

            } catch (Exception e) {
                log.error(ERROR_RELEASING_INDEX_SEARCHER, e);
            }
        	searchLock.readLock().unlock();
        }
    }
    
    /**
     * Returns the entity with the specified search index ID.
     * 
     * @param searchIndexId  the search index ID of the entity to retrieve
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return EntitySearchResult
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public EntitySearchResult getEntity(String searchIndexId, boolean resolveContent) throws RepositoryException {
    	List<EntitySearchResult> results = getEntities( Arrays.asList( searchIndexId ), resolveContent );
    	return results.isEmpty() ? null : results.get( 0 );
    }
    
    /**
     * Returns the entity with the given namespace and local name.
     * 
     * @param libraryIndexId  the search index ID of the owning library for which to retrieve entities
     * @param entityName  the local name of the entity to retrieve
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return EntitySearchResult
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public EntitySearchResult getEntity(String libraryIndexId, String entityName, boolean resolveContent)
    		throws RepositoryException {
    	EntitySearchResult searchResult = null;
    	
    	if (libraryIndexId != null) {
        	BooleanQuery query = newSearchIndexQuery();
        	List<Document> queryResults;
        	
        	query.add( new BooleanClause( new TermQuery(
    				new Term( ENTITY_TYPE_FIELD, TLLibrary.class.getName() ) ), Occur.MUST_NOT ));
        	query.add( new BooleanClause( new TermQuery(
    				new Term( OWNING_LIBRARY_FIELD, libraryIndexId ) ), Occur.MUST ));
        	query.add( new BooleanClause( new TermQuery(
    				new Term( ENTITY_NAME_FIELD, entityName ) ), Occur.MUST ));
        	queryResults = executeQuery( query, resolveContent ? null : nonContentAttrs );
        	
        	if (!queryResults.isEmpty()) {
        		searchResult = new EntitySearchResult( queryResults.get(0), this );
        	}
    	}
    	return searchResult;
    }
    
    /**
     * Returns all entities that are owned by the library the specified search index ID.
     * 
     * @param libraryIndexId  the search index ID of the owning library for which to retrieve entities
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<EntitySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<EntitySearchResult> getEntities(String libraryIndexId, boolean resolveContent) throws RepositoryException {
    	List<EntitySearchResult> searchResults = new ArrayList<>();
    	
    	if (libraryIndexId != null) {
        	BooleanQuery query = newSearchIndexQuery();
        	List<Document> queryResults;
        	
        	query.add( new BooleanClause( new TermQuery(
    				new Term( ENTITY_TYPE_FIELD, TLLibrary.class.getName() ) ), Occur.MUST_NOT ));
        	query.add( new BooleanClause( new TermQuery(
    				new Term( OWNING_LIBRARY_FIELD, libraryIndexId ) ), Occur.MUST ));
        	queryResults = executeQuery( query, resolveContent ? null : nonContentAttrs );
        	
        	for (Document doc : queryResults) {
        		searchResults.add( new EntitySearchResult( doc, this ) );
        	}
    	}
    	return searchResults;
    }
    
    /**
     * Returns a list of entities that correspond to the search index ID's provided.
     * 
     * @param searchIndexIds  the search index ID's for the entities to retrieve
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<EntitySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<EntitySearchResult> getEntities(List<String> searchIndexIds, boolean resolveContent) throws RepositoryException {
    	List<EntitySearchResult> searchResults = new ArrayList<>();
    	
    	if ((searchIndexIds != null) && !searchIndexIds.isEmpty()) {
        	BooleanQuery identityQuery = new BooleanQuery();
        	BooleanQuery masterQuery = newSearchIndexQuery();
        	List<Document> queryResults;
        	
        	for (String searchIndexId : searchIndexIds) {
        		identityQuery.add( new BooleanClause( new TermQuery(
        				new Term( IDENTITY_FIELD, searchIndexId ) ), Occur.SHOULD ));
        	}
        	masterQuery.add( new BooleanClause( new TermQuery(
    				new Term( ENTITY_TYPE_FIELD, TLLibrary.class.getName() ) ), Occur.MUST_NOT ));
        	masterQuery.add( identityQuery, Occur.MUST );
        	queryResults = executeQuery( masterQuery, resolveContent ? null : nonContentAttrs );
        	
        	for (Document doc : queryResults) {
        		searchResults.add( new EntitySearchResult( doc, this ) );
        	}
    	}
    	return searchResults;
    }
    
    /**
     * Returns a list of entities whose 'referenceName' values correspond to the search index ID's provided.
     * 
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @param searchIndexIds  the search index ID's for the entities to retrieve
     * @return List<EntitySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<EntitySearchResult> getEntitiesByReferenceIdentity(boolean resolveContent, String... searchIndexIds) throws RepositoryException {
    	List<EntitySearchResult> searchResults = new ArrayList<>();
    	
    	if (searchIndexIds.length > 0) {
        	BooleanQuery identityQuery = new BooleanQuery();
        	BooleanQuery masterQuery = newSearchIndexQuery();
        	List<Document> queryResults;
        	
        	for (String searchIndexId : searchIndexIds) {
        		identityQuery.add( new BooleanClause( new TermQuery(
        				new Term( REFERENCE_IDENTITY_FIELD, searchIndexId ) ), Occur.SHOULD ));
        	}
        	masterQuery.add( new BooleanClause( new TermQuery(
    				new Term( ENTITY_TYPE_FIELD, TLLibrary.class.getName() ) ), Occur.MUST_NOT ));
        	masterQuery.add( identityQuery, Occur.MUST );
        	queryResults = executeQuery( masterQuery, resolveContent ? null : nonContentAttrs );
        	
        	for (Document doc : queryResults) {
        		searchResults.add( new EntitySearchResult( doc, this ) );
        	}
    	}
    	return searchResults;
    }
    
    /**
     * Returns the list of entities that extend the entity with the specified index ID.
     * 
     * @param entityIndex  the entity for which to return the list of extending entities
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<EntitySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<EntitySearchResult> getExtendedByEntities(EntitySearchResult entityIndex, boolean resolveContent)
    		throws RepositoryException {
    	String entityIndexId = (entityIndex == null) ? null : entityIndex.getSearchIndexId();
        try {
    		Query query = new TermQuery( new Term( EXTENDS_ENTITY_FIELD, entityIndexId ) );
        	List<Document> queryResults = executeQuery( query, resolveContent ? null : nonContentAttrs );
        	List<EntitySearchResult> searchResults = new ArrayList<>();
            
        	for (Document doc : queryResults) {
        		searchResults.add( new EntitySearchResult( doc, this ) );
        	}
        	return searchResults;
        	
        } catch (Exception e) {
            throw new RepositoryException(
            		"Error retrieving extensions for entity: " + entityIndexId, e);
        }
    }
    
    /**
     * Returns the list of entities that directly (and possibly indirectly) reference the entity with the
     * specified entity index.
     * 
     * @param entityIndex  the index search result for the entity to use as the target of the where-used search
     * @param includeIndirectReferences  flag indicating whether indirect where-used references should be included
     *									 in the search results
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<LibrarySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<EntitySearchResult> getEntityWhereUsed(EntitySearchResult entityIndex, boolean includeIndirectReferences,
    		boolean resolveContent) throws RepositoryException {
        IndexSearcher searcher = null;
        try {
        	searchLock.readLock().lock();
        	List<EntitySearchResult> searchResults = new ArrayList<>();
        	
            searcher = searchManager.acquire();
            
            if (includeIndirectReferences) {
            	getIndirectEntityWhereUsed( searcher, entityIndex, new HashSet<String>(), searchResults, resolveContent );
            	
            } else {
            	searchResults.addAll( getDirectEntityWhereUsed( searcher, entityIndex, resolveContent ) );
            }
            return searchResults;
            
        } catch (Exception e) {
            throw new RepositoryException(
            		WHERE_USED_QUERY_ERROR + entityIndex.getSearchIndexId(), e);

        } finally {
            try {
                if (searcher != null) searchManager.release(searcher);

            } catch (Exception e) {
                log.error(ERROR_RELEASING_INDEX_SEARCHER, e);
            }
        	searchLock.readLock().unlock();
        }
    }
    
    /**
     * Performs a recursive where-used search to identify all entities that reference the given entity
     * via both direct and indirect references.
     * 
     * @param searcher  the searcher to use for the query operation(s)
     * @param entityIndex  the index search result for the entity to use as the target of the where-used search
     * @param foundIndexIds  cumulative list of entity search index ID's found during previous queries
     * @param searchResults  cumulative list of where-used search results
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    private void getIndirectEntityWhereUsed(IndexSearcher searcher, EntitySearchResult entityIndex,
    		Set<String> foundIndexIds, List<EntitySearchResult> searchResults, boolean resolveContent)
    				throws RepositoryException {
    	List<EntitySearchResult> directWhereUsed = getDirectEntityWhereUsed( searcher, entityIndex, resolveContent );
    	
    	for (EntitySearchResult dwuEntity : directWhereUsed) {
    		String dwuIndexId = dwuEntity.getSearchIndexId();
    		
    		if (!foundIndexIds.contains( dwuIndexId )) {
    			searchResults.add( dwuEntity );
    			foundIndexIds.add( dwuIndexId );
    			getIndirectEntityWhereUsed( searcher, dwuEntity, foundIndexIds, searchResults, resolveContent );
    		}
    	}
    }
    
    /**
     * Executes a single query to identify the entities that directly reference the given one.
     * 
     * @param searcher  the searcher to use for the query operation
     * @param entityIndex  the index search result for the entity to use as the target of the where-used search
     * @param resolveContent  flag indicating whether the content DETAILS should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<EntitySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    private List<EntitySearchResult> getDirectEntityWhereUsed(IndexSearcher searcher, EntitySearchResult entityIndex,
    		boolean resolveContent) throws RepositoryException {
    	List<EntitySearchResult> searchResults = new ArrayList<>();
    	BooleanQuery masterQuery = newSearchIndexQuery();
    	BooleanQuery identityQuery = new BooleanQuery();
    	List<Document> queryResults;
    	
    	for (String referenceIdentityId : entityIndex.getReferenceIdentityIds()) {
    		identityQuery.add( new BooleanClause( new TermQuery(
    				new Term( REFERENCED_ENTITY_FIELD, referenceIdentityId ) ), Occur.SHOULD ));
    	}
    	masterQuery.add( new BooleanClause( new TermQuery(
				new Term( ENTITY_TYPE_FIELD, TLLibrary.class.getName() ) ), Occur.MUST_NOT ));
    	masterQuery.add( identityQuery, Occur.MUST );
    	
    	queryResults = executeQuery( searcher, masterQuery, resolveContent ? null : nonContentAttrs );
    	
    	for (Document doc : queryResults) {
    		searchResults.add( new EntitySearchResult( doc, this ) );
    	}
    	return searchResults;
    }
    
    public List<SubscriptionSearchResult> getSubscriptions(String userId) throws RepositoryException {
    	List<SubscriptionSearchResult> searchResults = new ArrayList<>();
    	Map<String,SubscriptionSearchResult> resultMap = new HashMap<>();
    	BooleanQuery query = new BooleanQuery();
    	List<Document> queryResults;
    	
    	query.add( new BooleanClause( new TermQuery(
				new Term( ENTITY_TYPE_FIELD, Subscription.class.getName() ) ), Occur.MUST ));
    	query.add( new BooleanClause( new TermQuery(
				new Term( USERID_FIELD, userId ) ), Occur.MUST ));
    	queryResults = executeQuery( query, null );
    	
    	for (Document doc : queryResults) {
    		SubscriptionEventType eventType = SubscriptionEventType.valueOf( doc.get( EVENT_TYPE_FIELD ) );
    		String baseNamespace = doc.get( BASE_NAMESPACE_FIELD );
    		String libraryName = doc.get( LIBRARY_NAME_FIELD );
    		String version = doc.get( VERSION_FIELD );
    		String resultKey = baseNamespace + ":" + libraryName + ":" + version;
    		
    		resultMap.computeIfAbsent( resultKey, k -> {
    			SubscriptionTarget subscriptionTarget = new SubscriptionTarget();
    			
        		subscriptionTarget.setBaseNamespace( doc.get( BASE_NAMESPACE_FIELD ) );
        		subscriptionTarget.setLibraryName( doc.get( LIBRARY_NAME_FIELD ) );
        		subscriptionTarget.setVersion( doc.get( VERSION_FIELD ) );
    			return resultMap.put( k, new SubscriptionSearchResult( subscriptionTarget, userId ) );
    		} );
    		resultMap.get( resultKey ).getEventTypes().add( eventType );
    	}
    	searchResults.addAll( resultMap.values() );
    	Collections.sort( searchResults );
    	return searchResults;
    }
    
    /**
     * Returns the validation findings for the library with the specified search index ID.
     * 
     * @param libraryIndexId  the search index ID of the library for which to retrieve findings
     * @return List<ValidationResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<ValidationResult> getLibraryFindings(String libraryIndexId) throws RepositoryException {
    	return getValidationFindings( libraryIndexId, TARGET_LIBRARY_FIELD );
    }
    
    /**
     * Returns the validation findings for the entity with the specified search index ID.
     * 
     * @param entityIndexId  the search index ID of the entity for which to retrieve findings
     * @return List<ValidationResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<ValidationResult> getEntityFindings(String entityIndexId) throws RepositoryException {
    	return getValidationFindings( entityIndexId, TARGET_ENTITY_FIELD );
    }
    
    /**
     * Returns the validation findings for the library or entity with the specified search index ID.
     * 
     * @param searchIndexId  the search index ID of the library or entity for which to retrieve findings
     * @param searchField  the field to be queried on the validation finding index documents
     * @return List<ValidationResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    private List<ValidationResult> getValidationFindings(String searchIndexId, String searchField) throws RepositoryException {
        try {
    		Query query = new TermQuery( new Term( searchField, searchIndexId ) );
        	List<Document> queryResults = executeQuery( query, null );
        	List<ValidationResult> searchResults = new ArrayList<>();
            
        	for (Document doc : queryResults) {
        		searchResults.add( new ValidationResult( doc, this ) );
        	}
        	return searchResults;
        	
        } catch (Exception e) {
            throw new RepositoryException(
            		"Error retrieving validation findings for target: " + searchIndexId, e);
        }
    }
    
    /**
     * Executes the given query and returns the resulting list of search index documents.
     * 
     * @param query  the search index query to execute
     * @param fieldSet  the set of fields that should be returned in the resulting documents (null = all fields)
     * @return List<Document>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    protected List<Document> executeQuery(Query query, Set<String> fieldSet) throws RepositoryException {
        IndexSearcher searcher = null;
        try {
        	searchLock.readLock().lock();
            searcher = searchManager.acquire();
            
            return executeQuery( searcher, query, fieldSet );
            
        } catch (Exception e) {
            throw new RepositoryException(
            		"Error executing search index query: \"" + query.toString() + "\"", e);

        } finally {
            try {
                if (searcher != null) searchManager.release(searcher);

            } catch (Exception e) {
                log.error(ERROR_RELEASING_INDEX_SEARCHER, e);
            }
        	searchLock.readLock().unlock();
        }
    }
    
    /**
     * Executes the given query and returns the resulting list of search index documents.
     * 
     * @param searcher  the searcher to use for the query operation
     * @param query  the search index query to execute
     * @param fieldSet  the set of fields that should be returned in the resulting documents (null = all fields)
     * @return List<Document>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    protected List<Document> executeQuery(IndexSearcher searcher, Query query, Set<String> fieldSet) throws RepositoryException {
        try {
        	List<Document> docList = new ArrayList<>();
    		TopDocs queryResults;
    		
    		if (log.isDebugEnabled()) {
    			log.debug("Executing Query - " + query.toString());
    		}
    		
            queryResults = searcher.search( query, Integer.MAX_VALUE );
        	
            for (ScoreDoc queryDoc : queryResults.scoreDocs) {
            	Document doc = (fieldSet == null) ?
            			searcher.doc( queryDoc.doc ) : searcher.doc( queryDoc.doc, fieldSet );
            	
            	docList.add( doc );
            }
            return docList;
            
        } catch (Exception e) {
            throw new RepositoryException(
            		"Error executing search index query: \"" + query.toString() + "\"", e);
        }
    }
    
    /**
     * Returns the raw search index document for an OTM library or entity.
     * 
     * @param searchIndexId  the search index ID of the document to retrieve
     * @return Document
     * @throws RepositoryException  thrown if an error occurs while retrieving the document
     */
    protected Document getSearchIndexDocument(String searchIndexId) throws RepositoryException {
        IndexSearcher searcher = null;
        try {
        	searchLock.readLock().lock();
    		Query query = new TermQuery( new Term( IDENTITY_FIELD, searchIndexId ) );
    		TopDocs queryResults;
    		Document doc = null;
    		
            searcher = searchManager.acquire();
            queryResults = searcher.search( query, 1 );
            
            if (queryResults.scoreDocs.length == 1) {
            	doc = searcher.doc( queryResults.scoreDocs[0].doc, contentAttr );
            			
            } else if (queryResults.scoreDocs.length == 0) {
            	throw new RepositoryException("Item not found in search index: " + searchIndexId);
            	
            } else {
            	throw new RepositoryException("Abmiguous results.  Multiple entities found in search index with ID: " + searchIndexId);
            }
            return doc;
            
        } catch (Exception e) {
            throw new RepositoryException(
            		"Error retrieving search index content: " + searchIndexId, e);

        } finally {
            try {
                if (searcher != null) searchManager.release(searcher);

            } catch (Exception e) {
            	log.error("Error releasing free-text searcher.", e);
            }
        	searchLock.readLock().unlock();
        }
    }
    
    /**
     * Constructs a new <code>BooleanQuery</code> to be used for retrieving search index terms.
     * 
     * @return BooleanQuery
     */
    private BooleanQuery newSearchIndexQuery() {
    	BooleanQuery query = new BooleanQuery();
    	
		query.add( new BooleanClause( new TermQuery(
				new Term( SEARCH_INDEX_FIELD, Boolean.TRUE.toString() ) ), Occur.MUST ));
    	return query;
    }
    
}

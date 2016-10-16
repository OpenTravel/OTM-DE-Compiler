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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
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
import org.opentravel.schemacompiler.index.builder.IndexBuilderFactory;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * Repository service that uses Apache Lucene for indexing of reposited artifacts and free-text
 * searching.
 * 
 * @author S. Livezey
 */
public abstract class FreeTextSearchService implements IndexingTerms {
	
	private static final Set<String> nonContentAttrs = new HashSet<>( Arrays.asList(
			IDENTITY_FIELD, ENTITY_TYPE_FIELD, ENTITY_NAME_FIELD, ENTITY_NAMESPACE_FIELD,
			BASE_NAMESPACE_FIELD, FILENAME_FIELD, VERSION_FIELD, VERSION_SCHEME_FIELD,
			STATUS_FIELD, LOCKED_BY_USER_FIELD, REFERENCED_LIBRARY_FIELD, PREFIX_MAPPING_FIELD,
			OWNING_LIBRARY_FIELD, EXTENDS_ENTITY_FIELD, REFERENCE_IDENTITY_FIELD,
			FACET_OWNER_FIELD
		) );
	private static final Set<String> contentAttr = new HashSet<>( Arrays.asList( CONTENT_DATA_FIELD ) );
	
    private static Log log = LogFactory.getLog(FreeTextSearchService.class);
    
    protected static Object indexingLock = new Object();

    private File indexLocation;
    private RepositoryManager repositoryManager;
    private boolean isRunning = false;
    private Directory indexDirectory;
    private IndexWriterConfig writerConfig;
    private IndexWriter indexWriter;
    private SearcherManager searchManager;

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
    public synchronized void startService() throws IOException {
        if (isRunning) {
            throw new IllegalStateException(
                    "Unable to start - the indexing service is already running.");
        }

        // Check to make sure the index was properly closed, and release any lock that might exist
        this.indexDirectory = FSDirectory.open(indexLocation.toPath());

        // Configure the indexing and search components
        this.writerConfig = new IndexWriterConfig(new StandardAnalyzer());
        this.writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
        this.indexWriter = new IndexWriter(indexDirectory, writerConfig);
        this.searchManager = new SearcherManager(indexWriter, true, new SearcherFactory());
        this.isRunning = true;
    }

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
            searchManager.close();
            indexWriter.close();
            indexDirectory.close();

        } finally {
            searchManager = null;
            indexWriter = null;
            indexDirectory = null;
        }
        isRunning = false;
    }

    /**
     * Returns true if the service is running.
     * 
     * @return boolean
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
	 * Returns the index writer for the search service (null if the service is not running).
	 *
	 * @return IndexWriter
	 */
	public IndexWriter getIndexWriter() {
		return indexWriter;
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
        if (indexWriter == null) {
            throw new IllegalStateException(
                    "Unable to perform indexing task - the service is not currently running.");
        }

        // First, clear the contents of the entire index
        deleteSearchIndex();

        // Retrieve and index all items in the repository
    	List<RepositoryItem> itemsToIndex = new ArrayList<>();
    	
        for (String baseNamespace : repositoryManager.listBaseNamespaces()) {
            for (RepositoryItem item : repositoryManager.listItems(baseNamespace, false, true)) {
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
    	IndexBuilderFactory factory = new IndexBuilderFactory( repositoryManager, indexWriter );
    	List<RepositoryItem> itemsToIndex = new ArrayList<>();
    	String baseNamespace = item.getBaseNamespace();
    	String libraryName = item.getLibraryName();
    	
        // Find all versions of this library and re-index all of them
        for (RepositoryItem candidateItem : repositoryManager.listItems(baseNamespace, false, true)) {
            if (libraryName.equals( candidateItem.getLibraryName() )) {
        		itemsToIndex.add( candidateItem );
            }
        }
    	submitIndexingJob( itemsToIndex, false );
        
        // Re-index all libraries that directly reference this repository item to ensure
        // that the validation results are accurate
        indexWhereUsedLibraries( item, factory );
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
    	IndexBuilderFactory factory = new IndexBuilderFactory( repositoryManager, indexWriter );
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
        indexWhereUsedLibraries( item, factory );
    }
    
    /**
     * Updates the indexes for all libraries that directly reference the given repository
     * item.
     * 
     * @param item  the repository item for which to re-index all where-used libraries
     * @param factory  the index builder factory to use when creating new index builders
     * @throws RepositoryException  thrown if an error occurs during the indexing operation
     */
    private void indexWhereUsedLibraries(RepositoryItem item, IndexBuilderFactory factory) throws RepositoryException {
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
     * Submits the given index builder for processing.
     * 
     * @param itemsToIndex  the list of repository items to be indexed
     * @param deleteIndex  flag indicating whether the item's index is to be created or deleted
     */
    protected abstract void submitIndexingJob(List<RepositoryItem> itemsToIndex, boolean deleteIndex);

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
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<SearchResult<?>>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<SearchResult<?>> search(String freeText, TLLibraryStatus includeStatus,
    		boolean latestVersionsOnly, boolean resolveContent) throws RepositoryException {
    	try {
			Query keywordQuery = new QueryParser( KEYWORDS_FIELD, new StandardAnalyzer()).parse( freeText );
			BooleanQuery statusQuery = null;
			Query latestVersionQuery = null;
			
			// Construct the search query...
			if (!latestVersionsOnly && (includeStatus != null) && (includeStatus != TLLibraryStatus.DRAFT)) {
				statusQuery = new BooleanQuery();
				
				switch (includeStatus) {
					case UNDER_REVIEW:
						statusQuery.add( new BooleanClause( new TermQuery(
								new Term( STATUS_FIELD, TLLibraryStatus.UNDER_REVIEW.toString() ) ), Occur.SHOULD ));
					case FINAL:
						statusQuery.add( new BooleanClause( new TermQuery(
								new Term( STATUS_FIELD, TLLibraryStatus.FINAL.toString() ) ), Occur.SHOULD ));
					case OBSOLETE:
						statusQuery.add( new BooleanClause( new TermQuery(
								new Term( STATUS_FIELD, TLLibraryStatus.OBSOLETE.toString() ) ), Occur.SHOULD ));
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
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return LibrarySearchResult
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public LibrarySearchResult getLibrary(String searchIndexId, boolean resolveContent) throws RepositoryException {
    	List<LibrarySearchResult> results = getLibraries( Arrays.asList( searchIndexId ), resolveContent );
    	return (results.size() == 0) ? null : results.get( 0 );
    }
    
    /**
     * Returns the library from the search index that is associated with the given repository item.
     * 
     * @param repositoryItem  the repository item for which to return the library from the search index
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return LibrarySearchResult
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public LibrarySearchResult getLibrary(RepositoryItem item, boolean resolveContent) throws RepositoryException {
    	LibrarySearchResult searchResult = null;
    	BooleanQuery query = newSearchIndexQuery();
    	
		query.add( new BooleanClause( new TermQuery(
				new Term( ENTITY_TYPE_FIELD, TLLibrary.class.getName() ) ), Occur.MUST ));
		query.add( new BooleanClause( new TermQuery(
				new Term( BASE_NAMESPACE_FIELD, item.getBaseNamespace() ) ), Occur.MUST ));
		query.add( new BooleanClause( new TermQuery(
				new Term( FILENAME_FIELD, item.getFilename() ) ), Occur.MUST ));
		query.add( new BooleanClause( new TermQuery(
				new Term( VERSION_FIELD, item.getVersion() ) ), Occur.MUST ));
    	List<Document> queryResults = executeQuery( query, resolveContent ? null : nonContentAttrs );
    	
    	if (queryResults.size() > 0) {
    		searchResult = new LibrarySearchResult( queryResults.get( 0 ), repositoryManager, this );
    	}
    	return searchResult;
    }
    
    /**
     * Returns a list of libraries that correspond to the search index ID's provided.
     * 
     * @param searchIndexIds  the search index ID's for the libraries to retrieve
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<LibrarySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<LibrarySearchResult> getLibraries(Collection<String> searchIndexIds, boolean resolveContent) throws RepositoryException {
    	List<LibrarySearchResult> searchResults = new ArrayList<>();
    	
    	if ((searchIndexIds != null) && (searchIndexIds.size() > 0)) {
        	BooleanQuery identityQuery = new BooleanQuery();
        	BooleanQuery masterQuery = newSearchIndexQuery();
        	List<Document> queryResults;
        	
        	for (String searchIndexId : searchIndexIds) {
        		identityQuery.add( new BooleanClause( new TermQuery(
        				new Term( IDENTITY_FIELD, searchIndexId ) ), Occur.SHOULD ));
        	}
        	masterQuery.add( new BooleanClause( new TermQuery(
    				new Term( ENTITY_TYPE_FIELD, TLLibrary.class.getName() ) ), Occur.MUST ));
        	masterQuery.add( identityQuery, Occur.MUST );
        	queryResults = executeQuery( masterQuery, resolveContent ? null : nonContentAttrs );
        	
        	for (Document doc : queryResults) {
        		searchResults.add( new LibrarySearchResult( doc, repositoryManager, this ) );
        	}
    	}
    	return searchResults;
    }
    
    /**
     * Returns the list of libraries that are locked by the specified user.  If the user ID provided
     * is null, this method will return a list of all repository items that are locked by any user.
     * 
     * @param userId  the ID if the user for which to return locked libraries (null for all users)
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
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
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<LibrarySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<LibrarySearchResult> getLibraryWhereUsed(LibrarySearchResult libraryIndex, boolean includeIndirectReferences,
    		boolean resolveContent) throws RepositoryException {
        IndexSearcher searcher = null;
        try {
        	List<LibrarySearchResult> searchResults = new ArrayList<>();
    		searchManager.maybeRefreshBlocking();
            searcher = searchManager.acquire();
            
            if (includeIndirectReferences) {
            	getIndirectLibraryWhereUsed( searcher, libraryIndex, new HashSet<String>(), searchResults, resolveContent );
            	
            } else {
            	searchResults.addAll( getDirectLibraryWhereUsed( searcher, libraryIndex, resolveContent ) );
            }
            return searchResults;
            
        } catch (Exception e) {
            throw new RepositoryException(
            		"Error executing where-used query for entity: " + libraryIndex.getSearchIndexId(), e);

        } finally {
            try {
                if (searcher != null) searchManager.release(searcher);

            } catch (IOException e) {
                log.error("Error releasing index searcher.", e);
            }
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
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
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
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
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
     * Returns the entity with the specified search index ID.
     * 
     * @param searchIndexId  the search index ID of the entity to retrieve
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return EntitySearchResult
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public EntitySearchResult getEntity(String searchIndexId, boolean resolveContent) throws RepositoryException {
    	List<EntitySearchResult> results = getEntities( Arrays.asList( searchIndexId ), resolveContent );
    	return (results.size() == 0) ? null : results.get( 0 );
    }
    
    /**
     * Returns the entity with the given namespace and local name.
     * 
     * @param libraryIndexId  the search index ID of the owning library for which to retrieve entities
     * @param entityName  the local name of the entity to retrieve
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
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
        	
        	if (queryResults.size() > 0) {
        		searchResult = new EntitySearchResult( queryResults.get(0), this );
        	}
    	}
    	return searchResult;
    }
    
    /**
     * Returns all entities that are owned by the library the specified search index ID.
     * 
     * @param libraryIndexId  the search index ID of the owning library for which to retrieve entities
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
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
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<EntitySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<EntitySearchResult> getEntities(List<String> searchIndexIds, boolean resolveContent) throws RepositoryException {
    	List<EntitySearchResult> searchResults = new ArrayList<>();
    	
    	if ((searchIndexIds != null) && (searchIndexIds.size() > 0)) {
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
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
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
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<EntitySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<EntitySearchResult> getExtendedByEntities(EntitySearchResult entityIndex, boolean resolveContents)
    		throws RepositoryException {
    	String entityIndexId = (entityIndex == null) ? null : entityIndex.getSearchIndexId();
        try {
    		Query query = new TermQuery( new Term( EXTENDS_ENTITY_FIELD, entityIndexId ) );
        	List<Document> queryResults = executeQuery( query, null );
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
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<LibrarySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    public List<EntitySearchResult> getEntityWhereUsed(EntitySearchResult entityIndex, boolean includeIndirectReferences,
    		boolean resolveContent) throws RepositoryException {
        IndexSearcher searcher = null;
        try {
        	List<EntitySearchResult> searchResults = new ArrayList<>();
    		searchManager.maybeRefreshBlocking();
            searcher = searchManager.acquire();
            
            if (includeIndirectReferences) {
            	getIndirectEntityWhereUsed( searcher, entityIndex, new HashSet<String>(), searchResults, resolveContent );
            	
            } else {
            	searchResults.addAll( getDirectEntityWhereUsed( searcher, entityIndex, resolveContent ) );
            }
            return searchResults;
            
        } catch (Exception e) {
            throw new RepositoryException(
            		"Error executing where-used query for entity: " + entityIndex.getSearchIndexId(), e);

        } finally {
            try {
                if (searcher != null) searchManager.release(searcher);

            } catch (IOException e) {
                log.error("Error releasing index searcher.", e);
            }
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
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
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
     * @param resolveContent  flag indicating whether the content details should be pre-resolved; if false, content
     *						  is still available in the search results, but it will be initialized in a lazy fashion
     * @return List<EntitySearchResult>
     * @throws RepositoryException  thrown if an error occurs while performing the search
     */
    private List<EntitySearchResult> getDirectEntityWhereUsed(IndexSearcher searcher, EntitySearchResult entityIndex,
    		boolean resolveContent) throws RepositoryException {
    	List<EntitySearchResult> searchResults = new ArrayList<>();
    	BooleanQuery masterQuery = new BooleanQuery();
    	BooleanQuery identityQuery = newSearchIndexQuery();
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
    public List<ValidationResult> getValidationFindings(String searchIndexId, String searchField) throws RepositoryException {
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
    		searchManager.maybeRefreshBlocking();
            searcher = searchManager.acquire();
            
            return executeQuery( searcher, query, fieldSet );
        } catch (Exception e) {
            throw new RepositoryException(
            		"Error executing search index query: \"" + query.toString() + "\"", e);

        } finally {
            try {
                if (searcher != null) searchManager.release(searcher);

            } catch (IOException e) {
                log.error("Error releasing index searcher.", e);
            }
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
    		searchManager.maybeRefreshBlocking();
            searcher = searchManager.acquire();
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
    		Query query = new TermQuery( new Term( IDENTITY_FIELD, searchIndexId ) );
    		TopDocs queryResults;
    		Document doc = null;
    		
    		searchManager.maybeRefreshBlocking();
            searcher = searchManager.acquire();
            queryResults = searcher.search( query, 1 );
            
            if (queryResults.scoreDocs.length == 1) {
            	doc = searcher.doc( queryResults.scoreDocs[0].doc, contentAttr );
            			
            } else if (queryResults.scoreDocs.length == 1) {
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

            } catch (IOException e) {
            	log.error("Error releasing free-text searcher.", e);
            }
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

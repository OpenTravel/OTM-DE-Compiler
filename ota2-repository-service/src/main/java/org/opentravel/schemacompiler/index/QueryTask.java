/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemacompiler.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;

/**
 * Performs a free-text search of the repository and returns the results as
 * a list of repository items.
 * 
 * @author S. Livezey
 */
public class QueryTask extends AbstractFreeTextSearchTask {
	
	public static final int MAX_SEARCH_RESULTS = 50;
	
	private static Log log = LogFactory.getLog(QueryTask.class);
	
	private SearcherManager searchManager;
	private Analyzer indexAnalyzer;
	
	/**
	 * Constructor that provides the search manager to use for free-text searches, and the
	 * repository manager to use when accessing all repository content.
	 * 
	 * @param searchManager  the Lucene search manager to use for free-text searches
	 * @param repositoryManager  the repository that owns all content to be indexed
	 */
	public QueryTask(SearcherManager searchManager, RepositoryManager repositoryManager) {
		super( repositoryManager );
		this.searchManager = searchManager;
		this.indexAnalyzer = new StandardAnalyzer(Version.LUCENE_41);
	}
	
	/**
	 * Performs a search of the repository using the index reader provided and returns
	 * the list of matching repository items.
	 * 
	 * @param queryString  the query string for the search
	 * @param latestVersionsOnly  flag indicating whether the results should include all matching versions or just the latest version of each library
	 * @param includeDraftVersions  flag indicating whether items in <code>DRAFT</code> status should be included in the resulting list
	 * @param reader  the index reader to use when performing the search
	 * @throws RepositoryException  thrown if an error occurs while executing the search
	 */
	public List<RepositoryItem> queryRepositoryItems(String queryString, boolean latestVersionsOnly,
			boolean includeDraftVersions) throws RepositoryException {
		List<Document> indexDocuments = new ArrayList<Document>();
		
		// Search the index for matching documents
		refreshSearcher();
		IndexSearcher searcher = searchManager.acquire();
		try {
			Query query = buildQuery( queryString, latestVersionsOnly, includeDraftVersions );
			TopDocs scoredResults = searcher.search( query, MAX_SEARCH_RESULTS );
			
			for (ScoreDoc scoreDoc : scoredResults.scoreDocs) {
				indexDocuments.add( searcher.doc(scoreDoc.doc) );
			}
			
		} catch (Exception e) {
			throw new RepositoryException("Error executing free-text search.", e);
			
		} finally {
			try {
				searchManager.release( searcher );
				
			} catch (IOException e) {
				throw new RepositoryException("Error releasing free-text searcher.", e);
			}
		}
		
		// Attempt to resolve each matching document to a repository item
		List<RepositoryItem> searchResults = new ArrayList<RepositoryItem>();
		
		for (Document indexDoc : indexDocuments) {
			RepositoryItem item = getRepositoryItem( indexDoc );
			
			if (item != null) {
				searchResults.add( item );
			}
		}
		return searchResults;
	}
	
	/**
	 * Builds a Lucene query using the information provided.
	 * 
	 * @param contentTerms  the terms to use for the content portion of the query
	 * @param latestVersionsOnly  flag indicating whether the results should include all matching versions or just the latest version of each library
	 * @param includeDraftVersions  flag indicating whether items in <code>DRAFT</code> status should be included in the resulting list
	 * @return Query
	 * @throws ParseException  thrown if the query text cannot be parsed
	 */
	private Query buildQuery(String contentTerms, boolean latestVersionsOnly, boolean includeDraftVersions) throws ParseException {
		QueryParser parser = new QueryParser(Version.LUCENE_41, CONTENT_FIELD, indexAnalyzer);
		StringBuilder queryText = new StringBuilder( VERSION_TYPE_FIELD ).append(':');
		
		// Create the search term for the 'versionType' field
		if (latestVersionsOnly) {
			if (includeDraftVersions) {
				queryText.append( IndexVersionType.HEAD ).append(" AND ");
			} else {
				queryText.append( IndexVersionType.HEAD_FINAL ).append(" AND ");
			}
		} else {
			queryText.append( IndexVersionType.STANDARD ).append(" AND ");
		}
		
		// If needed, create a search term for the status field
		if (!includeDraftVersions) {
			queryText.append( STATUS_FIELD ).append(':').append( TLLibraryStatus.FINAL.toString() ).append(" AND ");
		}
		
		// Add the remaining content terms for the query
		queryText.append("(").append( contentTerms ).append(")");
		return parser.parse( queryText.toString() );
	}
	
	/**
	 * Refreshes the searcher to be current as of the latest indexing job.
	 */
	private synchronized void refreshSearcher() {
		try {
			searchManager.maybeRefreshBlocking();
			
		} catch (IOException e) {
			log.error("Error refreshing free-text searcher.", e);
		}
	}
	
	/**
	 * Returns the repository item associated with the given Lucene index document, or null if no
	 * such item exists.
	 * 
	 * @param indexDoc  the Lucene index document that was returned in a search result
	 * @return RepositoryItem
	 */
	private RepositoryItem getRepositoryItem(Document indexDoc) {
		String baseNamespace = indexDoc.get( BASE_NAMESPACE_FIELD );
		String filename = indexDoc.get( FILENAME_FIELD );
		String version = indexDoc.get( VERSION_FIELD );
		RepositoryItem item = null;
		try {
			if ((baseNamespace != null) && (filename != null) && (version != null)) {
				LibraryInfoType libraryMetadata = repositoryManager.getFileManager().loadLibraryMetadata(baseNamespace, filename, version);
				RepositoryItem candidateItem = RepositoryUtils.createRepositoryItem( repositoryManager, libraryMetadata );
				File metadataFile = getMetadataFile( candidateItem );
				File contentFile = getContentFile( candidateItem );
				
				if (metadataFile.exists() && contentFile.exists()) {
					item = candidateItem;
				}
			}
			
		} catch (RepositoryException e) {
			log.warn("Repository item found in search index, but could not be located: " + filename);
		}
		return item;
	}
	
	protected void displayIndex() {
		IndexSearcher searcher = searchManager.acquire();
		try {
			IndexReader reader = searcher.getIndexReader();
			
			for (int i = 0; i < reader.maxDoc(); i++) {
				Document doc = reader.document( i );
				
				System.out.println("DOCUMENT: " + doc.get(IDENTITY_FIELD));
				System.out.println("  " + BASE_NAMESPACE_FIELD + " : " + doc.get(BASE_NAMESPACE_FIELD));
				System.out.println("  " + FILENAME_FIELD + " : " + doc.get(FILENAME_FIELD));
				System.out.println("  " + STATUS_FIELD + " : " + doc.get(STATUS_FIELD));
				System.out.println("  " + VERSION_FIELD + " : " + doc.get(VERSION_FIELD));
				System.out.println("  " + VERSION_TYPE_FIELD + " : " + doc.get(VERSION_TYPE_FIELD));
			}
		} catch (Throwable t) {
			t.printStackTrace(System.out);
			
		} finally {
			try {
				searchManager.release( searcher );
				
			} catch (IOException e) {
				// Ignore error and continue
			}
		}
	}
	
}

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
package org.opentravel.schemacompiler.index.builder;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.opentravel.schemacompiler.index.IndexingTerms;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * Base class for all components used to create search index documents for OTM repository
 * artifacts.
 *
 * @param <T>  the type of the repository artifact to be indexed
 */
public abstract class IndexBuilder<T> implements IndexingTerms {
	
    private static Log log = LogFactory.getLog( IndexBuilder.class );

    private static Pattern tokenPattern = Pattern.compile( "[A-Za-z0-9]+" );

	private T sourceObject;
    private IndexBuilderFactory factory;
    private RepositoryManager repositoryManager;
    private IndexWriter indexWriter;
    private boolean createIndex = true;
	private Set<String> freeTextKeywords = new HashSet<>();
	
	/**
	 * Constructs the search index documents using the <code>IndexWriter</code> provided.
	 */
	public void performIndexingAction() {
		try {
			if (createIndex) {
				createIndex();
				
			} else {
				deleteIndex();
			}
			
		} catch (Throwable t) {
			log.error("Unknown error encountered during index processing.", t);
		}
	}
	
	/**
	 * Constructs the search index documents using the <code>IndexWriter</code> provided.
	 */
	protected abstract void createIndex();
	
	/**
	 * Deletes the search index documents using the <code>IndexWriter</code> provided.
	 */
	protected abstract void deleteIndex();
	
	/**
	 * Returns the set of free-text keywords that should be included in the search index document
	 * for the source object.
	 * 
	 * @return Set<String>
	 */
	protected Set<String> getFreeTextKeywords() {
		return freeTextKeywords;
	}
	
	/**
	 * Adds all keywords after tokenizing the given string.
	 * 
	 * @param keywordsStr  string containing the keyword tokens to add
	 */
	protected void addFreeTextKeywords(String keywordsStr) {
		if (keywordsStr != null) {
			String[] keywords = keywordsStr.split( "\\b" );
			
			for (String keyword : keywords) {
				if (tokenPattern.matcher( keyword ).matches()) {
					freeTextKeywords.add( keyword );
				}
			}
		}
	}
	
	/**
	 * Returns a string containing the free-text search tokens for the source object being indexed.
	 * 
	 * @return String
	 */
	protected String getFreeTextSearchContent() {
		StringBuilder content = new StringBuilder();
		
		for (String keyword : freeTextKeywords) {
			content.append( keyword ).append(" ");
		}
		return content.toString();
	}
	
	/**
	 * Returns source object that will provide all content for the search index(es).
	 *
	 * @return T
	 */
	public T getSourceObject() {
		return sourceObject;
	}

	/**
	 * Assigns the source object that will provide all content for the search index(es).
	 *
	 * @param sourceObject  the source object to assign
	 */
	public void setSourceObject(T sourceObject) {
		this.sourceObject = sourceObject;
	}

	/**
	 * Returns the factory that created this <code>IndexBuilder</code>.
	 *
	 * @return IndexBuilderFactory
	 */
	public IndexBuilderFactory getFactory() {
		return factory;
	}

	/**
	 * Assigns the factory that created this <code>IndexBuilder</code>.
	 *
	 * @param factory  the factory instance to assign
	 */
	public void setFactory(IndexBuilderFactory factory) {
		this.factory = factory;
	}

	/**
	 * Returns the manager for the repository that owns the item(s) to be indexed.
	 *
	 * @return RepositoryManager
	 */
	public RepositoryManager getRepositoryManager() {
		return repositoryManager;
	}

	/**
	 * Assigns the manager for the repository that owns the item(s) to be indexed.
	 *
	 * @param repositoryManager  the repository manager to assign
	 */
	public void setRepositoryManager(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}

	/**
	 * Returns the index writer to use for creating or deleting the search index document(s).
	 *
	 * @return IndexWriter
	 */
	public IndexWriter getIndexWriter() {
		return indexWriter;
	}

	/**
	 * Assigns the index writer to use for creating or deleting the search index document(s).
	 *
	 * @param indexWriter  the index writer to assign
	 */
	public void setIndexWriter(IndexWriter indexWriter) {
		this.indexWriter = indexWriter;
	}

	/**
	 * Assigns the flag value indicating whether to create or delete search index document(s).
	 *
	 * @param createIndex  the flag value to assign
	 */
	public void setCreateIndex(boolean createIndex) {
		this.createIndex = createIndex;
	}

}

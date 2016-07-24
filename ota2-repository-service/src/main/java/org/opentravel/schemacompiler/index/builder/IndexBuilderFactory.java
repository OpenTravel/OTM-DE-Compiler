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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * Factory used to construct <code>IndexBuilder</code> components.
 */
public class IndexBuilderFactory {
	
    private static Log log = LogFactory.getLog( IndexBuilderFactory.class );

    private RepositoryManager repositoryManager;
	private IndexWriter indexWriter;
	private ValidationIndexingService validationService;
	
	/**
	 * Constructor that specifies the repository manager and index writer to use when
	 * creating or deleting search index documents.
	 * 
	 * @param repositoryManager  the manager for the repository that owns the item(s) to be indexed
	 * @param indexWriter  the index writer to use for creating or deleting the search index document(s)
	 */
	public IndexBuilderFactory(RepositoryManager repositoryManager, IndexWriter indexWriter) {
		try {
			this.repositoryManager = new IndexingRepositoryManager( repositoryManager.getFileManager() );
			
		} catch (RepositoryException e) {
			log.error("Error configuring the IndexRepositoryManager (using default)", e);
			this.repositoryManager = repositoryManager;
		}
		this.indexWriter = indexWriter;
		this.validationService = new ValidationIndexingService( this.repositoryManager, this.indexWriter );
	}
	
	/**
	 * Returns a new <code>IndexBuilder</code> that will construct search index document(s)
	 * for the given source object.
	 * 
	 * @param <T>  the type of the repository artifact to be indexed
	 * @param indexSource  the source object for the search index document
	 * @return IndexDocumentBuilder<T>
	 */
	public <T> IndexBuilder<T> newCreateIndexBuilder(T indexSource) {
		return newIndexBuilder( indexSource, true );
	}
	
	/**
	 * Returns a new <code>IndexBuilder</code> that will construct search index document(s)
	 * for the given source object.
	 * 
	 * @param <T>  the type of the repository artifact to be indexed
	 * @param indexSource  the source object for the search index document
	 * @return IndexDocumentBuilder<T>
	 */
	public <T> IndexBuilder<T> newDeleteIndexBuilder(T indexSource) {
		return newIndexBuilder( indexSource, false );
	}
	
	/**
	 * Returns a new <code>IndexBuilder</code> that will construct search index document(s)
	 * for the given source object.
	 * 
	 * @param <T>  the type of the repository artifact to be indexed
	 * @param sourceObject  the source object for the search index document
	 * @param createIndex  flag indicating whether the index for the source item is to be created or deleted
	 * @return IndexDocumentBuilder<T>
	 */
	@SuppressWarnings("unchecked")
	private <T> IndexBuilder<T> newIndexBuilder(T sourceObject, boolean createIndex) {
		if (sourceObject == null) {
			throw new NullPointerException("Index source object cannot be null.");
		}
		IndexBuilder<T> builder;
		
		if (sourceObject instanceof RepositoryItem) {
			builder = (IndexBuilder<T>) new LibraryIndexBuilder();
			
		} else if (sourceObject instanceof NamedEntity) {
			builder = (IndexBuilder<T>) new EntityIndexBuilder<>();
			
		} else {
			throw new IllegalArgumentException("No index builder defined for objects of type: "
					+ sourceObject.getClass().getSimpleName() );
		}
		builder.setFactory( this );
		builder.setSourceObject( sourceObject );
		builder.setRepositoryManager( repositoryManager );
		builder.setIndexWriter( indexWriter );
		builder.setCreateIndex( createIndex );
		return builder;
	}

	/**
	 * Returns the service that can be used to perform validations of OTM libraries.
	 *
	 * @return ValidationIndexingService
	 */
	public ValidationIndexingService getValidationService() {
		return validationService;
	}
	
}

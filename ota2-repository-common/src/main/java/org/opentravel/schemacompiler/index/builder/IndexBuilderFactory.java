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

import org.apache.lucene.index.IndexWriter;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemType;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * Factory used to construct <code>IndexBuilder</code> components.
 */
public class IndexBuilderFactory {

    private RepositoryManager repositoryManager;
    private IndexWriter indexWriter;
    private FacetIndexingService facetService;

    /**
     * Constructor that specifies the repository manager and index writer to use when creating or deleting search index
     * documents.
     * 
     * @param repositoryManager the manager for the repository that owns the item(s) to be indexed
     * @param indexWriter the index writer to use for creating or deleting the search index document(s)
     */
    public IndexBuilderFactory(RepositoryManager repositoryManager, IndexWriter indexWriter) {
        this.repositoryManager = repositoryManager;
        this.indexWriter = indexWriter;
        this.facetService = new FacetIndexingService( this.indexWriter );
    }

    /**
     * Returns a new <code>IndexBuilder</code> that will construct search index document(s) for the given source object.
     * 
     * @param indexSource the source object for the search index document
     * @param <T> the type of the repository artifact to be indexed
     * @return IndexDocumentBuilder&lt;T&gt;
     */
    public <T> IndexBuilder<T> newCreateIndexBuilder(T indexSource) {
        return newIndexBuilder( indexSource, true );
    }

    /**
     * Returns a new <code>IndexBuilder</code> that will construct search index document(s) for the given source object.
     * 
     * @param indexSource the source object for the search index document
     * @param <T> the type of the repository artifact to be indexed
     * @return IndexDocumentBuilder&lt;T&gt;
     */
    public <T> IndexBuilder<T> newDeleteIndexBuilder(T indexSource) {
        return newIndexBuilder( indexSource, false );
    }

    /**
     * Returns a new <code>IndexBuilder</code> that will construct search index document(s) for the given source object.
     * 
     * @param indexSource the source object for the search index document
     * @return IndexDocumentBuilder&lt;T&gt;
     */
    public IndexBuilder<SubscriptionTarget> newSubscriptionIndexBuilder(SubscriptionTarget indexSource) {
        return newIndexBuilder( indexSource, true );
    }

    /**
     * Returns a new <code>IndexBuilder</code> that will construct search index document(s) for the given source object.
     * 
     * @param sourceObject the source object for the search index document
     * @param createIndex flag indicating whether the index for the source item is to be created or deleted
     * @param <T> the type of the repository artifact to be indexed
     * @return IndexDocumentBuilder&lt;T&gt;
     */
    @SuppressWarnings({"unchecked", "squid:S1905"})
    private <T> IndexBuilder<T> newIndexBuilder(T sourceObject, boolean createIndex) {
        if (sourceObject == null) {
            throw new NullPointerException( "Index source object cannot be null." );
        }
        IndexBuilder<T> builder;

        if (sourceObject instanceof RepositoryItem) {
            RepositoryItem item = (RepositoryItem) sourceObject;
            RepositoryItemType itemType = RepositoryItemType.fromFilename( item.getFilename() );

            switch (itemType) {
                case LIBRARY:
                    builder = (IndexBuilder<T>) new LibraryIndexBuilder();
                    break;
                case RELEASE:
                    builder = (IndexBuilder<T>) new ReleaseIndexBuilder();
                    break;
                case ASSEMBLY:
                    builder = (IndexBuilder<T>) new AssemblyIndexBuilder();
                    break;
                default:
                    builder = null;
                    break;
            }

        } else if (sourceObject instanceof NamedEntity) {
            builder = (IndexBuilder<T>) new EntityIndexBuilder<>();

        } else if (sourceObject instanceof SubscriptionTarget) {
            builder = (IndexBuilder<T>) new SubscriptionIndexBuilder();

        } else {
            throw new IllegalArgumentException(
                "No index builder defined for objects of type: " + sourceObject.getClass().getSimpleName() );
        }

        if (builder != null) {
            builder.setFactory( this );
            builder.setSourceObject( sourceObject );
            builder.setRepositoryManager( repositoryManager );
            builder.setIndexWriter( indexWriter );
            builder.setCreateIndex( createIndex );
        }
        return builder;
    }

    /**
     * Returns the service that handles post-process indexing of contextual facet owners.
     *
     * @return FacetIndexingService
     */
    public FacetIndexingService getFacetService() {
        return facetService;
    }

}

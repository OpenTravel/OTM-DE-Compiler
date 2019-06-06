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
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;
import org.opentravel.schemacompiler.index.IndexingTerms;
import org.opentravel.schemacompiler.index.IndexingUtils;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.validate.ValidationFinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Indexing service that handles post-processing of contextual-facet meta-data records that were created during the
 * initial indexing pass.
 */
public class FacetIndexingService {

    private static Log log = LogFactory.getLog( FacetIndexingService.class );

    private Set<String> rootFacetOwnerIds = new HashSet<>();
    private IndexWriter indexWriter;

    /**
     * Constructor that specifies the index writer to use for meta-data searches and for the creation of new search
     * index documents.
     * 
     * @param indexWriter the index writer to use for creating the search index document(s)
     */
    public FacetIndexingService(IndexWriter indexWriter) {
        this.indexWriter = indexWriter;
    }

    /**
     * Adds the identity string of a facet owner to the list of root facet owners that will be post-processed by this
     * service. It is not necessary to add contextual facet owners via this method; only business objects and choice
     * records will be processed as root facet owners.
     * 
     * @param ownerId the index identity of the root facet owner
     */
    public void addFacetOwnerID(String ownerId) {
        if (ownerId != null) {
            rootFacetOwnerIds.add( ownerId );
        }
    }

    /**
     * Performs post-process indexing for all root facet owners that have been registered with this service instance.
     */
    private void postProcessFacetOwners() {
        try (SearcherManager searchManager = new SearcherManager( indexWriter, true, new SearcherFactory() )) {
            log.info( "Indexing contextual facet content..." );
            searchManager.maybeRefreshBlocking();
            IndexSearcher searcher = searchManager.acquire();

            for (String facetOwnerId : rootFacetOwnerIds) {
                indexFacetOwner( facetOwnerId, searcher );
            }
            searchManager.release( searcher );

        } catch (IOException e) {
            log.error( "Error during contextual facet post-processing.", e );
        }
    }

    /**
     * Creates a search index document for the specified facet owner.
     * 
     * @param facetOwnerId the search index ID for the facet owner to be indexed
     * @param searcher the index searcher to use for locating the owner's contextual facets
     */
    private void indexFacetOwner(String facetOwnerId, IndexSearcher searcher) {
        try {
            Document facetOwner = getFacetOwnerSearchMetaData( facetOwnerId, searcher );

            if (facetOwner != null) {
                List<Document> contextualFacets = new ArrayList<>();
                Document searchableDoc;

                facetOwnerId = IndexingUtils.getSearchableIdentityKey( facetOwnerId );
                log.debug( "Post-processing contextual facet owner: " + facetOwnerId );
                findContextualFacets( facetOwnerId, contextualFacets, new HashSet<String>(), searcher );
                searchableDoc = createSearchableDocument( facetOwner, contextualFacets );

                indexWriter.updateDocument(
                    new Term( IndexingTerms.IDENTITY_FIELD, IndexingUtils.getSearchableIdentityKey( facetOwnerId ) ),
                    searchableDoc );

            } else {
                log.warn( "Search index meta-data not found for contextual facet owner: " + facetOwnerId );
            }

        } catch (Exception e) {
            log.error( "Unable to create index for contextual facet owner: " + facetOwnerId, e );
        }
    }

    /**
     * Retrieves the search index meta-data document for the facet owner with the given ID.
     * 
     * @param facetOwnerId the search index ID of the facet owner document to retrieve
     * @param searcher the index searcher to use for the query
     * @return Document
     * @throws IOException thrown if an error occurs while executing the search
     */
    private Document getFacetOwnerSearchMetaData(String facetOwnerId, IndexSearcher searcher) throws IOException {
        Query query = new TermQuery(
            new Term( IndexingTerms.IDENTITY_FIELD, IndexingUtils.getNonSearchableIdentityKey( facetOwnerId ) ) );
        TopDocs queryResults = searcher.search( query, Integer.MAX_VALUE );
        Document searchResult = null;

        if (queryResults.totalHits > 0) {
            searchResult = searcher.doc( queryResults.scoreDocs[0].doc );
        }
        return searchResult;
    }

    /**
     * Recursive method used to retrieve all of the contextual facet meta-data records for the given facet owner from
     * the search index.
     * 
     * @param facetOwnerId the search index ID of the facet owner for which to return contextual facets
     * @param contextualFacets the list of contextual facets to be populated
     * @param facetOwnerKeys the collection of facet owner keys that have already been visited
     * @param searcher the index searcher to use for all queries
     * @throws IOException thrown if an error occurs while executing the search
     */
    private void findContextualFacets(String facetOwnerId, List<Document> contextualFacets, Set<String> facetOwnerKeys,
        IndexSearcher searcher) throws IOException {
        if (!facetOwnerKeys.contains( facetOwnerId )) {
            List<Document> childFacets = findDirectChildFacets( facetOwnerId, searcher );

            facetOwnerKeys.add( facetOwnerId );

            for (Document facet : childFacets) {
                contextualFacets.add( facet );
                findContextualFacets( facet.get( IndexingTerms.IDENTITY_FIELD ), contextualFacets, facetOwnerKeys,
                    searcher );
            }
        }
    }

    /**
     * Returns the list of child facet records from the search index whose direct owner is the one specified.
     * 
     * @param facetOwnerId the search index ID of the facet owner for which to return child contextual facets
     * @param searcher the index searcher to use for all queries
     * @return List&lt;Document&gt;
     * @throws IOException thrown if an error occurs while executing the search
     */
    private List<Document> findDirectChildFacets(String facetOwnerId, IndexSearcher searcher) throws IOException {
        List<Document> searchResults = new ArrayList<>();
        BooleanQuery query = new BooleanQuery();
        TopDocs queryResults;

        query.add( new BooleanClause( new TermQuery( new Term( IndexingTerms.FACET_OWNER_FIELD, facetOwnerId ) ),
            Occur.MUST ) );
        query.add( new BooleanClause(
            new TermQuery( new Term( IndexingTerms.ENTITY_TYPE_FIELD, TLContextualFacet.class.getName() ) ),
            Occur.MUST ) );
        query.add( new BooleanClause(
            new TermQuery( new Term( IndexingTerms.SEARCH_INDEX_FIELD, Boolean.FALSE.toString() ) ), Occur.MUST ) );

        queryResults = searcher.search( query, Integer.MAX_VALUE );

        for (ScoreDoc queryDoc : queryResults.scoreDocs) {
            searchResults.add( searcher.doc( queryDoc.doc ) );
        }
        return searchResults;
    }

    /**
     * Constructs a searchable document for the index that contains the combined content of the given facet owner and
     * its constituent contextual facets.
     * 
     * @param facetOwner the facet owner for which to create a searchable index document
     * @param contextualFacets the contextual facets that are associated with the given facet owner
     * @return Document
     */
    private Document createSearchableDocument(Document facetOwner, List<Document> contextualFacets) {
        String identityKey = IndexingUtils.getSearchableIdentityKey( facetOwner.get( IndexingTerms.IDENTITY_FIELD ) );
        String libraryStatus = facetOwner.get( IndexingTerms.STATUS_FIELD );
        String lockedByUser = facetOwner.get( IndexingTerms.LOCKED_BY_USER_FIELD );
        String extendsEntityKey = facetOwner.get( IndexingTerms.EXTENDS_ENTITY_FIELD );
        String entityDescription = facetOwner.get( IndexingTerms.ENTITY_DESCRIPTION_FIELD );
        BytesRef facetOwnerContent = facetOwner.getBinaryValue( IndexingTerms.CONTENT_DATA_FIELD );
        String[] facetOwnerKeys = facetOwner.getValues( IndexingTerms.FACET_OWNER_FIELD );
        String[] referenceIdentityKeys = facetOwner.getValues( IndexingTerms.REFERENCE_IDENTITY_FIELD );
        String[] referencedEntityKeys = facetOwner.getValues( IndexingTerms.REFERENCED_ENTITY_FIELD );
        Document indexDoc = new Document();

        indexDoc.add( new StringField( IndexingTerms.IDENTITY_FIELD, identityKey, Field.Store.YES ) );
        indexDoc.add( new StringField( IndexingTerms.SEARCH_INDEX_FIELD, Boolean.TRUE.toString(), Field.Store.NO ) );
        indexDoc.add( new StringField( IndexingTerms.OWNING_LIBRARY_FIELD,
            facetOwner.get( IndexingTerms.OWNING_LIBRARY_FIELD ), Field.Store.YES ) );
        indexDoc.add( new StringField( IndexingTerms.ENTITY_TYPE_FIELD,
            facetOwner.get( IndexingTerms.ENTITY_TYPE_FIELD ), Field.Store.YES ) );
        indexDoc.add( new StringField( IndexingTerms.ENTITY_NAME_FIELD,
            facetOwner.get( IndexingTerms.ENTITY_NAME_FIELD ), Field.Store.YES ) );
        indexDoc.add( new StringField( IndexingTerms.ENTITY_LOCAL_NAME_FIELD,
            facetOwner.get( IndexingTerms.ENTITY_LOCAL_NAME_FIELD ), Field.Store.YES ) );
        indexDoc.add( new StringField( IndexingTerms.ENTITY_NAMESPACE_FIELD,
            facetOwner.get( IndexingTerms.ENTITY_NAMESPACE_FIELD ), Field.Store.YES ) );
        indexDoc.add( new StringField( IndexingTerms.VERSION_FIELD, facetOwner.get( IndexingTerms.VERSION_FIELD ),
            Field.Store.YES ) );
        indexDoc.add( new StringField( IndexingTerms.LATEST_VERSION_FIELD,
            facetOwner.get( IndexingTerms.LATEST_VERSION_FIELD ), Field.Store.NO ) );
        indexDoc.add( new StringField( IndexingTerms.LATEST_VERSION_AT_UNDER_REVIEW_FIELD,
            facetOwner.get( IndexingTerms.LATEST_VERSION_AT_UNDER_REVIEW_FIELD ), Field.Store.NO ) );
        indexDoc.add( new StringField( IndexingTerms.LATEST_VERSION_AT_FINAL_FIELD,
            facetOwner.get( IndexingTerms.LATEST_VERSION_AT_FINAL_FIELD ), Field.Store.NO ) );
        indexDoc.add( new StringField( IndexingTerms.LATEST_VERSION_AT_OBSOLETE_FIELD,
            facetOwner.get( IndexingTerms.LATEST_VERSION_AT_OBSOLETE_FIELD ), Field.Store.NO ) );

        if (entityDescription != null) {
            indexDoc
                .add( new StringField( IndexingTerms.ENTITY_DESCRIPTION_FIELD, entityDescription, Field.Store.YES ) );
        }
        if (libraryStatus != null) {
            indexDoc.add( new StringField( IndexingTerms.STATUS_FIELD, libraryStatus, Field.Store.YES ) );
        }
        if (lockedByUser != null) {
            indexDoc.add( new StringField( IndexingTerms.LOCKED_BY_USER_FIELD, lockedByUser, Field.Store.YES ) );
        }
        if (extendsEntityKey != null) {
            indexDoc.add( new StringField( IndexingTerms.EXTENDS_ENTITY_FIELD, extendsEntityKey, Field.Store.YES ) );
        }
        if (facetOwnerContent != null) {
            indexDoc.add( new StoredField( IndexingTerms.CONTENT_DATA_FIELD, facetOwnerContent ) );
        }
        for (String facetOwnerKey : facetOwnerKeys) {
            indexDoc.add( new StringField( IndexingTerms.FACET_OWNER_FIELD,
                IndexingUtils.getSearchableIdentityKey( facetOwnerKey ), Field.Store.YES ) );
        }
        for (String key : referenceIdentityKeys) {
            indexDoc.add( new StringField( IndexingTerms.REFERENCE_IDENTITY_FIELD, key, Field.Store.YES ) );
        }
        for (String key : referencedEntityKeys) {
            indexDoc.add( new StringField( IndexingTerms.REFERENCED_ENTITY_FIELD, key, Field.Store.NO ) );
        }

        // Combine the keywords from the facet owner and all of the contextual facets
        Set<String> freeTextKeywords = new HashSet<>();
        StringBuilder keywordsContent = new StringBuilder();

        for (Document facetDoc : contextualFacets) {
            addKeywords( facetDoc, freeTextKeywords );
        }
        addKeywords( facetOwner, freeTextKeywords );

        for (String keyword : freeTextKeywords) {
            keywordsContent.append( keyword ).append( " " );
        }
        indexDoc.add( new TextField( IndexingTerms.KEYWORDS_FIELD, keywordsContent.toString(), Field.Store.NO ) );

        // Add contextual facet binary content to the search index document
        for (Document facetDoc : contextualFacets) {
            BytesRef facetContent = facetDoc.getBinaryValue( IndexingTerms.CONTENT_DATA_FIELD );

            if (facetContent != null) {
                indexDoc.add(
                    new StoredField( IndexingTerms.FACET_CONTENT_FIELD, new BytesRef( facetContent.utf8ToString() ) ) );
            }
        }

        return indexDoc;
    }

    /**
     * Appends all free-text search keywords from the given document into the set provided.
     * 
     * @param doc the search index document from which to extract the keywords
     * @param keywords the collection of keywords being constructed
     */
    private void addKeywords(Document doc, Set<String> keywords) {
        String keywordsStr = doc.get( IndexingTerms.KEYWORDS_FIELD );

        if (keywordsStr != null) {
            for (String keyword : keywordsStr.split( "\\s+" )) {
                keywords.add( keyword );
            }
        }
    }

    /**
     * Returns an <code>IndexBuilder</code> that will store all validation findings as documents in the search index.
     * 
     * @return IndexBuilder&lt;ValidationFinding&gt;
     */
    public IndexBuilder<ValidationFinding> getIndexBuilder() {
        return new IndexBuilder<ValidationFinding>() {
            @Override
            public void performIndexingAction() {
                setCreateIndex( true );
                super.performIndexingAction();
            }

            protected void createIndex() {
                postProcessFacetOwners();
            }

            protected void deleteIndex() {
                // No action - deletion of validation findings is handled by the LibraryIndexBuilder
            }
        };
    }

}

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

package org.opentravel.repocommon.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.util.BytesRef;
import org.opentravel.repocommon.index.builder.IndexContentHelper;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Search result object that encapsulates all relevant information about an OTM entity.
 */
public class EntitySearchResult extends SearchResult<NamedEntity> {

    private static Logger log = LogManager.getLogger( EntitySearchResult.class );

    private String itemLocalName;
    private TLLibraryStatus status;
    private String lockedByUser;
    private String owningLibraryId;
    private String extendsEntityId;
    private Set<String> referenceIdentityIds = new HashSet<>();

    /**
     * Constructor that initializes the search result contents from the given <code>Document</code>.
     * 
     * @param doc the index document from which to initialize the library information
     * @param searchService the indexing search service that created this search result
     */
    public EntitySearchResult(Document doc, FreeTextSearchService searchService) {
        super( doc, searchService );
        String statusStr = doc.get( IndexingTerms.STATUS_FIELD );

        this.itemLocalName = doc.get( IndexingTerms.ENTITY_LOCAL_NAME_FIELD );
        this.lockedByUser = doc.get( IndexingTerms.LOCKED_BY_USER_FIELD );
        this.owningLibraryId = doc.get( IndexingTerms.OWNING_LIBRARY_FIELD );
        this.extendsEntityId = doc.get( IndexingTerms.EXTENDS_ENTITY_FIELD );
        this.referenceIdentityIds.addAll( Arrays.asList( doc.getValues( IndexingTerms.REFERENCE_IDENTITY_FIELD ) ) );

        if (statusStr != null) {
            try {
                this.status = TLLibraryStatus.valueOf( statusStr );

            } catch (IllegalArgumentException e) {
                // Ignore error - return a null status
            }
        }

        if (doc.getBinaryValue( IndexingTerms.CONTENT_DATA_FIELD ) != null) {
            initializeItemContent( doc );
        }
    }

    /**
     * @see org.opentravel.repocommon.index.SearchResult#initializeItemContent(org.apache.lucene.document.Document)
     */
    @Override
    protected void initializeItemContent(Document itemDoc) {
        try {
            BytesRef binaryContent =
                (itemDoc == null) ? null : itemDoc.getBinaryValue( IndexingTerms.CONTENT_DATA_FIELD );
            String content = (binaryContent == null) ? null : binaryContent.utf8ToString();

            if (content != null) {
                NamedEntity entity = IndexContentHelper.unmarshallEntity( content );

                if ((entity instanceof TLBusinessObject) || (entity instanceof TLChoiceObject)
                    || (entity instanceof TLContextualFacet)) {
                    initializeContextualFacets( (TLFacetOwner) entity, itemDoc );
                }
                setItemContent( entity );
            }

        } catch (RepositoryException e) {
            log.error( "Error initializing entity content.", e );
        }
    }

    /**
     * Unmarshals the contextual facet entities from the given search index document and re-assembles them as children
     * of the given facet owner.
     * 
     * @param facetOwner the facet owner for which to resolve contextual facets
     * @param itemDoc the search index document of the facet owner
     */
    private void initializeContextualFacets(TLFacetOwner facetOwner, Document itemDoc) {
        BytesRef[] facetContents = itemDoc.getBinaryValues( IndexingTerms.FACET_CONTENT_FIELD );

        // Start by unmarshalling each of the contextual facets
        for (BytesRef binaryContent : facetContents) {
            try {
                TLContextualFacet facet =
                    (TLContextualFacet) IndexContentHelper.unmarshallEntity( binaryContent.utf8ToString() );

                addContextualFacet( facet, facetOwner );

            } catch (RepositoryException e) {
                log.warn( "Error unmarshalling contextual facet content.", e );
            }
        }
    }

    /**
     * Adds the given contextual facet to the facet owner based on the owner and/or facet type.
     * 
     * @param facet the contextual facet to add
     * @param owner the owner of the contextual facet
     */
    private void addContextualFacet(TLContextualFacet facet, TLFacetOwner owner) {
        if (owner instanceof TLBusinessObject) {
            TLFacetType facetType = facet.getFacetType();

            if (facetType == TLFacetType.CUSTOM) {
                ((TLBusinessObject) owner).addCustomFacet( facet );

            } else if (facetType == TLFacetType.QUERY) {
                ((TLBusinessObject) owner).addQueryFacet( facet );

            } else if (facetType == TLFacetType.UPDATE) {
                ((TLBusinessObject) owner).addUpdateFacet( facet );
            }
        } else if (owner instanceof TLChoiceObject) {
            ((TLChoiceObject) owner).addChoiceFacet( facet );

        } else if (owner instanceof TLContextualFacet) {
            ((TLContextualFacet) owner).addChildFacet( facet );

        } else {
            throw new IllegalArgumentException( "Illegal contextual facet owner type: " + owner.getClass().getName() );
        }
    }

    /**
     * Returns the local name of the OTM item.
     *
     * @return String
     */
    public String getItemLocalName() {
        return itemLocalName;
    }

    /**
     * Returns the current status of the entity's owning library.
     *
     * @return TLLibraryStatus
     */
    public TLLibraryStatus getStatus() {
        return status;
    }

    /**
     * Returns the ID of the user who currently owns the lock on the entity's owning library (null if not locked).
     *
     * @return String
     */
    public String getLockedByUser() {
        return lockedByUser;
    }

    /**
     * Returns the search index ID of the library that owns this entity.
     *
     * @return String
     */
    public String getOwningLibraryId() {
        return owningLibraryId;
    }

    /**
     * Returns the search index ID of the entity that is extended by this one.
     *
     * @return String
     */
    public String getExtendsEntityId() {
        return extendsEntityId;
    }

    /**
     * Returns the list of search index ID's by which this entity can be referenced by other OTM model entities.
     *
     * @return Set&lt;String&gt;
     */
    public Set<String> getReferenceIdentityIds() {
        return referenceIdentityIds;
    }

}

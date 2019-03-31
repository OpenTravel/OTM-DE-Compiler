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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.util.BytesRef;
import org.opentravel.schemacompiler.index.builder.IndexContentHelper;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Search result object that encapsulates all relevant information about an OTM library.
 */
public class LibrarySearchResult extends SearchResult<TLLibrary> {

    private static Log log = LogFactory.getLog( LibrarySearchResult.class );

    private RepositoryItem repositoryItem;
    private TLLibraryStatus status;
    private String lockedByUser;
    private Map<String,String> prefixMappings = new HashMap<>();
    private List<String> referencedLibraryIds = new ArrayList<>();

    /**
     * Constructor that initializes the search result contents from the given <code>Document</code>.
     * 
     * @param doc the index document from which to initialize the library information
     * @param owningRepository the repository instance that owns the item represented by this search result
     * @param searchService the indexing search service that created this search result
     */
    public LibrarySearchResult(Document doc, Repository owningRepository, FreeTextSearchService searchService) {
        super( doc, searchService );
        RepositoryItemImpl item = new RepositoryItemImpl();
        String statusStr = doc.get( IndexingTerms.STATUS_FIELD );

        this.lockedByUser = doc.get( IndexingTerms.LOCKED_BY_USER_FIELD );
        this.referencedLibraryIds.addAll( Arrays.asList( doc.getValues( IndexingTerms.REFERENCED_LIBRARY_FIELD ) ) );

        if (statusStr != null) {
            try {
                this.status = TLLibraryStatus.valueOf( statusStr );

            } catch (IllegalArgumentException e) {
                // Ignore error - return a null status
            }
        }

        item.setRepository( owningRepository );
        item.setBaseNamespace( doc.get( IndexingTerms.BASE_NAMESPACE_FIELD ) );
        item.setNamespace( doc.get( IndexingTerms.ENTITY_NAMESPACE_FIELD ) );
        item.setLibraryName( doc.get( IndexingTerms.ENTITY_NAME_FIELD ) );
        item.setFilename( doc.get( IndexingTerms.FILENAME_FIELD ) );
        item.setVersion( doc.get( IndexingTerms.VERSION_FIELD ) );
        item.setVersionScheme( doc.get( IndexingTerms.VERSION_SCHEME_FIELD ) );
        item.setLockedByUser( doc.get( IndexingTerms.LOCKED_BY_USER_FIELD ) );
        item.setStatus( this.status );
        item.setState(
            (this.lockedByUser == null) ? RepositoryItemState.MANAGED_UNLOCKED : RepositoryItemState.MANAGED_LOCKED );
        this.repositoryItem = item;

        for (String prefixMapping : doc.getValues( IndexingTerms.PREFIX_MAPPING_FIELD )) {
            String[] mappingParts = prefixMapping.split( "\\~" );

            if (mappingParts.length == 2) {
                prefixMappings.put( mappingParts[0], mappingParts[1] );
            }
        }

        if (doc.getBinaryValue( IndexingTerms.CONTENT_DATA_FIELD ) != null) {
            initializeItemContent( doc );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.index.SearchResult#initializeItemContent(org.apache.lucene.document.Document)
     */
    @Override
    protected void initializeItemContent(Document itemDoc) {
        try {
            BytesRef binaryContent =
                (itemDoc == null) ? null : itemDoc.getBinaryValue( IndexingTerms.CONTENT_DATA_FIELD );
            String content = (binaryContent == null) ? null : binaryContent.utf8ToString();

            if (content != null) {
                setItemContent( IndexContentHelper.unmarshallLibrary( content ) );
            }

        } catch (RepositoryException e) {
            log.error( "Error initializing library content.", e );
        }
    }

    /**
     * Returns the reference identity ID for the entity referenced with the given type-reference string.
     * 
     * @param typeReference type-reference string in the format "entityName" or "prefix:entityName"
     * @return String
     */
    public String getReferenceIdentity(String typeReference) {
        String searchIndexId = null;

        if ((typeReference != null) && (typeReference.length() > 0)) {
            String prefix = null;
            String localName = null;
            String namespace = null;
            String[] refParts = typeReference.split( "\\:" );

            if (refParts.length == 1) {
                localName = refParts[0];

            } else if (refParts.length == 2) {
                prefix = refParts[0];
                localName = refParts[1];
            }
            namespace = (prefix == null) ? getItemNamespace() : prefixMappings.get( prefix );

            if ((namespace != null) && (localName != null) && (localName.length() > 0)) {
                searchIndexId = namespace + ":" + localName;
            }
        }
        return searchIndexId;
    }

    /**
     * Returns the repository item for the library.
     *
     * @return RepositoryItem
     */
    public RepositoryItem getRepositoryItem() {
        return repositoryItem;
    }

    /**
     * Returns the current status of the library.
     *
     * @return TLLibraryStatus
     */
    public TLLibraryStatus getStatus() {
        return status;
    }

    /**
     * Returns the ID of the user who currently owns the lock on the library (null if not locked).
     *
     * @return String
     */
    public String getLockedByUser() {
        return lockedByUser;
    }

    /**
     * Returns the search index ID's of all libraries that are referenced by this one.
     *
     * @return List&lt;String&gt;
     */
    public List<String> getReferencedLibraryIds() {
        return referencedLibraryIds;
    }

    /**
     * Returns the namespace URI that is mapped to the specified prefix in this library or null if the prefix is not
     * mapped to a namespace.
     * 
     * @param prefix the prefix for which to return a namespace URI
     * @return String
     */
    public String getNamespace(String prefix) {
        return prefixMappings.get( prefix );
    }

}

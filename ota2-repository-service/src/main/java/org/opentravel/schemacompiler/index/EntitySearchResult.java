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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.util.BytesRef;
import org.opentravel.schemacompiler.index.builder.IndexContentHelper;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryException;

/**
 * Search result object that encapsulates all relevant information about an OTM
 * entity.
 */
public class EntitySearchResult extends SearchResult<NamedEntity> {
	
    private static Log log = LogFactory.getLog( EntitySearchResult.class );

    private TLLibraryStatus status;
	private String lockedByUser;
	private String owningLibraryId;
	private String extendsEntityId;
	private Set<String> referenceIdentityIds = new HashSet<>();
	
    /**
	 * Constructor that initializes the search result contents from the given
	 * <code>Document</code>.
	 * 
	 * @param doc  the index document from which to initialize the library information
	 * @param searchService  the indexing search service that created this search result
	 */
	public EntitySearchResult(Document doc, FreeTextSearchService searchService) {
		super( doc, searchService );
		String statusStr = doc.get( STATUS_FIELD );
		
		this.lockedByUser = doc.get( LOCKED_BY_USER_FIELD );
		this.owningLibraryId = doc.get( OWNING_LIBRARY_FIELD );
		this.extendsEntityId = doc.get( EXTENDS_ENTITY_FIELD );
		this.referenceIdentityIds.addAll( Arrays.asList( doc.getValues( REFERENCE_IDENTITY_FIELD ) ) );
		
		if (statusStr != null) {
			try {
				this.status = TLLibraryStatus.valueOf( statusStr );
				
			} catch (IllegalArgumentException e) {
				// Ignore error - return a null status
			}
		}
		
		try {
			BytesRef xmlContent = doc.getBinaryValue( CONTENT_DATA_FIELD );
			
			if (xmlContent != null) {
				setItemContent( IndexContentHelper.unmarshallEntity( xmlContent.utf8ToString() ) );
			}
			
		} catch (RepositoryException e) {
			log.error("Error initializing entity content.", e);
		}
	}
	
	/**
	 * @see org.opentravel.schemacompiler.index.SearchResult#initializeItemContent()
	 */
	@Override
	protected void initializeItemContent() {
		try {
			setItemContent( getSearchService().getEntityContent( getSearchIndexId() ) );
			
		} catch (RepositoryException e) {
			log.error("Error initializing entity content.", e);
		}
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
	 * Returns the ID of the user who currently owns the lock on the entity's owning
	 * library (null if not locked).
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
	 * Returns the list of search index ID's by which this entity can be referenced
	 * by other OTM model entities.
	 *
	 * @return Set<String>
	 */
	public Set<String> getReferenceIdentityIds() {
		return referenceIdentityIds;
	}

}

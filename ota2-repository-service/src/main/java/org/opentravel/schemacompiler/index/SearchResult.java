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

/**
 * Abstract base class for all search results returned by the <code>IndexingSearchService</code>.
 */
public abstract class SearchResult<T> implements IndexingTerms {
	
    private static Log log = LogFactory.getLog( SearchResult.class );

    private IndexingSearchService searchService;
    private String searchIndexId;
    private Class<?> entityType;
    private String itemNamespace;
    private String itemName;
    private T itemContent;
    private boolean contentInitialized = false;
    
    /**
	 * Constructor that initializes the search result contents from the given
	 * <code>Document</code>.
	 * 
	 * @param doc  the index document from which to initialize the library information
	 * @param searchService  the indexing search service that created this search result
	 */
	public SearchResult(Document doc, IndexingSearchService searchService) {
		String entityTypeStr = doc.get( ENTITY_TYPE_FIELD );
		
		this.searchIndexId = doc.get( IDENTITY_FIELD );
		this.itemNamespace = doc.get( ENTITY_NAMESPACE_FIELD );
		this.itemName = doc.get( ENTITY_NAME_FIELD );
		this.searchService = searchService;
		
		try {
			this.entityType = Class.forName( entityTypeStr );
			
		} catch (ClassNotFoundException e) {
			log.error("No OTM type found for entity type: " + entityTypeStr);
		}
	}
	
	/**
	 * Returns the indexing search service that created this search result.
	 *
	 * @return IndexingSearchService
	 */
	public IndexingSearchService getSearchService() {
		return searchService;
	}

	/**
	 * Returns the search index ID of the OTM item.
	 *
	 * @return String
	 */
	public String getSearchIndexId() {
		return searchIndexId;
	}

	/**
	 * Returns the type of the OTM object contained within this search result.
	 *
	 * @return Class<?>
	 */
	public Class<?> getEntityType() {
		return entityType;
	}

	/**
	 * Returns the namespace URI to which the OTM item is assigned.
	 *
	 * @return String
	 */
	public String getItemNamespace() {
		return itemNamespace;
	}

	/**
	 * Returns the name of the OTM item.
	 *
	 * @return String
	 */
	public String getItemName() {
		return itemName;
	}

	/**
	 * Returns the structured content of the OTM item.  If the content has not yet been
	 * initialized, the <code>initializeItemContent()</code> method will be called to
	 * load it dynamically.
	 *
	 * @return T
	 */
	public synchronized T getItemContent() {
		if (!contentInitialized) {
			initializeItemContent();
		}
		return itemContent;
	}
	
	/**
	 * Assigns the structured content of the OTM item.  Calling this method has the side-
	 * effect of marking the item content as initialized.
	 *
	 * @param itemContent  the field value to assign
	 */
	synchronized void setItemContent(T itemContent) {
		this.itemContent = itemContent;
		this.contentInitialized = true;
	}
	
	/**
	 * Called to dynamically initialize the item content from persistent storage.
	 */
	protected abstract void initializeItemContent();
	
}

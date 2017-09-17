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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.util.BytesRef;
import org.opentravel.ns.ota2.release_v01_00.ReleaseStatus;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.repository.Release;
import org.opentravel.schemacompiler.repository.impl.ReleaseFileUtils;

/**
 * Search result object that encapsulates all relevant information about an OTM
 * release.
 */
public class ReleaseSearchResult extends SearchResult<Release> {
	
    private static Log log = LogFactory.getLog( ReleaseSearchResult.class );
    
    private String releaseName;
    private String namespace;
    private String baseNamespace;
    private String filename;
    private String version;
	private ReleaseStatus status;
	private List<String> referencedLibraryIds = new ArrayList<>();
	
	/**
	 * Constructor that initializes the search result contents from the given
	 * <code>Document</code>.
	 * 
	 * @param doc  the index document from which to initialize the library information
	 * @param searchService  the indexing search service that created this search result
	 */
	public ReleaseSearchResult(Document doc, FreeTextSearchService searchService) {
		super( doc, searchService );
		String statusStr = doc.get( STATUS_FIELD );
		
		if (statusStr != null) {
			try {
				this.status = ReleaseStatus.valueOf( statusStr );
				
			} catch (IllegalArgumentException e) {
				// Ignore error - return a null status
			}
		}
		
		this.releaseName = doc.get( ENTITY_NAME_FIELD );
		this.namespace = doc.get( ENTITY_NAMESPACE_FIELD );
		this.baseNamespace = doc.get( BASE_NAMESPACE_FIELD );
		this.filename = doc.get( FILENAME_FIELD );
		this.version = doc.get( VERSION_FIELD );
		this.referencedLibraryIds.addAll( Arrays.asList( doc.getValues( REFERENCED_LIBRARY_FIELD ) ) );
		
		if (doc.getBinaryValue( CONTENT_DATA_FIELD ) != null) {
			initializeItemContent( doc );
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.index.SearchResult#initializeItemContent(org.apache.lucene.document.Document)
	 */
	@Override
	protected void initializeItemContent(Document itemDoc) {
		try {
			BytesRef binaryContent = (itemDoc == null) ? null : itemDoc.getBinaryValue( CONTENT_DATA_FIELD );
			String content = (binaryContent == null) ? null : binaryContent.utf8ToString();
			
			if (content != null) {
				setItemContent( new ReleaseFileUtils( getSearchService().getRepositoryManager() )
						.loadReleaseContent( content ) );
			}
			
		} catch (LibraryLoaderException e) {
			log.error("Error initializing library content.", e);
		}
	}

	/**
	 * Returns the name of the OTM release.
	 *
	 * @return String
	 */
	public String getReleaseName() {
		return releaseName;
	}

	/**
	 * Returns the namespace of the OTM release.
	 *
	 * @return String
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Returns the base namespace of the OTM release.
	 *
	 * @return String
	 */
	public String getBaseNamespace() {
		return baseNamespace;
	}

	/**
	 * Returns the filename of the OTM release.
	 *
	 * @return String
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Returns the version of the OTM release.
	 *
	 * @return String
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Returns the status of the OTM release.
	 *
	 * @return ReleaseStatus
	 */
	public ReleaseStatus getStatus() {
		return status;
	}

	/**
	 * Returns the search index ID's of all libraries that are referenced by this release.
	 *
	 * @return List<String>
	 */
	public List<String> getReferencedLibraryIds() {
		return referencedLibraryIds;
	}
	
}

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
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFinding;

/**
 * Provides information about an error or warning that was identified for a library or
 * entity in the OTM repository.
 */
public class ValidationResult extends SearchResult<ValidationFinding> {
	
    private static Log log = LogFactory.getLog( ValidationResult.class );
    
	private String libraryIndexId;
	private String entityIndexId;
	private String findingSource;
	private FindingType findingType;
	private String findingMessage;
	
	/**
	 * Constructor for a validation finding that specifies the library and entity to which
	 * the finding applies.
	 * 
	 * @param findingType  the type of validation finding
	 * @param message  the plain-text message for the validation finding
	 * @param libraryIndexId  the search index ID of the library to which this finding applies
	 * @param entityIndexId  the search index ID of the entity to which this finding applies (may be null)
	 */
	public ValidationResult(Document doc, FreeTextSearchService searchService) {
		super( doc, searchService );
		String findingTypeStr = doc.get( FINDING_TYPE_FIELD );
		
		this.libraryIndexId = doc.get( TARGET_LIBRARY_FIELD );
		this.entityIndexId = doc.get( TARGET_ENTITY_FIELD );
		this.findingSource = doc.get( FINDING_SOURCE_FIELD );
		this.findingMessage = doc.get( FINDING_MESSAGE_FIELD );
		
		try {
			this.findingType = FindingType.valueOf( findingTypeStr );
			
		} catch (IllegalArgumentException e) {
			log.warn("Invalid finding type detected (assuming ERROR): " + findingTypeStr );
			this.findingType = FindingType.ERROR;
		}
		setItemContent( new ValidationFinding( null, findingType, findingMessage, null ) );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.index.SearchResult#initializeItemContent(org.apache.lucene.document.Document)
	 */
	@Override
	protected void initializeItemContent(Document itemDoc) {
		// No action required
	}

	/**
	 * Returns the search index ID of the library to which this finding applies.
	 *
	 * @return String
	 */
	public String getLibraryIndexId() {
		return libraryIndexId;
	}

	/**
	 * Returns the search index ID of the entity to which this finding applies (may
	 * be null).
	 *
	 * @return String
	 */
	public String getEntityIndexId() {
		return entityIndexId;
	}

	/**
	 * Returns the source object identity of the validation finding.
	 *
	 * @return String
	 */
	public String getFindingSource() {
		return findingSource;
	}

	/**
	 * Returns the severity of the validation finding.
	 *
	 * @return FindingType
	 */
	public FindingType getFindingType() {
		return findingType;
	}
	
	/**
	 * Returns the validation message for the finding.
	 *
	 * @return String
	 */
	public String getFindingMessage() {
		return findingMessage;
	}
	
}

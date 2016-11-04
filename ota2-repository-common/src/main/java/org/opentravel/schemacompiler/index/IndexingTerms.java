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

/**
 * Interface that defines constants for all indexing fields and terms used by the
 * free-text search service.
 */
public interface IndexingTerms {
	
	// Common fields used for all indexed entities
	
	public static final String IDENTITY_FIELD           = "identity";
	public static final String ENTITY_TYPE_FIELD        = "entityType";
	public static final String ENTITY_NAME_FIELD        = "entityName";
	public static final String ENTITY_NAMESPACE_FIELD   = "entityNamespace";
	public static final String ENTITY_DESCRIPTION_FIELD = "entityDescription";
	public static final String VERSION_FIELD            = "version";
	public static final String STATUS_FIELD             = "status";
	public static final String LOCKED_BY_USER_FIELD     = "lockedBy";
	public static final String KEYWORDS_FIELD           = "keywords";
	public static final String CONTENT_DATA_FIELD       = "contentData";
	public static final String SEARCH_INDEX_FIELD       = "searchIndexInd";
    
	// Common fields used to exclude library/entity versions from search results
	
	public static final String LATEST_VERSION_FIELD                 = "latestVersion";
	public static final String LATEST_VERSION_AT_UNDER_REVIEW_FIELD = "latestVersionAtUnderReview";
	public static final String LATEST_VERSION_AT_FINAL_FIELD        = "latestVersionAtFinal";
	public static final String LATEST_VERSION_AT_OBSOLETE_FIELD     = "latestVersionAtObsolete";
	
	// Fields used exclusively by OTM libraries and/or repository items
	
	public static final String BASE_NAMESPACE_FIELD     = "baseNamespace";
	public static final String FILENAME_FIELD           = "filename";
	public static final String VERSION_SCHEME_FIELD     = "versionScheme";
	public static final String REFERENCED_LIBRARY_FIELD = "referencedLibrary";
	public static final String PREFIX_MAPPING_FIELD     = "prefixMapping";
	
	// Fields used exclusively by OTM entities
	
	public static final String OWNING_LIBRARY_FIELD     = "owningLibrary";
	public static final String REFERENCE_IDENTITY_FIELD = "referenceIdentity";
	public static final String REFERENCED_ENTITY_FIELD  = "referencedEntity";
	public static final String EXTENDS_ENTITY_FIELD     = "extendsEntity";
	public static final String FACET_OWNER_FIELD        = "facetOwner";
	public static final String FACET_CONTENT_FIELD      = "facetContent";
	
	// Fields used exclusively by validation findings
	
	public static final String TARGET_LIBRARY_FIELD  = "targetLibrary";
	public static final String TARGET_ENTITY_FIELD   = "targetEntity";
	public static final String FINDING_SOURCE_FIELD  = "findingSource";
	public static final String FINDING_TYPE_FIELD    = "findingType";
	public static final String FINDING_MESSAGE_FIELD = "findingMessage";
	
}

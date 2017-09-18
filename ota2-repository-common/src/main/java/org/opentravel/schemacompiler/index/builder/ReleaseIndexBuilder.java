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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;
import org.opentravel.schemacompiler.index.IndexingUtils;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Release;
import org.opentravel.schemacompiler.repository.ReleaseMember;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.ReleaseFileUtils;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Index builder used to construct search index documents for managed OTM releases.
 */
public class ReleaseIndexBuilder extends IndexBuilder<RepositoryItem> {
	
    private static Log log = LogFactory.getLog( ReleaseIndexBuilder.class );
    
	/**
	 * @see org.opentravel.schemacompiler.index.builder.IndexBuilder#createIndex()
	 */
	@Override
	protected void createIndex() {
		RepositoryItem sourceObject = getSourceObject();
		try {
			RepositoryManager repositoryManager = getRepositoryManager();
			ReleaseFileUtils fileUtils = new ReleaseFileUtils( repositoryManager );
			URL releaseUrl = repositoryManager.getContentLocation( sourceObject );
			File releaseFile = URLUtils.toFile( releaseUrl );
			Release release = fileUtils.loadReleaseFile( releaseFile, new ValidationFindings() );
			String releaseContent = fileUtils.marshalReleaseContent( release );
			boolean latestVersion = isLatestVersion( sourceObject );
			
			// Add keywords for free-text search
			addFreeTextKeywords( release.getName() );
			addFreeTextKeywords( release.getDescription() );
			
			// Create the list of search ID's for referenced libraries
			List<String> referencedLibraryIds = new ArrayList<>();
			
			for (ReleaseMember member : release.getAllMembers()) {
				referencedLibraryIds.add( IndexingUtils.getIdentityKey( member.getRepositoryItem() ) );
			}
			
			// Build the index document
			String identityKey = IndexingUtils.getIdentityKey( sourceObject );
			Document indexDoc = new Document();
			
			indexDoc.add( new StringField( IDENTITY_FIELD, identityKey, Field.Store.YES ) );
			indexDoc.add( new StringField( SEARCH_INDEX_FIELD, Boolean.TRUE + "", Field.Store.NO ) );
			indexDoc.add( new StringField( ENTITY_TYPE_FIELD, Release.class.getName(), Field.Store.YES ) );
			indexDoc.add( new StringField( ENTITY_NAME_FIELD, release.getName(), Field.Store.YES ) );
			indexDoc.add( new StringField( ENTITY_NAMESPACE_FIELD, release.getNamespace(), Field.Store.YES ) );
			indexDoc.add( new StringField( BASE_NAMESPACE_FIELD, release.getBaseNamespace(), Field.Store.YES ) );
			indexDoc.add( new StringField( FILENAME_FIELD, sourceObject.getFilename(), Field.Store.YES ) );
			indexDoc.add( new StringField( VERSION_FIELD, release.getVersion(), Field.Store.YES ) );
			indexDoc.add( new StringField( VERSION_SCHEME_FIELD, sourceObject.getVersionScheme(), Field.Store.YES ) );
			indexDoc.add( new StringField( LATEST_VERSION_FIELD, latestVersion + "", Field.Store.NO ) );
			indexDoc.add( new StringField( LATEST_VERSION_AT_UNDER_REVIEW_FIELD, latestVersion + "", Field.Store.NO ) );
			indexDoc.add( new StringField( LATEST_VERSION_AT_FINAL_FIELD, latestVersion + "", Field.Store.NO ) );
			indexDoc.add( new StringField( LATEST_VERSION_AT_OBSOLETE_FIELD, Boolean.FALSE + "", Field.Store.NO ) );
			indexDoc.add( new TextField( KEYWORDS_FIELD, getFreeTextSearchContent(), Field.Store.NO ) );
			
			if (release.getDescription() != null) {
				indexDoc.add( new StringField( ENTITY_DESCRIPTION_FIELD, release.getDescription(), Field.Store.YES ) );
			}
			if (sourceObject.getStatus() != null) {
				indexDoc.add( new StringField( STATUS_FIELD, sourceObject.getStatus().toString(), Field.Store.YES ) );
			}
			if (release.getStatus() != null) {
				indexDoc.add( new StringField( RELEASE_STATUS_FIELD, release.getStatus().toString(), Field.Store.YES ) );
			}
			if (releaseContent != null) {
				indexDoc.add( new StoredField( CONTENT_DATA_FIELD, new BytesRef( releaseContent ) ) );
			}
			for (String libraryId : referencedLibraryIds) {
				indexDoc.add( new StringField( REFERENCED_LIBRARY_FIELD, libraryId, Field.Store.YES ) );
			}
			getIndexWriter().updateDocument( new Term( IDENTITY_FIELD, identityKey ), indexDoc );
			
		} catch (RepositoryException | LibraryLoaderException | LibrarySaveException | IOException e) {
			log.error("Error creating index for OTM release: " + sourceObject.getFilename(), e);
		}
	}
	
	/**
	 * @see org.opentravel.schemacompiler.index.builder.IndexBuilder#deleteIndex()
	 */
	@Override
	protected void deleteIndex() {
		try {
			RepositoryItem sourceObject = getSourceObject();
			String sourceObjectIdentity = IndexingUtils.getIdentityKey( sourceObject );
			
			getIndexWriter().deleteDocuments( new Term( IDENTITY_FIELD, sourceObjectIdentity ) );
			
		} catch (IOException e) {
			log.error("Error deleting search index for OTM release.", e);
        }
	}
	
	/**
	 * Returns true if the given repository item is the latest version of its chain.
	 * 
	 * @param item  the repository item to analyze
	 * @return boolean
	 * @throws RepositoryException  thrown if an error occurs while accessing the repository
	 */
	private boolean isLatestVersion(RepositoryItem item) throws RepositoryException {
		String releaseName = item.getLibraryName();
		String baseNS = item.getBaseNamespace();
		boolean result = true;
		
		for (RepositoryItem itemVersion : getRepositoryManager().listItems( baseNS, TLLibraryStatus.DRAFT, false )) {
			if (!releaseName.equals( itemVersion.getLibraryName() )) {
				continue;
				
			} else {
				// Listed items are sorted in descending version order.  If the first item we encounter
				// is not the one we are indexing, then the source item is not the latest version.
				result = item.getVersion().equals( itemVersion.getVersion() );
				break;
			}
		}
		return result;
	}
	
}

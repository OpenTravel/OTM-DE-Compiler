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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;
import org.opentravel.ns.ota2.release_v01_00.ReleaseIdentityType;
import org.opentravel.ns.ota2.release_v01_00.ReleaseMemberType;
import org.opentravel.ns.ota2.release_v01_00.ReleaseType;
import org.opentravel.schemacompiler.index.IndexingUtils;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Release;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.ReleaseFileUtils;
import org.opentravel.schemacompiler.util.URLUtils;

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
			ReleaseType release = loadRelease( releaseFile, fileUtils );
			ReleaseIdentityType releaseId = release.getReleaseIdentity();
			String releaseContent = fileUtils.marshalReleaseContent( release );
			boolean latestVersion = isLatestVersion( sourceObject );
			
			// Add keywords for free-text search
			addFreeTextKeywords( releaseId.getName() );
			addFreeTextKeywords( release.getDescription() );
			
			// Create the list of search ID's for referenced libraries
			List<String> referencedLibraryIds = new ArrayList<>();
			List<String> externalPrincipals = new ArrayList<>();
			List<String> externalReferences = new ArrayList<>();
			
			for (ReleaseMemberType member : release.getPrincipalMembers().getReleaseMember()) {
				addMember( member, referencedLibraryIds, externalPrincipals,
						repositoryManager.getId(), fileUtils );
			}
			for (ReleaseMemberType member : release.getReferencedMembers().getReleaseMember()) {
				addMember( member, referencedLibraryIds, externalReferences,
						repositoryManager.getId(), fileUtils );
			}
			
			// Build the index document
			String identityKey = IndexingUtils.getIdentityKey( sourceObject );
			Document indexDoc = new Document();
			
			indexDoc.add( new StringField( IDENTITY_FIELD, identityKey, Field.Store.YES ) );
			indexDoc.add( new StringField( SEARCH_INDEX_FIELD, Boolean.TRUE + "", Field.Store.NO ) );
			indexDoc.add( new StringField( ENTITY_TYPE_FIELD, Release.class.getName(), Field.Store.YES ) );
			indexDoc.add( new StringField( ENTITY_NAME_FIELD, releaseId.getName(), Field.Store.YES ) );
			indexDoc.add( new StringField( ENTITY_NAMESPACE_FIELD, sourceObject.getNamespace(), Field.Store.YES ) );
			indexDoc.add( new StringField( BASE_NAMESPACE_FIELD, releaseId.getBaseNamespace(), Field.Store.YES ) );
			indexDoc.add( new StringField( FILENAME_FIELD, sourceObject.getFilename(), Field.Store.YES ) );
			indexDoc.add( new StringField( VERSION_FIELD, releaseId.getVersion(), Field.Store.YES ) );
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
			for (String externalRef : externalPrincipals) {
				indexDoc.add( new StringField( EXTERNAL_PRINCIPAL_FIELD, externalRef, Field.Store.YES ) );
			}
			for (String externalRef : externalReferences) {
				indexDoc.add( new StringField( EXTERNAL_REFERENCE_FIELD, externalRef, Field.Store.YES ) );
			}
			getIndexWriter().updateDocument( new Term( IDENTITY_FIELD, identityKey ), indexDoc );
			
		} catch (RepositoryException | IOException e) {
			log.error("Error creating index for OTM release: " + sourceObject.getFilename(), e);
		}
	}
	
	/**
	 * Loads the release content from the given file.
	 * 
	 * @param releaseFile  the release file to load
	 * @param fileUtils  the release file utilities instance to use for the load
	 * @return ReleaseType
	 * @throws IOException  thrown if the file cannot be loaded
	 */
	private ReleaseType loadRelease(File releaseFile, ReleaseFileUtils fileUtils)
			throws IOException {
		try (Reader reader = new FileReader( releaseFile )) {
			return fileUtils.loadRawRelease( reader );
			
		} catch (JAXBException e) {
			throw new IOException("Error parsing file content for OTM release", e);
		}
	}
	
	/**
	 * Encodes the given release member and adds it to one of the lists provided, depending
	 * upon whether the member is internal (belongs to this repository) or external (owned by
	 * another repository).
	 * 
	 * @param member  the release member to encode and add
	 * @param internalReferences  the list of internal repository references
	 * @param externalReferences  the list of external repository references
	 * @param localRepositoryId  the ID of the local repository (used to determine internal/external)
	 * @param fileUtils  the release file utils to use when encoding external members
	 */
	private void addMember(ReleaseMemberType member, List<String> internalReferences,
			List<String> externalReferences, String localRepositoryId,
			ReleaseFileUtils fileUtils) throws IOException {
		
		if (localRepositoryId.equals( member.getRepositoryID() )) {
			internalReferences.add( IndexingUtils.getIdentityKey( member ) );
			
		} else { // external reference
			externalReferences.add( fileUtils.marshalReleaseMemberContent( member ) );
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
			if (releaseName.equals( itemVersion.getLibraryName() )) {
				// Listed items are sorted in descending version order.  If the first item we encounter
				// is not the one we are indexing, then the source item is not the latest version.
				result = item.getVersion().equals( itemVersion.getVersion() );
				break;
			}
		}
		return result;
	}
	
}

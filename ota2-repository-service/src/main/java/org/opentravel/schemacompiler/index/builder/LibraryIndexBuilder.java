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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;
import org.opentravel.ns.ota2.librarymodel_v01_05.Library;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;

/**
 * Index builder used to construct search index documents for managed repository items.
 */
public class LibraryIndexBuilder extends IndexBuilder<RepositoryItem> {
	
	private static Map<TLLibraryStatus,List<TLLibraryStatus>> inclusiveStatuses;
    private static Log log = LogFactory.getLog( LibraryIndexBuilder.class );
    
	/**
	 * @see org.opentravel.schemacompiler.index.builder.IndexBuilder#createIndex()
	 */
	@Override
	public void createIndex() {
		RepositoryItem sourceObject = getSourceObject();
		try {
			String libraryName = sourceObject.getLibraryName();
			String baseNS = sourceObject.getBaseNamespace();
			Set<TLLibraryStatus> laterVersionStatuses = new HashSet<>();
			boolean latestVersion = true;
			boolean latestVersionAtUnderReview = true;
			boolean latestVersionAtFinal = true;
			boolean latestVersionAtObsolete = true;
			
			// Start by searching the other sibling versions of this library to calculate
			// the 'latestVersion' values for the index.
			for (RepositoryItem itemVersion : getRepositoryManager().listItems( baseNS, false, true )) {
				if (!libraryName.equals( itemVersion.getLibraryName() )) {
					continue;
				}
				if (sourceObject.getVersion().equals( itemVersion.getVersion() )) {
					latestVersion = isLatestVersionAtStatus(
							sourceObject.getStatus(), TLLibraryStatus.DRAFT, laterVersionStatuses );
					latestVersionAtUnderReview = isLatestVersionAtStatus(
							sourceObject.getStatus(), TLLibraryStatus.UNDER_REVIEW, laterVersionStatuses );
					latestVersionAtFinal = isLatestVersionAtStatus(
							sourceObject.getStatus(), TLLibraryStatus.FINAL, laterVersionStatuses );
					latestVersionAtObsolete = isLatestVersionAtStatus(
							sourceObject.getStatus(), TLLibraryStatus.OBSOLETE, laterVersionStatuses );
					break;
					
				} else {
					laterVersionStatuses.add( itemVersion.getStatus() );
				}
			}
			
			// Now we can begin creating the index...
			Set<String> keywords = getFreeTextKeywords();
			LibraryInfoType libraryMetadata = loadLibraryMetadata( sourceObject );
			Library jaxbLibrary = loadLibrary( sourceObject );
			TLLibrary library = IndexContentHelper.transformLibrary( jaxbLibrary );
			List<NamedEntity> entityList = new ArrayList<>();
			
			log.info("Indexing Library: " + sourceObject.getFilename());
			
			// Assemble a list of all entities in the library
			for (NamedEntity entity : library.getNamedMembers()) {
				if (entity instanceof TLService) {
					for (TLOperation op : ((TLService) entity).getOperations()) {
						entityList.add( op );
					}
				} else {
					entityList.add( entity );
				}
			}
			if (library.getService() != null) {
				for (NamedEntity op : library.getService().getOperations()) {
					entityList.add( op );
				}
			}
			
			// Create an index for each entity; keywords for this library include the keywords
			// for each child entity.
			for (NamedEntity entity : entityList) {
				EntityIndexBuilder<NamedEntity> builder = (EntityIndexBuilder<NamedEntity>)
						getFactory().newCreateIndexBuilder( entity );
				
				if (builder != null) {
					builder.setLatestVersion( latestVersion );
					builder.setLatestVersionAtUnderReview( latestVersionAtUnderReview );
					builder.setLatestVersionAtFinal( latestVersionAtFinal );
					builder.setLatestVersionAtObsolete( latestVersionAtObsolete );
					builder.setLockedByUser( libraryMetadata.getLockedBy() );
					builder.performIndexingAction();
					keywords.addAll( builder.getFreeTextKeywords() );
				}
			}
			
			// Add keywords from this library
			addFreeTextKeywords( library.getName() );
			addFreeTextKeywords( library.getPrefix() );
			addFreeTextKeywords( library.getComments() );
			
			// Finish up by creating an index document for the library itself
			String libraryContent = IndexContentHelper.marshallLibrary( jaxbLibrary );
			String identityKey = IndexContentHelper.getIdentityKey( sourceObject );
			Document indexDoc = new Document();
			
			indexDoc.add( new StringField( IDENTITY_FIELD, identityKey, Field.Store.YES ) );
			indexDoc.add( new StringField( ENTITY_TYPE_FIELD, TLLibrary.class.getName(), Field.Store.YES ) );
			indexDoc.add( new StringField( ENTITY_NAME_FIELD, library.getName(), Field.Store.YES ) );
			indexDoc.add( new StringField( ENTITY_NAMESPACE_FIELD, library.getNamespace(), Field.Store.YES ) );
			indexDoc.add( new StringField( BASE_NAMESPACE_FIELD, baseNS, Field.Store.YES ) );
			indexDoc.add( new StringField( FILENAME_FIELD, sourceObject.getFilename(), Field.Store.YES ) );
			indexDoc.add( new StringField( VERSION_FIELD, sourceObject.getVersion(), Field.Store.YES ) );
			indexDoc.add( new StringField( VERSION_SCHEME_FIELD, sourceObject.getVersionScheme(), Field.Store.YES ) );
			indexDoc.add( new StringField( LATEST_VERSION_FIELD, latestVersion + "", Field.Store.NO ) );
			indexDoc.add( new StringField( LATEST_VERSION_AT_UNDER_REVIEW_FIELD, latestVersionAtUnderReview + "", Field.Store.NO ) );
			indexDoc.add( new StringField( LATEST_VERSION_AT_FINAL_FIELD, latestVersionAtFinal + "", Field.Store.NO ) );
			indexDoc.add( new StringField( LATEST_VERSION_AT_OBSOLETE_FIELD, latestVersionAtObsolete + "", Field.Store.NO ) );
			indexDoc.add( new TextField( KEYWORDS_FIELD, getFreeTextSearchContent(), Field.Store.NO ) );
			
			if (library.getStatus() != null) {
				indexDoc.add( new StringField( STATUS_FIELD, library.getStatus().toString(), Field.Store.YES ) );
			}
			if (libraryMetadata.getLockedBy() != null) {
				indexDoc.add( new StringField( LOCKED_BY_USER_FIELD, libraryMetadata.getLockedBy(), Field.Store.YES ) );
			}
			if (libraryContent != null) {
				indexDoc.add( new StoredField( CONTENT_DATA_FIELD, new BytesRef( libraryContent ) ) );
			}
			
			for (TLInclude nsInclude : library.getIncludes()) {
				String includeKey = getIdentityKey( nsInclude, library.getNamespace() );
				
				if (includeKey != null) {
					indexDoc.add( new StringField( REFERENCED_LIBRARY_FIELD, includeKey, Field.Store.YES ) );
				}
			}
			for (TLNamespaceImport nsImport : library.getNamespaceImports()) {
				List<String> importKeys = getIdentityKeys( nsImport );
				String prefix = nsImport.getPrefix();
				String ns = nsImport.getNamespace();
				
				for (String importKey : importKeys) {
					indexDoc.add( new StringField( REFERENCED_LIBRARY_FIELD, importKey, Field.Store.YES ) );
				}
				if ((prefix != null) && (ns != null)) {
					indexDoc.add( new StringField( PREFIX_MAPPING_FIELD, prefix + "~" + ns, Field.Store.YES ) );
				}
			}
			getIndexWriter().updateDocument( new Term( IDENTITY_FIELD, identityKey ), indexDoc );
			getFactory().getValidationService().validateLibrary( sourceObject );
			
		} catch (RepositoryException | IOException e) {
			log.error("Error creating index for repository item: " + sourceObject.getFilename(), e);
		}
	}
	
	/**
	 * Based on the statuses of later library versions, determines whether the given item's status is the
	 * latest at its version level relative to the check version.
	 * 
	 * @param itemStatus  the status of the library item to be analyzed
	 * @param checkStatus  the status level against which the item status is being checked
	 * @param laterVerionStatuses  the set of statuses identified for later versions of the library
	 * @return boolean
	 */
	private boolean isLatestVersionAtStatus(TLLibraryStatus itemStatus, TLLibraryStatus checkStatus, Set<TLLibraryStatus> laterVersionStatuses) {
		Set<TLLibraryStatus> statusesToConsider = new HashSet<>( inclusiveStatuses.get( checkStatus ) );
		boolean result = false;
		
		for (TLLibraryStatus status : laterVersionStatuses) {
			if (statusesToConsider.contains( status )) {
				statusesToConsider.removeAll( inclusiveStatuses.get( status ) );
			}
		}
		result = statusesToConsider.contains( itemStatus );
		return result;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.index.builder.IndexBuilder#deleteIndex()
	 */
	@Override
	public void deleteIndex() {
		RepositoryItem sourceObject = getSourceObject();
		String sourceObjectIdentity = IndexContentHelper.getIdentityKey( sourceObject );
		SearcherManager searchManager = null;
        IndexSearcher searcher = null;
        
		try {
			QueryParser parser = new QueryParser( OWNING_LIBRARY_FIELD, new StandardAnalyzer() );
			Query entityQuery = parser.parse( "\"" + IndexContentHelper.getIdentityKey( sourceObject ) + "\"" );
			IndexWriter indexWriter = getIndexWriter();
			
			searchManager = new SearcherManager( indexWriter, true, new SearcherFactory() );
			searcher = searchManager.acquire();
			
			// Search for the entity documents that are owned by the library whose index is being deleted
            TopDocs searchResults = searcher.search( entityQuery, Integer.MAX_VALUE );
            List<String> documentKeys = new ArrayList<>();
            
            for (ScoreDoc scoreDoc : searchResults.scoreDocs) {
            	Document entityDoc = searcher.doc( scoreDoc.doc );
            	IndexableField entityId = entityDoc.getField( IDENTITY_FIELD );
            	
            	if (entityId != null) {
                	documentKeys.add( entityId.stringValue() );
            	}
            }
            documentKeys.add( sourceObjectIdentity );
            
            // Delete all of the documents from the search index
            for (String documentId : documentKeys) {
        		log.info("Deleting index: " + documentId);
    			indexWriter.deleteDocuments( new Term( documentId ) );
            }
            
            // Delete any associated validation findings from the search index
            getFactory().getValidationService().deleteValidationResults( sourceObjectIdentity );
            
		} catch (IOException | ParseException e) {
			log.error("Error deleting search index for repository item.", e);
			
        } finally {
			try {
				if (searcher != null) searchManager.release( searcher );
			} catch (Throwable t) {}
			try {
				if (searchManager != null) searchManager.close();
			} catch (Throwable t) {}
		}
	}
	
	/**
	 * Returns a qualified identity key for the given library include, or null if the
	 * include does not resolve to a library within the local repository.
	 * 
	 * @param nsInclude  the library include for which to return an identity key
	 * @param libraryNamespace  the namespace of the library that declared the include
	 * @return String
	 */
	protected String getIdentityKey(TLInclude nsInclude, String libraryNamespace) {
		String fileHint = nsInclude.getPath();
		String identityKey = null;
		
		if ((libraryNamespace != null) && (fileHint != null)) {
			if (fileHint.startsWith("otm://")) {
				try {
					RepositoryItem importItem =
							getRepositoryManager().getRepositoryItem( fileHint, libraryNamespace );
					
					if (importItem != null) {
						identityKey = IndexContentHelper.getIdentityKey( importItem );
					}
					
				} catch (RepositoryException | URISyntaxException e) {
					// No error - return a null identity key value
				}
			}
		}
		return identityKey;
	}
	
	/**
	 * Returns the qualified identity keys for the given library import.
	 * 
	 * @param nsImport  the library import for which to return identity keys
	 * @return List<String>
	 */
	protected List<String> getIdentityKeys(TLNamespaceImport nsImport) {
		List<String> fileHints = nsImport.getFileHints();
		String namespace = nsImport.getNamespace();
		List<String> identityKeys = new ArrayList<>();
		
		if ((namespace != null) && (fileHints != null)) {
			for (String fileHint : fileHints) {
				if (fileHint.startsWith("otm://")) {
					try {
						RepositoryItem importItem =
								getRepositoryManager().getRepositoryItem( fileHint, namespace );
						
						if (importItem != null) {
							identityKeys.add( IndexContentHelper.getIdentityKey( importItem ) );
						}
						
					} catch (RepositoryException | URISyntaxException e) {
						// No error - skip this file hint
					}
				}
			}
		}
		return identityKeys;
	}
	
	/**
	 * Loads the contents of the given repository item as a JAXB object.
	 * 
	 * @param item  the repository item to load
	 * @return Library
	 * @throws RepositoryException
	 */
	protected Library loadLibrary(RepositoryItem item) throws RepositoryException {
		File contentFile = getRepositoryManager().getFileManager().getLibraryContentLocation(
				item.getBaseNamespace(), item.getFilename(), item.getVersion() );
		Library library = null;
		
		if ((contentFile != null) && contentFile.exists()) {
			library = IndexContentHelper.unmarshallLibrary( contentFile );
		}
		return library;
	}
	
	/**
	 * Returns the meta-data record for the given repository item.
	 * 
	 * @param item  the repository item for which to load meta-data
	 * @return LibraryInfoType
	 * @throws RepositoryException  thrown if the meta-data cannot be retrieved for any reason
	 */
	protected LibraryInfoType loadLibraryMetadata(RepositoryItem item) throws RepositoryException {
		return getRepositoryManager().getFileManager().loadLibraryMetadata(
				item.getBaseNamespace(), item.getFilename(), item.getVersion() );
	}
	
	/**
	 * Initializes the map of inclusive statuses used to calculate the 'lastVersionAt...' values.
	 */
	static {
		try {
			Map<TLLibraryStatus,List<TLLibraryStatus>> statusMap = new HashMap<>();
			
			statusMap.put( TLLibraryStatus.DRAFT,        Arrays.asList( TLLibraryStatus.DRAFT, TLLibraryStatus.UNDER_REVIEW, TLLibraryStatus.FINAL, TLLibraryStatus.OBSOLETE ) );
			statusMap.put( TLLibraryStatus.UNDER_REVIEW, Arrays.asList( TLLibraryStatus.UNDER_REVIEW, TLLibraryStatus.FINAL, TLLibraryStatus.OBSOLETE ) );
			statusMap.put( TLLibraryStatus.FINAL,        Arrays.asList( TLLibraryStatus.FINAL, TLLibraryStatus.OBSOLETE ) );
			statusMap.put( TLLibraryStatus.OBSOLETE,     Arrays.asList( TLLibraryStatus.OBSOLETE ) );
			inclusiveStatuses = Collections.unmodifiableMap( statusMap );
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
}

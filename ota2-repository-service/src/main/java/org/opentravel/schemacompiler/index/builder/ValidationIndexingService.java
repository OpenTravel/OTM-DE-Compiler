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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.opentravel.schemacompiler.index.IndexingTerms;
import org.opentravel.schemacompiler.index.IndexingUtils;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Service component that performs library validation during index creation processing.  The
 * service is optimized such that libraries can be skipped if their validations have already
 * been performed during previous validation passes (e.g. for common libraries).
 */
public class ValidationIndexingService implements IndexingTerms {
	
	private static Pattern findingSourcePattern = Pattern.compile( "[A-Za-z0-9_]*?\\.otm\\s+\\:\\s+(.*)" );
    private static Log log = LogFactory.getLog( ValidationIndexingService.class );
    
	private Set<String> indexedLibraryIds = new HashSet<>();
	private Set<String> processedLibraryIds = new HashSet<>();
	private Map<String,List<ValidationFinding>> findingsByLibrary = new HashMap<>();
	private Map<String,List<ValidationFinding>> findingsByEntity = new HashMap<>();
	private Map<String,String> librariesByEntity = new HashMap<>();
	private RepositoryManager repositoryManager;
	private IndexWriter indexWriter;
	
	/**
	 * Constructor that specifies the repository manager to use when loading and validating
	 * libraries.
	 * 
	 * @param repositoryManager  the repository manager to use when accessing library content
	 * @param indexWriter  the index writer to use for creating the search index document(s)
	 */
	public ValidationIndexingService(RepositoryManager repositoryManager, IndexWriter indexWriter) {
		this.repositoryManager = repositoryManager;
		this.indexWriter = indexWriter;
	}
	
	/**
	 * Validates the given library and saves the results for future storage in the search
	 * index.  If the given repository item has been processed during a previous validation
	 * pass (e.g. as a common/shared library) it will be skipped.
	 * 
	 * @param item  the repository item whose library is to be validated
	 */
	public void validateLibrary(RepositoryItem item) {
		File projectFile = null;
		try {
			String targetIndexId = IndexingUtils.getIdentityKey( item );
			
			if (!processedLibraryIds.contains( targetIndexId )) {
				ProjectManager manager = new ProjectManager( loadValidationModel(), false, repositoryManager );
				projectFile = File.createTempFile( "project", ".otp" );
				Project project = manager.newProject( projectFile,
						"http://www.OpenTravel.org/repository/index", "IndexValidation", null );
				ValidationFindings findings = new ValidationFindings();
				
				// Load the library; the validation findings will be populated as a side-effect of the load
				log.debug("Validating Library: " + item.getFilename());
				manager.addManagedProjectItems( Arrays.asList( item ), project, findings );
				
				// Save each of the findings that were discovered; cross-reference by library and entity
				for (ValidationFinding finding : findings.getAllFindingsAsList()) {
					TLModelElement targetEntity = getTargetEntity( finding.getSource() );
					NamedEntity entity = (targetEntity instanceof NamedEntity) ? (NamedEntity) targetEntity : null;
					TLLibrary library = (targetEntity instanceof TLLibrary) ? (TLLibrary) targetEntity : null;
					
					// Libraries will be saved as part of the validation finding if the target is an entity;
					// therefore, we only need to save library findings if they are directed to the library
					// itself adn not one of its member entities.
					if (targetEntity instanceof TLLibrary) {
						library = (TLLibrary) targetEntity;
					}
					
					// Ignore the entity if it does not come from a TLLibrary
					if ((entity != null) && !(entity.getOwningLibrary() instanceof TLLibrary)) {
						entity = null; 
					}
					
					if (library != null) {
						String libraryIndexId = IndexingUtils.getIdentityKey( library );
						List<ValidationFinding> libraryFindings = findingsByLibrary.get( libraryIndexId );
						
						if (libraryFindings == null) {
							libraryFindings = new ArrayList<>();
							findingsByLibrary.put( libraryIndexId, libraryFindings );
						}
						libraryFindings.add( finding );
					}
					if (entity != null) {
						String libraryIndexId = IndexingUtils.getIdentityKey( (TLLibrary) entity.getOwningLibrary() );
						String entityIndexId = IndexingUtils.getIdentityKey( entity );
						List<ValidationFinding> entityFindings = findingsByEntity.get( entityIndexId );
						
						if (entityFindings == null) {
							entityFindings = new ArrayList<>();
							findingsByEntity.put( entityIndexId, entityFindings );
						}
						entityFindings.add( finding );
						librariesByEntity.put( entityIndexId, libraryIndexId );
					}
				}
				
				// Save the ID's of all libraries that were directly or indirectly loaded so we
				// will not need to process them again later.
				for (ProjectItem pItem : project.getProjectItems()) {
					if (pItem.getState() == null) continue;
					
					switch (pItem.getState()) {
						case MANAGED_LOCKED:
						case MANAGED_UNLOCKED:
						case MANAGED_WIP:
							processedLibraryIds.add( IndexingUtils.getIdentityKey( pItem ) );
							break;
						default:
							break;
					}
				}
			}
			indexedLibraryIds.add( targetIndexId );
			
		} catch (LibrarySaveException | IOException | LibraryLoaderException | RepositoryException e) {
			log.error("Error creating validation index for library: " + item.getFilename(), e);
			
		} finally {
			if (projectFile != null) projectFile.delete();
		}
	}
	
	/**
	 * Returns an <code>IndexBuilder</code> that will store all validation findings
	 * as documents in the search index.
	 * 
	 * @return IndexBuilder<ValidationFinding>
	 */
	public IndexBuilder<ValidationFinding> getIndexBuilder() {
		return new IndexBuilder<ValidationFinding>() {
			public void performIndexingAction() {
				setCreateIndex( true );
				super.performIndexingAction();
			}
			protected void createIndex() {
				saveValidationResults();
			}
			protected void deleteIndex() {
				// No action - deletion of validation findings is handled by the LibraryIndexBuilder
			}
		};
	}
	
	/**
	 * Saves the current set of validation results.  Prior to creating the new validation finding
	 * documents in the search index, any existing documents are deleted.
	 */
	private void saveValidationResults() {
		try {
			log.info("Saving validation findings to search index...");
			
			for (String libraryIndexId : findingsByLibrary.keySet()) {
				List<ValidationFinding> findingList = findingsByLibrary.get( libraryIndexId );
				
				for (ValidationFinding finding : findingList) {
					saveValidationFinding( finding, libraryIndexId, null );
				}
			}
			
			for (String entityIndexId : findingsByEntity.keySet()) {
				List<ValidationFinding> findingList = findingsByEntity.get( entityIndexId );
				String libraryIndexId = librariesByEntity.get( entityIndexId );
				
				for (ValidationFinding finding : findingList) {
					saveValidationFinding( finding, libraryIndexId, entityIndexId );
				}
			}
			indexWriter.commit();
			
		} catch (IOException e) {
			log.error("Error indexing validation results.", e);
		}
	}
	
	/**
	 * Creates a search index document for the given validation finding that is associated
	 * with the given library and (optionally) entity search index ID's.
	 * 
	 * @param finding  the validation finding for which to create a search index document
	 * @param libraryIndexId  the search index ID of the library with which the finding record will be associated
	 * @param entityIndexId  the search index ID of the entity with which the finding record will be associated (may be null)
	 */
	private void saveValidationFinding(ValidationFinding finding, String libraryIndexId, String entityIndexId) {
		try {
			QName sourceObjectName = getSourceObjectQName( finding.getSource() );
			String findingSource = finding.getSource().getValidationIdentity();
			String identityKey = UUID.randomUUID().toString();
			Document indexDoc = new Document();
			Matcher m;
			
			if ((m = findingSourcePattern.matcher( findingSource )).matches()) {
				findingSource = m.group( 1 );
			}
			
			indexDoc.add( new StringField( IDENTITY_FIELD, identityKey, Field.Store.YES ) );
			indexDoc.add( new StringField( ENTITY_TYPE_FIELD, ValidationFinding.class.getName(), Field.Store.YES ) );
			indexDoc.add( new StringField( ENTITY_NAMESPACE_FIELD, sourceObjectName.getNamespaceURI(), Field.Store.YES ) );
			indexDoc.add( new StringField( ENTITY_NAME_FIELD, sourceObjectName.getLocalPart(), Field.Store.YES ) );
			indexDoc.add( new StringField( TARGET_LIBRARY_FIELD, libraryIndexId, Field.Store.YES ) );
			indexDoc.add( new StringField( FINDING_SOURCE_FIELD, findingSource, Field.Store.YES ) );
			indexDoc.add( new StringField( FINDING_TYPE_FIELD, finding.getType().toString(), Field.Store.YES ) );
			indexDoc.add( new StringField( FINDING_MESSAGE_FIELD, finding.getFormattedMessage(
					FindingMessageFormat.MESSAGE_ONLY_FORMAT ), Field.Store.YES ) );
			
			if (entityIndexId != null) {
				indexDoc.add( new StringField( TARGET_ENTITY_FIELD, entityIndexId, Field.Store.YES ) );
			}
			indexWriter.updateDocument( new Term( IDENTITY_FIELD, identityKey ), indexDoc );
			
		} catch (IOException e) {
            log.warn("Error indexing validation result for library: " + libraryIndexId, e);
		}
	}
	
	/**
	 * Returns the qualified name of the library or named entity that is the target of
	 * a validation finding.
	 * 
	 * @param sourceObject  the source object for which to return a qualified name
	 * @return QName
	 */
	private QName getSourceObjectQName(Object sourceObject) {
		TLModelElement targetObject = getTargetEntity( sourceObject );
		QName objName;
		
		if (targetObject instanceof TLLibrary) {
			TLLibrary library = (TLLibrary) targetObject;
			objName = new QName( library.getNamespace(), library.getName() );
			
		} else {
			NamedEntity entity = (NamedEntity) targetObject;
			objName = new QName( entity.getNamespace(), entity.getLocalName() );
		}
		return objName;
	}
	
	/**
	 * Deletes any existing validation result documents for the specified library from
	 * the search index.
	 * 
	 * @param libraryIndexId  the search index ID of the library for which to delete validation results
	 */
	public void deleteValidationResults(String libraryIndexId) {
		SearcherManager searchManager = null;
        IndexSearcher searcher = null;
		
		try {
			Query query = new TermQuery( new Term( TARGET_LIBRARY_FIELD, libraryIndexId ) );
			searchManager = new SearcherManager( indexWriter, true, new SearcherFactory() );
			searcher = searchManager.acquire();
            TopDocs searchResults = searcher.search( query, Integer.MAX_VALUE );
            List<String> documentKeys = new ArrayList<>();
            
            for (ScoreDoc scoreDoc : searchResults.scoreDocs) {
            	Document entityDoc = searcher.doc( scoreDoc.doc );
            	IndexableField entityId = entityDoc.getField( IDENTITY_FIELD );
            	
            	if (entityId != null) {
                	documentKeys.add( entityId.stringValue() );
            	}
            }
            
            // Delete all of the documents from the search index
            for (String documentId : documentKeys) {
        		log.info("Deleting index: " + documentId);
    			indexWriter.deleteDocuments( new Term( documentId ) );
            }
			
		} catch (IOException e) {
			log.error("Error configuring search manager for index.", e);
			
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
	 * Returns the entity or library that will be the indexing target for the source
	 * object of a validation finding.  For example, the owning entity of an attribute
	 * finding would be the core object that declared the attribute.
	 * 
	 * @param findingSource  the object that is the source of a validation finding
	 * @return TLModelElement
	 */
	private TLModelElement getTargetEntity(Object findingSource) {
		TLModelElement targetEntity = null;
		
		if (findingSource instanceof TLLibrary) {
        	targetEntity = (TLLibrary) findingSource;
			
        } else if (findingSource instanceof TLContext) {
        	targetEntity = ((TLContext) findingSource).getOwningLibrary();

        } else if (findingSource instanceof TLInclude) {
        	targetEntity = ((TLInclude) findingSource).getOwningLibrary();

        } else if (findingSource instanceof TLNamespaceImport) {
        	targetEntity = ((TLNamespaceImport) findingSource).getOwningLibrary();

        } else if (findingSource instanceof TLService) {
        	targetEntity = ((TLService) findingSource).getOwningLibrary();

		} else if (findingSource instanceof TLSimple) {
        	targetEntity = (TLSimple) findingSource;

        } else if (findingSource instanceof TLValueWithAttributes) {
        	targetEntity = (TLValueWithAttributes) findingSource;

        } else if (findingSource instanceof TLClosedEnumeration) {
        	targetEntity = (TLClosedEnumeration) findingSource;

        } else if (findingSource instanceof TLOpenEnumeration) {
        	targetEntity = (TLOpenEnumeration) findingSource;

        } else if (findingSource instanceof TLChoiceObject) {
        	targetEntity = (TLChoiceObject) findingSource;

        } else if (findingSource instanceof TLCoreObject) {
        	targetEntity = (TLCoreObject) findingSource;

        } else if (findingSource instanceof TLBusinessObject) {
        	targetEntity = (TLBusinessObject) findingSource;

        } else if (findingSource instanceof TLResource) {
        	targetEntity = (TLResource) findingSource;

        } else if (findingSource instanceof TLOperation) {
        	targetEntity = (TLOperation) findingSource;

        } else if (findingSource instanceof TLExtensionPointFacet) {
        	targetEntity = (TLExtensionPointFacet) findingSource;

        } else if (findingSource instanceof TLFacet) {
        	targetEntity = getTargetEntity( ((TLFacet) findingSource).getOwningEntity() );

        } else if (findingSource instanceof TLActionFacet) {
        	targetEntity = getTargetEntity( ((TLActionFacet) findingSource).getOwningResource() );

        } else if (findingSource instanceof TLSimpleFacet) {
        	targetEntity = getTargetEntity( ((TLSimpleFacet) findingSource).getOwningEntity() );

        } else if (findingSource instanceof TLListFacet) {
        	targetEntity = getTargetEntity( ((TLListFacet) findingSource).getOwningEntity() );

        } else if (findingSource instanceof TLAlias) {
        	targetEntity = getTargetEntity( ((TLAlias) findingSource).getOwningEntity() );

        } else if (findingSource instanceof TLParamGroup) {
        	targetEntity = getTargetEntity( ((TLParamGroup) findingSource).getOwner() );

        } else if (findingSource instanceof TLParameter) {
        	targetEntity = getTargetEntity( ((TLParameter) findingSource).getOwner() );

        } else if (findingSource instanceof TLResourceParentRef) {
        	targetEntity = getTargetEntity( ((TLResourceParentRef) findingSource).getOwner() );

        } else if (findingSource instanceof TLAction) {
        	targetEntity = getTargetEntity( ((TLAction) findingSource).getOwner() );

        } else if (findingSource instanceof TLActionRequest) {
        	targetEntity = getTargetEntity( ((TLActionRequest) findingSource).getOwner() );

        } else if (findingSource instanceof TLActionResponse) {
        	targetEntity = getTargetEntity( ((TLActionResponse) findingSource).getOwner() );

        } else if (findingSource instanceof TLExtension) {
        	targetEntity = getTargetEntity( ((TLExtension) findingSource).getOwner() );

        } else if (findingSource instanceof TLDocumentation) {
        	targetEntity = getTargetEntity( ((TLDocumentation) findingSource).getOwner() );

        } else if (findingSource instanceof TLEquivalent) {
        	targetEntity = getTargetEntity( ((TLEquivalent) findingSource).getOwningEntity() );

        } else if (findingSource instanceof TLExample) {
        	targetEntity = getTargetEntity( ((TLExample) findingSource).getOwningEntity() );

        } else if (findingSource instanceof TLAttribute) {
        	targetEntity = getTargetEntity( ((TLAttribute) findingSource).getOwner() );

        } else if (findingSource instanceof TLProperty) {
        	targetEntity = getTargetEntity( ((TLProperty) findingSource).getOwner() );

        } else if (findingSource instanceof TLIndicator) {
        	targetEntity = getTargetEntity( ((TLIndicator) findingSource).getOwner() );

        } else if (findingSource instanceof TLEnumValue) {
        	targetEntity = getTargetEntity( ((TLEnumValue) findingSource).getOwningEnum() );

        } else if (findingSource instanceof TLRoleEnumeration) {
        	targetEntity = getTargetEntity( ((TLRoleEnumeration) findingSource).getOwningEntity() );

        } else if (findingSource instanceof TLRole) {
        	targetEntity = getTargetEntity( ((TLRole) findingSource).getRoleEnumeration() );
        }
        return targetEntity;
	}
	
	/**
	 * Returns a <code>TLModel</code> instance that has the 'OTA_SimpleTypes' library pre-loaded.
	 */
	private TLModel loadValidationModel() {
		try {
			LibraryModelLoader<InputStream> loader = new LibraryModelLoader<>();
			URL ota2SimpleTypesUrl = LibraryIndexBuilder.class.getResource( "/otm-models/OTA_SimpleTypes_0_0_0.otm" );
			LibraryInputSource<InputStream> ota2SimpleTypesSource = new LibraryStreamInputSource( ota2SimpleTypesUrl );
			loader.loadLibraryModel( ota2SimpleTypesSource );
			TLModel model = loader.getLibraryModel();
			TLLibrary ota2Simples = (TLLibrary) model.getLibrary( "http://www.opentravel.org/OTM/Common/v0", "OTA_SimpleTypes" );
			ota2Simples.setLibraryUrl( URLUtils.toURL( new File( repositoryManager.getFileManager().getRepositoryLocation(),
					"/http/org/opentravel/www/otm/common/0.0.0/OTA_SimpleTypes_0_0_0.otm" ) ) );
			return model;
			
		} catch (LibraryLoaderException e) {
			log.error("Error loading static validation model.", e);
			return new TLModel();
		}
	}
	
}

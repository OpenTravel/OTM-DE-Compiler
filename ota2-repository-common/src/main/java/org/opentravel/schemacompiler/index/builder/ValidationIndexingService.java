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
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.transform.util.ModelReferenceResolver;
import org.opentravel.schemacompiler.util.FileUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

/**
 * Service component that performs library validation during index creation processing. The service is optimized such
 * that libraries can be skipped if their validations have already been performed during previous validation passes
 * (e.g. for common libraries).
 */
public class ValidationIndexingService {

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
     * Constructor that specifies the repository manager to use when loading and validating libraries.
     * 
     * @param repositoryManager the repository manager to use when accessing library content
     * @param indexWriter the index writer to use for creating the search index document(s)
     */
    public ValidationIndexingService(RepositoryManager repositoryManager, IndexWriter indexWriter) {
        ModelReferenceResolver.setObsoleteBuiltInVisitorType( ObsoleteBuiltInValidationVisitor.class );
        this.repositoryManager = repositoryManager;
        this.indexWriter = indexWriter;
    }

    /**
     * Validates the given library and saves the results for future storage in the search index. If the given repository
     * item has been processed during a previous validation pass (e.g. as a common/shared library) it will be skipped.
     * 
     * @param item the repository item whose library is to be validated
     */
    public void validateLibrary(RepositoryItem item) {
        File projectFile = null;
        try {
            String targetIndexId = IndexingUtils.getIdentityKey( item );

            if (!processedLibraryIds.contains( targetIndexId )) {
                ProjectManager manager = new ProjectManager( new TLModel(), false, repositoryManager );
                projectFile = File.createTempFile( "project", ".otp" );
                Project project = manager.newProject( projectFile, "http://www.OpenTravel.org/repository/index",
                    "IndexValidation", null );
                ValidationFindings findings = new ValidationFindings();

                // Load the library; the validation findings will be populated as a side-effect of the load
                log.debug( "Validating Library: " + item.getFilename() );
                manager.addManagedProjectItems( Arrays.asList( item ), project, findings );

                // Save each of the findings that was discovered; cross-reference by library and entity
                for (ValidationFinding finding : findings.getAllFindingsAsList()) {
                    saveAndCrossReferenceFinding( finding );
                }

                // Save the ID's of all libraries that were directly or indirectly loaded so we
                // will not need to process them again later.
                for (ProjectItem pItem : project.getProjectItems()) {
                    if (pItem.getState() == null) {
                        continue;
                    }

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
            log.error( "Error creating validation index for library: " + item.getFilename(), e );

        } finally {
            FileUtils.delete( projectFile );
        }
    }

    /**
     * Save the given validation finding, cross-referencing by library and entity.
     * 
     * @param finding the validation finding to save
     */
    private void saveAndCrossReferenceFinding(ValidationFinding finding) {
        TLModelElement targetEntity = IndexingUtils.getTargetEntity( finding.getSource() );
        NamedEntity entity = (targetEntity instanceof NamedEntity) ? (NamedEntity) targetEntity : null;
        TLLibrary library = (targetEntity instanceof TLLibrary) ? (TLLibrary) targetEntity : null;

        // Libraries will be saved as part of the validation finding if the target is an entity.
        // Therefore, we only need to save library findings if they are directed to the library
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

            findingsByLibrary.computeIfAbsent( libraryIndexId, id -> findingsByLibrary.put( id, new ArrayList<>() ) );
            findingsByLibrary.get( libraryIndexId ).add( finding );
        }
        if (entity != null) {
            String libraryIndexId = IndexingUtils.getIdentityKey( (TLLibrary) entity.getOwningLibrary() );
            String entityIndexId = IndexingUtils.getIdentityKey( entity );

            findingsByEntity.computeIfAbsent( entityIndexId, id -> findingsByEntity.put( id, new ArrayList<>() ) );
            findingsByEntity.get( entityIndexId ).add( finding );
            librariesByEntity.put( entityIndexId, libraryIndexId );
        }
    }

    /**
     * Returns an <code>IndexBuilder</code> that will store all validation findings as documents in the search index.
     * 
     * @return IndexBuilder&lt;ValidationFinding&gt;
     */
    public IndexBuilder<ValidationFinding> getIndexBuilder() {
        return new IndexBuilder<ValidationFinding>() {
            @Override
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
     * Saves the current set of validation results. Prior to creating the new validation finding documents in the search
     * index, any existing documents are deleted.
     */
    private void saveValidationResults() {
        try {
            log.info( "Saving validation findings to search index..." );

            for (Entry<String,List<ValidationFinding>> entry : findingsByLibrary.entrySet()) {
                String libraryIndexId = entry.getKey();
                List<ValidationFinding> findingList = entry.getValue();

                for (ValidationFinding finding : findingList) {
                    saveValidationFinding( finding, libraryIndexId, null );
                }
            }

            for (Entry<String,List<ValidationFinding>> entry : findingsByEntity.entrySet()) {
                String entityIndexId = entry.getKey();
                List<ValidationFinding> findingList = entry.getValue();
                String libraryIndexId = librariesByEntity.get( entityIndexId );

                for (ValidationFinding finding : findingList) {
                    saveValidationFinding( finding, libraryIndexId, entityIndexId );
                }
            }
            indexWriter.commit();

        } catch (IOException e) {
            log.error( "Error indexing validation results.", e );
        }
    }

    /**
     * Creates a search index document for the given validation finding that is associated with the given library and
     * (optionally) entity search index ID's.
     * 
     * @param finding the validation finding for which to create a search index document
     * @param libraryIndexId the search index ID of the library with which the finding record will be associated
     * @param entityIndexId the search index ID of the entity with which the finding record will be associated (may be
     *        null)
     */
    private void saveValidationFinding(ValidationFinding finding, String libraryIndexId, String entityIndexId) {
        try {
            QName sourceObjectName = IndexingUtils.getQualifiedName( finding.getSource() );
            String findingSource = finding.getSource().getValidationIdentity();
            String identityKey = IndexingUtils.getIdentityKey( finding );
            Document indexDoc = new Document();
            Matcher m = findingSourcePattern.matcher( findingSource );

            if (m.matches()) {
                findingSource = m.group( 1 );
            }

            indexDoc.add( new StringField( IndexingTerms.IDENTITY_FIELD, identityKey, Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.ENTITY_TYPE_FIELD, ValidationFinding.class.getName(),
                Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.ENTITY_NAMESPACE_FIELD, sourceObjectName.getNamespaceURI(),
                Field.Store.YES ) );
            indexDoc.add(
                new StringField( IndexingTerms.ENTITY_NAME_FIELD, sourceObjectName.getLocalPart(), Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.TARGET_LIBRARY_FIELD, libraryIndexId, Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.FINDING_SOURCE_FIELD, findingSource, Field.Store.YES ) );
            indexDoc.add(
                new StringField( IndexingTerms.FINDING_TYPE_FIELD, finding.getType().toString(), Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.FINDING_MESSAGE_FIELD,
                finding.getFormattedMessage( FindingMessageFormat.MESSAGE_ONLY_FORMAT ), Field.Store.YES ) );

            if (entityIndexId != null) {
                indexDoc.add( new StringField( IndexingTerms.TARGET_ENTITY_FIELD, entityIndexId, Field.Store.YES ) );
            }
            indexWriter.updateDocument( new Term( IndexingTerms.IDENTITY_FIELD, identityKey ), indexDoc );

        } catch (IOException e) {
            log.warn( "Error indexing validation result for library: " + libraryIndexId, e );
        }
    }

    /**
     * Deletes any existing validation result documents for the specified library from the search index.
     * 
     * @param libraryIndexId the search index ID of the library for which to delete validation results
     */
    public void deleteValidationResults(String libraryIndexId) {
        try (SearcherManager searchManager = new SearcherManager( indexWriter, true, new SearcherFactory() )) {
            Query query = new TermQuery( new Term( IndexingTerms.TARGET_LIBRARY_FIELD, libraryIndexId ) );
            IndexSearcher searcher = searchManager.acquire();
            TopDocs searchResults = searcher.search( query, Integer.MAX_VALUE );
            List<String> documentKeys = new ArrayList<>();

            for (ScoreDoc scoreDoc : searchResults.scoreDocs) {
                Document entityDoc = searcher.doc( scoreDoc.doc );
                IndexableField entityId = entityDoc.getField( IndexingTerms.IDENTITY_FIELD );

                if (entityId != null) {
                    documentKeys.add( entityId.stringValue() );
                }
            }

            // Delete all of the documents from the search index
            for (String documentId : documentKeys) {
                log.info( "Deleting index: " + documentId );
                indexWriter.deleteDocuments( new Term( IndexingTerms.IDENTITY_FIELD, documentId ) );
            }

        } catch (IOException e) {
            log.error( "Error configuring search manager for index.", e );
        }
    }

}

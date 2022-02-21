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

package org.opentravel.repocommon.index.builder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
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
import org.apache.lucene.util.BytesRef;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.repocommon.index.IndexingTerms;
import org.opentravel.repocommon.index.IndexingUtils;
import org.opentravel.schemacompiler.ic.ImportManagementIntegrityChecker;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.FileUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

/**
 * Index builder used to construct search index documents for managed repository items.
 */
public class LibraryIndexBuilder extends IndexBuilder<RepositoryItem> {

    private static final Pattern findingSourcePattern = Pattern.compile( "[A-Za-z0-9_]*?\\.otm\\s+\\:\\s+(.*)" );

    private static Map<TLLibraryStatus,List<TLLibraryStatus>> inclusiveStatuses;
    private static Logger log = LogManager.getLogger( LibraryIndexBuilder.class );

    /**
     * @see org.opentravel.repocommon.index.builder.IndexBuilder#createIndex()
     */
    @Override
    public void createIndex() {
        RepositoryItem sourceObject = getSourceObject();
        try {
            log.debug( "Indexing Library: " + sourceObject.getFilename() );
            deleteIndex(); // Start by deleting all index documents associated with this library

            // Now we can begin creating the index...
            Map<TLLibraryStatus,Boolean> latestVersionsByStatus = getLatestVersionsByStatus( sourceObject );
            Set<String> keywords = getFreeTextKeywords();
            LibraryInfoType libraryMetadata = loadLibraryMetadata( sourceObject );
            TLLibrary jaxbLibrary = loadJaxbLibrary( sourceObject );
            TLLibrary library = loadLibraryModel( sourceObject );

            // Add keywords from this library
            addFreeTextKeywords( library.getName() );
            addFreeTextKeywords( library.getPrefix() );
            addFreeTextKeywords( library.getComments() );

            // Index the library's named entities and collect additional search keywords
            indexLibraryEntities( library, keywords, latestVersionsByStatus, libraryMetadata );

            // Finish up by creating an index document for the library itself
            String libraryContent = IndexContentHelper.marshallLibrary( library );
            String identityKey = IndexingUtils.getIdentityKey( sourceObject );
            Document indexDoc = new Document();

            indexDoc.add( new StringField( IndexingTerms.IDENTITY_FIELD, identityKey, Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.SEARCH_INDEX_FIELD, Boolean.TRUE + "", Field.Store.NO ) );
            indexDoc
                .add( new StringField( IndexingTerms.ENTITY_TYPE_FIELD, TLLibrary.class.getName(), Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.ENTITY_NAME_FIELD, library.getName(), Field.Store.YES ) );
            indexDoc.add(
                new StringField( IndexingTerms.ENTITY_NAMESPACE_FIELD, library.getNamespace(), Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.BASE_NAMESPACE_FIELD, sourceObject.getBaseNamespace(),
                Field.Store.YES ) );
            indexDoc
                .add( new StringField( IndexingTerms.FILENAME_FIELD, sourceObject.getFilename(), Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.VERSION_FIELD, sourceObject.getVersion(), Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.VERSION_SCHEME_FIELD, sourceObject.getVersionScheme(),
                Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.LATEST_VERSION_FIELD,
                latestVersionsByStatus.get( TLLibraryStatus.DRAFT ) + "", Field.Store.NO ) );
            indexDoc.add( new StringField( IndexingTerms.LATEST_VERSION_AT_UNDER_REVIEW_FIELD,
                latestVersionsByStatus.get( TLLibraryStatus.UNDER_REVIEW ) + "", Field.Store.NO ) );
            indexDoc.add( new StringField( IndexingTerms.LATEST_VERSION_AT_FINAL_FIELD,
                latestVersionsByStatus.get( TLLibraryStatus.FINAL ) + "", Field.Store.NO ) );
            indexDoc.add( new StringField( IndexingTerms.LATEST_VERSION_AT_OBSOLETE_FIELD,
                latestVersionsByStatus.get( TLLibraryStatus.OBSOLETE ) + "", Field.Store.NO ) );
            indexDoc.add( new TextField( IndexingTerms.KEYWORDS_FIELD, getFreeTextSearchContent(), Field.Store.NO ) );

            if (library.getComments() != null) {
                indexDoc.add(
                    new StringField( IndexingTerms.ENTITY_DESCRIPTION_FIELD, library.getComments(), Field.Store.YES ) );
            }
            if (library.getStatus() != null) {
                indexDoc.add(
                    new StringField( IndexingTerms.STATUS_FIELD, library.getStatus().toString(), Field.Store.YES ) );
            }
            if (libraryMetadata.getLockedBy() != null) {
                indexDoc.add( new StringField( IndexingTerms.LOCKED_BY_USER_FIELD, libraryMetadata.getLockedBy(),
                    Field.Store.YES ) );
            }
            if (libraryContent != null) {
                indexDoc.add( new StoredField( IndexingTerms.CONTENT_DATA_FIELD, new BytesRef( libraryContent ) ) );
            }

            for (TLInclude nsInclude : jaxbLibrary.getIncludes()) {
                String includeKey = getIdentityKey( nsInclude, library.getNamespace() );

                if (includeKey != null) {
                    indexDoc
                        .add( new StringField( IndexingTerms.REFERENCED_LIBRARY_FIELD, includeKey, Field.Store.YES ) );
                }
            }
            for (TLNamespaceImport nsImport : jaxbLibrary.getNamespaceImports()) {
                List<String> importKeys = getIdentityKeys( nsImport );
                String prefix = nsImport.getPrefix();
                String ns = nsImport.getNamespace();

                for (String importKey : importKeys) {
                    indexDoc
                        .add( new StringField( IndexingTerms.REFERENCED_LIBRARY_FIELD, importKey, Field.Store.YES ) );
                }
                if ((prefix != null) && (ns != null)) {
                    indexDoc.add(
                        new StringField( IndexingTerms.PREFIX_MAPPING_FIELD, prefix + "~" + ns, Field.Store.YES ) );
                }
            }
            getIndexWriter().updateDocument( new Term( IndexingTerms.IDENTITY_FIELD, identityKey ), indexDoc );
            ProjectManager.clearInstanceMap();

        } catch (LibraryLoaderException | LibrarySaveException | RepositoryException | IOException e) {
            log.error( "Error creating index for repository item: " + sourceObject.getFilename(), e );
        }
    }

    /**
     * Search the other sibling versions of this repository item to calculate the 'latestVersion' values for the index.
     * 
     * @param sourceObject the repository item for which to determine the latest version indicators by status
     * @return Map&lt;TLLibraryStatus,Boolean&gt;
     * @throws RepositoryException thrown if the OTM repository cannot be accessed
     */
    private Map<TLLibraryStatus,Boolean> getLatestVersionsByStatus(RepositoryItem sourceObject)
        throws RepositoryException {
        String libraryName = sourceObject.getLibraryName();
        String baseNS = sourceObject.getBaseNamespace();
        Set<TLLibraryStatus> laterVersionStatuses = new HashSet<>();
        Map<TLLibraryStatus,Boolean> latestVersionsByStatus = new EnumMap<>( TLLibraryStatus.class );

        for (RepositoryItem itemVersion : getRepositoryManager().listItems( baseNS, false, true )) {
            if (libraryName.equals( itemVersion.getLibraryName() )) {
                if (sourceObject.getVersion().equals( itemVersion.getVersion() )) {
                    Arrays.asList( TLLibraryStatus.values() ).forEach( s -> latestVersionsByStatus.put( s,
                        isLatestVersionAtStatus( sourceObject.getStatus(), s, laterVersionStatuses ) ) );
                    break;

                } else {
                    laterVersionStatuses.add( itemVersion.getStatus() );
                }
            }
        }
        return latestVersionsByStatus;
    }

    /**
     * Indexes the named entities of the given libraries. Also, collects search keywords from the library's named
     * entities to the set provided.
     * 
     * @param library the library whose named entities are to be indexed
     * @param keywords the set of keywords being constructed
     * @param latestVersionsByStatus map that indicates the latest-version indicators by status
     * @param libraryMetadata meta-data from the library that identifies the locked-by user
     */
    private void indexLibraryEntities(TLLibrary library, Set<String> keywords,
        Map<TLLibraryStatus,Boolean> latestVersionsByStatus, LibraryInfoType libraryMetadata) {
        List<NamedEntity> entityList = new ArrayList<>();

        // Assemble a list of all entities in the library
        for (LibraryMember entity : library.getNamedMembers()) {
            if ((entity instanceof TLContextualFacet) && ((TLContextualFacet) entity).isLocalFacet()) {
                continue; // skip local contextual facets since they will be indexed under their owner
            }
            addLibraryEntity( entity, entityList );
        }

        // Create an index for each entity; keywords for this library include the keywords
        // for each child entity.
        for (NamedEntity entity : entityList) {
            EntityIndexBuilder<NamedEntity> builder =
                (EntityIndexBuilder<NamedEntity>) getFactory().newCreateIndexBuilder( entity );

            if (builder != null) {
                builder.setLatestVersion( latestVersionsByStatus.get( TLLibraryStatus.DRAFT ) );
                builder.setLatestVersionAtUnderReview( latestVersionsByStatus.get( TLLibraryStatus.UNDER_REVIEW ) );
                builder.setLatestVersionAtFinal( latestVersionsByStatus.get( TLLibraryStatus.FINAL ) );
                builder.setLatestVersionAtObsolete( latestVersionsByStatus.get( TLLibraryStatus.OBSOLETE ) );
                builder.setLockedByUser( libraryMetadata.getLockedBy() );
                builder.performIndexingAction();
                keywords.addAll( builder.getFreeTextKeywords() );
            }
        }
    }

    /**
     * Adds the given entity (and its parents or children, when necessary) to the list of entities to be indexed.
     * 
     * @param entity the library entity to add
     * @param entityList the list of entities to be indexed
     */
    private void addLibraryEntity(LibraryMember entity, List<NamedEntity> entityList) {
        if (entity instanceof TLService) {
            ((TLService) entity).getOperations().forEach( entityList::add );

        } else {
            // If this is a contextual facet, index all of the owners (even if they are not in this library) so we
            // can make sure they are available in the search index during the contextual facet resolution phase of
            // the indexing process.
            if (entity instanceof TLContextualFacet) {
                TLContextualFacet facet = (TLContextualFacet) entity;

                while (facet != null) {
                    TLFacetOwner owner = facet.getOwningEntity();

                    if (owner != null) {
                        entityList.add( owner );
                    }
                    facet = (owner instanceof TLContextualFacet) ? (TLContextualFacet) owner : null;
                }
            }
            entityList.add( entity );
        }
    }

    /**
     * Adds documents to the search index for each of the validation findings provided that correspond to the given
     * library or one of its entities.
     * 
     * @param library the library for which to index validation findings
     * @param findings the validation findings to index
     */
    private void indexValidationFindings(TLLibrary library, ValidationFindings findings) {
        for (ValidationFinding finding : findings.getAllFindingsAsList()) {
            try {
                TLModelElement findingTarget = IndexingUtils.getTargetEntity( finding.getSource() );
                NamedEntity targetEntity = null;
                TLLibrary targetLibrary = null;

                if (findingTarget instanceof TLLibrary) {
                    targetLibrary = (TLLibrary) findingTarget;

                } else if (findingTarget instanceof NamedEntity) {
                    targetEntity = (NamedEntity) findingTarget;
                    targetLibrary = (TLLibrary) targetEntity.getOwningLibrary();
                }

                // Skip findings that are not attributed to the library we are indexing
                if (targetLibrary != library) {
                    continue;
                }

                String libraryIndexId = IndexingUtils.getIdentityKey( targetLibrary );
                String entityIndexId = (targetEntity == null) ? null : IndexingUtils.getIdentityKey( targetEntity );
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
                indexDoc.add( new StringField( IndexingTerms.ENTITY_NAME_FIELD, sourceObjectName.getLocalPart(),
                    Field.Store.YES ) );
                indexDoc.add( new StringField( IndexingTerms.TARGET_LIBRARY_FIELD, libraryIndexId, Field.Store.YES ) );
                indexDoc.add( new StringField( IndexingTerms.FINDING_SOURCE_FIELD, findingSource, Field.Store.YES ) );
                indexDoc.add( new StringField( IndexingTerms.FINDING_TYPE_FIELD, finding.getType().toString(),
                    Field.Store.YES ) );
                indexDoc.add( new StringField( IndexingTerms.FINDING_MESSAGE_FIELD,
                    finding.getFormattedMessage( FindingMessageFormat.MESSAGE_ONLY_FORMAT ), Field.Store.YES ) );

                if (entityIndexId != null) {
                    indexDoc
                        .add( new StringField( IndexingTerms.TARGET_ENTITY_FIELD, entityIndexId, Field.Store.YES ) );
                }
                getIndexWriter().updateDocument( new Term( IndexingTerms.IDENTITY_FIELD, identityKey ), indexDoc );

            } catch (IOException e) {
                log.warn( "Error creating index document for validation finding.", e );
            }
        }

    }

    /**
     * Based on the statuses of later library versions, determines whether the given item's status is the latest at its
     * version level relative to the check version.
     * 
     * @param itemStatus the status of the library item to be analyzed
     * @param checkStatus the status level against which the item status is being checked
     * @param laterVerionStatuses the set of statuses identified for later versions of the library
     * @return boolean
     */
    private boolean isLatestVersionAtStatus(TLLibraryStatus itemStatus, TLLibraryStatus checkStatus,
        Set<TLLibraryStatus> laterVersionStatuses) {
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
     * @see org.opentravel.repocommon.index.builder.IndexBuilder#deleteIndex()
     */
    @Override
    public void deleteIndex() {
        RepositoryItem sourceObject = getSourceObject();
        String libraryIndexId = IndexingUtils.getIdentityKey( sourceObject );

        try (SearcherManager searchManager =
            new SearcherManager( getIndexWriter(), true, true, new SearcherFactory() )) {
            // QueryParser parser = new QueryParser( IndexingTerms.OWNING_LIBRARY_FIELD, new StandardAnalyzer() );
            // Query entityQuery = parser.parse( "\"" + IndexingUtils.getIdentityKey( sourceObject ) + "\"" );
            Query entityQuery = new TermQuery(
                new Term( IndexingTerms.OWNING_LIBRARY_FIELD, IndexingUtils.getIdentityKey( sourceObject ) ) );
            Query validationQuery = new TermQuery( new Term( IndexingTerms.TARGET_LIBRARY_FIELD, libraryIndexId ) );
            IndexSearcher searcher = searchManager.acquire();
            IndexWriter indexWriter = getIndexWriter();
            List<String> documentKeys = new ArrayList<>();

            documentKeys.addAll( findIndexIds( entityQuery, searcher ) );
            documentKeys.addAll( findIndexIds( validationQuery, searcher ) );
            documentKeys.add( libraryIndexId );
            searchManager.release( searcher );

            // Delete all of the documents from the search index
            for (String documentId : documentKeys) {
                log.debug( "Deleting index: " + documentId );
                indexWriter.deleteDocuments( new Term( IndexingTerms.IDENTITY_FIELD, documentId ) );
            }

        } catch (IOException e) {
            log.error( "Error deleting search index for repository item.", e );
        }
    }

    /**
     * Search for the entity or validation documents that are owned by the library whose index is being deleted.
     * 
     * @param query the search index query to use for locating document IDs
     * @param searcher the searcher that will perform the query
     * @return List&lt;String&gt;
     * @throws IOException thrown if an error occurs while running the search query
     */
    private List<String> findIndexIds(Query query, IndexSearcher searcher) throws IOException {
        TopDocs searchResults = searcher.search( query, Integer.MAX_VALUE );
        List<String> documentKeys = new ArrayList<>();

        for (ScoreDoc scoreDoc : searchResults.scoreDocs) {
            Document entityDoc = searcher.doc( scoreDoc.doc );
            IndexableField entityId = entityDoc.getField( IndexingTerms.IDENTITY_FIELD );

            if (entityId != null) {
                documentKeys.add( entityId.stringValue() );
            }
        }
        return documentKeys;
    }

    /**
     * Returns a qualified identity key for the given library include, or null if the include does not resolve to a
     * library within the local repository.
     * 
     * @param nsInclude the library include for which to return an identity key
     * @param libraryNamespace the namespace of the library that declared the include
     * @return String
     */
    protected String getIdentityKey(TLInclude nsInclude, String libraryNamespace) {
        String fileHint = nsInclude.getPath();
        String identityKey = null;

        if ((libraryNamespace != null) && (fileHint != null) && fileHint.startsWith( "otm://" )) {
            try {
                RepositoryItem importItem = getRepositoryManager().getRepositoryItem( fileHint, libraryNamespace );

                if (importItem != null) {
                    identityKey = IndexingUtils.getIdentityKey( importItem );
                }

            } catch (RepositoryException | URISyntaxException e) {
                // No error - return a null identity key value
            }
        }
        return identityKey;
    }

    /**
     * Returns the qualified identity keys for the given library import.
     * 
     * @param nsImport the library import for which to return identity keys
     * @return List&lt;String&gt;
     */
    protected List<String> getIdentityKeys(TLNamespaceImport nsImport) {
        List<String> fileHints = nsImport.getFileHints();
        String namespace = nsImport.getNamespace();
        List<String> identityKeys = new ArrayList<>();

        if ((namespace != null) && (fileHints != null)) {
            for (String fileHint : fileHints) {
                if (fileHint.startsWith( "otm://" )) {
                    try {
                        RepositoryItem importItem = getRepositoryManager().getRepositoryItem( fileHint, namespace );

                        if (importItem != null) {
                            identityKeys.add( IndexingUtils.getIdentityKey( importItem ) );
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
     * Loads the library model into memory. In addition to performing the load, this method also performs indexing on
     * the validation findings discovered during the load so that validation does not need to be re-run later.
     * 
     * @param item the repository item of the library to be loaded
     * @return TLLibrary
     * @throws LibraryLoaderException thrown if the library cannot be loaded
     * @throws LibrarySaveException thrown if an error occurs while creating the temporary project file
     * @throws RepositoryException thrown if an error occurs while retrieving the library's content from the repository
     * @throws IOException thrown if the temporary project file cannot be created on the local file system
     */
    private TLLibrary loadLibraryModel(RepositoryItem item)
        throws LibraryLoaderException, LibrarySaveException, RepositoryException, IOException {
        File projectFile = null;
        try {
            TLModel model = new TLModel();
            ProjectManager manager = new ProjectManager( model, false, getRepositoryManager() );
            projectFile = File.createTempFile( "project", ".otp" );
            Project project = manager.newProject( projectFile, "http://www.OpenTravel.org/repository/index",
                "IndexValidation", null );
            ValidationFindings findings = new ValidationFindings();
            TLLibrary library;

            // Load the library; the validation findings will be populated as a side-effect of the load
            manager.addManagedProjectItems( Arrays.asList( item ), project, findings );
            library = (TLLibrary) model.getLibrary( item.getNamespace(), item.getLibraryName() );

            if (library == null) {
                throw new LibraryLoaderException( "Unable to load library: " + item.getFilename() );
            }
            ImportManagementIntegrityChecker.verifyReferencedLibraries( library );
            indexValidationFindings( library, findings );
            return library;

        } finally {
            FileUtils.delete( projectFile );
        }
    }

    /**
     * Loads the contents of the given repository item as a JAXB object and transforms it to a <code>TLLibrary</code>
     * structure. The difference between this method is that the resulting library is not incorporated into a model and
     * none of its references have been resolved.
     * 
     * @param item the repository item to load
     * @return Library
     * @throws RepositoryException thrown if an error occurs while accessing the repository content
     */
    protected TLLibrary loadJaxbLibrary(RepositoryItem item) throws RepositoryException {
        File contentFile = getRepositoryManager().getFileManager().getLibraryContentLocation( item.getBaseNamespace(),
            item.getFilename(), item.getVersion() );
        TLLibrary library = null;

        if ((contentFile != null) && contentFile.exists()) {
            library = IndexContentHelper.unmarshallLibrary( contentFile );
        }
        return library;
    }

    /**
     * Returns the meta-data record for the given repository item.
     * 
     * @param item the repository item for which to load meta-data
     * @return LibraryInfoType
     * @throws RepositoryException thrown if the meta-data cannot be retrieved for any reason
     */
    protected LibraryInfoType loadLibraryMetadata(RepositoryItem item) throws RepositoryException {
        return getRepositoryManager().getFileManager().loadLibraryMetadata( item.getBaseNamespace(), item.getFilename(),
            item.getVersion() );
    }

    /**
     * Initializes the map of inclusive statuses used to calculate the 'lastVersionAt...' values.
     */
    static {
        try {
            Map<TLLibraryStatus,List<TLLibraryStatus>> statusMap = new EnumMap<>( TLLibraryStatus.class );

            statusMap.put( TLLibraryStatus.DRAFT, Arrays.asList( TLLibraryStatus.DRAFT, TLLibraryStatus.UNDER_REVIEW,
                TLLibraryStatus.FINAL, TLLibraryStatus.OBSOLETE ) );
            statusMap.put( TLLibraryStatus.UNDER_REVIEW,
                Arrays.asList( TLLibraryStatus.UNDER_REVIEW, TLLibraryStatus.FINAL, TLLibraryStatus.OBSOLETE ) );
            statusMap.put( TLLibraryStatus.FINAL, Arrays.asList( TLLibraryStatus.FINAL, TLLibraryStatus.OBSOLETE ) );
            statusMap.put( TLLibraryStatus.OBSOLETE, Arrays.asList( TLLibraryStatus.OBSOLETE ) );
            inclusiveStatuses = Collections.unmodifiableMap( statusMap );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}

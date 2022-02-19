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
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;
import org.opentravel.ns.ota2.assembly_v01_00.AssemblyItemType;
import org.opentravel.ns.ota2.assembly_v01_00.AssemblyType;
import org.opentravel.repocommon.index.IndexingTerms;
import org.opentravel.repocommon.index.IndexingUtils;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.ServiceAssembly;
import org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl;
import org.opentravel.schemacompiler.repository.impl.ServiceAssemblyFileUtils;
import org.opentravel.schemacompiler.util.URLUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

/**
 * Index builder used to construct search index documents for managed OTM service assemblies.
 */
public class AssemblyIndexBuilder extends IndexBuilder<RepositoryItem> {

    private static Logger log = LogManager.getLogger( AssemblyIndexBuilder.class );

    @Override
    protected void createIndex() {
        RepositoryItem sourceObject = getSourceObject();
        try {
            RepositoryManager repositoryManager = getRepositoryManager();
            ServiceAssemblyFileUtils fileUtils = new ServiceAssemblyFileUtils( repositoryManager );
            URL assemblyUrl = repositoryManager.getContentLocation( sourceObject );
            File assemblyFile = URLUtils.toFile( assemblyUrl );
            AssemblyType assembly = fileUtils.loadJaxbAssembly( new FileReader( assemblyFile ) );
            String assemblyContent = fileUtils.marshalAssemblyContent( assembly );
            List<AssemblyItemType> allAssemblyItems = new ArrayList<>();
            boolean latestVersion = isLatestVersion( sourceObject );

            addFreeTextKeywords( assembly.getAssemblyIdentity().getName() );
            addFreeTextKeywords( assembly.getDescription() );

            allAssemblyItems.addAll( assembly.getProvider() );
            allAssemblyItems.addAll( assembly.getConsumer() );

            allAssemblyItems.forEach( m -> {
                if (m.getResourceName() != null) {
                    addFreeTextKeywords( m.getResourceName().getLocalName() );
                }
                addFreeTextKeywords( m.getLibraryName() );
            } );

            // Create the list of search ID's for referenced libraries
            List<String> referencedProviderIds = new ArrayList<>();
            List<String> referencedConsumerIds = new ArrayList<>();
            List<String> externalProviders = new ArrayList<>();
            List<String> externalConsumers = new ArrayList<>();

            for (AssemblyItemType member : assembly.getProvider()) {
                addMember( member, true, referencedProviderIds, externalProviders, repositoryManager.getId(),
                    fileUtils );
            }
            for (AssemblyItemType member : assembly.getConsumer()) {
                addMember( member, false, referencedConsumerIds, externalConsumers, repositoryManager.getId(),
                    fileUtils );
            }

            // Build the index document
            String identityKey = IndexingUtils.getIdentityKey( sourceObject );
            Document indexDoc = new Document();

            indexDoc.add( new StringField( IndexingTerms.IDENTITY_FIELD, identityKey, Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.SEARCH_INDEX_FIELD, Boolean.TRUE + "", Field.Store.NO ) );
            indexDoc.add(
                new StringField( IndexingTerms.ENTITY_TYPE_FIELD, ServiceAssembly.class.getName(), Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.ENTITY_NAME_FIELD, assembly.getAssemblyIdentity().getName(),
                Field.Store.YES ) );
            indexDoc.add(
                new StringField( IndexingTerms.ENTITY_NAMESPACE_FIELD, sourceObject.getNamespace(), Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.BASE_NAMESPACE_FIELD, sourceObject.getBaseNamespace(),
                Field.Store.YES ) );
            indexDoc
                .add( new StringField( IndexingTerms.FILENAME_FIELD, sourceObject.getFilename(), Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.VERSION_FIELD, sourceObject.getVersion(), Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.VERSION_SCHEME_FIELD, sourceObject.getVersionScheme(),
                Field.Store.YES ) );
            indexDoc.add( new StringField( IndexingTerms.LATEST_VERSION_FIELD, latestVersion + "", Field.Store.NO ) );
            indexDoc.add( new StringField( IndexingTerms.LATEST_VERSION_AT_UNDER_REVIEW_FIELD, latestVersion + "",
                Field.Store.NO ) );
            indexDoc.add(
                new StringField( IndexingTerms.LATEST_VERSION_AT_FINAL_FIELD, latestVersion + "", Field.Store.NO ) );
            indexDoc.add(
                new StringField( IndexingTerms.LATEST_VERSION_AT_OBSOLETE_FIELD, Boolean.FALSE + "", Field.Store.NO ) );
            indexDoc.add( new TextField( IndexingTerms.KEYWORDS_FIELD, getFreeTextSearchContent(), Field.Store.NO ) );

            if (assembly.getDescription() != null) {
                indexDoc.add( new StringField( IndexingTerms.ENTITY_DESCRIPTION_FIELD, assembly.getDescription(),
                    Field.Store.YES ) );
            }
            if (sourceObject.getStatus() != null) {
                indexDoc.add( new StringField( IndexingTerms.STATUS_FIELD, sourceObject.getStatus().toString(),
                    Field.Store.YES ) );
            }
            if (assemblyContent != null) {
                indexDoc.add( new StoredField( IndexingTerms.CONTENT_DATA_FIELD, new BytesRef( assemblyContent ) ) );
            }
            for (String releaseId : referencedProviderIds) {
                indexDoc.add( new StringField( IndexingTerms.REFERENCED_RELEASE_FIELD, releaseId, Field.Store.NO ) );
                indexDoc.add( new StringField( IndexingTerms.REFERENCED_PROVIDER_FIELD, releaseId, Field.Store.YES ) );
            }
            for (String releaseId : referencedConsumerIds) {
                indexDoc.add( new StringField( IndexingTerms.REFERENCED_RELEASE_FIELD, releaseId, Field.Store.NO ) );
                indexDoc.add( new StringField( IndexingTerms.REFERENCED_CONSUMER_FIELD, releaseId, Field.Store.YES ) );
            }
            for (String externalRef : externalProviders) {
                indexDoc.add( new StringField( IndexingTerms.EXTERNAL_PROVIDER_FIELD, externalRef, Field.Store.YES ) );
            }
            for (String externalRef : externalConsumers) {
                indexDoc.add( new StringField( IndexingTerms.EXTERNAL_CONSUMER_FIELD, externalRef, Field.Store.YES ) );
            }

            getIndexWriter().updateDocument( new Term( IndexingTerms.IDENTITY_FIELD, identityKey ), indexDoc );

        } catch (RepositoryException | IOException | JAXBException e) {
            log.error( "Error creating index for OTM service assembly: " + sourceObject.getFilename(), e );
        }
    }

    /**
     * Encodes the given assembly member and adds it to one of the lists provided, depending upon whether the member is
     * internal (belongs to this repository) or external (owned by another repository).
     * 
     * @param member the assembly member to encode and add
     * @param isProvider flag indicating whether the given item represents a consumer or a provider API
     * @param internalReferences the list of internal repository references
     * @param externalReferences the list of external repository references
     * @param localRepositoryId the ID of the local repository (used to determine internal/external)
     * @param fileUtils the release file utils to use when encoding external members
     * @throws JAXBException thrown if an error occurs while adding the member
     */
    private void addMember(AssemblyItemType member, boolean isProvider, List<String> internalReferences,
        List<String> externalReferences, String localRepositoryId, ServiceAssemblyFileUtils fileUtils)
        throws JAXBException {
        Repository releaseRepo = getRepositoryManager().getRepository( member.getRepositoryID() );

        if ((releaseRepo != null) && localRepositoryId.equals( releaseRepo.getId() )) {
            RepositoryItemImpl memberItem = new RepositoryItemImpl();

            memberItem.setBaseNamespace( member.getBaseNamespace() );
            memberItem.setNamespace( member.getNamespace() );
            memberItem.setFilename( member.getFilename() );
            memberItem.setLibraryName( member.getLibraryName() );
            memberItem.setVersion( member.getVersion() );
            internalReferences.add( IndexingUtils.getIdentityKey( memberItem ) );

        } else { // external reference
            externalReferences.add( fileUtils.marshalAssemblyMember( member, isProvider ) );
        }
    }

    /**
     * Returns true if the given repository item is the latest version of its chain.
     * 
     * @param item the repository item to analyze
     * @return boolean
     * @throws RepositoryException thrown if an error occurs while accessing the repository
     */
    private boolean isLatestVersion(RepositoryItem item) throws RepositoryException {
        String releaseName = item.getLibraryName();
        String baseNS = item.getBaseNamespace();
        boolean result = true;

        for (RepositoryItem itemVersion : getRepositoryManager().listItems( baseNS, TLLibraryStatus.DRAFT, false )) {
            if (releaseName.equals( itemVersion.getLibraryName() )) {
                // Listed items are sorted in descending version order. If the first item we encounter
                // is not the one we are indexing, then the source item is not the latest version.
                result = item.getVersion().equals( itemVersion.getVersion() );
                break;
            }
        }
        return result;
    }

    /**
     * @see org.opentravel.repocommon.index.builder.IndexBuilder#deleteIndex()
     */
    @Override
    protected void deleteIndex() {
        try {
            RepositoryItem sourceObject = getSourceObject();
            String sourceObjectIdentity = IndexingUtils.getIdentityKey( sourceObject );

            getIndexWriter().deleteDocuments( new Term( IndexingTerms.IDENTITY_FIELD, sourceObjectIdentity ) );

        } catch (IOException e) {
            log.error( "Error deleting search index for OTM service assembly.", e );
        }
    }

}

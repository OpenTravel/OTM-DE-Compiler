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
import org.apache.lucene.util.BytesRef;
import org.opentravel.ns.ota2.assembly_v01_00.AssemblyItemType;
import org.opentravel.ns.ota2.assembly_v01_00.AssemblyType;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.impl.ServiceAssemblyFileUtils;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

/**
 * Search result object that encapsulates all relevant information about an OTM service assembly.
 */
public class AssemblySearchResult extends SearchResult<AssemblyType> {

    private static Log log = LogFactory.getLog( AssemblySearchResult.class );

    private String assemblyName;
    private String namespace;
    private String baseNamespace;
    private String filename;
    private String version;
    private List<String> referencedProviderIds = new ArrayList<>();
    private List<String> referencedConsumerIds = new ArrayList<>();
    private List<AssemblyItemType> externalProviders = new ArrayList<>();
    private List<AssemblyItemType> externalConsumers = new ArrayList<>();

    /**
     * Constructor that initializes the search result contents from the given <code>Document</code>.
     * 
     * @param doc the index document from which to initialize the assembly information
     * @param searchService the indexing search service that created this search result
     */
    public AssemblySearchResult(Document doc, FreeTextSearchService searchService) {
        super( doc, searchService );
        ServiceAssemblyFileUtils fileUtils = new ServiceAssemblyFileUtils( searchService.getRepositoryManager() );

        this.assemblyName = doc.get( IndexingTerms.ENTITY_NAME_FIELD );
        this.namespace = doc.get( IndexingTerms.ENTITY_NAMESPACE_FIELD );
        this.baseNamespace = doc.get( IndexingTerms.BASE_NAMESPACE_FIELD );
        this.filename = doc.get( IndexingTerms.FILENAME_FIELD );
        this.version = doc.get( IndexingTerms.VERSION_FIELD );
        this.referencedProviderIds.addAll( Arrays.asList( doc.getValues( IndexingTerms.REFERENCED_PROVIDER_FIELD ) ) );
        this.referencedConsumerIds.addAll( Arrays.asList( doc.getValues( IndexingTerms.REFERENCED_CONSUMER_FIELD ) ) );

        for (String memberContent : doc.getValues( IndexingTerms.EXTERNAL_PROVIDER_FIELD )) {
            addAssemblyMember( memberContent, externalProviders, fileUtils );
        }
        for (String memberContent : doc.getValues( IndexingTerms.EXTERNAL_CONSUMER_FIELD )) {
            addAssemblyMember( memberContent, externalConsumers, fileUtils );
        }
        if (doc.getBinaryValue( IndexingTerms.CONTENT_DATA_FIELD ) != null) {
            initializeItemContent( doc );
        }
    }

    /**
     * Unmarshalls the assembly member content string provided and adds the member to the list provided.
     * 
     * @param memberContent the assembly member content string
     * @param memberList the list to which the assembly member will be added
     * @param fileUtils the file utilities to use when unmarshalling the assembly member
     */
    private void addAssemblyMember(String memberContent, List<AssemblyItemType> memberList,
        ServiceAssemblyFileUtils fileUtils) {
        try {
            memberList.add( fileUtils.unmarshalAssemblyMemberContent( memberContent ) );

        } catch (JAXBException e) {
            // Ignore error and continue
        }
    }

    /**
     * @see org.opentravel.schemacompiler.index.SearchResult#initializeItemContent(org.apache.lucene.document.Document)
     */
    @Override
    protected void initializeItemContent(Document itemDoc) {
        try {
            BytesRef binaryContent =
                (itemDoc == null) ? null : itemDoc.getBinaryValue( IndexingTerms.CONTENT_DATA_FIELD );
            String content = (binaryContent == null) ? null : binaryContent.utf8ToString();

            if (content != null) {
                setItemContent( new ServiceAssemblyFileUtils( getSearchService().getRepositoryManager() )
                    .loadJaxbAssembly( new StringReader( content ) ) );
            }

        } catch (RepositoryException | JAXBException e) {
            log.error( "Error initializing service assembly content.", e );
        }
    }

    /**
     * Returns the name of the OTM service assembly.
     *
     * @return String
     */
    public String getAssemblyName() {
        return assemblyName;
    }

    /**
     * Returns the namespace of the OTM service assembly.
     *
     * @return String
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the base namespace of the OTM service assembly.
     *
     * @return String
     */
    public String getBaseNamespace() {
        return baseNamespace;
    }

    /**
     * Returns the filename of the OTM service assembly.
     *
     * @return String
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Returns the version of the OTM service assembly.
     *
     * @return String
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the version of the OTM service assembly (always FINAL for assemblies).
     * 
     * @return TLLibraryStatus
     */
    public TLLibraryStatus getStatus() {
        return TLLibraryStatus.FINAL;
    }

    /**
     * Returns the search index ID's of all provider-side releases that are referenced by this service assembly.
     * 
     * @return List&lt;String&gt;
     */
    public List<String> getReferencedProviderIds() {
        return referencedProviderIds;
    }

    /**
     * Returns the search index ID's of all consumer-side releases that are referenced by this service assembly.
     * 
     * @return List&lt;String&gt;
     */
    public List<String> getReferencedConsumerIds() {
        return referencedConsumerIds;
    }

    /**
     * Returns the list provider-side releases that are managed by external repositories.
     * 
     * @return List&lt;AssemblyItemType&gt;
     */
    public List<AssemblyItemType> getExternalProviders() {
        return externalProviders;
    }

    /**
     * Returns the list consumer-side releases that are managed by external repositories.
     * 
     * @return List&lt;AssemblyItemType&gt;
     */
    public List<AssemblyItemType> getExternalConsumers() {
        return externalConsumers;
    }

}

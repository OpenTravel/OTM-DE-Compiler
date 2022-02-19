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

package org.opentravel.schemacompiler.repository.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.repository.Release;
import org.opentravel.schemacompiler.repository.ReleaseMember;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.w3._2001.xmlschema.Schema;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the <code>LibraryModuleLoader</code> interface that applies effective dates to the library input
 * sources across multiple OTM releases. All other module loader functions are delegated to an underlying module loader
 * instance.
 */
public class MultiReleaseModuleLoader implements LibraryModuleLoader<InputStream> {

    private static final Logger log = LogManager.getLogger( MultiReleaseModuleLoader.class );

    private Map<String,ReleaseMember> libraryUrltoReleaseMemberMap = new HashMap<>();
    private LibraryModuleLoader<InputStream> delegate;
    private RepositoryManager repositoryManager;

    /**
     * Constructor that supplies the list of OTM <code>Release</code>s for which library content is to be loaded.
     * 
     * @param releaseList the list of OTM releases to be loaded
     * @param repositoryManager the repository manager instance
     * @param delegateLoader the underlying delegate module loader that will perform most tasks
     */
    public MultiReleaseModuleLoader(List<Release> releaseList, RepositoryManager repositoryManager,
        LibraryModuleLoader<InputStream> delegateLoader) {
        this.repositoryManager = repositoryManager;
        this.delegate = delegateLoader;
        initialize( releaseList );
    }

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryModuleLoader#newInputSource(java.net.URL)
     */
    @Override
    public LibraryInputSource<InputStream> newInputSource(URL libraryUrl) {
        ReleaseMember releaseMember = libraryUrltoReleaseMemberMap.get( libraryUrl.toExternalForm() );
        LibraryInputSource<InputStream> inputSource = null;

        if (releaseMember != null) {
            try {
                inputSource = repositoryManager.getHistoricalContentSource( releaseMember.getRepositoryItem(),
                    releaseMember.getEffectiveDate() );

            } catch (RepositoryException e) {
                log.warn( "Unexpected exception while loading historical content: "
                    + releaseMember.getRepositoryItem().getFilename(), e );
            }
        }

        if (inputSource == null) {
            inputSource = delegate.newInputSource( libraryUrl );
        }
        return inputSource;
    }

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryModuleLoader#isLibraryInputSource(org.opentravel.schemacompiler.loader.LibraryInputSource)
     */
    @Override
    public boolean isLibraryInputSource(LibraryInputSource<InputStream> inputSource) {
        return delegate.isLibraryInputSource( inputSource );
    }

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryModuleLoader#loadLibrary(org.opentravel.schemacompiler.loader.LibraryInputSource,
     *      org.opentravel.schemacompiler.validate.ValidationFindings)
     */
    @Override
    public LibraryModuleInfo<Object> loadLibrary(LibraryInputSource<InputStream> inputSource,
        ValidationFindings validationFindings) throws LibraryLoaderException {
        return delegate.loadLibrary( inputSource, validationFindings );
    }

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryModuleLoader#loadSchema(org.opentravel.schemacompiler.loader.LibraryInputSource,
     *      org.opentravel.schemacompiler.validate.ValidationFindings)
     */
    @Override
    public LibraryModuleInfo<Schema> loadSchema(LibraryInputSource<InputStream> inputSource,
        ValidationFindings validationFindings) throws LibraryLoaderException {
        return delegate.loadSchema( inputSource, validationFindings );
    }

    /**
     * Initializes the mappings of library URLs to their corresponding release members.
     * 
     * @param releaseList the list of OTM releases to be loaded
     */
    private void initialize(List<Release> releaseList) {
        for (Release release : releaseList) {
            for (ReleaseMember member : release.getAllMembers()) {
                try {
                    RepositoryItem memberItem = member.getRepositoryItem();
                    File libraryFile = repositoryManager.getFileManager().getLibraryContentLocation(
                        memberItem.getBaseNamespace(), memberItem.getFilename(), memberItem.getVersion() );
                    String libraryUrl = URLUtils.toURL( libraryFile ).toExternalForm();

                    libraryUrltoReleaseMemberMap.put( libraryUrl, member );

                } catch (RepositoryException e) {
                    log.warn(
                        "Error resolving content location for library: " + member.getRepositoryItem().getFilename(),
                        e );
                }
            }
        }
    }

}

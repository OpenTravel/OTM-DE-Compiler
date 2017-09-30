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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.repository.Release;
import org.opentravel.schemacompiler.repository.ReleaseManager;
import org.opentravel.schemacompiler.repository.ReleaseMember;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemCommit;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2001.xmlschema.Schema;

/**
 * Implementation of the <code>LibraryModuleLoader</code> interface that applies effective
 * dates to the library input sources as required by the OTM release specification.  All other
 * module loader functions are delegated to an underlying module loader instance.
 * 
 * <p>During the loading process the list of referenced libraries is also refreshed.  Any
 * references that are no longer required are removed from the release, and any new references
 * are applied using the release's default effective date.
 */
public class ReleaseLibraryModuleLoader implements LibraryModuleLoader<InputStream> {
	
    private static final Logger log = LoggerFactory.getLogger( ReleaseLibraryModuleLoader.class );
    
	private ReleaseManager releaseManager;
	private LibraryModuleLoader<InputStream> delegate;
	private List<ReleaseMember> referencedMembers = new ArrayList<>();
	private Set<String> referenceUrls = new HashSet<>();
	
	/**
	 * Constructor that specifies the OTM release, as well as the module loader delegate.
	 * 
	 * @param releaseManager  the release manage that will provide the effective dates for loaded libraries
	 * @param delegateLoader  the underlying delegate module loader that will perform most tasks
	 */
	public ReleaseLibraryModuleLoader(ReleaseManager releaseManager,
			LibraryModuleLoader<InputStream> delegateLoader) {
		this.releaseManager = releaseManager;
		this.delegate = delegateLoader;
	}
	
	/**
	 * Updates the list of referenced items and their associated effective dates
	 * for the given release.  The new list of referenced items is obtained from the
	 * non-principal dependencies that were loaded by this module loader.
	 */
	public void updateReferencedItems() {
		Release release = releaseManager.getRelease();
		
		release.getReferencedMembers().clear();
		release.getReferencedMembers().addAll( referencedMembers );
	}

	/**
	 * @see org.opentravel.schemacompiler.loader.LibraryModuleLoader#newInputSource(java.net.URL)
	 */
	@Override
	public LibraryInputSource<InputStream> newInputSource(URL libraryUrl) {
		LibraryInputSource<InputStream> inputSource = null;
		
		if (URLUtils.isFileURL( libraryUrl )) {
			File libraryFile = URLUtils.toFile( libraryUrl );
			
			try {
				RepositoryItem item = releaseManager.getRepositoryItem( libraryUrl );
				
				if (item != null) {
					ReleaseMember principalMember = releaseManager.getPrincipalMember( item );
					ReleaseMember referencedMember = releaseManager.getReferencedMember( item );
					ReleaseMember member = (principalMember != null) ? principalMember : referencedMember;
					
					if (member == null) { // New referenced member for the release
						member = new ReleaseMember();
						member.setRepositoryItem( item );
						setEffectiveDate( member );
					}
					
					// Only need a date-effective input source if we have an effective date specified
					// by the release
					if (member.getEffectiveDate() != null) {
						inputSource = releaseManager.getInputSource( member );
					}
					
					if ((principalMember == null) && !referenceUrls.contains( libraryUrl.toExternalForm() )) {
						referenceUrls.add( libraryUrl.toExternalForm() );
						referencedMembers.add( member );
					}
				}
				
			} catch (RepositoryException e) {
				log.warn("Unexpected exception while loading historical content: "
						+ libraryFile.getName(), e);
			}
			
		}
		
		// If the effective date is not adjusted by the release specification,
		// use the delegate to create it.
		if (inputSource == null) {
			inputSource = delegate.newInputSource( libraryUrl );
		}
		return inputSource;
	}
	
	/**
	 * Assigns an appropriate effective date to the new release member.
	 * 
	 * @param member  the new release member whose effective date is to be configured
	 * @throws RepositoryException  thrown if the remote repository cannot be accessed
	 */
	private void setEffectiveDate(ReleaseMember member) throws RepositoryException {
		List<RepositoryItemCommit> commitHistory = releaseManager.getCommitHistory( member );
		Date defaultEffectiveDate = releaseManager.getRelease().getDefaultEffectiveDate();
		
		if (defaultEffectiveDate != null) {
			Date latestCommitDate = null;
			
			for (RepositoryItemCommit commit : commitHistory) {
				if ((latestCommitDate == null) || commit.getEffectiveOn().after( latestCommitDate )) {
					latestCommitDate = commit.getEffectiveOn();
				}
			}
			if (defaultEffectiveDate.after( latestCommitDate )) {
				member.setEffectiveDate( latestCommitDate );
				
			} else {
				member.setEffectiveDate( defaultEffectiveDate );
			}
			
		} else {
			member.setEffectiveDate( null );
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.loader.LibraryModuleLoader#isLibraryInputSource(org.opentravel.schemacompiler.loader.LibraryInputSource)
	 */
	@Override
	public boolean isLibraryInputSource(LibraryInputSource<InputStream> inputSource) {
		return delegate.isLibraryInputSource( inputSource );
	}

	/**
	 * @see org.opentravel.schemacompiler.loader.LibraryModuleLoader#loadLibrary(org.opentravel.schemacompiler.loader.LibraryInputSource, org.opentravel.schemacompiler.validate.ValidationFindings)
	 */
	@Override
	public LibraryModuleInfo<Object> loadLibrary(LibraryInputSource<InputStream> inputSource,
			ValidationFindings validationFindings) throws LibraryLoaderException {
		return delegate.loadLibrary( inputSource, validationFindings );
	}

	/**
	 * @see org.opentravel.schemacompiler.loader.LibraryModuleLoader#loadSchema(org.opentravel.schemacompiler.loader.LibraryInputSource, org.opentravel.schemacompiler.validate.ValidationFindings)
	 */
	@Override
	public LibraryModuleInfo<Schema> loadSchema(LibraryInputSource<InputStream> inputSource,
			ValidationFindings validationFindings) throws LibraryLoaderException {
		return delegate.loadSchema( inputSource, validationFindings );
	}
	
}

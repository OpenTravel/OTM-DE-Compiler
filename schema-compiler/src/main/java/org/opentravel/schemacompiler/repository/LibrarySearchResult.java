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
package org.opentravel.schemacompiler.repository;

import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;

/**
 * Search result from a remote repository that encapsulates an OTM library that
 * matched the search criteria.
 */
public class LibrarySearchResult extends RepositorySearchResult {
	
	/**
	 * Constructor that initializes the search result item using the information provided.
	 * 
	 * @param libraryInfo  library meta-data returned from the remote repository
	 * @param manager  the repository manager for the local environment
	 */
	public LibrarySearchResult(LibraryInfoType libraryInfo, RepositoryManager manager) {
		super( RepositoryUtils.createRepositoryItem( manager, libraryInfo ) );
		checkItemState( manager );
	}
	
}

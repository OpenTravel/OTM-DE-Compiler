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

package org.opentravel.schemacompiler.validate.assembly;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.repository.ReleaseItem;
import org.opentravel.schemacompiler.repository.ReleaseManager;
import org.opentravel.schemacompiler.repository.ReleaseMember;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemCommit;
import org.opentravel.schemacompiler.repository.RepositoryItemHistory;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.ServiceAssembly;
import org.opentravel.schemacompiler.repository.ServiceAssemblyItem;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationContext;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validation context to be used during the validation of
 * <code>ServiceAssembly</code> elements.
 */
public class AssemblyValidationContext implements ValidationContext {
	
	private static final Logger log = LoggerFactory.getLogger(ValidationContext.class);
	
	private RepositoryManager repositoryManager;
	private Map<String,List<Integer>> libraryCommitNumbers = new HashMap<>();
	private Map<String,List<RepositoryItem>> releaseLibraries = new HashMap<>();
	private ServiceAssembly assembly;
	
	/**
	 * Constructor that provides the service assembly being validated.
	 * 
	 * @param assembly  the assembly that is currently being validated
	 * @throws RepositoryException  throw if the default repository manager cannot be initialized
	 */
	public AssemblyValidationContext(ServiceAssembly assembly) throws RepositoryException {
		this( assembly, RepositoryManager.getDefault() );
	}
	
	/**
	 * Constructor that provides the service assembly being validated and the repository
	 * manager to use for remote access.
	 * 
	 * @param assembly  the assembly that is currently being validated
	 * @param repositoryManager  the repository manager instance
	 */
	public AssemblyValidationContext(ServiceAssembly assembly, RepositoryManager manager) {
		this.assembly = assembly;
		this.repositoryManager = manager;
		initialize();
	}
	
	/**
	 * Returns the service assembly that is the context of all validation activities.
	 *
	 * @return ServiceAssembly
	 */
	public ServiceAssembly getAssembly() {
		return assembly;
	}
	
	/**
	 * Returns the list of library repository items specified in the assembly item's
	 * release.
	 * 
	 * @param assemblyItem  the assembly item for which to return the list of library repository items
	 * @return List<RepositoryItem>
	 */
	public List<RepositoryItem> getLibraryItems(ServiceAssemblyItem assemblyItem) {
		List<RepositoryItem> itemList = new ArrayList<>();
		
		if (assemblyItem != null) {
			String releaseId = getUniqueId( assemblyItem.getReleaseItem() );

			if (releaseLibraries.containsKey( releaseId )) {
				itemList.addAll( releaseLibraries.get( releaseId ) );
			}
		}
		return itemList;
	}
	
	/**
	 * Returns true if multiple commit levels are specified for the given library
	 * within the <code>ServiceAssembly</code>.
	 * 
	 * @param libraryItem  the repository item for the library to check
	 * @return boolean
	 */
	public boolean hasMultipleCommitLevels(RepositoryItem libraryItem) {
		boolean multipleCommits = false;
		
		if (libraryItem != null) {
			String libraryId = getUniqueId( libraryItem );
			List<Integer> commitList = libraryCommitNumbers.get( libraryId );
			
			multipleCommits = (commitList != null) && (commitList.size() > 1);
		}
		return multipleCommits;
	}
	
	/**
	 * Initializes the map of OTM libraries to their corresponding commit number(s)
	 * specified in the various assembly releases, as well as the list of repository
	 * items from each assembly item's release.
	 * 
	 * <p>These caches will avoid redundant file loads and remote lookups during the
	 * validation process.
	 */
	private void initialize() {
		ReleaseManager releaseManager = new ReleaseManager( repositoryManager );
		List<ServiceAssemblyItem> saItems = assembly.getAllApis();
		
		for (ServiceAssemblyItem saItem : saItems) {
			try {
				ValidationFindings findings = new ValidationFindings();
				ReleaseItem releaseItem = releaseManager.loadRelease( saItem.getReleaseItem(), findings );
				String releaseId = getUniqueId( releaseItem );
				
				if (!releaseLibraries.containsKey( releaseId ) && !findings.hasFinding( FindingType.ERROR )) {
					List<RepositoryItem> libraryItems = new ArrayList<>();
					
					for (ReleaseMember member : releaseItem.getContent().getAllMembers()) {
						String libraryId = getUniqueId( member.getRepositoryItem() );
						int commitNumber = getCommitNumber( member );
						
						if (commitNumber >= 0) {
							libraryCommitNumbers.computeIfAbsent( libraryId,
									id -> libraryCommitNumbers.put( id, new ArrayList<>() ) );
							libraryCommitNumbers.get( libraryId ).add( commitNumber );
						}
						libraryItems.add( member.getRepositoryItem() );
					}
					releaseLibraries.put( releaseId, libraryItems );
				}
				
			} catch (RepositoryException e) {
				log.warn( "Unable to load release from remote repository", e );
			}
		}
	}
	
	/**
	 * Returns the commit number for the library associated with the given release
	 * member.  If no commit is valid for the member's effective date, -1 will be returned.
	 * 
	 * @param member  the release member for which to return a library commit number
	 * @return int
	 */
	private int getCommitNumber(ReleaseMember member) {
		int commitNumber = -1;
		try {
			RepositoryItemHistory memberHistory = repositoryManager.getHistory( member.getRepositoryItem() );
			Date effectiveDate = member.getEffectiveDate();
			
			if (effectiveDate == null) {
				RepositoryItemCommit lastCommit = memberHistory.getCommitHistory().get( 0 );
				
				commitNumber = lastCommit.getCommitNumber();
				
			} else {
				for (RepositoryItemCommit commitItem : memberHistory.getCommitHistory()) {
					Date commitDate = commitItem.getEffectiveOn();
					
					if (commitDate.compareTo( effectiveDate ) <= 0) {
						commitNumber = commitItem.getCommitNumber();
						break;
					}
				}
			}
			
		} catch (RepositoryException e) {
			log.warn( "Unable to load library history from remote repository.", e );
		}
		return commitNumber;
	}
	
	/**
	 * Returns a unique identity string for the given repository item.
	 * 
	 * @param item  the repository item for which to return a unique identifier
	 * @return String
	 */
	private String getUniqueId(RepositoryItem item) {
		String releaseId;
		
		if (item != null) {
			releaseId = item.getBaseNamespace() + "~" + item.getFilename() + "~" + item.getVersion();
		} else {
			releaseId = "UNKNOWN";
		}
		return releaseId;
	}
	
}

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
package org.opentravel.otm.repository.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Calculates the creation dates for OTM entities as well as when (and if)
 * they were deleted from the model.
 */
public class EntityDateCalculator {
	
	private static Comparator<Date> descendingDateComparator = new Comparator<Date> () {
		public int compare(Date d1, Date d2) {
			return -d1.compareTo( d2 );
		}
	};
	private static Comparator<TREntityVersion> entityVersionComparator = new Comparator<TREntityVersion> () {
		public int compare(TREntityVersion e1, TREntityVersion e2) {
			return e1.getCreateDate().compareTo( e2.getCreateDate() );
		}
	};
	
	private Map<String,SortedMap<Date,Set<TREntityVersion>>> createCommitGroupMap = new HashMap<>();
	private Map<String,SortedMap<Date,Set<TREntityVersion>>> deleteCommitGroupMap = new HashMap<>();
	
	/**
	 * Assigns creation and deletion dates for all of the entities and entity versions that have
	 * been added to this calculator.
	 */
	public void assignDates() {
		Map<TREntity,List<TREntityVersion>> masterVersionMap = new HashMap<>();
		
		for (String libraryKey : deleteCommitGroupMap.keySet()) {
			
			// Determine the create date for each entity version
			SortedMap<Date,Set<TREntityVersion>> createCommitGroups = createCommitGroupMap.get( libraryKey );
			
			for (Date commitDate : createCommitGroups.keySet()) {
				for (TREntityVersion entityVersion : createCommitGroups.get( commitDate )) {
					List<TREntityVersion> versionList = masterVersionMap.get( entityVersion.getEntity() );
					
					if (versionList == null) {
						versionList = new ArrayList<>();
						masterVersionMap.put( entityVersion.getEntity(), versionList );
					}
					versionList.add( entityVersion );
					entityVersion.setCreateDate( commitDate );
				}
			}
			
			// Determine the delete date (if any) for each entity version
			SortedMap<Date,Set<TREntityVersion>> deleteCommitGroups = deleteCommitGroupMap.get( libraryKey );
			Date previousCommitDate = null;
			
			for (Date commitDate : deleteCommitGroups.keySet()) {
				for (TREntityVersion entityVersion : deleteCommitGroups.get( commitDate )) {
					entityVersion.setDeleteDate( previousCommitDate );
				}
				previousCommitDate = commitDate;
			}
		}
		
		// Determine the create ad delete date for each entity master based upon
		// its earliest and latest versions respectively
		for (TREntity entityMaster : masterVersionMap.keySet()) {
			List<TREntityVersion> versionList = masterVersionMap.get( entityMaster );
			Collections.sort( versionList, entityVersionComparator );
			TREntityVersion earliestVersion = versionList.get( 0 );
			TREntityVersion latestVersion = versionList.get( versionList.size() - 1 );
			
			entityMaster.setCreateDate( earliestVersion.getCreateDate() );
			entityMaster.setDeleteDate( latestVersion.getDeleteDate() );
		}
	}
	
	/**
	 * Adds the given entity version to this calculator instance.
	 * 
	 * @param entityVersion  the entity version to add
	 * @param commitDate  the commit date for the entity
	 */
	public void add(TREntityVersion entityVersion, Date commitDate) {
		addToCommitGroup( entityVersion, commitDate, createCommitGroupMap, null );
		addToCommitGroup( entityVersion, commitDate, deleteCommitGroupMap, descendingDateComparator );
	}
	
	/**
	 * Adds the given entity to the commit group map provided.
	 * 
	 * @param entityVersion  the entity version to add
	 * @param commitDate  the commit date for the entity
	 * @param commitGroupMap  the commit group map to which the entity version should be added
	 * @param commitComparator  comparator used for sorting the commit dates in the map
	 */
	private void addToCommitGroup(TREntityVersion entityVersion, Date commitDate,
			Map<String,SortedMap<Date,Set<TREntityVersion>>> commitGroupMap, Comparator<Date> commitComparator) {
		String libraryKey = getLibraryKey( entityVersion );
		SortedMap<Date,Set<TREntityVersion>> commitGroups = commitGroupMap.get( libraryKey );
		Set<TREntityVersion> currentCommitGroup;
		boolean foundFirstOccurrance = false;
		
		if (commitGroups == null) {
			commitGroups = new TreeMap<>( commitComparator );
			commitGroupMap.put( libraryKey, commitGroups );
		}
		currentCommitGroup = commitGroups.get( commitDate );
		
		if (currentCommitGroup == null) {
			currentCommitGroup = new HashSet<>();
			commitGroups.put( commitDate, currentCommitGroup );
		}
		currentCommitGroup.add( entityVersion );
		
		for (Date cDate : commitGroups.keySet()) {
			Set<TREntityVersion> cGroup = commitGroups.get( cDate );
			
			if (!foundFirstOccurrance) {
				foundFirstOccurrance = cGroup.contains( entityVersion );
				
			} else {
				cGroup.remove( entityVersion );
			}
		}
	}
	
	/**
	 * Returns a unique key for the library master that owns the given entity
	 * version.
	 * 
	 * @param entityVersion  the entity for which to return a library key
	 * @return String
	 */
	private String getLibraryKey(TREntityVersion entityVersion) {
		TRLibraryVersion libraryVersion = entityVersion.getLibraryVersion();
		TRLibrary library = libraryVersion.getLibrary();
		
		return library.getBaseNamespace() + ":" + libraryVersion.getVersion() + ":" + library.getLibraryName();
	}
	
}

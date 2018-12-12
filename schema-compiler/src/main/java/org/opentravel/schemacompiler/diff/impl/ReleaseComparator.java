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

package org.opentravel.schemacompiler.diff.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.diff.LibraryChangeSet;
import org.opentravel.schemacompiler.diff.ModelCompareOptions;
import org.opentravel.schemacompiler.diff.ReleaseChangeItem;
import org.opentravel.schemacompiler.diff.ReleaseChangeSet;
import org.opentravel.schemacompiler.diff.ReleaseChangeType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.repository.Release;
import org.opentravel.schemacompiler.repository.ReleaseManager;

/**
 * Performs a comparison of two OTM projects.
 */
public class ReleaseComparator extends BaseComparator {
	
	/**
	 * Constructor that initializes the comparison options for the comparator.
	 * 
	 * @param compareOptions  the model comparison options to apply during processing
	 */
	public ReleaseComparator(ModelCompareOptions compareOptions) {
		super( compareOptions, null );
	}
	
	/**
	 * Compares two versions of the same OTM release.
	 * 
	 * @param oldReleaseManager  the old release version
	 * @param newReleaseManager  the new release version
	 * @return ReleaseChangeSet
	 */
	public ReleaseChangeSet compareReleases(ReleaseManager oldReleaseManager, ReleaseManager newReleaseManager) {
		Release oldRelease = oldReleaseManager.getRelease();
		Release newRelease = newReleaseManager.getRelease();
		String oldStatus = (oldRelease.getStatus() == null) ? null : oldRelease.getStatus().toString();
		String newStatus = (newRelease.getStatus() == null) ? null : newRelease.getStatus().toString();
		ReleaseChangeSet changeSet = new ReleaseChangeSet( oldRelease, newRelease );
		List<ReleaseChangeItem> changeItems = changeSet.getChangeItems();
		Map<QName,TLLibrary> oldLibraries = buildLibraryMap( oldReleaseManager );
		Map<QName,TLLibrary> newLibraries = buildLibraryMap( newReleaseManager );
		SortedSet<QName> oldLibraryNames = new TreeSet<>( new QNameComparator() );
		SortedSet<QName> newLibraryNames = new TreeSet<>( new QNameComparator() );
		List<ChangeSetItem> pendingChangeSets = new ArrayList<>();
		Iterator<QName> iterator;
		
		oldLibraryNames.addAll( oldLibraries.keySet() );
		newLibraryNames.addAll( newLibraries.keySet() );
		
		// Look for changes in the project values
		if (valueChanged( oldRelease.getName(), newRelease.getName() )) {
			changeItems.add( new ReleaseChangeItem( changeSet, ReleaseChangeType.NAME_CHANGED,
					oldRelease.getName(), newRelease.getName() ) );
		}
		if (valueChanged( oldRelease.getBaseNamespace(), newRelease.getBaseNamespace() )) {
			changeItems.add( new ReleaseChangeItem( changeSet, ReleaseChangeType.BASE_NAMESPACE_CHANGED,
					oldRelease.getBaseNamespace(), newRelease.getBaseNamespace() ) );
		}
		if (valueChanged( oldRelease.getVersion(), newRelease.getVersion() )) {
			changeItems.add( new ReleaseChangeItem( changeSet, ReleaseChangeType.VERSION_CHANGED,
					oldRelease.getVersion(), newRelease.getVersion() ) );
		}
		if (valueChanged( oldStatus, newStatus )) {
			changeItems.add( new ReleaseChangeItem(
					changeSet, ReleaseChangeType.STATUS_CHANGED, oldStatus, newStatus ) );
		}
		if (valueChanged( oldRelease.getDescription(), newRelease.getDescription() )) {
			changeItems.add( new ReleaseChangeItem( changeSet, ReleaseChangeType.DESCRIPTION_CHANGED,
					oldRelease.getDescription(), newRelease.getDescription() ) );
		}
		
		// Identify libraries that were modified at the same version.  As we identify
		// matching libraries, remove the names that were processed so they will not be
		// considered in subsequent steps.
		iterator = oldLibraryNames.iterator();
		
		while (iterator.hasNext()) {
			QName libraryName = iterator.next();
			
			if (newLibraries.containsKey( libraryName )) {
				TLLibrary oldLibrary = oldLibraries.get( libraryName );
				TLLibrary newLibrary = newLibraries.get( libraryName );
				
				pendingChangeSets.add( new ChangeSetItem(
						ReleaseChangeType.LIBRARY_CHANGED, oldLibrary, newLibrary ) );
				newLibraryNames.remove( libraryName );
				iterator.remove();
			}
		}
		
		// Identify libraries that were modified at a different version.  As we identify
		// matching libraries, remove the names that were processed so they will not be
		// considered in subsequent steps.
		iterator = oldLibraryNames.iterator();
		
		while (iterator.hasNext()) {
			QName libraryName = iterator.next();
			TLLibrary oldLibrary = oldLibraries.get( libraryName );
			String versionScheme = oldLibrary.getVersionScheme();
			List<QName> newVersionMatches = ModelCompareUtils.findMatchingVersions(
					libraryName, newLibraryNames, versionScheme );
			
			if (!newVersionMatches.isEmpty()) {
				QName closestVersionMatch = ModelCompareUtils.findClosestVersion(
						libraryName, newVersionMatches, versionScheme );
				
				if (closestVersionMatch != null) {
					TLLibrary newLibrary = newLibraries.get( closestVersionMatch );
					
					addNamespaceMapping( oldLibrary.getNamespace(), newLibrary.getNamespace() );
					pendingChangeSets.add( new ChangeSetItem(
							ReleaseChangeType.LIBRARY_VERSION_CHANGED, oldLibrary, newLibrary ) );
					newLibraryNames.remove( closestVersionMatch );
					iterator.remove();
				}
			}
		}
		
		// Any new names left over represent libraries that were added
		for (QName newName : newLibraryNames) {
			changeItems.add( new ReleaseChangeItem( changeSet,
					ReleaseChangeType.LIBRARY_ADDED, newLibraries.get( newName ) ) );
		}
		
		// Any old names left over represent libraries that were removed
		for (QName oldName : oldLibraryNames) {
			changeItems.add( new ReleaseChangeItem( changeSet,
					ReleaseChangeType.LIBRARY_DELETED, oldLibraries.get( oldName ) ) );
		}
		
		// Process all of the pending change sets that were identified during library
		// comparisons.
		for (ChangeSetItem item : pendingChangeSets) {
			LibraryChangeSet libraryChangeSet =
					new LibraryComparator( getCompareOptions(), getNamespaceMappings() )
							.compareLibraries( item.getOldVersion(), item.getNewVersion() );
			
			if (!libraryChangeSet.getChangeItems().isEmpty()) {
				changeItems.add( new ReleaseChangeItem( changeSet, item.getChangeType(), libraryChangeSet ) );
			}
		}
		
		return changeSet;
	}
	
	/**
	 * Constructs a map of QNames for each user-defined library in the release
	 * using the namespace and library name as the key.
	 * 
	 * @param release  the release from which to obtain the set of user-defined libraries
	 * @return Map<QName,TLLibrary>
	 */
	private Map<QName,TLLibrary> buildLibraryMap(ReleaseManager releaseManager) {
		Map<QName,TLLibrary> libraryMap = new HashMap<>();
		
		for (TLLibrary library : releaseManager.getModel().getUserDefinedLibraries()) {
			libraryMap.put( getLibraryName( library ), library );
		}
		return libraryMap;
	}
	
	/**
	 * Encapsulates a change set that has not yet been processed.
	 */
	private static class ChangeSetItem {
		
		private ReleaseChangeType changeType;
		private TLLibrary oldVersion;
		private TLLibrary newVersion;
		
		/**
		 * Full constructor.
		 * 
		 * @param changeType  the type of change that occurred
		 * @param oldVersion  the old version of the library
		 * @param newVersion  the new version of the library
		 */
		public ChangeSetItem(ReleaseChangeType changeType, TLLibrary oldVersion, TLLibrary newVersion) {
			this.setChangeType(changeType);
			this.setOldVersion(oldVersion);
			this.setNewVersion(newVersion);
		}

		/**
		 * Returns the value of the 'changeType' field.
		 *
		 * @return ReleaseChangeType
		 */
		public ReleaseChangeType getChangeType() {
			return changeType;
		}

		/**
		 * Assigns the value of the 'changeType' field.
		 *
		 * @param changeType  the field value to assign
		 */
		public void setChangeType(ReleaseChangeType changeType) {
			this.changeType = changeType;
		}

		/**
		 * Returns the value of the 'oldVersion' field.
		 *
		 * @return TLLibrary
		 */
		public TLLibrary getOldVersion() {
			return oldVersion;
		}

		/**
		 * Assigns the value of the 'oldVersion' field.
		 *
		 * @param oldVersion  the field value to assign
		 */
		public void setOldVersion(TLLibrary oldVersion) {
			this.oldVersion = oldVersion;
		}

		/**
		 * Returns the value of the 'newVersion' field.
		 *
		 * @return TLLibrary
		 */
		public TLLibrary getNewVersion() {
			return newVersion;
		}

		/**
		 * Assigns the value of the 'newVersion' field.
		 *
		 * @param newVersion  the field value to assign
		 */
		public void setNewVersion(TLLibrary newVersion) {
			this.newVersion = newVersion;
		}
		
	}
	
}

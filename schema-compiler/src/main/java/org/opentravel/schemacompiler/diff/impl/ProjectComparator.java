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
import org.opentravel.schemacompiler.diff.ProjectChangeItem;
import org.opentravel.schemacompiler.diff.ProjectChangeSet;
import org.opentravel.schemacompiler.diff.ProjectChangeType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;

/**
 * Performs a comparison of two OTM projects.
 */
public class ProjectComparator extends BaseComparator {
	
	/**
	 * Constructor that initializes the comparison options for the comparator.
	 * 
	 * @param compareOptions  the model comparison options to apply during processing
	 */
	public ProjectComparator(ModelCompareOptions compareOptions) {
		super( compareOptions, null );
	}
	
	/**
	 * Compares two versions of the same OTM project.
	 * 
	 * @param oldProject  the old project version
	 * @param newProject  the new project version
	 * @return ProjectChangeSet
	 */
	public ProjectChangeSet compareProjects(Project oldProject, Project newProject) {
		ProjectChangeSet changeSet = new ProjectChangeSet( oldProject, newProject );
		List<ProjectChangeItem> changeItems = changeSet.getChangeItems();
		Map<QName,TLLibrary> oldLibraries = buildLibraryMap( oldProject );
		Map<QName,TLLibrary> newLibraries = buildLibraryMap( newProject );
		SortedSet<QName> oldLibraryNames = new TreeSet<>( new QNameComparator() );
		SortedSet<QName> newLibraryNames = new TreeSet<>( new QNameComparator() );
		List<ChangeSetItem> pendingChangeSets = new ArrayList<>();
		Iterator<QName> iterator;
		
		oldLibraryNames.addAll( oldLibraries.keySet() );
		newLibraryNames.addAll( newLibraries.keySet() );
		
		// Look for changes in the project values
		if (valueChanged( oldProject.getName(), newProject.getName() )) {
			changeItems.add( new ProjectChangeItem( changeSet, ProjectChangeType.NAME_CHANGED,
					oldProject.getName(), newProject.getName() ) );
		}
		if (valueChanged( oldProject.getProjectId(), newProject.getProjectId() )) {
			changeItems.add( new ProjectChangeItem( changeSet, ProjectChangeType.NAMESPACE_CHANGED,
					oldProject.getProjectId(), newProject.getProjectId() ) );
		}
		if (valueChanged( oldProject.getDescription(), newProject.getDescription() )) {
			changeItems.add( new ProjectChangeItem( changeSet, ProjectChangeType.DESCRIPTION_CHANGED,
					oldProject.getDescription(), newProject.getDescription() ) );
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
						ProjectChangeType.LIBRARY_CHANGED, oldLibrary, newLibrary ) );
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
							ProjectChangeType.LIBRARY_VERSION_CHANGED, oldLibrary, newLibrary ) );
					newLibraryNames.remove( closestVersionMatch );
					iterator.remove();
				}
			}
		}
		
		// Any new names left over represent libraries that were added
		for (QName newName : newLibraryNames) {
			changeItems.add( new ProjectChangeItem( changeSet,
					ProjectChangeType.LIBRARY_ADDED, newLibraries.get( newName ) ) );
		}
		
		// Any old names left over represent libraries that were removed
		for (QName oldName : oldLibraryNames) {
			changeItems.add( new ProjectChangeItem( changeSet,
					ProjectChangeType.LIBRARY_DELETED, oldLibraries.get( oldName ) ) );
		}
		
		// Process all of the pending change sets that were identified during library
		// comparisons.
		for (ChangeSetItem item : pendingChangeSets) {
			LibraryChangeSet libraryChangeSet =
					new LibraryComparator( getCompareOptions(), getNamespaceMappings() )
							.compareLibraries( item.oldVersion, item.newVersion );
			
			if (!libraryChangeSet.getChangeItems().isEmpty()) {
				changeItems.add( new ProjectChangeItem( changeSet, item.changeType, libraryChangeSet ) );
			}
		}
		
		return changeSet;
	}
	
	/**
	 * Constructs a map of QNames for each user-defined library in the project
	 * using the namespace and library name as the key.
	 * 
	 * @param project  the project from which to obtain the set of user-defined libraries
	 * @return Map<QName,TLLibrary>
	 */
	private Map<QName,TLLibrary> buildLibraryMap(Project project) {
		Map<QName,TLLibrary> libraryMap = new HashMap<>();
		
		for (ProjectItem item : project.getProjectItems()) {
			if (item.getContent() instanceof TLLibrary) {
				TLLibrary library = (TLLibrary) item.getContent();
				
				libraryMap.put( getLibraryName( library ), library );
			}
		}
		return libraryMap;
	}
	
	/**
	 * Encapsulates a change set that has not yet been processed.
	 */
	private static class ChangeSetItem {
		
		public ProjectChangeType changeType;
		public TLLibrary oldVersion;
		public TLLibrary newVersion;
		
		/**
		 * Full constructor.
		 * 
		 * @param changeType  the type of change that occurred
		 * @param oldVersion  the old version of the library
		 * @param newVersion  the new version of the library
		 */
		public ChangeSetItem(ProjectChangeType changeType, TLLibrary oldVersion, TLLibrary newVersion) {
			this.changeType = changeType;
			this.oldVersion = oldVersion;
			this.newVersion = newVersion;
		}
		
	}
	
}

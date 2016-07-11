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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.diff.LibraryChangeSet;
import org.opentravel.schemacompiler.diff.ProjectChangeItem;
import org.opentravel.schemacompiler.diff.ProjectChangeSet;
import org.opentravel.schemacompiler.diff.ProjectChangeType;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.version.OTA2VersionComparator;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Performs a comparison of two OTM projects.
 */
public class ProjectComparator extends BaseComparator {
	
	private static VersionSchemeFactory vsFactory = VersionSchemeFactory.getInstance();
	
	/**
	 * Compares two versions of the same OTM project.
	 * 
	 * @param oldProject  the old project version
	 * @param newProject  the new project version
	 * @return ProjectChangeSet
	 */
	public ProjectChangeSet compareProjects(Project oldProject, Project newProject) {
		ProjectChangeSet changeSet = new ProjectChangeSet( oldProject, newProject );
		List<ProjectChangeItem> changeItems = changeSet.getProjectChangeItems();
		Map<QName,TLLibrary> oldLibraries = buildLibraryMap( oldProject );
		Map<QName,TLLibrary> newLibraries = buildLibraryMap( newProject );
		SortedSet<QName> oldLibraryNames = new TreeSet<QName>( new QNameComparator() );
		SortedSet<QName> newLibraryNames = new TreeSet<QName>( new QNameComparator() );
		List<ChangeSetItem> pendingChangeSets = new ArrayList<>();
		Iterator<QName> iterator;
		
		oldLibraryNames.addAll( oldLibraries.keySet() );
		newLibraryNames.addAll( newLibraries.keySet() );
		
		// Look for changes in the project values
		if (valueChanged( oldProject.getName(), newProject.getName() )) {
			changeItems.add( new ProjectChangeItem( ProjectChangeType.NAME_CHANGED,
					oldProject.getName(), newProject.getName() ) );
		}
		if (valueChanged( oldProject.getProjectId(), newProject.getProjectId() )) {
			changeItems.add( new ProjectChangeItem( ProjectChangeType.NAMESPACE_CHANGED,
					oldProject.getProjectId(), newProject.getProjectId() ) );
		}
		if (valueChanged( oldProject.getDescription(), newProject.getDescription() )) {
			changeItems.add( new ProjectChangeItem( ProjectChangeType.DESCRIPTION_CHANGED,
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
			List<QName> newVersionMatches = findMatchingVersions( libraryName, newLibraryNames, versionScheme );
			
			if (!newVersionMatches.isEmpty()) {
				QName closestVersionMatch = findClosestVersion( libraryName, newVersionMatches, versionScheme );
				
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
			changeItems.add( new ProjectChangeItem(
					ProjectChangeType.LIBRARY_ADDED, newLibraries.get( newName ) ) );
		}
		
		// Any old names left over represent libraries that were removed
		for (QName oldName : oldLibraryNames) {
			changeItems.add( new ProjectChangeItem(
					ProjectChangeType.LIBRARY_DELETED, oldLibraries.get( oldName ) ) );
		}
		
		// Process all of the pending change sets that were identified during library
		// comparisons.
		for (ChangeSetItem item : pendingChangeSets) {
			LibraryChangeSet libraryChangeSet =
					new LibraryComparator( getNamespaceMappings() )
							.compareLibraries( item.oldVersion, item.newVersion );
			
			if (!libraryChangeSet.getLibraryChangeItems().isEmpty()) {
				changeItems.add( new ProjectChangeItem( item.changeType, libraryChangeSet ) );
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
	 * Identifies all of the name versions in the given library that match the specified
	 * name.
	 * 
	 * @param name  the name to which all resulting versions should be matched
	 * @param nameSet  the set of all names from which to extract matching versions
	 * @param versionScheme  the versioning scheme to use when comparing namespace URI's
	 * @return List<QName>
	 */
	private List<QName> findMatchingVersions(QName name, Set<QName> nameSet, String versionScheme) {
		List<QName> matchingNames = new ArrayList<>();
		
		try {
			if ((name != null) && (name.getLocalPart() != null) && (name.getNamespaceURI() != null)) {
				VersionScheme vScheme = vsFactory.getVersionScheme( versionScheme );
				String targetBaseNS = vScheme.getBaseNamespace( name.getNamespaceURI() );
				
				for (QName testName : nameSet) {
					if (name.getLocalPart().equals( testName.getLocalPart() )) {
						String testBaseNS = vScheme.getBaseNamespace( testName.getNamespaceURI() );
						
						if (targetBaseNS.equals( testBaseNS )) {
							matchingNames.add( testName );
						}
					}
				}
			}
		} catch (VersionSchemeException e) {
			// Ignore and return no matching names
		}
		return matchingNames;
	}
	
	/**
	 * Returns the QName version from the given list that most closely matches the one provided.
	 * 
	 * @param name  the name to which the closest match should be returned
	 * @param matchingVersions  the list of matching versions from which the closest match should be identified
	 * @param versionScheme  the versioning scheme to use when comparing namespace URI's
	 * @return QName
	 */
	private QName findClosestVersion(QName name, List<QName> matchingVersions, String versionScheme) {
		QName closestMatch = null;
		
		if (!matchingVersions.isEmpty()) {
			List<VersionedName> nameVersions = new ArrayList<>();
			
			// Sort the list of all versions in ascending order
			for (QName matchingVersion : matchingVersions) {
				nameVersions.add( new VersionedName( matchingVersion, versionScheme ) );
			}
			nameVersions.add( new VersionedName( name, versionScheme ) );
			Collections.sort( nameVersions, new OTA2VersionComparator( true ) );
			
			// Locate the original name in the sorted list
			for (int i = 0; i < nameVersions.size(); i++) {
				VersionedName vName = nameVersions.get( i );
				
				if (vName.name == name) {
					// If the name was the last item in the list, take the previous
					// version; otherwise, always assume that the next later version
					// is the closest match.
					if (i == (nameVersions.size() - 1)) {
						closestMatch = nameVersions.get( i - 1 ).name;
						
					} else {
						closestMatch = nameVersions.get( i + 1 ).name;
					}
					break;
				}
			}
		}
		return closestMatch;
	}
	
	/**
	 * Encapsulates a library change set that has not yet been processed.
	 */
	private static class ChangeSetItem {
		
		public ProjectChangeType changeType;
		public TLLibrary oldVersion;
		public TLLibrary newVersion;
		
		/**
		 * Full constructor.
		 * 
		 * @param changeType  the type of library change that occurred
		 * @param oldVersion  the old version of the library
		 * @param newVersion  the new version of the library
		 */
		public ChangeSetItem(ProjectChangeType changeType, TLLibrary oldVersion, TLLibrary newVersion) {
			this.changeType = changeType;
			this.oldVersion = oldVersion;
			this.newVersion = newVersion;
		}
		
	}
	
	/**
	 * Versioned wrapper for library QNames used for sorting in version number order.
	 */
	private class VersionedName implements Versioned {
		
		public QName name;
		private String baseNS;
		private String versionSchemeId;
		private String versionId;
		
		/**
		 * Constructor that provides the qualified name and version scheme to use for
		 * processing.
		 * 
		 * @param name  the qualified name
		 * @param versionScheme  the version scheme to use for processing and comparisons
		 */
		public VersionedName(QName name, String versionScheme) {
			this.name = name;
			this.versionSchemeId = versionScheme;
			
			try {
				VersionScheme vScheme = vsFactory.getVersionScheme( versionScheme );
				
				this.baseNS = vScheme.getBaseNamespace( name.getNamespaceURI() );
				this.versionId = vScheme.getVersionIdentifier( name.getNamespaceURI() );
				
			} catch (VersionSchemeException e) {
				// Ignore and assign default values
				this.baseNS = name.getNamespaceURI();
				this.versionId = "0.0.0";
			}
		}

		/**
		 * @see org.opentravel.schemacompiler.version.Versioned#getNamespace()
		 */
		@Override
		public String getNamespace() {
			return name.getNamespaceURI();
		}

		/**
		 * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
		 */
		@Override
		public String getLocalName() {
			return name.getLocalPart();
		}

		/**
		 * @see org.opentravel.schemacompiler.version.Versioned#getVersion()
		 */
		@Override
		public String getVersion() {
			return versionId;
		}

		/**
		 * @see org.opentravel.schemacompiler.version.Versioned#getVersionScheme()
		 */
		@Override
		public String getVersionScheme() {
			return versionSchemeId;
		}

		/**
		 * @see org.opentravel.schemacompiler.version.Versioned#getBaseNamespace()
		 */
		@Override
		public String getBaseNamespace() {
			return baseNS;
		}

		/**
		 * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
		 */
		@Override
		public AbstractLibrary getOwningLibrary() {
			return null;
		}

		/**
		 * @see org.opentravel.schemacompiler.model.LibraryElement#cloneElement()
		 */
		@Override
		public LibraryElement cloneElement() {
			return null;
		}

		/**
		 * @see org.opentravel.schemacompiler.model.LibraryElement#cloneElement(org.opentravel.schemacompiler.model.AbstractLibrary)
		 */
		@Override
		public LibraryElement cloneElement(AbstractLibrary namingContext) {
			return null;
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
		 */
		@Override
		public TLModel getOwningModel() {
			return null;
		}

		/**
		 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
		 */
		@Override
		public String getValidationIdentity() {
			return null;
		}

		/**
		 * @see org.opentravel.schemacompiler.version.Versioned#isLaterVersion(org.opentravel.schemacompiler.version.Versioned)
		 */
		@Override
		public boolean isLaterVersion(Versioned otherVersionedItem) {
			return false;
		}
		
	}
	
	/**
	 * Comparator used to sort lists of <code>QName</code> objects.
	 */
	private class QNameComparator implements Comparator<QName> {

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(QName qn1, QName qn2) {
			int result;
			
			if (qn1 == null) {
				result = (qn2 == null) ? 0 : 1;
				
			} else if (qn2 == null) {
				result = -1;
				
			} else {
				String ns1 = qn1.getNamespaceURI();
				String ns2 = qn2.getNamespaceURI();
				
				if (ns1 == null) {
					result = (ns2 == null) ? 0 : 1;
					
				} else if (ns2 == null) {
					result = -1;
					
				} else {
					String local1 = qn1.getLocalPart();
					String local2 = qn2.getLocalPart();
					
					if (local1 == null) {
						result = (local2 == null) ? 0 : 1;
						
					} else {
						result = local1.compareTo( local2 );
					}
				}
			}
			return result;
		}
		
	}
	
}

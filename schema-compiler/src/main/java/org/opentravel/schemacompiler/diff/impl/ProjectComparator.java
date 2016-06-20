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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.diff.LibraryChangeSet;
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
		
		// Identify new libraries that were added
		for (QName newName : newLibraryNames) {
			if (!oldLibraries.containsKey( newName )) {
				changeItems.add( new ProjectChangeItem(
						ProjectChangeType.LIBRARY_ADDED, newLibraries.get( newName ) ) );
			}
		}
		
		// Identify old libraries that were deleted
		for (QName oldName : oldLibraryNames) {
			if (!newLibraries.containsKey( oldName )) {
				changeItems.add( new ProjectChangeItem(
						ProjectChangeType.LIBRARY_DELETED, oldLibraries.get( oldName ) ) );
			}
		}
		
		// Identify libraries that were modified
		for (QName libraryName : oldLibraryNames) {
			if (newLibraries.containsKey( libraryName )) {
				TLLibrary oldLibrary = oldLibraries.get( libraryName );
				TLLibrary newLibrary = newLibraries.get( libraryName );
				LibraryChangeSet libraryChangeSet =
						new LibraryComparator( getNamespaceMappings() )
								.compareLibraries( oldLibrary, newLibrary );
				
				if (!libraryChangeSet.getLibraryChangeItems().isEmpty()) {
					changeItems.add( new ProjectChangeItem( libraryChangeSet ) );
				}
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

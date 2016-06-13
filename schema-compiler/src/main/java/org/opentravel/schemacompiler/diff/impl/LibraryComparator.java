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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opentravel.schemacompiler.diff.LibraryChangeItem;
import org.opentravel.schemacompiler.diff.LibraryChangeSet;
import org.opentravel.schemacompiler.diff.LibraryChangeType;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOperation;

/**
 * Performs a comparison of two OTM libraries.
 */
public class LibraryComparator extends BaseComparator {
	
	/**
	 * Compares two versions of the same OTM library.
	 * 
	 * @param oldLibrary  the old library version
	 * @param newLibrary  the new library version
	 * @return LibraryChangeSet
	 */
	public LibraryChangeSet compareLibraries(TLLibrary oldLibrary, TLLibrary newLibrary) {
		String oldStatus = (oldLibrary.getStatus() == null) ? null : oldLibrary.getStatus().toString();
		String newStatus = (newLibrary.getStatus() == null) ? null : newLibrary.getStatus().toString();
		Map<String,NamedEntity> oldEntities = buildEntityMap( oldLibrary );
		Map<String,NamedEntity> newEntities = buildEntityMap( newLibrary );
		SortedSet<String> oldEntityNames = new TreeSet<String>( oldEntities.keySet() );
		SortedSet<String> newEntityNames = new TreeSet<String>( newEntities.keySet() );
		LibraryChangeSet changeSet = new LibraryChangeSet( oldLibrary, newLibrary );
		List<LibraryChangeItem> changeItems = changeSet.getLibraryChangeItems();
		
		// Look for changes in the library values
		if (valueChanged( oldLibrary.getName(), newLibrary.getName() )) {
			changeItems.add( new LibraryChangeItem( LibraryChangeType.NAME_CHANGED,
					oldLibrary.getName(), newLibrary.getName() ) );
		}
		if (valueChanged( oldLibrary.getNamespace(), newLibrary.getNamespace() )) {
			changeItems.add( new LibraryChangeItem( LibraryChangeType.NAMESPACE_CHANGED,
					oldLibrary.getNamespace(), newLibrary.getNamespace() ) );
		}
		if (valueChanged( oldLibrary.getPrefix(), newLibrary.getPrefix() )) {
			changeItems.add( new LibraryChangeItem( LibraryChangeType.PREFIX_CHANGED,
					oldLibrary.getPrefix(), newLibrary.getPrefix() ) );
		}
		if (valueChanged( oldLibrary.getVersionScheme(), newLibrary.getVersionScheme() )) {
			changeItems.add( new LibraryChangeItem( LibraryChangeType.VERSION_SCHEME_CHANGED,
					oldLibrary.getVersionScheme(), newLibrary.getVersionScheme() ) );
		}
		if (valueChanged( oldStatus, newStatus )) {
			changeItems.add( new LibraryChangeItem( LibraryChangeType.STATUS_CHANGED,
					oldStatus, newStatus ) );
		}
		if (valueChanged( oldLibrary.getComments(), newLibrary.getComments() )) {
			changeItems.add( new LibraryChangeItem( LibraryChangeType.COMMENTS_CHANGED,
					oldLibrary.getComments(), newLibrary.getComments() ) );
		}
		
		// Identify new entities that were added
		for (String newName : newEntityNames) {
			if (!oldEntities.containsKey( newName )) {
				changeItems.add( new LibraryChangeItem(
						LibraryChangeType.MEMBER_ADDED, newEntities.get( newName ) ) );
			}
		}
		
		// Identify old entities that were deleted
		for (String oldName : oldEntityNames) {
			if (!newEntities.containsKey( oldName )) {
				changeItems.add( new LibraryChangeItem(
						LibraryChangeType.MEMBER_DELETED, oldEntities.get( oldName ) ) );
			}
		}
		
		// Identify entities that were modified
		for (String entityName : oldEntityNames) {
			if (newEntities.containsKey( entityName )) {
				NamedEntity oldEntity = oldEntities.get( entityName );
				NamedEntity newEntity = newEntities.get( entityName );
				
				changeItems.add( new LibraryChangeItem(
						new EntityComparator().compareEntities(
								new EntityComparisonFacade( oldEntity ),
								new EntityComparisonFacade( newEntity ) ) ) );
			}
		}
		return changeSet;
	}
	
	/**
	 * Constructs a map of all named entities in the library that associates the
	 * entitys' local names with the entities themselves.
	 * 
	 * @param library  the library for which to create an entity map
	 * @return Map<String,NamedEntity>
	 */
	private Map<String,NamedEntity> buildEntityMap(TLLibrary library) {
		Map<String,NamedEntity> entityMap = new HashMap<>();
		
		for (NamedEntity entity : library.getNamedMembers()) {
			entityMap.put( entity.getLocalName(), entity );
		}
		if (library.getService() != null) {
			for (TLOperation op : library.getService().getOperations()) {
				entityMap.put( op.getLocalName(), op );
			}
		}
		return entityMap;
	}
	
}

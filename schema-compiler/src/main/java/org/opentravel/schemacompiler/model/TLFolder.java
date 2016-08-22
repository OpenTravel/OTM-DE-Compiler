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
package org.opentravel.schemacompiler.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Non-functional container within an OTM library used to organize its member
 * entities.  Beyond this purpose, folders have no effect on artifacts generated
 * by the compiler.
 */
public class TLFolder implements TLFolderOwner {
	
	private static final Comparator<TLFolder> folderComparator = new FolderComparator();
	private static final Comparator<LibraryMember> itemComparator = new FolderItemComparator();
	
	private String name;
	private TLFolder parentFolder;
	private TLLibrary owningLibrary;
	private List<TLFolder> subFolders = new ArrayList<>();
	private List<LibraryMember> entities = new ArrayList<>();
	
	/**
	 * Constructs a root folder for the given library.  The root folder of a library is not
	 * intended to be displayed to application users, and it is not allowed to directly contain
	 * any member entities.
	 * 
	 * @param owningLibrary  the owning library for which to construct a new root folder
	 */
	protected TLFolder(TLLibrary owningLibrary) {
		if (owningLibrary == null) {
			throw new IllegalArgumentException("The owning library of a folder cannot be null.");
		}
		this.owningLibrary = owningLibrary;
	}
	
	/**
	 * Constructor that specifies the name and owning library of the folder.
	 * 
	 * @param name  the name of the new folder
	 * @param owningLibrary  the owning library for the new folder
	 */
	public TLFolder(String name, TLLibrary owningLibrary) {
		this( owningLibrary );
		setName( name );
	}
	
	/**
	 * Returns the name of the folder.
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Assigns the name of the folder.
	 *
	 * @param name  the field value to assign
	 */
	public void setName(String name) {
		// TODO: Check for duplicate folder when setting name
		// TODO: Sort sub-folders of parent when changing folder name
		// TODO: Broadcast model event
		if ((name == null) || (name.length() == 0)) {
			throw new IllegalArgumentException("The folder name is a required value");
		}
		this.name = name;
	}
	
	/**
	 * Returns the parent folder to which this folder belongs (null for root
	 * library folders).
	 *
	 * @return TLFolder
	 */
	public TLFolder getParentFolder() {
		return parentFolder;
	}

	/**
	 * Returns the owning library for this folder.
	 *
	 * @return TLLibrary
	 */
	public TLLibrary getOwningLibrary() {
		return owningLibrary;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLFolderOwner#getFolders()
	 */
	@Override
	public List<TLFolder> getFolders() {
		List<TLFolder> sortedSubFolders = new ArrayList<>( subFolders );
		
		Collections.sort( sortedSubFolders, folderComparator );
		return Collections.unmodifiableList( subFolders );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLFolderOwner#addFolder(org.opentravel.schemacompiler.model.TLFolder)
	 */
	@Override
	public void addFolder(TLFolder folder) {
		// TODO: Check for duplicate folder when adding sub-folder
		// TODO: Sort folders when adding new sub-folder
		// TODO: Broadcast model event
		if (folder == null) {
			throw new IllegalArgumentException("The sub-folder cannot be null.");
		}
		if (folder.owningLibrary != this.owningLibrary) {
			throw new IllegalArgumentException(
					"Sub-folders must belong to the same library as their parent folder.");
		}
		checkCircularFolders( folder );
		
		folder.parentFolder = this;
		subFolders.add( folder );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLFolderOwner#removeFolder(org.opentravel.schemacompiler.model.TLFolder)
	 */
	@Override
	public void removeFolder(TLFolder folder) {
		// TODO: Broadcast model event
		if (subFolders.contains( folder )) {
			subFolders.remove( folder );
			folder.parentFolder = null;
		}
	}
	
	/**
	 * Returns the list of library entities for this folder.  The list of entities
	 * that is returned is unmodifiable and sorted in alphabetical order by entity
	 * local name.
	 *
	 * @return List<LibraryMember>
	 */
	public List<LibraryMember> getEntities() {
		// TODO: Sort entities when adding new items
		List<LibraryMember> sortedItems = new ArrayList<>( entities );
		
		Collections.sort( sortedItems, itemComparator );
		return Collections.unmodifiableList( sortedItems );
	}
	
	/**
	 * Adds the given entity to the list for this folder.
	 * 
	 * @param entity  the library entity to add
	 * @throws IllegalArgumentException  thrown if the entity is already assigned to a folder
	 *									 within the owning library or is not a member of the same
	 *									 owning library
	 */
	public void addEntity(LibraryMember entity) {
		// TODO: Broadcast model event
		if (entity == null) {
			throw new IllegalArgumentException("The member entity cannot be null.");
		}
		if (entity.getOwningLibrary() != this.owningLibrary) {
			throw new IllegalArgumentException(
					"Entities must belong to the same library as their parent folder.");
		}
		if (isExistingFolderedEntity( subFolders, entity )) {
			throw new IllegalArgumentException(
					"The entity is already assigned to the folder structure for its owning library.");
		}
		entities.add( entity );
	}
	
	/**
	 * Removes the given entity from the list of member items for this folder.  If the entity
	 * is not currently a direct member of this folder, this method will take no action.
	 * 
	 * @param entity  the library entity to remove
	 */
	public void removeEntity(LibraryMember entity) {
		// TODO: Broadcast model event
		if (entities.contains( entity )) {
			entities.remove( entity );
		}
	}
	
	/**
	 * Checks the given folder to verify that its addition to the list of sub-folders
	 * will not create any circular references in the owning library's folder structure.
	 * 
	 * @param checkFolder  the folder to check for circular references
	 * @throws IllegalArgumentException  thrown if the addition of the folder would create
	 *									 a circular reference
	 */
	private void checkCircularFolders(TLFolder checkFolder) {
		TLFolder parent = this.parentFolder;
		
		while (parent != null) {
			if (parent == checkFolder) {
				throw new IllegalArgumentException(
						"The given sub-folder is already a parent in the existing folder structure.");
			}
			parent = parent.parentFolder;
		}
		
		for (TLFolder sf : subFolders) {
			if (isChildFolder( sf, checkFolder )) {
				throw new IllegalArgumentException(
						"The given sub-folder is already a member of the existing folder structure.");
			}
		}
	}
	
	/**
	 * Recursively checks to determine whether the given check-folder is already a child
	 * in the existing folder structure.
	 * 
	 * @param subFolder  the sub-folder being navigated
	 * @param checkFolder  the folder to check for existence in the existing sub-folder structure
	 * @return boolean
	 */
	private boolean isChildFolder(TLFolder subFolder, TLFolder checkFolder) {
		boolean result = false;
		
		if (subFolder == checkFolder) {
			result = true;
			
		} else {
			for (TLFolder sf : subFolder.subFolders) {
				result = isChildFolder( sf, checkFolder );
			}
		}
		return result;
	}
	
	/**
	 * Returns true if the given entity is already a member of one of the given folders.
	 * 
	 * @param folders  the list of folders to check for the existence of the given entity
	 * @param entity  the entity being searched for
	 * @return boolean
	 */
	private boolean isExistingFolderedEntity(List<TLFolder> folders, LibraryMember entity) {
		boolean result = false;
		
		for (TLFolder folder : folders) {
			if (folder.getEntities().contains( entity )) {
				result = true;
				break;
				
			} else {
				result = isExistingFolderedEntity( folder.subFolders, entity );
			}
		}
		return result;
	}
	
	/**
	 * Comparator used to sort lists of folders in ascending order.
	 */
	protected static class FolderComparator implements Comparator<TLFolder> {

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(TLFolder folder1, TLFolder folder2) {
			String fName1 = (folder1 == null) ? null : folder1.name;
			String fName2 = (folder2 == null) ? null : folder2.name;
			int result;
			
			if (fName1 == null) {
				result = (fName2 == null) ? 0 : 1;
			} else {
				result = (fName2 == null) ? -1 : fName1.compareTo( fName2 );
			}
			return result;
		}
		
	}
	
	/**
	 * Comparator used to sort lists of folders in ascending order.
	 */
	private static class FolderItemComparator implements Comparator<LibraryMember> {

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(LibraryMember entity1, LibraryMember entity2) {
			String eName1 = (entity1 == null) ? null : entity1.getLocalName();
			String eName2 = (entity2 == null) ? null : entity2.getLocalName();
			int result;
			
			if (eName1 == null) {
				result = (eName2 == null) ? 0 : 1;
			} else {
				result = (eName2 == null) ? -1 : eName1.compareTo( eName2 );
			}
			return result;
		}
		
	}
	
}

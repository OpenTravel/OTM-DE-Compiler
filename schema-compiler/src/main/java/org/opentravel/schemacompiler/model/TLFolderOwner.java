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

import java.util.List;

/**
 * Interface to be implemented by components that can own and manage
 * <code>TLFolder</code> structures.
 */
public interface TLFolderOwner {
	
	/**
	 * Returns the list of sub-folders for this folder owner.  The list of folders
	 * that is returned is unmodifiable and sorted in alphabetical order by folder
	 * name.
	 *
	 * @return List<TLFolder>
	 */
	public List<TLFolder> getFolders();
	
	/**
	 * Adds a new sub-folder to the current list for this owner.
	 * 
	 * @param folder  the sub-folder to be added
	 * @throws IllegalArgumentException  thrown if the addition of the folder would create
	 *									 a circular reference or is not a member of the same
	 *									 owning library
	 */
	public void addFolder(TLFolder folder);
	
	/**
	 * Removes the given sub-folder from the list for this owner.  If the folder is not
	 * currently a direct sub-folder of this folder owner, this method will take no action.
	 * 
	 * @param folder  the existing sub-folder to remove
	 */
	public void removeFolder(TLFolder folder);
	
}

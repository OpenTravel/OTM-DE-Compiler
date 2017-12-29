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

package org.opentravel.schemacompiler.diff;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLResource;

/**
 * Describes a single change identified during comparison of two OTM libraries.
 */
public class LibraryChangeItem extends ChangeItem<LibraryChangeType> {
	
	private NamedEntity addedEntity;
	private NamedEntity deletedEntity;
	private EntityChangeSet modifiedEntity;
	private TLResource addedResource;
	private TLResource deletedResource;
	private ResourceChangeSet modifiedResource;
	
	/**
	 * Constructor used when an entity was added or deleted from its owning library.
	 * 
	 * @param changeType  the type of library change
	 * @param affectedEntity  the entity that was added or removed
	 */
	public LibraryChangeItem(LibraryChangeType changeType, NamedEntity affectedEntity) {
		this.changeType = changeType;
		
		switch (changeType) {
			case MEMBER_ADDED:
				this.addedEntity = affectedEntity;
				break;
			case MEMBER_DELETED:
				this.deletedEntity = affectedEntity;
				break;
			default:
				throw new IllegalArgumentException("Illegal change type for entity addition or deletion: " + changeType);
		}
	}
	
	/**
	 * Constructor used when a library entity was modified.
	 * 
	 * @param modifiedEntity  the change set for a modified entity
	 */
	public LibraryChangeItem(EntityChangeSet modifiedEntity) {
		this.changeType = LibraryChangeType.MEMBER_CHANGED;
		this.modifiedEntity = modifiedEntity;
	}
	
	/**
	 * Constructor used when a resource was added or deleted from its owning library.
	 * 
	 * @param changeType  the type of library change
	 * @param affectedResource  the resource that was added or removed
	 */
	public LibraryChangeItem(LibraryChangeType changeType, TLResource affectedResource) {
		this.changeType = changeType;
		
		switch (changeType) {
			case RESOURCE_ADDED:
				this.addedResource = affectedResource;
				break;
			case RESOURCE_DELETED:
				this.deletedResource = affectedResource;
				break;
			default:
				throw new IllegalArgumentException("Illegal change type for entity addition or deletion: " + changeType);
		}
	}
	
	/**
	 * Constructor used when a library resource was modified.
	 * 
	 * @param modifiedResource  the change set for a modified resource
	 */
	public LibraryChangeItem(ResourceChangeSet modifiedResource) {
		this.changeType = LibraryChangeType.RESOURCE_CHANGED;
		this.modifiedResource = modifiedResource;
	}
	
	/**
	 * Constructor used when a library value was changed.
	 * 
	 * @param changeType  the type of library change
	 * @param oldValue  the affected value from the old version
	 * @param newValue  the affected value from the new version
	 */
	public LibraryChangeItem(LibraryChangeType changeType, String oldValue, String newValue) {
		this.changeType = changeType;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * Returns the entity that was added.
	 *
	 * @return NamedEntity
	 */
	public NamedEntity getAddedEntity() {
		return addedEntity;
	}

	/**
	 * Returns the entity that was deleted.
	 *
	 * @return NamedEntity
	 */
	public NamedEntity getDeletedEntity() {
		return deletedEntity;
	}

	/**
	 * Returns the change set for a modified entity.
	 *
	 * @return EntityChangeSet
	 */
	public EntityChangeSet getModifiedEntity() {
		return modifiedEntity;
	}

	/**
	 * Returns the resource that was added.
	 *
	 * @return TLResource
	 */
	public TLResource getAddedResource() {
		return addedResource;
	}

	/**
	 * Returns the resource that was deleted.
	 *
	 * @return TLResource
	 */
	public TLResource getDeletedResource() {
		return deletedResource;
	}

	/**
	 * Returns the change set for a modified resource.
	 *
	 * @return ResourceChangeSet
	 */
	public ResourceChangeSet getModifiedResource() {
		return modifiedResource;
	}

}

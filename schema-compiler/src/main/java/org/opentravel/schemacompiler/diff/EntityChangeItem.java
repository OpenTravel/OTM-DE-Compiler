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

import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLMemberFieldOwner;

/**
 * Describes a single change identified during comparison of two OTM entities.
 */
public class EntityChangeItem extends ChangeItem<EntityChangeType> {
	
	private EntityChangeSet changeSet;
	private TLMemberField<TLMemberFieldOwner> addedField;
	private TLMemberField<TLMemberFieldOwner> deletedField;
	private FieldChangeSet modifiedField;
	
	/**
	 * Constructor used when a field was added or deleted from its owning entity.
	 * 
	 * @param changeSet  the change set to which this item belongs
	 * @param changeType  the type of entity change
	 * @param affectedField  the field that was added or removed
	 */
	public EntityChangeItem(EntityChangeSet changeSet, EntityChangeType changeType, TLMemberField<TLMemberFieldOwner> affectedField) {
		this.changeSet = changeSet;
		this.changeType = changeType;
		
		switch (changeType) {
			case MEMBER_FIELD_ADDED:
				this.addedField = affectedField;
				break;
			case MEMBER_FIELD_DELETED:
				this.deletedField = affectedField;
				break;
			default:
				throw new IllegalArgumentException("Illegal change type for field addition or deletion: " + changeType);
		}
	}
	
	/**
	 * Constructor used when an entity field was modified.
	 * 
	 * @param changeSet  the change set to which this item belongs
	 * @param modifiedField  the change set for a modified field
	 */
	public EntityChangeItem(EntityChangeSet changeSet, FieldChangeSet modifiedField) {
		this.changeSet = changeSet;
		this.changeType = EntityChangeType.MEMBER_FIELD_CHANGED;
		this.modifiedField = modifiedField;
	}
	
	/**
	 * Constructor used when an entity value was changed.
	 * 
	 * @param changeSet  the change set to which this item belongs
	 * @param changeType  the type of entity change
	 * @param oldValue  the affected value from the old version
	 * @param newValue  the affected value from the new version
	 */
	public EntityChangeItem(EntityChangeSet changeSet, EntityChangeType changeType, String oldValue, String newValue) {
		this.changeSet = changeSet;
		this.changeType = changeType;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * Returns the change set to which this item belongs.
	 *
	 * @return EntityChangeSet
	 */
	public EntityChangeSet getChangeSet() {
		return changeSet;
	}

	/**
	 * Returns the field that was added.
	 *
	 * @return TLMemberField<TLMemberFieldOwner>
	 */
	public TLMemberField<TLMemberFieldOwner> getAddedField() {
		return addedField;
	}

	/**
	 * Returns the field that was deleted.
	 *
	 * @return TLMemberField<TLMemberFieldOwner>
	 */
	public TLMemberField<TLMemberFieldOwner> getDeletedField() {
		return deletedField;
	}

	/**
	 * Returns the change set for a modified field.
	 *
	 * @return FieldChangeSet
	 */
	public FieldChangeSet getModifiedField() {
		return modifiedField;
	}

}

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

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLMemberField;

/**
 * Container for all change items identified during the comparison of two fields.
 */
public class FieldChangeSet {
	
	private TLMemberField<?> oldField;
	private TLMemberField<?> newField;
	private List<FieldChangeItem> fieldChanges = new ArrayList<>();
	
	/**
	 * Constructor that assigns the old and new version of a field that was modified.
	 * 
	 * @param oldField  the old version of the field
	 * @param newField  the new version of the field
	 */
	public FieldChangeSet(TLMemberField<?> oldField, TLMemberField<?> newField) {
		this.oldField = oldField;
		this.newField = newField;
	}
	
	/**
	 * Returns the old version of the field.
	 *
	 * @return TLMemberField<?>
	 */
	public TLMemberField<?> getOldField() {
		return oldField;
	}
	
	/**
	 * Returns the new version of the field.
	 *
	 * @return TLMemberField<?>
	 */
	public TLMemberField<?> getNewField() {
		return newField;
	}
	
	/**
	 * Returns the list of changes between the old and new version of the field.
	 *
	 * @return List<FieldChangeItem>
	 */
	public List<FieldChangeItem> getFieldChanges() {
		return fieldChanges;
	}
	
}

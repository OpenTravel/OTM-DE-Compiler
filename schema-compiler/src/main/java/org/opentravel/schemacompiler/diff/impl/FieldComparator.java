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

import java.util.List;

import org.opentravel.schemacompiler.diff.FieldChangeItem;
import org.opentravel.schemacompiler.diff.FieldChangeSet;
import org.opentravel.schemacompiler.diff.FieldChangeType;

/**
 * Performs a comparison of two OTM member fields.
 */
public class FieldComparator extends BaseComparator {
	
	private DisplayFomatter formatter = new DisplayFomatter();
	
	/**
	 * Compares two versions of the same OTM member field.
	 * 
	 * @param oldField  facade for the old field version
	 * @param newField  facade for the new field version
	 * @return FieldChangeSet
	 */
	public FieldChangeSet compareFields(FieldComparisonFacade oldField, FieldComparisonFacade newField) {
		int oldRepeatCount = (oldField.getRepeatCount() <= 1) ? 0 : oldField.getRepeatCount();
		int newRepeatCount = (newField.getRepeatCount() <= 1) ? 0 : newField.getRepeatCount();
		FieldChangeSet changeSet = new FieldChangeSet( oldField.getField(), newField.getField() );
		List<FieldChangeItem> changeItems = changeSet.getFieldChanges();
		
		if (valueChanged( oldField.getMemberType(), newField.getMemberType() )) {
			changeItems.add( new FieldChangeItem( FieldChangeType.MEMBER_TYPE_CHANGED,
					formatter.getDisplayName( oldField.getMemberType() ),
					formatter.getDisplayName( newField.getMemberType() ) ) );
		}
		if (valueChanged( oldField.getOwningFacet(), newField.getOwningFacet() )) {
			changeItems.add( new FieldChangeItem( FieldChangeType.OWNING_FACET_CHANGED,
					oldField.getOwningFacet(), newField.getOwningFacet() ) );
		}
		if (valueChanged( getEntityName( oldField.getAssignedType() ), getEntityName( newField.getAssignedType() ) )) {
			changeItems.add( new FieldChangeItem( FieldChangeType.TYPE_CHANGED,
					formatter.getDisplayName( oldField.getAssignedType() ),
					formatter.getDisplayName( newField.getAssignedType() ) ) );
		}
		if (oldRepeatCount != newRepeatCount) {
			changeItems.add( new FieldChangeItem( FieldChangeType.CARDINALITY_CHANGE,
					"" + oldRepeatCount, "" + newRepeatCount ) );
		}
		if (oldField.isMandatory() != newField.isMandatory()) {
			FieldChangeType changeType = newField.isMandatory() ?
					FieldChangeType.CHANGED_TO_MANDATORY : FieldChangeType.CHANGED_TO_OPTIONAL;
			changeItems.add( new FieldChangeItem( changeType, "" + oldField.isMandatory(), "" + newField.isMandatory() ) );
		}
		if (oldField.isReference() != newField.isReference()) {
			FieldChangeType changeType = newField.isReference() ?
					FieldChangeType.CHANGED_TO_REFERENCE : FieldChangeType.CHANGED_TO_NON_REFERENCE;
			changeItems.add( new FieldChangeItem( changeType, "" + oldField.isReference(), "" + newField.isReference() ) );
		}
		if (valueChanged( oldField.getDocumentation(), newField.getDocumentation() )) {
			changeItems.add( new FieldChangeItem( FieldChangeType.DOCUMENTATION_CHANGED, null, null ) );
		}
		
		compareListContents( oldField.getEquivalents(), newField.getEquivalents(),
				FieldChangeType.EQUIVALENT_ADDED, FieldChangeType.EQUIVALENT_DELETED, changeItems );
		compareListContents( oldField.getExamples(), newField.getExamples(),
				FieldChangeType.EXAMPLE_ADDED, FieldChangeType.EXAMPLE_DELETED, changeItems );
		
		return changeSet;
	}
	
	/**
	 * Compares the old and new versions of the value list and creates field change items
	 * to represent the values that were added and/or removed in the new version of the list.
	 * 
	 * @param oldValues  the old version's list of values
	 * @param newValues  the new version's list of values
	 * @param addedChangeType  the field change type to use for added values
	 * @param deletedChangeType  the field change type to use for deleted values
	 * @param changeItems  the list of change items for the field
	 */
	private void compareListContents(List<String> oldValues, List<String> newValues,
			FieldChangeType addedChangeType, FieldChangeType deletedChangeType, List<FieldChangeItem> changeItems) {
		for (String newValue : newValues) {
			if (!oldValues.contains( newValue )) {
				changeItems.add( new FieldChangeItem( addedChangeType, null, newValue ) );
			}
		}
		for (String oldValue : oldValues) {
			if (!newValues.contains( oldValue )) {
				changeItems.add( new FieldChangeItem( deletedChangeType, oldValue, null ) );
			}
		}
	}
	
}

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

import org.opentravel.schemacompiler.diff.FieldChangeItem;
import org.opentravel.schemacompiler.diff.FieldChangeSet;
import org.opentravel.schemacompiler.diff.FieldChangeType;
import org.opentravel.schemacompiler.diff.ModelCompareOptions;
import org.opentravel.schemacompiler.model.NamedEntity;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Performs a comparison of two OTM member fields.
 */
public class FieldComparator extends BaseComparator {

    private DisplayFormatter formatter = new DisplayFormatter();

    /**
     * Constructor that initializes the comparison options and namespace mappings for the comparator.
     * 
     * @param compareOptions the model comparison options to apply during processing
     * @param namespaceMappings the initial namespace mappings
     */
    public FieldComparator(ModelCompareOptions compareOptions, Map<String,String> namespaceMappings) {
        super( compareOptions, namespaceMappings );
    }

    /**
     * Compares two versions of the same OTM member field.
     * 
     * @param oldField facade for the old field version
     * @param newField facade for the new field version
     * @return FieldChangeSet
     */
    public FieldChangeSet compareFields(FieldComparisonFacade oldField, FieldComparisonFacade newField) {
        int oldRepeatCount = (oldField.getRepeatCount() <= 1) ? 0 : oldField.getRepeatCount();
        int newRepeatCount = (newField.getRepeatCount() <= 1) ? 0 : newField.getRepeatCount();
        FieldChangeSet changeSet = new FieldChangeSet( oldField.getField(), newField.getField() );
        List<FieldChangeItem> changeItems = changeSet.getChangeItems();

        if (valueChanged( oldField.getMemberType(), newField.getMemberType() )) {
            changeItems.add( new FieldChangeItem( changeSet, FieldChangeType.MEMBER_TYPE_CHANGED,
                formatter.getEntityTypeDisplayName( oldField.getMemberType() ),
                formatter.getEntityTypeDisplayName( newField.getMemberType() ) ) );
        }
        if (valueChanged( oldField.getOwningFacet(), newField.getOwningFacet() )) {
            changeItems.add( new FieldChangeItem( changeSet, FieldChangeType.OWNING_FACET_CHANGED,
                oldField.getOwningFacet(), newField.getOwningFacet() ) );
        }

        compareAssignedType( oldField, newField, changeSet, changeItems );

        if (oldRepeatCount != newRepeatCount) {
            changeItems.add( new FieldChangeItem( changeSet, FieldChangeType.CARDINALITY_CHANGE, "" + oldRepeatCount,
                "" + newRepeatCount ) );
        }
        if (oldField.isMandatory() != newField.isMandatory()) {
            FieldChangeType changeType =
                newField.isMandatory() ? FieldChangeType.CHANGED_TO_MANDATORY : FieldChangeType.CHANGED_TO_OPTIONAL;
            changeItems.add( new FieldChangeItem( changeSet, changeType, "" + oldField.isMandatory(),
                "" + newField.isMandatory() ) );
        }
        if (oldField.isReference() != newField.isReference()) {
            FieldChangeType changeType = newField.isReference() ? FieldChangeType.CHANGED_TO_REFERENCE
                : FieldChangeType.CHANGED_TO_NON_REFERENCE;
            changeItems.add( new FieldChangeItem( changeSet, changeType, "" + oldField.isReference(),
                "" + newField.isReference() ) );
        }
        if (!getCompareOptions().isSuppressDocumentationChanges()
            && valueChanged( oldField.getDocumentation(), newField.getDocumentation() )) {
            changeItems.add( new FieldChangeItem( changeSet, FieldChangeType.DOCUMENTATION_CHANGED, null, null ) );
        }

        compareListContents( oldField.getEquivalents(), newField.getEquivalents(), FieldChangeType.EQUIVALENT_ADDED,
            FieldChangeType.EQUIVALENT_DELETED, changeSet );
        compareListContents( oldField.getExamples(), newField.getExamples(), FieldChangeType.EXAMPLE_ADDED,
            FieldChangeType.EXAMPLE_DELETED, changeSet );

        return changeSet;
    }

    /**
     * Compares the type assignments of the old and new-version fields.
     * 
     * @param oldField facade for the old field version
     * @param newField facade for the new field version
     * @param changeSet the change set to which any new change items will be assigned
     * @param changeItems the list of change items being constructed
     */
    private void compareAssignedType(FieldComparisonFacade oldField, FieldComparisonFacade newField,
        FieldChangeSet changeSet, List<FieldChangeItem> changeItems) {
        QName oldAssignedTypeName = getEntityName( oldField.getAssignedType() );
        QName newAssignedTypeName = getEntityName( newField.getAssignedType() );

        if (valueChanged( oldAssignedTypeName, newAssignedTypeName )) {
            String versionScheme = getVersionScheme( oldField.getAssignedType() );

            if (versionScheme == null) {
                versionScheme = getVersionScheme( newField.getAssignedType() );
            }

            if (isVersionChange( oldAssignedTypeName, newAssignedTypeName, versionScheme )) {
                if (!getCompareOptions().isSuppressFieldVersionChanges()) {
                    changeItems.add( new FieldChangeItem( changeSet, FieldChangeType.TYPE_VERSION_CHANGED,
                        getVersion( (NamedEntity) oldField.getAssignedType() ),
                        getVersion( (NamedEntity) newField.getAssignedType() ) ) );
                }

            } else {
                changeItems.add( new FieldChangeItem( changeSet, FieldChangeType.TYPE_CHANGED,
                    formatter.getEntityDisplayName( oldField.getAssignedType() ),
                    formatter.getEntityDisplayName( newField.getAssignedType() ) ) );
            }
        }
    }

    /**
     * Compares the old and new versions of the value list and creates field change items to represent the values that
     * were added and/or removed in the new version of the list.
     * 
     * @param oldValues the old version's list of values
     * @param newValues the new version's list of values
     * @param addedChangeType the field change type to use for added values
     * @param deletedChangeType the field change type to use for deleted values
     * @param changeItems the list of change items for the field
     */
    private void compareListContents(List<String> oldValues, List<String> newValues, FieldChangeType addedChangeType,
        FieldChangeType deletedChangeType, FieldChangeSet changeSet) {
        List<FieldChangeItem> changeItems = changeSet.getChangeItems();

        for (String newValue : newValues) {
            if (!oldValues.contains( newValue )) {
                changeItems.add( new FieldChangeItem( changeSet, addedChangeType, null, newValue ) );
            }
        }
        for (String oldValue : oldValues) {
            if (!newValues.contains( oldValue )) {
                changeItems.add( new FieldChangeItem( changeSet, deletedChangeType, oldValue, null ) );
            }
        }
    }

}

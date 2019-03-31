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

import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.diff.EntityChangeItem;
import org.opentravel.schemacompiler.diff.EntityChangeSet;
import org.opentravel.schemacompiler.diff.EntityChangeType;
import org.opentravel.schemacompiler.diff.FieldChangeSet;
import org.opentravel.schemacompiler.diff.ModelCompareOptions;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLMemberFieldOwner;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.util.SchemaCompilerRuntimeException;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.namespace.QName;

/**
 * Performs a comparison of two OTM entities.
 */
public class EntityComparator extends BaseComparator {

    private DisplayFormatter formatter = new DisplayFormatter();
    private VersionScheme versionScheme;

    /**
     * Constructor that initializes the comparison options and namespace mappings for the comparator.
     * 
     * @param compareOptions the model comparison options to apply during processing
     * @param namespaceMappings the initial namespace mappings
     */
    public EntityComparator(ModelCompareOptions compareOptions, Map<String,String> namespaceMappings) {
        super( compareOptions, namespaceMappings );
    }

    /**
     * Compares two versions of the same OTM entity.
     * 
     * @param oldEntity facade for the old entity version
     * @param newEntity facade for the new entity version
     * @return EntityChangeSet
     */
    public EntityChangeSet compareEntities(EntityComparisonFacade oldEntity, EntityComparisonFacade newEntity) {
        EntityChangeSet changeSet = new EntityChangeSet( oldEntity.getEntity(), newEntity.getEntity() );
        List<EntityChangeItem> changeItems = changeSet.getChangeItems();

        this.versionScheme = getVScheme( oldEntity.getOwningLibrary() );

        // Look for changes in the library values
        AbstractLibrary owningLibrary = oldEntity.getOwningLibrary();
        String vScheme = (owningLibrary == null) ? null : owningLibrary.getVersionScheme();
        boolean isMinorVersionCompare = isMinorVersionCompare( oldEntity, newEntity, vScheme );

        if (valueChanged( oldEntity.getEntityType(), newEntity.getEntityType() )) {
            changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.ENTITY_TYPE_CHANGED,
                formatter.getEntityTypeDisplayName( oldEntity.getEntityType() ),
                formatter.getEntityTypeDisplayName( newEntity.getEntityType() ) ) );
        }
        if (valueChanged( oldEntity.getName(), newEntity.getName() )) {
            changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.NAME_CHANGED, oldEntity.getName(),
                newEntity.getName() ) );
        }
        if (!getCompareOptions().isSuppressDocumentationChanges()
            && valueChanged( oldEntity.getDocumentation(), newEntity.getDocumentation() )) {
            changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.DOCUMENTATION_CHANGED, null, null ) );
        }

        compareEntityRelationships( oldEntity, newEntity, changeSet, changeItems, vScheme );

        compareActionFacetProperties( oldEntity, newEntity, changeSet, changeItems, vScheme );

        compareSimpleTypeConstraints( oldEntity, newEntity, changeSet, changeItems );

        compareListContents( oldEntity.getAliasNames(), newEntity.getAliasNames(), EntityChangeType.ALIAS_ADDED,
            EntityChangeType.ALIAS_DELETED, changeSet, isMinorVersionCompare );
        compareListContents( oldEntity.getFacetNames(), newEntity.getFacetNames(), EntityChangeType.FACET_ADDED,
            EntityChangeType.FACET_DELETED, changeSet, isMinorVersionCompare );
        compareListContents( oldEntity.getRoleNames(), newEntity.getRoleNames(), EntityChangeType.ROLE_ADDED,
            EntityChangeType.ROLE_DELETED, changeSet, isMinorVersionCompare );
        compareListContents( oldEntity.getEnumValues(), newEntity.getEnumValues(), EntityChangeType.ENUM_VALUE_ADDED,
            EntityChangeType.ENUM_VALUE_DELETED, changeSet, isMinorVersionCompare );

        compareMemberFields( oldEntity.getMemberFields(), newEntity.getMemberFields(), changeSet,
            isMinorVersionCompare );

        compareListContents( oldEntity.getEquivalents(), newEntity.getEquivalents(), EntityChangeType.EQUIVALENT_ADDED,
            EntityChangeType.EQUIVALENT_DELETED, changeSet, isMinorVersionCompare );
        compareListContents( oldEntity.getExamples(), newEntity.getExamples(), EntityChangeType.EXAMPLE_ADDED,
            EntityChangeType.EXAMPLE_DELETED, changeSet, isMinorVersionCompare );

        return changeSet;
    }

    /**
     * Compares the relationships between the old and new-version entities.
     * 
     * @param oldEntity the old-version entity being compared
     * @param newEntity the new-version entity being compared
     * @param changeSet the change set to which all new change items will be assigned
     * @param changeItems the list of change items being constructed
     * @param vScheme the version scheme of the entities being compared
     */
    private void compareEntityRelationships(EntityComparisonFacade oldEntity, EntityComparisonFacade newEntity,
        EntityChangeSet changeSet, List<EntityChangeItem> changeItems, String vScheme) {
        QName oldParentTypeName = getEntityName( oldEntity.getParentType() );
        QName newParentTypeName = getEntityName( newEntity.getParentType() );
        QName oldExtendsTypeName = getEntityName( oldEntity.getExtendsType() );
        QName newExtendsTypeName = getEntityName( newEntity.getExtendsType() );
        QName oldSimpleCoreTypeName = getEntityName( oldEntity.getSimpleCoreType() );
        QName newSimpleCoreTypeName = getEntityName( newEntity.getSimpleCoreType() );

        if (valueChanged( oldParentTypeName, newParentTypeName )) {
            if (isVersionChange( oldParentTypeName, newParentTypeName, vScheme )) {
                changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.PARENT_TYPE_VERSION_CHANGED,
                    oldEntity.getOwningLibrary().getVersion(), newEntity.getOwningLibrary().getVersion() ) );

            } else {
                changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.PARENT_TYPE_CHANGED,
                    formatter.getEntityDisplayName( oldEntity.getParentType() ),
                    formatter.getEntityDisplayName( newEntity.getParentType() ) ) );
            }
        }
        if (valueChanged( oldExtendsTypeName, newExtendsTypeName )) {
            if (isVersionChange( oldExtendsTypeName, newExtendsTypeName, vScheme )) {
                changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.EXTENSION_VERSION_CHANGED,
                    oldEntity.getOwningLibrary().getVersion(), newEntity.getOwningLibrary().getVersion() ) );

            } else {
                changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.EXTENSION_CHANGED,
                    formatter.getEntityDisplayName( oldEntity.getExtendsType() ),
                    formatter.getEntityDisplayName( newEntity.getExtendsType() ) ) );
            }
        }

        if (valueChanged( oldSimpleCoreTypeName, newSimpleCoreTypeName )) {
            if (isVersionChange( oldSimpleCoreTypeName, newSimpleCoreTypeName, vScheme )) {
                changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.SIMPLE_CORE_TYPE_VERSION_CHANGED,
                    oldEntity.getOwningLibrary().getVersion(), newEntity.getOwningLibrary().getVersion() ) );

            } else {
                changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.SIMPLE_CORE_TYPE_CHANGED,
                    formatter.getEntityDisplayName( oldEntity.getSimpleCoreType() ),
                    formatter.getEntityDisplayName( newEntity.getSimpleCoreType() ) ) );
            }
        }
    }

    /**
     * Compares the entity properties that are specific to resource action facets.
     * 
     * @param oldEntity the old-version entity being compared
     * @param newEntity the new-version entity being compared
     * @param changeSet the change set to which all new change items will be assigned
     * @param changeItems the list of change items being constructed
     * @param vScheme the version scheme of the entities being compared
     */
    private void compareActionFacetProperties(EntityComparisonFacade oldEntity, EntityComparisonFacade newEntity,
        EntityChangeSet changeSet, List<EntityChangeItem> changeItems, String vScheme) {
        QName oldBasePayloadTypeName = getEntityName( oldEntity.getBasePayloadType() );
        QName newBasePayloadTypeName = getEntityName( newEntity.getBasePayloadType() );

        if (valueChanged( oldBasePayloadTypeName, newBasePayloadTypeName )) {
            if (isVersionChange( oldBasePayloadTypeName, newBasePayloadTypeName, vScheme )) {
                changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.BASE_PAYLOAD_VERSION_CHANGED,
                    oldEntity.getOwningLibrary().getVersion(), newEntity.getOwningLibrary().getVersion() ) );

            } else {
                changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.BASE_PAYLOAD_CHANGED,
                    formatter.getEntityDisplayName( oldEntity.getBasePayloadType() ),
                    formatter.getEntityDisplayName( newEntity.getBasePayloadType() ) ) );
            }
        }
        if (oldEntity.getReferenceType() != newEntity.getReferenceType()) {
            changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.REFERENCE_TYPE_CHANGED,
                oldEntity.getReferenceType() + "", newEntity.getReferenceType() + "" ) );
        }
        if (valueChanged( oldEntity.getReferenceFacetName(), newEntity.getReferenceFacetName() )) {
            changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.REFERENCE_FACET_CHANGED,
                oldEntity.getReferenceFacetName(), newEntity.getReferenceFacetName() ) );
        }
        if (oldEntity.getReferenceRepeat() != newEntity.getReferenceRepeat()) {
            changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.REFERENCE_REPEAT_CHANGED,
                oldEntity.getReferenceRepeat() + "", newEntity.getReferenceRepeat() + "" ) );
        }
    }

    /**
     * Compares the entity properties that relate to simple type constraints.
     * 
     * @param oldEntity the old-version entity being compared
     * @param newEntity the new-version entity being compared
     * @param changeSet the change set to which all new change items will be assigned
     * @param changeItems the list of change items being constructed
     */
    private void compareSimpleTypeConstraints(EntityComparisonFacade oldEntity, EntityComparisonFacade newEntity,
        EntityChangeSet changeSet, List<EntityChangeItem> changeItems) {
        if (oldEntity.isSimpleList() != newEntity.isSimpleList()) {
            EntityChangeType changeType = newEntity.isSimpleList() ? EntityChangeType.CHANGED_TO_SIMPLE_LIST
                : EntityChangeType.CHANGED_TO_SIMPLE_NON_LIST;
            changeItems.add( new EntityChangeItem( changeSet, changeType, "" + oldEntity.isSimpleList(),
                "" + newEntity.isSimpleList() ) );
        }
        if (valueChanged( oldEntity.getPatternConstraint(), newEntity.getPatternConstraint() )) {
            changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.PATTERN_CONSTRAINT_CHANGED,
                oldEntity.getPatternConstraint(), newEntity.getPatternConstraint() ) );
        }
        if (oldEntity.getMinLengthConstraint() != newEntity.getMinLengthConstraint()) {
            changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.MIN_LENGTH_CONSTRAINT_CHANGED,
                "" + oldEntity.getMinLengthConstraint(), "" + newEntity.getMinLengthConstraint() ) );
        }
        if (oldEntity.getMaxLengthConstraint() != newEntity.getMaxLengthConstraint()) {
            changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.MAX_LENGTH_CONSTRAINT_CHANGED,
                "" + oldEntity.getMaxLengthConstraint(), "" + newEntity.getMaxLengthConstraint() ) );
        }
        if (oldEntity.getFractionDigitsConstraint() != newEntity.getFractionDigitsConstraint()) {
            changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.FRACTION_DIGITS_CONSTRAINT_CHANGED,
                "" + oldEntity.getFractionDigitsConstraint(), "" + newEntity.getFractionDigitsConstraint() ) );
        }
        if (oldEntity.getTotalDigitsConstraint() != newEntity.getTotalDigitsConstraint()) {
            changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.TOTAL_DIGITS_CONSTRAINT_CHANGED,
                "" + oldEntity.getTotalDigitsConstraint(), "" + newEntity.getTotalDigitsConstraint() ) );
        }
        if (valueChanged( oldEntity.getMinInclusiveConstraint(), newEntity.getMinInclusiveConstraint() )) {
            changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.MIN_INCLUSIVE_CONSTRAINT_CHANGED,
                oldEntity.getMinInclusiveConstraint(), newEntity.getMinInclusiveConstraint() ) );
        }
        if (valueChanged( oldEntity.getMaxInclusiveConstraint(), newEntity.getMaxInclusiveConstraint() )) {
            changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.MAX_INCLUSIVE_CONSTRAINT_CHANGED,
                oldEntity.getMaxInclusiveConstraint(), newEntity.getMaxInclusiveConstraint() ) );
        }
        if (valueChanged( oldEntity.getMinExclusiveConstraint(), newEntity.getMinExclusiveConstraint() )) {
            changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.MIN_EXCLUSIVE_CONSTRAINT_CHANGED,
                oldEntity.getMinExclusiveConstraint(), newEntity.getMinExclusiveConstraint() ) );
        }
        if (valueChanged( oldEntity.getMaxExclusiveConstraint(), newEntity.getMaxExclusiveConstraint() )) {
            changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.MAX_EXCLUSIVE_CONSTRAINT_CHANGED,
                oldEntity.getMaxExclusiveConstraint(), newEntity.getMaxExclusiveConstraint() ) );
        }
    }

    /**
     * Compares the old and new versions of the value list and creates entity change items to represent the values that
     * were added and/or removed in the new version of the list.
     * 
     * @param oldValues the old version's list of values
     * @param newValues the new version's list of values
     * @param addedChangeType the entity change type to use for added values
     * @param deletedChangeType the entity change type to use for deleted values
     * @param changeSet the change set for the entity
     * @param isMinorVersionCompare true if the second entity is a later minor version of the first
     */
    private void compareListContents(List<String> oldValues, List<String> newValues, EntityChangeType addedChangeType,
        EntityChangeType deletedChangeType, EntityChangeSet changeSet, boolean isMinorVersionCompare) {
        List<EntityChangeItem> changeItems = changeSet.getChangeItems();

        for (String newValue : newValues) {
            if (!oldValues.contains( newValue )) {
                changeItems.add( new EntityChangeItem( changeSet, addedChangeType, null, newValue ) );
            }
        }

        if (!isMinorVersionCompare) {
            for (String oldValue : oldValues) {
                if (!newValues.contains( oldValue )) {
                    changeItems.add( new EntityChangeItem( changeSet, deletedChangeType, oldValue, null ) );
                }
            }
        }
    }

    /**
     * Compares the lists of member fields from the old and new versions of the entity. Change items are appended to the
     * list for any fields that are identified as being added, deleted, or modified in the new version.
     * 
     * @param list the list of fields for the old entity version
     * @param list2 the list of fields for the new entity version
     * @param changeSet the change set for the entity
     * @param isMinorVersionCompare true if the second entity is a later minor version of the first
     */
    private void compareMemberFields(List<TLMemberField<TLMemberFieldOwner>> list,
        List<TLMemberField<TLMemberFieldOwner>> list2, EntityChangeSet changeSet, boolean isMinorVersionCompare) {
        List<EntityChangeItem> changeItems = changeSet.getChangeItems();
        ModelCompareOptions options = getCompareOptions();
        Map<String,String> fieldNSMappings =
            options.isSuppressFieldVersionChanges() ? getNamespaceMappings() : new HashMap<>();
        Map<QName,List<TLMemberField<?>>> oldFieldMap = buildFieldMap( list );
        Map<QName,List<TLMemberField<?>>> newFieldMap = buildFieldMap( list2 );
        SortedSet<QName> oldFieldNames = new TreeSet<>( new QNameComparator() );
        SortedSet<QName> newFieldNames = new TreeSet<>( new QNameComparator() );
        List<EntityChangeItem> pendingChangeItems = new ArrayList<>();
        Iterator<QName> iterator;

        oldFieldNames.addAll( oldFieldMap.keySet() );
        newFieldNames.addAll( newFieldMap.keySet() );

        // Look for fields that exist in both versions. Depending upon the situation, duplicate
        // fields may still be classified as added, removed, or changed
        iterator = oldFieldNames.iterator();

        while (iterator.hasNext()) {
            QName oldFieldName = iterator.next();
            List<TLMemberField<?>> oldVersionFields = oldFieldMap.get( oldFieldName );
            String vScheme = getVersionScheme( oldVersionFields );
            List<QName> matchingNewFieldNames =
                ModelCompareUtils.findMatchingVersions( oldFieldName, newFieldNames, vScheme );
            QName newFieldName = ModelCompareUtils.findClosestVersion( oldFieldName, matchingNewFieldNames, vScheme );

            if (newFieldName != null) {
                List<TLMemberField<?>> newVersionFields = newFieldMap.get( newFieldName );

                // Simple Case: One field of this name in each version (even if its facet location
                // may have changed)
                if ((oldVersionFields.size() == 1) && (newVersionFields.size() == 1)) {
                    FieldChangeSet fieldChangeSet = new FieldComparator( options, fieldNSMappings ).compareFields(
                        new FieldComparisonFacade( oldVersionFields.get( 0 ) ),
                        new FieldComparisonFacade( newVersionFields.get( 0 ) ) );

                    if (!fieldChangeSet.getChangeItems().isEmpty()) {
                        pendingChangeItems.add( new EntityChangeItem( changeSet, fieldChangeSet ) );
                    }

                } else {
                    // Complex Case: Multiple fields with the same name in the old and/or new
                    // version. Because all of the field names are identical in this situation,
                    // we need to re-categorize the fields by their owner's name to determine
                    // which ones were added, deleted, or changed.
                    Map<QName,List<TLMemberField<TLMemberFieldOwner>>> oldFieldsByOwner =
                        buildFieldOwnerMap( oldVersionFields );
                    Map<QName,List<TLMemberField<TLMemberFieldOwner>>> newFieldsByOwner =
                        buildFieldOwnerMap( newVersionFields );

                    // Consider the field to be added if the owner is in the new version but not in the old
                    findAddedNamespaceFields( oldFieldsByOwner, newFieldsByOwner, changeSet, pendingChangeItems );

                    // Consider the field to be deleted if the owner is in the old version but not in the new
                    findDeletedNamspaceFields( oldFieldsByOwner, newFieldsByOwner, changeSet, pendingChangeItems );

                    // Consider the field to be modified if it belongs to the same owner in both the old
                    // and new versions
                    findModifiedNamespaceFields( changeSet, oldFieldsByOwner, newFieldsByOwner, options,
                        fieldNSMappings, pendingChangeItems );
                }
                newFieldNames.remove( newFieldName );
                iterator.remove();
            }
        }

        // Look for fields that were added in the new version
        findAddedFields( oldFieldMap, newFieldMap, changeSet, changeItems, newFieldNames );

        // Look for fields that were deleted from the old version (does not apply for minor versions)
        if (!isMinorVersionCompare) {
            findDeletedFields( oldFieldMap, newFieldMap, changeSet, changeItems, oldFieldNames );
        }

        // Append any pending change items that were discovered earlier in the process
        changeItems.addAll( pendingChangeItems );
    }

    /**
     * Searches the list of old and new fields to determine which ones have been added.
     * 
     * @param oldFieldMap the map of old version fields by qualified name
     * @param newFieldMap the map of new version fields by qualified name
     * @param changeSet the change set to which all new change items will be assigned
     * @param changeItems the list of change items being constructed
     * @param newFieldNames the set of all unique new-version field names
     */
    @SuppressWarnings("unchecked")
    private void findAddedFields(Map<QName,List<TLMemberField<?>>> oldFieldMap,
        Map<QName,List<TLMemberField<?>>> newFieldMap, EntityChangeSet changeSet, List<EntityChangeItem> changeItems,
        SortedSet<QName> newFieldNames) {
        for (QName fieldName : newFieldNames) {
            if (!oldFieldMap.containsKey( fieldName )) {
                List<TLMemberField<?>> addedFields = newFieldMap.get( fieldName );

                for (TLMemberField<?> addedField : addedFields) {
                    changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.MEMBER_FIELD_ADDED,
                        (TLMemberField<TLMemberFieldOwner>) addedField ) );
                }
            }
        }
    }

    /**
     * Searches the list of old and new fields to determine which ones have been added.
     * 
     * @param oldFieldMap the map of old version fields by qualified name
     * @param newFieldMap the map of new version fields by qualified name
     * @param changeSet the change set to which all new change items will be assigned
     * @param changeItems the list of change items being constructed
     * @param oldFieldNames the set of all unique old-version field names
     */
    @SuppressWarnings("unchecked")
    private void findDeletedFields(Map<QName,List<TLMemberField<?>>> oldFieldMap,
        Map<QName,List<TLMemberField<?>>> newFieldMap, EntityChangeSet changeSet, List<EntityChangeItem> changeItems,
        SortedSet<QName> oldFieldNames) {
        for (QName fieldName : oldFieldNames) {
            if (!newFieldMap.containsKey( fieldName )) {
                List<TLMemberField<?>> deletedFields = oldFieldMap.get( fieldName );

                for (TLMemberField<?> deletedField : deletedFields) {
                    changeItems.add( new EntityChangeItem( changeSet, EntityChangeType.MEMBER_FIELD_DELETED,
                        (TLMemberField<TLMemberFieldOwner>) deletedField ) );
                }
            }
        }
    }

    /**
     * Searches the collections of old and new fields to determine which fields have been added.
     * 
     * @param oldFieldsByOwner the map that correlates old fields by their owning entity name
     * @param newFieldsByOwner the map that correlates new fields by their owning entity name
     * @param changeSet the change set to which all new change items will be assigned
     * @param pendingChangeItems the list of change items being constructed
     */
    private void findAddedNamespaceFields(Map<QName,List<TLMemberField<TLMemberFieldOwner>>> oldFieldsByOwner,
        Map<QName,List<TLMemberField<TLMemberFieldOwner>>> newFieldsByOwner, EntityChangeSet changeSet,
        List<EntityChangeItem> pendingChangeItems) {
        for (Entry<QName,List<TLMemberField<TLMemberFieldOwner>>> entry : newFieldsByOwner.entrySet()) {
            QName fieldOwner = entry.getKey();

            if (!oldFieldsByOwner.containsKey( fieldOwner )) {
                List<TLMemberField<TLMemberFieldOwner>> newVersionOwnerFields = newFieldsByOwner.get( fieldOwner );

                for (TLMemberField<TLMemberFieldOwner> addedField : newVersionOwnerFields) {
                    pendingChangeItems
                        .add( new EntityChangeItem( changeSet, EntityChangeType.MEMBER_FIELD_ADDED, addedField ) );
                }
            }
        }
    }

    /**
     * Searches the collections of old and new fields to determine which fields have been deleted.
     * 
     * @param oldFieldsByOwner the map that correlates old fields by their owning entity name
     * @param newFieldsByOwner the map that correlates new fields by their owning entity name
     * @param changeSet the change set to which all new change items will be assigned
     * @param pendingChangeItems the list of change items being constructed
     */
    private void findDeletedNamspaceFields(Map<QName,List<TLMemberField<TLMemberFieldOwner>>> oldFieldsByOwner,
        Map<QName,List<TLMemberField<TLMemberFieldOwner>>> newFieldsByOwner, EntityChangeSet changeSet,
        List<EntityChangeItem> pendingChangeItems) {
        for (Entry<QName,List<TLMemberField<TLMemberFieldOwner>>> entry : oldFieldsByOwner.entrySet()) {
            QName fieldOwner = entry.getKey();

            if (!newFieldsByOwner.containsKey( fieldOwner )) {
                List<TLMemberField<TLMemberFieldOwner>> oldVersionOwnerFields = oldFieldsByOwner.get( fieldOwner );

                for (TLMemberField<TLMemberFieldOwner> deletedField : oldVersionOwnerFields) {
                    pendingChangeItems
                        .add( new EntityChangeItem( changeSet, EntityChangeType.MEMBER_FIELD_DELETED, deletedField ) );
                }

            }
        }
    }

    /**
     * Searches the given collections of old and new fields to determine which have been modified and, in some extreme
     * edge cases, which duplicate fields are really additions or deletions.
     * 
     * @param changeSet the change set to which all new change items will be assigned
     * @param oldFieldsByOwner the map that correlates old fields by their owning entity name
     * @param newFieldsByOwner the map that correlates new fields by their owning entity name
     * @param options the model comparison options
     * @param fieldNSMappings map that correlates a field name with its namespace assignment
     * @param pendingChangeItems the list of change items being constructed
     */
    private void findModifiedNamespaceFields(EntityChangeSet changeSet,
        Map<QName,List<TLMemberField<TLMemberFieldOwner>>> oldFieldsByOwner,
        Map<QName,List<TLMemberField<TLMemberFieldOwner>>> newFieldsByOwner, ModelCompareOptions options,
        Map<String,String> fieldNSMappings, List<EntityChangeItem> pendingChangeItems) {
        for (Entry<QName,List<TLMemberField<TLMemberFieldOwner>>> entry : newFieldsByOwner.entrySet()) {
            QName fieldOwner = entry.getKey();

            if (oldFieldsByOwner.containsKey( fieldOwner )) {
                List<TLMemberField<TLMemberFieldOwner>> oldVersionOwnerFields = oldFieldsByOwner.get( fieldOwner );
                List<TLMemberField<TLMemberFieldOwner>> newVersionOwnerFields = newFieldsByOwner.get( fieldOwner );
                int maxLength = Math.max( oldVersionOwnerFields.size(), newVersionOwnerFields.size() );

                // Extreme Edge Case: If there are multiple members of both list, make a guess that
                // the fields are 1:1 correlations to one another. Any "extra" fields will be considered
                // as added or deleted.
                for (int i = 0; i < maxLength; i++) {
                    TLMemberField<?> oldField =
                        (i >= oldVersionOwnerFields.size()) ? null : oldVersionOwnerFields.get( i );
                    TLMemberField<?> newField =
                        (i >= newVersionOwnerFields.size()) ? null : newVersionOwnerFields.get( i );

                    performFieldComparison( oldField, newField, changeSet, options, fieldNSMappings,
                        pendingChangeItems );
                }
            }
        }
    }

    /**
     * Performs a comparison of the given new and old field. At this point, the fields are known to be different. This
     * method makes the final determination as to whether a field was added, deleted, or modified.
     * 
     * @param oldField the old field being compared (may be null)
     * @param newField the new field being compared (may be null)
     * @param changeSet the change set to which each new change item will belong
     * @param options the model comparison options
     * @param fieldNSMappings map that correlates a field name with its namespace assignment
     * @param pendingChangeItems the list of change items being constructed
     */
    @SuppressWarnings("unchecked")
    private void performFieldComparison(TLMemberField<?> oldField, TLMemberField<?> newField, EntityChangeSet changeSet,
        ModelCompareOptions options, Map<String,String> fieldNSMappings, List<EntityChangeItem> pendingChangeItems) {
        if (oldField == null) {
            pendingChangeItems.add( new EntityChangeItem( changeSet, EntityChangeType.MEMBER_FIELD_ADDED,
                (TLMemberField<TLMemberFieldOwner>) newField ) );

        } else if (newField == null) {
            pendingChangeItems.add( new EntityChangeItem( changeSet, EntityChangeType.MEMBER_FIELD_DELETED,
                (TLMemberField<TLMemberFieldOwner>) oldField ) );

        } else {
            FieldChangeSet fieldChangeSet = new FieldComparator( options, fieldNSMappings )
                .compareFields( new FieldComparisonFacade( oldField ), new FieldComparisonFacade( newField ) );

            if (!fieldChangeSet.getChangeItems().isEmpty()) {
                pendingChangeItems.add( new EntityChangeItem( changeSet, fieldChangeSet ) );
            }
        }
    }

    /**
     * Constructs a field map that associates each field name with a list of declared fields with that name.
     * 
     * @param list the list of fields from which to construct the map
     * @return Map&lt;QName,List&lt;TLMemberField&lt;?&gt;&gt;&gt;
     */
    private Map<QName,List<TLMemberField<?>>> buildFieldMap(List<TLMemberField<TLMemberFieldOwner>> list) {
        Map<QName,List<TLMemberField<?>>> fieldMap = new HashMap<>();

        for (TLMemberField<?> field : list) {
            QName fieldName = new QName( ((NamedEntity) field.getOwner()).getNamespace(), field.getName() );

            if (field instanceof TLProperty) {
                TLProperty fieldProp = (TLProperty) field;
                QName elementName = XsdCodegenUtils.getGlobalElementName( fieldProp.getType() );

                if (elementName != null) {
                    fieldName = elementName;
                }
            }
            fieldName = getAdjustedQName( fieldName );

            List<TLMemberField<?>> fields = fieldMap.get( fieldName );

            if (fields == null) {
                fields = new ArrayList<>();
                fieldMap.put( fieldName, fields );
            }
            fields.add( field );
        }
        return fieldMap;
    }

    /**
     * Constructs a field map that associates the name of each field owner with a list of declared fields with that same
     * owner.
     * 
     * @param oldVersionFields the list of fields from which to construct the map
     * @return Map&lt;QName,List&lt;TLMemberField&lt;?&gt;&gt;&gt;
     */
    @SuppressWarnings("unchecked")
    private Map<QName,List<TLMemberField<TLMemberFieldOwner>>> buildFieldOwnerMap(
        List<TLMemberField<?>> oldVersionFields) {
        Map<QName,List<TLMemberField<TLMemberFieldOwner>>> fieldMap = new HashMap<>();

        for (TLMemberField<?> field : oldVersionFields) {
            NamedEntity fieldOwner = (NamedEntity) field.getOwner();
            String mappedNS = getNamespaceMappings().get( fieldOwner.getNamespace() );
            QName ownerName = getAdjustedQName(
                new QName( (mappedNS != null) ? mappedNS : fieldOwner.getNamespace(), fieldOwner.getLocalName() ) );

            fieldMap.computeIfAbsent( ownerName, n -> fieldMap.put( n, new ArrayList<>() ) );
            fieldMap.get( ownerName ).add( (TLMemberField<TLMemberFieldOwner>) field );
        }
        return fieldMap;
    }

    /**
     * If the comparison option is activated to ignore version changes, the namespace of the given <code>QName</code>
     * will be adjusted to the base namespace. If the option is not active, the original <code>QName</code> will be
     * returned.
     * 
     * @param name the qualified name to adjust
     * @return QName
     */
    private QName getAdjustedQName(QName name) {
        QName adjustedName = name;

        if (getCompareOptions().isSuppressFieldVersionChanges()) {
            adjustedName = new QName( versionScheme.getBaseNamespace( name.getNamespaceURI() ), name.getLocalPart() );
        }
        return adjustedName;
    }

    /**
     * Returns the version scheme of the owning library that contains the given field.
     * 
     * @param fieldList the list of fields
     * @return String
     */
    private String getVersionScheme(List<TLMemberField<?>> fieldList) {
        TLMemberField<?> field = (fieldList == null) ? null : fieldList.get( 0 );
        String vScheme = null;

        if ((field != null) && (field.getOwner() instanceof NamedEntity)) {
            NamedEntity fieldOwner = (NamedEntity) field.getOwner();
            AbstractLibrary library = fieldOwner.getOwningLibrary();

            if (library != null) {
                vScheme = library.getVersionScheme();
            }
        }
        if (vScheme == null) {
            vScheme = vsFactory.getDefaultVersionScheme();
        }
        return vScheme;
    }

    /**
     * Returns the version scheme for the given library.
     * 
     * @param library the library for which to return a version scheme
     * @return VersionScheme
     */
    private VersionScheme getVScheme(AbstractLibrary library) {
        VersionScheme vScheme = null;
        try {
            if (library != null) {
                vScheme = vsFactory.getVersionScheme( library.getVersionScheme() );
            }

        } catch (VersionSchemeException e) {
            try {
                vScheme = vsFactory.getVersionScheme( vsFactory.getDefaultVersionScheme() );

            } catch (VersionSchemeException e1) {
                throw new SchemaCompilerRuntimeException( "Error - Default version scheme could not be identified." );
            }
        }
        return vScheme;
    }

}

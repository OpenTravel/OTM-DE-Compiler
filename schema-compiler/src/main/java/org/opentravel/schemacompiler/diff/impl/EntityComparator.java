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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.diff.EntityChangeItem;
import org.opentravel.schemacompiler.diff.EntityChangeSet;
import org.opentravel.schemacompiler.diff.EntityChangeType;
import org.opentravel.schemacompiler.diff.FieldChangeSet;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * Performs a comparison of two OTM entities.
 */
public class EntityComparator extends BaseComparator {
	
	private static VersionSchemeFactory vsFactory = VersionSchemeFactory.getInstance();
	
	private DisplayFomatter formatter = new DisplayFomatter();
	
	/**
	 * Default constructor.
	 */
	public EntityComparator() {}
	
	/**
	 * Constructor that initializes the namespace mappings for the comparator.
	 * 
	 * @param namespaceMappings  the initial namespace mappings
	 */
	protected EntityComparator(Map<String,String> namespaceMappings) {
		super( namespaceMappings );
	}
	
	/**
	 * Compares two versions of the same OTM entity.
	 * 
	 * @param oldEntity  facade for the old entity version
	 * @param newEntity  facade for the new entity version
	 * @return EntityChangeSet
	 */
	public EntityChangeSet compareEntities(EntityComparisonFacade oldEntity, EntityComparisonFacade newEntity) {
		EntityChangeSet changeSet = new EntityChangeSet( oldEntity.getEntity(), newEntity.getEntity() );
		List<EntityChangeItem> changeItems = changeSet.getEntityChangeItems();
		
		// Look for changes in the library values
		AbstractLibrary owningLibrary = oldEntity.getOwningLibrary();
		String versionScheme = (owningLibrary == null) ? null : owningLibrary.getVersionScheme();
		QName oldParentTypeName = getEntityName( oldEntity.getParentType() );
		QName newParentTypeName = getEntityName( newEntity.getParentType() );
		QName oldExtendsTypeName = getEntityName( oldEntity.getExtendsType() );
		QName newExtendsTypeName = getEntityName( newEntity.getExtendsType() );
		QName oldSimpleCoreTypeName = getEntityName( oldEntity.getSimpleCoreType() );
		QName newSimpleCoreTypeName = getEntityName( newEntity.getSimpleCoreType() );
		
		if (valueChanged( oldEntity.getEntityType(), newEntity.getEntityType() )) {
			changeItems.add( new EntityChangeItem( EntityChangeType.ENTITY_TYPE_CHANGED,
					formatter.getEntityTypeDisplayName( oldEntity.getEntityType() ),
					formatter.getEntityTypeDisplayName( newEntity.getEntityType() ) ) );
		}
		if (valueChanged( oldEntity.getName(), newEntity.getName() )) {
			changeItems.add( new EntityChangeItem( EntityChangeType.NAME_CHANGED,
					oldEntity.getName(), newEntity.getName() ) );
		}
		if (valueChanged( oldEntity.getDocumentation(), newEntity.getDocumentation() )) {
			changeItems.add( new EntityChangeItem( EntityChangeType.DOCUMENTATION_CHANGED, null, null ) );
		}
		if (valueChanged( oldParentTypeName, newParentTypeName )) {
			if (isVersionChange( oldParentTypeName, newParentTypeName, versionScheme )) {
				changeItems.add( new EntityChangeItem( EntityChangeType.PARENT_TYPE_VERSION_CHANGED,
						oldEntity.getOwningLibrary().getVersion(), newEntity.getOwningLibrary().getVersion() ) );
				
			} else {
				changeItems.add( new EntityChangeItem( EntityChangeType.PARENT_TYPE_CHANGED,
						formatter.getEntityDisplayName( oldEntity.getParentType() ),
						formatter.getEntityDisplayName( newEntity.getParentType() ) ) );
			}
		}
		if (valueChanged( oldExtendsTypeName, newExtendsTypeName )) {
			if (isVersionChange( oldExtendsTypeName, newExtendsTypeName, versionScheme )) {
				changeItems.add( new EntityChangeItem( EntityChangeType.EXTENSION_VERSION_CHANGED,
						oldEntity.getOwningLibrary().getVersion(), newEntity.getOwningLibrary().getVersion() ) );
				
			} else {
				changeItems.add( new EntityChangeItem( EntityChangeType.EXTENSION_CHANGED,
						formatter.getEntityDisplayName( oldEntity.getExtendsType() ),
						formatter.getEntityDisplayName( newEntity.getExtendsType() ) ) );
			}
		}
		if (valueChanged( oldSimpleCoreTypeName, newSimpleCoreTypeName )) {
			if (isVersionChange( oldSimpleCoreTypeName, newSimpleCoreTypeName, versionScheme )) {
				changeItems.add( new EntityChangeItem( EntityChangeType.SIMPLE_CORE_TYPE_VERSION_CHANGED,
						oldEntity.getOwningLibrary().getVersion(), newEntity.getOwningLibrary().getVersion() ) );
				
			} else {
				changeItems.add( new EntityChangeItem( EntityChangeType.SIMPLE_CORE_TYPE_CHANGED,
						formatter.getEntityDisplayName( oldEntity.getSimpleCoreType() ),
						formatter.getEntityDisplayName( newEntity.getSimpleCoreType() ) ) );
			}
		}
		if (oldEntity.isSimpleList() != newEntity.isSimpleList()) {
			EntityChangeType changeType = newEntity.isSimpleList() ?
					EntityChangeType.CHANGED_TO_SIMPLE_LIST : EntityChangeType.CHANGED_TO_SIMPLE_NON_LIST;
			changeItems.add( new EntityChangeItem( changeType, "" + oldEntity.isSimpleList(), "" + newEntity.isSimpleList() ) );
		}
		if (valueChanged( oldEntity.getPatternConstraint(), newEntity.getPatternConstraint() )) {
			changeItems.add( new EntityChangeItem( EntityChangeType.PATTERN_CONSTRAINT_CHANGED,
					oldEntity.getPatternConstraint(), newEntity.getPatternConstraint() ) );
		}
		if (oldEntity.getMinLengthConstraint() != newEntity.getMinLengthConstraint()) {
			changeItems.add( new EntityChangeItem( EntityChangeType.MIN_LENGTH_CONSTRAINT_CHANGED,
					"" + oldEntity.getMinLengthConstraint(), "" + newEntity.getMinLengthConstraint() ) );
		}
		if (oldEntity.getMaxLengthConstraint() != newEntity.getMaxLengthConstraint()) {
			changeItems.add( new EntityChangeItem( EntityChangeType.MAX_LENGTH_CONSTRAINT_CHANGED,
					"" + oldEntity.getMaxLengthConstraint(), "" + newEntity.getMaxLengthConstraint() ) );
		}
		if (oldEntity.getFractionDigitsConstraint() != newEntity.getFractionDigitsConstraint()) {
			changeItems.add( new EntityChangeItem( EntityChangeType.FRACTION_DIGITS_CONSTRAINT_CHANGED,
					"" + oldEntity.getFractionDigitsConstraint(), "" + newEntity.getFractionDigitsConstraint() ) );
		}
		if (oldEntity.getTotalDigitsConstraint() != newEntity.getTotalDigitsConstraint()) {
			changeItems.add( new EntityChangeItem( EntityChangeType.TOTAL_DIGITS_CONSTRAINT_CHANGED,
					"" + oldEntity.getTotalDigitsConstraint(), "" + newEntity.getTotalDigitsConstraint() ) );
		}
		if (valueChanged( oldEntity.getMinInclusiveConstraint(), newEntity.getMinInclusiveConstraint() )) {
			changeItems.add( new EntityChangeItem( EntityChangeType.MIN_INCLUSIVE_CONSTRAINT_CHANGED,
					oldEntity.getMinInclusiveConstraint(), newEntity.getMinInclusiveConstraint() ) );
		}
		if (valueChanged( oldEntity.getMaxInclusiveConstraint(), newEntity.getMaxInclusiveConstraint() )) {
			changeItems.add( new EntityChangeItem( EntityChangeType.MAX_INCLUSIVE_CONSTRAINT_CHANGED,
					oldEntity.getMaxInclusiveConstraint(), newEntity.getMaxInclusiveConstraint() ) );
		}
		if (valueChanged( oldEntity.getMinExclusiveConstraint(), newEntity.getMinExclusiveConstraint() )) {
			changeItems.add( new EntityChangeItem( EntityChangeType.MIN_EXCLUSIVE_CONSTRAINT_CHANGED,
					oldEntity.getMinExclusiveConstraint(), newEntity.getMinExclusiveConstraint() ) );
		}
		if (valueChanged( oldEntity.getMaxExclusiveConstraint(), newEntity.getMaxExclusiveConstraint() )) {
			changeItems.add( new EntityChangeItem( EntityChangeType.MAX_EXCLUSIVE_CONSTRAINT_CHANGED,
					oldEntity.getMaxExclusiveConstraint(), newEntity.getMaxExclusiveConstraint() ) );
		}
		
		compareListContents( oldEntity.getAliasNames(), newEntity.getAliasNames(),
				EntityChangeType.ALIAS_ADDED, EntityChangeType.ALIAS_DELETED, changeItems );
		compareListContents( oldEntity.getFacetNames(), newEntity.getFacetNames(),
				EntityChangeType.FACET_ADDED, EntityChangeType.FACET_DELETED, changeItems );
		compareListContents( oldEntity.getRoleNames(), newEntity.getRoleNames(),
				EntityChangeType.ROLE_ADDED, EntityChangeType.ROLE_DELETED, changeItems );
		compareListContents( oldEntity.getEnumValues(), newEntity.getEnumValues(),
				EntityChangeType.ENUM_VALUE_ADDED, EntityChangeType.ENUM_VALUE_DELETED, changeItems );
		
		compareMemberFields( oldEntity.getMemberFields(), newEntity.getMemberFields(), changeItems );
		
		compareListContents( oldEntity.getEquivalents(), newEntity.getEquivalents(),
				EntityChangeType.EQUIVALENT_ADDED, EntityChangeType.EQUIVALENT_DELETED, changeItems );
		compareListContents( oldEntity.getExamples(), newEntity.getExamples(),
				EntityChangeType.EXAMPLE_ADDED, EntityChangeType.EXAMPLE_DELETED, changeItems );
		
		return changeSet;
	}
	
	/**
	 * Compares the old and new versions of the value list and creates entity change items
	 * to represent the values that were added and/or removed in the new version of the list.
	 * 
	 * @param oldValues  the old version's list of values
	 * @param newValues  the new version's list of values
	 * @param addedChangeType  the entity change type to use for added values
	 * @param deletedChangeType  the entity change type to use for deleted values
	 * @param changeItems  the list of change items for the entity
	 */
	private void compareListContents(List<String> oldValues, List<String> newValues,
			EntityChangeType addedChangeType, EntityChangeType deletedChangeType, List<EntityChangeItem> changeItems) {
		for (String newValue : newValues) {
			if (!oldValues.contains( newValue )) {
				changeItems.add( new EntityChangeItem( addedChangeType, null, newValue ) );
			}
		}
		for (String oldValue : oldValues) {
			if (!newValues.contains( oldValue )) {
				changeItems.add( new EntityChangeItem( deletedChangeType, oldValue, null ) );
			}
		}
	}
	
	/**
	 * Compares the lists of member fields from the old and new versions of the entity.  Change
	 * items are appended to the list for any fields that are identified as being added, deleted,
	 * or modified in the new version.
	 * 
	 * @param oldFields  the list of fields for the old entity version
	 * @param newFields  the list of fields for the new entity version
	 * @param changeItems  the list of change items for the entity
	 */
	private void compareMemberFields(List<TLMemberField<?>> oldFields, List<TLMemberField<?>> newFields,
			List<EntityChangeItem> changeItems) {
		Map<QName,List<TLMemberField<?>>> oldFieldMap = buildFieldMap( oldFields );
		Map<QName,List<TLMemberField<?>>> newFieldMap = buildFieldMap( newFields );
		SortedSet<QName> oldFieldNames = new TreeSet<>( new QNameComparator() );
		SortedSet<QName> newFieldNames = new TreeSet<>( new QNameComparator() );
		List<EntityChangeItem> pendingChangeItems = new ArrayList<>();
		Iterator<QName> iterator;
		
		oldFieldNames.addAll( oldFieldMap.keySet() );
		newFieldNames.addAll( newFieldMap.keySet() );
		
		// Look for fields that exist in both versions.  Depending upon the situation, duplicate
		// fields may still be classified as added, removed, or changed
		iterator = oldFieldNames.iterator();
		
		while (iterator.hasNext()) {
			QName oldFieldName = iterator.next();
			List<TLMemberField<?>> oldVersionFields = oldFieldMap.get( oldFieldName );
			String versionScheme = getVersionScheme( oldVersionFields );
			List<QName> matchingNewFieldNames = ModelCompareUtils.findMatchingVersions( oldFieldName, newFieldNames,  versionScheme );
			QName newFieldName = ModelCompareUtils.findClosestVersion( oldFieldName, matchingNewFieldNames, versionScheme );
			
			if (newFieldName != null) {
				List<TLMemberField<?>> newVersionFields = newFieldMap.get( newFieldName );
				
				// Simple Case: One field of this name in each version (even if its facet location
				// may have changed)
				if ((oldVersionFields.size() == 1) && (newVersionFields.size() == 1)) {
					FieldChangeSet fieldChangeSet = new FieldComparator( getNamespaceMappings() ).compareFields(
							new FieldComparisonFacade( oldVersionFields.get( 0 ) ),
							new FieldComparisonFacade( newVersionFields.get( 0 ) ) );
					
					if (!fieldChangeSet.getFieldChangeItems().isEmpty()) {
						pendingChangeItems.add( new EntityChangeItem( fieldChangeSet ) );
					}
					
				} else {
					// Complex Case: Multiple fields with the same name in the old and/or new
					// version.  Because all of the field names are identical in this situation,
					// we need to re-categorize the fields by their owner's name to determine
					// which ones were added, deleted, or changed.
					Map<QName,List<TLMemberField<?>>> oldFieldsByOwner = buildFieldOwnerMap( oldVersionFields );
					Map<QName,List<TLMemberField<?>>> newFieldsByOwner = buildFieldOwnerMap( newVersionFields );
					
					// Consider the field to be added if the owner is in the new version but not in the old
					for (QName fieldOwner : newFieldsByOwner.keySet()) {
						if (!oldFieldsByOwner.containsKey( fieldOwner )) {
							List<TLMemberField<?>> newVersionOwnerFields = newFieldsByOwner.get( fieldOwner );
							
							for (TLMemberField<?> addedField : newVersionOwnerFields) {
								pendingChangeItems.add( new EntityChangeItem( EntityChangeType.MEMBER_FIELD_ADDED, addedField ) );
							}
						}
					}
					
					// Consider the field to be deleted if the owner is in the old version but not in the new
					for (QName fieldOwner : oldFieldsByOwner.keySet()) {
						if (!newFieldsByOwner.containsKey( fieldOwner )) {
							List<TLMemberField<?>> oldVersionOwnerFields = oldFieldsByOwner.get( fieldOwner );
							
							for (TLMemberField<?> deletedField : oldVersionOwnerFields) {
								pendingChangeItems.add( new EntityChangeItem( EntityChangeType.MEMBER_FIELD_DELETED, deletedField ) );
							}
							
						}
					}
					
					// Consider the field to be modified if it belongs to the same owner in both the old
					// and new versions
					for (QName fieldOwner : newFieldsByOwner.keySet()) {
						if (oldFieldsByOwner.containsKey( fieldOwner )) {
							List<TLMemberField<?>> oldVersionOwnerFields = oldFieldsByOwner.get( fieldOwner );
							List<TLMemberField<?>> newVersionOwnerFields = newFieldsByOwner.get( fieldOwner );
							int maxLength = Math.max( oldVersionOwnerFields.size(), newVersionOwnerFields.size() );
							
							// Extreme Edge Case:  If there are multiple members of both list, make a guess that
							// the fields are 1:1 correlations to one another.  Any "extra" fields will be considered
							// as added or deleted.
							for (int i = 0; i < maxLength; i++) {
								TLMemberField<?> oldField = (i >= oldVersionOwnerFields.size()) ? null : oldVersionOwnerFields.get( i );
								TLMemberField<?> newField = (i >= newVersionOwnerFields.size()) ? null : newVersionOwnerFields.get( i );
								
								if (oldField == null) {
									pendingChangeItems.add( new EntityChangeItem( EntityChangeType.MEMBER_FIELD_ADDED, newField ) );
									
								} else if (newField == null) {
									pendingChangeItems.add( new EntityChangeItem( EntityChangeType.MEMBER_FIELD_DELETED, oldField ) );
									
								} else {
									FieldChangeSet fieldChangeSet = new FieldComparator( getNamespaceMappings() ).compareFields(
											new FieldComparisonFacade( oldField ), new FieldComparisonFacade( newField ) );
									
									if (!fieldChangeSet.getFieldChangeItems().isEmpty()) {
										pendingChangeItems.add( new EntityChangeItem( fieldChangeSet ) );
									}
								}
							}
						}
					}
				}
				newFieldNames.remove( newFieldName );
				iterator.remove();
			}
		}
		
		// Look for fields that were added in the new version
		for (QName fieldName : newFieldNames) {
			if (!oldFieldMap.containsKey( fieldName )) {
				List<TLMemberField<?>> addedFields = newFieldMap.get( fieldName );
				
				for (TLMemberField<?> addedField : addedFields) {
					changeItems.add( new EntityChangeItem( EntityChangeType.MEMBER_FIELD_ADDED, addedField ) );
				}
			}
		}
		
		// Look for fields that were deleted from the old version
		for (QName fieldName : oldFieldNames) {
			if (!newFieldMap.containsKey( fieldName )) {
				List<TLMemberField<?>> deletedFields = oldFieldMap.get( fieldName );
				
				for (TLMemberField<?> deletedField : deletedFields) {
					changeItems.add( new EntityChangeItem( EntityChangeType.MEMBER_FIELD_DELETED, deletedField ) );
				}
			}
		}
		
		// Append any pending change items that were discovered earlier in the process
		changeItems.addAll( pendingChangeItems );
	}
	
	/**
	 * Constructs a field map that associates each field name with a list of declared
	 * fields with that name.
	 * 
	 * @param fieldList  the list of fields from which to construct the map
	 * @return Map<QName,List<TLMemberField<?>>>
	 */
	private Map<QName,List<TLMemberField<?>>> buildFieldMap(List<TLMemberField<?>> fieldList) {
		Map<QName,List<TLMemberField<?>>> fieldMap = new HashMap<>();
		
		for (TLMemberField<?> field : fieldList) {
			QName fieldName = new QName( ((NamedEntity) field.getOwner()).getNamespace(), field.getName() );
			
			if (field instanceof TLProperty) {
				TLProperty fieldProp = (TLProperty) field;
				QName elementName = XsdCodegenUtils.getGlobalElementName( fieldProp.getType() );
				
				if (elementName != null) {
					fieldName = elementName;
				}
			}
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
	 * Constructs a field map that associates the name of each field owner with a list
	 * of declared fields with that same owner.
	 * 
	 * @param fieldList  the list of fields from which to construct the map
	 * @return Map<QName,List<TLMemberField<?>>>
	 */
	private Map<QName,List<TLMemberField<?>>> buildFieldOwnerMap(List<TLMemberField<?>> fieldList) {
		Map<QName,List<TLMemberField<?>>> fieldMap = new HashMap<>();
		
		for (TLMemberField<?> field : fieldList) {
			NamedEntity fieldOwner = (NamedEntity) field.getOwner();
			String mappedNS = getNamespaceMappings().get( fieldOwner.getNamespace() );
			QName ownerName = new QName(
					(mappedNS != null) ? mappedNS : fieldOwner.getNamespace(), fieldOwner.getLocalName() );
			List<TLMemberField<?>> fields = fieldMap.get( ownerName );
			
			if (fields == null) {
				fields = new ArrayList<>();
				fieldMap.put( ownerName, fields );
			}
			fields.add( field );
		}
		return fieldMap;
	}
	
	/**
	 * Returns the version scheme of the owning library that contains the given field.
	 * 
	 * @param fieldList  the list of fields
	 * @return String
	 */
	private String getVersionScheme(List<TLMemberField<?>> fieldList) {
		TLMemberField<?> field = (fieldList == null) ? null : fieldList.get( 0 );
		String versionScheme = null;
		
		if ((field != null) && (field.getOwner() instanceof NamedEntity)) {
			NamedEntity fieldOwner = (NamedEntity) field.getOwner();
			AbstractLibrary library = fieldOwner.getOwningLibrary();
			
			if (library != null) {
				versionScheme = library.getVersionScheme();
			}
		}
		if (versionScheme == null) {
			versionScheme = vsFactory.getDefaultVersionScheme();
		}
		return versionScheme;
	}
	
}

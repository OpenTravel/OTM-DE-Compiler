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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.diff.EntityChangeSet;
import org.opentravel.schemacompiler.diff.ModelCompareOptions;
import org.opentravel.schemacompiler.diff.ResourceActionChangeSet;
import org.opentravel.schemacompiler.diff.ResourceActionResponseChangeSet;
import org.opentravel.schemacompiler.diff.ResourceChangeItem;
import org.opentravel.schemacompiler.diff.ResourceChangeSet;
import org.opentravel.schemacompiler.diff.ResourceChangeType;
import org.opentravel.schemacompiler.diff.ResourceParamGroupChangeSet;
import org.opentravel.schemacompiler.diff.ResourceParameterChangeSet;
import org.opentravel.schemacompiler.diff.ResourceParentRefChangeSet;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;

/**
 * Performs a comparison of two OTM resources.
 */
public class ResourceComparator extends BaseComparator {
	
	private DisplayFormatter formatter = new DisplayFormatter();
	
	/**
	 * Constructor that initializes the comparison options and namespace mappings
	 * for the comparator.
	 * 
	 * @param compareOptions  the model comparison options to apply during processing
	 * @param namespaceMappings  the initial namespace mappings
	 */
	public ResourceComparator(ModelCompareOptions compareOptions, Map<String,String> namespaceMappings) {
		super( compareOptions, namespaceMappings );
	}
	
	/**
	 * Compares two versions of the same OTM resource.
	 * 
	 * @param oldResource  facade for the old resource version
	 * @param newResource  facade for the new resource version
	 * @return ResourceChangeSet
	 */
	public ResourceChangeSet compareResources(TLResource oldResource, TLResource newResource) {
		ResourceChangeSet changeSet = new ResourceChangeSet( oldResource, newResource );
		List<ResourceChangeItem> changeItems = changeSet.getChangeItems();
		
		// Look for changes in resource values
		AbstractLibrary owningLibrary = oldResource.getOwningLibrary();
		String versionScheme = (owningLibrary == null) ? null : owningLibrary.getVersionScheme();
		boolean isMinorVersionCompare = isMinorVersionCompare( new EntityComparisonFacade( oldResource ),
				new EntityComparisonFacade( newResource ), versionScheme );
		NamedEntity oldExtendsType = (oldResource.getExtension() == null) ? null : oldResource.getExtension().getExtendsEntity();
		NamedEntity newExtendsType = (newResource.getExtension() == null) ? null : newResource.getExtension().getExtendsEntity();
		QName oldExtendsTypeName = (oldExtendsType == null) ? null : getEntityName( oldExtendsType );
		QName newExtendsTypeName = (newExtendsType == null) ? null : getEntityName( newExtendsType );
		NamedEntity oldBORefType = (oldResource.getExtension() == null) ? null : oldResource.getBusinessObjectRef();
		NamedEntity newBORefType = (newResource.getExtension() == null) ? null : newResource.getBusinessObjectRef();
		QName oldBORefTypeName = (oldBORefType == null) ? null : getEntityName( oldBORefType );
		QName newBORefTypeName = (newBORefType == null) ? null : getEntityName( newBORefType );
		
		if (valueChanged( oldResource.getName(), newResource.getName() )) {
			changeItems.add( new ResourceChangeItem( changeSet,
					ResourceChangeType.NAME_CHANGED,
					oldResource.getName(), newResource.getName() ) );
		}
		if (valueChanged( oldResource.getDocumentation(), newResource.getDocumentation() )) {
			changeItems.add( new ResourceChangeItem( changeSet,
					ResourceChangeType.DOCUMENTATION_CHANGED, null, null ) );
		}
		if (valueChanged( oldResource.getBasePath(), newResource.getBasePath() )) {
			changeItems.add( new ResourceChangeItem( changeSet,
					ResourceChangeType.BASE_PATH_CHANGED,
					oldResource.getBasePath(), newResource.getBasePath() ) );
		}
		if (oldResource.isAbstract() != newResource.isAbstract()) {
			changeItems.add( new ResourceChangeItem( changeSet,
					ResourceChangeType.ABSTRACT_IND_CHANGED,
					oldResource.isAbstract() + "", newResource.isAbstract() + "" ) );
		}
		if (oldResource.isFirstClass() != newResource.isFirstClass()) {
			changeItems.add( new ResourceChangeItem( changeSet,
					ResourceChangeType.FIRST_CLASS_IND_CHANGED,
					oldResource.isFirstClass() + "", newResource.isFirstClass() + "" ) );
		}
		if (valueChanged( oldExtendsTypeName, newExtendsTypeName )) {
			if (isVersionChange( oldExtendsTypeName, newExtendsTypeName, versionScheme )) {
				changeItems.add( new ResourceChangeItem( changeSet,
						ResourceChangeType.EXTENSION_VERSION_CHANGED,
						oldResource.getOwningLibrary().getVersion(),
						newResource.getOwningLibrary().getVersion() ) );
				
			} else {
				changeItems.add( new ResourceChangeItem( changeSet,
						ResourceChangeType.EXTENSION_CHANGED,
						formatter.getEntityDisplayName( oldExtendsType ),
						formatter.getEntityDisplayName( newExtendsType ) ) );
			}
		}
		if (valueChanged( oldBORefTypeName, newBORefTypeName )) {
			if (isVersionChange( oldBORefTypeName, newBORefTypeName, versionScheme )) {
				changeItems.add( new ResourceChangeItem( changeSet,
						ResourceChangeType.BUSINESS_OBJECT_REF_VERSION_CHANGED,
						oldResource.getOwningLibrary().getVersion(),
						newResource.getOwningLibrary().getVersion() ) );
				
			} else {
				changeItems.add( new ResourceChangeItem( changeSet,
						ResourceChangeType.BUSINESS_OBJECT_REF_CHANGED,
						formatter.getEntityDisplayName( oldBORefType ),
						formatter.getEntityDisplayName( newBORefType ) ) );
			}
		}
		
		// Look for added, removed, and changed TLResourceParentReference
		for (TLResourceParentRef newParentRef : newResource.getParentRefs()) {
			String newParentRefName = newParentRef.getParentResource().getName() +
					"/" + newParentRef.getParentParamGroup().getName();
			TLResourceParentRef oldParentRef = oldResource.getParentRef( newParentRefName );
			
			if (oldParentRef == null) {
				changeItems.add( new ResourceChangeItem( changeSet,
						ResourceChangeType.PARENT_REF_ADDED, null, newParentRefName ) );
				
			} else {
				ResourceParentRefChangeSet parentRefChangeSet = compareParentRefs(
						oldParentRef, newParentRef, changeSet, isMinorVersionCompare );
				
				if (!parentRefChangeSet.getChangeItems().isEmpty()) {
					changeItems.add( new ResourceChangeItem( changeSet, parentRefChangeSet ) );
				}
			}
		}
		if (!isMinorVersionCompare) {
			for (TLResourceParentRef oldParentRef : oldResource.getParentRefs()) {
				String oldParentRefName = oldParentRef.getParentResource().getName() +
						"/" + oldParentRef.getParentParamGroup().getName();
				TLResourceParentRef newParentRef = oldResource.getParentRef( oldParentRefName );
				
				if (newParentRef == null) {
					changeItems.add( new ResourceChangeItem( changeSet,
							ResourceChangeType.PARENT_REF_DELETED, oldParentRefName, null ) );
				}
			}
		}
		
		// Look for added, removed, and changed TLParamGroup
		for (TLParamGroup newParamGroup : newResource.getParamGroups()) {
			String newParamGroupName = newParamGroup.getName();
			TLParamGroup oldParamGroup = oldResource.getParamGroup( newParamGroupName );
			
			if (oldParamGroup == null) {
				changeItems.add( new ResourceChangeItem( changeSet,
						ResourceChangeType.PARAM_GROUP_ADDED, null, newParamGroupName ) );
				
			} else {
				ResourceParamGroupChangeSet paramGroupChangeSet = compareParamGroups(
						oldParamGroup, newParamGroup, changeSet, isMinorVersionCompare );
				
				if (!paramGroupChangeSet.getChangeItems().isEmpty()) {
					changeItems.add( new ResourceChangeItem( changeSet, paramGroupChangeSet ) );
				}
			}
		}
		if (!isMinorVersionCompare) {
			for (TLParamGroup oldParamGroup : oldResource.getParamGroups()) {
				String oldParamGroupName = oldParamGroup.getName();
				TLParamGroup newParamGroup = oldResource.getParamGroup( oldParamGroupName );
				
				if (newParamGroup == null) {
					changeItems.add( new ResourceChangeItem( changeSet,
							ResourceChangeType.PARAM_GROUP_DELETED, oldParamGroupName, null ) );
				}
			}
		}
		
		// Look for added, removed, and changed TLActionFacet
		for (TLActionFacet newActionFacet : newResource.getActionFacets()) {
			String newActionFacetName = newActionFacet.getName();
			TLActionFacet oldActionFacet = oldResource.getActionFacet( newActionFacetName );
			
			if (oldActionFacet == null) {
				changeItems.add( new ResourceChangeItem( changeSet,
						ResourceChangeType.ACTION_FACET_ADDED, newActionFacetName, null ) );
				
			} else {
				EntityComparator comparator = new EntityComparator( getCompareOptions(), getNamespaceMappings() );
				EntityChangeSet actionFacetChangeSet = comparator.compareEntities(
						new EntityComparisonFacade( oldActionFacet ), new EntityComparisonFacade( newActionFacet ) );
				
				if (!actionFacetChangeSet.getChangeItems().isEmpty()) {
					changeItems.add( new ResourceChangeItem( changeSet, actionFacetChangeSet ) );
				}
			}
		}
		if (!isMinorVersionCompare) {
			for (TLActionFacet oldActionFacet : oldResource.getActionFacets()) {
				String oldActionFacetName = oldActionFacet.getName();
				TLActionFacet newActionFacet = oldResource.getActionFacet( oldActionFacetName );
				
				if (newActionFacet == null) {
					changeItems.add( new ResourceChangeItem( changeSet,
							ResourceChangeType.ACTION_FACET_DELETED, oldActionFacetName, null ) );
				}
			}
		}
		
		// Look for added, removed, and changed TLAction
		for (TLAction newAction : newResource.getActions()) {
			String newActionName = newAction.getActionId();
			TLAction oldAction = oldResource.getAction( newActionName );
			
			if (oldAction == null) {
				changeItems.add( new ResourceChangeItem( changeSet,
						ResourceChangeType.ACTION_ADDED, null, newActionName ) );
				
			} else {
				ResourceActionChangeSet actionChangeSet = compareActions(
						oldAction, newAction, changeSet, isMinorVersionCompare );
				
				if (!actionChangeSet.getChangeItems().isEmpty()) {
					changeItems.add( new ResourceChangeItem( changeSet, actionChangeSet ) );
				}
			}
		}
		if (!isMinorVersionCompare) {
			for (TLAction oldAction : oldResource.getActions()) {
				String oldActionName = oldAction.getActionId();
				TLAction newAction = oldResource.getAction( oldActionName );
				
				if (newAction == null) {
					changeItems.add( new ResourceChangeItem( changeSet,
							ResourceChangeType.ACTION_DELETED, oldActionName, null ) );
				}
			}
		}
		
		return changeSet;
	}
	
	/**
	 * Compares two versions of the same OTM resource parent reference.
	 * 
	 * @param oldParentRef  facade for the old resource parent reference version
	 * @param newParentRef  facade for the new resource parent reference version
	 * @param resourceChangeSet  change set for the owning resource
	 * @param isMinorVersionCompare  true if the new version is a later minor version of the old one
	 * @return ResourceParentRefChangeSet
	 */
	public ResourceParentRefChangeSet compareParentRefs(TLResourceParentRef oldParentRef,
			TLResourceParentRef newParentRef, ResourceChangeSet resourceChangeSet, boolean isMinorVersionCompare) {
		ResourceParentRefChangeSet changeSet = new ResourceParentRefChangeSet( oldParentRef, newParentRef );
		List<ResourceChangeItem> changeItems = changeSet.getChangeItems();
		
		if (valueChanged( oldParentRef.getPathTemplate(), newParentRef.getPathTemplate() )) {
			changeItems.add( new ResourceChangeItem( resourceChangeSet,
					ResourceChangeType.PATH_TEMPLATE_CHANGED,
					oldParentRef.getPathTemplate(), newParentRef.getPathTemplate() ) );
		}
		if (valueChanged( oldParentRef.getParentParamGroupName(), newParentRef.getParentParamGroupName() )) {
			changeItems.add( new ResourceChangeItem( resourceChangeSet,
					ResourceChangeType.PARENT_PARAM_GROUP_CHANGED,
					oldParentRef.getParentParamGroupName(),
					newParentRef.getParentParamGroupName() ) );
		}
		if (valueChanged( oldParentRef.getDocumentation(), newParentRef.getDocumentation() )) {
			changeItems.add( new ResourceChangeItem( resourceChangeSet,
					ResourceChangeType.DOCUMENTATION_CHANGED, null, null ) );
		}
		return changeSet;
	}
	
	/**
	 * Compares two versions of the same OTM parameter group reference.
	 * 
	 * @param oldParamGroup  facade for the old resource parameter group version
	 * @param newParamGroup  facade for the new resource parameter group version
	 * @param resourceChangeSet  change set for the owning resource
	 * @param isMinorVersionCompare  true if the new version is a later minor version of the old one
	 * @return ResourceParamGroupChangeSet
	 */
	public ResourceParamGroupChangeSet compareParamGroups(TLParamGroup oldParamGroup,
			TLParamGroup newParamGroup, ResourceChangeSet resourceChangeSet, boolean isMinorVersionCompare) {
		ResourceParamGroupChangeSet changeSet = new ResourceParamGroupChangeSet( oldParamGroup, newParamGroup );
		List<ResourceChangeItem> changeItems = changeSet.getChangeItems();
		
		if (valueChanged( oldParamGroup.getFacetRefName(), newParamGroup.getFacetRefName() )) {
			changeItems.add( new ResourceChangeItem( resourceChangeSet, ResourceChangeType.FACET_REF_CHANGED,
					oldParamGroup.getFacetRefName(), newParamGroup.getFacetRefName() ) );
		}
		if (valueChanged( oldParamGroup.getDocumentation(), newParamGroup.getDocumentation() )) {
			changeItems.add( new ResourceChangeItem( resourceChangeSet,
					ResourceChangeType.DOCUMENTATION_CHANGED, null, null ) );
		}
		
		// Look for added, removed, and changed TLActionResponse
		for (TLParameter newParam : newParamGroup.getParameters()) {
			String newParamName = newParam.getFieldRefName();
			TLParameter oldParam = oldParamGroup.getParameter( newParamName );
			
			if (oldParam == null) {
				changeItems.add( new ResourceChangeItem( resourceChangeSet,
						ResourceChangeType.PARAMETER_ADDED, null, newParamName ) );
				
			} else {
				ResourceParameterChangeSet paramChangeSet = compareParameters(
						oldParam, newParam, resourceChangeSet, isMinorVersionCompare );
				
				if (!paramChangeSet.getChangeItems().isEmpty()) {
					changeItems.add( new ResourceChangeItem( resourceChangeSet, paramChangeSet ) );
				}
			}
		}
		
		if (!isMinorVersionCompare) {
			for (TLParameter oldParam : oldParamGroup.getParameters()) {
				String oldParamName = oldParam.getFieldRefName();
				TLParameter newParam = newParamGroup.getParameter( oldParamName );
				
				if (newParam == null) {
					changeItems.add( new ResourceChangeItem( resourceChangeSet,
							ResourceChangeType.PARAMETER_DELETED, oldParamName, null ) );
				}
			}
		}
		
		return changeSet;
	}
	
	/**
	 * Compares two versions of the same OTM resource parameter.
	 * 
	 * @param oldParameter  facade for the old resource parameter version
	 * @param newParameter  facade for the new resource parameter version
	 * @param resourceChangeSet  change set for the owning resource
	 * @param isMinorVersionCompare  true if the new version is a later minor version of the old one
	 * @return ResourceParameterChangeSet
	 */
	public ResourceParameterChangeSet compareParameters(TLParameter oldParameter,
			TLParameter newParameter, ResourceChangeSet resourceChangeSet, boolean isMinorVersionCompare) {
		ResourceParameterChangeSet changeSet = new ResourceParameterChangeSet( oldParameter, newParameter );
		List<ResourceChangeItem> changeItems = changeSet.getChangeItems();
		List<String> oldEqualvalents = ModelCompareUtils.getEquivalents( oldParameter );
		List<String> newEqualvalents = ModelCompareUtils.getEquivalents( newParameter );
		List<String> oldExamples = ModelCompareUtils.getExamples( oldParameter );
		List<String> newExamples = ModelCompareUtils.getExamples( newParameter );
		
		if (oldParameter.getLocation() != newParameter.getLocation() ) {
			changeItems.add( new ResourceChangeItem( resourceChangeSet,
					ResourceChangeType.LOCATION_CHANGED,
					oldParameter.getLocation() + "", newParameter.getLocation() + "" ) );
		}
		if (valueChanged( oldParameter.getDocumentation(), newParameter.getDocumentation() )) {
			changeItems.add( new ResourceChangeItem( resourceChangeSet,
					ResourceChangeType.DOCUMENTATION_CHANGED, null, null ) );
		}
		
		compareListContents( oldEqualvalents, newEqualvalents, ResourceChangeType.EQUIVALENT_ADDED,
				ResourceChangeType.EQUIVALENT_DELETED, changeItems, resourceChangeSet, isMinorVersionCompare );
		compareListContents( oldExamples, newExamples, ResourceChangeType.EXAMPLE_ADDED,
				ResourceChangeType.EXAMPLE_DELETED, changeItems, resourceChangeSet, isMinorVersionCompare );
		return changeSet;
	}
	
	/**
	 * Compares two versions of the same OTM resource action.
	 * 
	 * @param oldAction  facade for the old resource action version
	 * @param newAction  facade for the new resource action version
	 * @param resourceChangeSet  change set for the owning resource
	 * @param isMinorVersionCompare  true if the new version is a later minor version of the old one
	 * @return ResourceActionChangeSet
	 */
	public ResourceActionChangeSet compareActions(TLAction oldAction, TLAction newAction,
			ResourceChangeSet resourceChangeSet, boolean isMinorVersionCompare) {
		ResourceActionChangeSet changeSet = new ResourceActionChangeSet( oldAction, newAction );
		List<ResourceChangeItem> changeItems = changeSet.getChangeItems();
		AbstractLibrary owningLibrary = oldAction.getOwningLibrary();
		String versionScheme = (owningLibrary == null) ? null : owningLibrary.getVersionScheme();
		TLActionRequest oldRequest = oldAction.getRequest();
		TLActionRequest newRequest = newAction.getRequest();
		NamedEntity oldPayloadType = (oldRequest == null) ? null : oldRequest.getPayloadType();
		NamedEntity newPayloadType = (newRequest == null) ? null : newRequest.getPayloadType();
		QName oldPayloadTypeName = (oldPayloadType == null) ? null : getEntityName( oldPayloadType );
		QName newPayloadTypeName = (newPayloadType == null) ? null : getEntityName( newPayloadType );
		TLHttpMethod oldHttpMethod = (oldRequest == null) ? null : oldRequest.getHttpMethod();
		TLHttpMethod newHttpMethod = (newRequest == null) ? null : newRequest.getHttpMethod();
		String oldPathTemplate = (oldRequest == null) ? null : oldRequest.getPathTemplate();
		String newPathTemplate = (newRequest == null) ? null : newRequest.getPathTemplate();
		
		if (oldAction.isCommonAction() != newAction.isCommonAction() ) {
			changeItems.add( new ResourceChangeItem( resourceChangeSet,
					ResourceChangeType.COMMON_ACTION_IND_CHANGED,
					oldAction.isCommonAction() + "", newAction.isCommonAction() + "" ) );
		}
		if (oldHttpMethod != newHttpMethod) {
			changeItems.add( new ResourceChangeItem( resourceChangeSet,
					ResourceChangeType.REQUEST_METHOD_CHANGED,
					oldHttpMethod + "", newHttpMethod + "" ) );
		}
		if (valueChanged( oldRequest.getParamGroupName(), newRequest.getParamGroupName() )) {
			changeItems.add( new ResourceChangeItem( resourceChangeSet,
					ResourceChangeType.REQUEST_PARAM_GROUP_CHANGED,
					oldRequest.getParamGroupName(), newRequest.getParamGroupName() ) );
		}
		if (valueChanged( oldPathTemplate, newPathTemplate )) {
			changeItems.add( new ResourceChangeItem( resourceChangeSet,
					ResourceChangeType.REQUEST_PATH_TEMPLATE_CHANGED,
					oldRequest.getPathTemplate(), newRequest.getPathTemplate() ) );
		}
		if (valueChanged( oldPayloadTypeName, newPayloadTypeName )) {
			if (isVersionChange( oldPayloadTypeName, newPayloadTypeName, versionScheme )) {
				changeItems.add( new ResourceChangeItem( resourceChangeSet,
						ResourceChangeType.REQUEST_PAYLOAD_TYPE_VERSION_CHANGED,
						oldAction.getOwningLibrary().getVersion(),
						newAction.getOwningLibrary().getVersion() ) );
				
			} else {
				changeItems.add( new ResourceChangeItem( resourceChangeSet,
						ResourceChangeType.REQUEST_PAYLOAD_TYPE_CHANGED,
						formatter.getEntityDisplayName( oldPayloadType ),
						formatter.getEntityDisplayName( newPayloadType ) ) );
			}
		}
		
		if ((oldRequest != null) && (newRequest != null)) {
			compareListContents( getStrings( oldRequest.getMimeTypes() ), getStrings( newRequest.getMimeTypes() ),
					ResourceChangeType.REQUEST_MIME_TYPE_ADDED, ResourceChangeType.REQUEST_MIME_TYPE_DELETED,
					changeItems, resourceChangeSet, false);
		}
		
		if (valueChanged( oldAction.getDocumentation(), newAction.getDocumentation() )) {
			changeItems.add( new ResourceChangeItem( resourceChangeSet,
					ResourceChangeType.DOCUMENTATION_CHANGED, null, null ) );
		}
		
		// Look for added, removed, and changed TLActionResponse
		for (TLActionResponse newResponse : newAction.getResponses()) {
			String newResponseName = getResponseId( newResponse );
			TLActionResponse oldResponse = getResponse( oldAction, newResponseName );
			
			if (oldResponse == null) {
				changeItems.add( new ResourceChangeItem( resourceChangeSet,
						ResourceChangeType.RESPONSE_ADDED, null, newResponseName ) );
				
			} else {
				ResourceActionResponseChangeSet responseChangeSet = compareActionResponses(
						oldResponse, newResponse, resourceChangeSet, isMinorVersionCompare );
				
				if (!responseChangeSet.getChangeItems().isEmpty()) {
					changeItems.add( new ResourceChangeItem( resourceChangeSet, responseChangeSet ) );
				}
			}
		}
		
		if (!isMinorVersionCompare) {
			for (TLActionResponse oldResponse : oldAction.getResponses()) {
				String oldResponseName = getResponseId( oldResponse );
				TLActionResponse newResponse = getResponse( newAction, oldResponseName );
				
				if (newResponse == null) {
					changeItems.add( new ResourceChangeItem( resourceChangeSet,
							ResourceChangeType.RESPONSE_DELETED, oldResponseName, null ) );
				}
			}
		}
		
		return changeSet;
	}
	
	/**
	 * Compares two versions of the same OTM resource action response.
	 * 
	 * @param oldResponse  facade for the old resource action response version
	 * @param newResponse  facade for the new resource action response version
	 * @param resourceChangeSet  change set for the owning resource
	 * @param isMinorVersionCompare  true if the new version is a later minor version of the old one
	 * @return ResourceActionResponseChangeSet
	 */
	public ResourceActionResponseChangeSet compareActionResponses(TLActionResponse oldResponse,
			TLActionResponse newResponse, ResourceChangeSet resourceChangeSet, boolean isMinorVersionCompare) {
		ResourceActionResponseChangeSet changeSet = new ResourceActionResponseChangeSet( oldResponse, newResponse );
		List<ResourceChangeItem> changeItems = changeSet.getChangeItems();
		AbstractLibrary owningLibrary = oldResponse.getOwningLibrary();
		String versionScheme = (owningLibrary == null) ? null : owningLibrary.getVersionScheme();
		NamedEntity oldPayloadType = oldResponse.getPayloadType();
		NamedEntity newPayloadType = newResponse.getPayloadType();
		QName oldPayloadTypeName = (oldPayloadType == null) ? null : getEntityName( oldPayloadType );
		QName newPayloadTypeName = (newPayloadType == null) ? null : getEntityName( newPayloadType );
		
		compareListContents( getStrings( oldResponse.getStatusCodes() ), getStrings( newResponse.getStatusCodes() ),
				ResourceChangeType.STATUS_CODE_ADDED, ResourceChangeType.STATUS_CODE_DELETED,
				changeItems, resourceChangeSet, false);
		
		if (valueChanged( oldPayloadTypeName, newPayloadTypeName )) {
			if (isVersionChange( oldPayloadTypeName, newPayloadTypeName, versionScheme )) {
				changeItems.add( new ResourceChangeItem( resourceChangeSet,
						ResourceChangeType.RESPONSE_PAYLOAD_TYPE_VERSION_CHANGED,
						oldResponse.getOwningLibrary().getVersion(), newResponse.getOwningLibrary().getVersion() ) );
				
			} else {
				changeItems.add( new ResourceChangeItem( resourceChangeSet,
						ResourceChangeType.RESPONSE_PAYLOAD_TYPE_CHANGED,
						formatter.getEntityDisplayName( oldPayloadType ),
						formatter.getEntityDisplayName( newPayloadType ) ) );
			}
		}
		
		compareListContents( getStrings( oldResponse.getMimeTypes() ), getStrings( newResponse.getMimeTypes() ),
				ResourceChangeType.RESPONSE_MIME_TYPE_ADDED, ResourceChangeType.RESPONSE_MIME_TYPE_DELETED,
				changeItems, resourceChangeSet, false);
		
		if (valueChanged( oldResponse.getDocumentation(), newResponse.getDocumentation() )) {
			changeItems.add( new ResourceChangeItem( resourceChangeSet,
					ResourceChangeType.DOCUMENTATION_CHANGED, null, null ) );
		}
		
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
	 * @param resourceChangeSet  change set for the owning resource
	 * @param isMinorVersionCompare  true if the second entity is a later minor version of the first
	 */
	private void compareListContents(List<String> oldValues, List<String> newValues,
			ResourceChangeType addedChangeType, ResourceChangeType deletedChangeType,
			List<ResourceChangeItem> changeItems, ResourceChangeSet resourceChangeSet,
			boolean isMinorVersionCompare) {
		for (String newValue : newValues) {
			if (!oldValues.contains( newValue )) {
				changeItems.add( new ResourceChangeItem( resourceChangeSet, addedChangeType, null, newValue ) );
			}
		}
		
		if (!isMinorVersionCompare) {
			for (String oldValue : oldValues) {
				if (!newValues.contains( oldValue )) {
					changeItems.add( new ResourceChangeItem( resourceChangeSet, deletedChangeType, oldValue, null ) );
				}
			}
		}
	}
	
	/**
	 * Returns a unique string identifier for the given response.
	 * 
	 * @param response  the response for which to return an identifier
	 * @return String
	 */
	protected static String getResponseId(TLActionResponse response) {
		List<Integer> statusCodes = new ArrayList<>( response.getStatusCodes() );
		StringBuilder id = new StringBuilder();
		boolean first = true;
		
		Collections.sort( statusCodes );
		
		for (Integer statusCode : statusCodes) {
			if (!first) id.append(", ");
			id.append( statusCode );
			first = false;
		}
		return id.toString();
	}
	
	/**
	 * Returns the action's response that matches the identifer provided.
	 * 
	 * @param action  the action from which to return a response
	 * @param responseId  the unique identifier of the response to return
	 * @return TLActionResponse
	 */
	private TLActionResponse getResponse(TLAction action, String responseId) {
		TLActionResponse result = null;
		
		for (TLActionResponse response : action.getResponses()) {
			if (responseId.equals( getResponseId( response ) )) {
				result = response;
				break;
			}
		}
		return result;
	}
	
	/**
	 * Returns a list of string representations for the given list of objects.
	 * 
	 * @param objList  the list of objects for which to return the string equivalents
	 * @return List<String>
	 */
	private List<String> getStrings(List<?> objList) {
		List<String> labels = new ArrayList<>();
		
		for (Object obj : objList) {
			labels.add( (obj == null) ? "" : obj.toString() );
		}
		return labels;
	}
}

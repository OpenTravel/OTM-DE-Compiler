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

import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResourceParentRef;

/**
 * Describes a single change identified during comparison of two OTM resources or one
 * of their constituent components.
 */
public class ResourceChangeItem extends ChangeItem<ResourceChangeType> {
	
	private TLResourceParentRef addedParentRef;
	private TLResourceParentRef deletedParentRef;
	private ResourceParentRefChangeSet modifiedParentRef;
	private TLParamGroup addedParamGroup;
	private TLParamGroup deletedParamGroup;
	private ResourceParamGroupChangeSet modifiedParamGroup;
	private TLParameter addedParameter;
	private TLParameter deletedParameter;
	private ResourceParameterChangeSet modifiedParameter;
	private TLActionFacet addedActionFacet;
	private TLActionFacet deletedActionFacet;
	private EntityChangeSet modifiedActionFacet;
	private TLAction addedAction;
	private TLAction deletedAction;
	private ResourceActionChangeSet modifiedAction;
	private TLActionResponse addedActionResponse;
	private TLActionResponse deletedActionResponse;
	private ResourceActionResponseChangeSet modifiedActionResponse;
	
	/**
	 * Constructor used when a parent reference was added or deleted from its owning resource.
	 * 
	 * @param changeType  the type of resource change
	 * @param affectedParentRef  the parent reference that was added or removed
	 */
	public ResourceChangeItem(ResourceChangeType changeType, TLResourceParentRef affectedParentRef) {
		this.changeType = changeType;
		
		switch (changeType) {
			case PARENT_REF_ADDED:
				this.addedParentRef = affectedParentRef;
				break;
			case PARENT_REF_DELETED:
				this.deletedParentRef = affectedParentRef;
				break;
			default:
				throw new IllegalArgumentException("Illegal change type for parent reference addition or deletion: " + changeType);
		}
	}
	
	/**
	 * Constructor used when a resource parent reference was modified.
	 * 
	 * @param modifiedParentRef  the change set for a modified parent reference
	 */
	public ResourceChangeItem(ResourceParentRefChangeSet modifiedParentRef) {
		this.changeType = ResourceChangeType.PARENT_REF_CHANGED;
		this.modifiedParentRef = modifiedParentRef;
	}

	/**
	 * Constructor used when a parent reference was added or deleted from its owning resource.
	 * 
	 * @param changeType  the type of resource change
	 * @param affectedParentRef  the parameter group that was added or removed
	 */
	public ResourceChangeItem(ResourceChangeType changeType, TLParamGroup affectedParamGroup) {
		this.changeType = changeType;
		
		switch (changeType) {
			case PARAM_GROUP_ADDED:
				this.addedParamGroup = affectedParamGroup;
				break;
			case PARAM_GROUP_DELETED:
				this.deletedParamGroup = affectedParamGroup;
				break;
			default:
				throw new IllegalArgumentException("Illegal change type for parameter group addition or deletion: " + changeType);
		}
	}
	
	/**
	 * Constructor used when a resource parameter group was modified.
	 * 
	 * @param modifiedParamGroup  the change set for a modified parameter group
	 */
	public ResourceChangeItem(ResourceParamGroupChangeSet modifiedParamGroup) {
		this.changeType = ResourceChangeType.PARAM_GROUP_CHANGED;
		this.modifiedParamGroup = modifiedParamGroup;
	}

	/**
	 * Constructor used when a parameter was added or deleted from its owning resource.
	 * 
	 * @param changeType  the type of resource change
	 * @param affectedParameter  the parameter that was added or removed
	 */
	public ResourceChangeItem(ResourceChangeType changeType, TLParameter affectedParameter) {
		this.changeType = changeType;
		
		switch (changeType) {
			case PARAMETER_ADDED:
				this.addedParameter = affectedParameter;
				break;
			case PARAMETER_DELETED:
				this.deletedParameter = affectedParameter;
				break;
			default:
				throw new IllegalArgumentException("Illegal change type for parameter addition or deletion: " + changeType);
		}
	}
	
	/**
	 * Constructor used when a resource parameter was modified.
	 * 
	 * @param modifiedParameter  the change set for a modified parameter
	 */
	public ResourceChangeItem(ResourceParameterChangeSet modifiedParameter) {
		this.changeType = ResourceChangeType.PARAMETER_CHANGED;
		this.modifiedParameter = modifiedParameter;
	}

	/**
	 * Constructor used when a action facet was added or deleted from its owning resource.
	 * 
	 * @param changeType  the type of resource change
	 * @param affectedActionFacet  the action facet that was added or removed
	 */
	public ResourceChangeItem(ResourceChangeType changeType, TLActionFacet affectedActionFacet) {
		this.changeType = changeType;
		
		switch (changeType) {
			case ACTION_FACET_ADDED:
				this.addedActionFacet = affectedActionFacet;
				break;
			case ACTION_FACET_DELETED:
				this.deletedActionFacet = affectedActionFacet;
				break;
			default:
				throw new IllegalArgumentException("Illegal change type for action facet addition or deletion: " + changeType);
		}
	}
	
	/**
	 * Constructor used when a resource action facet was modified.
	 * 
	 * @param modifiedActionFacet  the change set for a modified action facet
	 */
	public ResourceChangeItem(EntityChangeSet modifiedActionFacet) {
		this.changeType = ResourceChangeType.ACTION_FACET_CHANGED;
		this.modifiedActionFacet = modifiedActionFacet;
	}

	/**
	 * Constructor used when an action was added or deleted from its owning resource.
	 * 
	 * @param changeType  the type of resource change
	 * @param affectedAction  the action that was added or removed
	 */
	public ResourceChangeItem(ResourceChangeType changeType, TLAction affectedAction) {
		this.changeType = changeType;
		
		switch (changeType) {
			case ACTION_ADDED:
				this.addedAction = affectedAction;
				break;
			case ACTION_DELETED:
				this.deletedAction = affectedAction;
				break;
			default:
				throw new IllegalArgumentException("Illegal change type for action addition or deletion: " + changeType);
		}
	}
	
	/**
	 * Constructor used when a resource action was modified.
	 * 
	 * @param modifiedAction  the change set for a modified action
	 */
	public ResourceChangeItem(ResourceActionChangeSet modifiedAction) {
		this.changeType = ResourceChangeType.ACTION_CHANGED;
		this.modifiedAction = modifiedAction;
	}

	/**
	 * Constructor used when an action response was added or deleted from its owning resource.
	 * 
	 * @param changeType  the type of resource change
	 * @param affectedActionResponse  the action response that was added or removed
	 */
	public ResourceChangeItem(ResourceChangeType changeType, TLActionResponse affectedActionResponse) {
		this.changeType = changeType;
		
		switch (changeType) {
			case RESPONSE_ADDED:
				this.addedActionResponse = affectedActionResponse;
				break;
			case RESPONSE_DELETED:
				this.deletedActionResponse = affectedActionResponse;
				break;
			default:
				throw new IllegalArgumentException("Illegal change type for action response addition or deletion: " + changeType);
		}
	}
	
	/**
	 * Constructor used when an action response was modified.
	 * 
	 * @param modifiedActionResponse  the change set for a modified action response
	 */
	public ResourceChangeItem(ResourceActionResponseChangeSet modifiedActionResponse) {
		this.changeType = ResourceChangeType.RESPONSE_CHANGED;
		this.modifiedActionResponse = modifiedActionResponse;
	}

	/**
	 * Constructor used when a resource value was changed.
	 * 
	 * @param changeType  the type of entity change
	 * @param oldValue  the affected value from the old version
	 * @param newValue  the affected value from the new version
	 */
	public ResourceChangeItem(ResourceChangeType changeType, String oldValue, String newValue) {
		this.changeType = changeType;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * Returns the parent reference that was added.
	 *
	 * @return TLResourceParentRef
	 */
	public TLResourceParentRef getAddedParentRef() {
		return addedParentRef;
	}

	/**
	 * Returns the parent reference that was deleted.
	 *
	 * @return TLResourceParentRef
	 */
	public TLResourceParentRef getDeletedParentRef() {
		return deletedParentRef;
	}

	/**
	 * Returns the change set of the parent reference that was modified.
	 *
	 * @return ResourceParentRefChangeSet
	 */
	public ResourceParentRefChangeSet getModifiedParentRef() {
		return modifiedParentRef;
	}

	/**
	 * Returns the parameter group that was added.
	 *
	 * @return TLParamGroup
	 */
	public TLParamGroup getAddedParamGroup() {
		return addedParamGroup;
	}

	/**
	 * Returns the parameter group that was deleted.
	 *
	 * @return TLParamGroup
	 */
	public TLParamGroup getDeletedParamGroup() {
		return deletedParamGroup;
	}

	/**
	 * Returns the change set of the parameter group that was modified.
	 *
	 * @return ResourceParamGroupChangeSet
	 */
	public ResourceParamGroupChangeSet getModifiedParamGroup() {
		return modifiedParamGroup;
	}
	
	/**
	 * Returns the parameter that was added.
	 *
	 * @return TLParameter
	 */
	public TLParameter getAddedParam() {
		return addedParameter;
	}

	/**
	 * Returns the parameter that was deleted.
	 *
	 * @return TLParameter
	 */
	public TLParameter getDeletedParam() {
		return deletedParameter;
	}

	/**
	 * Returns the change set of the parameter that was modified.
	 *
	 * @return ResourceParameterChangeSet
	 */
	public ResourceParameterChangeSet getModifiedParam() {
		return modifiedParameter;
	}

	/**
	 * Returns the action facet that was added.
	 *
	 * @return TLActionFacet
	 */
	public TLActionFacet getAddedActionFacet() {
		return addedActionFacet;
	}

	/**
	 * Returns the action facet that was deleted.
	 *
	 * @return TLActionFacet
	 */
	public TLActionFacet getDeletedActionFacet() {
		return deletedActionFacet;
	}

	/**
	 * Returns the change set of the action facet that was modified.
	 *
	 * @return ResourceActionFacetChangeSet
	 */
	public EntityChangeSet getModifiedActionFacet() {
		return modifiedActionFacet;
	}

	/**
	 * Returns the action that was added.
	 *
	 * @return TLAction
	 */
	public TLAction getAddedAction() {
		return addedAction;
	}

	/**
	 * Returns the action that was deleted.
	 *
	 * @return TLAction
	 */
	public TLAction getDeletedAction() {
		return deletedAction;
	}

	/**
	 * Returns the change set of the action that was modified.
	 *
	 * @return ResourceActionChangeSet
	 */
	public ResourceActionChangeSet getModifiedAction() {
		return modifiedAction;
	}

	/**
	 * Returns the action response that was added.
	 *
	 * @return TLActionResponse
	 */
	public TLActionResponse getAddedActionResponse() {
		return addedActionResponse;
	}

	/**
	 * Returns the action response that was deleted.
	 *
	 * @return TLActionResponse
	 */
	public TLActionResponse getDeletedActionResponse() {
		return deletedActionResponse;
	}

	/**
	 * Returns the change set of the action response that was modified.
	 *
	 * @return ResourceActionResponseChangeSet
	 */
	public ResourceActionResponseChangeSet getModifiedActionResponse() {
		return modifiedActionResponse;
	}

}

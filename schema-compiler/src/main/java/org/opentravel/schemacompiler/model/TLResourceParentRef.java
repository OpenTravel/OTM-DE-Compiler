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

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;

/**
 * Defines a parent reference relationship between two resource entities.
 * 
 * @author S. Livezey
 */
public class TLResourceParentRef extends TLModelElement implements LibraryElement, TLDocumentationOwner {
	
	private TLResource owner;
	private TLResource parentResource;
	private String parentResourceName;
	private TLParamGroup parentParamGroup;
	private String parentParamGroupName;
	private String pathTemplate;
	private TLDocumentation documentation;
	
	/**
	 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		StringBuilder identity = new StringBuilder();
		
		if (owner != null) {
			identity.append( owner.getValidationIdentity() ).append( "/" );
		}
		identity.append( "Parent-Ref/" );
		
		if (parentResource == null) {
			if (parentResourceName == null) {
				identity.append( "[Unspecified Parent Resource]" );
			} else {
				identity.append( parentResourceName );
			}
		} else {
			identity.append( parentResource.getName() );
		}
		identity.append( "-" );
		
		if (parentParamGroup == null) {
			if (parentParamGroupName == null) {
				identity.append( "[Unspecified Param Group]" );
			} else {
				identity.append( parentParamGroupName );
			}
		} else {
			identity.append( parentParamGroup.getName() );
		}
		return identity.toString();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
	 */
	@Override
	public AbstractLibrary getOwningLibrary() {
		return (owner == null) ? null : owner.getOwningLibrary();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
	 */
	@Override
	public TLModel getOwningModel() {
		return (owner == null) ? null : owner.getOwningModel();
	}
	
	/**
	 * Returns the value of the 'owner' field.
	 *
	 * @return TLResource
	 */
	public TLResource getOwner() {
		return owner;
	}
	
	/**
	 * Assigns the value of the 'owner' field.
	 *
	 * @param owner the field value to assign
	 */
	public void setOwner(TLResource owner) {
		this.owner = owner;
	}
	
	/**
	 * Returns the value of the 'parentResource' field.
	 *
	 * @return TLResource
	 */
	public TLResource getParentResource() {
		return parentResource;
	}
	
	/**
	 * Assigns the value of the 'parentResource' field.
	 *
	 * @param parentResource the field value to assign
	 */
	public void setParentResource(TLResource parentResource) {
		ModelEvent<?> event = new ModelEventBuilder( ModelEventType.PARENT_RESOURCE_MODIFIED, this )
			.setOldValue( this.parentResource ).setNewValue( parentResource ).buildEvent();
		
		this.parentResource = parentResource;
		publishEvent( event );
	}
	
	/**
	 * Returns the value of the 'parentResourceName' field.
	 *
	 * @return String
	 */
	public String getParentResourceName() {
		return parentResourceName;
	}
	
	/**
	 * Assigns the value of the 'parentResourceName' field.
	 *
	 * @param parentResourceName the field value to assign
	 */
	public void setParentResourceName(String parentResourceName) {
		this.parentResourceName = parentResourceName;
	}
	
	/**
	 * Returns the value of the 'parentParamGroup' field.
	 *
	 * @return TLParamGroup
	 */
	public TLParamGroup getParentParamGroup() {
		return parentParamGroup;
	}
	
	/**
	 * Assigns the value of the 'parentParamGroup' field.
	 *
	 * @param parentParamGroup the field value to assign
	 */
	public void setParentParamGroup(TLParamGroup parentParamGroup) {
		ModelEvent<?> event = new ModelEventBuilder( ModelEventType.PARENT_PARAM_GROUP_MODIFIED, this )
			.setOldValue( this.parentParamGroup ).setNewValue( parentParamGroup ).buildEvent();
		
		this.parentParamGroupName = (parentParamGroup == null) ? null : parentParamGroup.getName();
		this.parentParamGroup = parentParamGroup;
		publishEvent( event );
	}
	
	/**
	 * Returns the value of the 'parentParamGroupName' field.
	 *
	 * @return String
	 */
	public String getParentParamGroupName() {
		return parentParamGroupName;
	}
	
	/**
	 * Assigns the value of the 'parentParamGroupName' field.
	 *
	 * @param parentParamGroupName the field value to assign
	 */
	public void setParentParamGroupName(String parentParamGroupName) {
		this.parentParamGroupName = parentParamGroupName;
	}
	
	/**
	 * Returns the value of the 'pathTemplate' field.
	 *
	 * @return String
	 */
	public String getPathTemplate() {
		return pathTemplate;
	}
	
	/**
	 * Assigns the value of the 'pathTemplate' field.
	 *
	 * @param pathTemplate the field value to assign
	 */
	public void setPathTemplate(String pathTemplate) {
		ModelEvent<?> event = new ModelEventBuilder( ModelEventType.PATH_TEMPLATE_MODIFIED, this )
			.setOldValue( this.pathTemplate ).setNewValue( pathTemplate ).buildEvent();
		
		this.pathTemplate = pathTemplate;
		publishEvent( event );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLDocumentationOwner#getDocumentation()
	 */
	public TLDocumentation getDocumentation() {
		return documentation;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLDocumentationOwner#setDocumentation(org.opentravel.schemacompiler.model.TLDocumentation)
	 */
	public void setDocumentation(TLDocumentation documentation) {
		if (documentation != this.documentation) {
			ModelEvent<?> event = new ModelEventBuilder( ModelEventType.DOCUMENTATION_MODIFIED, this )
				.setOldValue( this.documentation ).setNewValue( documentation ).buildEvent();
			
			if (documentation != null) {
				documentation.setOwner( this );
			}
			if (this.documentation != null) {
				this.documentation.setOwner( null );
			}
			this.documentation = documentation;
			publishEvent( event );
		}
	}
	
	/**
	 * Manages lists of <code>TLResourceParentRef</code> entities.
	 * 
	 * @author S. Livezey
	 */
	protected static class ResourceParentRefListManager extends ChildEntityListManager<TLResourceParentRef,TLResource> {
		
		/**
		 * Constructor that specifies the owner of the unerlying list.
		 * 
		 * @param owner the owner of the underlying list of children
		 */
		public ResourceParentRefListManager(TLResource owner) {
			super( owner, ModelEventType.PARAMETER_ADDED, ModelEventType.PARAMETER_REMOVED );
		}
		
		/**
		 * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
		 */
		@Override
		protected String getChildName(TLResourceParentRef child) {
			StringBuilder childName = new StringBuilder();
			
			childName.append( (child.getParentResource() == null) ? "Unknown" : child.getParentResource().getName() );
			childName.append( "/" );
			childName
				.append( (child.getParentParamGroup() == null) ? "Unknown" : child.getParentParamGroup().getName() );
			return childName.toString();
		}
		
		/**
		 * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
		 *      java.lang.Object)
		 */
		@Override
		protected void assignOwner(TLResourceParentRef child, TLResource owner) {
			child.setOwner( owner );
		}
		
		/**
		 * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
		 *      org.opentravel.schemacompiler.event.ModelEvent)
		 */
		@Override
		protected void publishEvent(TLResource owner, ModelEvent<?> event) {
			TLModel owningModel = owner.getOwningModel();
			
			if (owningModel != null) {
				owningModel.publishEvent( event );
			}
		}
		
	}
	
}

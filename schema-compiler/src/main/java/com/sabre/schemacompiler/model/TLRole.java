/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.model;

import com.sabre.schemacompiler.event.ModelEvent;
import com.sabre.schemacompiler.event.ModelEventBuilder;
import com.sabre.schemacompiler.event.ModelEventType;

/**
 * Role definition for <code>TLCoreObject</code> library members.
 * 
 * @author S. Livezey
 */
public class TLRole extends TLModelElement implements TLDocumentationOwner {
	
	private TLRoleEnumeration owningEnum;
	private String name;
	private TLDocumentation documentation;
	
	/**
	 * @see com.sabre.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		TLCoreObject owningCore = (owningEnum == null) ? null : owningEnum.getOwningEntity();
		StringBuilder identity = new StringBuilder();
		
		if (owningCore != null) {
			identity.append(owningCore.getValidationIdentity()).append(" : ");
		}
		if (getName() == null) {
			identity.append("[Unnamed Role]");
		} else {
			identity.append(getName());
		}
		return identity.toString();
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.ModelElement#getOwningModel()
	 */
	@Override
	public TLModel getOwningModel() {
		return (owningEnum == null) ? null : owningEnum.getOwningModel();
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.LibraryElement#getOwningLibrary()
	 */
	@Override
	public AbstractLibrary getOwningLibrary() {
		return (owningEnum == null) ? null : owningEnum.getOwningLibrary();
	}

	/**
	 * Returns the role enumeration that owns this role instance.
	 *
	 * @return TLRoleEnumeration
	 */
	public TLRoleEnumeration getRoleEnumeration() {
		return owningEnum;
	}

	/**
	 * Assigns the role enumeration that owns this role instance.
	 *
	 * @param owningEnum  the role enumeration owner to assign
	 */
	public void setRoleEnumeration(TLRoleEnumeration owningEnum) {
		this.owningEnum = owningEnum;
	}

	/**
	 * Moves this role up by one position in the list of roles maintained by its
	 * owner.  If the owner is null, or this role is already at the front of the list, this
	 * method has no effect.
	 */
	public void moveUp() {
		if (owningEnum != null) {
			owningEnum.moveUp(this);
		}
	}

	/**
	 * Moves this role down by one position in the list of roles maintained by its
	 * owner.  If the owner is null, or this role is already at the end of the list, this
	 * method has no effect.
	 */
	public void moveDown() {
		if (owningEnum != null) {
			owningEnum.moveDown(this);
		}
	}

	/**
	 * Returns the value of the 'name' field.
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Assigns the value of the 'name' field.
	 *
	 * @param name  the field value to assign
	 */
	public void setName(String name) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.NAME_MODIFIED, this)
				.setOldValue(this.name).setNewValue(name).buildEvent();

		this.name = name;
		publishEvent(event);
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLDocumentationOwner#getDocumentation()
	 */
	public TLDocumentation getDocumentation() {
		return documentation;
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLDocumentationOwner#setDocumentation(com.sabre.schemacompiler.model.TLDocumentation)
	 */
	public void setDocumentation(TLDocumentation documentation) {
		if (documentation != this.documentation) {
			ModelEvent<?> event = new ModelEventBuilder(ModelEventType.DOCUMENTATION_MODIFIED, this)
				.setOldValue(this.documentation).setNewValue(documentation).buildEvent();
			
			if (documentation != null) {
				documentation.setOwner(this);
			}
			if (this.documentation != null) {
				this.documentation.setOwner(null);
			}
			this.documentation = documentation;
			publishEvent(event);
		}
	}

	/**
	 * Manages lists of <code>TLRole</code> entities.
	 *
	 * @author S. Livezey
	 */
	protected static class RoleListManager extends ChildEntityListManager<TLRole,TLRoleEnumeration> {

		/**
		 * Constructor that specifies the owner of the unerlying list.
		 * 
		 * @param owner  the owner of the underlying list of children
		 */
		public RoleListManager(TLRoleEnumeration owner) {
			super(owner, ModelEventType.ROLE_ADDED, ModelEventType.ROLE_REMOVED);
		}

		/**
		 * @see com.sabre.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
		 */
		@Override
		protected String getChildName(TLRole child) {
			return child.getName();
		}

		/**
		 * @see com.sabre.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object, java.lang.Object)
		 */
		@Override
		protected void assignOwner(TLRole child, TLRoleEnumeration owner) {
			child.setRoleEnumeration(owner);
		}

		/**
		 * @see com.sabre.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object, com.sabre.schemacompiler.event.ModelEvent)
		 */
		@Override
		protected void publishEvent(TLRoleEnumeration owner, ModelEvent<?> event) {
			TLModel owningModel = owner.getOwningModel();
			
			if (owningModel != null) {
				owningModel.publishEvent(event);
			}
		}

	}
	
}

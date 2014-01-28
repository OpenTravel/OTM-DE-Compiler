/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.model;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;

/**
 * Model element representing eqivalence information for library terms.
 * 
 * @author S. Livezey
 */
public class TLEquivalent extends TLModelElement implements TLContextReferrer {
	
	private TLEquivalentOwner owningEntity;
	private String context;
	private String description;
	
	/**
	 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		StringBuilder identity = new StringBuilder();
		
		if (owningEntity != null) {
			identity.append(owningEntity.getValidationIdentity()).append(" : ");
		}
		if (context == null) {
			identity.append("[Unnamed Equivalence]");
		} else {
			identity.append("Equivalent:").append(context);
		}
		return identity.toString();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
	 */
	@Override
	public TLModel getOwningModel() {
		TLModel owningModel = null;
		
		if (owningEntity instanceof TLModelElement) {
			owningModel = ((TLModelElement) owningEntity).getOwningModel();
		}
		return owningModel;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
	 */
	@Override
	public AbstractLibrary getOwningLibrary() {
		return (owningEntity == null) ? null : owningEntity.getOwningLibrary();
	}

	/**
	 * Returns the value of the 'owningEntity' field.
	 *
	 * @return TLEquivalentOwner
	 */
	public TLEquivalentOwner getOwningEntity() {
		return owningEntity;
	}

	/**
	 * Assigns the value of the 'owningEntity' field.
	 *
	 * @param owningEntity  the field value to assign
	 */
	public void setOwningEntity(TLEquivalentOwner owningEntity) {
		this.owningEntity = owningEntity;
	}

	/**
	 * Moves this equivalent up by one position in the list of equivalents maintained by its
	 * owner.  If the owner is null, or this equivalent is already at the front of the list, this
	 * method has no effect.
	 */
	public void moveUp() {
		if (owningEntity != null) {
			owningEntity.moveUp(this);
		}
	}

	/**
	 * Moves this equivalent down by one position in the list of equivalents maintained by its
	 * owner.  If the owner is null, or this equivalent is already at the end of the list, this
	 * method has no effect.
	 */
	public void moveDown() {
		if (owningEntity != null) {
			owningEntity.moveDown(this);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.model.TLContextReferrer#getContext()
	 */
	public String getContext() {
		return context;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.TLContextReferrer#setContext(java.lang.String)
	 */
	public void setContext(String context) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.CONTEXT_MODIFIED, this)
				.setOldValue(this.context).setNewValue(context).buildEvent();

		this.context = context;
		publishEvent(event);
	}

	/**
	 * Returns the value of the 'description' field.
	 *
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Assigns the value of the 'description' field.
	 *
	 * @param description  the field value to assign
	 */
	public void setDescription(String description) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.EQUIVALENT_DESCRIPTION_MODIFIED, this)
				.setOldValue(this.description).setNewValue(description).buildEvent();

		this.description = description;
		publishEvent(event);
	}
	
	/**
	 * Manages lists of <code>TLEquivalent</code> entities.
	 *
	 * @author S. Livezey
	 */
	protected static class EquivalentListManager extends ChildEntityListManager<TLEquivalent,TLEquivalentOwner> {

		/**
		 * Constructor that specifies the owner of the unerlying list.
		 * 
		 * @param owner  the owner of the underlying list of children
		 */
		public EquivalentListManager(TLEquivalentOwner owner) {
			super(owner, ModelEventType.EQUIVALENT_ADDED, ModelEventType.EQUIVALENT_REMOVED);
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
		 */
		@Override
		protected String getChildName(TLEquivalent child) {
			return child.getContext();
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object, java.lang.Object)
		 */
		@Override
		protected void assignOwner(TLEquivalent child, TLEquivalentOwner owner) {
			child.setOwningEntity(owner);
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object, org.opentravel.schemacompiler.event.ModelEvent)
		 */
		@Override
		protected void publishEvent(TLEquivalentOwner owner, ModelEvent<?> event) {
			TLModel owningModel = owner.getOwningModel();
			
			if (owningModel != null) {
				owningModel.publishEvent(event);
			}
		}

	}
	
}

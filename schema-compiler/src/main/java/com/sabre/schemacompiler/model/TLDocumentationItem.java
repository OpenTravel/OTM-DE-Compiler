/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.model;

import com.sabre.schemacompiler.event.ModelEvent;
import com.sabre.schemacompiler.event.ModelEventBuilder;
import com.sabre.schemacompiler.event.ModelEventType;

/**
 * Encapsulates a single block of documentation.
 * 
 * @author S. Livezey
 */
public class TLDocumentationItem extends TLModelElement implements LibraryElement {
	
	private TLDocumentation owningDocumentation;
	private TLDocumentationType type;
	private String text;
	
	/**
	 * @see com.sabre.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		StringBuilder identity = new StringBuilder();
		
		if (owningDocumentation != null) {
			identity.append(owningDocumentation.getValidationIdentity()).append("/");
		}
		if (type == null) {
			identity.append("[Unknown Documentation Element]");
		} else {
			identity.append(type.getDisplayIdentity());
		}
		return identity.toString();
	}

	/**
	 * @see com.sabre.schemacompiler.model.ModelElement#getOwningModel()
	 */
	@Override
	public TLModel getOwningModel() {
		return (owningDocumentation == null) ? null : owningDocumentation.getOwningModel();
	}

	/**
	 * @see com.sabre.schemacompiler.model.LibraryElement#getOwningLibrary()
	 */
	@Override
	public AbstractLibrary getOwningLibrary() {
		return (owningDocumentation == null) ? null : owningDocumentation.getOwningLibrary();
	}

	/**
	 * Returns the value of the 'owningDocumentation' field.
	 *
	 * @return TLDocumentation
	 */
	public TLDocumentation getOwningDocumentation() {
		return owningDocumentation;
	}

	/**
	 * Assigns the value of the 'owningDocumentation' field.
	 *
	 * @param owningDocumentation  the field value to assign
	 */
	public void setOwningDocumentation(TLDocumentation owningDocumentation) {
		this.owningDocumentation = owningDocumentation;
	}

	/**
	 * Returns the value of the 'type' field.
	 *
	 * @return TLDocumentationType
	 */
	public TLDocumentationType getType() {
		return type;
	}

	/**
	 * Assigns the value of the 'type' field.
	 *
	 * @param type  the field value to assign
	 */
	public void setType(TLDocumentationType type) {
		this.type = type;
	}

	/**
	 * Returns the value of the 'text' field.
	 *
	 * @return String
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Assigns the value of the 'text' field.
	 *
	 * @param text  the field value to assign
	 */
	public void setText(String description) {
		String _description = TLDocumentation.adjustStringEncoding( description );
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.DOC_TEXT_MODIFIED, this)
				.setOldValue(this.text).setNewValue(_description).buildEvent();

		this.text = _description;
		publishEvent(event);
	}
	
	/**
	 * Manages lists of <code>TLDocumentationItem</code> entities.
	 *
	 * @author S. Livezey
	 */
	protected static class DocumentationItemListManager extends ChildEntityListManager<TLDocumentationItem,TLDocumentation> {

		/**
		 * Constructor that specifies the owner of the unerlying list.
		 * 
		 * @param owner  the owner of the underlying list of children
		 * @param addEventType  the type of event to publish when a child entity is added
		 * @param removeEventType  the type of event to publish when a child entity is removed
		 */
		public DocumentationItemListManager(TLDocumentation owner, ModelEventType addEventType, ModelEventType removeEventType) {
			super(owner, addEventType, removeEventType);
		}

		/**
		 * @see com.sabre.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
		 */
		@Override
		protected String getChildName(TLDocumentationItem child) {
			return null;
		}

		/**
		 * @see com.sabre.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object, java.lang.Object)
		 */
		@Override
		protected void assignOwner(TLDocumentationItem child, TLDocumentation owner) {
			child.setOwningDocumentation(owner);
		}

		/**
		 * @see com.sabre.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object, com.sabre.schemacompiler.event.ModelEvent)
		 */
		@Override
		protected void publishEvent(TLDocumentation owner, ModelEvent<?> event) {
			if (owner != null) {
				owner.publishEvent(event);
			}
		}

	}
	
}

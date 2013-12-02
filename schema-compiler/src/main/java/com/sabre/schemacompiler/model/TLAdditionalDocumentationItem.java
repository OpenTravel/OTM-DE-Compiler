/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.model;

import com.sabre.schemacompiler.event.ModelEvent;
import com.sabre.schemacompiler.event.ModelEventBuilder;
import com.sabre.schemacompiler.event.ModelEventType;

/**
 * Encapsulates the context and actual documentation associated with an additional documentation
 * section of the model.
 * 
 * @author S. Livezey
 */
public class TLAdditionalDocumentationItem extends TLDocumentationItem implements TLContextReferrer {
	
	private String context;
	
	/**
	 * @see com.sabre.schemacompiler.model.TLContextReferrer#getContext()
	 */
	public String getContext() {
		return context;
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLContextReferrer#setContext(java.lang.String)
	 */
	public void setContext(String context) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.CONTEXT_MODIFIED, this)
				.setOldValue(this.context).setNewValue(context).buildEvent();

		this.context = context;
		publishEvent(event);
	}
	
	/**
	 * Manages lists of <code>TLAdditionalDocumentationItem</code> entities.
	 *
	 * @author S. Livezey
	 */
	protected static class AdditionalDocumentationItemListManager extends ChildEntityListManager<TLAdditionalDocumentationItem,TLDocumentation> {

		/**
		 * Constructor that specifies the owner of the unerlying list.
		 * 
		 * @param owner  the owner of the underlying list of children
		 * @param addEventType  the type of event to publish when a child entity is added
		 * @param removeEventType  the type of event to publish when a child entity is removed
		 */
		public AdditionalDocumentationItemListManager(TLDocumentation owner, ModelEventType addEventType, ModelEventType removeEventType) {
			super(owner, addEventType, removeEventType);
		}

		/**
		 * @see com.sabre.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
		 */
		@Override
		protected String getChildName(TLAdditionalDocumentationItem child) {
			return child.getContext();
		}

		/**
		 * @see com.sabre.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object, java.lang.Object)
		 */
		@Override
		protected void assignOwner(TLAdditionalDocumentationItem child, TLDocumentation owner) {
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

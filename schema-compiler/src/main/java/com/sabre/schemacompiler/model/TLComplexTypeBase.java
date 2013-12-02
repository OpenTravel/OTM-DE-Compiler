/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.model;

import java.util.Comparator;
import java.util.List;

import com.sabre.schemacompiler.event.ModelEvent;
import com.sabre.schemacompiler.event.ModelEventBuilder;
import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.model.TLEquivalent.EquivalentListManager;


/**
 * Abstract class that represents the common fields for complex object types such as
 * Core and Business Objects.
 * 
 * @author S. Livezey
 */
public abstract class TLComplexTypeBase extends LibraryMember implements TLPropertyType, TLExtensionOwner, TLDocumentationOwner, TLEquivalentOwner {
	
	private String name;
	private boolean notExtendable;
	private TLExtension extension;
	private EquivalentListManager equivalentManager = new EquivalentListManager(this);
	private TLDocumentation documentation;
	
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
	 * @see com.sabre.schemacompiler.model.NamedEntity#getLocalName()
	 */
	@Override
	public String getLocalName() {
		return name;
	}

	/**
	 * Returns the value of the 'notExtendable' field.
	 *
	 * @return boolean
	 */
	public boolean isNotExtendable() {
		return notExtendable;
	}

	/**
	 * Assigns the value of the 'notExtendable' field.
	 *
	 * @param notExtendable  the field value to assign
	 */
	public void setNotExtendable(boolean notExtendable) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.NOT_EXTENDABLE_FLAG_MODIFIED, this)
				.setOldValue(this.notExtendable).setNewValue(notExtendable).buildEvent();

		this.notExtendable = notExtendable;
		publishEvent(event);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLExtensionOwner#getExtension()
	 */
	@Override
	public TLExtension getExtension() {
		return extension;
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLExtensionOwner#setExtension(com.sabre.schemacompiler.model.TLExtension)
	 */
	@Override
	public void setExtension(TLExtension extension) {
		if (extension != this.extension) {
			// Even though there is only one extension, send to events so that all extension owners behave
			// the same (as if there is a list of multiple extensions).
			if (this.extension != null) {
				ModelEvent<?> event = new ModelEventBuilder(ModelEventType.EXTENDS_REMOVED, this)
					.setAffectedItem(this.extension).buildEvent();

				this.extension.setOwner(null);
				this.extension = null;
				publishEvent(event);
			}
			if (extension != null) {
				ModelEvent<?> event = new ModelEventBuilder(ModelEventType.EXTENDS_ADDED, this)
					.setAffectedItem(extension).buildEvent();
				
				extension.setOwner(this);
				this.extension = extension;
				publishEvent(event);
			}
		}
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLEquivalentOwner#getEquivalents()
	 */
	@Override
	public List<TLEquivalent> getEquivalents() {
		return equivalentManager.getChildren();
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLEquivalentOwner#getEquivalent(java.lang.String)
	 */
	@Override
	public TLEquivalent getEquivalent(String context) {
		return equivalentManager.getChild(context);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLEquivalentOwner#addEquivalent(com.sabre.schemacompiler.model.TLEquivalent)
	 */
	@Override
	public void addEquivalent(TLEquivalent equivalent) {
		equivalentManager.addChild(equivalent);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLEquivalentOwner#addEquivalent(int, com.sabre.schemacompiler.model.TLEquivalent)
	 */
	@Override
	public void addEquivalent(int index, TLEquivalent equivalent) {
		equivalentManager.addChild(index, equivalent);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLEquivalentOwner#removeEquivalent(com.sabre.schemacompiler.model.TLEquivalent)
	 */
	@Override
	public void removeEquivalent(TLEquivalent equivalent) {
		equivalentManager.removeChild(equivalent);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLEquivalentOwner#moveUp(com.sabre.schemacompiler.model.TLEquivalent)
	 */
	@Override
	public void moveUp(TLEquivalent equivalent) {
		equivalentManager.moveUp(equivalent);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLEquivalentOwner#moveDown(com.sabre.schemacompiler.model.TLEquivalent)
	 */
	@Override
	public void moveDown(TLEquivalent equivalent) {
		equivalentManager.moveDown(equivalent);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLEquivalentOwner#sortEquivalents(java.util.Comparator)
	 */
	@Override
	public void sortEquivalents(Comparator<TLEquivalent> comparator) {
		equivalentManager.sortChildren(comparator);
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

}

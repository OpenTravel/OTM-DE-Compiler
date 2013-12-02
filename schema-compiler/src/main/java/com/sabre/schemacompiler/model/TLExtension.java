/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.model;

import com.sabre.schemacompiler.event.ModelEvent;
import com.sabre.schemacompiler.event.ModelEventBuilder;
import com.sabre.schemacompiler.event.ModelEventType;

/**
 * Model element that indicates an "extends" between the extension owner and the entity
 * to which the extension refers.
 * 
 * @author S. Livezey
 */
public class TLExtension extends TLModelElement implements TLDocumentationOwner {
	
	private TLExtensionOwner owner;
	private NamedEntity extendsEntity;
	private String extendsEntityName;
	private TLDocumentation documentation;
	
	/**
	 * @see com.sabre.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		StringBuilder identity = new StringBuilder();
		
		if (owner != null) {
			identity.append(owner.getValidationIdentity()).append(" : ");
		} else if (extendsEntity == null) {
			identity.append("[Unspecified Extension]");
		} else {
			identity.append("Extension:").append(extendsEntity.getLocalName());
		}
		return identity.toString();
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.ModelElement#getOwningModel()
	 */
	@Override
	public TLModel getOwningModel() {
		return (owner == null) ? null : owner.getOwningModel();
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.LibraryElement#getOwningLibrary()
	 */
	@Override
	public AbstractLibrary getOwningLibrary() {
		return (owner == null) ? null : owner.getOwningLibrary();
	}
	
	/**
	 * Returns the value of the 'owner' field.
	 *
	 * @return TLExtensionOwner
	 */
	public TLExtensionOwner getOwner() {
		return owner;
	}

	/**
	 * Assigns the value of the 'owner' field.
	 *
	 * @param owner  the field value to assign
	 */
	public void setOwner(TLExtensionOwner owner) {
		this.owner = owner;
	}

	/**
	 * Returns the value of the 'extendsEntity' field.
	 *
	 * @return NamedEntity
	 */
	public NamedEntity getExtendsEntity() {
		return extendsEntity;
	}

	/**
	 * Assigns the value of the 'extendsEntity' field.
	 *
	 * @param extendsEntity  the field value to assign
	 */
	public void setExtendsEntity(NamedEntity extendsEntity) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.EXTENDS_ENTITY_MODIFIED, this)
			.setOldValue(this.extendsEntity).setNewValue(extendsEntity).buildEvent();
		
		this.extendsEntity = extendsEntity;
		publishEvent(event);
	}

	/**
	 * Returns the value of the 'extendsEntityName' field.
	 *
	 * @return String
	 */
	public String getExtendsEntityName() {
		return extendsEntityName;
	}

	/**
	 * Assigns the value of the 'extendsEntityName' field.
	 *
	 * @param extendsEntityName  the field value to assign
	 */
	public void setExtendsEntityName(String extendsEntityName) {
		this.extendsEntityName = extendsEntityName;
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLDocumentationOwner#getDocumentation()
	 */
	@Override
	public TLDocumentation getDocumentation() {
		return documentation;
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLDocumentationOwner#setDocumentation(com.sabre.schemacompiler.model.TLDocumentation)
	 */
	@Override
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

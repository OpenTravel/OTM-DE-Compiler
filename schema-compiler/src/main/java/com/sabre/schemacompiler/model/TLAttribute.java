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
import com.sabre.schemacompiler.model.TLExample.ExampleListManager;

/**
 * Attribute definition for library types.
 * 
 * @author S. Livezey
 */
public class TLAttribute extends TLModelElement implements TLDocumentationOwner, TLEquivalentOwner, TLExampleOwner {
	
	private TLAttributeOwner attributeOwner;
	private String name;
	private TLAttributeType type;
	private String typeName;
	private boolean mandatory;
	private TLDocumentation documentation;
	private EquivalentListManager equivalentManager = new EquivalentListManager(this);
	private ExampleListManager exampleManager = new ExampleListManager(this);
	
	/**
	 * @see com.sabre.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		StringBuilder identity = new StringBuilder();
		
		if (attributeOwner != null) {
			identity.append(attributeOwner.getValidationIdentity()).append("/");
		}
		if (name == null) {
			identity.append("[Unnamed Attribute]");
		} else {
			identity.append(name);
		}
		return identity.toString();
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.ModelElement#getOwningModel()
	 */
	@Override
	public TLModel getOwningModel() {
		TLModel owningModel = null;
		
		if ((attributeOwner != null) && (attributeOwner instanceof TLModelElement)) {
			owningModel = ((TLModelElement) attributeOwner).getOwningModel();
		}
		return owningModel;
	}

	/**
	 * @see com.sabre.schemacompiler.model.LibraryElement#getOwningLibrary()
	 */
	@Override
	public AbstractLibrary getOwningLibrary() {
		return (attributeOwner == null) ? null : attributeOwner.getOwningLibrary();
	}

	/**
	 * Returns the value of the 'attributeOwner' field.
	 *
	 * @return TLAttributeOwner
	 */
	public TLAttributeOwner getAttributeOwner() {
		return attributeOwner;
	}

	/**
	 * Assigns the value of the 'attributeOwner' field.
	 *
	 * @param attributeOwner  the field value to assign
	 */
	public void setAttributeOwner(TLAttributeOwner attributeOwner) {
		this.attributeOwner = attributeOwner;
	}
	
	/**
	 * Moves this attribute up by one position in the list of attributes maintained by its
	 * owner.  If the owner is null, or this attribute is already at the front of the list, this
	 * method has no effect.
	 */
	public void moveUp() {
		if (attributeOwner != null) {
			attributeOwner.moveUp(this);
		}
	}

	/**
	 * Moves this attribute down by one position in the list of attributes maintained by its
	 * owner.  If the owner is null, or this attribute is already at the end of the list, this
	 * method has no effect.
	 */
	public void moveDown() {
		if (attributeOwner != null) {
			attributeOwner.moveDown(this);
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
	 * Returns the value of the 'type' field.
	 *
	 * @return TLAttributeType
	 */
	public TLAttributeType getType() {
		return type;
	}
	
	/**
	 * Assigns the value of the 'type' field.
	 *
	 * @param type  the field value to assign
	 */
	public void setType(TLAttributeType type) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.TYPE_ASSIGNMENT_MODIFIED, this)
				.setOldValue(this.type).setNewValue(type).buildEvent();

		this.type = type;
		publishEvent(event);
	}
	
	/**
	 * Returns the value of the 'typeName' field.
	 *
	 * @return String
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * Assigns the value of the 'typeName' field.
	 *
	 * @param typeName  the field value to assign
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	/**
	 * Returns the value of the 'manditory' field.
	 *
	 * @return boolean
	 */
	public boolean isMandatory() {
		return mandatory;
	}
	
	/**
	 * Assigns the value of the 'mandatory' field.
	 *
	 * @param mandatory  the field value to assign
	 */
	public void setMandatory(boolean mandatory) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.MANDATORY_FLAG_MODIFIED, this)
				.setOldValue(this.mandatory).setNewValue(mandatory).buildEvent();

		this.mandatory = mandatory;
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
	 * @see com.sabre.schemacompiler.model.TLExampleOwner#getExamples()
	 */
	public List<TLExample> getExamples() {
		return exampleManager.getChildren();
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLExampleOwner#getExample(java.lang.String)
	 */
	public TLExample getExample(String contextId) {
		return exampleManager.getChild(contextId);
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLExampleOwner#addExample(com.sabre.schemacompiler.model.TLExample)
	 */
	public void addExample(TLExample example) {
		exampleManager.addChild(example);
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLExampleOwner#addExample(int, com.sabre.schemacompiler.model.TLExample)
	 */
	public void addExample(int index, TLExample example) {
		exampleManager.addChild(index, example);
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLExampleOwner#removeExample(com.sabre.schemacompiler.model.TLExample)
	 */
	public void removeExample(TLExample example) {
		exampleManager.removeChild(example);
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLExampleOwner#moveUp(com.sabre.schemacompiler.model.TLExample)
	 */
	public void moveUp(TLExample example) {
		exampleManager.moveUp(example);
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLExampleOwner#moveDown(com.sabre.schemacompiler.model.TLExample)
	 */
	public void moveDown(TLExample example) {
		exampleManager.moveDown(example);
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLExampleOwner#sortExamples(java.util.Comparator)
	 */
	public void sortExamples(Comparator<TLExample> comparator) {
		exampleManager.sortChildren(comparator);
	}
	
	/**
	 * Manages lists of <code>TLAttribute</code> entities.
	 *
	 * @author S. Livezey
	 */
	protected static class AttributeListManager extends ChildEntityListManager<TLAttribute,TLAttributeOwner> {

		/**
		 * Constructor that specifies the owner of the unerlying list.
		 * 
		 * @param owner  the owner of the underlying list of children
		 */
		public AttributeListManager(TLAttributeOwner owner) {
			super(owner, ModelEventType.ATTRIBUTE_ADDED, ModelEventType.ATTRIBUTE_REMOVED);
		}

		/**
		 * @see com.sabre.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
		 */
		@Override
		protected String getChildName(TLAttribute child) {
			return child.getName();
		}

		/**
		 * @see com.sabre.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object, java.lang.Object)
		 */
		@Override
		protected void assignOwner(TLAttribute child, TLAttributeOwner owner) {
			child.setAttributeOwner(owner);
		}

		/**
		 * @see com.sabre.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object, com.sabre.schemacompiler.event.ModelEvent)
		 */
		@Override
		protected void publishEvent(TLAttributeOwner owner, ModelEvent<?> event) {
			TLModel owningModel = owner.getOwningModel();
			
			if (owningModel != null) {
				owningModel.publishEvent(event);
			}
		}

	}
	
}

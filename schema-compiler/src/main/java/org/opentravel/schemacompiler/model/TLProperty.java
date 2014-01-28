/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.model;

import java.util.Comparator;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.model.TLEquivalent.EquivalentListManager;
import org.opentravel.schemacompiler.model.TLExample.ExampleListManager;

/**
 * Property definition for complex library types.
 * 
 * @author S. Livezey
 */
public class TLProperty extends TLModelElement implements TLDocumentationOwner, TLEquivalentOwner, TLExampleOwner {
	
	private TLPropertyOwner propertyOwner;
	private String name;
	private TLPropertyType type;
	private String typeName;
	private boolean isReference;
	private int repeat;
	private boolean mandatory;
	private TLDocumentation documentation;
	private EquivalentListManager equivalentManager = new EquivalentListManager(this);
	private ExampleListManager exampleManager = new ExampleListManager(this);
	
	/**
	 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		StringBuilder identity = new StringBuilder();
		
		if (propertyOwner != null) {
			identity.append(propertyOwner.getValidationIdentity()).append("/");
		}
		if (name == null) {
			identity.append("[Unnamed Element]");
		} else {
			identity.append(name);
		}
		return identity.toString();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
	 */
	@Override
	public TLModel getOwningModel() {
		return (propertyOwner == null) ? null : propertyOwner.getOwningModel();
	}

	/**
	 * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
	 */
	@Override
	public AbstractLibrary getOwningLibrary() {
		return (propertyOwner == null) ? null : propertyOwner.getOwningLibrary();
	}

	/**
	 * Returns the value of the 'propertyOwner' field.
	 *
	 * @return TLPropertyOwner
	 */
	public TLPropertyOwner getPropertyOwner() {
		return propertyOwner;
	}

	/**
	 * Assigns the value of the 'propertyOwner' field.
	 *
	 * @param propertyOwner  the field value to assign
	 */
	public void setPropertyOwner(TLPropertyOwner propertyOwner) {
		this.propertyOwner = propertyOwner;
	}

	/**
	 * Moves this element up by one position in the list of elements maintained by its
	 * owner.  If the owner is null, or this element is already at the front of the list, this
	 * method has no effect.
	 */
	public void moveUp() {
		if (propertyOwner != null) {
			propertyOwner.moveUp(this);
		}
	}

	/**
	 * Moves this element down by one position in the list of elements maintained by its
	 * owner.  If the owner is null, or this element is already at the end of the list, this
	 * method has no effect.
	 */
	public void moveDown() {
		if (propertyOwner != null) {
			propertyOwner.moveDown(this);
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
	 * @return TLPropertyType
	 */
	public TLPropertyType getType() {
		return type;
	}
	
	/**
	 * Assigns the value of the 'type' field.
	 *
	 * @param type  the field value to assign
	 */
	public void setType(TLPropertyType type) {
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
	 * Returns the value of the 'isReference' field.
	 *
	 * @return boolean
	 */
	public boolean isReference() {
		return isReference;
	}

	/**
	 * Assigns the value of the 'isReference' field.
	 *
	 * @param isReference  the field value to assign
	 */
	public void setReference(boolean isReference) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.REFERENCE_FLAG_MODIFIED, this)
				.setOldValue(this.isReference).setNewValue(isReference).buildEvent();

		this.isReference = isReference;
		publishEvent(event);
	}

	/**
	 * Returns the value of the 'repeat' field.
	 *
	 * @return int
	 */
	public int getRepeat() {
		return repeat;
	}
	
	/**
	 * Assigns the value of the 'repeat' field.
	 *
	 * @param repeat  the field value to assign
	 */
	public void setRepeat(int repeat) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.REPEAT_MODIFIED, this)
				.setOldValue(this.repeat).setNewValue(repeat).buildEvent();

		this.repeat = repeat;
		publishEvent(event);
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
	 * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#getEquivalents()
	 */
	@Override
	public List<TLEquivalent> getEquivalents() {
		return equivalentManager.getChildren();
	}

	/**
	 * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#getEquivalent(java.lang.String)
	 */
	@Override
	public TLEquivalent getEquivalent(String context) {
		return equivalentManager.getChild(context);
	}

	/**
	 * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#addEquivalent(org.opentravel.schemacompiler.model.TLEquivalent)
	 */
	@Override
	public void addEquivalent(TLEquivalent equivalent) {
		equivalentManager.addChild(equivalent);
	}

	/**
	 * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#addEquivalent(int, org.opentravel.schemacompiler.model.TLEquivalent)
	 */
	@Override
	public void addEquivalent(int index, TLEquivalent equivalent) {
		equivalentManager.addChild(index, equivalent);
	}

	/**
	 * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#removeEquivalent(org.opentravel.schemacompiler.model.TLEquivalent)
	 */
	@Override
	public void removeEquivalent(TLEquivalent equivalent) {
		equivalentManager.removeChild(equivalent);
	}

	/**
	 * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#moveUp(org.opentravel.schemacompiler.model.TLEquivalent)
	 */
	@Override
	public void moveUp(TLEquivalent equivalent) {
		equivalentManager.moveUp(equivalent);
	}

	/**
	 * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#moveDown(org.opentravel.schemacompiler.model.TLEquivalent)
	 */
	@Override
	public void moveDown(TLEquivalent equivalent) {
		equivalentManager.moveDown(equivalent);
	}

	/**
	 * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#sortEquivalents(java.util.Comparator)
	 */
	@Override
	public void sortEquivalents(Comparator<TLEquivalent> comparator) {
		equivalentManager.sortChildren(comparator);
	}

	/**
	 * @see org.opentravel.schemacompiler.model.TLExampleOwner#getExamples()
	 */
	public List<TLExample> getExamples() {
		return exampleManager.getChildren();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLExampleOwner#getExample(java.lang.String)
	 */
	public TLExample getExample(String contextId) {
		return exampleManager.getChild(contextId);
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLExampleOwner#addExample(org.opentravel.schemacompiler.model.TLExample)
	 */
	public void addExample(TLExample example) {
		exampleManager.addChild(example);
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLExampleOwner#addExample(int, org.opentravel.schemacompiler.model.TLExample)
	 */
	public void addExample(int index, TLExample example) {
		exampleManager.addChild(index, example);
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLExampleOwner#removeExample(org.opentravel.schemacompiler.model.TLExample)
	 */
	public void removeExample(TLExample example) {
		exampleManager.removeChild(example);
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLExampleOwner#moveUp(org.opentravel.schemacompiler.model.TLExample)
	 */
	public void moveUp(TLExample example) {
		exampleManager.moveUp(example);
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLExampleOwner#moveDown(org.opentravel.schemacompiler.model.TLExample)
	 */
	public void moveDown(TLExample example) {
		exampleManager.moveDown(example);
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.TLExampleOwner#sortExamples(java.util.Comparator)
	 */
	public void sortExamples(Comparator<TLExample> comparator) {
		exampleManager.sortChildren(comparator);
	}
	
	/**
	 * Manages lists of <code>TLProperty</code> entities.
	 *
	 * @author S. Livezey
	 */
	protected static class PropertyListManager extends ChildEntityListManager<TLProperty,TLPropertyOwner> {

		/**
		 * Constructor that specifies the owner of the unerlying list.
		 * 
		 * @param owner  the owner of the underlying list of children
		 */
		public PropertyListManager(TLPropertyOwner owner) {
			super(owner, ModelEventType.PROPERTY_ADDED, ModelEventType.PROPERTY_REMOVED);
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
		 */
		@Override
		protected String getChildName(TLProperty child) {
			return child.getName();
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object, java.lang.Object)
		 */
		@Override
		protected void assignOwner(TLProperty child, TLPropertyOwner owner) {
			child.setPropertyOwner(owner);
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object, org.opentravel.schemacompiler.event.ModelEvent)
		 */
		@Override
		protected void publishEvent(TLPropertyOwner owner, ModelEvent<?> event) {
			if ((owner != null) && (owner.getOwningModel() != null)) {
				owner.getOwningModel().publishEvent(event);
			}
		}

	}
	
}

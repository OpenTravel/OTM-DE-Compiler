/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.model;

import java.util.Comparator;
import java.util.List;

import com.sabre.schemacompiler.event.ModelEvent;
import com.sabre.schemacompiler.event.ModelEventBuilder;
import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.model.TLEnumValue.EnumValueListManager;

/**
 * Abstract base class for the open and closed enumeration types.
 * 
 * @author S. Livezey
 */
public abstract class TLAbstractEnumeration extends LibraryMember implements TLDocumentationOwner {
	
	private String name;
	private TLDocumentation documentation;
	private EnumValueListManager enumValueManager = new EnumValueListManager(this);
	
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
	 * Returns the value of the 'values' field.
	 *
	 * @return List<TLEnumValue>
	 */
	public List<TLEnumValue> getValues() {
		return enumValueManager.getChildren();
	}

	/**
	 * Adds a <code>TLEnumValue</code> to the current list.
	 * 
	 * @param value  the enumeration value to add
	 */
	public void addValue(TLEnumValue value) {
		enumValueManager.addChild(value);
	}

	/**
	 * Adds a <code>TLEnumValue</code> element to the current list.
	 * 
	 * @param index  the index at which the given indicator should be added
	 * @param enumeration  the enumeration value to add
	 * @throws IndexOutOfBoundsException  thrown if the index is out of range (index < 0 || index > size())
	 */
	public void addValue(int index, TLEnumValue value) {
		enumValueManager.addChild(index, value);
	}
	
	/**
	 * Removes a <code>TLEnumValue</code> from the current list.
	 * 
	 * @param value  the enumeration value to remove
	 */
	public void removeValue(TLEnumValue value) {
		enumValueManager.removeChild(value);
	}
	
	/**
	 * Moves this value up by one position in the list.  If the value is not owned by this
	 * object or it is already at the front of the list, this method has no effect.
	 * 
	 * @param value  the value to move
	 */
	public void moveUp(TLEnumValue value) {
		enumValueManager.moveUp(value);
	}
	
	/**
	 * Moves this value down by one position in the list.  If the value is not owned by this
	 * object or it is already at the end of the list, this method has no effect.
	 * 
	 * @param value  the value to move
	 */
	public void moveDown(TLEnumValue value) {
		enumValueManager.moveDown(value);
	}
	
	/**
	 * Sorts the list of values using the comparator provided.
	 * 
	 * @param comparator  the comparator to use when sorting the list
	 */
	public void sortValues(Comparator<TLEnumValue> comparator) {
		enumValueManager.sortChildren(comparator);
	}
	
}

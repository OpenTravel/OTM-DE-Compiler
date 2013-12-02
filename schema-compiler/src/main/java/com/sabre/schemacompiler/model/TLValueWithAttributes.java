/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.model;

import java.util.Comparator;
import java.util.List;

import com.sabre.schemacompiler.event.ModelEvent;
import com.sabre.schemacompiler.event.ModelEventBuilder;
import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.model.TLAttribute.AttributeListManager;
import com.sabre.schemacompiler.model.TLEquivalent.EquivalentListManager;
import com.sabre.schemacompiler.model.TLExample.ExampleListManager;
import com.sabre.schemacompiler.model.TLIndicator.IndicatorListManager;
import com.sabre.schemacompiler.version.Versioned;

/**
 * Simple schema type with attributes.
 * 
 * @author S. Livezey
 */
public class TLValueWithAttributes extends LibraryMember implements Versioned, TLAttributeType, TLAttributeOwner,
				TLIndicatorOwner, TLDocumentationOwner, TLEquivalentOwner, TLExampleOwner {
	
	private String name;
	private TLDocumentation documentation;
	private TLDocumentation valueDocumentation;
	private TLAttributeType parentType;
	private String parentTypeName;
	private AttributeListManager attributeManager = new AttributeListManager(this);
	private IndicatorListManager indicatorManager = new IndicatorListManager(this);
	private EquivalentListManager equivalentManager = new EquivalentListManager(this);
	private ExampleListManager exampleManager = new ExampleListManager(this);
	
	/**
	 * @see com.sabre.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		AbstractLibrary owningLibrary = getOwningLibrary();
		StringBuilder identity = new StringBuilder();
		
		if (owningLibrary != null) {
			identity.append(owningLibrary.getValidationIdentity()).append(" : ");
		}
		if (name == null) {
			identity.append("[Unnamed ValueWithAttributes Type]");
		} else {
			identity.append(name);
		}
		return identity.toString();
	}
	
	/**
	 * @see com.sabre.schemacompiler.version.Versioned#getVersion()
	 */
	@Override
	public String getVersion() {
		AbstractLibrary owningLibrary = getOwningLibrary();
		String version = null;
		
		if (owningLibrary instanceof TLLibrary) {
			version = ((TLLibrary) owningLibrary).getVersion();
		}
		return version;
	}

	/**
	 * @see com.sabre.schemacompiler.version.Versioned#getVersionScheme()
	 */
	@Override
	public String getVersionScheme() {
		AbstractLibrary owningLibrary = getOwningLibrary();
		String versionScheme = null;
		
		if (owningLibrary instanceof TLLibrary) {
			versionScheme = ((TLLibrary) owningLibrary).getVersionScheme();
		}
		return versionScheme;
	}

	/**
	 * @see com.sabre.schemacompiler.version.Versioned#getBaseNamespace()
	 */
	@Override
	public String getBaseNamespace() {
		AbstractLibrary owningLibrary = getOwningLibrary();
		String baseNamespace;
		
		if (owningLibrary instanceof TLLibrary) {
			baseNamespace = ((TLLibrary) owningLibrary).getBaseNamespace();
		} else {
			baseNamespace = getNamespace();
		}
		return baseNamespace;
	}

	/**
	 * @see com.sabre.schemacompiler.version.Versioned#isLaterVersion(com.sabre.schemacompiler.version.Versioned)
	 */
	@Override
	public boolean isLaterVersion(Versioned otherVersionedItem) {
		boolean result = false;
		
		if ((otherVersionedItem != null) && otherVersionedItem.getClass().equals(this.getClass())
				&& (this.getOwningLibrary() != null) && (otherVersionedItem.getOwningLibrary() != null)
				&& (this.getLocalName() != null) && this.getLocalName().equals(otherVersionedItem.getLocalName())) {
			result = this.getOwningLibrary().isLaterVersion(otherVersionedItem.getOwningLibrary());
		}
		return result;
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
	 * @see com.sabre.schemacompiler.model.NamedEntity#getLocalName()
	 */
	@Override
	public String getLocalName() {
		return name;
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLAttributeType#getXSDFacetProfile()
	 */
	@Override
	public XSDFacetProfile getXSDFacetProfile() {
		return null;
	}

	/**
	 * Returns the documentation for the value assignment of this Value-With-Attributes.
	 *
	 * @return TLDocumentation
	 */
	public TLDocumentation getValueDocumentation() {
		return valueDocumentation;
	}

	/**
	 * Assigns the documentation for the value assignment of this Value-With-Attributes.
	 *
	 * @param valueDocumentation  the documentation instance to assign
	 */
	public void setValueDocumentation(TLDocumentation valueDocumentation) {
		if (valueDocumentation != this.valueDocumentation) {
			ModelEvent<?> event = new ModelEventBuilder(ModelEventType.VALUE_DOCUMENTATION_MODIFIED, this)
				.setOldValue(this.valueDocumentation).setNewValue(valueDocumentation).buildEvent();
			
			if (valueDocumentation != null) {
				valueDocumentation.setOwner(this);
			}
			if (this.valueDocumentation != null) {
				this.valueDocumentation.setOwner(null);
			}
			this.valueDocumentation = valueDocumentation;
			publishEvent(event);
		}
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
	 * Returns the value of the 'parentType' field.
	 *
	 * @return TLAttributeType
	 */
	public TLAttributeType getParentType() {
		return parentType;
	}
	
	/**
	 * Assigns the value of the 'parentType' field.
	 *
	 * @param parentType  the field value to assign
	 */
	public void setParentType(TLAttributeType parentType) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.TYPE_ASSIGNMENT_MODIFIED, this)
				.setOldValue(this.parentType).setNewValue(parentType).buildEvent();

		this.parentType = parentType;
		publishEvent(event);
	}
	
	/**
	 * Returns the value of the 'parentTypeName' field.
	 *
	 * @return String
	 */
	public String getParentTypeName() {
		return parentTypeName;
	}

	/**
	 * Assigns the value of the 'parentTypeName' field.
	 *
	 * @param parentTypeName  the field value to assign
	 */
	public void setParentTypeName(String parentTypeName) {
		this.parentTypeName = parentTypeName;
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLAttributeOwner#getAttributes()
	 */
	public List<TLAttribute> getAttributes() {
		return attributeManager.getChildren();
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLAttributeOwner#getAttribute(java.lang.String)
	 */
	public TLAttribute getAttribute(String attributeName) {
		return attributeManager.getChild(attributeName);
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLAttributeOwner#addAttribute(com.sabre.schemacompiler.model.TLAttribute)
	 */
	public void addAttribute(TLAttribute attribute) {
		attributeManager.addChild(attribute);
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLAttributeOwner#addAttribute(int, com.sabre.schemacompiler.model.TLAttribute)
	 */
	@Override
	public void addAttribute(int index, TLAttribute attribute) {
		attributeManager.addChild(index, attribute);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLAttributeOwner#removeAttribute(com.sabre.schemacompiler.model.TLAttribute)
	 */
	public void removeAttribute(TLAttribute attribute) {
		attributeManager.removeChild(attribute);
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLAttributeOwner#moveUp(com.sabre.schemacompiler.model.TLAttribute)
	 */
	@Override
	public void moveUp(TLAttribute attribute) {
		attributeManager.moveUp(attribute);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLAttributeOwner#moveDown(com.sabre.schemacompiler.model.TLAttribute)
	 */
	@Override
	public void moveDown(TLAttribute attribute) {
		attributeManager.moveDown(attribute);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLAttributeOwner#sortAttributes(java.util.Comparator)
	 */
	@Override
	public void sortAttributes(Comparator<TLAttribute> comparator) {
		attributeManager.sortChildren(comparator);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLIndicatorOwner#getIndicators()
	 */
	@Override
	public List<TLIndicator> getIndicators() {
		return indicatorManager.getChildren();
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLIndicatorOwner#getIndicator(java.lang.String)
	 */
	@Override
	public TLIndicator getIndicator(String indicatorName) {
		return indicatorManager.getChild(indicatorName);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLIndicatorOwner#addIndicator(com.sabre.schemacompiler.model.TLIndicator)
	 */
	@Override
	public void addIndicator(TLIndicator indicator) {
		indicatorManager.addChild(indicator);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLIndicatorOwner#addIndicator(int, com.sabre.schemacompiler.model.TLIndicator)
	 */
	@Override
	public void addIndicator(int index, TLIndicator indicator) {
		indicatorManager.addChild(index, indicator);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLIndicatorOwner#removeIndicator(com.sabre.schemacompiler.model.TLIndicator)
	 */
	@Override
	public void removeIndicator(TLIndicator indicator) {
		indicatorManager.removeChild(indicator);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLIndicatorOwner#moveUp(com.sabre.schemacompiler.model.TLIndicator)
	 */
	@Override
	public void moveUp(TLIndicator indicator) {
		indicatorManager.moveUp(indicator);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLIndicatorOwner#moveDown(com.sabre.schemacompiler.model.TLIndicator)
	 */
	@Override
	public void moveDown(TLIndicator indicator) {
		indicatorManager.moveDown(indicator);
	}

	/**
	 * @see com.sabre.schemacompiler.model.TLIndicatorOwner#sortIndicators(java.util.Comparator)
	 */
	@Override
	public void sortIndicators(Comparator<TLIndicator> comparator) {
		indicatorManager.sortChildren(comparator);
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
	
}

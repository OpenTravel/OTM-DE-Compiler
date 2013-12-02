/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.model;

import java.util.Comparator;
import java.util.List;

import javax.xml.namespace.QName;

import com.sabre.schemacompiler.event.ModelEvent;
import com.sabre.schemacompiler.event.ModelEventBuilder;
import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.ioc.SchemaDependency;
import com.sabre.schemacompiler.model.TLEquivalent.EquivalentListManager;
import com.sabre.schemacompiler.model.TLExample.ExampleListManager;


/**
 * Facet type that references a declared simple type for use as a facet on a
 * complex object.
 * 
 * @author S. Livezey
 */
public class TLSimpleFacet extends TLAbstractFacet implements TLAttributeType, TLEquivalentOwner, TLExampleOwner {
	
	private NamedEntity simpleType;
	private String simpleTypeName;
	private EquivalentListManager equivalentManager = new EquivalentListManager(this);
	private ExampleListManager exampleManager = new ExampleListManager(this);
	
	/**
	 * @see com.sabre.schemacompiler.model.TLAbstractFacet#declaresContent()
	 */
	@Override
	public boolean declaresContent() {
		return super.declaresContent() || !isEmptyType();
	}
	
	/**
	 * Returns true if the simple type assignment is null or 'ota:EMPTY', indicating that the simple facet
	 * is to be omitted from the model.
	 * 
	 * @return boolean
	 */
	public boolean isEmptyType() {
		if (simpleType == null) {
			return true;
		} else {
			QName emptyElementType = SchemaDependency.getEmptyElement().toQName();
			
			return emptyElementType.getNamespaceURI().equals(simpleType.getNamespace())
					&& emptyElementType.getLocalPart().equals(simpleType.getLocalName());
		}
	}

	/**
	 * @see com.sabre.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		TLFacetOwner owningEntity = getOwningEntity();
		TLFacetType facetType = getFacetType();
		StringBuilder identity = new StringBuilder();
		
		if (owningEntity != null) {
			identity.append(owningEntity.getValidationIdentity()).append("/");
		}
		if (facetType == null) {
			identity.append("[Unnamed Simple Facet]");
		} else {
			identity.append(facetType.getIdentityName());
		}
		return identity.toString();
	}

	/**
	 * Returns the value of the 'simpleType' field.
	 * 
	 * @return NamedEntity
	 */
	public NamedEntity getSimpleType() {
		return simpleType;
	}
	
	/**
	 * Assigns the value of the 'simpleType' field.
	 * 
	 * @param simpleType  the field value to assign
	 */
	public void setSimpleType(NamedEntity simpleType) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.TYPE_ASSIGNMENT_MODIFIED, this)
				.setOldValue(this.simpleType).setNewValue(simpleType).buildEvent();

		this.simpleType = simpleType;
		publishEvent(event);
	}
	
	/**
	 * Returns the value of the 'simpleTypeName' field.
	 * 
	 * @return String
	 */
	public String getSimpleTypeName() {
		return simpleTypeName;
	}
	
	/**
	 * Assigns the value of the 'simpleTypeName' field.
	 * 
	 * @param simpleTypeName
	 *            the field value to assign
	 */
	public void setSimpleTypeName(String simpleTypeName) {
		this.simpleTypeName = simpleTypeName;
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.TLAttributeType#getXSDFacetProfile()
	 */
	@Override
	public XSDFacetProfile getXSDFacetProfile() {
		return (simpleType instanceof TLAttributeType) ? ((TLAttributeType) simpleType).getXSDFacetProfile() : null;
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

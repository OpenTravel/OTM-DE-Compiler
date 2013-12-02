/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.model;

import com.sabre.schemacompiler.event.ModelEvent;
import com.sabre.schemacompiler.event.ModelEventBuilder;
import com.sabre.schemacompiler.event.ModelEventType;

/**
 * Abstract base class for all facet entities.
 * 
 * @author S. Livezey
 */
public abstract class TLAbstractFacet extends TLModelElement implements TLPropertyType, TLDocumentationOwner {
	
	private TLFacetOwner owningEntity;
	private TLFacetType facetType;
	private TLDocumentation documentation;
	
	/**
	 * Returns true if this facet contains any child elements that contribute to the information
	 * model.  Non-content items such as aliases, documentation, equivalents, etc. are not considered
	 * during this check.
	 * 
	 * @return boolean
	 */
	public boolean declaresContent() {
		return (facetType == TLFacetType.ID);
	}
	
	/**
	 * @see com.sabre.schemacompiler.model.ModelElement#getOwningModel()
	 */
	@Override
	public TLModel getOwningModel() {
		return (owningEntity instanceof TLModelElement) ? ((TLModelElement) owningEntity).getOwningModel() : null;
	}

	/**
	 * @see com.sabre.schemacompiler.model.NamedEntity#getOwningLibrary()
	 */
	@Override
	public AbstractLibrary getOwningLibrary() {
		TLFacetOwner owningEntity = getOwningEntity();
		
		return (owningEntity == null) ? null : owningEntity.getOwningLibrary();
	}

	/**
	 * @see com.sabre.schemacompiler.model.NamedEntity#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return (owningEntity == null) ? null : owningEntity.getNamespace();
	}

	/**
	 * @see com.sabre.schemacompiler.model.NamedEntity#getLocalName()
	 */
	@Override
	public String getLocalName() {
		StringBuilder localName = new StringBuilder();
		
		if (owningEntity != null) {
			localName.append(owningEntity.getLocalName()).append('_');
		}
		if (facetType != null) {
			localName.append(facetType.getIdentityName());
		} else {
			localName.append("Unnamed_Facet");
		}
		return localName.toString();
	}

	/**
	 * Returns the value of the 'owningEntity' field.
	 *
	 * @return TLFacetOwner
	 */
	public TLFacetOwner getOwningEntity() {
		return owningEntity;
	}

	/**
	 * Assigns the value of the 'owningEntity' field.
	 *
	 * @param owningEntity  the field value to assign
	 */
	public void setOwningEntity(TLFacetOwner owningEntity) {
		this.owningEntity = owningEntity;
	}

	/**
	 * Returns the value of the 'facetType' field.
	 *
	 * @return TLFacetType
	 */
	public TLFacetType getFacetType() {
		return facetType;
	}

	/**
	 * Assigns the value of the 'facetType' field.
	 *
	 * @param facetType  the field value to assign
	 */
	public void setFacetType(TLFacetType facetType) {
		this.facetType = facetType;
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

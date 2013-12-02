/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.ic;

import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.event.ValueChangeEvent;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.transform.SymbolResolver;
import com.sabre.schemacompiler.transform.util.ChameleonFilter;
import com.sabre.schemacompiler.transform.util.LibraryPrefixResolver;
import com.sabre.schemacompiler.validate.impl.TLModelSymbolResolver;

/**
 * Model integrity check listener, that ensures all 'typeName' fields are synchronized with the
 * type instances that are assigned to the referencing model object.
 * 
 * @author S. Livezey
 */
public class TypeNameIntegrityChecker extends AbstractIntegrityChecker<ValueChangeEvent<TLModelElement,NamedEntity>,TLModelElement> {

	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#processModelEvent(com.sabre.schemacompiler.event.ModelEvent)
	 */
	@Override
	public void processModelEvent(ValueChangeEvent<TLModelElement, NamedEntity> event) {
		if ((event.getType() == ModelEventType.TYPE_ASSIGNMENT_MODIFIED) ||
				(event.getType() == ModelEventType.EXTENDS_ENTITY_MODIFIED)) {
			TLModelElement sourceObject = event.getSource();
			String entityName = buildEntityName(event.getNewValue(), sourceObject);
			
			if (sourceObject instanceof TLSimple) {
				((TLSimple) sourceObject).setParentTypeName(entityName);
				
			} else if (sourceObject instanceof TLValueWithAttributes) {
				((TLValueWithAttributes) sourceObject).setParentTypeName(entityName);
				
			} else if (sourceObject instanceof TLSimpleFacet) {
				((TLSimpleFacet) sourceObject).setSimpleTypeName(entityName);
				
			} else if (sourceObject instanceof TLProperty) {
				((TLProperty) sourceObject).setTypeName(entityName);
				
			} else if (sourceObject instanceof TLAttribute) {
				((TLAttribute) sourceObject).setTypeName(entityName);
				
			} else if (sourceObject instanceof TLExtension) {
				((TLExtension) sourceObject).setExtendsEntityName(entityName);
			}
		}
	}
	
	/**
	 * Returns the name of the given entity as either 'prefix:localName' or simple 'localName' (if the
	 * entity is assigned to the local namespace provided).
	 * 
	 * @param assignedEntity  the entity whose name is to be returned
	 * @param sourceObject  the object to which the named entity was assigned
	 * @return String
	 */
	private String buildEntityName(NamedEntity assignedEntity, TLModelElement sourceObject) {
		String entityName = null;
		
		if (assignedEntity != null) {
			AbstractLibrary owningLibrary = getOwningLibrary(sourceObject);
			SymbolResolver symbolResolver = new TLModelSymbolResolver(sourceObject.getOwningModel());
			
			symbolResolver.setPrefixResolver(new LibraryPrefixResolver( owningLibrary ));
			symbolResolver.setAnonymousEntityFilter(new ChameleonFilter( owningLibrary ));
			entityName = symbolResolver.buildEntityName(assignedEntity.getNamespace(), assignedEntity.getLocalName());
		}
		return entityName;
	}

	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#getEventClass()
	 */
	@Override
	public Class<?> getEventClass() {
		return ValueChangeEvent.class;
	}

	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#getSourceObjectClass()
	 */
	@Override
	public Class<TLModelElement> getSourceObjectClass() {
		return TLModelElement.class;
	}
	
}

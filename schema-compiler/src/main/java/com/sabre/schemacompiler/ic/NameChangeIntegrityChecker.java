/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.ic;

import java.util.ArrayList;
import java.util.List;

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
import com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter;
import com.sabre.schemacompiler.visitor.ModelNavigator;

/**
 * Model integrity check listener, that updates all 'typeName' fields in referencing entities when
 * the referred entity's local name is modified.
 * 
 * @author S. Livezey
 */
public class NameChangeIntegrityChecker extends AbstractIntegrityChecker<ValueChangeEvent<TLModelElement,NamedEntity>,TLModelElement> {
	
	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#processModelEvent(com.sabre.schemacompiler.event.ModelEvent)
	 */
	@Override
	public void processModelEvent(ValueChangeEvent<TLModelElement, NamedEntity> event) {
		TLModelElement sourceObject = event.getSource();
		
		if ((sourceObject instanceof NamedEntity) && (event.getType() == ModelEventType.NAME_MODIFIED)) {
			resolveAssignedTypeNames( (NamedEntity) sourceObject );
		}
	}
	
	/**
	 * Scans the entire model for references to the given entity and refreshes the type-name assignment
	 * (typically a 'prefix:local-name' value) for each occurrance.
	 * 
	 * @param modifiedEntity  the modified entity whose references should be updated
	 */
	public static void resolveAssignedTypeNames(NamedEntity modifiedEntity) {
		List<NamedEntity> affectedEntities = getAffectedEntities( modifiedEntity );
		AbstractLibrary localLibrary = modifiedEntity.getOwningLibrary();
		SymbolResolver symbolResolver = new TLModelSymbolResolver(localLibrary.getOwningModel());
		
		symbolResolver.setPrefixResolver(new LibraryPrefixResolver(localLibrary));
		symbolResolver.setAnonymousEntityFilter(new ChameleonFilter(localLibrary));
		ModelNavigator.navigate(modifiedEntity.getOwningModel(),
				new EntityNameChangeVisitor(affectedEntities, symbolResolver));
	}
	
	/**
	 * Returns the list of model entities that were affected by the name-change event.
	 * 
	 * @param modifiedEntity  the source object whose name was modified
	 * @return List<NamedEntity>
	 */
	private static List<NamedEntity> getAffectedEntities(NamedEntity modifiedEntity) {
		ModelElementCollector collectVisitor = new ModelElementCollector();
		
		ModelNavigator.navigate(modifiedEntity, collectVisitor);
		return new ArrayList<NamedEntity>( collectVisitor.getLibraryEntities() );
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
	
	/**
	 * Visitor that handles updates the entity name field for any entities who reference another entity whose
	 * local name was modified.
	 *
	 * @author S. Livezey
	 */
	private static class EntityNameChangeVisitor extends ModelElementVisitorAdapter {
		
		private List<NamedEntity> modifiedEntities;
		private SymbolResolver symbolResolver;
		
		/**
		 * Constructor that assigns the list of modified entities and the symbol resolver used to
		 * construct new entity name values.
		 * 
		 * @param modifiedEntities  the named entities that were affected by the name change event
		 * @param symbolResolver  the symbol resolver used to construct new entity names
		 */
		public EntityNameChangeVisitor(List<NamedEntity> modifiedEntities, SymbolResolver symbolResolver) {
			this.modifiedEntities = modifiedEntities;
			this.symbolResolver = symbolResolver;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(com.sabre.schemacompiler.model.TLSimple)
		 */
		@Override
		public boolean visitSimple(TLSimple simple) {
			NamedEntity referencedEntity = simple.getParentType();
			
			if (modifiedEntities.contains(referencedEntity)) {
				simple.setParentTypeName(symbolResolver.buildEntityName(
						referencedEntity.getNamespace(), referencedEntity.getLocalName()));
			}
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(com.sabre.schemacompiler.model.TLValueWithAttributes)
		 */
		@Override
		public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
			NamedEntity referencedEntity = valueWithAttributes.getParentType();
			
			if (modifiedEntities.contains(referencedEntity)) {
				valueWithAttributes.setParentTypeName(symbolResolver.buildEntityName(
						referencedEntity.getNamespace(), referencedEntity.getLocalName()));
			}
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(com.sabre.schemacompiler.model.TLExtension)
		 */
		@Override
		public boolean visitExtension(TLExtension extension) {
			NamedEntity referencedEntity = extension.getExtendsEntity();
			
			if (modifiedEntities.contains(referencedEntity)) {
				extension.setExtendsEntityName(symbolResolver.buildEntityName(
						referencedEntity.getNamespace(), referencedEntity.getLocalName()));
			}
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(com.sabre.schemacompiler.model.TLSimpleFacet)
		 */
		@Override
		public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
			NamedEntity referencedEntity = simpleFacet.getSimpleType();
			
			if (modifiedEntities.contains(referencedEntity)) {
				simpleFacet.setSimpleTypeName(symbolResolver.buildEntityName(
						referencedEntity.getNamespace(), referencedEntity.getLocalName()));
			}
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(com.sabre.schemacompiler.model.TLAttribute)
		 */
		@Override
		public boolean visitAttribute(TLAttribute attribute) {
			NamedEntity referencedEntity = attribute.getType();
			
			if (modifiedEntities.contains(referencedEntity)) {
				attribute.setTypeName(symbolResolver.buildEntityName(
						referencedEntity.getNamespace(), referencedEntity.getLocalName()));
			}
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(com.sabre.schemacompiler.model.TLProperty)
		 */
		@Override
		public boolean visitElement(TLProperty element) {
			NamedEntity referencedEntity = element.getType();
			
			if (modifiedEntities.contains(referencedEntity)) {
				element.setTypeName(symbolResolver.buildEntityName(
						referencedEntity.getNamespace(), referencedEntity.getLocalName()));
			}
			return true;
		}
		
	}
	
}

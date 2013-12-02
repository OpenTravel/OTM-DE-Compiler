/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.ic;

import com.sabre.schemacompiler.event.ModelEventType;
import com.sabre.schemacompiler.event.ValueChangeEvent;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLNamespaceImport;
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
 * Model integrity check listener, that ensures all 'typeName' fields in a library are updated when the prefix
 * field of a referenced namespace is modified.
 *
 * @author S. Livezey
 */
public class PrefixChangeIntegrityChecker extends AbstractIntegrityChecker<ValueChangeEvent<TLNamespaceImport,String>,TLNamespaceImport> {

	/**
	 * @see com.sabre.schemacompiler.event.ModelEventListener#processModelEvent(com.sabre.schemacompiler.event.ModelEvent)
	 */
	@Override
	public void processModelEvent(ValueChangeEvent<TLNamespaceImport, String> event) {
		if (event.getType() == ModelEventType.PREFIX_MODIFIED) {
			TLNamespaceImport sourceObject = event.getSource();
			AbstractLibrary affectedLibrary = (sourceObject == null) ? null : sourceObject.getOwningLibrary();
			
			if (affectedLibrary != null) {
				ModelNavigator.navigate(affectedLibrary,
						new ImportPrefixChangeVisitor(affectedLibrary, sourceObject.getNamespace()));
			}
		}
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
	public Class<TLNamespaceImport> getSourceObjectClass() {
		return TLNamespaceImport.class;
	}
	
	/**
	 * Visitor that handles updates the entity name field for any entities who reference another entity
	 * assigned to the namespace whose prefix was modified.
	 *
	 * @author S. Livezey
	 */
	private static class ImportPrefixChangeVisitor extends ModelElementVisitorAdapter {
		
		private SymbolResolver symbolResolver;
		private String affectedNamespace;
		
		/**
		 * Constructor that specifies the library whose members may have been affected by a prefix-change
		 * event, and the namespace whose associated prefix was modified.
		 * 
		 * @param library  the library whose members are to be examined
		 * @param affectedNamespace  the namespace whose associated prefix was modified
		 */
		public ImportPrefixChangeVisitor(AbstractLibrary library, String affectedNamespace) {
			this.affectedNamespace = affectedNamespace;
			this.symbolResolver = new TLModelSymbolResolver(library.getOwningModel());
			symbolResolver.setPrefixResolver(new LibraryPrefixResolver(library));
			symbolResolver.setAnonymousEntityFilter(new ChameleonFilter(library));
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(com.sabre.schemacompiler.model.TLSimple)
		 */
		@Override
		public boolean visitSimple(TLSimple simple) {
			if ((simple.getParentType() != null) && affectedNamespace.equals(simple.getParentType().getNamespace())) {
				simple.setParentTypeName( symbolResolver.buildEntityName(affectedNamespace, simple.getParentType().getLocalName()) );
			}
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(com.sabre.schemacompiler.model.TLValueWithAttributes)
		 */
		@Override
		public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
			if ((valueWithAttributes.getParentType() != null) && affectedNamespace.equals(valueWithAttributes.getParentType().getNamespace())) {
				valueWithAttributes.setParentTypeName( symbolResolver.buildEntityName(affectedNamespace, valueWithAttributes.getParentType().getLocalName()) );
			}
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(com.sabre.schemacompiler.model.TLExtension)
		 */
		@Override
		public boolean visitExtension(TLExtension extension) {
			if ((extension.getExtendsEntity() != null) && affectedNamespace.equals(extension.getExtendsEntity().getNamespace())) {
				extension.setExtendsEntityName( symbolResolver.buildEntityName(affectedNamespace, extension.getExtendsEntity().getLocalName()) );
			}
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(com.sabre.schemacompiler.model.TLSimpleFacet)
		 */
		@Override
		public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
			if ((simpleFacet.getSimpleType() != null) && affectedNamespace.equals(simpleFacet.getSimpleType().getNamespace())) {
				simpleFacet.setSimpleTypeName( symbolResolver.buildEntityName(affectedNamespace, simpleFacet.getSimpleType().getLocalName()) );
			}
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(com.sabre.schemacompiler.model.TLAttribute)
		 */
		@Override
		public boolean visitAttribute(TLAttribute attribute) {
			if ((attribute.getType() != null) && affectedNamespace.equals(attribute.getType().getNamespace())) {
				attribute.setTypeName( symbolResolver.buildEntityName(affectedNamespace, attribute.getType().getLocalName()) );
			}
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(com.sabre.schemacompiler.model.TLProperty)
		 */
		@Override
		public boolean visitElement(TLProperty element) {
			if ((element.getType() != null) && affectedNamespace.equals(element.getType().getNamespace())) {
				element.setTypeName( symbolResolver.buildEntityName(affectedNamespace, element.getType().getLocalName()) );
			}
			return true;
		}
		
	}
	
}

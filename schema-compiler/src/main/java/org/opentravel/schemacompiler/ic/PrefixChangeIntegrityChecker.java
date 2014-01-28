
package org.opentravel.schemacompiler.ic;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.transform.SymbolResolver;
import org.opentravel.schemacompiler.transform.util.ChameleonFilter;
import org.opentravel.schemacompiler.transform.util.LibraryPrefixResolver;
import org.opentravel.schemacompiler.validate.impl.TLModelSymbolResolver;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Model integrity check listener, that ensures all 'typeName' fields in a library are updated when the prefix
 * field of a referenced namespace is modified.
 *
 * @author S. Livezey
 */
public class PrefixChangeIntegrityChecker extends AbstractIntegrityChecker<ValueChangeEvent<TLNamespaceImport,String>,TLNamespaceImport> {

	/**
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
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
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#getEventClass()
	 */
	@Override
	public Class<?> getEventClass() {
		return ValueChangeEvent.class;
	}

	/**
	 * @see org.opentravel.schemacompiler.event.ModelEventListener#getSourceObjectClass()
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
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
		 */
		@Override
		public boolean visitSimple(TLSimple simple) {
			if ((simple.getParentType() != null) && affectedNamespace.equals(simple.getParentType().getNamespace())) {
				simple.setParentTypeName( symbolResolver.buildEntityName(affectedNamespace, simple.getParentType().getLocalName()) );
			}
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
		 */
		@Override
		public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
			if ((valueWithAttributes.getParentType() != null) && affectedNamespace.equals(valueWithAttributes.getParentType().getNamespace())) {
				valueWithAttributes.setParentTypeName( symbolResolver.buildEntityName(affectedNamespace, valueWithAttributes.getParentType().getLocalName()) );
			}
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
		 */
		@Override
		public boolean visitExtension(TLExtension extension) {
			if ((extension.getExtendsEntity() != null) && affectedNamespace.equals(extension.getExtendsEntity().getNamespace())) {
				extension.setExtendsEntityName( symbolResolver.buildEntityName(affectedNamespace, extension.getExtendsEntity().getLocalName()) );
			}
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
		 */
		@Override
		public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
			if ((simpleFacet.getSimpleType() != null) && affectedNamespace.equals(simpleFacet.getSimpleType().getNamespace())) {
				simpleFacet.setSimpleTypeName( symbolResolver.buildEntityName(affectedNamespace, simpleFacet.getSimpleType().getLocalName()) );
			}
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
		 */
		@Override
		public boolean visitAttribute(TLAttribute attribute) {
			if ((attribute.getType() != null) && affectedNamespace.equals(attribute.getType().getNamespace())) {
				attribute.setTypeName( symbolResolver.buildEntityName(affectedNamespace, attribute.getType().getLocalName()) );
			}
			return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
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

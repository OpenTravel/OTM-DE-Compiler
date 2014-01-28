package org.opentravel.schemacompiler.ic;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLModelElement;
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
 * Model integrity check listener, that updates all 'typeName' fields in referencing entities when
 * the referred entity's local name is modified.
 * 
 * @author S. Livezey
 */
public class NameChangeIntegrityChecker extends
        AbstractIntegrityChecker<ValueChangeEvent<TLModelElement, NamedEntity>, TLModelElement> {

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
     */
    @Override
    public void processModelEvent(ValueChangeEvent<TLModelElement, NamedEntity> event) {
        TLModelElement sourceObject = event.getSource();

        if ((sourceObject instanceof NamedEntity)
                && (event.getType() == ModelEventType.NAME_MODIFIED)) {
            resolveAssignedTypeNames((NamedEntity) sourceObject);
        }
    }

    /**
     * Scans the entire model for references to the given entity and refreshes the type-name
     * assignment (typically a 'prefix:local-name' value) for each occurrance.
     * 
     * @param modifiedEntity
     *            the modified entity whose references should be updated
     */
    public static void resolveAssignedTypeNames(NamedEntity modifiedEntity) {
        List<NamedEntity> affectedEntities = getAffectedEntities(modifiedEntity);
        AbstractLibrary localLibrary = modifiedEntity.getOwningLibrary();
        SymbolResolver symbolResolver = new TLModelSymbolResolver(localLibrary.getOwningModel());

        symbolResolver.setPrefixResolver(new LibraryPrefixResolver(localLibrary));
        symbolResolver.setAnonymousEntityFilter(new ChameleonFilter(localLibrary));
        ModelNavigator.navigate(modifiedEntity.getOwningModel(), new EntityNameChangeVisitor(
                affectedEntities, symbolResolver));
    }

    /**
     * Returns the list of model entities that were affected by the name-change event.
     * 
     * @param modifiedEntity
     *            the source object whose name was modified
     * @return List<NamedEntity>
     */
    private static List<NamedEntity> getAffectedEntities(NamedEntity modifiedEntity) {
        ModelElementCollector collectVisitor = new ModelElementCollector();

        ModelNavigator.navigate(modifiedEntity, collectVisitor);
        return new ArrayList<NamedEntity>(collectVisitor.getLibraryEntities());
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
    public Class<TLModelElement> getSourceObjectClass() {
        return TLModelElement.class;
    }

    /**
     * Visitor that handles updates the entity name field for any entities who reference another
     * entity whose local name was modified.
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
         * @param modifiedEntities
         *            the named entities that were affected by the name change event
         * @param symbolResolver
         *            the symbol resolver used to construct new entity names
         */
        public EntityNameChangeVisitor(List<NamedEntity> modifiedEntities,
                SymbolResolver symbolResolver) {
            this.modifiedEntities = modifiedEntities;
            this.symbolResolver = symbolResolver;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
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
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
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
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
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
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
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
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
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
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
         */
        @Override
        public boolean visitElement(TLProperty element) {
            NamedEntity referencedEntity = element.getType();

            if (modifiedEntities.contains(referencedEntity)) {
                element.setTypeName(symbolResolver.buildEntityName(referencedEntity.getNamespace(),
                        referencedEntity.getLocalName()));
            }
            return true;
        }

    }

}

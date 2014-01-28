package org.opentravel.schemacompiler.ic;

import java.util.Collection;

import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Abstract integrity checker component that provides common functions to release references to an
 * entity or entities when they are removed from the model.
 * 
 * @param <S>
 *            the source object type for the event
 * @param <I>
 *            the type of item that was added or removed from the parent entity
 * @author S. Livezey
 */
public abstract class EntityRemovedIntegrityChecker<S, I> extends
        AbstractIntegrityChecker<OwnershipEvent<S, I>, S> {

    /**
     * Purges all references to each of the specified entities from the model.
     * 
     * @param removedEntity
     *            the entity that was removed from the model
     * @param model
     *            the model from which all entity references should be purged
     */
    protected void purgeEntitiesFromModel(TLModelElement removedEntity, TLModel model) {
        // First, build a collection of all the named entities being removed from the model
        ModelElementCollector collectVisitor = new ModelElementCollector();

        if (removedEntity instanceof AbstractLibrary) {
            ModelNavigator.navigate((AbstractLibrary) removedEntity, collectVisitor);

        } else if (removedEntity instanceof LibraryElement) {
            ModelNavigator.navigate((LibraryElement) removedEntity, collectVisitor);
        }

        // Next, purge any references to those entities we just collected
        Collection<NamedEntity> removedEntities = collectVisitor.getLibraryEntities();

        if (!removedEntities.isEmpty()) {
            PurgeEntityVisitor purgeVisitor = new PurgeEntityVisitor(removedEntities);

            for (TLLibrary library : model.getUserDefinedLibraries()) {
                ModelNavigator.navigate(library, purgeVisitor);
            }
        }
    }

    /**
     * Visitor that handles updates the entity name field for any entities who reference another
     * entity whose local name was modified.
     * 
     * @author S. Livezey
     */
    private static class PurgeEntityVisitor extends ModelElementVisitorAdapter {

        private NamedEntity[] removedEntities;

        /**
         * Constructor that assigns the list of modified entities and the symbol resolver used to
         * construct new entity name values.
         * 
         * @param removedEntities
         *            the named entities that were removed from the model
         */
        public PurgeEntityVisitor(Collection<NamedEntity> removedEntities) {
            if (removedEntities != null) {
                this.removedEntities = new NamedEntity[removedEntities.size()];
                int i = 0;

                for (NamedEntity removedEntity : removedEntities) {
                    this.removedEntities[i] = removedEntity;
                    i++;
                }
            } else {
                this.removedEntities = new NamedEntity[0];
            }
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
         */
        @Override
        public boolean visitSimple(TLSimple simple) {
            NamedEntity referencedEntity = simple.getParentType();

            if (isRemovedEntity(referencedEntity)) {
                TLModel model = simple.getOwningModel();
                boolean listenersEnabled = disableListeners(model);

                simple.setParentType(null);
                restoreListeners(model, listenersEnabled);
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
         */
        @Override
        public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
            NamedEntity referencedEntity = valueWithAttributes.getParentType();

            if (isRemovedEntity(referencedEntity)) {
                TLModel model = valueWithAttributes.getOwningModel();
                boolean listenersEnabled = disableListeners(model);

                valueWithAttributes.setParentType(null);
                restoreListeners(model, listenersEnabled);
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
         */
        @Override
        public boolean visitExtension(TLExtension extension) {
            NamedEntity referencedEntity = extension.getExtendsEntity();

            if (isRemovedEntity(referencedEntity)) {
                TLModel model = extension.getOwningModel();
                boolean listenersEnabled = disableListeners(model);

                extension.setExtendsEntity(null);
                restoreListeners(model, listenersEnabled);
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
         */
        @Override
        public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
            NamedEntity referencedEntity = simpleFacet.getSimpleType();

            if (isRemovedEntity(referencedEntity)) {
                TLModel model = simpleFacet.getOwningModel();
                boolean listenersEnabled = disableListeners(model);

                simpleFacet.setSimpleType(null);
                restoreListeners(model, listenersEnabled);
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
         */
        @Override
        public boolean visitAttribute(TLAttribute attribute) {
            NamedEntity referencedEntity = attribute.getType();

            if (isRemovedEntity(referencedEntity)) {
                TLModel model = attribute.getOwningModel();
                boolean listenersEnabled = disableListeners(model);

                attribute.setType(null);
                restoreListeners(model, listenersEnabled);
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
         */
        @Override
        public boolean visitElement(TLProperty element) {
            NamedEntity referencedEntity = element.getType();

            if (isRemovedEntity(referencedEntity)) {
                TLModel model = element.getOwningModel();
                boolean listenersEnabled = disableListeners(model);

                element.setType(null);
                restoreListeners(model, listenersEnabled);
            }
            return true;
        }

        /**
         * Returns true if the given named entity is one of the instances flagged for removal from
         * the model. This performs a reference check on the removed entity list instead of the
         * 'equals()' method that is employed by the Java collection API, eliminating unexpected
         * behavior for model entity class(es) that override the 'equals()' method.
         * 
         * @param entity
         *            the named entity to check
         * @return boolean
         */
        private boolean isRemovedEntity(NamedEntity entity) {
            boolean result = false;

            for (NamedEntity removedEntity : removedEntities) {
                if (entity == removedEntity) {
                    result = true;
                    break;
                }
            }
            return result;
        }

        /**
         * Disables listener event propagation for the given model. The return value indicates the
         * original state of the flag before events were disabled.
         * 
         * @param model
         *            the model for which listener events should be disabled
         * @return boolean
         */
        private boolean disableListeners(TLModel model) {
            boolean listenersEnabled;

            if (model != null) {
                listenersEnabled = model.isListenersEnabled();
                model.setListenersEnabled(false);

            } else {
                listenersEnabled = false;
            }
            return listenersEnabled;
        }

        /**
         * Restores the original value for the 'listenersEnabled' flag for the given model.
         * 
         * @param model
         *            the model for which the listener state should be restored
         * @param listenersEnabled
         *            the original flag value for the 'listenersEnabled' flag
         */
        private void restoreListeners(TLModel model, boolean listenersEnabled) {
            if (model != null) {
                model.setListenersEnabled(listenersEnabled);
            }
        }

    }

}

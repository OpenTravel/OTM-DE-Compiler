/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemacompiler.ic;

import java.util.Collection;

import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
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
        Collection<TLModelElement> removedEntities = collectVisitor.getLibraryEntities();

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

        private TLModelElement[] removedEntities;

        /**
         * Constructor that assigns the list of modified entities and the symbol resolver used to
         * construct new entity name values.
         * 
         * @param removedEntities
         *            the named entities that were removed from the model
         */
        public PurgeEntityVisitor(Collection<TLModelElement> removedEntities) {
            if (removedEntities != null) {
                this.removedEntities = new TLModelElement[removedEntities.size()];
                int i = 0;

                for (TLModelElement removedEntity : removedEntities) {
                    this.removedEntities[i] = removedEntity;
                    i++;
                }
            } else {
                this.removedEntities = new TLModelElement[0];
            }
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
         */
        @Override
        public boolean visitSimple(TLSimple simple) {
        	TLModelElement referencedEntity = (TLModelElement) simple.getParentType();

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
        	TLModelElement referencedEntity = (TLModelElement) valueWithAttributes.getParentType();

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
        	TLModelElement referencedEntity = (TLModelElement) extension.getExtendsEntity();

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
        	TLModelElement referencedEntity = (TLModelElement) simpleFacet.getSimpleType();

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
        	TLModelElement referencedEntity = (TLModelElement) attribute.getType();

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
        	TLModelElement referencedEntity = (TLModelElement) element.getType();

            if (isRemovedEntity(referencedEntity)) {
                TLModel model = element.getOwningModel();
                boolean listenersEnabled = disableListeners(model);

                element.setType(null);
                restoreListeners(model, listenersEnabled);
            }
            return true;
        }

        /**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResource(org.opentravel.schemacompiler.model.TLResource)
		 */
		@Override
		public boolean visitResource(TLResource resource) {
			TLModelElement referencedEntity = resource.getBusinessObjectRef();

            if (isRemovedEntity(referencedEntity)) {
                TLModel model = resource.getOwningModel();
                boolean listenersEnabled = disableListeners(model);

                resource.setBusinessObjectRef(null);
                restoreListeners(model, listenersEnabled);
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResourceParentRef(org.opentravel.schemacompiler.model.TLResourceParentRef)
		 */
		@Override
		public boolean visitResourceParentRef(TLResourceParentRef parentRef) {
			TLModelElement referencedResource = parentRef.getParentResource();
            TLParamGroup referencedParamGroup = parentRef.getParentParamGroup();
            TLModel model = parentRef.getOwningModel();

            if (isRemovedEntity(referencedResource)) {
                boolean listenersEnabled = disableListeners(model);

                parentRef.setParentResource(null);
                restoreListeners(model, listenersEnabled);
            }
            if (isRemovedEntity(referencedParamGroup)) {
                boolean listenersEnabled = disableListeners(model);

                parentRef.setParentParamGroup(null);
                restoreListeners(model, listenersEnabled);
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParamGroup(org.opentravel.schemacompiler.model.TLParamGroup)
		 */
		@Override
		public boolean visitParamGroup(TLParamGroup paramGroup) {
			TLModelElement referencedEntity = paramGroup.getFacetRef();

            if (isRemovedEntity(referencedEntity)) {
                TLModel model = paramGroup.getOwningModel();
                boolean listenersEnabled = disableListeners(model);

                paramGroup.setFacetRef(null);
                restoreListeners(model, listenersEnabled);
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParameter(org.opentravel.schemacompiler.model.TLParameter)
		 */
		@Override
		public boolean visitParameter(TLParameter parameter) {
			TLModelElement referencedEntity = (TLModelElement) parameter.getFieldRef();

            if (isRemovedEntity(referencedEntity)) {
                TLModel model = parameter.getOwningModel();
                boolean listenersEnabled = disableListeners(model);
                
                // If a field is deleted, we will delete any corresponing parameters
                // rather than setting their reference to null
                parameter.getOwner().removeParameter(parameter);
                restoreListeners(model, listenersEnabled);
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionRequest(org.opentravel.schemacompiler.model.TLActionRequest)
		 */
		@Override
		public boolean visitActionRequest(TLActionRequest actionRequest) {
            TLParamGroup referencedParamGroup = actionRequest.getParamGroup();
            TLActionFacet referencedActionFacet = actionRequest.getActionFacet();
            TLModel model = actionRequest.getOwningModel();
            
            if (isRemovedEntity(referencedParamGroup)) {
                boolean listenersEnabled = disableListeners(model);

                actionRequest.setParamGroup(null);
                restoreListeners(model, listenersEnabled);
            }
            if (isRemovedEntity(referencedActionFacet)) {
                boolean listenersEnabled = disableListeners(model);

                actionRequest.setActionFacet(null);
                restoreListeners(model, listenersEnabled);
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionResponse(org.opentravel.schemacompiler.model.TLActionResponse)
		 */
		@Override
		public boolean visitActionResponse(TLActionResponse actionResponse) {
            TLActionFacet referencedActionFacet = actionResponse.getActionFacet();
            
            if (isRemovedEntity(referencedActionFacet)) {
                TLModel model = actionResponse.getOwningModel();
                boolean listenersEnabled = disableListeners(model);

                actionResponse.setActionFacet(null);
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
        private boolean isRemovedEntity(TLModelElement entity) {
            boolean result = false;

            for (TLModelElement removedEntity : removedEntities) {
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

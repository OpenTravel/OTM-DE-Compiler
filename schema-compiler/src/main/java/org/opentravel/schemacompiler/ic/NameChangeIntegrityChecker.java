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

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.ModelElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
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
        AbstractIntegrityChecker<ValueChangeEvent<ModelElement, NamedEntity>, ModelElement> {

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
     */
    @Override
    public void processModelEvent(ValueChangeEvent<ModelElement, NamedEntity> event) {
        ModelElement sourceObject = event.getSource();
        
        if (event.getType() == ModelEventType.NAME_MODIFIED) {
            if (sourceObject instanceof NamedEntity) {
                resolveAssignedTypeNames((NamedEntity) sourceObject);
                
            } else if (sourceObject instanceof TLMemberField) {
            	resolveAssignedFieldNames((TLMemberField<?>) sourceObject);
            	
            } else if (sourceObject instanceof TLParamGroup) {
            	resolveAssignedParamGroupNames((TLParamGroup) sourceObject);
            }
        	
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
        List<TLModelElement> affectedEntities = getAffectedEntities(modifiedEntity);
        AbstractLibrary localLibrary = modifiedEntity.getOwningLibrary();
        SymbolResolver symbolResolver = new TLModelSymbolResolver(localLibrary.getOwningModel());

        symbolResolver.setPrefixResolver(new LibraryPrefixResolver(localLibrary));
        symbolResolver.setAnonymousEntityFilter(new ChameleonFilter(localLibrary));
        ModelNavigator.navigate(modifiedEntity.getOwningModel(), new EntityNameChangeVisitor(
                affectedEntities, symbolResolver));
    }

    /**
     * Scans the entire model for references to the given member field and refreshes the name
     * assignment for each occurrance.
     * 
     * @param modifiedField
     *            the modified field whose references should be updated
     */
    public static void resolveAssignedFieldNames(TLMemberField<?> modifiedField) {
        AbstractLibrary localLibrary = ((LibraryElement) modifiedField.getOwner()).getOwningLibrary();
        SymbolResolver symbolResolver = new TLModelSymbolResolver(localLibrary.getOwningModel());
        List<TLModelElement> affectedEntities = new ArrayList<>();
        
        affectedEntities.add((TLModelElement) modifiedField);
        symbolResolver.setPrefixResolver(new LibraryPrefixResolver(localLibrary));
        symbolResolver.setAnonymousEntityFilter(new ChameleonFilter(localLibrary));
        ModelNavigator.navigate(localLibrary.getOwningModel(), new EntityNameChangeVisitor(
                affectedEntities, symbolResolver));
    }
    
    /**
     * Scans the entire model for references to the given parameter group and refreshes the name
     * assignment for each occurrance.
     * 
     * @param modifiedParamGroup
     *            the modified parameter group whose references should be updated
     */
    public static void resolveAssignedParamGroupNames(TLParamGroup modifiedParamGroup) {
        AbstractLibrary localLibrary = modifiedParamGroup.getOwningLibrary();
        SymbolResolver symbolResolver = new TLModelSymbolResolver(localLibrary.getOwningModel());
        List<TLModelElement> affectedEntities = new ArrayList<>();
        
        affectedEntities.add(modifiedParamGroup);
        symbolResolver.setPrefixResolver(new LibraryPrefixResolver(localLibrary));
        symbolResolver.setAnonymousEntityFilter(new ChameleonFilter(localLibrary));
        ModelNavigator.navigate(localLibrary.getOwningModel(), new EntityNameChangeVisitor(
                affectedEntities, symbolResolver));
    }
    
    /**
     * Returns the list of model entities that were affected by the name-change event.
     * 
     * @param modifiedEntity
     *            the source object whose name was modified
     * @return List<TLModelElement>
     */
    private static List<TLModelElement> getAffectedEntities(NamedEntity modifiedEntity) {
        ModelElementCollector collectVisitor = new ModelElementCollector();

        ModelNavigator.navigate(modifiedEntity, collectVisitor);
        return new ArrayList<TLModelElement>(collectVisitor.getLibraryEntities());
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
    public Class<ModelElement> getSourceObjectClass() {
        return ModelElement.class;
    }

    /**
     * Visitor that handles updates the entity name field for any entities who reference another
     * entity whose local name was modified.
     * 
     * @author S. Livezey
     */
    private static class EntityNameChangeVisitor extends ModelElementVisitorAdapter {

        private List<TLModelElement> modifiedEntities = new ArrayList<>();
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
        public EntityNameChangeVisitor(List<TLModelElement> modifiedEntities,
                SymbolResolver symbolResolver) {
        	if (modifiedEntities != null) {
                this.modifiedEntities.addAll(modifiedEntities);
        	}
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

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResource(org.opentravel.schemacompiler.model.TLResource)
		 */
		@Override
		public boolean visitResource(TLResource resource) {
			TLBusinessObject referencedEntity = resource.getBusinessObjectRef();
			
            if (modifiedEntities.contains(referencedEntity)) {
            	resource.setBusinessObjectRefName(symbolResolver.buildEntityName(referencedEntity.getNamespace(),
                        referencedEntity.getLocalName()));
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResourceParentRef(org.opentravel.schemacompiler.model.TLResourceParentRef)
		 */
		@Override
		public boolean visitResourceParentRef(TLResourceParentRef parentRef) {
			TLResource referencedResource = parentRef.getParentResource();
			TLParamGroup referencedParamGroup = parentRef.getParentParamGroup();
			
            if (modifiedEntities.contains(referencedResource)) {
            	parentRef.setParentResourceName(symbolResolver.buildEntityName(referencedResource.getNamespace(),
            			referencedResource.getLocalName()));
            }
            if (modifiedEntities.contains(referencedParamGroup)) {
            	parentRef.setParentParamGroupName(referencedParamGroup.getName());
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParamGroup(org.opentravel.schemacompiler.model.TLParamGroup)
		 */
		@Override
		public boolean visitParamGroup(TLParamGroup paramGroup) {
			TLFacet referencedEntity = paramGroup.getFacetRef();
			
            if (modifiedEntities.contains(referencedEntity)) {
            	paramGroup.setFacetRefName(symbolResolver.buildEntityName(referencedEntity.getNamespace(),
                        referencedEntity.getLocalName()));
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParameter(org.opentravel.schemacompiler.model.TLParameter)
		 */
		@Override
		public boolean visitParameter(TLParameter parameter) {
			TLMemberField<?> referencedEntity = parameter.getFieldRef();
			
            if (modifiedEntities.contains(referencedEntity)) {
            	parameter.setFieldRefName(referencedEntity.getName());
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionRequest(org.opentravel.schemacompiler.model.TLActionRequest)
		 */
		@Override
		public boolean visitActionRequest(TLActionRequest actionRequest) {
			TLParamGroup referencedParamGroup = actionRequest.getParamGroup();
			TLActionFacet referencedPayloadType = actionRequest.getPayloadType();
			
            if (modifiedEntities.contains(referencedParamGroup)) {
            	actionRequest.setParamGroupName(referencedParamGroup.getName());
            }
            if (modifiedEntities.contains(referencedPayloadType)) {
            	actionRequest.setPayloadTypeName(symbolResolver.buildEntityName(referencedPayloadType.getNamespace(),
            			referencedPayloadType.getLocalName()));
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionResponse(org.opentravel.schemacompiler.model.TLActionResponse)
		 */
		@Override
		public boolean visitActionResponse(TLActionResponse actionResponse) {
			NamedEntity referencedPayloadType = actionResponse.getPayloadType();
			
            if (modifiedEntities.contains(referencedPayloadType)) {
            	actionResponse.setPayloadTypeName(symbolResolver.buildEntityName(referencedPayloadType.getNamespace(),
            			referencedPayloadType.getLocalName()));
            }
            return true;
		}

    }

}

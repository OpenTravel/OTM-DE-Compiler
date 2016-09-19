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

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLParamGroup;
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
 * Model integrity check listener, that ensures all 'typeName' fields in a library are updated when
 * the prefix field of a referenced namespace is modified.
 * 
 * @author S. Livezey
 */
public class PrefixChangeIntegrityChecker extends
        AbstractIntegrityChecker<ValueChangeEvent<TLNamespaceImport, String>, TLNamespaceImport> {

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
     */
    @Override
    public void processModelEvent(ValueChangeEvent<TLNamespaceImport, String> event) {
        if (event.getType() == ModelEventType.PREFIX_MODIFIED) {
            TLNamespaceImport sourceObject = event.getSource();
            AbstractLibrary affectedLibrary = (sourceObject == null) ? null : sourceObject
                    .getOwningLibrary();

            if (affectedLibrary != null) {
                ModelNavigator.navigate(affectedLibrary, new ImportPrefixChangeVisitor(
                        affectedLibrary, sourceObject.getNamespace()));
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
     * Visitor that handles updates the entity name field for any entities who reference another
     * entity assigned to the namespace whose prefix was modified.
     * 
     * @author S. Livezey
     */
    private static class ImportPrefixChangeVisitor extends ModelElementVisitorAdapter {

        private SymbolResolver symbolResolver;
        private String affectedNamespace;

        /**
         * Constructor that specifies the library whose members may have been affected by a
         * prefix-change event, and the namespace whose associated prefix was modified.
         * 
         * @param library
         *            the library whose members are to be examined
         * @param affectedNamespace
         *            the namespace whose associated prefix was modified
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
            if ((simple.getParentType() != null)
                    && affectedNamespace.equals(simple.getParentType().getNamespace())) {
                simple.setParentTypeName(symbolResolver.buildEntityName(affectedNamespace, simple
                        .getParentType().getLocalName()));
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
         */
        @Override
        public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
            if ((valueWithAttributes.getParentType() != null)
                    && affectedNamespace.equals(valueWithAttributes.getParentType().getNamespace())) {
                valueWithAttributes.setParentTypeName(symbolResolver.buildEntityName(
                        affectedNamespace, valueWithAttributes.getParentType().getLocalName()));
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
         */
        @Override
        public boolean visitExtension(TLExtension extension) {
            if ((extension.getExtendsEntity() != null)
                    && affectedNamespace.equals(extension.getExtendsEntity().getNamespace())) {
                extension.setExtendsEntityName(symbolResolver.buildEntityName(affectedNamespace,
                        extension.getExtendsEntity().getLocalName()));
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
         */
        @Override
        public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
            if ((simpleFacet.getSimpleType() != null)
                    && affectedNamespace.equals(simpleFacet.getSimpleType().getNamespace())) {
                simpleFacet.setSimpleTypeName(symbolResolver.buildEntityName(affectedNamespace,
                        simpleFacet.getSimpleType().getLocalName()));
            }
            return true;
        }

        /**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitContextualFacet(org.opentravel.schemacompiler.model.TLContextualFacet)
		 */
		@Override
		public boolean visitContextualFacet(TLContextualFacet facet) {
            if ((facet.getOwningEntity() != null)
                    && affectedNamespace.equals(facet.getOwningEntity().getNamespace())) {
                facet.setOwningEntityName(symbolResolver.buildEntityName(affectedNamespace,
                		facet.getOwningEntity().getLocalName()));
            }
            return true;
		}

		/**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
         */
        @Override
        public boolean visitAttribute(TLAttribute attribute) {
            if ((attribute.getType() != null)
                    && affectedNamespace.equals(attribute.getType().getNamespace())) {
                attribute.setTypeName(symbolResolver.buildEntityName(affectedNamespace, attribute
                        .getType().getLocalName()));
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
         */
        @Override
        public boolean visitElement(TLProperty element) {
            if ((element.getType() != null)
                    && affectedNamespace.equals(element.getType().getNamespace())) {
                element.setTypeName(symbolResolver.buildEntityName(affectedNamespace, element
                        .getType().getLocalName()));
            }
            return true;
        }

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResource(org.opentravel.schemacompiler.model.TLResource)
		 */
		@Override
		public boolean visitResource(TLResource resource) {
            if ((resource.getBusinessObjectRef() != null)
                    && affectedNamespace.equals(resource.getBusinessObjectRef().getNamespace())) {
            	resource.setBusinessObjectRefName(symbolResolver.buildEntityName(affectedNamespace, resource
                        .getBusinessObjectRef().getLocalName()));
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResourceParentRef(org.opentravel.schemacompiler.model.TLResourceParentRef)
		 */
		@Override
		public boolean visitResourceParentRef(TLResourceParentRef parentRef) {
            if ((parentRef.getParentResource() != null)
                    && affectedNamespace.equals(parentRef.getParentResource().getNamespace())) {
            	parentRef.setParentResourceName(symbolResolver.buildEntityName(affectedNamespace, parentRef
                        .getParentResource().getLocalName()));
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParamGroup(org.opentravel.schemacompiler.model.TLParamGroup)
		 */
		@Override
		public boolean visitParamGroup(TLParamGroup paramGroup) {
            if ((paramGroup.getFacetRef() != null)
                    && affectedNamespace.equals(paramGroup.getFacetRef().getNamespace())) {
            	paramGroup.setFacetRefName(symbolResolver.buildEntityName(affectedNamespace, paramGroup
                        .getFacetRef().getLocalName()));
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionRequest(org.opentravel.schemacompiler.model.TLActionRequest)
		 */
		@Override
		public boolean visitActionRequest(TLActionRequest actionRequest) {
            if ((actionRequest.getPayloadType() != null)
                    && affectedNamespace.equals(actionRequest.getPayloadType().getNamespace())) {
            	actionRequest.setPayloadTypeName(symbolResolver.buildEntityName(affectedNamespace, actionRequest
                        .getPayloadType().getLocalName()));
            }
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionResponse(org.opentravel.schemacompiler.model.TLActionResponse)
		 */
		@Override
		public boolean visitActionResponse(TLActionResponse actionResponse) {
            if ((actionResponse.getPayloadType() != null)
                    && affectedNamespace.equals(actionResponse.getPayloadType().getNamespace())) {
            	actionResponse.setPayloadTypeName(symbolResolver.buildEntityName(affectedNamespace, actionResponse
                        .getPayloadType().getLocalName()));
            }
            return true;
		}

    }

}

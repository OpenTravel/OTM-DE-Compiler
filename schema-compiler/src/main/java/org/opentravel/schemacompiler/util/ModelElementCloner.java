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
package org.opentravel.schemacompiler.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentravel.ns.ota2.librarymodel_v01_06.Action;
import org.opentravel.ns.ota2.librarymodel_v01_06.ActionRequest;
import org.opentravel.ns.ota2.librarymodel_v01_06.ActionResponse;
import org.opentravel.ns.ota2.librarymodel_v01_06.Attribute;
import org.opentravel.ns.ota2.librarymodel_v01_06.BusinessObject;
import org.opentravel.ns.ota2.librarymodel_v01_06.ChoiceObject;
import org.opentravel.ns.ota2.librarymodel_v01_06.ContextDeclaration;
import org.opentravel.ns.ota2.librarymodel_v01_06.CoreObject;
import org.opentravel.ns.ota2.librarymodel_v01_06.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_06.EnumValue;
import org.opentravel.ns.ota2.librarymodel_v01_06.EnumerationClosed;
import org.opentravel.ns.ota2.librarymodel_v01_06.EnumerationOpen;
import org.opentravel.ns.ota2.librarymodel_v01_06.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_06.Example;
import org.opentravel.ns.ota2.librarymodel_v01_06.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_06.ExtensionPointFacet;
import org.opentravel.ns.ota2.librarymodel_v01_06.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_06.FacetAction;
import org.opentravel.ns.ota2.librarymodel_v01_06.FacetContextual;
import org.opentravel.ns.ota2.librarymodel_v01_06.Indicator;
import org.opentravel.ns.ota2.librarymodel_v01_06.Operation;
import org.opentravel.ns.ota2.librarymodel_v01_06.ParamGroup;
import org.opentravel.ns.ota2.librarymodel_v01_06.Parameter;
import org.opentravel.ns.ota2.librarymodel_v01_06.Property;
import org.opentravel.ns.ota2.librarymodel_v01_06.Resource;
import org.opentravel.ns.ota2.librarymodel_v01_06.ResourceParentRef;
import org.opentravel.ns.ota2.librarymodel_v01_06.Role;
import org.opentravel.ns.ota2.librarymodel_v01_06.Service;
import org.opentravel.ns.ota2.librarymodel_v01_06.Simple;
import org.opentravel.ns.ota2.librarymodel_v01_06.SimpleFacet;
import org.opentravel.ns.ota2.librarymodel_v01_06.ValueWithAttributes;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.ModelElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.ObjectTransformerContext;
import org.opentravel.schemacompiler.transform.SymbolTable;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.symbols.SymbolTableFactory;
import org.opentravel.schemacompiler.transform.tl2jaxb.TL2JaxbLibrarySymbolResolver;
import org.opentravel.schemacompiler.transform.util.LibraryPrefixResolver;

/**
 * Generic cloning utility that uses the <code>ObjectTransformer</code> functions to construct
 * copies of model element instances.
 * 
 * <p>
 * NOTE: The implementation of this class is NOT thread-safe.
 * 
 * @author S. Livezey
 */
public class ModelElementCloner {

    private static final Map<Class<?>, Class<?>> intermediateObjectTypes;

    private SymbolTable modelSymbols;
    private SymbolResolverTransformerContext sourceTransformContext;
    private DefaultTransformerContext targetTransformContext;
    private TypeMappingTransformerFactory<SymbolResolverTransformerContext> sourceTransformerFactory;
    private TypeMappingTransformerFactory<DefaultTransformerContext> targetTransformerFactory;

    /**
     * Constructor that provides access to the global model that owns all
     * possible entities that can be cloned.
     * 
     * @param model
     *            the owning model instance for all clonable entities
     */
    public ModelElementCloner(TLModel model) {
        this.modelSymbols = SymbolTableFactory.newSymbolTableFromModel(model);

        this.sourceTransformContext = new SymbolResolverTransformerContext();
        this.sourceTransformContext
                .setSymbolResolver(new TL2JaxbLibrarySymbolResolver(modelSymbols));
        this.sourceTransformerFactory = new TypeMappingTransformerFactory<SymbolResolverTransformerContext>(
                TransformerFactory.getInstance(
                        SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
                        sourceTransformContext));
        this.sourceTransformerFactory.setContext(this.sourceTransformContext);

        this.targetTransformContext = new DefaultTransformerContext();
        this.targetTransformerFactory = new TypeMappingTransformerFactory<DefaultTransformerContext>(
                TransformerFactory.getInstance(
                        SchemaCompilerApplicationContext.LOADER_TRANSFORMER_FACTORY,
                        targetTransformContext));
        this.targetTransformerFactory.setContext(this.targetTransformContext);
    }

    /**
     * Constructs a deep clone of the given object using the transformation module of the compiler.
     * 
     * @param source
     *            the source object to clone
     * @return C
     * @throws IllegalArgumentException
     *             thrown if the given source object cannot be cloned
     */
    public <C extends ModelElement, I> C clone(C source) {
        C clonedObject = null;

        if (source instanceof LibraryElement) {
            clonedObject = clone(source, ((LibraryElement) source).getOwningLibrary());
        }
        return clonedObject;
    }

    /**
     * Constructs a deep clone of the given object using the transformation module of the compiler.
     * 
     * @param source
     *            the source object to clone
     * @param resolverContext
     *            the library whose owning that should be used for reference lookups when resolving
     *            names in the cloned entity; the library itself is used to resolve namespace prefix
     *            references during reference lookups
     * @return C
     * @throws IllegalArgumentException
     *             thrown if the given source object cannot be cloned
     */
    @SuppressWarnings("unchecked")
    public <C extends ModelElement, I> C clone(C source, AbstractLibrary resolverContext) {
        C clonedObject = null;

        if (source instanceof LibraryElement) {
            Class<I> intermediateType = (Class<I>) intermediateObjectTypes.get(source.getClass());
            I intermediateObject;

            if (intermediateType == null) {
                throw new IllegalArgumentException("Unable to clone object of type: "
                        + source.getClass().getName());
            }

            // Use the transformer subsystem to create the clone of the object
            Map<Object, NamedEntity> typeAssignments = new HashMap<Object, NamedEntity>();

            sourceTransformerFactory.setTypeAssignments(typeAssignments);
            targetTransformerFactory.setTypeAssignments(typeAssignments);

            ObjectTransformer<C, I, SymbolResolverTransformerContext> sourceTransformer = sourceTransformerFactory
                    .getTransformer(source, intermediateType);
            ObjectTransformer<I, C, DefaultTransformerContext> targetTransformer = targetTransformerFactory
                    .getTransformer(intermediateType, (Class<C>) source.getClass());

            if (resolverContext != null) {
                sourceTransformContext.getSymbolResolver().setPrefixResolver(
                        new LibraryPrefixResolver(resolverContext));
            }
            intermediateObject = sourceTransformer.transform(source);
            clonedObject = targetTransformer.transform(intermediateObject);
            
            // Handle special cases for contextual facet owners since contextual facets are
            // managed externally to their business/choice object owner
            if (source instanceof TLChoiceObject) {
            	cloneContextualFacets( (TLChoiceObject) source, (TLChoiceObject) clonedObject );
            	
            } else if (source instanceof TLBusinessObject) {
            	cloneContextualFacets( (TLBusinessObject) source, (TLBusinessObject) clonedObject );
            }
            
        } else if (source != null) {
            throw new IllegalArgumentException("Unable to clone object of type: "
                    + source.getClass().getName());
        }
        return clonedObject;
    }
    
    /**
     * Clones the local contextual facets of the given choice object.
     *  
     * @param choice  the choice object whose contextual facets are to be cloned
     * @param target  the choice object that will received the cloned facets
     */
    private void cloneContextualFacets(TLChoiceObject source, TLChoiceObject target) {
    	List<TLContextualFacet> clonedChoiceFacets = cloneContextualFacets( source.getChoiceFacets() );
    	
    	for (TLContextualFacet clonedFacet : clonedChoiceFacets) {
    		target.addChoiceFacet( clonedFacet );
    	}
    }

    /**
     * Clones the local contextual facets of the given business object.
     *  
     * @param source  the business object whose contextual facets are to be cloned
     * @param target  the business object that will received the cloned facets
     */
    private void cloneContextualFacets(TLBusinessObject source, TLBusinessObject target) {
    	List<TLContextualFacet> clonedCustomFacets = cloneContextualFacets( source.getCustomFacets() );
    	List<TLContextualFacet> clonedQueryFacets = cloneContextualFacets( source.getQueryFacets() );
    	List<TLContextualFacet> clonedUpdateFacets = cloneContextualFacets( source.getUpdateFacets() );
    	
    	for (TLContextualFacet clonedFacet : clonedCustomFacets) {
    		target.addCustomFacet( clonedFacet );
    	}
    	for (TLContextualFacet clonedFacet : clonedQueryFacets) {
    		target.addQueryFacet( clonedFacet );
    	}
    	for (TLContextualFacet clonedFacet : clonedUpdateFacets) {
    		target.addUpdateFacet( clonedFacet );
    	}
    }
    
    /**
     * Recursively clones the given list of contextual facets (only local facets will
     * be cloned).
     * 
     * @param facetList  the list of contextual facets to clone
     * @return List<TLContextualFacet>
     */
    private List<TLContextualFacet> cloneContextualFacets(List<TLContextualFacet> facetList) {
        ObjectTransformer<TLContextualFacet, FacetContextual, SymbolResolverTransformerContext> sourceTransformer =
        		sourceTransformerFactory.getTransformer(TLContextualFacet.class, FacetContextual.class);
        ObjectTransformer<FacetContextual, TLContextualFacet, DefaultTransformerContext> targetTransformer =
        		targetTransformerFactory.getTransformer(FacetContextual.class, TLContextualFacet.class);
    	List<TLContextualFacet> clonedFacets = new ArrayList<>();
    	
    	for (TLContextualFacet sourceFacet : facetList) {
    		if (sourceFacet.isLocalFacet()) {
                FacetContextual intermediateFacet = sourceTransformer.transform( sourceFacet );
                TLContextualFacet clonedFacet = targetTransformer.transform( intermediateFacet );
        		List<TLContextualFacet> clonedChildren = cloneContextualFacets( sourceFacet.getChildFacets() );
                
        		for (TLContextualFacet clonedChild : clonedChildren) {
        			clonedFacet.addChildFacet( clonedChild );
        		}
        		clonedFacets.add( clonedFacet );
    		}
    	}
        return clonedFacets;
    }

    /**
     * Utility method that adds the given cloned entity to the specified target library.  In most
     * cases, this is a simple call to <code>targetLibrary.addNamedMember()</code>.  For entities
     * that include contextual facets, however, the facets must also be added to the library as
     * separate entities.  Since only local facets are cloned, this method assumes that any facet
     * not already assigned a library owner should be added to the same target library as the cloned
     * entity.
     * 
     * @param clonedEntity  the entity that was cloned
     * @param targetLibrary  the target library to which the entity should be added
     */
    public static void addToLibrary(LibraryMember clonedEntity, TLLibrary targetLibrary) {
    	targetLibrary.addNamedMember( clonedEntity );
    	
    	if (clonedEntity instanceof TLChoiceObject) {
    		addToLibrary( ((TLChoiceObject) clonedEntity).getChoiceFacets(), targetLibrary );
    		
    	} else if (clonedEntity instanceof TLBusinessObject) {
    		TLBusinessObject clonedBO = (TLBusinessObject) clonedEntity;
    		
    		addToLibrary( clonedBO.getCustomFacets(), targetLibrary );
    		addToLibrary( clonedBO.getQueryFacets(), targetLibrary );
    		addToLibrary( clonedBO.getUpdateFacets(), targetLibrary );
    	}
    }
    
    /**
     * Recursively adds any contextual facets to the specified target library that have not already
     * been assigned a library owner.
     * 
     * @param facetList  the list of facets to add
     * @param targetLibrary  the library to which the contextual facets will be added
     */
    private static void addToLibrary(List<TLContextualFacet> facetList, TLLibrary targetLibrary) {
    	for (TLContextualFacet facet : facetList) {
    		if (facet.getOwningLibrary() == null) {
    			targetLibrary.addNamedMember( facet );
    		}
    		addToLibrary( facet.getChildFacets(), targetLibrary );
    	}
    }
    
    /**
     * Transformer factory that wraps each of the <code>ObjectTransformer</code> instances produced
     * with either a <code>TypeMappingTransformer</code> or <code>TypeAssignmentTransformer</code>,
     * depending upon the type of the source object.
     */
    private class TypeMappingTransformerFactory<C extends ObjectTransformerContext> extends
            TransformerFactory<C> {

        private Map<Object, NamedEntity> typeAssignments;
        private TransformerFactory<C> delegateFactory;

        /**
         * Constructor that assigns the delegate transformer factory.
         * 
         * @param delegateFactory
         *            the delegate transformer factory
         */
        public TypeMappingTransformerFactory(TransformerFactory<C> delegateFactory) {
            this.delegateFactory = delegateFactory;
        }

        /**
         * @see org.opentravel.schemacompiler.transform.TransformerFactory#setContext(org.opentravel.schemacompiler.transform.ObjectTransformerContext)
         */
        @Override
        public void setContext(C transformerContext) {
            super.setContext(transformerContext);
        }

        /**
         * Assigns the map instance to use for type assignments collected and assigned by the
         * transformers returned by this factory.
         * 
         * @param typeAssignments
         *            the map instance to use for handling type assignments
         */
        public void setTypeAssignments(Map<Object, NamedEntity> typeAssignments) {
            this.typeAssignments = typeAssignments;
        }

        /**
         * @see org.opentravel.schemacompiler.transform.TransformerFactory#getTransformer(java.lang.Class,
         *      java.lang.Class)
         */
        @Override
        public <S, T> ObjectTransformer<S, T, C> getTransformer(Class<S> sourceType,
                Class<T> targetType) {
            ObjectTransformer<S, T, C> delegateTransformer = delegateFactory.getTransformer(
                    sourceType, targetType);
            ObjectTransformer<S, T, C> transformer;

            if (TLModelElement.class.isAssignableFrom(sourceType)) {
                transformer = new TypeMappingTransformer<S, T, C>(delegateTransformer,
                        typeAssignments);
            } else {
                transformer = new TypeAssignmentTransformer<S, T, C>(delegateTransformer,
                        typeAssignments);
            }
            transformer.setContext(getContext());
            return transformer;
        }

    }

    /**
     * Wrapper class for a transformer that captures the type assignments for the source element
     * being transformed.
     */
    private class TypeMappingTransformer<S, T, C extends ObjectTransformerContext> implements
            ObjectTransformer<S, T, C> {

        private Map<Object, NamedEntity> typeAssignments;
        private ObjectTransformer<S, T, C> delegate;

        /**
         * Constructor that assigns the delegate transformer and the mapping of type assignments.
         * 
         * @param delegate
         *            the delegate transformer that will perform the actual object transformation
         * @param typeAssignments
         *            the mappings of type assignments for the entities being transformed
         * @param typeNameAssignments
         *            the mappings of type names for the entities being transformed
         */
        public TypeMappingTransformer(ObjectTransformer<S, T, C> delegate,
                Map<Object, NamedEntity> typeAssignments) {
            this.typeAssignments = typeAssignments;
            this.delegate = delegate;
        }

        /**
         * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
         */
        @Override
        public T transform(S source) {
            T intermediateObject = delegate.transform(source);

            if (source instanceof TLSimple) {
                typeAssignments.put(intermediateObject, ((TLSimple) source).getParentType());

            } else if (source instanceof TLSimpleFacet) {
                typeAssignments.put(intermediateObject, ((TLSimpleFacet) source).getSimpleType());

            } else if (source instanceof TLValueWithAttributes) {
                typeAssignments.put(intermediateObject,
                        ((TLValueWithAttributes) source).getParentType());

            } else if (source instanceof TLAttribute) {
                typeAssignments.put(intermediateObject, ((TLAttribute) source).getType());

            } else if (source instanceof TLProperty) {
                typeAssignments.put(intermediateObject, ((TLProperty) source).getType());

            } else if (source instanceof TLExtension) {
                typeAssignments.put(intermediateObject, ((TLExtension) source).getExtendsEntity());
            }
            return intermediateObject;
        }

        /**
         * @see org.opentravel.schemacompiler.transform.ObjectTransformer#setContext(org.opentravel.schemacompiler.transform.ObjectTransformerContext)
         */
        @Override
        public void setContext(C context) {
            delegate.setContext(context);
        }

    }

    /**
     * Wrapper class for a transformer that re-assigns the type assignments for the target element
     * being transformed (originally captured from the element being cloned).
     */
    private class TypeAssignmentTransformer<S, T, C extends ObjectTransformerContext> implements
            ObjectTransformer<S, T, C> {

        private Map<Object, NamedEntity> typeAssignments;
        private ObjectTransformer<S, T, C> delegate;

        /**
         * Constructor that assigns the delegate transformer and the mapping of type assignments.
         * 
         * @param delegate
         *            the delegate transformer that will perform the actual object transformation
         * @param typeAssignments
         *            the mappings of type assignments for the entities being transformed
         */
        public TypeAssignmentTransformer(ObjectTransformer<S, T, C> delegate,
                Map<Object, NamedEntity> typeAssignments) {
            this.typeAssignments = typeAssignments;
            this.delegate = delegate;
        }

        /**
         * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
         */
        @Override
        public T transform(S source) {
            T targetObject = delegate.transform(source);

            if (targetObject instanceof TLSimple) {
                ((TLSimple) targetObject).setParentType((TLAttributeType) typeAssignments
                        .get(source));

            } else if (targetObject instanceof TLSimpleFacet) {
                ((TLSimpleFacet) targetObject).setSimpleType((TLAttributeType) typeAssignments
                        .get(source));

            } else if (targetObject instanceof TLValueWithAttributes) {
                ((TLValueWithAttributes) targetObject)
                        .setParentType((TLAttributeType) typeAssignments.get(source));

            } else if (targetObject instanceof TLAttribute) {
                ((TLAttribute) targetObject).setType((TLPropertyType) typeAssignments.get(source));

            } else if (targetObject instanceof TLProperty) {
                ((TLProperty) targetObject).setType((TLPropertyType) typeAssignments.get(source));

            } else if (targetObject instanceof TLExtension) {
                ((TLExtension) targetObject).setExtendsEntity(typeAssignments.get(source));
            }
            return targetObject;
        }

        /**
         * @see org.opentravel.schemacompiler.transform.ObjectTransformer#setContext(org.opentravel.schemacompiler.transform.ObjectTransformerContext)
         */
        @Override
        public void setContext(C context) {
            delegate.setContext(context);
        }

    }

    /**
     * Initializes the mappings of intermediate object types used by the cloning routine.
     */
    static {
        try {
            Map<Class<?>, Class<?>> iotMap = new HashMap<Class<?>, Class<?>>();

            iotMap.put(TLActionFacet.class, FacetAction.class);
            iotMap.put(TLAction.class, Action.class);
            iotMap.put(TLActionRequest.class, ActionRequest.class);
            iotMap.put(TLActionResponse.class, ActionResponse.class);
            iotMap.put(TLAttribute.class, Attribute.class);
            iotMap.put(TLBusinessObject.class, BusinessObject.class);
            iotMap.put(TLClosedEnumeration.class, EnumerationClosed.class);
            iotMap.put(TLContext.class, ContextDeclaration.class);
            iotMap.put(TLCoreObject.class, CoreObject.class);
            iotMap.put(TLChoiceObject.class, ChoiceObject.class);
            iotMap.put(TLDocumentation.class, Documentation.class);
            iotMap.put(TLEnumValue.class, EnumValue.class);
            iotMap.put(TLEquivalent.class, Equivalent.class);
            iotMap.put(TLExample.class, Example.class);
            iotMap.put(TLExtension.class, Extension.class);
            iotMap.put(TLExtensionPointFacet.class, ExtensionPointFacet.class);
            iotMap.put(TLFacet.class, Facet.class);
            iotMap.put(TLContextualFacet.class, FacetContextual.class);
            iotMap.put(TLIndicator.class, Indicator.class);
            iotMap.put(TLOpenEnumeration.class, EnumerationOpen.class);
            iotMap.put(TLOperation.class, Operation.class);
            iotMap.put(TLParamGroup.class, ParamGroup.class);
            iotMap.put(TLParameter.class, Parameter.class);
            iotMap.put(TLProperty.class, Property.class);
            iotMap.put(TLResource.class, Resource.class);
            iotMap.put(TLResourceParentRef.class, ResourceParentRef.class);
            iotMap.put(TLRole.class, Role.class);
            iotMap.put(TLService.class, Service.class);
            iotMap.put(TLSimpleFacet.class, SimpleFacet.class);
            iotMap.put(TLSimple.class, Simple.class);
            iotMap.put(TLValueWithAttributes.class, ValueWithAttributes.class);
            intermediateObjectTypes = Collections.unmodifiableMap(iotMap);

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}

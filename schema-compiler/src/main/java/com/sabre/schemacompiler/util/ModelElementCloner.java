/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opentravel.ns.ota2.librarymodel_v01_04.Attribute;
import org.opentravel.ns.ota2.librarymodel_v01_04.BusinessObject;
import org.opentravel.ns.ota2.librarymodel_v01_04.ContextDeclaration;
import org.opentravel.ns.ota2.librarymodel_v01_04.CoreObject;
import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.EnumValue;
import org.opentravel.ns.ota2.librarymodel_v01_04.EnumerationClosed;
import org.opentravel.ns.ota2.librarymodel_v01_04.EnumerationOpen;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Example;
import org.opentravel.ns.ota2.librarymodel_v01_04.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_04.ExtensionPointFacet;
import org.opentravel.ns.ota2.librarymodel_v01_04.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_04.FacetContextual;
import org.opentravel.ns.ota2.librarymodel_v01_04.Indicator;
import org.opentravel.ns.ota2.librarymodel_v01_04.Operation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Property;
import org.opentravel.ns.ota2.librarymodel_v01_04.Role;
import org.opentravel.ns.ota2.librarymodel_v01_04.Service;
import org.opentravel.ns.ota2.librarymodel_v01_04.Simple;
import org.opentravel.ns.ota2.librarymodel_v01_04.SimpleFacet;
import org.opentravel.ns.ota2.librarymodel_v01_04.ValueWithAttributes;

import com.sabre.schemacompiler.ioc.SchemaCompilerApplicationContext;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.LibraryElement;
import com.sabre.schemacompiler.model.ModelElement;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLAttributeType;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLContext;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLOpenEnumeration;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLPropertyType;
import com.sabre.schemacompiler.model.TLRole;
import com.sabre.schemacompiler.model.TLService;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.ObjectTransformerContext;
import com.sabre.schemacompiler.transform.SymbolTable;
import com.sabre.schemacompiler.transform.TransformerFactory;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import com.sabre.schemacompiler.transform.symbols.SymbolTableFactory;
import com.sabre.schemacompiler.transform.tl2jaxb.TL2JaxbLibrarySymbolResolver;
import com.sabre.schemacompiler.transform.util.LibraryPrefixResolver;

/**
 * Generic cloning utility that uses the <code>ObjectTransformer</code> functions to
 * construct copies of model element instances.
 * 
 * <p>NOTE: The implementation of this class is NOT thread-safe.
 * 
 * @author S. Livezey
 */
public class ModelElementCloner {
	
	private static final Map<Class<?>,Class<?>> intermediateObjectTypes;
	
	private SymbolTable modelSymbols;
	private SymbolResolverTransformerContext sourceTransformContext;
	private DefaultTransformerContext targetTransformContext;
	private TypeMappingTransformerFactory<SymbolResolverTransformerContext> sourceTransformerFactory;
	private TypeMappingTransformerFactory<DefaultTransformerContext> targetTransformerFactory;
	
	/**
	 * that can be cloned.
	 * Constructor that provides access to the global model that owns all possible entities
	 * 
	 * @param model  the owning model instance for all clonable entities
	 */
	public ModelElementCloner(TLModel model) {
		this.modelSymbols = SymbolTableFactory.newSymbolTableFromModel(model);
		
		this.sourceTransformContext = new SymbolResolverTransformerContext();
		this.sourceTransformContext.setSymbolResolver( new TL2JaxbLibrarySymbolResolver(modelSymbols) );
		this.sourceTransformerFactory = new TypeMappingTransformerFactory<SymbolResolverTransformerContext>(
				TransformerFactory.getInstance( SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY, sourceTransformContext) );
		this.sourceTransformerFactory.setContext( this.sourceTransformContext );
		
		this.targetTransformContext = new DefaultTransformerContext();
		this.targetTransformerFactory = new TypeMappingTransformerFactory<DefaultTransformerContext>(
				TransformerFactory.getInstance( SchemaCompilerApplicationContext.LOADER_TRANSFORMER_FACTORY, targetTransformContext) );
		this.targetTransformerFactory.setContext( this.targetTransformContext );
	}
	
	/**
	 * Constructs a deep clone of the given object using the transformation module of the compiler.
	 * 
	 * @param source  the source object to clone
	 * @return C
	 * @throws IllegalArgumentException  thrown if the given source object cannot be cloned
	 */
	public <C extends ModelElement,I> C clone(C source) {
		C clonedObject = null;
		
		if (source instanceof LibraryElement) {
			clonedObject = clone( source, ((LibraryElement) source).getOwningLibrary() );
		}
		return clonedObject;
	}
	
	/**
	 * Constructs a deep clone of the given object using the transformation module of the compiler.
	 * 
	 * @param source  the source object to clone
	 * @param resolverContext  the library whose owning that should be used for reference lookups when
	 *						   resolving names in the cloned entity; the library itself is used to resolve
	 *						   namespace prefix references during reference lookups
	 * @return C
	 * @throws IllegalArgumentException  thrown if the given source object cannot be cloned
	 */
	@SuppressWarnings("unchecked")
	public <C extends ModelElement,I> C clone(C source, AbstractLibrary resolverContext) {
		C clonedObject = null;
		
		if (source instanceof LibraryElement) {
			Class<I> intermediateType = (Class<I>) intermediateObjectTypes.get( source.getClass() );
			I intermediateObject;
			
			if (intermediateType == null) {
				throw new IllegalArgumentException("Unable to clone object of type: " + source.getClass().getName());
			}
			
			// Use the transformer subsystem to create the clone of the object
			Map<Object,NamedEntity> typeAssignments = new HashMap<Object,NamedEntity>();
			
			sourceTransformerFactory.setTypeAssignments(typeAssignments);
			targetTransformerFactory.setTypeAssignments(typeAssignments);
			
			ObjectTransformer<C,I,SymbolResolverTransformerContext> sourceTransformer =
					sourceTransformerFactory.getTransformer(source, intermediateType);
			ObjectTransformer<I,C,DefaultTransformerContext> targetTransformer =
					targetTransformerFactory.getTransformer(intermediateType, (Class<C>) source.getClass());
			
			if (resolverContext != null) {
				sourceTransformContext.getSymbolResolver().setPrefixResolver( new LibraryPrefixResolver(resolverContext) );
			}
			intermediateObject = sourceTransformer.transform( source );
			clonedObject = targetTransformer.transform( intermediateObject );
			
		} else if (source != null) {
			throw new IllegalArgumentException("Unable to clone object of type: " + source.getClass().getName());
		}
		return clonedObject;
	}
	
	/**
	 * Transformer factory that wraps each of the <code>ObjectTransformer</code> instances produced with either
	 * a <code>TypeMappingTransformer</code> or <code>TypeAssignmentTransformer</code>, depending upon the type
	 * of the source object.
	 */
	private class TypeMappingTransformerFactory<C extends ObjectTransformerContext> extends TransformerFactory<C> {
		
		private Map<Object,NamedEntity> typeAssignments;
		private TransformerFactory<C> delegateFactory;
		
		/**
		 * Constructor that assigns the delegate transformer factory.
		 * 
		 * @param delegateFactory  the delegate transformer factory
		 */
		public TypeMappingTransformerFactory(TransformerFactory<C> delegateFactory) {
			this.delegateFactory = delegateFactory;
		}
		
		/**
		 * @see com.sabre.schemacompiler.transform.TransformerFactory#setContext(com.sabre.schemacompiler.transform.ObjectTransformerContext)
		 */
		@Override
		public void setContext(C transformerContext) {
			super.setContext(transformerContext);
		}

		/**
		 * Assigns the map instance to use for type assignments collected and assigned by the transformers
		 * returned by this factory.
		 * 
		 * @param typeAssignments  the map instance to use for handling type assignments
		 */
		public void setTypeAssignments(Map<Object,NamedEntity> typeAssignments) {
			this.typeAssignments = typeAssignments;
		}

		/**
		 * @see com.sabre.schemacompiler.transform.TransformerFactory#getTransformer(java.lang.Class, java.lang.Class)
		 */
		@Override
		public <S, T> ObjectTransformer<S, T, C> getTransformer(Class<S> sourceType, Class<T> targetType) {
			ObjectTransformer<S,T,C> delegateTransformer = delegateFactory.getTransformer(sourceType, targetType);
			ObjectTransformer<S,T,C> transformer;
			
			if (TLModelElement.class.isAssignableFrom(sourceType)) {
				transformer = new TypeMappingTransformer<S, T, C>(delegateTransformer, typeAssignments);
			} else {
				transformer = new TypeAssignmentTransformer<S, T, C>(delegateTransformer, typeAssignments);
			}
			transformer.setContext( getContext() );
			return transformer;
		}
		
	}
	
	/**
	 * Wrapper class for a transformer that captures the type assignments for the source element
	 * being transformed.
	 */
	private class TypeMappingTransformer<S,T,C extends ObjectTransformerContext> implements ObjectTransformer<S,T,C> {
		
		private Map<Object,NamedEntity> typeAssignments;
		private ObjectTransformer<S,T,C> delegate;
		
		/**
		 * Constructor that assigns the delegate transformer and the mapping of type assignments.
		 * 
		 * @param delegate  the delegate transformer that will perform the actual object transformation
		 * @param typeAssignments  the mappings of type assignments for the entities being transformed
		 * @param typeNameAssignments  the mappings of type names for the entities being transformed
		 */
		public TypeMappingTransformer(ObjectTransformer<S,T,C> delegate, Map<Object,NamedEntity> typeAssignments) {
			this.typeAssignments = typeAssignments;
			this.delegate = delegate;
		}
		
		/**
		 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
		 */
		@Override
		public T transform(S source) {
			T intermediateObject = delegate.transform(source);
			
			if (source instanceof TLSimple) {
				typeAssignments.put( intermediateObject, ((TLSimple) source).getParentType() );
				
			} else if (source instanceof TLSimpleFacet) {
				typeAssignments.put( intermediateObject, ((TLSimpleFacet) source).getSimpleType() );
				
			} else if (source instanceof TLValueWithAttributes) {
				typeAssignments.put( intermediateObject, ((TLValueWithAttributes) source).getParentType() );
				
			} else if (source instanceof TLAttribute) {
				typeAssignments.put( intermediateObject, ((TLAttribute) source).getType() );
				
			} else if (source instanceof TLProperty) {
				typeAssignments.put( intermediateObject, ((TLProperty) source).getType() );
				
			} else if (source instanceof TLExtension) {
				typeAssignments.put( intermediateObject, ((TLExtension) source).getExtendsEntity() );
			}
			return intermediateObject;
		}

		/**
		 * @see com.sabre.schemacompiler.transform.ObjectTransformer#setContext(com.sabre.schemacompiler.transform.ObjectTransformerContext)
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
	private class TypeAssignmentTransformer<S,T,C extends ObjectTransformerContext> implements ObjectTransformer<S,T,C> {
		
		private Map<Object,NamedEntity> typeAssignments;
		private ObjectTransformer<S,T,C> delegate;
		
		/**
		 * Constructor that assigns the delegate transformer and the mapping of type assignments.
		 * 
		 * @param delegate  the delegate transformer that will perform the actual object transformation
		 * @param typeAssignments  the mappings of type assignments for the entities being transformed
		 */
		public TypeAssignmentTransformer(ObjectTransformer<S,T,C> delegate, Map<Object,NamedEntity> typeAssignments) {
			this.typeAssignments = typeAssignments;
			this.delegate = delegate;
		}
		
		/**
		 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
		 */
		@Override
		public T transform(S source) {
			T targetObject = delegate.transform(source);
			
			if (targetObject instanceof TLSimple) {
				((TLSimple) targetObject).setParentType( (TLAttributeType) typeAssignments.get(source) );
				
			} else if (targetObject instanceof TLSimpleFacet) {
				((TLSimpleFacet) targetObject).setSimpleType( (TLAttributeType) typeAssignments.get(source) );
				
			} else if (targetObject instanceof TLValueWithAttributes) {
				((TLValueWithAttributes) targetObject).setParentType( (TLAttributeType) typeAssignments.get(source) );
				
			} else if (targetObject instanceof TLAttribute) {
				((TLAttribute) targetObject).setType( (TLAttributeType) typeAssignments.get(source) );
				
			} else if (targetObject instanceof TLProperty) {
				((TLProperty) targetObject).setType( (TLPropertyType) typeAssignments.get(source) );
				
			} else if (targetObject instanceof TLExtension) {
				((TLExtension) targetObject).setExtendsEntity( typeAssignments.get(source) );
			}
			return targetObject;
		}

		/**
		 * @see com.sabre.schemacompiler.transform.ObjectTransformer#setContext(com.sabre.schemacompiler.transform.ObjectTransformerContext)
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
			Map<Class<?>,Class<?>> iotMap = new HashMap<Class<?>,Class<?>>();
			
			iotMap.put(TLAttribute.class, Attribute.class);
			iotMap.put(TLBusinessObject.class, BusinessObject.class);
			iotMap.put(TLClosedEnumeration.class, EnumerationClosed.class);
			iotMap.put(TLContext.class, ContextDeclaration.class);
			iotMap.put(TLCoreObject.class, CoreObject.class);
			iotMap.put(TLDocumentation.class, Documentation.class);
			iotMap.put(TLEnumValue.class, EnumValue.class);
			iotMap.put(TLEquivalent.class, Equivalent.class);
			iotMap.put(TLExample.class, Example.class);
			iotMap.put(TLExtension.class, Extension.class);
			iotMap.put(TLExtensionPointFacet.class, ExtensionPointFacet.class);
			iotMap.put(TLFacet.class, Facet.class);
			iotMap.put(TLFacet.class, FacetContextual.class);
			iotMap.put(TLIndicator.class, Indicator.class);
			iotMap.put(TLOpenEnumeration.class, EnumerationOpen.class);
			iotMap.put(TLOperation.class, Operation.class);
			iotMap.put(TLProperty.class, Property.class);
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

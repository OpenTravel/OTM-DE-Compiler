/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.sabre.schemacompiler.ioc.SchemaCompilerApplicationContext;

/**
 * Default implementation of the transformer factory that uses Java annotations to identify the
 * transformer implementations.
 * 
 * @param C  the type of context required by the transformers provided by the factory
 * @author S. Livezey
 */
public class TransformerFactory<C extends ObjectTransformerContext> {
	
	private static final Logger log = LoggerFactory.getLogger(TransformerFactory.class);
	
	private Map<Class<?>,Map<Class<?>,Class<?>>> sourceTypeMappings = new HashMap<Class<?>, Map<Class<?>,Class<?>>>();
	private C transformerContext;
	
	/**
	 * Returns the an instance of the <code>TransformerFactory</code> from the application context
	 * with the specified factory name.
	 * 
	 * @param factoryName  the bean ID of the factory instance from the application context
	 * @param transformerContext  the transformer context with which the new factory instance will be associated
	 * @return TransformerFactory
	 */
	@SuppressWarnings("unchecked")
	public static <C extends ObjectTransformerContext> TransformerFactory<C> getInstance(String factoryName, C transformerContext) {
		ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();
		TransformerFactory<C> factory = (TransformerFactory<C>) appContext.getBean(factoryName);
		
		factory.setContext(transformerContext);
		return factory;
	}
	
	/**
	 * Returns the transformer context for this factory instance.
	 * 
	 * @return C
	 */
	public C getContext() {
		return transformerContext;
	}
	
	/**
	 * Assigns the transformer context for this factory instance.
	 * 
	 * @param transformerContext  the transformer context to assign
	 */
	public void setContext(C transformerContext) {
		if (transformerContext != null) {
			transformerContext.setTransformerFactory(this);
		}
		this.transformerContext = transformerContext;
	}
	
	/**
	 * Assigns the transformer mappings to be used by this factory instance.
	 * 
	 * @param mappings  the mapping specifications for this transformer
	 */
	public void setTransformerMappings(Collection<TransformerMapping> mappings) {
		sourceTypeMappings.clear();
		
		for (TransformerMapping mapping : mappings) {
			Map<Class<?>,Class<?>> targetTypeMappings = sourceTypeMappings.get(mapping.getSource());
			
			if (targetTypeMappings == null) {
				targetTypeMappings = new HashMap<Class<?>, Class<?>>();
				sourceTypeMappings.put(mapping.getSource(), targetTypeMappings);
			}
			targetTypeMappings.put(mapping.getTarget(), mapping.getTransformer());
		}
	}
	
	/**
	 * Returns a target type that is mapped to the specified source object type.  If multiple
	 * targets are mapped to the specified source type, the first-available mapping will
	 * be returned.
	 * 
	 * @param sourceObj  the source object for which to return a mapping
	 * @return Set<Class<?>>
	 */
	@SuppressWarnings("unchecked")
	public Set<Class<?>> findTargetTypes(Object sourceObj) {
		return (sourceObj == null) ? Collections.EMPTY_SET : findTargetTypes(sourceObj.getClass());
	}
	
	/**
	 * Returns a target type that is mapped to the specified source type.  If multiple
	 * targets are mapped to the specified source type, the first-available mapping will
	 * be returned.
	 * 
	 * @param sourceType  the source type for which to return a mapping
	 * @return Set<Class<?>>
	 */
	public Set<Class<?>> findTargetTypes(Class<?> sourceType) {
		Map<Class<?>,Class<?>> targetTypeMappings = sourceTypeMappings.get(sourceType);
		Set<Class<?>> targetTypes;
		
		if ((targetTypeMappings != null) && !targetTypeMappings.isEmpty()) {
			targetTypes = targetTypeMappings.keySet();
		} else {
			targetTypes = new HashSet<Class<?>>();
		}
		return targetTypes;
	}
	
	/**
	 * Returns an <code>ObjectTransformer</code> that will transform the given source object into
	 * an object of the requested target type.  If a qualifying transformer cannot be identified
	 * or created, this method will return null.
	 * 
	 * @param <S>  the source type of the object transformation
	 * @param <T>  the target type of the object transformation
	 * @param sourceObject  the source object to be converted
	 * @param targetType  the target object type for the transformation
	 * @return ObjectTransformer<S,T,C>
	 */
	@SuppressWarnings("unchecked")
	public <S,T> ObjectTransformer<S,T,C> getTransformer(S sourceObject, Class<T> targetType) {
		ObjectTransformer<S,T,C> transformer = null;
		
		if (sourceObject != null) {
			transformer = getTransformer((Class<S>)sourceObject.getClass(), targetType);
		}
		return transformer;
	}
	
	/**
	 * Returns an <code>ObjectTransformer</code> that will transform an object of the specified source
	 * type into an object of the requested target type.  If a qualifying transformer cannot be identified
	 * or created, this method will return null.
	 * 
	 * @param <S>  the source type of the object transformation
	 * @param <T>  the target type of the object transformation
	 * @param sourceType  the source object type for the transformation
	 * @param targetType  the target object type for the transformation
	 * @return ObjectTransformer<S,T,C>
	 */
	@SuppressWarnings("unchecked")
	public <S,T> ObjectTransformer<S,T,C> getTransformer(Class<S> sourceType, Class<T> targetType) {
		Map<Class<?>,Class<?>> targetTypeMappings = sourceTypeMappings.get(sourceType);
		ObjectTransformer<S,T,C> transformer = null;
		
		if (targetTypeMappings != null) {
			Class<ObjectTransformer<S,T,C>> transformerClass = (Class<ObjectTransformer<S,T,C>>) targetTypeMappings.get(targetType);
			
			try {
				if (transformerClass != null) {
					transformer = transformerClass.newInstance();
					transformer.setContext(transformerContext);
				}
			} catch (Throwable t) {
				log.error("Unable to instantiate transformer for type: " + sourceType.getName(), t);
			}
		}
		return transformer;
	}
	
}

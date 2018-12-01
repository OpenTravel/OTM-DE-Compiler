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
package org.opentravel.schemacompiler.transform;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Default implementation of the transformer factory that uses Java annotations to identify the
 * transformer implementations.
 * 
 * @param C
 *            the type of context required by the transformers provided by the factory
 * @author S. Livezey
 */
public class TransformerFactory<C extends ObjectTransformerContext> {

    private static final Logger log = LoggerFactory.getLogger(TransformerFactory.class);

    private Map<Class<?>,Map<Class<?>,Class<?>>> sourceTypeMappings = new HashMap<>();
    private C transformerContext;

    /**
     * Returns the an instance of the <code>TransformerFactory</code> from the application context
     * with the specified factory name.
     * 
     * @param factoryName
     *            the bean ID of the factory instance from the application context
     * @param transformerContext
     *            the transformer context with which the new factory instance will be associated
     * @return TransformerFactory
     */
    @SuppressWarnings("unchecked")
    public static <C extends ObjectTransformerContext> TransformerFactory<C> getInstance(
            String factoryName, C transformerContext) {
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
     * @param transformerContext
     *            the transformer context to assign
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
     * @param mappings
     *            the mapping specifications for this transformer
     */
    public void setTransformerMappings(Collection<TransformerMapping> mappings) {
        sourceTypeMappings.clear();

        for (TransformerMapping mapping : mappings) {
            Map<Class<?>, Class<?>> targetTypeMappings = sourceTypeMappings
                    .get(mapping.getSource());

            if (targetTypeMappings == null) {
                targetTypeMappings = new HashMap<>();
                sourceTypeMappings.put(mapping.getSource(), targetTypeMappings);
            }
            targetTypeMappings.put(mapping.getTarget(), mapping.getTransformer());
        }
    }
    
    /**
     * Returns the list of all source-to-target type mappings registered for this factory.
     * 
     * @return Map<Class<?>,Set<Class<?>>>
     */
    public Map<Class<?>,Set<Class<?>>> getTypeMappings() {
    	Map<Class<?>,Set<Class<?>>> mappings = new HashMap<>();
    	
    	for (Class<?> sourceType : sourceTypeMappings.keySet()) {
    		Map<Class<?>,Class<?>> targetTypes = sourceTypeMappings.get( sourceType );
    		
    		if (targetTypes != null) {
        		mappings.put( sourceType, new HashSet<>( targetTypes.keySet() ) );
    		}
    	}
    	return Collections.unmodifiableMap( mappings );
    }

    /**
     * Returns a target type that is mapped to the specified source object type. If multiple targets
     * are mapped to the specified source type, the first-available mapping will be returned.
     * 
     * @param sourceObj
     *            the source object for which to return a mapping
     * @return Set<Class<?>>
     */
    public Set<Class<?>> findTargetTypes(Object sourceObj) {
        return (sourceObj == null) ? Collections.emptySet() : findTargetTypes(sourceObj.getClass());
    }

    /**
     * Returns a target type that is mapped to the specified source type. If multiple targets are
     * mapped to the specified source type, the first-available mapping will be returned.
     * 
     * @param sourceType
     *            the source type for which to return a mapping
     * @return Set<Class<?>>
     */
    public Set<Class<?>> findTargetTypes(Class<?> sourceType) {
        Map<Class<?>, Class<?>> targetTypeMappings = sourceTypeMappings.get(sourceType);
        Set<Class<?>> targetTypes;

        if ((targetTypeMappings != null) && !targetTypeMappings.isEmpty()) {
            targetTypes = targetTypeMappings.keySet();
        } else {
            targetTypes = new HashSet<>();
        }
        return targetTypes;
    }

    /**
     * Returns an <code>ObjectTransformer</code> that will transform the given source object into an
     * object of the requested target type. If a qualifying transformer cannot be identified or
     * created, this method will return null.
     * 
     * @param <S>
     *            the source type of the object transformation
     * @param <T>
     *            the target type of the object transformation
     * @param sourceObject
     *            the source object to be converted
     * @param targetType
     *            the target object type for the transformation
     * @return ObjectTransformer<S,T,C>
     */
    @SuppressWarnings("unchecked")
    public <S, T> ObjectTransformer<S, T, C> getTransformer(S sourceObject, Class<T> targetType) {
        ObjectTransformer<S, T, C> transformer = null;

        if (sourceObject != null) {
            transformer = getTransformer((Class<S>) sourceObject.getClass(), targetType);
        }
        return transformer;
    }

    /**
     * Returns an <code>ObjectTransformer</code> that will transform an object of the specified
     * source type into an object of the requested target type. If a qualifying transformer cannot
     * be identified or created, this method will return null.
     * 
     * @param <S>
     *            the source type of the object transformation
     * @param <T>
     *            the target type of the object transformation
     * @param sourceType
     *            the source object type for the transformation
     * @param targetType
     *            the target object type for the transformation
     * @return ObjectTransformer<S,T,C>
     */
    @SuppressWarnings("unchecked")
    public <S, T> ObjectTransformer<S, T, C> getTransformer(Class<S> sourceType, Class<T> targetType) {
        Map<Class<?>, Class<?>> targetTypeMappings = sourceTypeMappings.get(sourceType);
        ObjectTransformer<S, T, C> transformer = null;

        if (targetTypeMappings != null) {
            Class<ObjectTransformer<S, T, C>> transformerClass = (Class<ObjectTransformer<S, T, C>>) targetTypeMappings
                    .get(targetType);

            try {
                if (transformerClass != null) {
                    transformer = transformerClass.newInstance();
                    transformer.setContext(transformerContext);
                }
            } catch (Exception e) {
                log.error("Unable to instantiate transformer for type: " + sourceType.getName(), e);
            }
        }
        return transformer;
    }

}

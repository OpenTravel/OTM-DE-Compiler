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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Provides a mechanism for defining and applying a <code>Function</code> on a class-specific basis.
 *
 * @param <R> the type of result to the function
 */
public class ClassSpecificFunction<R> {

    private Map<Class<?>,Function<?,R>> functionMap = new HashMap<>();

    /**
     * Builder method that adds a new class-specific function definition.
     * 
     * @param clazz the class to which the function will apply
     * @param function the function to be applied for the class
     * @param <T> the type of the object to which the function will be applied
     * @return ClassSpecificFunction&lt;T,R&gt;
     */
    public <T> ClassSpecificFunction<R> addFunction(Class<T> clazz, Function<T,R> function) {
        functionMap.put( clazz, function );
        return this;
    }

    /**
     * Returns true if a function can be applied for the specified class.
     * 
     * @param obj the object to check for function applicability
     * @return boolean
     */
    public boolean canApply(Object obj) {
        Class<?> clazz = (obj == null) ? null : obj.getClass();
        boolean applies = false;

        while (!applies && (clazz != null)) {
            applies = functionMap.containsKey( obj.getClass() );
            clazz = clazz.getSuperclass();
        }
        return applies;
    }

    /**
     * Applies the function to the object provided.
     * 
     * @param obj the object to which the function should be applied
     * @return R
     * @param <T> the type of input to the function
     * @throws IllegalArgumentException thrown if a function is not defined for an object of the given type (use
     *         <code>canApply()</code> to avoid)
     */
    @SuppressWarnings("unchecked")
    public <T> R apply(T obj) {
        Class<?> clazz = (obj == null) ? null : obj.getClass();
        Function<T,R> function = null;

        while ((function == null) && (clazz != null)) {
            function = (Function<T,R>) functionMap.get( clazz );
            clazz = clazz.getSuperclass();
        }

        if (function == null) {
            String objType = (obj == null) ? "[NULL VALUE]" : obj.getClass().getName();

            throw new IllegalArgumentException( "Function not defined for object of type: " + objType );
        }
        return function.apply( obj );
    }

}

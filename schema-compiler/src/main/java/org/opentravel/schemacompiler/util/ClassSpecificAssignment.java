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

/**
 * Provides a mechanism for defining and applying an <code>Assignment</code> on a class-specific basis.
 *
 * @param <V> the type of the value being assigned
 */
public class ClassSpecificAssignment<V> {

    private Map<Class<?>,Assignment<?,V>> assignmentMap = new HashMap<>();

    /**
     * Builder method that adds a new class-specific assignment definition.
     * 
     * @param clazz the class to which the function will apply
     * @param assignment the assignment to be applied for the class
     * @param <T> the type of the object to which the assignment will be performed
     * @return ClassSpecificFunction&lt;T,R&gt;
     */
    public <T> ClassSpecificAssignment<V> addAssignment(Class<T> clazz, Assignment<T,V> assignment) {
        assignmentMap.put( clazz, assignment );
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
            applies = assignmentMap.containsKey( obj.getClass() );
            clazz = clazz.getSuperclass();
        }
        return applies;
    }

    /**
     * Applies the assignment to the target object provided.
     * 
     * @param targetObj the target object to which the value should be assigned
     * @param value the value to be assigned
     * @param <T> the type the target object for the assignment
     * @throws IllegalArgumentException thrown if an assignment is not defined for an object of the given type (use
     *         <code>canApply()</code> to avoid)
     */
    @SuppressWarnings("unchecked")
    public <T> void apply(T targetObj, V value) {
        Class<?> clazz = (targetObj == null) ? null : targetObj.getClass();
        Assignment<T,V> assignment = null;

        while ((assignment == null) && (clazz != null)) {
            assignment = (Assignment<T,V>) assignmentMap.get( clazz );
            clazz = clazz.getSuperclass();
        }

        if (assignment == null) {
            String objType = (targetObj == null) ? "[NULL VALUE]" : targetObj.getClass().getName();

            throw new IllegalArgumentException( "Assignment not defined for object of type: " + objType );
        }
        assignment.apply( targetObj, value );
    }

}

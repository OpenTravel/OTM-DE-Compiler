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

/**
 * Functional interface that allows a value to be assigned to a particular
 * target object.
 *
 * @param <T>  the type of the target object to which the assignment will be applied
 * @param <V>  the type of the value being assigned
 */
public interface Assignment<T,V> {
	
	/**
	 * Performs the assignment of the given value to the target object provided.
	 * 
	 * @param targetObj  the target object to which the value will be assigned
	 * @param value  the value to be assigned
	 */
	public void apply(T targetObj, V value);
	
}

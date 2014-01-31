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
package org.opentravel.schemacompiler.model;

/**
 * Interface to be implemented by all model entities that are contained within a library.
 * 
 * @author S. Livezey
 */
public interface LibraryElement extends ModelElement {

    /**
     * Returns the library instance that owns this model element.
     * 
     * @return AbstractLibrary
     */
    public AbstractLibrary getOwningLibrary();

    /**
     * Creates a deep-copy of this model element. The only difference between this one and the new
     * copy is that the new copy will not yet be assigned to an owning library.
     * 
     * @return LibraryElement
     * @throws IllegalArgumentException
     *             thrown if this model element cannot be cloned
     */
    public LibraryElement cloneElement();

    /**
     * Creates a deep-copy of this model element. The only difference between this one and the new
     * copy is that the new copy will not yet be assigned to an owning library.
     * 
     * @param namingContext
     *            the library whose owning that should be used for reference lookups when resolving
     *            names in the cloned entity; the library itself is used to resolve namespace prefix
     *            references during reference lookups
     * @return LibraryElement
     * @throws IllegalArgumentException
     *             thrown if this model element cannot be cloned
     */
    public LibraryElement cloneElement(AbstractLibrary namingContext);

}

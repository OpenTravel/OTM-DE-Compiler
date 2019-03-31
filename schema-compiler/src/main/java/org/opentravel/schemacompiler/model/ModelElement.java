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

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.validate.Validatable;

import java.util.Collection;

/**
 * Basic interface implemented by all entities that are candidate members of a <code>TLModel</code> instance.
 * 
 * @author S. Livezey
 */
public interface ModelElement extends Validatable {

    /**
     * Returns the model that owns this member element.
     * 
     * @return TLModel
     */
    public TLModel getOwningModel();

    /**
     * Registers a listener for this model element.
     * 
     * @param listener the listener instance to register
     */
    public void addListener(ModelElementListener listener);

    /**
     * Unregisteres a listener from this model element.
     * 
     * @param listener the lister instance to unregister
     */
    public void removeListener(ModelElementListener listener);

    /**
     * Returns an unmodifiable collections of all listeners that have been registered with this model element.
     * 
     * @return Collection&lt;ModelElementListener&gt;
     */
    public Collection<ModelElementListener> getListeners();

    /**
     * Creates a deep-copy of this model element. The only difference between this one and the new copy is that the new
     * copy will not yet be assigned to an owning library.
     * 
     * @return TLModelElement
     * @throws IllegalArgumentException thrown if this model element cannot be cloned
     */
    public LibraryElement cloneElement();

    /**
     * Creates a deep-copy of this model element. The only difference between this one and the new copy is that the new
     * copy will not yet be assigned to an owning library.
     * 
     * @param namingContext the library whose owning that should be used for reference lookups when resolving names in
     *        the cloned entity; the library itself is used to resolve namespace prefix references during reference
     *        lookups
     * @return LibraryElement
     * @throws IllegalArgumentException thrown if this model element cannot be cloned
     */
    public LibraryElement cloneElement(AbstractLibrary namingContext);

}

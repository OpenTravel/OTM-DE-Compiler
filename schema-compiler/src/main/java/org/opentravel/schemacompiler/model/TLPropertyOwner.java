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

import java.util.Comparator;
import java.util.List;

/**
 * Interface to be implemented by all model components that can own properties.
 * 
 * @author S. Livezey
 */
public interface TLPropertyOwner extends NamedEntity, TLMemberFieldOwner {

    /**
     * Returns the value of the 'elements' field.
     * 
     * @return List&lt;TLProperty&gt;
     */
    public List<TLProperty> getElements();

    /**
     * Returns the element with the specified name.
     * 
     * @param elementName the name of the element to return
     * @return TLPropertyg
     */
    public TLProperty getElement(String elementName);

    /**
     * Adds a <code>TLProperty</code> element to the current list.
     * 
     * @param element the element value to add
     */
    public void addElement(TLProperty element);

    /**
     * Adds a <code>TLProperty</code> element to the current list.
     * 
     * @param index the index at which the given element should be added
     * @param element the element value to add
     * @throws IndexOutOfBoundsException thrown if the index is out of range (index &lt; 0 || index &gt; size())
     */
    public void addElement(int index, TLProperty element);

    /**
     * Removes the specified <code>TLProperty</code> from the current list.
     * 
     * @param element the element value to remove
     */
    public void removeProperty(TLProperty element);

    /**
     * Moves this element up by one position in the list. If the element is not owned by this object or it is already at
     * the front of the list, this method has no effect.
     * 
     * @param element the element to move
     */
    public void moveUp(TLProperty element);

    /**
     * Moves this element down by one position in the list. If the element is not owned by this object or it is already
     * at the end of the list, this method has no effect.
     * 
     * @param element the element to move
     */
    public void moveDown(TLProperty element);

    /**
     * Sorts the list of elements using the comparator provided.
     * 
     * @param comparator the comparator to use when sorting the list
     */
    public void sortElements(Comparator<TLProperty> comparator);

}

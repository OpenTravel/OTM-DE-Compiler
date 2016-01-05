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
 * Interface to be implemented by all model components that can own attributes.
 * 
 * @author S. Livezey
 */
public interface TLAttributeOwner extends NamedEntity, TLMemberFieldOwner {

    /**
     * Returns the value of the 'attributes' field.
     * 
     * @return List<TLAttribute>
     */
    public List<TLAttribute> getAttributes();

    /**
     * Returns the attribute with the specified name.
     * 
     * @param attributeName
     *            the name of the attribute to return
     * @return TLAttribute
     */
    public TLAttribute getAttribute(String attributeName);

    /**
     * Adds a <code>TLAttribute</code> element to the current list.
     * 
     * @param attribute
     *            the attribute value to add
     */
    public void addAttribute(TLAttribute attribute);

    /**
     * Adds a <code>TLAttribute</code> element to the current list.
     * 
     * @param index
     *            the index at which the given attribute should be added
     * @param attribute
     *            the attribute value to add
     * @throws IndexOutOfBoundsException
     *             thrown if the index is out of range (index < 0 || index > size())
     */
    public void addAttribute(int index, TLAttribute attribute);

    /**
     * Removes the specified <code>TLAttribute</code> from the current list.
     * 
     * @param attribute
     *            the attribute value to remove
     */
    public void removeAttribute(TLAttribute attribute);

    /**
     * Moves this attribute up by one position in the list. If the attribute is not owned by this
     * object or it is already at the front of the list, this method has no effect.
     * 
     * @param attribute
     *            the attribute to move
     */
    public void moveUp(TLAttribute attribute);

    /**
     * Moves this attribute down by one position in the list. If the attribute is not owned by this
     * object or it is already at the end of the list, this method has no effect.
     * 
     * @param attribute
     *            the attribute to move
     */
    public void moveDown(TLAttribute attribute);

    /**
     * Sorts the list of attributes using the comparator provided.
     * 
     * @param comparator
     *            the comparator to use when sorting the list
     */
    public void sortAttributes(Comparator<TLAttribute> comparator);

}

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
 * Interface to be implemented by any model component that can provide an EXAMPLE of its usage.
 * 
 * @author S. Livezey
 */
public interface TLExampleOwner extends LibraryElement {

    /**
     * Returns the value of the 'examples' field.
     * 
     * @return List<TLExample>
     */
    public List<TLExample> getExamples();

    /**
     * Returns the EXAMPLE with the specified context ID.
     * 
     * @param contextId
     *            the context of the EXAMPLE to return
     * @return TLExample
     */
    public TLExample getExample(String contextId);

    /**
     * Adds a <code>TLExample</code> element to the current list.
     * 
     * @param EXAMPLE
     *            the EXAMPLE value to add
     */
    public void addExample(TLExample example);

    /**
     * Adds a <code>TLExample</code> element to the current list.
     * 
     * @param index
     *            the index at which the given EXAMPLE should be added
     * @param EXAMPLE
     *            the EXAMPLE value to add
     * @throws IndexOutOfBoundsException
     *             thrown if the index is out of range (index < 0 || index > size())
     */
    public void addExample(int index, TLExample example);

    /**
     * Removes the specified <code>TLExample</code> from the current list.
     * 
     * @param EXAMPLE
     *            the EXAMPLE value to remove
     */
    public void removeExample(TLExample example);

    /**
     * Moves this EXAMPLE up by one position in the list. If the EXAMPLE is not owned by this object
     * or it is already at the front of the list, this method has no effect.
     * 
     * @param EXAMPLE
     *            the EXAMPLE to move
     */
    public void moveUp(TLExample example);

    /**
     * Moves this EXAMPLE down by one position in the list. If the EXAMPLE is not owned by this
     * object or it is already at the end of the list, this method has no effect.
     * 
     * @param EXAMPLE
     *            the EXAMPLE to move
     */
    public void moveDown(TLExample example);

    /**
     * Sorts the list of examples using the comparator provided.
     * 
     * @param comparator
     *            the comparator to use when sorting the list
     */
    public void sortExamples(Comparator<TLExample> comparator);

}

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
 * Interface to be implemented by all model components that can own <code>TLEquivalent</code> elements.
 * 
 * @author S. Livezey
 */
public interface TLEquivalentOwner extends LibraryElement {

    /**
     * Returns the value of the 'equivalents' field.
     * 
     * @return List&lt;TLEquivalent&gt;
     */
    public List<TLEquivalent> getEquivalents();

    /**
     * Returns the equivalent with the specified context.
     * 
     * @param context the context of the equivalent to return
     * @return TLEquivalent
     */
    public TLEquivalent getEquivalent(String context);

    /**
     * Adds a <code>TLEquivalent</code> element to the current list.
     * 
     * @param equivalent the equivalent value to add
     */
    public void addEquivalent(TLEquivalent equivalent);

    /**
     * Adds a <code>TLEquivalent</code> element to the current list.
     * 
     * @param index the index at which the given equivalent should be added
     * @param equivalent the equivalent value to add
     * @throws IndexOutOfBoundsException thrown if the index is out of range (index &lt; 0 || index &gt; size())
     */
    public void addEquivalent(int index, TLEquivalent equivalent);

    /**
     * Removes the specified <code>TLEquivalent</code> from the current list.
     * 
     * @param equivalent the equivalent value to remove
     */
    public void removeEquivalent(TLEquivalent equivalent);

    /**
     * Moves this equivalent up by one position in the list. If the equivalent is not owned by this object or it is
     * already at the front of the list, this method has no effect.
     * 
     * @param equivalent the equivalent to move
     */
    public void moveUp(TLEquivalent equivalent);

    /**
     * Moves this equivalent down by one position in the list. If the equivalent is not owned by this object or it is
     * already at the end of the list, this method has no effect.
     * 
     * @param equivalent the equivalent to move
     */
    public void moveDown(TLEquivalent equivalent);

    /**
     * Sorts the list of equivalents using the comparator provided.
     * 
     * @param comparator the comparator to use when sorting the list
     */
    public void sortEquivalents(Comparator<TLEquivalent> comparator);

}

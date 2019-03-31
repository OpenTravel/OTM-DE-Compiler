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
 * Interface to be implemented by all model components that can own indicators.
 * 
 * @author S. Livezey
 */
public interface TLIndicatorOwner extends NamedEntity, TLMemberFieldOwner {

    /**
     * Returns the value of the 'indicators' field.
     * 
     * @return List&lt;TLIndicator&gt;
     */
    public List<TLIndicator> getIndicators();

    /**
     * Returns the indicator with the specified name.
     * 
     * @param indicatorName the name of the indicator to return
     * @return TLIndicator
     */
    public TLIndicator getIndicator(String indicatorName);

    /**
     * Adds a <code>TLIndicator</code> element to the current list.
     * 
     * @param indicator the indicator value to add
     */
    public void addIndicator(TLIndicator indicator);

    /**
     * Adds a <code>TLIndicator</code> element to the current list.
     * 
     * @param index the index at which the given indicator should be added
     * @param indicator the indicator value to add
     * @throws IndexOutOfBoundsException thrown if the index is out of range (index &lt; 0 || index &gt; size())
     */
    public void addIndicator(int index, TLIndicator indicator);

    /**
     * Removes the specified <code>TLIndicator</code> from the current list.
     * 
     * @param indicator the indicator value to remove
     */
    public void removeIndicator(TLIndicator indicator);

    /**
     * Moves this indicator up by one position in the list. If the indicator is not owned by this object or it is
     * already at the front of the list, this method has no effect.
     * 
     * @param indicator the indicator to move
     */
    public void moveUp(TLIndicator indicator);

    /**
     * Moves this indicator down by one position in the list. If the indicator is not owned by this object or it is
     * already at the end of the list, this method has no effect.
     * 
     * @param indicator the indicator to move
     */
    public void moveDown(TLIndicator indicator);

    /**
     * Sorts the list of elements using the comparator provided.
     * 
     * @param comparator the comparator to use when sorting the list
     */
    public void sortIndicators(Comparator<TLIndicator> comparator);

}

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
 * Interface to be implemented by all model components that can be represented by alternate local names within the same
 * namespace.
 * 
 * @author S. Livezey
 */
public interface TLAliasOwner extends NamedEntity {

    /**
     * Returns the list of aliases for this entity.
     * 
     * @return List&lt;TLAlias&gt;
     */
    public List<TLAlias> getAliases();

    /**
     * Returns the alias with the specified name or null if no such alias has been defined.
     * 
     * @param aliasName the name of the alias to return
     * @return TLAlias
     */
    public TLAlias getAlias(String aliasName);

    /**
     * Adds an alias to the current list.
     * 
     * @param alias the alias to add
     */
    public void addAlias(TLAlias alias);

    /**
     * Adds an alias to the current list.
     * 
     * @param index the index at which the given alias should be added
     * @param alias the alias to add
     * @throws IndexOutOfBoundsException thrown if the index is out of range (index &lt; 0 || index &gt; size())
     */
    public void addAlias(int index, TLAlias alias);

    /**
     * Removes the specified alias from the current list.
     * 
     * @param alias the alias to remove
     */
    public void removeAlias(TLAlias alias);

    /**
     * Moves this alias up by one position in the list. If the alias is not owned by this object or it is already at the
     * front of the list, this method has no effect.
     * 
     * @param alias the alias to move
     */
    public void moveUp(TLAlias alias);

    /**
     * Moves this alias down by one position in the list. If the alias is not owned by this object or it is already at
     * the end of the list, this method has no effect.
     * 
     * @param alias the alias to move
     */
    public void moveDown(TLAlias alias);

    /**
     * Sorts the list of aliases using the comparator provided.
     * 
     * @param comparator the comparator to use when sorting the list
     */
    public void sortAliases(Comparator<TLAlias> comparator);

    /**
     * Returns the child entity list manager that manages the list of aliases for this entity.
     * 
     * @param <O> the type of the alias owner
     * @return ChildEntityListManager&lt;TLAlias,?&gt;
     */
    public <O> ChildEntityListManager<TLAlias,O> getAliasListManager();

}

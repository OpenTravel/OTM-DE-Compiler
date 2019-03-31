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

package org.opentravel.schemacompiler.diff;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all OTM-Diff change sets.
 */
public abstract class ChangeSet<E, C extends ChangeItem<?>> {

    private E oldVersion;
    private E newVersion;
    private List<C> changeItems = new ArrayList<>();

    /**
     * Constructor that assigns the old and new version of an entity that was modified.
     * 
     * @param oldVersion the old version of the object
     * @param newVersion the new version of the object
     */
    public ChangeSet(E oldVersion, E newVersion) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    /**
     * Returns the old version of the object.
     *
     * @return E
     */
    public E getOldVersion() {
        return oldVersion;
    }

    /**
     * Returns the new version of the object.
     *
     * @return E
     */
    public E getNewVersion() {
        return newVersion;
    }

    /**
     * Returns the list of changes between the old and new version.
     *
     * @return List&lt;C&gt;
     */
    public List<C> getChangeItems() {
        return changeItems;
    }

    /**
     * Returns a unique ID for this change set that can be used as a bookmark anchor in the formatted report output.
     * 
     * @return String
     */
    public abstract String getBookmarkId();

    /**
     * Returns a bookmark ID for the given library.
     * 
     * @param library the library for which to return a bookmark ID
     * @return String
     */
    protected String getBookmarkId(AbstractLibrary library) {
        String libraryName = (library == null) ? "UNKNOWN_LIBRARY" : library.getName().replaceAll( "\\s", "_" );
        String prefix = (library == null) ? "ns" : library.getPrefix();

        return prefix + "$" + libraryName;
    }

    /**
     * Returns a bookmark ID for the given entity.
     * 
     * @param entity the named entity for which to return a bookmark ID
     * @return String
     */
    protected String getBookmarkId(NamedEntity entity) {
        AbstractLibrary owningLibrary = (entity == null) ? null : entity.getOwningLibrary();
        String localName = (entity == null) ? null : entity.getLocalName();

        return getBookmarkId( owningLibrary ) + "$" + ((localName == null) ? "UNKNOWN_ENTITY" : localName);
    }

}

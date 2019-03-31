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

import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Describes a single change identified during comparison of two OTM projects.
 */
public class ProjectChangeItem extends ChangeItem<ProjectChangeType> {

    private ProjectChangeSet changeSet;
    private TLLibrary addedLibrary;
    private TLLibrary deletedLibrary;
    private LibraryChangeSet modifiedLibrary;

    /**
     * Constructor used when a library was added or deleted from its owning project.
     * 
     * @param changeSet the change set to which this item belongs
     * @param changeType the type of project change
     * @param affectedLibrary the library that was added or removed
     */
    public ProjectChangeItem(ProjectChangeSet changeSet, ProjectChangeType changeType, TLLibrary affectedLibrary) {
        this.changeSet = changeSet;
        this.changeType = changeType;

        switch (changeType) {
            case LIBRARY_ADDED:
                this.addedLibrary = affectedLibrary;
                break;
            case LIBRARY_DELETED:
                this.deletedLibrary = affectedLibrary;
                break;
            default:
                throw new IllegalArgumentException(
                    "Illegal change type for library addition or deletion: " + changeType );
        }
    }

    /**
     * Constructor used when a project library was modified.
     * 
     * @param changeSet the change set to which this item belongs
     * @param changeType the type of project change
     * @param modifiedLibrary the change set for a modified library
     */
    public ProjectChangeItem(ProjectChangeSet changeSet, ProjectChangeType changeType,
        LibraryChangeSet modifiedLibrary) {
        this.changeSet = changeSet;

        switch (changeType) {
            case LIBRARY_CHANGED:
            case LIBRARY_VERSION_CHANGED:
                this.changeType = changeType;
                break;
            default:
                throw new IllegalArgumentException( "Illegal change type for library modification: " + changeType );
        }
        this.modifiedLibrary = modifiedLibrary;
    }

    /**
     * Constructor used when a project value was changed.
     * 
     * @param changeSet the change set to which this item belongs
     * @param changeType the type of project change
     * @param oldValue the affected value from the old version
     * @param newValue the affected value from the new version
     */
    public ProjectChangeItem(ProjectChangeSet changeSet, ProjectChangeType changeType, String oldValue,
        String newValue) {
        this.changeSet = changeSet;
        this.changeType = changeType;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Returns the change set to which this item belongs.
     *
     * @return ProjectChangeSet
     */
    public ProjectChangeSet getChangeSet() {
        return changeSet;
    }

    /**
     * Returns the library that was added.
     *
     * @return TLLibrary
     */
    public TLLibrary getAddedLibrary() {
        return addedLibrary;
    }

    /**
     * Returns the library that was removed.
     *
     * @return TLLibrary
     */
    public TLLibrary getDeletedLibrary() {
        return deletedLibrary;
    }

    /**
     * Returns the change set for a modified library.
     *
     * @return LibraryChangeSet
     */
    public LibraryChangeSet getModifiedLibrary() {
        return modifiedLibrary;
    }

}

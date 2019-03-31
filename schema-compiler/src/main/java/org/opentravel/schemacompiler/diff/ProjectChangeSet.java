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

import org.opentravel.schemacompiler.repository.Project;

/**
 * Container for all change items identified during the comparison of two projects, as well as the library change sets
 * for the libraries that existed in both versions of the project.
 */
public class ProjectChangeSet extends ChangeSet<Project,ProjectChangeItem> {

    /**
     * Constructor that assigns the old and new version of a project that was modified.
     * 
     * @param oldProject the old version of the project
     * @param newProject the new version of the project
     */
    public ProjectChangeSet(Project oldProject, Project newProject) {
        super( oldProject, newProject );
    }

    /**
     * @see org.opentravel.schemacompiler.diff.ChangeSet#getBookmarkId()
     */
    public String getBookmarkId() {
        Project project = (getNewVersion() != null) ? getNewVersion() : getOldVersion();
        return (project == null) ? "UNKNOWN_PROJECT" : ("prj$" + project.getName());
    }

}

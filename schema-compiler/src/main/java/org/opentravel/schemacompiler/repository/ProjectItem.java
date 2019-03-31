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

package org.opentravel.schemacompiler.repository;

import org.opentravel.schemacompiler.model.AbstractLibrary;

import java.util.List;

/**
 * Represents a <code>RepositoryItem</code> component that is accessible from a local model project.
 * 
 * @author S. Livezey
 */
public interface ProjectItem extends RepositoryItem {

    /**
     * Returns the <code>ProjectManager</code> that owns this item.
     * 
     * @return ProjectManager
     */
    public ProjectManager getProjectManager();

    /**
     * Returns the list of projects of which this <code>ProjectItem</code> is a member.
     * 
     * @return List&lt;Project&gt;
     */
    public List<Project> memberOfProjects();

    /**
     * Returns the library content of this repository item.
     * 
     * @return AbstractLibrary
     */
    public AbstractLibrary getContent();

    /**
     * Returns true if the project item's content is to be considered read-only by an editor application.
     * 
     * @return boolean
     */
    public boolean isReadOnly();

}

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

/**
 * Defines the interface for components that need to be notified when a <code>Project</code> or one of its
 * <code>ProjectItem</code> members is modified.
 * 
 * @author S. Livezey
 */
public interface ProjectChangeListener {

    /**
     * Called when the identity and/or descriptive information about a project has been modified.
     * 
     * @param project the project that was modified
     */
    public void projectInformationModified(Project project);

    /**
     * Called when a <code>ProjectItem</code> member is added to the specified project.
     * 
     * @param project the project to which the new member was added
     * @param item the project item that was added
     */
    public void projectItemAdded(Project project, ProjectItem item);

    /**
     * Called when a <code>ProjectItem</code> member is removed from the specified project.
     * 
     * @param project the project from which the new member was removed
     * @param item the project item that was removed
     */
    public void projectItemRemoved(Project project, ProjectItem item);

}

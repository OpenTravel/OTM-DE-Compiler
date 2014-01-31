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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLModel;

/**
 * Container that maintains a set of local <code>RepositoryItem</code> artifacts.
 * <code>RepositoryItems</code> managed by a model project represent unmanaged files that have not
 * yet been shared via a remote repository, work-in-process (WIP) items that have been locked for
 * editing by the local user, or unlocked items from remote repositories that have been included for
 * reference only.
 * 
 * @author S. Livezey
 */
public class Project {

    private ProjectManager projectManager;
    private String projectId;
    private File projectFile;
    private String name;
    private String description;
    private ProjectItem defaultItem;
    private String defaultContextId;
    private List<ProjectItem> projectItems = new ArrayList<ProjectItem>();
    private Collection<ProjectChangeListener> listeners = new ArrayList<ProjectChangeListener>();

    /**
     * Constructor that assigns the <code>ProjectManger</code> that will own the new project.
     * 
     * @param model
     *            the shared model instance
     */
    public Project(ProjectManager projectManager) {
        if (projectManager == null) {
            throw new NullPointerException("The project manager instance cannot be null.");
        }
        this.projectManager = projectManager;
    }

    /**
     * Returns the <code>ProjectManager</code> that owns this project instance.
     * 
     * @return ProjectManager
     */
    public ProjectManager getProjectManager() {
        return projectManager;
    }

    /**
     * Returns the <code>TLModel</code> instance that is managed by this workspace.
     * 
     * @return TLModel
     */
    public TLModel getModel() {
        return projectManager.getModel();
    }

    /**
     * Returns the ID of the project.
     * 
     * @return String
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * Assigns the ID of the project.
     * 
     * @param projectId
     *            the project ID to assign
     * @throws IllegalArgumentException
     *             thrown if the specified project ID is already in use
     */
    public void setProjectId(String projectId) {
        boolean isEqual = (this.projectId == null) ? (projectId == null) : this.projectId
                .equals(projectId);

        if (!isEqual) {
            projectManager.validateProjectID(projectId, this);
            this.projectId = projectId;
            fireProjectInformationModifiedEvent();
        }
    }

    /**
     * Returns the file location where this project's configuration is stored.
     * 
     * @return File
     */
    public File getProjectFile() {
        return projectFile;
    }

    /**
     * Assigns the file location where this project's configuration is stored.
     * 
     * @param projectFile
     *            the location of the project's data file
     * @throws IllegalArgumentException
     *             thrown if the specified project file is already in use
     */
    public void setProjectFile(File projectFile) {
        boolean isEqual = (this.projectFile == null) ? (projectFile == null) : this.projectFile
                .equals(projectFile);

        if (!isEqual) {
            projectManager.validateProjectFile(projectFile, this);
            this.projectFile = projectFile;
            fireProjectInformationModifiedEvent();
        }
    }

    /**
     * Returns the user-displayable name of the project.
     * 
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Assigns the user-displayable name of the project.
     * 
     * @param name
     *            the project name to assign
     */
    public void setName(String name) {
        boolean isEqual = (this.name == null) ? (name == null) : this.name.equals(name);

        if (!isEqual) {
            this.name = name;
            fireProjectInformationModifiedEvent();
        }
    }

    /**
     * Returns the project description.
     * 
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Assigns the project description.
     * 
     * @param description
     *            the project description to assign
     */
    public void setDescription(String description) {
        boolean isEqual = (this.description == null) ? (description == null) : this.description
                .equals(description);

        if (!isEqual) {
            this.description = description;
            fireProjectInformationModifiedEvent();
        }
    }

    /**
     * Returns the default context ID to use for example generation in this project.
     * 
     * @return String
     */
    public String getDefaultContextId() {
        return defaultContextId;
    }

    /**
     * Assigns the default context ID to use for example generation in this project.
     * 
     * @param defaultContextId
     *            the context ID value to assign
     */
    public void setDefaultContextId(String defaultContextId) {
        boolean isEqual = (this.defaultContextId == null) ? (defaultContextId == null)
                : this.defaultContextId.equals(defaultContextId);

        if (!isEqual) {
            this.defaultContextId = defaultContextId;
            fireProjectInformationModifiedEvent();
        }
    }

    /**
     * Returns the default item/member of this project.
     * 
     * @return ProjectItem
     */
    public ProjectItem getDefaultItem() {
        ProjectItem result = defaultItem;

        if ((result == null) && (projectItems.size() > 0)) {
            result = projectItems.get(0);
        }
        return result;
    }

    /**
     * Assigns the given project item as the default one for the project.
     * 
     * @param item
     *            the project item to identify as the default
     */
    public void setDefaultItem(ProjectItem defaultItem) {
        if (!projectItems.contains(defaultItem)) {
            throw new IllegalArgumentException(
                    "The new default project item is not a member of this project.");
        }
        if (this.defaultItem != defaultItem) {
            this.defaultItem = defaultItem;
            fireProjectInformationModifiedEvent();
        }
    }

    /**
     * Returns the list of repository items that are available in this model project.
     * 
     * @return List<ProjectItem>
     */
    public List<ProjectItem> getProjectItems() {
        return new ArrayList<ProjectItem>(projectItems);
    }

    /**
     * Adds the given project-item to this project.
     * 
     * @param item
     *            the project item to remove
     */
    protected void add(ProjectItem item) {
        if (!projectItems.contains(item)) {
            projectItems.add(item);
            fireProjectItemAdded(item);
        }
    }

    /**
     * Removes the given project-item from this project.
     * 
     * @param item
     *            the project item to remove
     */
    public void remove(ProjectItem item) {
        if ((item != null) && projectItems.contains(item)) {
            if (defaultItem == item) {
                defaultItem = null;
            }
            projectItems.remove(item);
            fireProjectItemRemoved(item);
            projectManager.purgeOrphanedProjectItems();
        }
    }

    /**
     * Removes the project-item associated with the given library from this project.
     * 
     * @param library
     *            the library whose project item is to be removed
     */
    public void remove(AbstractLibrary library) {
        ProjectItem item = projectManager.getProjectItem(library);

        if (item != null) {
            remove(item);
        }
    }

    /**
     * Removes the project-item associated with the given library URL from this project.
     * 
     * @param libraryUrl
     *            the file URL of the library whose project item is to be removed
     */
    public void remove(URL libraryUrl) {
        ProjectItem item = projectManager.getProjectItem(libraryUrl);

        if (item != null) {
            remove(item);
        }
    }

    /**
     * Returns true if the given <code>ProjectItem</code> is a member of this project.
     * 
     * @param item
     *            the project item to check
     * @return boolean
     */
    public boolean isMemberOf(ProjectItem item) {
        return projectItems.contains(item);
    }

    /**
     * Returns the value of the 'listeners' field.
     * 
     * @return Collection<ProjectChangeListener>
     */
    public Collection<ProjectChangeListener> getProjectChangeListeners() {
        return Collections.unmodifiableCollection(listeners);
    }

    /**
     * Adds the given listener to the list maintained by this project.
     * 
     * @param listener
     *            the listener instance to add
     */
    public void addProjectChangeListener(ProjectChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes the given listener from the list maintained by this project.
     * 
     * @param listener
     *            the listener instance to remove
     */
    public void removeProjectChangeListener(ProjectChangeListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    /**
     * Broadcasts an event to all listeners that the project's descriptive information has been
     * modified.
     */
    private void fireProjectInformationModifiedEvent() {
        for (ProjectChangeListener listener : listeners) {
            listener.projectInformationModified(this);
        }
    }

    /**
     * Broadcasts an event to all listeners that a new <code>ProjectItem</code> has been added to
     * this project.
     */
    private void fireProjectItemAdded(ProjectItem item) {
        for (ProjectChangeListener listener : listeners) {
            listener.projectItemAdded(this, item);
        }
    }

    /**
     * Broadcasts an event to all listeners that a <code>ProjectItem</code> has been removed from
     * this project.
     */
    private void fireProjectItemRemoved(ProjectItem item) {
        for (ProjectChangeListener listener : listeners) {
            listener.projectItemRemoved(this, item);
        }
    }

}

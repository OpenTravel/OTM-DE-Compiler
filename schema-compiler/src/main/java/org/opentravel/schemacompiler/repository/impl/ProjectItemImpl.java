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

package org.opentravel.schemacompiler.repository.impl;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the <code>ProjectItem</code> interface.
 * 
 * @author S. Livezey
 */
public class ProjectItemImpl extends RepositoryItemImpl implements ProjectItem {

    private ProjectManager projectManager;
    private AbstractLibrary libraryContent;

    /**
     * Constructor that supplies the content of the library for the project item.
     * 
     * @param libraryContent the content of the library
     * @param projectManager the project manager that owns the the item
     */
    protected ProjectItemImpl(AbstractLibrary libraryContent, ProjectManager projectManager) {
        this.libraryContent = libraryContent;
        this.projectManager = projectManager;
    }

    /**
     * Returns a new unmanaged project item instance.
     * 
     * @param libraryContent the content of the library
     * @param projectManager the project manager that owns the the item
     * @return ProjectItem
     */
    public static ProjectItem newUnmanagedItem(AbstractLibrary libraryContent, ProjectManager projectManager) {
        ProjectItemImpl item = new ProjectItemImpl( libraryContent, projectManager );

        item.setState( RepositoryItemState.UNMANAGED );
        return item;
    }

    /**
     * Returns a new managed project item instance.
     * 
     * @param repositoryItem the repository item for which to create a managed project item
     * @param libraryContent the content of the library
     * @param projectManager the project manager that owns the the item
     * @return ProjectItem
     */
    public static ProjectItem newManagedItem(RepositoryItem repositoryItem, AbstractLibrary libraryContent,
        ProjectManager projectManager) {
        ProjectItemImpl item = new ProjectItemImpl( libraryContent, projectManager );

        item.setRepository( repositoryItem.getRepository() );
        item.setState( repositoryItem.getState() );
        item.setLockedByUser( repositoryItem.getLockedByUser() );
        return item;
    }

    /**
     * Changes the state of the given project item to <code>MANAGED_UNLOCKED</code>.
     * 
     * @param item the project item whose state is to be modified
     * @param newContent the new library content for the project item
     */
    public static void changeStateToManagedUnlocked(ProjectItem item, AbstractLibrary newContent) {
        switch (item.getState()) {
            case MANAGED_UNLOCKED:
            case BUILT_IN:
                throw new IllegalStateException( String.format(
                    "Cannnot change a project item's state from '%s' to MANAGED_UNLOCKED.", item.getState() ) );
            default:
                break;
        }
        ProjectItemImpl pItem = (ProjectItemImpl) item;

        pItem.setContent( newContent );
        pItem.setState( RepositoryItemState.MANAGED_UNLOCKED );
    }

    /**
     * Changes the state of the given project item to <code>MANAGED_LOCKED</code>.
     * 
     * @param item the project item whose state is to be modified
     */
    public static void changeStateToManagedLocked(ProjectItem item) {
        if (item.getState() != RepositoryItemState.MANAGED_UNLOCKED) {
            throw new IllegalStateException( String
                .format( "Cannnot change a project item's state from '%s' to MANAGED_LOCKED.", item.getState() ) );
        }
        ((ProjectItemImpl) item).setState( RepositoryItemState.MANAGED_LOCKED );
    }

    /**
     * Changes the state of the given project item to <code>MANAGED_WIP</code>.
     * 
     * @param item the project item whose state is to be modified
     * @param newContent the new library content for the project item
     */
    public static void changeStateToWorkInProcess(ProjectItem item, AbstractLibrary newContent) {
        if (item.getState() != RepositoryItemState.MANAGED_UNLOCKED) {
            throw new IllegalStateException(
                String.format( "Cannnot change a project item's state from '%s' to MANAGED_WIP.", item.getState() ) );
        }
        ProjectItemImpl pItem = (ProjectItemImpl) item;

        pItem.setContent( newContent );
        pItem.setState( RepositoryItemState.MANAGED_WIP );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.ProjectItem#getProjectManager()
     */
    @Override
    public ProjectManager getProjectManager() {
        return projectManager;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.ProjectItem#memberOfProjects()
     */
    @Override
    public List<Project> memberOfProjects() {
        List<Project> projectList = new ArrayList<>();

        for (Project project : projectManager.getAllProjects()) {
            if (project.isMemberOf( this )) {
                projectList.add( project );
            }
        }
        return projectList;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getRepository()
     */
    @Override
    public Repository getRepository() {
        return (getState() == RepositoryItemState.UNMANAGED) ? null : super.getRepository();
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getNamespace()
     */
    @Override
    public String getNamespace() {
        return (libraryContent == null) ? null : libraryContent.getNamespace();
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setNamespace(java.lang.String)
     */
    @Override
    public void setNamespace(String namespace) {
        throw new UnsupportedOperationException( "Method 'setNamespace()' not supported for project items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getBaseNamespace()
     */
    @Override
    public String getBaseNamespace() {
        String baseNS = null;

        if (libraryContent instanceof TLLibrary) {
            baseNS = ((TLLibrary) libraryContent).getBaseNamespace();

        } else {
            baseNS = libraryContent.getNamespace();
        }
        return baseNS;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setBaseNamespace(java.lang.String)
     */
    @Override
    public void setBaseNamespace(String baseNamespace) {
        throw new UnsupportedOperationException( "Method 'setBaseNamespace()' not supported for project items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getFilename()
     */
    @Override
    public String getFilename() {
        URL libraryUrl = (libraryContent == null) ? null : libraryContent.getLibraryUrl();

        return URLUtils.getUrlFilename( libraryUrl );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setFilename(java.lang.String)
     */
    @Override
    public void setFilename(String filename) {
        throw new UnsupportedOperationException( "Method 'setFilename()' not supported for project items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getLibraryName()
     */
    @Override
    public String getLibraryName() {
        return (libraryContent == null) ? null : libraryContent.getName();
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setLibraryName(java.lang.String)
     */
    @Override
    public void setLibraryName(String libraryName) {
        throw new UnsupportedOperationException( "Method 'setLibraryName()' not supported for project items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getVersion()
     */
    @Override
    public String getVersion() {
        String version;

        if (libraryContent instanceof TLLibrary) {
            version = ((TLLibrary) libraryContent).getVersion();

        } else {
            try {
                VersionSchemeFactory factory = VersionSchemeFactory.getInstance();
                version = factory.getVersionScheme( factory.getDefaultVersionScheme() ).getDefaultVersionIdentifier();

            } catch (VersionSchemeException e) {
                // Should never happen, but just in case...
                version = "1.0.0";
            }
        }
        return version;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setVersion(java.lang.String)
     */
    @Override
    public void setVersion(String version) {
        throw new UnsupportedOperationException( "Method 'setVersion()' not supported for project items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getVersionScheme()
     */
    @Override
    public String getVersionScheme() {
        String versionScheme;

        if (libraryContent instanceof TLLibrary) {
            versionScheme = ((TLLibrary) libraryContent).getVersionScheme();

        } else {
            versionScheme = VersionSchemeFactory.getInstance().getDefaultVersionScheme();
        }
        return versionScheme;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setVersionScheme(java.lang.String)
     */
    @Override
    public void setVersionScheme(String versionScheme) {
        throw new UnsupportedOperationException( "Method 'setVersionScheme()' not supported for project items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getStatus()
     */
    @Override
    public TLLibraryStatus getStatus() {
        return (libraryContent instanceof TLLibrary) ? ((TLLibrary) libraryContent).getStatus() : TLLibraryStatus.FINAL;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setStatus(org.opentravel.schemacompiler.model.TLLibraryStatus)
     */
    @Override
    public void setStatus(TLLibraryStatus status) {
        throw new UnsupportedOperationException( "Method 'setStatus()' not supported for project items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.ProjectItem#getContent()
     */
    @Override
    public AbstractLibrary getContent() {
        return libraryContent;
    }

    /**
     * Assigns the given library as the content for this project item. This effectively replaces the original content of
     * the item.
     * 
     * @param libraryContent the library that will replace the existing item content
     */
    public void setContent(AbstractLibrary libraryContent) {
        this.libraryContent = libraryContent;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.ProjectItem#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        boolean result;

        switch (getState()) {
            case MANAGED_LOCKED:
            case MANAGED_UNLOCKED:
            case BUILT_IN:
                result = true;
                break;
            case MANAGED_WIP:
                result = false;
                break;
            default:
                result = (libraryContent == null) || libraryContent.isReadOnly();
        }
        return result;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#toURI(boolean)
     */
    @Override
    public URI toURI(boolean fullyQualified) {
        URI uri;

        if (getState() == RepositoryItemState.UNMANAGED) {
            uri = URLUtils.toURI( libraryContent.getLibraryUrl() );

        } else {
            uri = super.toURI( fullyQualified );
        }
        return uri;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (libraryContent == null) ? 0 : libraryContent.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return ((obj instanceof ProjectItemImpl) && (this.libraryContent == ((ProjectItemImpl) obj).libraryContent));
    }

}

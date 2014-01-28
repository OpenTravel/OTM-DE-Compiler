package org.opentravel.schemacompiler.repository.impl;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

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
     * @param libraryContent
     *            the content of the library
     * @param projectManager
     *            the project manager that owns the the item
     */
    protected ProjectItemImpl(AbstractLibrary libraryContent, ProjectManager projectManager) {
        this.libraryContent = libraryContent;
        this.projectManager = projectManager;
    }

    /**
     * Returns a new unmanaged project item instance.
     * 
     * @param libraryFile
     *            the local file associated with the library content provided
     * @param libraryContent
     *            the content of the library
     * @param projectManager
     *            the project manager that owns the the item
     * @return ProjectItem
     */
    public static ProjectItem newUnmanagedItem(File libraryFile, AbstractLibrary libraryContent,
            ProjectManager projectManager) {
        ProjectItemImpl item = new ProjectItemImpl(libraryContent, projectManager);

        item.setState(RepositoryItemState.UNMANAGED);
        return item;
    }

    /**
     * Returns a new managed project item instance.
     * 
     * @param libraryFile
     *            the local file associated with the library content provided
     * @param libraryContent
     *            the content of the library
     * @param projectManager
     *            the project manager that owns the the item
     * @param repositoryItem
     *            the repository item for which to create a managed project item
     * @return ProjectItem
     */
    public static ProjectItem newManagedItem(RepositoryItem repositoryItem,
            AbstractLibrary libraryContent, ProjectManager projectManager) {
        ProjectItemImpl item = new ProjectItemImpl(libraryContent, projectManager);

        item.setRepository(repositoryItem.getRepository());
        item.setState(repositoryItem.getState());
        item.setLockedByUser(repositoryItem.getLockedByUser());
        return item;
    }

    /**
     * Changes the state of the given project item to <code>MANAGED_UNLOCKED</code>.
     * 
     * @param item
     *            the project item whose state is to be modified
     * @param newContent
     *            the new library content for the project item
     */
    public static void changeStateToManagedUnlocked(ProjectItem item, AbstractLibrary newContent) {
        switch (item.getState()) {
            case MANAGED_UNLOCKED:
            case BUILT_IN:
                throw new IllegalStateException("Cannnot change a project item's state from '"
                        + item.getState() + "' to MANAGED_UNLOCKED.");
        }
        ProjectItemImpl _item = (ProjectItemImpl) item;

        _item.setContent(newContent);
        _item.setState(RepositoryItemState.MANAGED_UNLOCKED);
    }

    /**
     * Changes the state of the given project item to <code>MANAGED_LOCKED</code>.
     * 
     * @param item
     *            the project item whose state is to be modified
     */
    public static void changeStateToManagedLocked(ProjectItem item) {
        if (item.getState() != RepositoryItemState.MANAGED_UNLOCKED) {
            throw new IllegalStateException("Cannnot change a project item's state from '"
                    + item.getState() + "' to MANAGED_UNLOCKED.");
        }
        ((ProjectItemImpl) item).setState(RepositoryItemState.MANAGED_LOCKED);
    }

    /**
     * Changes the state of the given project item to <code>MANAGED_WIP</code>.
     * 
     * @param item
     *            the project item whose state is to be modified
     * @param newContent
     *            the new library content for the project item
     */
    public static void changeStateToManagedWIP(ProjectItem item, AbstractLibrary newContent) {
        if (item.getState() != RepositoryItemState.MANAGED_UNLOCKED) {
            throw new IllegalStateException("Cannnot change a project item's state from '"
                    + item.getState() + "' to MANAGED_UNLOCKED.");
        }
        ProjectItemImpl _item = (ProjectItemImpl) item;

        _item.setContent(newContent);
        _item.setState(RepositoryItemState.MANAGED_WIP);
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
        List<Project> projectList = new ArrayList<Project>();

        for (Project project : projectManager.getAllProjects()) {
            if (project.isMemberOf(this)) {
                projectList.add(project);
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
        throw new UnsupportedOperationException(
                "Method 'setNamespace()' not supported for project items.");
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
        throw new UnsupportedOperationException(
                "Method 'setBaseNamespace()' not supported for project items.");
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getFilename()
     */
    @Override
    public String getFilename() {
        URL libraryUrl = (libraryContent == null) ? null : libraryContent.getLibraryUrl();
        String urlString = (libraryUrl == null) ? null : libraryUrl.toExternalForm();
        String filename;

        if (urlString.endsWith("/")) {
            filename = null;

        } else {
            int idx = urlString.lastIndexOf('/');
            filename = (idx < 0) ? urlString : urlString.substring(idx + 1);
        }
        return filename;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setFilename(java.lang.String)
     */
    @Override
    public void setFilename(String filename) {
        throw new UnsupportedOperationException(
                "Method 'setFilename()' not supported for project items.");
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
        throw new UnsupportedOperationException(
                "Method 'setLibraryName()' not supported for project items.");
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
                version = factory.getVersionScheme(factory.getDefaultVersionScheme())
                        .getDefaultVersionIdentifer();

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
        throw new UnsupportedOperationException(
                "Method 'setVersion()' not supported for project items.");
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
        throw new UnsupportedOperationException(
                "Method 'setVersionScheme()' not supported for project items.");
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getStatus()
     */
    @Override
    public TLLibraryStatus getStatus() {
        return (libraryContent instanceof TLLibrary) ? ((TLLibrary) libraryContent).getStatus()
                : TLLibraryStatus.FINAL;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setStatus(org.opentravel.schemacompiler.model.TLLibraryStatus)
     */
    @Override
    public void setStatus(TLLibraryStatus status) {
        throw new UnsupportedOperationException(
                "Method 'setStatus()' not supported for project items.");
    }

    /**
     * @see org.opentravel.schemacompiler.repository.ProjectItem#getContent()
     */
    @Override
    public AbstractLibrary getContent() {
        return libraryContent;
    }

    /**
     * Assigns the given library as the content for this project item. This effectively replaces the
     * original content of the item.
     * 
     * @param libraryContent
     *            the library that will replace the existing item content
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
            try {
                uri = libraryContent.getLibraryUrl().toURI();

            } catch (URISyntaxException e) {
                // Should never happen; throw a runtime exception just in case
                throw new RuntimeException(e);
            }
        } else {
            uri = super.toURI(fullyQualified);
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

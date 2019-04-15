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

import org.opentravel.ns.ota2.release_v01_00.ReleaseStatus;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Release;
import org.opentravel.schemacompiler.repository.ReleaseItem;
import org.opentravel.schemacompiler.repository.ReleaseManager;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

import java.net.URI;
import java.net.URL;

/**
 * Implementation of the <code>ReleaseItem</code> interface.
 */
public class ReleaseItemImpl extends RepositoryItemImpl implements ReleaseItem {

    private ReleaseManager releaseManager;

    /**
     * Constructor that supplies the content and owning manager for the release item.
     * 
     * @param releaseManager the release manager that owns this release item
     */
    ReleaseItemImpl(ReleaseManager releaseManager) {
        this.releaseManager = releaseManager;
    }

    /**
     * Creates a new release item for the current release of the given manager.
     * 
     * @param releaseManager the release manager that owns the new item
     * @return ReleaseItem
     */
    public static ReleaseItem newUnmanagedItem(ReleaseManager releaseManager) {
        ReleaseItemImpl item = new ReleaseItemImpl( releaseManager );

        item.setState( RepositoryItemState.UNMANAGED );
        return item;
    }

    /**
     * Creates a new release item for the current release of the given manager.
     * 
     * @param repositoryItem the repository item for which to create a managed release item
     * @param releaseManager the release manager that owns the new item
     * @return ReleaseItem
     */
    public static ReleaseItem newManagedItem(RepositoryItem repositoryItem, ReleaseManager releaseManager) {
        ReleaseItemImpl item = new ReleaseItemImpl( releaseManager );

        item.setRepository( repositoryItem.getRepository() );
        item.setState( repositoryItem.getState() );
        item.setLockedByUser( repositoryItem.getLockedByUser() );
        return item;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.ReleaseItem#getReleaseManager()
     */
    @Override
    public ReleaseManager getReleaseManager() {
        return releaseManager;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.ReleaseItem#getContent()
     */
    @Override
    public Release getContent() {
        return releaseManager.getRelease();
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
        return releaseManager.getRelease().getNamespace();
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setNamespace(java.lang.String)
     */
    @Override
    public void setNamespace(String namespace) {
        throw new UnsupportedOperationException( "Method 'setNamespace()' not supported for release items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getBaseNamespace()
     */
    @Override
    public String getBaseNamespace() {
        return releaseManager.getRelease().getBaseNamespace();
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setBaseNamespace(java.lang.String)
     */
    @Override
    public void setBaseNamespace(String baseNamespace) {
        throw new UnsupportedOperationException( "Method 'setBaseNamespace()' not supported for release items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getFilename()
     */
    @Override
    public String getFilename() {
        Release release = getContent();
        URL releaseUrl = (release == null) ? null : release.getReleaseUrl();

        return URLUtils.getUrlFilename( releaseUrl );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setFilename(java.lang.String)
     */
    @Override
    public void setFilename(String filename) {
        throw new UnsupportedOperationException( "Method 'setFilename()' not supported for release items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getLibraryName()
     */
    @Override
    public String getLibraryName() {
        return releaseManager.getRelease().getName();
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setLibraryName(java.lang.String)
     */
    @Override
    public void setLibraryName(String libraryName) {
        throw new UnsupportedOperationException( "Method 'setLibraryName()' not supported for release items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getVersion()
     */
    @Override
    public String getVersion() {
        return releaseManager.getRelease().getVersion();
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setVersion(java.lang.String)
     */
    @Override
    public void setVersion(String version) {
        throw new UnsupportedOperationException( "Method 'setVersion()' not supported for release items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getVersionScheme()
     */
    @Override
    public String getVersionScheme() {
        return VersionSchemeFactory.getInstance().getDefaultVersionScheme();
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setVersionScheme(java.lang.String)
     */
    @Override
    public void setVersionScheme(String versionScheme) {
        throw new UnsupportedOperationException( "Method 'setVersionScheme()' not supported for release items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getStatus()
     */
    @Override
    public TLLibraryStatus getStatus() {
        return (releaseManager.getRelease().getStatus() == ReleaseStatus.DRAFT) ? TLLibraryStatus.DRAFT
            : TLLibraryStatus.FINAL;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setStatus(org.opentravel.schemacompiler.model.TLLibraryStatus)
     */
    @Override
    public void setStatus(TLLibraryStatus status) {
        throw new UnsupportedOperationException( "Method 'setStatus()' not supported for release items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#toURI(boolean)
     */
    @Override
    public URI toURI(boolean fullyQualified) {
        URI uri;

        if (getState() == RepositoryItemState.UNMANAGED) {
            uri = URLUtils.toURI( releaseManager.getRelease().getReleaseUrl() );

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
        return (releaseManager.getRelease() == null) ? 0 : releaseManager.getRelease().hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof ReleaseItemImpl) {
            Release otherRelease = ((ReleaseItemImpl) obj).getReleaseManager().getRelease();
            Release thisRelease = releaseManager.getRelease();

            result = (thisRelease == otherRelease);
        }
        return result;
    }

}

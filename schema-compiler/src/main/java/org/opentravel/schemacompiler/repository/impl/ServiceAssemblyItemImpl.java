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

import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.ServiceAssembly;
import org.opentravel.schemacompiler.repository.ServiceAssemblyItem;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

import java.net.URI;
import java.net.URL;

/**
 * Implementation of the <code>ServiceAssemblyItem</code> interface.
 */
public class ServiceAssemblyItemImpl extends RepositoryItemImpl implements ServiceAssemblyItem {

    private ServiceAssembly assembly;

    /**
     * Constructor that initializes the content from the given assembly and the repository information from the
     * repository item provided.
     * 
     * @param assembly the assembly content for the new item
     * @param repositoryItem the repository item associated with the assembly
     */
    public ServiceAssemblyItemImpl(ServiceAssembly assembly, RepositoryItem repositoryItem) {
        this.assembly = assembly;
        this.setRepository( repositoryItem.getRepository() );
        this.setStatus( repositoryItem.getStatus() );
        this.setState( repositoryItem.getState() );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getNamespace()
     */
    @Override
    public String getNamespace() {
        return assembly.getNamespace();
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setNamespace(java.lang.String)
     */
    @Override
    public void setNamespace(String namespace) {
        throw new UnsupportedOperationException( "Method 'setNamespace()' not supported for service assembly items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getBaseNamespace()
     */
    @Override
    public String getBaseNamespace() {
        return assembly.getBaseNamespace();
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setBaseNamespace(java.lang.String)
     */
    @Override
    public void setBaseNamespace(String baseNamespace) {
        throw new UnsupportedOperationException(
            "Method 'setBaseNamespace()' not supported for service assembly items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getFilename()
     */
    @Override
    public String getFilename() {
        URL assemblyUrl = (assembly == null) ? null : assembly.getAssemblyUrl();

        return URLUtils.getUrlFilename( assemblyUrl );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setFilename(java.lang.String)
     */
    @Override
    public void setFilename(String filename) {
        throw new UnsupportedOperationException( "Method 'setFilename()' not supported for service assembly items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getLibraryName()
     */
    @Override
    public String getLibraryName() {
        return assembly.getName();
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setLibraryName(java.lang.String)
     */
    @Override
    public void setLibraryName(String libraryName) {
        throw new UnsupportedOperationException(
            "Method 'setLibraryName()' not supported for service assembly items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#getVersion()
     */
    @Override
    public String getVersion() {
        return assembly.getVersion();
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#setVersion(java.lang.String)
     */
    @Override
    public void setVersion(String version) {
        throw new UnsupportedOperationException( "Method 'setVersion()' not supported for service assembly items." );
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
        throw new UnsupportedOperationException(
            "Method 'setVersionScheme()' not supported for service assembly items." );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl#toURI(boolean)
     */
    @Override
    public URI toURI(boolean fullyQualified) {
        URI uri;

        if (getState() == RepositoryItemState.UNMANAGED) {
            uri = URLUtils.toURI( assembly.getAssemblyUrl() );

        } else {
            uri = super.toURI( fullyQualified );
        }
        return uri;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.ServiceAssemblyItem#getContent()
     */
    @Override
    public ServiceAssembly getContent() {
        return assembly;
    }

    @Override
    public int hashCode() {
        return ((assembly == null) ? "" : assembly.getName()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof ServiceAssemblyItemImpl) {
            ServiceAssembly otherAssembly = ((ServiceAssemblyItemImpl) obj).assembly;

            result = (this.assembly == otherAssembly);
        }
        return result;
    }

}

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
package org.opentravel.schemacompiler.index;

import java.io.File;
import java.net.URL;

import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * Abstract base class for all tasks associated with the <code>FreeTextSearchService</code> of the
 * OTA2.0 repository.
 * 
 * @author S. Livezey
 */
public abstract class AbstractFreeTextSearchTask {

    /**
     * Indicates the version type of an indexed document.
     */
    protected static enum IndexVersionType {
        STANDARD, HEAD, HEAD_FINAL
    };

    protected static final String IDENTITY_FIELD = "identity";
    protected static final String BASE_NAMESPACE_FIELD = "baseNamespace";
    protected static final String FILENAME_FIELD = "filename";
    protected static final String STATUS_FIELD = "status";
    protected static final String VERSION_FIELD = "version";
    protected static final String VERSION_TYPE_FIELD = "versionType";
    protected static final String CONTENT_FIELD = "content";

    protected RepositoryManager repositoryManager;

    /**
     * Constructor that provides the repository manager to use when accessing all repository
     * context.
     * 
     * @param repositoryManager
     *            the repository that owns all content to be indexed
     */
    public AbstractFreeTextSearchTask(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    /**
     * Returns the metadata for the given repository item.
     * 
     * @param item
     *            the item whose metadata is to be identified
     * @return File
     */
    protected File getMetadataFile(RepositoryItem item) throws RepositoryException {
        URL metadataUrl = repositoryManager.getContentLocation(item);
        File metadataFile = URLUtils.toFile(metadataUrl);

        return metadataFile;
    }

    /**
     * Returns the file content of the given repository item.
     * 
     * @param item
     *            the item whose content is to be identified
     * @return File
     */
    protected File getContentFile(RepositoryItem item) throws RepositoryException {
        URL contentUrl = repositoryManager.getContentLocation(item);
        File contentFile = URLUtils.toFile(contentUrl);

        return contentFile;
    }

}

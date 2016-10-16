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
import java.io.IOException;

import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryInfoType;
import org.opentravel.schemacompiler.config.FileResource;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.repository.impl.DefaultRepositoryFileManager;

/**
 * File resource used to access the repository meta-data record.
 * 
 * @author S. Livezey
 */
public class RepositoryMetadataResource extends FileResource<RepositoryInfoType> {

    public RepositoryFileManager fileManager;

    public RepositoryMetadataResource(File repositoryLocation) {
        super(repositoryLocation);
        this.fileManager = new DefaultRepositoryFileManager(repositoryLocation);
        invalidateResource();
    }

    /**
     * @see org.opentravel.schemacompiler.config.FileResource#loadResource(java.io.File)
     */
    @Override
    protected RepositoryInfoType loadResource(File dataFile) throws IOException {
        try {
            RepositoryInfoType metadata = null;

            if (fileManager != null) {
                metadata = fileManager.loadRepositoryMetadata();
            }
            return metadata;

        } catch (RepositoryException e) {
            throw new IOException(e.getMessage());
        }
    }

}

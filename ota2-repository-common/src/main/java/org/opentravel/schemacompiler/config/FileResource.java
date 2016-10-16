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
package org.opentravel.schemacompiler.config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generic resource that provides access to structured information that is loaded from a data file.
 * Each time the resource is accessed, the timestamp of the file is checked to determine if the
 * content of the resource needs to be reloaded.
 * 
 * @param R
 *            the type of resource that is loaded from the data file
 * @author S. Livezey
 */
public abstract class FileResource<R> {

    private static Log log = LogFactory.getLog(FileResource.class);
    private File dataFile;
    private long lastUpdated = -1;
    private R resource;

    /**
     * Constructor that specifies the data file from which the resource is to be loaded.
     * 
     * @param dataFile
     *            the data file that provides persistent content for this resource instance
     */
    public FileResource(File dataFile) {
        this.dataFile = dataFile;
        refreshResource(dataFile);

        if (resource == null) {
            resource = getDefaultResourceValue();
        }
    }

    /**
     * Returns the handle of the data file for this resource.
     * 
     * @return File
     */
    protected File getDataFile() {
        return dataFile;
    }

    /**
     * Returns the resource instance after first checking to determine if an update from the data
     * file is required.
     * 
     * @return R
     */
    public R getResource() {
        refreshResource(dataFile);
        return resource;
    }

    /**
     * If the resource cannot be loaded from the file system, this method will provide a default
     * value for the resource. This base class method returns null - sub-classes must override to
     * provide customized default values (if a default is, in fact, desired).
     * 
     * @return R
     */
    protected R getDefaultResourceValue() {
        return null;
    }

    /**
     * Checks the last-updated data of the file from which this resource is loaded. If the file has
     * been modified, its contents will be reloaded. If an error occurs during reload, the problem
     * will be logged and the existing resource value left unchanged.
     * 
     * @param dataFile
     *            the data file that provides persistent content for this resource instance
     */
    protected void refreshResource(File dataFile) {
        try {
            if (dataFile.exists() && (lastUpdated < dataFile.lastModified())) {
                resource = loadResource(dataFile);
                lastUpdated = dataFile.lastModified();
            }
        } catch (IOException e) {
            log.error("Error loading resource contents: " + dataFile.getName(), e);
        }
    }

    /**
     * Adjusts the internally-tracked modification date of the file resource, forcing it to be
     * reloaded the next time it is accessed.
     */
    public void invalidateResource() {
        lastUpdated = -1;
    }

    /**
     * Loads the contents of the resource from the indicated data file.
     * 
     * @param dataFile
     *            the data file from which to load the resource's content
     * @return R
     * @throws IOException
     *             thrown if the resource cannot be loaded
     */
    protected abstract R loadResource(File dataFile) throws IOException;

}

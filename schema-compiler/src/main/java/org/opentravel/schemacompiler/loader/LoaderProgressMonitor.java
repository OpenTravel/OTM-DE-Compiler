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

package org.opentravel.schemacompiler.loader;

/**
 * Interactivly reports the progress of a project-load operation.
 */
public interface LoaderProgressMonitor {

    /**
     * Called by the <code>ProjectManager</code> when a project-load operation is about to begin.
     * 
     * @param libraryCount the number of libraries expected to load
     */
    public void beginLoad(int libraryCount);

    /**
     * Called when a library is about to be loaded by the <code>ProjectManager</code>.
     * 
     * @param libraryFilename the filename of the library about to be loaded
     */
    public void loadingLibrary(String libraryFilename);

    /**
     * Called immediately following a library load.
     */
    public void libraryLoaded();

    /**
     * Called when the project-load operation is complete.
     */
    public void done();

}

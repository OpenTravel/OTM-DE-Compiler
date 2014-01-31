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
package org.opentravel.schemacompiler.saver;

import java.net.URL;

import org.opentravel.ns.ota2.librarymodel_v01_04.Library;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Performs the work of saving library content back to its original URL location.
 * 
 * @author S. Livezey
 */
public interface LibrarySaveHandler {

    /**
     * Returns true if this handler is capable of saving library content to the given URL.
     * 
     * @param libraryUrl
     *            the library URL to analyze
     * @return boolean
     */
    public boolean canSave(URL libraryUrl);

    /**
     * Performs a preliminary validation of the library before actually attempting to store content
     * using the 'saveLibraryContent()' method. Unlike the components of the compiler's
     * <code>validate</code> package, this method only ensures that the required prerequisites for
     * marshalling file content have been fulfilled (e.g. conformance with the library XML schema).
     * 
     * @param library
     *            the library content to validate
     * @return ValidationFindings
     */
    public ValidationFindings validateLibraryContent(Library library);

    /**
     * Saves the given JAXB library to the specified URL location.
     * 
     * @param libraryUrl
     *            the target location where the library's content will be saved
     * @param library
     *            the library to save
     * @throws LibrarySaveException
     *             thrown if a system error occurs during the save operation
     * @throws IllegalArgumentException
     *             thrown if the handler does not support the protocol or location specified by the
     *             URL provided
     */
    public void saveLibraryContent(URL libraryUrl, Library library) throws LibrarySaveException;

    /**
     * Returns true if this save handler will create a backup file during processing of the
     * 'saveLibraryContent()' method.
     * 
     * @return boolean
     */
    public boolean isCreateBackupFile();

    /**
     * Assigns the flag value that determines whether this save handler will create a backup file
     * during processing of the 'saveLibraryContent()' method.
     * 
     * @param createBackupFile
     *            the flag value to assign
     */
    public void setCreateBackupFile(boolean createBackupFile);

}

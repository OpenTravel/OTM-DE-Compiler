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
package org.opentravel.schemacompiler.security.impl;

import java.io.File;
import java.net.URL;

import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.security.LibraryAccessController;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * Library access controller component that utilizes the <code>ProtectedNamespaceRegistry</code> to
 * determine if a user has write access to libraries.
 * 
 * @author S. Livezey
 */
public class DefaultLibraryAccessController implements LibraryAccessController {

    /**
     * @see org.opentravel.schemacompiler.security.LibraryAccessController#hasModifyPermission(org.opentravel.schemacompiler.model.TLLibrary)
     */
    @Override
    public boolean hasModifyPermission(TLLibrary library) {
        // Only check protected namespace credentials if the file is not read-only
        return isWritableLibraryFile(library);
    }

    /**
     * Returns true if the library is located in a writable file on the local file system, and its
     * status is non-Final (i.e. a draft library).
     * 
     * @param library
     *            the library to analyze
     * @return boolean
     */
    private boolean isWritableLibraryFile(TLLibrary library) {
        URL libraryUrl = library.getLibraryUrl();
        boolean canModify = (libraryUrl == null) || (library.getStatus() == TLLibraryStatus.FINAL);

        if (!canModify && URLUtils.isFileURL(libraryUrl)) {
            File libraryFile = URLUtils.toFile(libraryUrl);

            // Allow the file to be written if it is a new file that does not yet exist on the file
            // system
            canModify = !libraryFile.exists() || libraryFile.canWrite();
        }
        return canModify;
    }

}

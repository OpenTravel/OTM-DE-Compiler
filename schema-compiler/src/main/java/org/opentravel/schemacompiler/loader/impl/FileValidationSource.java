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
package org.opentravel.schemacompiler.loader.impl;

import java.io.File;

import org.opentravel.schemacompiler.validate.Validatable;

/**
 * Source object wrapper for URL objects used for compatability with the validation framework.
 * 
 * @author S. Livezey
 */
public class FileValidationSource implements Validatable {

    private File file;

    /**
     * Constructor that specifies the file reference instance to be wrapped.
     * 
     * @param file
     *            the file instance
     */
    public FileValidationSource(File file) {
        this.file = file;
    }

    /**
     * Returns the underlying File instance.
     * 
     * @return File
     */
    public File getFile() {
        return file;
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        return (file == null) ? "[UNKNOWN FILE]" : file.getName();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof FileValidationSource) {
            result = (((FileValidationSource) obj).file == this.file);
        }
        return result;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (file == null) ? 0 : file.hashCode();
    }

}

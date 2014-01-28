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

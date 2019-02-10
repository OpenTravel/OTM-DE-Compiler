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

import java.io.File;

import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Wrapper class that contains the <code>TLLibrary</code> content as well as an indicator of whether the library was
 * originally saved in the 1.6 format.
 */
public class LibraryContentWrapper {
    
    private TLLibrary content;
    private File contentFile;
    private boolean is16Library;
    
    /**
     * Full constructor.
     * 
     * @param content the OTM library content
     * @param contentFile the original file from which the library content was loaded
     * @param is16Library flag indicating whether the library was originally saved in the 1.6 format
     */
    public LibraryContentWrapper(TLLibrary content, File contentFile, boolean is16Library) {
        this.setContent( content );
        this.setContentFile( contentFile );
        this.setIs16Library( is16Library );
    }
    
    /**
     * Returns the value of the 'content' field.
     *
     * @return TLLibrary
     */
    public TLLibrary getContent() {
        return content;
    }
    
    /**
     * Assigns the value of the 'content' field.
     *
     * @param content the field value to assign
     */
    public void setContent(TLLibrary content) {
        this.content = content;
    }
    
    /**
     * Returns the value of the 'contentFile' field.
     *
     * @return File
     */
    public File getContentFile() {
        return contentFile;
    }
    
    /**
     * Assigns the value of the 'contentFile' field.
     *
     * @param contentFile the field value to assign
     */
    public void setContentFile(File contentFile) {
        this.contentFile = contentFile;
    }
    
    /**
     * Returns the value of the 'is16Library' field.
     *
     * @return boolean
     */
    public boolean isIs16Library() {
        return is16Library;
    }
    
    /**
     * Assigns the value of the 'is16Library' field.
     *
     * @param is16Library the field value to assign
     */
    public void setIs16Library(boolean is16Library) {
        this.is16Library = is16Library;
    }
    
}

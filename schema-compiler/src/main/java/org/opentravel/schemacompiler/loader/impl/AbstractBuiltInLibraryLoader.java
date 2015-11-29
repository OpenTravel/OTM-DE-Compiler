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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.loader.BuiltInLibraryLoader;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;

/**
 * Base class for <code>BuiltInLibraryLoader</code> components that obtain content from a file on
 * the file system or from the local classpath.
 * 
 * @author S. Livezey
 */
public abstract class AbstractBuiltInLibraryLoader implements BuiltInLibraryLoader {

    private SchemaDeclaration libraryDeclaration;

    /**
     * Returns an input source for the schema location that has been specified for the built-in
     * library file.
     * 
     * @return LibraryInputSource<InputStream>
     * @throws LibraryLoaderException
     *             thrown if an error occurs while constructing the input source or the library file
     *             does not exist
     */
    protected LibraryInputSource<InputStream> getInputSource() throws LibraryLoaderException {
        LibraryInputSource<InputStream> inputSource = null;
        String libraryUrl = getLibraryUrl();

        if (libraryDeclaration == null) {
            throw new LibraryLoaderException(
                    "No library declaration assigned for the built-in library loader.");
        }
        try {
            inputSource = new LibraryStreamInputSource(new URL(libraryUrl), libraryDeclaration);

        } catch (MalformedURLException e) {
            throw new LibraryLoaderException("Invalid library URL: " + libraryUrl
                    + " (the namespace and/or library name needs to be modified).");
        } catch (Throwable t) {
            throw new LibraryLoaderException("Unknown error loading built-in library: "
                    + libraryDeclaration.getName(), t);
        }
        return inputSource;
    }

    /**
     * Returns the library URL as a concatenation of the namespace and name for the assigned schema
     * declaration.
     * 
     * @return String
     */
    private String getLibraryUrl() {
        StringBuilder libraryUrl = new StringBuilder();

        if (libraryDeclaration != null) {
            if (libraryDeclaration.getNamespace() != null) {
                libraryUrl.append(libraryDeclaration.getNamespace());
            }
            if (!libraryUrl.toString().endsWith("/")) {
                libraryUrl.append("/");
            }
            String location = libraryDeclaration.getLocation(
            		CodeGeneratorFactory.XSD_TARGET_FORMAT).replaceAll("\\\\", "/");
            int pathPos = location.lastIndexOf('/');
            String filename;

            if (pathPos >= 0) {
                if ((pathPos + 1) < location.length()) {
                    filename = location.substring(pathPos + 1);
                } else {
                    filename = ""; // no filename - path ends with a '/'
                }
            } else {
                filename = location;
            }
            libraryUrl.append(filename);
        }
        return libraryUrl.toString();
    }

    /**
     * Returns the declaration for the built-in library to be loaded.
     * 
     * @return SchemaDeclaration
     */
    public SchemaDeclaration getLibraryDeclaration() {
        return libraryDeclaration;
    }

    /**
     * Assigns the declaration for the built-in library to be loaded.
     * 
     * @param libraryDeclaration
     *            the declaration to assign
     */
    public void setLibraryDeclaration(SchemaDeclaration libraryDeclaration) {
        this.libraryDeclaration = libraryDeclaration;
    }

}

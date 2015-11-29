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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * Input source used by loaders that obtain their content from an input stream.
 * 
 * @author S. Livezey
 */
public class LibraryStreamInputSource implements LibraryInputSource<InputStream> {

    private URL libraryUrl;
    private SchemaDeclaration schemaDeclaration;

    /**
     * Constructor that assigns the URL from which the library's content will be loaded.
     * 
     * @param libraryUrl
     *            the URL location of the library's content
     */
    public LibraryStreamInputSource(URL libraryUrl) {
        this.libraryUrl = URLUtils.normalizeUrl(libraryUrl);
    }

    /**
     * Constructor that assigns the file from which the library's content will be loaded.
     * 
     * @param libraryFile
     *            the file location of the library's content
     */
    public LibraryStreamInputSource(File libraryFile) {
        this.libraryUrl = URLUtils.toURL(libraryFile);
    }

    /**
     * Constructor that explicitly assigns a content stream for the library to that of the given
     * schema declaration instead of allowing the URL location to resolve the location of the file's
     * input stream.
     * 
     * @param libraryUrl
     *            the URL to assign for the library after loading is complete (not used to obtain
     *            content location)
     * @param schemaDeclaration
     *            the schema declaration that provides direct acces to the library file's content
     */
    public LibraryStreamInputSource(URL libraryUrl, SchemaDeclaration schemaDeclaration) {
        this.libraryUrl = libraryUrl;
        this.schemaDeclaration = schemaDeclaration;
    }

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryInputSource#getLibraryURL()
     */
    public URL getLibraryURL() {
        return libraryUrl;
    }

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryInputSource#getLibraryContent()
     */
    public InputStream getLibraryContent() {
        InputStream contentStream = null;

        try {
            if (schemaDeclaration != null) {
                contentStream = schemaDeclaration.getContent(CodeGeneratorFactory.XSD_TARGET_FORMAT);
            }
            if ((contentStream == null) && (libraryUrl != null)) {
                if (URLUtils.isFileURL(libraryUrl)) {
                    try {
                        contentStream = new FileInputStream(URLUtils.toFile(libraryUrl));

                    } catch (IllegalArgumentException e) {
                        // No action - use the URL.openStream() method to obtain a connection
                    }
                }
                if (contentStream == null) {
                    contentStream = libraryUrl.openStream();
                }
            }

        } catch (IOException e) {
            // No action - the contract requires that we eat this exception and return null
        }
        return contentStream;
    }

}

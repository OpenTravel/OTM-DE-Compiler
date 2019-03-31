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

package org.opentravel.schemacompiler.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Utility methods used for general file handling.
 */
public class FileUtils {

    private static final Logger log = LoggerFactory.getLogger( FileUtils.class );

    /**
     * Private constructor to prevent instantiation.
     */
    private FileUtils() {}

    /**
     * Deletes the given file from the file system. If the file cannot be deleted, a warning message is logged to
     * explain the reason. If the file passed to this method is null, no action will be taken.
     * 
     * @param file the file to be deleted
     */
    public static void delete(File file) {
        confirmDelete( file );
    }

    /**
     * Deletes the given file from the file system and returns true if successful. If the file cannot be deleted, a
     * warning message is logged to explain the reason and false will be returned. If the file passed to this method is
     * null, no action will be taken but false will be returned.
     * 
     * @param file the file to be deleted
     * @return boolean
     */
    public static boolean confirmDelete(File file) {
        boolean successful = false;

        if (file != null) {
            try {
                Files.deleteIfExists( file.toPath() );
                successful = true;

            } catch (IOException e) {
                log.warn( String.format( "Error deleting file: %s", e.getMessage() ) );
            }
        }
        return successful;
    }

    /**
     * Renames the given file on the file system. If the file cannot be renamed, a warning message is logged to explain
     * the reason. If the file passed to this method is null, no action will be taken.
     * 
     * @param origFile the original file to be renamed
     * @param renamedFile the new name for the file
     */
    public static void renameTo(File origFile, File renamedFile) {
        confirmRenameTo( origFile, renamedFile );
    }

    /**
     * Renames the given file on the file system. If the file cannot be renamed, a warning message is logged to explain
     * the reason. If the file passed to this method is null, no action will be taken.
     * 
     * @param origFile the file to be renamed
     * @param renamedFile the new name for the file
     * @return boolean
     */
    public static boolean confirmRenameTo(File origFile, File renamedFile) {
        boolean successful = false;

        if ((origFile != null) && (renamedFile != null)) {
            successful = origFile.renameTo( renamedFile );
        }
        return successful;
    }

    /**
     * Parses content from the given file using the JAXB unmarshaller provided.
     * 
     * @param file the file from which to unmarshal content
     * @param unmarshaller the unmarshaller to use for parsing and building the corresponding object structure
     * @return Object
     * @throws JAXBException thrown if the file content cannot be parsed by the unmarshaller
     * @throws IOException thrown if the file cannot be opened or read from
     */
    public static Object unmarshalFileContent(File file, Unmarshaller unmarshaller) throws JAXBException, IOException {
        try (Reader reader = new FileReader( file )) {
            return unmarshaller.unmarshal( reader );
        }
    }

    /**
     * Parses content from the given input stream using the JAXB unmarshaller provided.
     * 
     * @param in the input stream from which to unmarshal content
     * @param unmarshaller the unmarshaller to use for parsing and building the corresponding object structure
     * @return Object
     * @throws JAXBException thrown if the stream content cannot be parsed by the unmarshaller
     * @throws IOException thrown if the stream cannot be opened or read from
     */
    public static Object unmarshalStreamContent(InputStream in, Unmarshaller unmarshaller)
        throws JAXBException, IOException {
        try (Reader reader = new InputStreamReader( in )) {
            return unmarshaller.unmarshal( reader );
        }
    }

}

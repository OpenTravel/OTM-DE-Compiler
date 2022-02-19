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

package org.opentravel.schemacompiler.repository.testutil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.schemacompiler.util.FileUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Static utility methods shared by numerous tests.
 * 
 * @author S. Livezey
 */
public class RepositoryTestUtils {

    private static Logger log = LogManager.getLogger( RepositoryTestUtils.class );

    /**
     * Private constructor to prevent instantiation.
     */
    private RepositoryTestUtils() {}

    /**
     * Displays the validation findings if debugging is enabled.
     * 
     * @param findings the validation findings to display
     */
    public static void printFindings(ValidationFindings findings) {
        printFindings( findings, null );
    }

    /**
     * Displays the validation findings if one or more findings of the specified type are present (and debugging is
     * enabled).
     * 
     * @param findings the validation findings to display
     * @param findingType the finding type to search for
     */
    public static void printFindings(ValidationFindings findings, FindingType findingType) {
        boolean hasFindings = ((findingType == null) && findings.hasFinding())
            || ((findingType != null) && findings.hasFinding( findingType ));

        if (hasFindings) {
            log.error( "Validation Findings:" );

            for (String message : findings.getAllValidationMessages( FindingMessageFormat.DEFAULT )) {
                log.error( "  " + message );
            }
        }
    }

    /**
     * Recursively deletes the contents of the specified folder and removes the folder itself.
     * 
     * @param fileOrFolder the file or folder location to delete
     */
    public static void deleteContents(File fileOrFolder) {
        if (fileOrFolder.isDirectory()) {
            for (File folderMember : fileOrFolder.listFiles()) {
                deleteContents( folderMember );
            }
        }
        FileUtils.delete( fileOrFolder );
    }

    /**
     * Recursively copies the contents of the source folder to the specified destination.
     * 
     * @param src the source folder location
     * @param dest the destination folder location
     * @throws IOException thrown if one or more files cannot be copied
     */
    public static void copyContents(File src, File dest) throws IOException {
        if (!src.exists()) {
            throw new IOException( "Source file not found: " + src.getAbsolutePath() + "." );

        } else if (!src.canRead()) {
            throw new IOException( "Source file cannot be read: " + src.getAbsolutePath() + "." );
        }

        if (src.isDirectory()) {
            if (!dest.exists() && !dest.mkdirs()) {
                throw new IOException( "Unable to create direcotry: " + dest.getAbsolutePath() + "." );
            }
            String[] list = src.list();

            for (int i = 0; i < list.length; i++) {
                File dest1 = new File( dest, list[i] );
                File src1 = new File( src, list[i] );

                if (src1.getName().equals( ".svn" )) {
                    continue;
                }
                copyContents( src1, dest1 );
            }

        } else {
            byte[] buffer = new byte[4096];
            int bytesRead;

            try (InputStream fin = new FileInputStream( src )) {
                try (OutputStream fout = new FileOutputStream( dest )) {
                    while ((bytesRead = fin.read( buffer )) >= 0) {
                        fout.write( buffer, 0, bytesRead );
                    }
                }
            }
        }
    }

}

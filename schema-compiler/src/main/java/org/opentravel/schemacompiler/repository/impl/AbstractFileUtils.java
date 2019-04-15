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

import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Base class for utility file managers that provide methods for creating, restoring, and deleting backup files during
 * save operations.
 */
public abstract class AbstractFileUtils {

    RepositoryManager repositoryManager;

    /**
     * Constructor that supplies the repository manager to be used during file utility operations.
     * 
     * @param repositoryManager the repository manager instance
     */
    public AbstractFileUtils(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    /**
     * Returns true if the given file references a location in the user's local repository -- either as a
     * locally-managed item or a local copy of a remotely-managed library or release.
     * 
     * @param file the release or library file to analyze
     * @return boolean
     */
    public boolean isRepositoryFile(File file) {
        boolean result = false;

        if (file != null) {
            File repositoryLocation = repositoryManager.getRepositoryLocation();
            File libraryFolder = file.getParentFile();

            while (!result && (libraryFolder != null)) {
                result = libraryFolder.equals( repositoryLocation );
                libraryFolder = libraryFolder.getParentFile();
            }
        }
        return result;
    }

    /**
     * Creates a backup of the specified original file on the local file system. The location of the newly-created
     * backup file is returned by this method.
     * 
     * @param originalFile the original file to backup
     * @return File
     * @throws IOException thrown if the backup file cannot be created
     */
    public File createBackupFile(File originalFile) throws IOException {
        File backupFile;

        if (!originalFile.exists()) {
            backupFile = null;

        } else {
            byte[] buffer = new byte[1024];
            int bytesRead;

            backupFile = new File( originalFile.getParentFile(), getBackupFilename( originalFile ) );
            try (InputStream in = new FileInputStream( originalFile )) {
                try (OutputStream out = new FileOutputStream( backupFile )) {
                    while ((bytesRead = in.read( buffer )) >= 0) {
                        out.write( buffer, 0, bytesRead );
                    }
                }
            }
        }
        return backupFile;
    }

    /**
     * Creates a backup of the specified original file on the local file system.
     * 
     * @param backupFile the backup file to restore
     * @param originalFilename the original name of the file to be restored (without the filepath)
     * @throws IOException thrown if the backup file cannot be restored
     */
    public void restoreBackupFile(File backupFile, String originalFilename) throws IOException {
        if ((backupFile != null) && backupFile.exists()) {
            String filename = backupFile.getName();

            if (!filename.endsWith( ".bak" )) {
                throw new IllegalArgumentException( "The specified file is not a valid backup." );
            }
            if (!backupFile.exists()) {
                throw new IllegalStateException(
                    "The specified backup file no longer exists: " + backupFile.getAbsolutePath() );
            }
            File originalFile = new File( backupFile.getParentFile(), originalFilename );

            if (originalFile.exists() && !FileUtils.confirmDelete( originalFile )) {
                throw new IOException(
                    "Unable to delete original file during restoration of backup: " + backupFile.getAbsolutePath() );
            }
            FileUtils.renameTo( backupFile, originalFile );
        }
    }

    /**
     * Deletes the inidicated backup file from the local file system.
     * 
     * @param backupFile the backup file to remove
     */
    public void removeBackupFile(File backupFile) {
        FileUtils.delete( backupFile );
    }

    /**
     * Returns the name of the backup file for the specified original.
     * 
     * @param originalFile the original file for which a backup is being created
     * @return String
     */
    public String getBackupFilename(File originalFile) {
        String filename = null;

        if (originalFile != null) {
            String originalFilename = originalFile.getName();
            int dotIdx = originalFilename.lastIndexOf( '.' );

            filename = ((dotIdx < 0) ? originalFilename : originalFilename.substring( 0, dotIdx )) + ".bak";
        }
        return filename;
    }

    /**
     * Copies the specified source file's content to the destination file's location. If the destination file already
     * exists, it will be overwritten.
     * 
     * @param sourceFile the source file's location
     * @param destinationFile the destination file's location
     * @throws IOException thrown if the file cannot be copied
     */
    public void copyFile(File sourceFile, File destinationFile) throws IOException {
        if (!destinationFile.getParentFile().exists()) {
            destinationFile.getParentFile().mkdirs();
        }
        try (InputStream in = new FileInputStream( sourceFile )) {
            try (OutputStream out = new FileOutputStream( destinationFile )) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = in.read( buffer )) >= 0) {
                    out.write( buffer, 0, bytesRead );
                }
            }
        }
    }

}

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.util.FileUtils;

/**
 * Default implementation of the <code>RepositoryFileManager</code> that employs a simple file
 * system strategy for maintaining the integrity of files. Each time a file is saved (or manually
 * added to the change set), a backup of the affected file is created. When the change set is
 * committed, all backup files are deleted. If the change set is rolled back, the contents of each
 * file is replaced with the backup that was created.
 * 
 * @author S. Livezey
 */
public class DefaultRepositoryFileManager extends RepositoryFileManager {

    private static Log log = LogFactory.getLog(DefaultRepositoryFileManager.class);

    /**
     * Constructor that initializes a new instance used for managing and retrieving files in an
     * OTA2.0 repository at the specified file location.
     * 
     * @param repositoryLocation
     *            the folder location where the OTA2.0 repository resides on the local file system
     */
    public DefaultRepositoryFileManager(File repositoryLocation) {
        super(repositoryLocation);
    }

    /**
     * Prior to a file being added to the change set, this method creates a backup of that file on
     * the local file system.
     * 
     * @see org.opentravel.schemacompiler.repository.RepositoryFileManager#addToChangeSet(java.io.File)
     */
    @Override
    public void addToChangeSet(File file) throws RepositoryException {
        File backupFile = getBackupFile(file);
        try {
            if (file.exists()) {
                createBackupFile(file, backupFile);

                if (log.isDebugEnabled()) {
                		log.debug(String.format("Created backup file: %s [Change Set - %s]",
                				file.getName(), Thread.currentThread().getName()));
                }
            }
            super.addToChangeSet(file);

        } catch (IOException e) {
        		log.error(String.format("Error creating backup file: %s [Change Set - %s]",
        				file.getName(), Thread.currentThread().getName()));
            throw new RepositoryException("Error creating backup file: " + file.getName(), e);
        }
    }

    /**
     * Removes all backup files that were created by the <code>addToChangeSet()</code> method since
     * this thread's last call to <code>startChangeSet()</code>.
     * 
     * @see org.opentravel.schemacompiler.repository.RepositoryFileManager#commitChangeSet(java.util.Set)
     */
    @Override
    protected void commitChangeSet(Set<File> changeSet) throws RepositoryException {
        for (File changedFile : changeSet) {
            File backupFile = getBackupFile(changedFile);

            if (backupFile.exists()) {
                removeBackupFile(backupFile);

                if (log.isDebugEnabled()) {
                		log.debug(String.format("Removed backup file: %s [Change Set - %s]",
                				backupFile.getName(), Thread.currentThread().getName()));
                }
            }
        }
    }

    /**
     * For each file in the change set, the contents of the original file are replaced with the
     * backup.
     * 
     * @see org.opentravel.schemacompiler.repository.RepositoryFileManager#rollbackChangeSet(java.util.Set)
     */
    @Override
    protected void rollbackChangeSet(Set<File> changeSet) throws RepositoryException {
        boolean success = true;

        for (File changedFile : changeSet) {
            File backupFile = getBackupFile(changedFile);
            try {
                restoreBackupFile(backupFile, changedFile);

                if (log.isDebugEnabled()) {
                    log.debug("Restored backup file: " + changedFile.getName() + " [Change Set - "
                            + Thread.currentThread().getName() + "]");
                }
            } catch (IOException e) {
                log.error("Error restoring backup file: " + backupFile.getName()
                        + " [Change Set - " + Thread.currentThread().getName() + "]", e);
                success = false;
            }
        }
        if (!success) {
            throw new RepositoryException(
                    "Error while restoring one or more backup files in the repository.");
        }
    }

    /**
     * Returns a handle for the backup file for the specified repository file. Neither the
     * repository file nor the backup file needs to exist for this method to succeed.
     * 
     * @param repositoryFile
     *            the repository file for which to retrieve the backup file handle
     * @return File
     */
    protected File getBackupFile(File repositoryFile) {
        return new File(repositoryFile.getParentFile(), repositoryFile.getName() + ".bak");
    }

    /**
     * Creates a backup of the specified original file on the local file system. If the original
     * file passed to this method does not exist, an exception will be thrown.
     * 
     * @param originalFile
     *            the original file to backup
     * @return File
     * @throws IOException
     *             thrown if the backup file cannot be created
     */
	protected void createBackupFile(File originalFile, File backupFile) throws IOException {
		if (originalFile.isFile()) {
			try (InputStream in = new FileInputStream(originalFile)) {
				try (OutputStream out = new FileOutputStream(backupFile)) {
					byte[] buffer = new byte[1024];
					int bytesRead;
					
					while ((bytesRead = in.read(buffer)) >= 0) {
						out.write(buffer, 0, bytesRead);
					}
				}
			}
		}
	}
	
    /**
     * Creates a backup of the specified original file on the local file system. If the backup file
     * does not exist, the original file is considered be newly created; in these cases the original
     * file is simply deleted by this method.
     * 
     * @param backupFile
     *            the backup file to restore
     * @param originalFile
     *            the original file whose content is to be restored from the backup
     * @throws IOException
     *             thrown if the backup file cannot be restored
     */
    protected void restoreBackupFile(File backupFile, File originalFile) throws IOException {
        if (originalFile.exists() && !FileUtils.confirmDelete(originalFile)) {
            throw new IOException(
                    "Unable to delete original file during restoration of backup: "
                            + backupFile.getAbsolutePath());
        }
        FileUtils.renameTo( backupFile, originalFile );
    }

    /**
     * Deletes the inidicated backup file from the local file system.
     * 
     * @param backupFile
     *            the backup file to remove
     */
    protected void removeBackupFile(File backupFile) {
    		FileUtils.delete( backupFile );
    }

}

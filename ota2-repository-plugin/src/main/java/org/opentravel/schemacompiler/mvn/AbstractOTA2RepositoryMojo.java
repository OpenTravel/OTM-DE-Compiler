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
package org.opentravel.schemacompiler.mvn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.opentravel.schemacompiler.ic.ImportManagementIntegrityChecker;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.loader.impl.MultiVersionLibraryModuleLoader;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.saver.impl.Library15FileSaveHandler;
import org.opentravel.schemacompiler.saver.impl.Library16FileSaveHandler;
import org.opentravel.schemacompiler.security.LibraryCrcCalculator;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Base plugin class that handles the creation of OTA2 repository snapshots.  These snapshots
 * allow development teams to insulate themselves from daily changes to DRAFT items in the
 * OTA2.0 repository by copying the contents of an OTM project into a local directory.  Teams
 * may then manually invoke the snapshot plugin to force an update of the snapshot when they
 * are ready to incorporate the latest changes into their code.
 * 
 * <p>It should be noted that these plugins are only intended for use with projects that are
 * under active development.  Teams should be careful to ensure that this plugin is disabled
 * once their work is complete to ensure that their local codebase in synchronized with the
 * contents of the OTA2.0 repository.
 */
public abstract class AbstractOTA2RepositoryMojo extends AbstractMojo {
	
	/**
	 * The location of the OTM project file from which the repository snapshot is to be
	 * taken (required).
	 */
	@Parameter( required=true )
	protected File otmProject;
	
	/**
	 * Indicates whether execution of the 'ota2-repository' goals are enabled (default is true).
	 */
	@Parameter
	protected boolean enabled = true;
	
	/**
	 * Creates or updates the OTA2 repository snapshot.
	 * 
	 * @throws MojoFailureException  thrown if validation errors are detected in the OTM project
	 * @throws MojoExecutionException  thrown if the snapshot update fails for any reason
	 */
	protected void createOrUpdateSnapshot() throws MojoFailureException, MojoExecutionException {
		File snapshotProjectFile = getSnapshotProjectFile();
		File snapshotFolder = getSnapshotFolder();
		File backupProject = backup( snapshotProjectFile );
		File backupFolder = backup( snapshotFolder );
		boolean success = false;
		Log log = getLog();
		
		try {
			log.info("Loading OTM project: " + otmProject.getName());
			ValidationFindings findings = new ValidationFindings();
			ProjectManager projectManager = new ProjectManager( false );
			Project originalProject = projectManager.loadProject( otmProject, findings );
			
			// Ensure no errors exist in the OTM project before proceeding
			if (findings.hasFinding( FindingType.ERROR )) {
                String[] messages = findings
                        .getAllValidationMessages(FindingMessageFormat.IDENTIFIED_FORMAT);

                log.info("Errors/warnings detected in project:");
                for (String message : messages) {
                    log.info(message);
                }
                throw new MojoFailureException(
                		"Unable to create repository snapshot because the project contains errors.");
			}
			
			// Create the new project and migrate all OTM files to the snapshot folder
			log.info("Building snapshot project: " + snapshotProjectFile.getName());
			Project snapshotProject = createSnapshotProject( originalProject, snapshotProjectFile, snapshotFolder );
			List<File> snapshotLibraryFiles = new ArrayList<>();
			Map<String,Boolean> otm16Registry = new HashMap<>();
			
			for (AbstractLibrary library : projectManager.getModel().getAllLibraries()) {
				if (library instanceof BuiltInLibrary) {
					continue;
				}
				File libraryFile = URLUtils.toFile( library.getLibraryUrl() );
				File snapshotFile = new File( snapshotFolder, libraryFile.getName() );
				URL snapshotUrl = URLUtils.toURL( snapshotFile );
				
				snapshotLibraryFiles.add( snapshotFile );
				library.setLibraryUrl( snapshotUrl );
				otm16Registry.put( snapshotUrl.toExternalForm(), is16Library( libraryFile ) );
			}
			
			// Update the imports and includes to the new snapshot folder location
			for (AbstractLibrary library : projectManager.getModel().getAllLibraries()) {
				if (library instanceof TLLibrary) {
					ImportManagementIntegrityChecker.verifyReferencedLibraries( (TLLibrary) library );
				}
			}
			
			// Save each library in its original format
	        LibraryModelSaver modelSaver = new LibraryModelSaver();
	        
			for (TLLibrary library : projectManager.getModel().getUserDefinedLibraries()) {
				boolean is16Library = otm16Registry.get( library.getLibraryUrl().toExternalForm() );
				
		        if (is16Library) {
		        	modelSaver.setSaveHandler( new Library16FileSaveHandler() );
		        } else {
		        	modelSaver.setSaveHandler( new Library15FileSaveHandler() );
		        }
		        modelSaver.saveLibrary( library );
		        
		        if (library.getStatus() != TLLibraryStatus.DRAFT) {
					File libraryFile = URLUtils.toFile( library.getLibraryUrl() );
					
		        	try {
						LibraryCrcCalculator.recalculateLibraryCrc( libraryFile );
						
					} catch (IOException e) {
						log.warn("Error recalculating library CRC.", e);
					}
		        }
			}
	        
			// Delete any .bak files that might exist in the snapshot folder
			for (File ssFile : snapshotFolder.listFiles()) {
				if (ssFile.getName().endsWith(".bak")) {
					ssFile.delete();
				}
			}
			
			// Populate the snapshot project with the newly-saved libraries in the snapshot folder
			projectManager.closeProject( originalProject );
			projectManager.addUnmanagedProjectItems( snapshotLibraryFiles, snapshotProject );
			projectManager.saveProject( snapshotProject, false, null );
			deleteProjectBackup( snapshotProjectFile );
			success = true;
			
		} catch (LibraryLoaderException | LibrarySaveException | RepositoryException e) {
			throw new MojoExecutionException("Error loading OTM project.", e);
			
		} finally {
			if (success) { // delete backup on success
				delete( backupProject );
				delete( backupFolder );
				
			} else { // restore backup on failure
				restoreBackup( backupProject, snapshotProjectFile );
				restoreBackup( backupFolder, snapshotFolder );
			}
		}
	}
	
	/**
	 * Creates the snapshot project file and the snapshot folder where the OTM files will be stored.
	 * 
	 * @param originalProject  the original OTM project
	 * @param snapshotProjectFile  the file location of the repository snapshot project
	 * @param snapshotFolder  the snapshot folder where the project's OTM files will be stored
	 * @return Project
	 * @throws LibrarySaveException  thrown if the new snapshot project cannot be created or saved
	 */
	protected Project createSnapshotProject(Project originalProject, File snapshotProjectFile, File snapshotFolder)
			throws LibrarySaveException {
		ProjectManager projectManager = originalProject.getProjectManager();
		Project snapshotProject = projectManager.newProject(
				snapshotProjectFile, originalProject.getProjectId() + "#snapshot",
				originalProject.getName(), originalProject.getDescription() );
		
		snapshotFolder.mkdirs();
		return snapshotProject;
	}
	
	/**
	 * Creates a backup of the given file or folder by renaming it.  The file handle
	 * returned is for the renamed backup folder.  If the file or folder cannot be
	 * backed up (i.e. renamed), this method will return null.
	 * 
	 * @param fileOrFolder  the file or folder to rename as a backup
	 * @return File
	 * @throws MojoExecutionException  thrown if the backup cannot be created because of an error
	 */
	protected File backup(File fileOrFolder) throws MojoExecutionException {
		File backupFileOrFolder = null;
		
		if ((fileOrFolder != null) && fileOrFolder.exists()) {
			String suffix = "";
			int count = 0;
			
			// Continue changing the suffix until we find a backup name that does not yet exist
			while ((backupFileOrFolder = new File(
					fileOrFolder.getParentFile(), fileOrFolder.getName() + ".bak" + suffix )).exists()) {
				suffix = (++count) + "";
			}
			
			if (!fileOrFolder.renameTo( backupFileOrFolder )) {
				throw new MojoExecutionException("Unable to create backup of existing snapshot item: "
						+ fileOrFolder.getName());
			}
		}
		return backupFileOrFolder;
	}
	
	/**
	 * Restores the original contents of the file or folder by deleting the 'original'
	 * and renaming the 'backup' to replace it.
	 * 
	 * @param backup  the backup file or folder to be restored
	 * @param original  the original file or folder name to be restored
	 */
	protected void restoreBackup(File backup, File original) {
		if (backup != null) {
			if (original.exists()) {
				delete( original );
			}
			backup.renameTo( original );
		}
	}
	
	/**
	 * Deletes the specified file or folder (and all of its contents).
	 * 
	 * @param folder  the folder to be deleted 
	 */
	protected void delete(File fileOrFolder) {
		if ((fileOrFolder != null) && fileOrFolder.exists()) {
			if (fileOrFolder.isDirectory()) {
				for (File folderItem : fileOrFolder.listFiles()) {
					delete( folderItem );
				}
			}
			fileOrFolder.delete();
		}
	}
	
	/**
	 * If a complier-generated backup of the given project file exists, it will
	 * be deleted by this method.
	 * 
	 * @param projectFile  the project file whose compiler backup will be deleted
	 */
	private void deleteProjectBackup(File projectFile) {
		String backupFilename = projectFile.getName();
		int extIdx = backupFilename.lastIndexOf('.');
		File backupFile;
		
		backupFilename = ((extIdx < 0) ? backupFilename : backupFilename.substring( 0, extIdx ) ) + ".bak";
		backupFile = new File( projectFile.getParentFile(), backupFilename );
		
		if (backupFile.exists()) {
			backupFile.delete();
		}
	}
	
	/**
	 * Validates the configuration settings of the plugin and returns true if everything
	 * is valid.  Otherwise returns false or throws a <code>MojoFailureException</code>,
	 * depending on the error that was discovered.
	 * 
	 * @return boolean
	 */
	protected boolean validate() throws MojoFailureException {
		boolean isValid = true;
		Log log = getLog();
		
		if (!enabled) {
			log.info("OTA2 repository snapshot processing disabled (skipping).");
			isValid = false;
		}
		if (otmProject == null) {
			throw new MojoFailureException("OTM project file not specified.");
		}
		if (!otmProject.exists()) {
			throw new MojoFailureException("The OTM project file does not exist: " + otmProject.getAbsolutePath());
		}
		File snapshotFolder = getSnapshotFolder();
		
		if (snapshotFolder.exists() && !snapshotFolder.isDirectory()) {
			throw new MojoFailureException("The OTM snapshot folder exists, but is not a directory: "
					+ snapshotFolder.getAbsolutePath());
		}
		return isValid;
	}
	
	/**
	 * Returns the file name and location of the repository snapshot project file.
	 * 
	 * @return File
	 */
	protected File getSnapshotProjectFile() {
		return new File( otmProject.getParentFile(), getSnapshotFilename() + ".otp" );
	}
	
	/**
	 * Returns the file name and location of the folder that will contain the OTM files
	 * for the repository snapshot.
	 * 
	 * @return File
	 */
	protected File getSnapshotFolder() {
		return new File( otmProject.getParentFile(), getSnapshotFilename() );
	}
	
	/**
	 * Returns the name of the snapshot folder based on the name of the OTM project file.  The
	 * resulting filename does not include the ".otp" extension.
	 * 
	 * @return String
	 */
	private String getSnapshotFilename() {
		String projectFilename = otmProject.getName();
		int extIdx = projectFilename.lastIndexOf('.');
		String snapshotFilename;
		
		if (extIdx < 0) {
			snapshotFilename = projectFilename;
			
		} else {
			snapshotFilename = projectFilename.substring( 0, extIdx );
		}
		snapshotFilename += "-snapshot";
		return snapshotFilename;
	}
	
	/**
	 * Returns true if the given library was originally saved in the OTM 1.6 file format.
	 * 
	 * @param libraryFile  the library file to be analyzed
	 * @return boolean
	 */
	private boolean is16Library(File libraryFile) {
    	boolean is16Library = false;

        try {
            if ((libraryFile != null) && libraryFile.exists()
                    && !libraryFile.getName().toLowerCase().endsWith(".xsd")) {
                LibraryModuleLoader<InputStream> loader = new MultiVersionLibraryModuleLoader();
                LibraryModuleInfo<Object> moduleInfo = loader.loadLibrary(
                        new LibraryStreamInputSource(libraryFile), new ValidationFindings());
                Object jaxbLibrary = moduleInfo.getJaxbArtifact();
                
                if (jaxbLibrary != null) {
                    is16Library = (jaxbLibrary instanceof org.opentravel.ns.ota2.librarymodel_v01_06.Library);
                }
            }
        } catch (Exception e) {
            // No action - method will return false
        }
        return is16Library;
	}
	
	/**
	 * Since this is a read-only application, enable the OTM 1.6 file format for
	 * all operations.
	 */
	static {
		OTM16Upgrade.otm16Enabled = true;
	}
	
}

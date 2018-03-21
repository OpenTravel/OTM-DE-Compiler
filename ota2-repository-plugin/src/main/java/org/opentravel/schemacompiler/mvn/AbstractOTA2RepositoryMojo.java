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
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.ReleaseManager;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemType;
import org.opentravel.schemacompiler.repository.RepositoryManager;
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
	@Parameter
	protected File otmProject;
	
    /**
     * The repository information for the OTM release to be compiled.
     */
	@Parameter
    protected Release release;
    private RepositoryItem releaseItem = null;
    
	/**
	 * The folder location where the snapshot project will be created.  If not specified,
	 * this will be the same folder as the original '.otp' file, or <code>/src/main/resources</code>
	 * in the case of a managed OTM release.
	 */
	@Parameter
	protected File snapshotProjectFolder;
	
	/**
	 * Indicates whether execution of the 'ota2-repository' goals are enabled (default is true).
	 */
	@Parameter
	protected boolean enabled = true;
	
	@Parameter(readonly = true, defaultValue = "${project.basedir}")
	private File javaProjectFolder;
	
	/**
	 * Creates or updates the OTA2 repository snapshot.
	 * 
	 * @throws MojoFailureException  thrown if validation errors are detected in the OTM project
	 * @throws MojoExecutionException  thrown if the snapshot update fails for any reason
	 */
	protected void createOrUpdateSnapshot() throws MojoFailureException, MojoExecutionException {
		File snapshotProjectFile = getSnapshotProjectFile();
		File snapshotLibraryFolder = getSnapshotLibraryFolder();
		File backupProject = backup( snapshotProjectFile );
		File backupFolder = backup( snapshotLibraryFolder );
		boolean success = false;
		Log log = getLog();
		
		try {
			ReleaseManager releaseManager = new ReleaseManager();
			ProjectManager projectManager = new ProjectManager( false );
			ValidationFindings findings = new ValidationFindings();
			Project originalProject = null, snapshotProject = null;
			TLModel model;
			
			if (otmProject != null) {
				if (RepositoryItemType.RELEASE.isItemType( otmProject.getName() )) {
					log.info("Loading OTM release: " + otmProject.getName());
					releaseManager.loadRelease( otmProject, findings );
					model = releaseManager.getModel();
					
				} else { // Load from an OTP file
					log.info("Loading OTM project: " + otmProject.getName());
					originalProject = projectManager.loadProject( otmProject, findings );
					model = projectManager.getModel();
				}
				
			} else { // Must be an OTM release
				log.info("Loading OTM release: " + releaseItem.getFilename());
				releaseManager.loadRelease( releaseItem, findings );
				model = releaseManager.getModel();
			}
			
			// Ensure no errors exist in the OTM project or release before proceeding
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
			
			if (originalProject != null) {
				snapshotProject = createSnapshotProject( originalProject, snapshotProjectFile,
						snapshotLibraryFolder );
				
			} else { // Must be an OTM release
				snapshotProject = createSnapshotProject( releaseManager.getRelease(),
						projectManager, snapshotProjectFile, snapshotLibraryFolder );
			}
			
			List<File> snapshotLibraryFiles = new ArrayList<>();
			Map<String,Boolean> otm16Registry = new HashMap<>();
			
			for (AbstractLibrary library : model.getAllLibraries()) {
				if (library instanceof BuiltInLibrary) {
					continue;
				}
				String libraryFilename = ProjectManager.getPublicationFilename( library );
				File snapshotFile = new File( snapshotLibraryFolder, libraryFilename );
				URL snapshotUrl = URLUtils.toURL( snapshotFile );
				
				snapshotLibraryFiles.add( snapshotFile );
				otm16Registry.put( snapshotUrl.toExternalForm(), is16Library( library.getLibraryUrl() ) );
				library.setLibraryUrl( snapshotUrl );
			}
			
			// Update the imports and includes to the new snapshot folder location
			for (AbstractLibrary library : model.getAllLibraries()) {
				if (library instanceof TLLibrary) {
					ImportManagementIntegrityChecker.verifyReferencedLibraries( (TLLibrary) library );
				}
			}
			
			// Save each library in its original format
	        LibraryModelSaver modelSaver = new LibraryModelSaver();
	        
			for (TLLibrary library : model.getUserDefinedLibraries()) {
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
			for (File ssFile : snapshotLibraryFolder.listFiles()) {
				if (ssFile.getName().endsWith(".bak")) {
					ssFile.delete();
				}
			}
			
			// Populate the snapshot project with the newly-saved libraries in the snapshot folder
			if (originalProject != null) {
				projectManager.closeProject( originalProject );
			}
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
				restoreBackup( backupFolder, snapshotLibraryFolder );
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
	 * Creates the snapshot project file and the snapshot folder where the OTM files will be stored.
	 * 
	 * @param release  the OTM release from which to create the snapshot project
	 * @param projectManager  the project manager that should be used to create the new project
	 * @param snapshotProjectFile  the file location of the repository snapshot project
	 * @param snapshotFolder  the snapshot folder where the project's OTM files will be stored
	 * @return Project
	 * @throws LibrarySaveException  thrown if the new snapshot project cannot be created or saved
	 */
	protected Project createSnapshotProject(org.opentravel.schemacompiler.repository.Release release,
			ProjectManager projectManager, File snapshotProjectFile, File snapshotFolder)
					throws LibrarySaveException {
		Project snapshotProject = projectManager.newProject(
				snapshotProjectFile, release.getBaseNamespace() + "#snapshot",
				release.getName(), release.getDescription() );
		
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
        if (otmProject != null) {
            if (!otmProject.exists()) {
                throw new MojoFailureException("Source file not found: "
                        + otmProject.getAbsolutePath());
            }
        	
        } else if (release != null) {
        	try {
        		releaseItem = RepositoryManager.getDefault().getRepositoryItem(
        				release.getBaseNamespace(), release.getFilename(), release.getVersion() );
        		
        		if (!RepositoryItemType.RELEASE.isItemType( releaseItem.getFilename() )) {
        			throw new MojoFailureException(
        					"The specified repository item is not an OTM release: " + releaseItem.getFilename());
        		}
        		
        	} catch (RepositoryException e) {
        		throw new MojoFailureException(
        				"The specified repository item does not exist or is not an OTM release.", e);
        		
        	} catch (Throwable t) {
        		throw new MojoFailureException(
        				"Unknown error while accessing the OTM repository", t);
        	}
        	
        } else {
        	throw new MojoFailureException("Either a libraryFile or a release must be specified.");
        }
        
		File snapshotFolder = getSnapshotLibraryFolder();
		
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
		return new File( getSnapshotProjectFolder(), getSnapshotFilename() + ".otp" );
	}
	
	/**
	 * Returns the file name and location of the folder that will contain the OTM files
	 * for the repository snapshot.
	 * 
	 * @return File
	 */
	protected File getSnapshotLibraryFolder() {
		return new File( getSnapshotProjectFolder(), getSnapshotFilename() );
	}
	
	/**
	 * Returns the folder location where the snapshot project file will be stored.
	 * 
	 * @return File
	 */
	protected File getSnapshotProjectFolder() {
		File projectFolder;
		
		if (snapshotProjectFolder != null) {
			projectFolder = snapshotProjectFolder;
			
		} else if (otmProject != null) {
			projectFolder = otmProject.getParentFile();
			
		} else {
			projectFolder = new File( javaProjectFolder, "/src/main/resources" );
		}
		return projectFolder;
	}
	
	/**
	 * Returns the name of the snapshot folder based on the name of the OTM project file.  The
	 * resulting filename does not include the ".otp" extension.
	 * 
	 * @return String
	 */
	private String getSnapshotFilename() {
		String sourceFilename;
		String snapshotFilename;
		
		if (otmProject != null) {
			sourceFilename = otmProject.getName();
		} else {
			sourceFilename = releaseItem.getFilename();
		}
		int extIdx = sourceFilename.lastIndexOf('.');
		
		if (extIdx < 0) {
			snapshotFilename = sourceFilename;
			
		} else {
			snapshotFilename = sourceFilename.substring( 0, extIdx );
		}
		snapshotFilename += "-snapshot";
		return snapshotFilename;
	}
	
	/**
	 * Returns true if the given library was originally saved in the OTM 1.6 file format.
	 * 
	 * @param libraryUrl  the URL location of the library to be analyzed
	 * @return boolean
	 */
	private boolean is16Library(URL libraryUrl) {
    	boolean is16Library = false;

        try {
            if ((libraryUrl != null) && !libraryUrl.getPath().toLowerCase().endsWith(".xsd")) {
                LibraryModuleLoader<InputStream> loader = new MultiVersionLibraryModuleLoader();
                LibraryModuleInfo<Object> moduleInfo = loader.loadLibrary(
                        new LibraryStreamInputSource(libraryUrl), new ValidationFindings());
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

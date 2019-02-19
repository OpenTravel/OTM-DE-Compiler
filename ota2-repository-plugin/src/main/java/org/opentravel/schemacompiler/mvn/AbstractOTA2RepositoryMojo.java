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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.opentravel.schemacompiler.repository.ServiceAssembly;
import org.opentravel.schemacompiler.repository.ServiceAssemblyManager;
import org.opentravel.schemacompiler.repository.impl.BuiltInProject;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.saver.impl.Library15FileSaveHandler;
import org.opentravel.schemacompiler.saver.impl.Library16FileSaveHandler;
import org.opentravel.schemacompiler.security.LibraryCrcCalculator;
import org.opentravel.schemacompiler.util.FileUtils;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Base plugin class that handles the creation of OTA2 repository snapshots. These snapshots allow development teams to
 * insulate themselves from daily changes to DRAFT items in the OTA2.0 repository by copying the contents of an OTM
 * project into a local directory. Teams may then manually invoke the snapshot plugin to force an update of the snapshot
 * when they are ready to incorporate the latest changes into their code.
 * 
 * <p>
 * It should be noted that these plugins are only intended for use with projects that are under active development.
 * Teams should be careful to ensure that this plugin is disabled once their work is complete to ensure that their local
 * codebase in synchronized with the contents of the OTA2.0 repository.
 */
public abstract class AbstractOTA2RepositoryMojo extends AbstractMojo {
	
	private static final String SNAPSHOT_SUFFIX = "#snapshot";
	
	private static final String DEFAULT_SNAPSHOT_SUFFIX = "-snapshot";
	private static final String PROVIDER_SNAPSHOT_SUFFIX = "-provider-snapshot";
	private static final String CONSUMER_SNAPSHOT_SUFFIX = "-consumer-snapshot";
	private static final String IMPL_SNAPSHOT_SUFFIX = "-impl-snapshot";
	
	/**
	 * The location of the OTM project file from which the repository snapshot is to be taken (required).
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
	 * The folder location where the snapshot project will be created. If not specified, this will be the same folder as
	 * the original '.otp' file, or <code>/src/main/resources</code> in the case of a managed OTM release.
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
	 * @throws MojoFailureException thrown if validation errors are detected in the OTM project
	 * @throws MojoExecutionException thrown if the snapshot update fails for any reason
	 */
	protected void createOrUpdateSnapshot() throws MojoFailureException, MojoExecutionException {
		ProjectManager projectManager = new ProjectManager( false );
		Map<String,TLModel> modelsBySnapshot = new HashMap<>();
		ReleaseManager releaseManager;
		Project originalProject = null;
		ServiceAssembly assembly = null;
		Log log = getLog();
		
		try {
			ValidationFindings findings = new ValidationFindings();
			
			releaseManager = new ReleaseManager();
			
			if (otmProject != null) {
				if (RepositoryItemType.RELEASE.isItemType( otmProject.getName() )) { // Load from an OTR file
					log.info( "Loading OTM release: " + otmProject.getName() );
					releaseManager.loadRelease( otmProject, findings );
					modelsBySnapshot.put( DEFAULT_SNAPSHOT_SUFFIX, releaseManager.getModel() );
					
				} else if (otmProject.getName().toLowerCase().endsWith( ".otp" )) { // Load from an OTP file
					log.info( "Loading OTM project: " + otmProject.getName() );
					originalProject = projectManager.loadProject( otmProject, findings );
					modelsBySnapshot.put( DEFAULT_SNAPSHOT_SUFFIX, projectManager.getModel() );
					
				} else if (otmProject.getName().toLowerCase().endsWith( ".osm" )) { // Load from an OSR file
					log.info( "Loading OTM service assembly: " + otmProject.getName() );
					ServiceAssemblyManager assemblyManager = new ServiceAssemblyManager();
					
					assembly = assemblyManager.loadAssembly( otmProject, findings );
					modelsBySnapshot.put( PROVIDER_SNAPSHOT_SUFFIX,
							assemblyManager.loadProviderModel( assembly, findings ) );
					modelsBySnapshot.put( CONSUMER_SNAPSHOT_SUFFIX,
							assemblyManager.loadConsumerModel( assembly, findings ) );
					modelsBySnapshot.put( IMPL_SNAPSHOT_SUFFIX,
							assemblyManager.loadImplementationModel( assembly, findings ) );
					
				} else {
					throw new MojoFailureException( "Unknown file type: " + otmProject.getName() );
				}
				
			} else { // Must be an OTM release
				log.info( "Loading OTM release: " + releaseItem.getFilename() );
				releaseManager.loadRelease( releaseItem, findings );
				modelsBySnapshot.put( DEFAULT_SNAPSHOT_SUFFIX, releaseManager.getModel() );
			}
			
			// Ensure no errors exist in the OTM project or release before proceeding
			validateNoErrors( findings );
			
		} catch (SchemaCompilerException e) {
			throw new MojoExecutionException( "Error building snapshot project.", e );
		}
		
		// Create a new snapshot project for each model that was loaded
		for (Entry<String,TLModel> entry : modelsBySnapshot.entrySet()) {
			String snapshotSuffix = entry.getKey();
			TLModel model = entry.getValue();
			File snapshotProjectFile = getSnapshotProjectFile( snapshotSuffix );
			File snapshotLibraryFolder = getSnapshotLibraryFolder( snapshotSuffix );
			
			saveSnapshotProject( snapshotProjectFile, snapshotLibraryFolder, originalProject,
					assembly, model, projectManager, releaseManager );
		}
	}

	/**
	 * Create the new project and migrate all OTM files to the snapshot folder.
	 * 
	 * @param snapshotProjectFile  the file where the snapshot project should be saved
	 * @param snapshotLibraryFolder  the folder where all of the snapshot libraries will be stored
	 * @param originalProject  the original project that contains the non-snapshot libraries (null if assembly is present)
	 * @param assembly  the assembly from which the original model was loaded (null if originalProject is present)
	 * @param model  the model that contains all of the libraries to include in the snapshot
	 * @param projectManager  the project manager instance
	 * @param releaseManager  the release manager instance (null if the source was not a release)
	 * @throws MojoExecutionException  thrown if an error occurs while creating the snapshot
	 */
	private void saveSnapshotProject(File snapshotProjectFile, File snapshotLibraryFolder, Project originalProject,
			ServiceAssembly assembly, TLModel model, ProjectManager projectManager, ReleaseManager releaseManager)
					throws MojoExecutionException {
		File backupProject = backup( snapshotProjectFile );
		File backupFolder = backup( snapshotLibraryFolder );
		Project snapshotProject = null;
		boolean success = false;
		Log log = getLog();
		
		try {
			log.info( "Building snapshot project: " + snapshotProjectFile.getName() );
			
			if (assembly != null) {
				snapshotProject = createSnapshotProject( assembly, projectManager, snapshotProjectFile,
						snapshotLibraryFolder );
				
			} else if (originalProject != null) {
				snapshotProject = createSnapshotProject( originalProject, snapshotProjectFile,
						snapshotLibraryFolder );
				
			} else { // Must be an OTM release
				snapshotProject = createSnapshotProject( releaseManager.getRelease(), projectManager,
						snapshotProjectFile, snapshotLibraryFolder );
			}
			
			List<File> snapshotLibraryFiles = new ArrayList<>();
			Map<String,Boolean> otm16Registry = new HashMap<>();
			Set<String> existingFilenames = new HashSet<>();
			
			for (AbstractLibrary library : model.getAllLibraries()) {
				if (library instanceof BuiltInLibrary) {
					continue;
				}
				String defaultFilename = ProjectManager.getPublicationFilename( library );
				String libraryFilename = getLibrarySnapshotFilename( defaultFilename, existingFilenames );
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
			saveSnapshotLibraries( snapshotProject, snapshotLibraryFiles,
					snapshotLibraryFolder, otm16Registry, projectManager, model );
			deleteProjectBackup( snapshotProjectFile );
			projectManager.closeAll();
			success = true;
			
		} catch (LibraryLoaderException | LibrarySaveException | RepositoryException e) {
			throw new MojoExecutionException( "Error building snapshot project.", e );
			
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
	 * Saves all libraries in the given model to the snapshot folder on the local file system.
	 * 
	 * @param snapshotProject  the snapshot project that will contain all of the snapshot libraries
	 * @param snapshotLibraryFiles  the list of all snapshot library files
	 * @param snapshotLibraryFolder  the folder where all of the snapshot libraries will be stored
	 * @param otm16Registry  registry that determines which files should be saved in 1.6 OTM format
	 * @param projectManager  the project manager instance
	 * @param model  the model that contains all libraries to be saved
	 * @throws LibrarySaveException  thrown if one of the libraries cannot be saved
	 * @throws LibraryLoaderException  thrown if one of the libraries cannot be added to the model
	 * @throws RepositoryException  thrown if the remote repository cannot be accessed
	 */
	private void saveSnapshotLibraries(Project snapshotProject, List<File> snapshotLibraryFiles,
			File snapshotLibraryFolder, Map<String,Boolean> otm16Registry, ProjectManager projectManager, TLModel model)
			throws LibrarySaveException, LibraryLoaderException, RepositoryException {
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
				recalulateCrc( URLUtils.toFile( library.getLibraryUrl() ) );
			}
		}
		
		// Delete any .bak files that might exist in the snapshot folder
		for (File ssFile : snapshotLibraryFolder.listFiles()) {
			if (ssFile.getName().endsWith( ".bak" )) {
				FileUtils.delete( ssFile );
			}
		}
		
		// Close any projects that are not our snapshot that is being created
		for (Project project : projectManager.getAllProjects()) {
			if ((project != snapshotProject) && !(project instanceof BuiltInProject)) {
				projectManager.closeProject( project );
			}
		}
		
		// Populate the snapshot project with the newly-saved libraries in the snapshot folder
		projectManager.addUnmanagedProjectItems( snapshotLibraryFiles, snapshotProject );
		projectManager.saveProject( snapshotProject, false, null );
	}

	/**
	 * Logs any errors/warnings and throws an exception if any error findings are present.
	 * 
	 * @param findings  the validation findings to check
	 * @throws MojoFailureException  thrown if one or more error findings are present
	 */
	private void validateNoErrors(ValidationFindings findings) throws MojoFailureException {
		if (findings.hasFinding()) {
			String[] messages = findings.getAllValidationMessages( FindingMessageFormat.IDENTIFIED_FORMAT );
			
			getLog().info( "Errors/warnings detected in project:" );
			for (String message : messages) {
				getLog().info( message );
			}
		}
		if (findings.hasFinding( FindingType.ERROR )) {
			throw new MojoFailureException(
					"Unable to create repository snapshot because the project contains errors." );
		}
	}

	/**
	 * Forces a recalculation of the library's CRC values and re-saves the file.
	 * 
     * @param libraryFile  the library file to be updated with a new CRC value
     * @throws LibraryLoaderException  thrown if an unexpected exception occurs while
     *								  attempting to load the contents of the library
     * @throws LibrarySaveException  thrown if the content of the library cannot be re-saved
	 */
	private void recalulateCrc(File libraryFile) throws LibraryLoaderException, LibrarySaveException {
		try {
			LibraryCrcCalculator.recalculateLibraryCrc( libraryFile );
			
		} catch (IOException e) {
			getLog().warn( "Error recalculating library CRC.", e );
		}
	}
	
	/**
	 * Creates the snapshot project file and the snapshot folder where the OTM files will be stored.
	 * 
	 * @param originalProject the original OTM project
	 * @param snapshotProjectFile the file location of the repository snapshot project
	 * @param snapshotFolder the snapshot folder where the project's OTM files will be stored
	 * @return Project
	 * @throws LibrarySaveException thrown if the new snapshot project cannot be created or saved
	 */
	protected Project createSnapshotProject(Project originalProject, File snapshotProjectFile, File snapshotFolder)
			throws LibrarySaveException {
		ProjectManager projectManager = originalProject.getProjectManager();
		Project snapshotProject = projectManager.newProject( snapshotProjectFile,
				originalProject.getProjectId() + SNAPSHOT_SUFFIX, originalProject.getName(),
				originalProject.getDescription() );
		
		snapshotFolder.mkdirs();
		return snapshotProject;
	}
	
	/**
	 * Creates the snapshot project file and the snapshot folder where the OTM files will be stored.
	 * 
	 * @param release the OTM release from which to create the snapshot project
	 * @param projectManager the project manager that should be used to create the new project
	 * @param snapshotProjectFile the file location of the repository snapshot project
	 * @param snapshotFolder the snapshot folder where the project's OTM files will be stored
	 * @return Project
	 * @throws LibrarySaveException thrown if the new snapshot project cannot be created or saved
	 */
	protected Project createSnapshotProject(org.opentravel.schemacompiler.repository.Release release,
			ProjectManager projectManager, File snapshotProjectFile, File snapshotFolder) throws LibrarySaveException {
		Project snapshotProject = projectManager.newProject( snapshotProjectFile,
				release.getBaseNamespace() + SNAPSHOT_SUFFIX, release.getName(), release.getDescription() );
		
		snapshotFolder.mkdirs();
		return snapshotProject;
	}
	
	/**
	 * Creates the snapshot project file and the snapshot folder where the OTM files will be stored.
	 * 
	 * @param release the OTM release from which to create the snapshot project
	 * @param projectManager the project manager that should be used to create the new project
	 * @param snapshotProjectFile the file location of the repository snapshot project
	 * @param snapshotFolder the snapshot folder where the project's OTM files will be stored
	 * @return Project
	 * @throws LibrarySaveException thrown if the new snapshot project cannot be created or saved
	 */
	protected Project createSnapshotProject(ServiceAssembly assembly, ProjectManager projectManager,
			File snapshotProjectFile, File snapshotFolder) throws LibrarySaveException {
		Project snapshotProject = projectManager.newProject( snapshotProjectFile,
				assembly.getBaseNamespace() + SNAPSHOT_SUFFIX, assembly.getName(), null );
		
		snapshotFolder.mkdirs();
		return snapshotProject;
	}
	
	/**
	 * Returns true if the snapshot files and folders have already been initialized.
	 * 
	 * @return boolean
	 */
	protected boolean snapshotInitialized() {
		List<String> snapshotSuffixes = new ArrayList<>();
		boolean snapshotInitialized = true;
		
		if ((otmProject != null) && (otmProject.getName().endsWith( ".osm" ))) {
			snapshotSuffixes
				.addAll( Arrays.asList( PROVIDER_SNAPSHOT_SUFFIX, CONSUMER_SNAPSHOT_SUFFIX, IMPL_SNAPSHOT_SUFFIX ) );
		} else {
			snapshotSuffixes.add( DEFAULT_SNAPSHOT_SUFFIX );
		}
		
		for (String suffix : snapshotSuffixes) {
			File snapshotProjectFile = getSnapshotProjectFile( suffix );
			File snapshotLibraryFolder = getSnapshotLibraryFolder( suffix );
			
			snapshotInitialized &= snapshotProjectFile.exists();
			snapshotInitialized &= snapshotLibraryFolder.exists();
		}
		return snapshotInitialized;
	}
	
	/**
	 * Creates a backup of the given file or folder by renaming it. The file handle returned is for the renamed backup
	 * folder. If the file or folder cannot be backed up (i.e. renamed), this method will return null.
	 * 
	 * @param fileOrFolder the file or folder to rename as a backup
	 * @return File
	 * @throws MojoExecutionException thrown if the backup cannot be created because of an error
	 */
	protected File backup(File fileOrFolder) throws MojoExecutionException {
		File backupFileOrFolder = null;
		
		if ((fileOrFolder != null) && fileOrFolder.exists()) {
			String suffix = "";
			int count = 0;
			
			// Continue changing the suffix until we find a backup name that does not yet exist
			while ((backupFileOrFolder = new File( fileOrFolder.getParentFile(),
					fileOrFolder.getName() + ".bak" + suffix )).exists()) {
				suffix = (++count) + "";
			}
			
			if (!fileOrFolder.renameTo( backupFileOrFolder )) {
				throw new MojoExecutionException(
						"Unable to create backup of existing snapshot item: " + fileOrFolder.getName() );
			}
		}
		return backupFileOrFolder;
	}
	
	/**
	 * Restores the original contents of the file or folder by deleting the 'original' and renaming the 'backup' to
	 * replace it.
	 * 
	 * @param backup the backup file or folder to be restored
	 * @param original the original file or folder name to be restored
	 */
	protected void restoreBackup(File backup, File original) {
		if (backup != null) {
			FileUtils.delete( original );
			FileUtils.renameTo( backup, original );
		}
	}
	
	/**
	 * Deletes the specified file or folder (and all of its contents).
	 * 
	 * @param folder the folder to be deleted
	 */
	protected void delete(File fileOrFolder) {
		if ((fileOrFolder != null) && fileOrFolder.exists()) {
			if (fileOrFolder.isDirectory()) {
				for (File folderItem : fileOrFolder.listFiles()) {
					delete( folderItem );
				}
			}
			FileUtils.delete( fileOrFolder );
		}
	}
	
	/**
	 * If a complier-generated backup of the given project file exists, it will be deleted by this method.
	 * 
	 * @param projectFile the project file whose compiler backup will be deleted
	 */
	private void deleteProjectBackup(File projectFile) {
		String backupFilename = projectFile.getName();
		int extIdx = backupFilename.lastIndexOf( '.' );
		File backupFile;
		
		backupFilename = ((extIdx < 0) ? backupFilename : backupFilename.substring( 0, extIdx )) + ".bak";
		backupFile = new File( projectFile.getParentFile(), backupFilename );
		FileUtils.delete( backupFile );
	}
	
	/**
	 * Validates the configuration settings of the plugin and returns true if everything is valid. Otherwise returns
	 * false or throws a <code>MojoFailureException</code>, depending on the error that was discovered.
	 * 
	 * @return boolean
	 */
	protected boolean validate() throws MojoFailureException {
		boolean isValid = true;
		Log log = getLog();
		
		if (!enabled) {
			log.info( "OTA2 repository snapshot processing disabled (skipping)." );
			isValid = false;
		}
		if (otmProject != null) {
			if (!otmProject.exists()) {
				throw new MojoFailureException( "Source file not found: " + otmProject.getAbsolutePath() );
			}
			
		} else if (release != null) {
			try {
				releaseItem = RepositoryManager.getDefault().getRepositoryItem( release.getBaseNamespace(),
						release.getFilename(), release.getVersion() );
				
				if (!RepositoryItemType.RELEASE.isItemType( releaseItem.getFilename() )) {
					throw new MojoFailureException(
							"The specified repository item is not an OTM release: " + releaseItem.getFilename() );
				}
				
			} catch (RepositoryException e) {
				throw new MojoFailureException(
						"The specified repository item does not exist or is not an OTM release.", e );
				
			} catch (Exception e) {
				throw new MojoFailureException( "Unknown error while accessing the OTM repository", e );
			}
			
		} else {
			throw new MojoFailureException( "Either a libraryFile or a release must be specified." );
		}
		
		return isValid;
	}
	
	/**
	 * Returns the file name and location of the repository snapshot project file.
	 * 
	 * @param snapshotSuffix the naming suffix to use for the snapshot project folder
	 * @return File
	 */
	protected File getSnapshotProjectFile(String snapshotSuffix) {
		return new File( getSnapshotProjectFolder(), getSnapshotFilename( snapshotSuffix ) + ".otp" );
	}
	
	/**
	 * Returns the file name and location of the folder that will contain the OTM files for the repository snapshot.
	 * 
	 * @param snapshotSuffix the naming suffix to use for the snapshot project folder
	 * @return File
	 */
	protected File getSnapshotLibraryFolder(String snapshotSuffix) {
		return new File( getSnapshotProjectFolder(), getSnapshotFilename( snapshotSuffix ) );
	}
	
	/**
	 * Returns the folder location where the snapshot project file will be stored.
	 * 
	 * @return File
	 */
	@SuppressWarnings("squid:S1075") // suppress warning for local project URI creation (support for testability)
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
	 * Returns the name of the snapshot folder based on the name of the OTM project file. The resulting filename does
	 * not include the ".otp" extension.
	 * 
	 * @param snapshotSuffix the naming suffix to use for the snapshot project folder
	 * @return String
	 */
	private String getSnapshotFilename(String snapshotSuffix) {
		String sourceFilename;
		String snapshotFilename;
		
		if (otmProject != null) {
			sourceFilename = otmProject.getName();
		} else {
			sourceFilename = releaseItem.getFilename();
		}
		int extIdx = sourceFilename.lastIndexOf( '.' );
		
		if (extIdx < 0) {
			snapshotFilename = sourceFilename;
			
		} else {
			snapshotFilename = sourceFilename.substring( 0, extIdx );
		}
		snapshotFilename += snapshotSuffix;
		return snapshotFilename;
	}
	
	/**
	 * Returns a unique snapshot filename for the library with the given default filename.
	 * 
	 * @param defaultFilename the library's default filename
	 * @param existingFilenames the set of existing filenames already created
	 * @return String
	 * @throws LibrarySaveException thrown if the library snapshot filename cannot be calculated
	 */
	private String getLibrarySnapshotFilename(String defaultFilename, Set<String> existingFilenames)
			throws LibrarySaveException {
		String snapshotFilename = null;
		
		if (!existingFilenames.contains( defaultFilename )) {
			existingFilenames.add( defaultFilename );
			snapshotFilename = defaultFilename;
			
		} else {
			for (char suffix = 'a'; suffix <= 'z'; suffix++) {
				snapshotFilename = defaultFilename.replace( ".otm", "_" + suffix + ".otm" );
				
				if (!existingFilenames.contains( snapshotFilename )) {
					existingFilenames.add( snapshotFilename );
					break;
				}
			}
		}
		
		if (snapshotFilename == null) {
			throw new LibrarySaveException( "Too many libraries of the same name included in the snapshot." );
		}
		return snapshotFilename;
	}
	
	/**
	 * Returns true if the given library was originally saved in the OTM 1.6 file format.
	 * 
	 * @param libraryUrl the URL location of the library to be analyzed
	 * @return boolean
	 */
	private boolean is16Library(URL libraryUrl) {
		boolean is16Library = false;
		
		try {
			if ((libraryUrl != null) && !libraryUrl.getPath().toLowerCase().endsWith( ".xsd" )) {
				LibraryModuleLoader<InputStream> loader = new MultiVersionLibraryModuleLoader();
				LibraryModuleInfo<Object> moduleInfo = loader.loadLibrary( new LibraryStreamInputSource( libraryUrl ),
						new ValidationFindings() );
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
	 * Since this is a read-only application, enable the OTM 1.6 file format for all operations.
	 */
	static {
		OTM16Upgrade.otm16Enabled = true;
	}
	
}

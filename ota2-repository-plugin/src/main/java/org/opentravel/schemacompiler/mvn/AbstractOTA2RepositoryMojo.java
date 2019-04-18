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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.opentravel.ns.ota2.project_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.project_v01_00.ProjectType;
import org.opentravel.ns.ota2.project_v01_00.UnmanagedProjectItemType;
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
import org.opentravel.schemacompiler.repository.impl.ProjectFileUtils;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.saver.impl.Library15FileSaveHandler;
import org.opentravel.schemacompiler.saver.impl.Library16FileSaveHandler;
import org.opentravel.schemacompiler.security.LibraryCrcCalculator;
import org.opentravel.schemacompiler.task.AssemblyModelType;
import org.opentravel.schemacompiler.util.FileUtils;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

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

    private static final String SNAPSHOT_PROJECTID_SUFFIX = "#snapshot";

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

    /**
     * The repository information for the OTM service assembly to be compiled.
     */
    @Parameter
    protected Assembly assembly;

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

    private RepositoryItem repositoryItem;
    private AssemblyModelType modelType;
    private RepositoryManager repositoryManager;
    private RepositoryManager alternateRepositoryManager;

    /**
     * Default constructor.
     */
    public AbstractOTA2RepositoryMojo() {
        this( null );
    }

    /**
     * Constructor that specifies an alternate repository manager from the default.
     * 
     * @param rm the repository manager to use when executing the mojo
     */
    public AbstractOTA2RepositoryMojo(RepositoryManager rm) {
        this.alternateRepositoryManager = rm;
    }

    /**
     * Creates or updates the OTA2 repository snapshot.
     * 
     * @throws MojoFailureException thrown if validation errors are detected in the OTM project
     * @throws MojoExecutionException thrown if the snapshot update fails for any reason
     */
    protected void createOrUpdateSnapshot() throws MojoFailureException, MojoExecutionException {
        Map<String,TLModel> modelsBySnapshot = new HashMap<>();
        String ssBaseNamespace;
        String ssProjectName;
        Log log = getLog();

        try {
            ValidationFindings findings = new ValidationFindings();

            initRepositoryManager();

            if (otmProject != null) {
                if (RepositoryItemType.RELEASE.isItemType( otmProject.getName() )) { // Load from an OTR file
                    log.info( "Loading OTM release: " + otmProject.getName() );
                    ReleaseManager releaseManager = new ReleaseManager( repositoryManager );

                    releaseManager.loadRelease( otmProject, findings );
                    modelsBySnapshot.put( DEFAULT_SNAPSHOT_SUFFIX, releaseManager.getModel() );
                    ssBaseNamespace = releaseManager.getRelease().getBaseNamespace();
                    ssProjectName = releaseManager.getRelease().getName();

                } else if (otmProject.getName().toLowerCase().endsWith( ".otp" )) { // Load from an OTP file
                    log.info( "Loading OTM project: " + otmProject.getName() );
                    ProjectManager projectManager = new ProjectManager( new TLModel(), false, repositoryManager );
                    Project originalProject = projectManager.loadProject( otmProject, findings );

                    modelsBySnapshot.put( DEFAULT_SNAPSHOT_SUFFIX, projectManager.getModel() );
                    ssBaseNamespace = originalProject.getProjectId();
                    ssProjectName = originalProject.getName();
                    projectManager.closeAll();

                } else if (otmProject.getName().toLowerCase().endsWith( ".osm" )) { // Load from an OSR file
                    log.info( "Loading OTM service assembly: " + otmProject.getName() );
                    ServiceAssemblyManager assemblyManager = new ServiceAssemblyManager( repositoryManager );
                    ServiceAssembly svcAssembly = assemblyManager.loadAssembly( otmProject, findings );

                    loadAssemblyModels( svcAssembly, assemblyManager, modelsBySnapshot, findings );
                    ssBaseNamespace = svcAssembly.getBaseNamespace();
                    ssProjectName = svcAssembly.getName();

                } else {
                    throw new MojoFailureException( "Unknown file type: " + otmProject.getName() );
                }

            } else {
                if (release != null) {
                    log.info( "Loading OTM release: " + repositoryItem.getFilename() );
                    ReleaseManager releaseManager = new ReleaseManager( repositoryManager );

                    releaseManager.loadRelease( repositoryItem, findings );
                    modelsBySnapshot.put( DEFAULT_SNAPSHOT_SUFFIX, releaseManager.getModel() );

                } else if (assembly != null) {
                    ServiceAssemblyManager assemblyManager = new ServiceAssemblyManager( repositoryManager );
                    ServiceAssembly svcAssembly = assemblyManager.loadAssembly( repositoryItem, findings );

                    loadAssemblyModels( svcAssembly, assemblyManager, modelsBySnapshot, findings );
                }
                ssBaseNamespace = repositoryItem.getBaseNamespace();
                ssProjectName = repositoryItem.getLibraryName();
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

            saveSnapshotProject( snapshotProjectFile, snapshotLibraryFolder, ssBaseNamespace, ssProjectName, model );
        }
    }

    /**
     * Loads the model(s) required for the service assembly. If the 'modelType' field of the assembly specified by the
     * user was null, all three models will be loaded.
     * 
     * @param assembly the assembly from which to load the required model(s)
     * @param assemblyManager the assembly manager to use when loading the models
     * @param modelsBySnapshot the map of models for which snapshots will be created
     * @param findings errors/warnings detected during the load
     * @throws SchemaCompilerException thrown if an error occurs while loading the model(s)
     */
    private void loadAssemblyModels(ServiceAssembly assembly, ServiceAssemblyManager assemblyManager,
        Map<String,TLModel> modelsBySnapshot, ValidationFindings findings) throws SchemaCompilerException {

        if ((modelType == null) || (modelType == AssemblyModelType.PROVIDER)) {
            modelsBySnapshot.put( PROVIDER_SNAPSHOT_SUFFIX, assemblyManager.loadProviderModel( assembly, findings ) );
        }
        if ((modelType == null) || (modelType == AssemblyModelType.CONSUMER)) {
            modelsBySnapshot.put( CONSUMER_SNAPSHOT_SUFFIX, assemblyManager.loadConsumerModel( assembly, findings ) );
        }
        if ((modelType == null) || (modelType == AssemblyModelType.IMPLEMENTATION)) {
            modelsBySnapshot.put( IMPL_SNAPSHOT_SUFFIX, assemblyManager.loadImplementationModel( assembly, findings ) );
        }
    }

    /**
     * Create the new project and migrate all OTM files to the snapshot folder.
     * 
     * @param snapshotProjectFile the file where the snapshot project should be saved
     * @param snapshotLibraryFolder the folder where all of the snapshot libraries will be stored
     * @param ssBaseNamespace the base namespace to use as the ID of the snapshot project
     * @param ssProjectName the name of the snapshot project to be created
     * @param model the model that contains all of the libraries to include in the snapshot
     * @throws MojoExecutionException thrown if an error occurs while creating the snapshot
     */
    private void saveSnapshotProject(File snapshotProjectFile, File snapshotLibraryFolder, String ssBaseNamespace,
        String ssProjectName, TLModel model) throws MojoExecutionException {
        File backupProject = backup( snapshotProjectFile );
        File backupFolder = backup( snapshotLibraryFolder );
        ProjectType snapshotProject = new ProjectType();
        boolean success = false;
        Log log = getLog();

        try {
            log.info( "Building snapshot project: " + snapshotProjectFile.getName() );

            snapshotProject.setProjectId( ssBaseNamespace + SNAPSHOT_PROJECTID_SUFFIX );
            snapshotProject.setName( ssProjectName );

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
            saveSnapshotLibraries( snapshotLibraryFolder, otm16Registry, model );
            deleteProjectBackup( snapshotProjectFile );
            success = true;

            // Create the snapshot project file
            for (File libraryFile : snapshotLibraryFiles) {
                UnmanagedProjectItemType projectItem = new UnmanagedProjectItemType();

                projectItem.setFileLocation( snapshotLibraryFolder.getName() + "/" + libraryFile.getName() );
                snapshotProject.getProjectItemBase()
                    .add( new ObjectFactory().createUnmanagedProjectItem( projectItem ) );
            }
            new ProjectFileUtils().saveProjectFile( snapshotProject, snapshotProjectFile );

        } catch (LibraryLoaderException | LibrarySaveException e) {
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
     * @param snapshotLibraryFolder the folder where all of the snapshot libraries will be stored
     * @param otm16Registry registry that determines which files should be saved in 1.6 OTM format
     * @param model the model that contains all libraries to be saved
     * @throws LibrarySaveException thrown if one of the libraries cannot be saved
     * @throws LibraryLoaderException thrown if one of the libraries cannot be added to the model
     */
    private void saveSnapshotLibraries(File snapshotLibraryFolder, Map<String,Boolean> otm16Registry, TLModel model)
        throws LibrarySaveException, LibraryLoaderException {
        LibraryModelSaver modelSaver = new LibraryModelSaver();

        snapshotLibraryFolder.mkdirs();

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
    }

    /**
     * Logs any errors/warnings and throws an exception if any error findings are present.
     * 
     * @param findings the validation findings to check
     * @throws MojoFailureException thrown if one or more error findings are present
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
     * @param libraryFile the library file to be updated with a new CRC value
     * @throws LibraryLoaderException thrown if an unexpected exception occurs while attempting to load the contents of
     *         the library
     * @throws LibrarySaveException thrown if the content of the library cannot be re-saved
     */
    private void recalulateCrc(File libraryFile) throws LibraryLoaderException, LibrarySaveException {
        try {
            LibraryCrcCalculator.recalculateLibraryCrc( libraryFile );

        } catch (IOException e) {
            getLog().warn( "Error recalculating library CRC.", e );
        }
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
            while ((backupFileOrFolder =
                new File( fileOrFolder.getParentFile(), fileOrFolder.getName() + ".bak" + suffix )).exists()) {
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
     * @param fileOrFolder the file or folder to be deleted
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
     * @throws MojoFailureException thrown if a validation error in the plugin configuration is detected
     * @throws MojoExecutionException thrown if an error occurs during initialization of the plugin
     */
    protected boolean validate() throws MojoFailureException, MojoExecutionException {
        boolean isValid = true;
        Log log = getLog();

        initRepositoryManager();

        if (!enabled) {
            log.info( "OTA2 repository snapshot processing disabled (skipping)." );
            isValid = false;
        }
        if (otmProject != null) {
            if (!otmProject.exists()) {
                throw new MojoFailureException( "Source file not found: " + otmProject.getAbsolutePath() );
            }

        } else if (release != null) {
            loadAndValidateRepositoryItem( release.getBaseNamespace(), release.getFilename(), release.getVersion(),
                RepositoryItemType.RELEASE );

        } else if (assembly != null) {
            modelType = AssemblyModelType.fromIdentifier( assembly.getModelType() );
            loadAndValidateRepositoryItem( assembly.getBaseNamespace(), assembly.getFilename(), assembly.getVersion(),
                RepositoryItemType.ASSEMBLY );

        } else {
            throw new MojoFailureException( "Either a libraryFile or a release must be specified." );
        }

        return isValid;
    }

    /**
     * Loads the specified repository item for this mojo and verifies that it is of the expected file type.
     * 
     * @param baseNamespace the base namespace of the repository item to load
     * @param filename the base namespace of the repository item to load
     * @param version the base namespace of the repository item to load
     * @param expectedItemType the expected type of the repository item
     * @throws MojoFailureException thrown if the repository item does not exist or it is not of the expected type
     */
    private void loadAndValidateRepositoryItem(String baseNamespace, String filename, String version,
        RepositoryItemType expectedItemType) throws MojoFailureException {
        try {
            repositoryItem = repositoryManager.getRepositoryItem( baseNamespace, filename, version );

            if (!expectedItemType.isItemType( filename )) {
                throw new MojoFailureException( String.format( "The specified repository item is not an OTM %s: %s",
                    expectedItemType.toString().toLowerCase(), filename ) );
            }

        } catch (RepositoryException e) {
            throw new MojoFailureException( "The specified repository item does not exist or is not an OTM release.",
                e );

        } catch (Exception e) {
            throw new MojoFailureException( "Unknown error while accessing the OTM repository", e );
        }
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
        String sourceFilename = "";
        String snapshotFilename;

        if (otmProject != null) {
            sourceFilename = otmProject.getName();

        } else if (release != null) {
            sourceFilename = release.getFilename();

        } else if (assembly != null) {
            sourceFilename = assembly.getFilename();
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
                LibraryModuleInfo<Object> moduleInfo =
                    loader.loadLibrary( new LibraryStreamInputSource( libraryUrl ), new ValidationFindings() );
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
     * Initializes the repository manager to be used by this mojo. If an alternate manager has not been provided by the
     * constructor, the default instance will be used.
     * 
     * @throws MojoExecutionException thrown if the default instance cannot be initialized
     */
    private void initRepositoryManager() throws MojoExecutionException {
        if (this.repositoryManager == null) {
            if (alternateRepositoryManager == null) {
                try {
                    this.repositoryManager = RepositoryManager.getDefault();

                } catch (RepositoryException e) {
                    throw new MojoExecutionException( "Error initializing the default repository manager.", e );
                }

            } else {
                this.repositoryManager = alternateRepositoryManager;
            }
        }
    }

    /**
     * Since this is a read-only application, enable the OTM 1.6 file format for all operations.
     */
    static {
        OTM16Upgrade.otm16Enabled = true;
    }

}

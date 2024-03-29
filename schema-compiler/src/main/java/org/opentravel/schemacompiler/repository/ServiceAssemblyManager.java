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

package org.opentravel.schemacompiler.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.repository.impl.MultiReleaseModuleLoader;
import org.opentravel.schemacompiler.repository.impl.ServiceAssemblyFileUtils;
import org.opentravel.schemacompiler.repository.impl.ServiceAssemblyItemImpl;
import org.opentravel.schemacompiler.transform.util.ModelReferenceResolver;
import org.opentravel.schemacompiler.util.FileUtils;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.ValidatorFactory;
import org.opentravel.schemacompiler.validate.assembly.AssemblyValidationContext;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemacompiler.visitor.DependencyNavigator;
import org.opentravel.schemacompiler.visitor.ModelElementVisitor;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * Manager responsible for loading, saving, and validating service assemblies.
 */
public class ServiceAssemblyManager {

    private static final Logger log = LogManager.getLogger( ServiceAssemblyManager.class );

    private RepositoryManager repositoryManager;
    private ServiceAssemblyFileUtils fileUtils;

    /**
     * Default constructor.
     * 
     * @throws RepositoryException throw if the default repository manager cannot be initialized
     */
    public ServiceAssemblyManager() throws RepositoryException {
        this( RepositoryManager.getDefault() );
    }

    /**
     * Constructor that provides the repository manager instance to use when accessing remote content.
     * 
     * @param repositoryManager the repository manager instance
     */
    public ServiceAssemblyManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
        this.fileUtils = new ServiceAssemblyFileUtils( repositoryManager );
    }

    /**
     * Creates a new (empty) service assembly using the information provided.
     * 
     * <p>
     * NOTE: The assembly returned from this method does have its 'assemblyUrl' field configured using the
     * 'assemblyFile' parameter, but the assembly itself is not saved to the file system by this method.
     * 
     * @param baseNS the base namespace of the assembly
     * @param name the name of the assembly
     * @param version the version of the assembly
     * @param assemblyFile the file location where the assembly file will be stored
     * @return ServiceAssembly
     * @throws SchemaCompilerException thrown if any of the information provided is invalid
     * @see #saveAssembly(ServiceAssembly)
     */
    public ServiceAssembly newAssembly(String baseNS, String name, String version, File assemblyFile)
        throws SchemaCompilerException {
        ServiceAssembly assembly = new ServiceAssembly();

        if (assemblyFile != null) {
            assembly.setAssemblyUrl( URLUtils.toURL( assemblyFile ) );
        }
        assembly.setBaseNamespace( baseNS );
        assembly.setName( name );
        assembly.setVersion( version );
        return assembly;
    }

    /**
     * Loads a service assembly from the specified file.
     * 
     * @param assemblyFile the assembly file to load
     * @param findings the validation findings where errors and warning should be reported
     * @return ServiceAssembly
     * @throws LibraryLoaderException thrown if an error occurs while loading the file
     */
    public ServiceAssembly loadAssembly(File assemblyFile, ValidationFindings findings) throws LibraryLoaderException {
        return fileUtils.loadAssemblyFile( assemblyFile, findings );
    }

    /**
     * Loads a service assembly from a remote OTM repository.
     * 
     * @param assemblyItem the repository item for the assembly to load
     * @param findings the validation findings where errors and warning should be reported
     * @return ServiceAssembly
     * @throws RepositoryException thrown if an error occurs while accessing the remote repository
     * @throws LibraryLoaderException thrown if an error occurs while loading the file
     */
    public ServiceAssembly loadAssembly(RepositoryItem assemblyItem, ValidationFindings findings)
        throws RepositoryException, LibraryLoaderException {
        Repository repository = assemblyItem.getRepository();
        File assemblyFile;

        if (repository instanceof RemoteRepository) {
            ((RemoteRepository) repository).downloadContent( assemblyItem, true );
        }
        assemblyFile = URLUtils.toFile( repositoryManager.getContentLocation( assemblyItem ) );
        return loadAssembly( assemblyFile, findings );
    }

    /**
     * Saves the given assembly. The file location is specified by its 'assemblyUrl' field; non-file URL's will result
     * in an error.
     * 
     * @param assembly the service assembly to be saved
     * @throws SchemaCompilerException thrown if an error occurs while saving the file
     */
    public void saveAssembly(ServiceAssembly assembly) throws SchemaCompilerException {
        fileUtils.saveAssemblyFile( assembly, true );
    }

    /**
     * Validates the contents of the given assembly. The scope of this validation does not include the contents of the
     * libraries in the various releases. Instead, it primarily looks for inconsistencies in commit dates when different
     * BETA releases reference the same library file.
     * 
     * @param assembly the service assembly to be validated
     * @return ValidationFindings
     */
    public ValidationFindings validateAssembly(ServiceAssembly assembly) {
        ValidatorFactory factory = ValidatorFactory.getInstance( ValidatorFactory.ASSEMBLY_RULE_SET_ID,
            new AssemblyValidationContext( assembly, repositoryManager ) );

        return factory.getValidatorForTarget( assembly ).validate( assembly );
    }

    /**
     * Loads the provider model for the given assembly which only includes the provider API releases in the resulting
     * model. If the given assembly contains errors, this method will return null and report the errors in the
     * validation findings provided.
     * 
     * @param assembly the service assembly for which to load the provider model
     * @param findings the validation findings that will be used to collect any errors or warnings that are detected
     *        during the load
     * @return TLModel
     * @throws SchemaCompilerException thrown if an error occurs while loading the model
     */
    public TLModel loadProviderModel(ServiceAssembly assembly, ValidationFindings findings)
        throws SchemaCompilerException {
        TLModel model = null;

        findings.addAll( validateAssembly( assembly ) );

        if (!findings.hasFinding( FindingType.ERROR )) {
            model = loadModel( assembly.getProviderApis() );
        }
        return model;
    }

    /**
     * Loads the consumer model for the given assembly which only includes the consumer API releases in the resulting
     * model. If the given assembly contains errors, this method will return null and report the errors in the
     * validation findings provided.
     * 
     * @param assembly the service assembly for which to load the consumer model
     * @param findings the validation findings that will be used to collect any errors or warnings that are detected
     *        during the load
     * @return TLModel
     * @throws SchemaCompilerException thrown if an error occurs while loading the model
     */
    public TLModel loadConsumerModel(ServiceAssembly assembly, ValidationFindings findings)
        throws SchemaCompilerException {
        TLModel model = null;

        findings.addAll( validateAssembly( assembly ) );

        if (!findings.hasFinding( FindingType.ERROR )) {
            model = loadModel( assembly.getConsumerApis() );
        }
        return model;
    }

    /**
     * Loads the implementation model for the given assembly which only includes all API releases (provider and
     * consumer) in the resulting model. If the given assembly contains errors, this method will return null and report
     * the errors in the validation findings provided.
     * 
     * @param assembly the service assembly for which to load the consumer model
     * @param findings the validation findings that will be used to collect any errors or warnings that are detected
     *        during the load
     * @return TLModel
     * @throws SchemaCompilerException thrown if an error occurs while loading the model
     */
    public TLModel loadImplementationModel(ServiceAssembly assembly, ValidationFindings findings)
        throws SchemaCompilerException {
        TLModel model = null;

        findings.addAll( validateAssembly( assembly ) );

        if (!findings.hasFinding( FindingType.ERROR )) {
            model = loadModel( assembly.getAllApis() );
        }
        return model;
    }

    /**
     * Publishes the given service assembly to a remote repository and returns the repository item that can be used to
     * access it.
     * 
     * @param assembly the assembly to be published
     * @param repository the repository to which the assembly will be published
     * @return ServiceAssemblyItem
     * @throws RepositoryException thrown if an error occurs while accessing the remote repository
     */
    public ServiceAssemblyItem publishAssembly(ServiceAssembly assembly, Repository repository)
        throws RepositoryException {
        URL originalUrl = assembly.getAssemblyUrl();
        File assemblyFile = URLUtils.isFileURL( originalUrl ) ? URLUtils.toFile( originalUrl ) : null;

        if (assemblyFile == null) {
            throw new RepositoryException(
                "Cannot publish a release that is not accessible from from the local file system." );

        } else if (fileUtils.isRepositoryFile( assemblyFile )) {
            throw new IllegalStateException( "The service assembly is already managed by a remote repository." );
        }

        // Reload the release model just to be sure it is in-sync and contains no errors
        try {
            ValidationFindings findings = validateAssembly( assembly );

            if (findings.hasFinding( FindingType.ERROR )) {
                throw new RepositoryException(
                    "Unable to publish the service assembly because it contains validation errors: "
                        + assemblyFile.getName() );
            }

        } catch (SchemaCompilerException e) {
            throw new RepositoryException( "Error validating service assembly model: " + assemblyFile.getName(), e );
        }

        // Backup the file locally before publishing
        try {
            fileUtils.createBackupFile( assemblyFile );

        } catch (IOException e) {
            throw new RepositoryException( "Error creating service assembly backup file: " + assemblyFile.getName(),
                e );
        }

        // Publish the assembly and delete the local copy of the file (backup will remain)
        RepositoryItem repoItem;

        try (InputStream contentStream = new FileInputStream( assemblyFile )) {
            repoItem = repository.publish( contentStream, fileUtils.getAssemblyFilename( assembly ), assembly.getName(),
                assembly.getNamespace(), assembly.getVersion(),
                VersionSchemeFactory.getInstance().getDefaultVersionScheme(), TLLibraryStatus.FINAL );
            assembly.setAssemblyUrl( repositoryManager.getContentLocation( repoItem ) );

        } catch (IOException e) {
            throw new RepositoryException( "Unable to read from service assembly data file: " + assemblyFile.getName(),
                e );
        }
        FileUtils.delete( assemblyFile );
        return new ServiceAssemblyItemImpl( assembly, repoItem );
    }

    /**
     * Deletes a service assembly from a remote repository and saves its content on the local file system. If a null
     * folder location is passed to this method, the assembly will be deleted from the repository without saving it to
     * the local file system. If the assembly is saved, its file location will be returned by this method.
     * 
     * @param assemblyItem the repository item of the assembly to be unpublished
     * @param saveFolder the folder location on the local file system where the assembly file should be saved (may be
     *        null)
     * @throws RepositoryException thrown if an error occurs while accessing the remote repository
     * @throws IOException thrown if an error occurs while saving the repository to the local file system
     */
    public void unpublishAssembly(RepositoryItem assemblyItem, File saveFolder)
        throws RepositoryException, IOException {
        File managedFile = URLUtils.toFile( repositoryManager.getContentLocation( assemblyItem ) );
        File saveFile = null;

        if (saveFolder != null) {
            saveFile = new File( saveFolder, managedFile.getName() );
            fileUtils.copyFile( managedFile, saveFile );
        }
        repositoryManager.delete( assemblyItem );
    }

    /**
     * Loads a model that consists of the assembly items provided. This method assumes the assembly has been
     * pre-validated prior to calling this method.
     * 
     * @param assemblyItems the list of assembly items to be loaded
     * @return TLModel
     * @throws SchemaCompilerException thrown if an error occurs while loading the model
     */
    private TLModel loadModel(List<ServiceAssemblyMember> assemblyItems) throws SchemaCompilerException {
        ReleaseManager releaseManager = new ReleaseManager( repositoryManager );
        Map<ServiceAssemblyMember,Release> itemReleaseMap = new HashMap<>();
        List<Release> allReleases = new ArrayList<>();

        // Start by loading each of the releases associated with each assembly item
        for (ServiceAssemblyMember saItem : assemblyItems) {
            try {
                ValidationFindings rFindings = new ValidationFindings();
                ReleaseItem releaseItem = releaseManager.loadRelease( saItem.getReleaseItem(), rFindings );

                if (!rFindings.hasFinding( FindingType.ERROR )) {
                    itemReleaseMap.put( saItem, releaseItem.getContent() );
                    allReleases.add( releaseItem.getContent() );
                }

            } catch (RepositoryException e) {
                log.warn( String.format( "Unable to load release from remote repository: %s",
                    saItem.getReleaseItem().getFilename() ) );
            }
        }

        // Next, load the model using the releases to assign effective dates for
        // each library
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<>();
        MultiReleaseModuleLoader moduleLoader =
            new MultiReleaseModuleLoader( allReleases, repositoryManager, modelLoader.getModuleLoader() );
        TLModel model;

        modelLoader.setModuleLoader( moduleLoader );
        modelLoader.setResolveModelReferences( false );

        for (Release release : allReleases) {
            for (ReleaseMember member : release.getAllMembers()) {
                LibraryInputSource<InputStream> inputSource = repositoryManager
                    .getHistoricalContentSource( member.getRepositoryItem(), member.getEffectiveDate() );

                modelLoader.loadLibraryModel( inputSource );
            }
        }
        model = modelLoader.getLibraryModel();
        ModelReferenceResolver.resolveReferences( model );

        // Finally, we need to identify and purge all unwanted resources from the model
        purgeUnwantedResources( assemblyItems, itemReleaseMap, model );

        return model;
    }

    /**
     * Scans the given model and removes any resources that were not explicitly called out in the service assembly.
     * 
     * @param assemblyItems the list of service assembly items
     * @param itemReleaseMap the map of assembly items to their corresponding releases
     * @param model the model to be scanned for unwanted resources
     */
    private void purgeUnwantedResources(List<ServiceAssemblyMember> assemblyItems,
        Map<ServiceAssemblyMember,Release> itemReleaseMap, TLModel model) {
        Set<TLResource> keepResources = new HashSet<>();

        for (ServiceAssemblyMember saItem : assemblyItems) {
            Release release = itemReleaseMap.get( saItem );
            List<TLLibrary> saLibraries = (release == null) ? new ArrayList<>() : getLibraries( release, model );
            QName resourceName = saItem.getResourceName();

            keepResources.addAll( findRetainedResources( resourceName, saLibraries ) );
        }

        // Purge all resource not identified for keeping
        for (TLLibrary library : model.getUserDefinedLibraries()) {
            List<TLResource> libraryResources = new ArrayList<>( library.getResourceTypes() );

            for (TLResource resource : libraryResources) {
                if (!keepResources.contains( resource )) {
                    library.removeNamedMember( resource );
                }
            }
        }
    }

    /**
     * Returns the list of all resources that should be retained from the given list of libraries.
     * 
     * @param resourceName the qualified name of the resource to retain (may be null)
     * @param libraries the list of libraries to search
     * @return Set&lt;TLResource&gt;
     */
    private Set<TLResource> findRetainedResources(QName resourceName, List<TLLibrary> libraries) {
        Set<TLResource> keepResources = new HashSet<>();

        if (resourceName != null) {
            // If a resource name is explicitly provided, only keep that resource from
            // the associated release (and all of its parents/extensions).
            ModelElementVisitor visitor = new ModelElementVisitorAdapter() {
                @Override
                public boolean visitResource(TLResource resource) {
                    keepResources.add( resource );
                    return true;
                }
            };
            DependencyNavigator navigator = new DependencyNavigator( visitor );

            for (TLLibrary library : libraries) {
                if (library.getNamespace().equals( resourceName.getNamespaceURI() )) {
                    TLResource resource = library.getResourceType( resourceName.getLocalPart() );

                    if (resource != null) {
                        // Using the dependency navigator will allow us to keep the
                        // resource that was explicitly requested, plus all of its
                        // extension and parent-reference dependencies
                        navigator.navigate( resource );
                    }
                }
            }

        } else {
            // If a resource name is not explicitly provided, keep all resources from
            // the associated release
            for (TLLibrary library : libraries) {
                keepResources.addAll( library.getResourceTypes() );
            }
        }
        return keepResources;
    }

    /**
     * Returns all of the user-defined libraries from the model that are associated with the given release.
     * 
     * @param release the release for which to return the associated libraries
     * @param model the model from which to retrieve libraries
     * @return List&lt;TLLibrary&gt;
     */
    private List<TLLibrary> getLibraries(Release release, TLModel model) {
        List<TLLibrary> libraryList = new ArrayList<>();

        for (ReleaseMember member : release.getAllMembers()) {
            RepositoryItem mItem = member.getRepositoryItem();
            AbstractLibrary library = model.getLibrary( mItem.getNamespace(), mItem.getLibraryName() );

            if (library instanceof TLLibrary) {
                libraryList.add( (TLLibrary) library );
            }
        }
        return libraryList;
    }

}

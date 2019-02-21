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
package org.opentravel.schemacompiler.task;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.LibraryFilenameBuilder;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.CatalogLibraryNamespaceResolver;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLDocumentationPatch;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.ReleaseManager;
import org.opentravel.schemacompiler.repository.ReleaseMember;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.ValidatorFactory;
import org.opentravel.schemacompiler.validate.impl.TLModelValidator;

/**
 * Base class for all code generation tasks that provides shared methods, as well as an implementation of the
 * <code>CommonCompilerTaskOptions</code>. Code generation tasks may be invoked using an existing <code>TLModel</code>
 * instance or the file location of an OTM library file. If an OTM library file is used, the model will be loaded and
 * validated prior to executing the code generation task itself.
 * 
 * @author S. Livezey
 */
public abstract class AbstractCompilerTask implements CommonCompilerTaskOptions {
    
    /**
     * 
     */
    private static final String TWO_PARENTS_RELATIVE_PATH = "../../";
    protected RepositoryManager repositoryManager;
    private Map<String,File> generatedFiles = new TreeMap<>();
    private List<AbstractLibrary> primaryLibraries = new ArrayList<>();
    private String validationRuleSetId;
    private String catalogLocation;
    private String outputFolder;
    protected String projectFilename;
    
    /**
     * Default constructor.
     */
    public AbstractCompilerTask() {
        try {
            repositoryManager = RepositoryManager.getDefault();
            
        } catch (RepositoryException e) {
            // Ignore - should never happen, but proceed with null repo-manager if it does
        }
    }
    
    /**
     * Constructor that assigns the repository manager for this task instance.
     * 
     * @param repositoryManager the repository manager to use when retrieving managed content
     */
    public AbstractCompilerTask(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }
    
    /**
     * Loads a model using the content of the specified library (or project) file and compiles the output using the
     * options assigned for this task.
     * 
     * @param libraryOrProjectOrReleaseFile the location of the library/project/release file on the local file system
     * @return ValidationFindings
     * @throws SchemaCompilerException thrown if an unexpected error occurs during the compilation process
     */
    public ValidationFindings compileOutput(File libraryOrProjectOrReleaseFile) throws SchemaCompilerException {
        return compileOutput( URLUtils.toURL( libraryOrProjectOrReleaseFile ) );
    }
    
    /**
     * Loads a model using the content of the specified library (or project or release) file and compiles the output
     * using the options assigned for this task.
     * 
     * @param libraryOrProjectOrReleaseUrl the URL location of the library/project/release file
     * @return ValidationFindings
     * @throws SchemaCompilerException thrown if an unexpected error occurs during the compilation process
     */
    public ValidationFindings compileOutput(URL libraryOrProjectOrReleaseUrl) throws SchemaCompilerException {
        Collection<TLLibrary> userDefinedLibraries = new ArrayList<>();
        Collection<XSDLibrary> legacySchemas = new ArrayList<>();
        ValidationFindings findings;
        
        if (isProjectFile( libraryOrProjectOrReleaseUrl )) {
            findings = compileProject( libraryOrProjectOrReleaseUrl, userDefinedLibraries, legacySchemas );
            
        } else if (isReleaseFile( libraryOrProjectOrReleaseUrl )) {
            findings = compileRelease( libraryOrProjectOrReleaseUrl, userDefinedLibraries, legacySchemas );
            
        } else { // Must be an OTM file
            findings = compileLibrary( libraryOrProjectOrReleaseUrl, userDefinedLibraries, legacySchemas );
        }
        
        // Proceed with compilation if no errors were detected during the load
        if (!findings.hasFinding( FindingType.ERROR )) {
            findings.addAll( compileOutput( userDefinedLibraries, legacySchemas ) );
        }
        return findings;
    }
    
    /**
     * Compiles the project at the URL provided.
     * 
     * @param projectUrl the URL of the project to compile
     * @param userDefinedLibraries the list of user-defined libraries in the model
     * @param legacySchemas the list of legacy XML schemas in the model
     * @return ValidationFindings
     * @throws LibraryLoaderException thrown if an error occurs while loading the library
     * @throws RepositoryException thrown if a remote repository cannot be accessed
     */
    private ValidationFindings compileProject(URL projectUrl, Collection<TLLibrary> userDefinedLibraries,
            Collection<XSDLibrary> legacySchemas) throws LibraryLoaderException, RepositoryException {
        ValidationFindings findings;
        findings = new ValidationFindings();
        ProjectManager projectManager = new ProjectManager( false );
        Project project = projectManager.loadProject( URLUtils.toFile( projectUrl ), findings );
        
        for (ProjectItem item : project.getProjectItems()) {
            AbstractLibrary itemContent = item.getContent();
            
            if (project.getDefaultItem() == item) {
                primaryLibraries.add( itemContent );
            }
            if (itemContent instanceof TLLibrary) {
                userDefinedLibraries.add( (TLLibrary) itemContent );
                
            } else if (itemContent instanceof XSDLibrary) {
                legacySchemas.add( (XSDLibrary) itemContent );
            }
        }
        projectFilename = project.getProjectFile().getName();
        return findings;
    }
    
    /**
     * Compiles the library at the URL provided.
     * 
     * @param userDefinedLibraries the list of user-defined libraries in the model
     * @param legacySchemas the list of legacy XML schemas in the model
     * @return ValidationFindings
     * @throws LibraryLoaderException thrown if an error occurs while loading the library
     */
    private ValidationFindings compileLibrary(URL libraryUrl, Collection<TLLibrary> userDefinedLibraries,
            Collection<XSDLibrary> legacySchemas) throws LibraryLoaderException {
        ValidationFindings findings;
        LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource( libraryUrl );
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<>();
        String catalogLoc = getCatalogLocation();
        TLModel model;
        
        if (catalogLoc != null) {
            modelLoader.setNamespaceResolver(
                    new CatalogLibraryNamespaceResolver( TaskUtils.getPathFromOptionValue( catalogLoc ) ) );
        }
        findings = modelLoader.loadLibraryModel( libraryInput );
        model = modelLoader.getLibraryModel();
        userDefinedLibraries.addAll( model.getUserDefinedLibraries() );
        legacySchemas.addAll( model.getLegacySchemaLibraries() );
        primaryLibraries.add( model.getLibrary( libraryUrl ) );
        return findings;
    }
    
    /**
     * Compiles the release at the URL provided.
     * 
     * @param releaseUrl the URL of the release to compile
     * @param userDefinedLibraries the list of user-defined libraries in the model
     * @param legacySchemas the list of legacy XML schemas in the model
     * @return ValidationFindings
     * @throws RepositoryException thrown if a remote repository cannot be accessed
     */
    private ValidationFindings compileRelease(URL releaseUrl, Collection<TLLibrary> userDefinedLibraries,
            Collection<XSDLibrary> legacySchemas) throws RepositoryException {
        ValidationFindings findings;
        findings = new ValidationFindings();
        ReleaseManager releaseManager = new ReleaseManager( repositoryManager );
        String targetFolder = getOutputFolder();
        
        releaseManager.loadRelease( URLUtils.toFile( releaseUrl ), findings );
        
        for (ReleaseMember member : releaseManager.getRelease().getPrincipalMembers()) {
            AbstractLibrary library = releaseManager.getLibrary( member );
            
            if (library != null) {
                primaryLibraries.add( library );
            }
        }
        
        for (ReleaseMember member : releaseManager.getRelease().getAllMembers()) {
            AbstractLibrary library = releaseManager.getLibrary( member );
            
            if (library != null) {
                if (library instanceof TLLibrary) {
                    userDefinedLibraries.add( (TLLibrary) library );
                    
                } else if (library instanceof XSDLibrary) {
                    legacySchemas.add( (XSDLibrary) library );
                }
            }
        }
        
        // For a release, the release compiler options take precedence over
        // any other options assigned for this task.
        CompilerExtensionRegistry
            .setActiveExtension( releaseManager.getRelease().getCompileOptions().getBindingStyle() );
        applyTaskOptions( releaseManager.getRelease().getCompileOptions() );
        setOutputFolder( targetFolder );
        return findings;
    }
    
    /**
     * Validates an existing <code>Project</code> instance and compiles the output using the options assigned for this
     * task.
     * 
     * @param project the project that contains all of the libraries for which to compile output
     * @return ValidationFindings
     * @throws SchemaCompilerException thrown if an unexpected error occurs during the compilation process
     */
    public ValidationFindings compileOutput(Project project) throws SchemaCompilerException {
        Collection<TLLibrary> userDefinedLibraries = new ArrayList<>();
        Collection<XSDLibrary> legacySchemas = new ArrayList<>();
        
        for (ProjectItem item : project.getProjectItems()) {
            AbstractLibrary itemContent = item.getContent();
            
            if (project.getDefaultItem() == item) {
                primaryLibraries.add( itemContent );
            }
            if (itemContent instanceof TLLibrary) {
                userDefinedLibraries.add( (TLLibrary) itemContent );
                
            } else if (itemContent instanceof XSDLibrary) {
                legacySchemas.add( (XSDLibrary) itemContent );
            }
        }
        projectFilename = project.getProjectFile().getName();
        return compileOutput( userDefinedLibraries, legacySchemas );
    }
    
    /**
     * Validates an existing <code>Release</code> instance and compiles the output using the options that have been
     * pre-configured in the release.
     * 
     * @param releaseManager the manager for a release that contains all of the libraries for which to compile output
     * @return ValidationFindings
     * @throws SchemaCompilerException thrown if an unexpected error occurs during the compilation process
     */
    public ValidationFindings compileOutput(ReleaseManager releaseManager) throws SchemaCompilerException {
        Collection<TLLibrary> userDefinedLibraries = new ArrayList<>();
        Collection<XSDLibrary> legacySchemas = new ArrayList<>();
        String targetFolder = getOutputFolder();
        
        for (ReleaseMember member : releaseManager.getRelease().getPrincipalMembers()) {
            AbstractLibrary library = releaseManager.getLibrary( member );
            
            if (library != null) {
                primaryLibraries.add( library );
            }
        }
        
        for (ReleaseMember member : releaseManager.getRelease().getAllMembers()) {
            AbstractLibrary library = releaseManager.getLibrary( member );
            
            if (library != null) {
                if (library instanceof TLLibrary) {
                    userDefinedLibraries.add( (TLLibrary) library );
                    
                } else if (library instanceof XSDLibrary) {
                    legacySchemas.add( (XSDLibrary) library );
                }
            }
        }
        CompilerExtensionRegistry
            .setActiveExtension( releaseManager.getRelease().getCompileOptions().getBindingStyle() );
        applyTaskOptions( releaseManager.getRelease().getCompileOptions() );
        setOutputFolder( targetFolder );
        return compileOutput( userDefinedLibraries, legacySchemas );
    }
    
    /**
     * Loads the specified <code>Release</code> from an OTM repository and compiles the output using the options that
     * have been pre-configured in the release.
     * 
     * @param releaseItem the repository item for a release that contains all of the libraries for which to compile
     *            output
     * @return ValidationFindings
     * @throws SchemaCompilerException thrown if an unexpected error occurs during the compilation process
     */
    public ValidationFindings compileOutput(RepositoryItem releaseItem) throws SchemaCompilerException {
        ReleaseManager releaseManager = new ReleaseManager( repositoryManager );
        ValidationFindings findings = new ValidationFindings();
        
        releaseManager.loadRelease( releaseItem, findings );
        
        if (!findings.hasFinding( FindingType.ERROR )) {
            findings.addAll( compileOutput( releaseManager ) );
        }
        return findings;
    }
    
    /**
     * Validates an existing <code>TLModel</code> instance and compiles the output using the options assigned for this
     * task.
     * 
     * @param model the model that contains all of the libraries for which to compile output
     * @return ValidationFindings
     * @throws SchemaCompilerException thrown if an unexpected error occurs during the compilation process
     */
    public ValidationFindings compileOutput(TLModel model) throws SchemaCompilerException {
        return compileOutput( model.getUserDefinedLibraries(), model.getLegacySchemaLibraries() );
    }
    
    /**
     * Validates an existing <code>TLModel</code> instance and compiles the output using the options assigned for this
     * task.
     * 
     * @param userDefinedLibraries the list of user-defined libraries for which to compile output
     * @param legacySchemas the list of legacy schemas (xsd files) for which to compile output
     * @return ValidationFindings
     * @throws SchemaCompilerException thrown if an unexpected error occurs during the compilation process
     */
    public ValidationFindings compileOutput(Collection<TLLibrary> userDefinedLibraries,
            Collection<XSDLibrary> legacySchemas) throws SchemaCompilerException {
        ValidationFindings findings = validateLibraries( userDefinedLibraries );
        
        if (!findings.hasFinding( FindingType.ERROR )) {
            generateOutput( userDefinedLibraries, legacySchemas );
        }
        return findings;
    }
    
    /**
     * Validates the given model using the rule set assigned for this task.
     * 
     * @param userDefinedLibraries the list of user-defined libraries to validate
     * @return ValidationFindings
     * @throws SchemaCompilerException thrown if an unexpected error occurs during validation
     */
    protected ValidationFindings validateLibraries(Collection<TLLibrary> userDefinedLibraries)
            throws SchemaCompilerException {
        try {
            ValidationFindings findings = new ValidationFindings();
            
            for (TLLibrary library : userDefinedLibraries) {
                findings.addAll( TLModelValidator.validateModelElement( library,
                        (validationRuleSetId != null) ? validationRuleSetId : ValidatorFactory.COMPILE_RULE_SET_ID ) );
            }
            return findings;
            
        } catch (Exception e) {
            throw new SchemaCompilerException( e );
        }
    }
    
    /**
     * After loading and validation, this method is called to perform the actions require to produce the generated
     * output files.
     * 
     * @param userDefinedLibraries the list of user-defined libraries for which to generate output
     * @param legacySchemas the list of legacy schemas (xsd files) for which to generate output
     * @throws SchemaCompilerException
     */
    protected abstract void generateOutput(Collection<TLLibrary> userDefinedLibraries,
            Collection<XSDLibrary> legacySchemas) throws SchemaCompilerException;
    
    /**
     * Factory method that returns a new code generator instance using the information provided.
     * 
     * @param <S> the source meta-model type to be converted
     * @param targetFormat the string that identifies the desired target output format
     * @param sourceType the source meta-model type to be converted
     * @param filenameBuilder the filename builder to assign to the new code generator
     * @param filter the filter to assign to the new code generator
     * @return CodeGenerator<S>
     * @throws CodeGenerationException
     */
    protected <S> CodeGenerator<S> newCodeGenerator(String targetFormat, Class<S> sourceType,
            CodeGenerationFilenameBuilder<S> filenameBuilder, CodeGenerationFilter filter)
            throws CodeGenerationException {
        CodeGenerator<S> generator = CodeGeneratorFactory.getInstance().newCodeGenerator( targetFormat, sourceType );
        
        if (filenameBuilder != null) {
            generator.setFilenameBuilder( filenameBuilder );
        }
        if (filter != null) {
            generator.setFilter( filter );
        }
        return generator;
    }
    
    /**
     * Constructs the code generation context using the assigned task option values.
     * 
     * @return CodeGenerationContext
     */
    protected CodeGenerationContext createContext() {
        CodeGenerationContext context = new CodeGenerationContext();
        
        if (outputFolder != null) {
            context.setValue( CodeGenerationContext.CK_OUTPUT_FOLDER,
                    TaskUtils.getPathFromOptionValue( outputFolder ).getAbsolutePath() );
        }
        if (projectFilename != null) {
            context.setValue( CodeGenerationContext.CK_PROJECT_FILENAME, projectFilename );
        }
        context.setValue( CodeGenerationContext.CK_COPY_COMPILE_TIME_DEPENDENCIES, Boolean.TRUE.toString() );
        
        // Assign the output folders for legacy and built-in schemas (for now, these values are
        // hard-coded)
        context.setValue( CodeGenerationContext.CK_BUILTIN_SCHEMA_FOLDER, XsdCodegenUtils.BUILT_INS_FOLDER );
        context.setValue( CodeGenerationContext.CK_LEGACY_SCHEMA_FOLDER, XsdCodegenUtils.LEGACY_FOLDER );
        
        return context;
    }
    
    /**
     * Returns true if the given file represents a project (.otp) file, or false if the URL should be interpreted as
     * referring to a release or individual library.
     * 
     * @param url the URL to analyze
     * @return boolean
     */
    protected boolean isProjectFile(URL url) {
        return url.getFile().toLowerCase().endsWith( ".otp" );
    }
    
    /**
     * Returns true if the given file represents an OTM release (.otr) file, or false if the URL should be interpreted
     * as referring to a project or individual library.
     * 
     * @param url the URL to analyze
     * @return boolean
     */
    protected boolean isReleaseFile(URL url) {
        return url.getFile().toLowerCase().endsWith( ".otr" );
    }
    
    /**
     * Returns the owning model for each of the libraries/schemas provided. NOTE: If both collections are empty, this
     * method will return null.
     * 
     * @param userDefinedLibraries the list of user-defined libraries for which to return the owning model
     * @param legacySchemas the list of legacy schemas (xsd files) for which to return the owning model
     * @return TLModel
     */
    protected TLModel getModel(Collection<TLLibrary> userDefinedLibraries, Collection<XSDLibrary> legacySchemas) {
        TLModel model = null;
        
        if (!userDefinedLibraries.isEmpty()) {
            model = userDefinedLibraries.iterator().next().getOwningModel();
            
        } else if (!legacySchemas.isEmpty()) {
            model = legacySchemas.iterator().next().getOwningModel();
        }
        return model;
    }
    
    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#applyTaskOptions(org.opentravel.schemacompiler.task.CommonCompilerTaskOptions)
     */
    @Override
    public void applyTaskOptions(CommonCompilerTaskOptions taskOptions) {
        setCatalogLocation( taskOptions.getCatalogLocation() );
        setOutputFolder( taskOptions.getOutputFolder() );
    }
    
    /**
     * After processing by one of the 'compileOutput()' methods, this method will return the list of all files that were
     * produced by the code generation performed by this task.
     * 
     * @return List<File>
     */
    public List<File> getGeneratedFiles() {
        List<File> fileList = new ArrayList<>();
        
        for (Entry<String,File> entry : generatedFiles.entrySet()) {
            fileList.add( generatedFiles.get( entry.getKey() ) );
        }
        return Collections.unmodifiableList( fileList );
    }
    
    /**
     * Adds a generated file to the list of output artifacts produced by this task.
     * 
     * @param generatedFile the generated file to add
     */
    protected void addGeneratedFile(File generatedFile) {
        String filePath = (generatedFile == null) ? null : generatedFile.getAbsolutePath();
        
        if ((filePath != null) && !generatedFiles.containsKey( filePath )) {
            generatedFiles.put( filePath, generatedFile );
        }
    }
    
    /**
     * Adds each of the generated file to the list of output artifacts produced by this task.
     * 
     * @param generatedFiles the list of generated files to add
     */
    protected void addGeneratedFiles(Collection<File> generatedFiles) {
        if (generatedFiles != null) {
            for (File file : generatedFiles) {
                addGeneratedFile( file );
            }
        }
    }
    
    /**
     * Generates EXAMPLE XML files for all elements of the given library.
     * 
     * @param userDefinedLibraries the list of user-defined libraries for which to generate EXAMPLE XML files
     * @param context the code generation context to use for code generation
     * @param filenameBuilder the filename builder to use for schema location filename construction
     * @param filter the filter used to identify specific artifacts for which EXAMPLE generation is required
     * @param targetFormat indicates the output format of the generated EXAMPLE files (e.g. "XML" or "JSON")
     * @throws SchemaCompilerException
     */
    protected void generateExampleArtifacts(Collection<TLLibrary> userDefinedLibraries, CodeGenerationContext context,
            CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder, CodeGenerationFilter filter,
            String targetFormat) throws SchemaCompilerException {
        CodeGenerator<TLModelElement> exampleGenerator = getExampleGenerator( targetFormat, filenameBuilder );
        CodeGenerationContext exampleContext = context.getCopy();
        
        // Generate examples for all model entities that are not excluded by the filter
        for (TLLibrary library : userDefinedLibraries) {
            if (processLibrary( library, filter )) {
                continue;
            }
            
            // Generate EXAMPLE files for each member of the library
            for (LibraryMember member : library.getNamedMembers()) {
                if (processEntity( member, filter ) || (member instanceof TLDocumentationPatch)) {
                    continue;
                }
                
                exampleContext.setValue( CodeGenerationContext.CK_OUTPUT_FOLDER,
                        getExampleOutputFolder( member, context ) );
                exampleContext.setValue( CodeGenerationContext.CK_EXAMPLE_SCHEMA_RELATIVE_PATH,
                        getSchemaRelativeFolderPath( member, exampleContext ) );
                
                if (member instanceof TLService) {
                    generateServiceArtifacts( (TLService) member, exampleGenerator, exampleContext );
                    
                } else if (member instanceof TLResource) {
                    generateResourceArtifacts( (TLResource) member, filter, exampleGenerator, exampleContext );
                    
                } else {
                    addGeneratedFiles( exampleGenerator.generateOutput( (TLModelElement) member, exampleContext ) );
                }
            }
        }
    }
    
    /**
     * Returns true if the given library should be processed for code generation.
     * 
     * @param library the library to check
     * @param filter the code generation filter
     * @return boolean
     */
    private boolean processLibrary(TLLibrary library, CodeGenerationFilter filter) {
        return (filter != null) && !filter.processLibrary( library );
    }
    
    /**
     * Returns true if the given library member should be processed for code generation.
     * 
     * @param member the library member to check
     * @param filter the code generation filter
     * @return boolean
     */
    private boolean processEntity(LibraryMember member, CodeGenerationFilter filter) {
        return (filter != null) && !filter.processEntity( member );
    }
    
    /**
     * Generates code output artifacts for the given service.
     * 
     * @param service the service for which to generate output
     * @param exampleGenerator the example generator to use during code generation
     * @param exampleContext the example context to use during code generation
     * @throws ValidationException thrown if one or more validation errors are detected
     * @throws CodeGenerationException thrown if an error occurs while generating output
     */
    private void generateServiceArtifacts(TLService service, CodeGenerator<TLModelElement> exampleGenerator,
            CodeGenerationContext exampleContext) throws ValidationException, CodeGenerationException {
        for (TLOperation operation : service.getOperations()) {
            if (operation.getRequest().declaresContent()) {
                addGeneratedFiles( exampleGenerator.generateOutput( operation.getRequest(), exampleContext ) );
            }
            if (operation.getResponse().declaresContent()) {
                addGeneratedFiles( exampleGenerator.generateOutput( operation.getResponse(), exampleContext ) );
            }
            if (operation.getNotification().declaresContent()) {
                addGeneratedFiles( exampleGenerator.generateOutput( operation.getNotification(), exampleContext ) );
            }
        }
    }
    
    /**
     * @param resource the resource for which to generate output artifacts.
     * 
     * @param filter the filter to use during code generation
     * @param exampleGenerator the example generator to use during code generation
     * @param exampleContext the example context to use during code generation
     * @throws ValidationException thrown if one or more validation errors are detected
     * @throws CodeGenerationException thrown if an error occurs while generating output
     */
    private void generateResourceArtifacts(TLResource resource, CodeGenerationFilter filter,
            CodeGenerator<TLModelElement> exampleGenerator, CodeGenerationContext exampleContext)
            throws ValidationException, CodeGenerationException {
        for (TLAction action : ResourceCodegenUtils.getInheritedActions( resource )) {
            TLActionRequest request = ResourceCodegenUtils.getDeclaredOrInheritedRequest( action );
            
            if (request != null) {
                NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( request.getPayloadType() );
                
                if ((payloadType != null) && ((filter == null) || filter.processEntity( payloadType ))) {
                    addGeneratedFiles(
                            exampleGenerator.generateOutput( (TLModelElement) payloadType, exampleContext ) );
                }
            }
            
            for (TLActionResponse response : ResourceCodegenUtils.getInheritedResponses( action )) {
                NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( response.getPayloadType() );
                
                if ((payloadType != null) && ((filter == null) || filter.processEntity( payloadType ))) {
                    addGeneratedFiles(
                            exampleGenerator.generateOutput( (TLModelElement) payloadType, exampleContext ) );
                }
            }
        }
    }
    
    /**
     * Gets the EXAMPLE generator for the specified target output format.
     * 
     * @param targetFormat the target output format of the EXAMPLE files to be generated
     * @param filenameBuilder the filename builder for the EXAMPLE code generator
     * @return the list of EXAMPLE generators.
     * @throws CodeGenerationException
     */
    private CodeGenerator<TLModelElement> getExampleGenerator(String targetFormat,
            CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder) throws CodeGenerationException {
        CodeGenerator<TLModelElement> exampleGenerator = CodeGeneratorFactory.getInstance()
            .newCodeGenerator( targetFormat, TLModelElement.class );
        TrimmedExampleFilenameBuilder trimmedFilenameBuilder = new TrimmedExampleFilenameBuilder(
                exampleGenerator.getFilenameBuilder(), filenameBuilder );
        
        exampleGenerator.setFilenameBuilder( trimmedFilenameBuilder );
        return exampleGenerator;
    }
    
    /**
     * Returns the location of the EXAMPLE output folder for all members of the given library.
     * 
     * @param libraryMember the library member element for which the EXAMPLE output folder is needed
     * @param context the code generation context
     * @return String
     */
    protected String getExampleOutputFolder(LibraryMember libraryMember, CodeGenerationContext context) {
        String libraryFolderName = "examples/"
                + new LibraryFilenameBuilder<AbstractLibrary>().buildFilename( libraryMember.getOwningLibrary(), "" );
        String rootOutputFolder = context.getValue( CodeGenerationContext.CK_OUTPUT_FOLDER );
        
        if (rootOutputFolder == null) {
            rootOutputFolder = System.getProperty( "user.dir" );
        }
        return new File( rootOutputFolder, libraryFolderName ).getAbsolutePath();
    }
    
    /**
     * Returns a string that specifies the relative folder location of the schema to be generated for the specified
     * library member.
     * 
     * @param libraryMember the library member element for which the schema location folder is needed
     * @param context the code generation context
     * @return String
     */
    protected String getSchemaRelativeFolderPath(LibraryMember libraryMember, CodeGenerationContext context) {
        return TWO_PARENTS_RELATIVE_PATH;
    }
    
    /**
     * Returns the libraries that are the primary focus of the compilation effort. If these libraries are present, all
     * others will be trimmed to only those dependencies that are required by the primary.
     *
     * @return List<AbstractLibrary>
     */
    public List<AbstractLibrary> getPrimaryLibraries() {
        return primaryLibraries;
    }
    
    /**
     * Returns application context ID of the rule set to use when validating models prior to code generation.
     * 
     * @return String
     */
    public String getValidationRuleSetId() {
        return validationRuleSetId;
    }
    
    /**
     * Assigns application context ID of the rule set to use when validating models prior to code generation.
     * 
     * @param validationRuleSetId the ID of the validation rule set to assign
     */
    public void setValidationRuleSetId(String validationRuleSetId) {
        this.validationRuleSetId = validationRuleSetId;
    }
    
    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#getCatalogLocation()
     */
    @Override
    public String getCatalogLocation() {
        return catalogLocation;
    }
    
    /**
     * Assigns the location of the library catalog file as either an absolute or relative URL string.
     * 
     * @param catalogLocation the task option value to assign
     */
    public void setCatalogLocation(String catalogLocation) {
        this.catalogLocation = catalogLocation;
    }
    
    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#getOutputFolder()
     */
    @Override
    public String getOutputFolder() {
        return outputFolder;
    }
    
    /**
     * Assigns the output folder location as either an absolute or relative URL string.
     * 
     * @param outputFolder the task option value to assign
     */
    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }
    
    /**
     * Returns the manager being used by this task to access remote repositories.
     *
     * @return RepositoryManager
     */
    public RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }
    
    /**
     * Assigns the manager being used by this task to access remote repositories.
     *
     * @param repositoryManager the repository manager instance to assign
     */
    public void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }
    
    /**
     * 
     * Wrapper around default {@link CodeGenerationFilenameBuilder} that support different handling for
     * {@link AbstractLibrary}.
     * 
     * @author Pawel Jedruch
     * 
     */
    class TrimmedExampleFilenameBuilder implements CodeGenerationFilenameBuilder<TLModelElement> {
        
        private CodeGenerationFilenameBuilder<TLModelElement> defaultNonLibraryBuilder;
        private CodeGenerationFilenameBuilder<AbstractLibrary> libraryFilenameBuilder;
        
        /**
         * @param defaultNonLibraryBuilder - builder used for all non {@link AbstractLibrary} models
         * @param libraryFilenameBuilder - builder used only for {@link AbstractLibrary}
         */
        public TrimmedExampleFilenameBuilder(CodeGenerationFilenameBuilder<TLModelElement> defaultNonLibraryBuilder,
                CodeGenerationFilenameBuilder<AbstractLibrary> libraryFilenameBuilder) {
            this.defaultNonLibraryBuilder = defaultNonLibraryBuilder;
            this.libraryFilenameBuilder = libraryFilenameBuilder;
        }
        
        @Override
        public String buildFilename(TLModelElement item, String fileExtension) {
            if (item instanceof AbstractLibrary) {
                return libraryFilenameBuilder.buildFilename( (AbstractLibrary) item, fileExtension );
            } else {
                return defaultNonLibraryBuilder.buildFilename( (TLModelElement) item, fileExtension );
            }
        }
        
    }
    
}

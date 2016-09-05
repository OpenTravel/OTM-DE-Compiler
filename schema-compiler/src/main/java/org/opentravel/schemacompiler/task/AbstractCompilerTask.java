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
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.CatalogLibraryNamespaceResolver;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
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
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.ValidatorFactory;
import org.opentravel.schemacompiler.validate.impl.TLModelValidator;

/**
 * Base class for all code generation tasks that provides shared methods, as well as an
 * implementation of the <code>CommonCompilerTaskOptions</code>. Code generation tasks may be
 * invoked using an existing <code>TLModel</code> instance or the file location of an OTM library
 * file. If an OTM library file is used, the model will be loaded and validated prior to executing
 * the code generation task itself.
 * 
 * @author S. Livezey
 */
public abstract class AbstractCompilerTask implements CommonCompilerTaskOptions {

    private Map<String, File> generatedFiles = new TreeMap<String, File>();
    private String validationRuleSetId;
    private String catalogLocation;
    private String outputFolder;
    protected String projectFilename;

    /**
     * Loads a model using the content of the specified library (or project) file and compiles the
     * output using the options assigned for this task.
     * 
     * @param libraryOrProjectFile
     *            the location of the library/project file on the local file system
     * @return ValidationFindings
     * @throws SchemaCompilerException
     *             thrown if an unexpected error occurs during the compilation process
     */
    public ValidationFindings compileOutput(File libraryOrProjectFile)
            throws SchemaCompilerException {
        return compileOutput(URLUtils.toURL(libraryOrProjectFile));
    }

    /**
     * Loads a model using the content of the specified library (or project) file and compiles the
     * output using the options assigned for this task.
     * 
     * @param libraryOrProjectUrl
     *            the URL location of the library/project file
     * @return ValidationFindings
     * @throws SchemaCompilerException
     *             thrown if an unexpected error occurs during the compilation process
     */
    public ValidationFindings compileOutput(URL libraryOrProjectUrl) throws SchemaCompilerException {
        Collection<TLLibrary> userDefinedLibraries = new ArrayList<TLLibrary>();
        Collection<XSDLibrary> legacySchemas = new ArrayList<XSDLibrary>();
        ValidationFindings findings;

        if (isProjectFile(libraryOrProjectUrl)) {
            findings = new ValidationFindings();
            ProjectManager projectManager = new ProjectManager(false);
            Project project = projectManager.loadProject(URLUtils.toFile(libraryOrProjectUrl),
                    findings);

            for (ProjectItem item : project.getProjectItems()) {
                AbstractLibrary itemContent = item.getContent();

                if (itemContent instanceof TLLibrary) {
                    userDefinedLibraries.add((TLLibrary) itemContent);

                } else if (itemContent instanceof XSDLibrary) {
                    legacySchemas.add((XSDLibrary) itemContent);
                }
            }
            projectFilename = project.getProjectFile().getName();

        } else {
            LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(
                    libraryOrProjectUrl);
            LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
            String catalogLocation = getCatalogLocation();

            if (catalogLocation != null) {
                modelLoader.setNamespaceResolver(new CatalogLibraryNamespaceResolver(TaskUtils
                        .getPathFromOptionValue(catalogLocation)));
            }
            findings = modelLoader.loadLibraryModel(libraryInput);
            userDefinedLibraries.addAll(modelLoader.getLibraryModel().getUserDefinedLibraries());
            legacySchemas.addAll(modelLoader.getLibraryModel().getLegacySchemaLibraries());
        }

        // Proceed with compilation if no errors were detected during the load
        if (!findings.hasFinding(FindingType.ERROR)) {
            findings.addAll(compileOutput(userDefinedLibraries, legacySchemas));
        }
        return findings;
    }

    /**
     * Validates an existing <code>Project</code> instance and compiles the output using the options
     * assigned for this task.
     * 
     * @param project
     *            the project that contains all of the libraries for which to compile output
     * @return ValidationFindings
     * @throws SchemaCompilerException
     *             thrown if an unexpected error occurs during the compilation process
     */
    public ValidationFindings compileOutput(Project project) throws SchemaCompilerException {
        Collection<TLLibrary> userDefinedLibraries = new ArrayList<TLLibrary>();
        Collection<XSDLibrary> legacySchemas = new ArrayList<XSDLibrary>();

        for (ProjectItem item : project.getProjectItems()) {
            AbstractLibrary itemContent = item.getContent();

            if (itemContent instanceof TLLibrary) {
                userDefinedLibraries.add((TLLibrary) itemContent);

            } else if (itemContent instanceof XSDLibrary) {
                legacySchemas.add((XSDLibrary) itemContent);
            }
        }
        projectFilename = project.getProjectFile().getName();
        return compileOutput(userDefinedLibraries, legacySchemas);
    }

    /**
     * Validates an existing <code>TLModel</code> instance and compiles the output using the options
     * assigned for this task.
     * 
     * @param model
     *            the model that contains all of the libraries for which to compile output
     * @return ValidationFindings
     * @throws SchemaCompilerException
     *             thrown if an unexpected error occurs during the compilation process
     */
    public ValidationFindings compileOutput(TLModel model) throws SchemaCompilerException {
        return compileOutput(model.getUserDefinedLibraries(), model.getLegacySchemaLibraries());
    }

    /**
     * Validates an existing <code>TLModel</code> instance and compiles the output using the options
     * assigned for this task.
     * 
     * @param userDefinedLibraries
     *            the list of user-defined libraries for which to compile output
     * @param legacySchemas
     *            the list of legacy schemas (xsd files) for which to compile output
     * @return ValidationFindings
     * @throws SchemaCompilerException
     *             thrown if an unexpected error occurs during the compilation process
     */
    public ValidationFindings compileOutput(Collection<TLLibrary> userDefinedLibraries,
            Collection<XSDLibrary> legacySchemas) throws SchemaCompilerException {
        ValidationFindings findings = validateLibraries(userDefinedLibraries);

        if (!findings.hasFinding(FindingType.ERROR)) {
            generateOutput(userDefinedLibraries, legacySchemas);
        }
        return findings;
    }

    /**
     * Validates the given model using the rule set assigned for this task.
     * 
     * @param userDefinedLibraries
     *            the list of user-defined libraries to validate
     * @return ValidationFindings
     * @throws SchemaCompilerException
     *             thrown if an unexpected error occurs during validation
     */
    protected ValidationFindings validateLibraries(Collection<TLLibrary> userDefinedLibraries)
            throws SchemaCompilerException {
        try {
            ValidationFindings findings = new ValidationFindings();

            for (TLLibrary library : userDefinedLibraries) {
                return TLModelValidator.validateModelElement(library,
                        (validationRuleSetId != null) ? validationRuleSetId
                                : ValidatorFactory.COMPILE_RULE_SET_ID);
            }
            return findings;

        } catch (Throwable t) {
            throw new SchemaCompilerException(t);
        }
    }

    /**
     * After loading and validation, this method is called to perform the actions require to produce
     * the generated output files.
     * 
     * @param userDefinedLibraries
     *            the list of user-defined libraries for which to generate output
     * @param legacySchemas
     *            the list of legacy schemas (xsd files) for which to generate output
     * @throws SchemaCompilerException
     */
    protected abstract void generateOutput(Collection<TLLibrary> userDefinedLibraries,
            Collection<XSDLibrary> legacySchemas) throws SchemaCompilerException;

    /**
     * Factory method that returns a new code generator instance using the information provided.
     * 
     * @param <S>
     *            the source meta-model type to be converted
     * @param targetFormat
     *            the string that identifies the desired target output format
     * @param sourceType
     *            the source meta-model type to be converted
     * @param filenameBuilder
     *            the filename builder to assign to the new code generator
     * @param filter
     *            the filter to assign to the new code generator
     * @return CodeGenerator<S>
     * @throws CodeGenerationException
     */
    protected <S> CodeGenerator<S> newCodeGenerator(String targetFormat, Class<S> sourceType,
            CodeGenerationFilenameBuilder<S> filenameBuilder, CodeGenerationFilter filter)
            throws CodeGenerationException {
        CodeGenerator<S> generator = CodeGeneratorFactory.getInstance().newCodeGenerator(
                targetFormat, sourceType);

        if (filenameBuilder != null) {
            generator.setFilenameBuilder(filenameBuilder);
        }
        if (filter != null) {
            generator.setFilter(filter);
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
            context.setValue(CodeGenerationContext.CK_OUTPUT_FOLDER, TaskUtils
                    .getPathFromOptionValue(outputFolder).getAbsolutePath());
        }
        if (projectFilename != null) {
            context.setValue(CodeGenerationContext.CK_PROJECT_FILENAME, projectFilename);
        }
        context.setValue(CodeGenerationContext.CK_COPY_COMPILE_TIME_DEPENDENCIES,
                Boolean.TRUE.toString());

        // Assign the output folders for legacy and built-in schemas (for now, these values are
        // hard-coded)
        context.setValue(CodeGenerationContext.CK_BUILTIN_SCHEMA_FOLDER,
                XsdCodegenUtils.BUILT_INS_FOLDER);
        context.setValue(CodeGenerationContext.CK_LEGACY_SCHEMA_FOLDER,
                XsdCodegenUtils.LEGACY_FOLDER);

        return context;
    }

    /**
     * Returns true if the given file represents a project (.otp) file, or false if the URL should
     * be interpreted as referring to an individual library.
     * 
     * @param url
     *            the URL to analyze
     * @return boolean
     */
    protected boolean isProjectFile(URL url) {
        return url.getFile().toLowerCase().endsWith(".otp");
    }

    /**
     * Returns the owning model for each of the libraries/schemas provided. NOTE: If both
     * collections are empty, this method will return null.
     * 
     * @param userDefinedLibraries
     *            the list of user-defined libraries for which to return the owning model
     * @param legacySchemas
     *            the list of legacy schemas (xsd files) for which to return the owning model
     * @return TLModel
     */
    protected TLModel getModel(Collection<TLLibrary> userDefinedLibraries,
            Collection<XSDLibrary> legacySchemas) {
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
        setCatalogLocation(taskOptions.getCatalogLocation());
        setOutputFolder(taskOptions.getOutputFolder());
    }

    /**
     * After processing by one of the 'compileOutput()' methods, this method will return the list of
     * all files that were produced by the code generation performed by this task.
     * 
     * @return List<File>
     */
    public List<File> getGeneratedFiles() {
        List<File> fileList = new ArrayList<File>();

        for (String filePath : generatedFiles.keySet()) {
            fileList.add(generatedFiles.get(filePath));
        }
        return Collections.unmodifiableList(fileList);
    }

    /**
     * Adds a generated file to the list of output artifacts produced by this task.
     * 
     * @param generatedFile
     *            the generated file to add
     */
    protected void addGeneratedFile(File generatedFile) {
        String filePath = (generatedFile == null) ? null : generatedFile.getAbsolutePath();

        if ((filePath != null) && !generatedFiles.containsKey(filePath)) {
            generatedFiles.put(filePath, generatedFile);
        }
    }

    /**
     * Adds each of the generated file to the list of output artifacts produced by this task.
     * 
     * @param generatedFiles
     *            the list of generated files to add
     */
    protected void addGeneratedFiles(Collection<File> generatedFiles) {
        if (generatedFiles != null) {
            for (File file : generatedFiles) {
                addGeneratedFile(file);
            }
        }
    }

    /**
     * Generates example XML files for all elements of the given library.
     * 
     * @param userDefinedLibraries
     *            the list of user-defined libraries for which to generate example XML files
     * @param context
     *            the code generation context to use for code generation
     * @param filenameBuilder
     *            the filename builder to use for schema location filename construction
     * @param filter
     *            the filter used to identify specific artifacts for which example generation is
     *            required
     * @param targetFormat
     * 			  indicates the output format of the generated example files (e.g. "XML" or "JSON")
     * @throws SchemaCompilerException
     */
	protected void generateExampleArtifacts(Collection<TLLibrary> userDefinedLibraries,
            CodeGenerationContext context, CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder,
            CodeGenerationFilter filter, String targetFormat) throws SchemaCompilerException {
		CodeGenerator<TLModelElement> exampleGenerator = getExampleGenerator(targetFormat, filenameBuilder);
        CodeGenerationContext exampleContext = context.getCopy();

        // Generate examples for all model entities that are not excluded by the filter
        for (TLLibrary library : userDefinedLibraries) {
            if ((filter != null) && !filter.processLibrary(library)) {
                continue;
            }

            // Generate example files for each member of the library
            for (LibraryMember member : library.getNamedMembers()) {
                if ((filter != null) && !filter.processEntity(member)) {
                    continue;
                }
                exampleContext.setValue(CodeGenerationContext.CK_OUTPUT_FOLDER,
                        getExampleOutputFolder(member, context));
                exampleContext.setValue(CodeGenerationContext.CK_EXAMPLE_SCHEMA_RELATIVE_PATH,
                        getSchemaRelativeFolderPath(member, exampleContext));

                if (member instanceof TLService) {
                    TLService service = (TLService) member;

                    for (TLOperation operation : service.getOperations()) {
                        if (operation.getRequest().declaresContent()) {
							addGeneratedFiles(exampleGenerator.generateOutput(operation.getRequest(), exampleContext));
                        }
                        if (operation.getResponse().declaresContent()) {
							addGeneratedFiles(exampleGenerator.generateOutput(operation.getResponse(), exampleContext));
                        }
                        if (operation.getNotification().declaresContent()) {
							addGeneratedFiles(exampleGenerator.generateOutput(operation.getNotification(), exampleContext));
                        }
                    }
                    
                } else if (member instanceof TLResource) {
                	TLResource resource = (TLResource) member;
                	
                	for (TLAction action : ResourceCodegenUtils.getInheritedActions( resource )) {
                		TLActionRequest request = ResourceCodegenUtils.getDeclaredOrInheritedRequest( action );
                		
                		if (request != null) {
                			NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( request.getPayloadType() );
                			
                			if ((payloadType != null) && ((filter == null) || filter.processEntity( payloadType ))) {
        						addGeneratedFiles( exampleGenerator.generateOutput(
        								(TLModelElement) payloadType, exampleContext ) );
                			}
                		}
                		
                		for (TLActionResponse response : ResourceCodegenUtils.getInheritedResponses( action )) {
                			NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( response.getPayloadType() );
                			
                			if ((payloadType != null) && ((filter == null) || filter.processEntity( payloadType ))) {
        						addGeneratedFiles( exampleGenerator.generateOutput(
        								(TLModelElement) payloadType, exampleContext ) );
                			}
                		}
                	}
                	
				} else {
					addGeneratedFiles(exampleGenerator.generateOutput((TLModelElement) member, exampleContext));
               	}
            }
        }
	}
                	
	/**
	 * Gets the example generator for the specified target output format.
	 * 
	 * @param targetFormat  the target output format of the example files to be generated
	 * @param filenameBuilder  the filename builder for the example code generator
	 * @return the list of example generators.
	 * @throws CodeGenerationException
	 */
	private CodeGenerator<TLModelElement> getExampleGenerator(String targetFormat,
			CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder) throws CodeGenerationException {
		CodeGenerator<TLModelElement> exampleGenerator =
				CodeGeneratorFactory.getInstance().newCodeGenerator(targetFormat, TLModelElement.class);
		TrimmedExampleFilenameBuilder trimmedFilenameBuilder = new TrimmedExampleFilenameBuilder(
				exampleGenerator.getFilenameBuilder(), filenameBuilder);
		
		exampleGenerator.setFilenameBuilder(trimmedFilenameBuilder);
		return exampleGenerator;
	}

    /**
     * Returns the location of the example output folder for all members of the given library.
     * 
     * @param libraryMember
     *            the library member element for which the example output folder is needed
     * @param context
     *            the code generation context
     * @return String
     */
    protected String getExampleOutputFolder(LibraryMember libraryMember, CodeGenerationContext context) {
        String libraryFolderName = "examples/"
                + new LibraryFilenameBuilder<AbstractLibrary>().buildFilename(
                        libraryMember.getOwningLibrary(), "");
        String rootOutputFolder = context.getValue(CodeGenerationContext.CK_OUTPUT_FOLDER);

        if (rootOutputFolder == null) {
            rootOutputFolder = System.getProperty("user.dir");
        }
        return new File(rootOutputFolder, libraryFolderName).getAbsolutePath();
    }

    /**
     * Returns a string that specifies the relative folder location of the schema to be generated
     * for the specified library member.
     * 
     * @param libraryMember
     *            the library member element for which the schema location folder is needed
     * @param context
     *            the code generation context
     * @return String
     */
    protected String getSchemaRelativeFolderPath(LibraryMember libraryMember,
            CodeGenerationContext context) {
        return "../../";
    }

    /**
     * Returns application context ID of the rule set to use when validating models prior to code
     * generation.
     * 
     * @return String
     */
    public String getValidationRuleSetId() {
        return validationRuleSetId;
    }

    /**
     * Assigns application context ID of the rule set to use when validating models prior to code
     * generation.
     * 
     * @param validationRuleSetId
     *            the ID of the validation rule set to assign
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
     * Assigns the location of the library catalog file as either an absolute or relative URL
     * string.
     * 
     * @param catalogLocation
     *            the task option value to assign
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
     * @param outputFolder
     *            the task option value to assign
     */
    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

     /**
     * 
     * Wrapper around default {@link CodeGenerationFilenameBuilder} that support different handling
     * for {@link AbstractLibrary}.
     * 
     * @author Pawel Jedruch
     * 
     */
    class TrimmedExampleFilenameBuilder implements CodeGenerationFilenameBuilder<TLModelElement> {

        private CodeGenerationFilenameBuilder<TLModelElement> defaultNonLibraryBuilder;
        private CodeGenerationFilenameBuilder<AbstractLibrary> libraryFilenameBuilder;

        /**
         * @param defaultNonLibraryBuilder
         *            - builder used for all non {@link AbstractLibrary} models
         * @param libraryFilenameBuilder
         *            - builder used only for {@link AbstractLibrary}
         */
        public TrimmedExampleFilenameBuilder(
                CodeGenerationFilenameBuilder<TLModelElement> defaultNonLibraryBuilder,
                CodeGenerationFilenameBuilder<AbstractLibrary> libraryFilenameBuilder) {
            this.defaultNonLibraryBuilder = defaultNonLibraryBuilder;
            this.libraryFilenameBuilder = libraryFilenameBuilder;
        }

        @Override
        public String buildFilename(TLModelElement item, String fileExtension) {
            if (item instanceof AbstractLibrary) {
                return libraryFilenameBuilder.buildFilename((AbstractLibrary) item, fileExtension);
            } else {
                return defaultNonLibraryBuilder.buildFilename((TLModelElement) item, fileExtension);
            }
        }

    }

}

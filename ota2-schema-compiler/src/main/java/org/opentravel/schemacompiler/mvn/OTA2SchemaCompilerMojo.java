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
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemType;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.task.CommonCompilerTaskOptions;
import org.opentravel.schemacompiler.task.CompileAllCompilerTask;
import org.opentravel.schemacompiler.task.CompileAllTaskOptions;
import org.opentravel.schemacompiler.task.TaskFactory;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

/**
 * Maven Mojo that handles the compilation of files from the OTA2 library format into XML schemas and WSDL documents.
 * 
 * @author S. Livezey
 */
@Mojo(name = "ota2-compile", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
@Execute(goal = "ota2-compile", phase = LifecyclePhase.GENERATE_SOURCES)
public class OTA2SchemaCompilerMojo extends AbstractMojo implements CompileAllTaskOptions {

    private static final String NULL_VALUE = "[NULL]";

    /**
     * The location of the library file to be compiled.
     */
    @Parameter
    protected File libraryFile;

    /**
     * The repository information for the OTM release to be compiled.
     */
    @Parameter
    protected Release release;

    /**
     * The location of the library catalog file.
     */
    @Parameter
    protected File catalog;

    /**
     * The output folder location for generated files (default location is
     * &lt;project&gt;/target/generated-sources/ota2).
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/ota2")
    protected File outputFolder;

    /**
     * The binding style for generated schemas and services (default is 'OTA2').
     */
    @Parameter
    protected String bindingStyle;

    /**
     * Boolean flag indicating that XML schema files should be generated.
     */
    @Parameter(defaultValue = "true")
    protected boolean compileSchemas;

    /**
     * Boolean flag indicating that JSON schema files should be generated.
     */
    @Parameter(defaultValue = "true")
    protected boolean compileJson;

    /**
     * Boolean flag indicating that WSDL schema files should be generated.
     */
    @Parameter(defaultValue = "true")
    protected boolean compileServices;

    /**
     * Boolean flag indicating that Swagger files should be generated.
     */
    @Parameter(defaultValue = "true")
    protected boolean compileSwagger;

    /**
     * Boolean flag indicating that html files should be generated.
     */
    @Parameter(defaultValue = "true")
    protected boolean compileHtml;

    /**
     * Boolean flag indicating that example data files should be generated.
     */
    @Parameter(defaultValue = "true")
    protected boolean generateExamples;

    /**
     * The URL of the root service endpoint for WSDL services.
     */
    @Parameter
    protected String serviceEndpointUrl;

    /**
     * The base URL path for all generated REST resources.
     */
    @Parameter
    protected String resourceBaseUrl;

    /**
     * Boolean flag indicating whether the compiler should supress all 'x-otm-' extensions in the generated swagger
     * document(s).
     */
    @Parameter(defaultValue = "false")
    protected boolean suppressOtmExtensions;

    /**
     * Boolean flag indicating that the maximum amount of detail is to be included in generated example data. If false,
     * minimum detail will be generated.
     */
    @Parameter(defaultValue = "true")
    protected boolean generateMaxDetailsForExamples;

    /**
     * The preferred context ID to use when producing example values for simple data types.
     */
    @Parameter
    protected String exampleContext;

    /**
     * The maximum number of times that repeating elements should be displayed in generated example output.
     */
    @Parameter
    protected Integer exampleMaxRepeat;

    /**
     * The maximum depth that should be included for nested elements in generated example output.
     */
    @Parameter
    protected Integer exampleMaxDepth;

    /**
     * Boolean flag indicating whether optional fields should be suppressed during example generation.
     */
    @Parameter(defaultValue = "true")
    protected boolean suppressOptionalFields;

    /**
     * Flag used for debugging that causes this Mojo's configuration settings to be displayed prior to execution.
     */
    @Parameter(defaultValue = "false")
    protected boolean debug;

    private RepositoryManager repositoryManager;

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        synchronized (OTA2SchemaCompilerMojo.class) {
            try {
                if (debug) {
                    displayOptions();
                }
                initRepositoryManager( null );

                // Validate the source file or managed release and the output folder
                RepositoryItem releaseItem = null;

                if (libraryFile != null) {
                    if (!libraryFile.exists()) {
                        throw new FileNotFoundException( "Source file not found: " + libraryFile.getAbsolutePath() );
                    }

                } else if (release != null) {
                    releaseItem = getReleaseItem();

                } else {
                    throw new MojoFailureException( "Either a libraryFile or a release must be specified." );
                }
                createOutputFolder();

                // Select the user-specified schema compiler extension
                if (bindingStyle != null) {
                    if (CompilerExtensionRegistry.getAvailableExtensionIds().contains( bindingStyle )) {
                        CompilerExtensionRegistry.setActiveExtension( bindingStyle );

                    } else {
                        throw new MojoFailureException( "Invalid binding style specified: " + bindingStyle );
                    }
                }

                // Execute the compilation and return
                CompileAllCompilerTask compilerTask = TaskFactory.getTask( CompileAllCompilerTask.class );
                ValidationFindings findings = null;
                Log log = getLog();

                compilerTask.applyTaskOptions( this );
                compilerTask.setRepositoryManager( repositoryManager );

                if (libraryFile != null) {
                    log.info( "Compiling OTA2 Library: " + libraryFile.getName() );
                    findings = compilerTask.compileOutput( libraryFile );

                } else if (releaseItem != null) {
                    log.info( "Compiling OTA2 Release: " + releaseItem.getFilename() );
                    findings = compilerTask.compileOutput( releaseItem );
                }
                displayValidationFindings( findings );

            } catch (Exception e) {
                throw new MojoExecutionException( "Error during OTA2 library compilation.", e );
            }
        }
    }

    /**
     * Displays the validation findings and throws an exception if any errors exist.
     * 
     * @param findings the validation findings to display
     * @throws MojoFailureException thrown if one or more errors exist
     */
    private void displayValidationFindings(ValidationFindings findings) throws MojoFailureException {
        Log log = getLog();

        if (findings != null) {
            if (findings.hasFinding()) {
                String[] messages = findings.getAllValidationMessages( FindingMessageFormat.IDENTIFIED_FORMAT );

                log.info( "Errors/warnings detected during compilation:" );
                for (String message : messages) {
                    log.info( message );
                }
            }

            if (!findings.hasFinding( FindingType.ERROR )) {
                log.info( "Library compilation completed successfully." );
            } else {
                throw new MojoFailureException( "Schema compilation aborted due to errors." );
            }
        }
    }

    /**
     * Creates the output folder specified in the plugin configuration. If the folder does not exist and cannot be
     * created (or is a file), an exception will be thrown.
     * 
     * @throws IOException thrown if the folder is not valid or cannot be created
     */
    private void createOutputFolder() throws IOException {
        if (outputFolder.exists()) {
            if (!outputFolder.isDirectory()) {
                throw new IOException(
                    "The specified output folder is not a directory: " + outputFolder.getAbsolutePath() );
            }

        } else if (!outputFolder.mkdirs()) {
            throw new IOException( "Unable to create ouput folder: " + outputFolder.getAbsolutePath() );
        }
    }

    /**
     * Returns the repository item for the release to be loaded.
     * 
     * @return RepositoryItem
     * @throws MojoFailureException thrown if the specified release does not exist
     * @throws MojoExecutionException thrown if an error occurs while accessing the OTM repository
     */
    private RepositoryItem getReleaseItem() throws MojoFailureException, MojoExecutionException {
        RepositoryItem releaseItem;
        try {
            releaseItem = repositoryManager.getRepositoryItem( release.getBaseNamespace(), release.getFilename(),
                release.getVersion() );

            if (!RepositoryItemType.RELEASE.isItemType( releaseItem.getFilename() )) {
                throw new RepositoryException(
                    "The specified repository item is not an OTM release: " + releaseItem.getFilename() );
            }

        } catch (RepositoryException e) {
            throw new MojoFailureException( "The specified repository item does not exist or is not an OTM release.",
                e );

        } catch (Exception e) {
            throw new MojoExecutionException( "Unknown error while accessing the OTM repository", e );
        }
        return releaseItem;
    }

    /**
     * Displays the configuration options for this Mojo for debugging purposes.
     */
    protected void displayOptions() {
        Log log = getLog();

        log.info(
            "libraryFile                   = " + ((libraryFile == null) ? NULL_VALUE : libraryFile.getAbsolutePath()) );
        log.info( "catalog                       = " + ((catalog == null) ? NULL_VALUE : catalog.getAbsolutePath()) );
        log.info( "outputFolder                  = "
            + ((outputFolder == null) ? NULL_VALUE : outputFolder.getAbsolutePath()) );
        log.info( "bindingStyle                  = " + bindingStyle );
        log.info( "compileSchemas                = " + compileSchemas );
        log.info( "compileJson                   = " + compileJson );
        log.info( "compileServices               = " + compileServices );
        log.info( "compileSwagger                = " + compileSwagger );
        log.info( "generateExamples              = " + generateExamples );
        log.info( "serviceEndpointUrl            = " + serviceEndpointUrl );
        log.info( "resourceBaseUrl               = " + resourceBaseUrl );
        log.info( "generateMaxDetailsForExamples = " + generateMaxDetailsForExamples );
        log.info( "exampleContext                = " + exampleContext );
        log.info( "exampleMaxRepeat              = " + exampleMaxRepeat );
        log.info( "exampleMaxDepth               = " + exampleMaxDepth );
        log.info( "suppressOptionalFields        = " + suppressOptionalFields );
    }

    /**
     * Initializes the repository manager to be used by this mojo. If null, the default manager instance will be used.
     * 
     * @param repositoryManager the repository manager instance (null to use default)
     * @throws RepositoryException thrown if the default instance cannot be initialized
     */
    protected void initRepositoryManager(RepositoryManager repositoryManager) throws RepositoryException {
        if (this.repositoryManager == null) {
            if (repositoryManager == null) {
                this.repositoryManager = RepositoryManager.getDefault();

            } else {
                this.repositoryManager = repositoryManager;
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileSchemas()
     */
    @Override
    public boolean isCompileSchemas() {
        return compileSchemas;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileJsonSchemas()
     */
    @Override
    public boolean isCompileJsonSchemas() {
        return compileJson;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileServices()
     */
    @Override
    public boolean isCompileServices() {
        return compileServices;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileHtml()
     */
    @Override
    public boolean isCompileHtml() {
        return compileHtml;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileSwagger()
     */
    @Override
    public boolean isCompileSwagger() {
        return compileSwagger;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#getCatalogLocation()
     */
    @Override
    public String getCatalogLocation() {
        return (catalog == null) ? null : catalog.getAbsolutePath();
    }

    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#getOutputFolder()
     */
    @Override
    public String getOutputFolder() {
        return (outputFolder == null) ? null : outputFolder.getAbsolutePath();
    }

    /**
     * @see org.opentravel.schemacompiler.task.ServiceCompilerTaskOptions#getServiceLibraryUrl()
     */
    @Override
    public URL getServiceLibraryUrl() {
        return (compileServices && !compileSchemas) ? URLUtils.toURL( libraryFile ) : null;
    }

    /**
     * @see org.opentravel.schemacompiler.task.ServiceCompilerTaskOptions#getServiceEndpointUrl()
     */
    @Override
    public String getServiceEndpointUrl() {
        return serviceEndpointUrl;
    }

    /**
     * @see org.opentravel.schemacompiler.task.ResourceCompilerTaskOptions#getResourceBaseUrl()
     */
    @Override
    public String getResourceBaseUrl() {
        return resourceBaseUrl;
    }

    /**
     * @see org.opentravel.schemacompiler.task.ResourceCompilerTaskOptions#isSuppressOtmExtensions()
     */
    @Override
    public boolean isSuppressOtmExtensions() {
        return suppressOtmExtensions;
    }

    /**
     * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#isGenerateExamples()
     */
    @Override
    public boolean isGenerateExamples() {
        return generateExamples;
    }

    /**
     * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#isGenerateMaxDetailsForExamples()
     */
    @Override
    public boolean isGenerateMaxDetailsForExamples() {
        return generateMaxDetailsForExamples;
    }

    /**
     * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#getExampleContext()
     */
    @Override
    public String getExampleContext() {
        return exampleContext;
    }

    /**
     * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#getExampleMaxRepeat()
     */
    @Override
    public Integer getExampleMaxRepeat() {
        return exampleMaxRepeat;
    }

    /**
     * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#getExampleMaxDepth()
     */
    @Override
    public Integer getExampleMaxDepth() {
        return exampleMaxDepth;
    }

    /**
     * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#isSuppressOptionalFields()
     */
    @Override
    public boolean isSuppressOptionalFields() {
        return suppressOptionalFields;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#applyTaskOptions(org.opentravel.schemacompiler.task.CommonCompilerTaskOptions)
     */
    @Override
    public void applyTaskOptions(CommonCompilerTaskOptions taskOptions) {
        throw new UnsupportedOperationException();
    }

    /**
     * Initializes the default extension for the schema compiler.
     */
    static {
        try {
            // Force the load of the extension registry class and the initialization of the default
            // compiler extension (as determined by the local configuration file).
            CompilerExtensionRegistry.getActiveExtension();

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

    /**
     * Since this is a read-only application, enable the OTM 1.6 file format for all operations.
     */
    static {
        OTM16Upgrade.otm16Enabled = true;
    }

}

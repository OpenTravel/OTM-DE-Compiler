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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.task.CommonCompilerTaskOptions;
import org.opentravel.schemacompiler.task.CompileAllCompilerTask;
import org.opentravel.schemacompiler.task.CompileAllTaskOptions;
import org.opentravel.schemacompiler.task.TaskFactory;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Maven Mojo that handles the compilation of files from the OTA2 library format into XML schemas
 * and WSDL documents.
 * 
 * @goal ota2-compile
 * @phase generate-sources
 * @threadSafe
 * @author S. Livezey
 */
public class OTA2SchemaCompilerMojo extends AbstractMojo implements CompileAllTaskOptions {

    /**
     * The location of the library file to be compiled.
     * 
     * @parameter
     * @required
     */
    protected File libraryFile;

    /**
     * The location of the library catalog file.
     * 
     * @parameter
     */
    protected File catalog;

    /**
     * The output folder location for generated files (default location is
     * &lt;project&gt;/target/generated-sources/ota2).
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/ota2"
     */
    protected File outputFolder;

    /**
     * The binding style for generated schemas and services (default is 'OTA2').
     * 
     * @parameter
     */
    protected String bindingStyle;

    /**
     * Boolean flag indicating that XML schema files should be generated.
     * 
     * @parameter default-value="true"
     */
    protected boolean compileSchemas;

    /**
     * Boolean flag indicating that WSDL schema files should be generated.
     * 
     * @parameter default-value="true"
     */
    protected boolean compileServices;

    /**
     * Boolean flag indicating that example data files should be generated.
     * 
     * @parameter default-value="true"
     */
    protected boolean generateExamples;

    /**
     * The URL of the root service endpoint for WSDL services.
     * 
     * @parameter
     */
    protected String serviceEndpointUrl;

    /**
     * Boolean flag indicating that the maximum amount of detail is to be included in generated
     * example data. If false, minimum detail will be generated.
     * 
     * @parameter default-value="true"
     */
    protected boolean generateMaxDetailsForExamples;

    /**
     * The preferred context ID to use when producing example values for simple data types.
     * 
     * @parameter
     */
    protected String exampleContext;

    /**
     * The maximum number of times that repeating elements should be displayed in generated example
     * output.
     * 
     * @parameter
     */
    protected Integer exampleMaxRepeat;

    /**
     * The maximum depth that should be included for nested elements in generated example output.
     * 
     * @parameter
     */
    protected Integer exampleMaxDepth;

    /**
     * Flag used for debugging that causes this Mojo's configuration settings to be displayed prior
     * to execution.
     * 
     * @parameter default-value="false"
     */
    protected boolean debug;

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        synchronized (OTA2SchemaCompilerMojo.class) {
            try {
                if (debug)
                    displayOptions();

                // Validate the source file and output folder
                if (!libraryFile.exists()) {
                    throw new FileNotFoundException("Source file not found: "
                            + libraryFile.getAbsolutePath());
                }
                if (!outputFolder.exists()) {
                    if (!outputFolder.mkdirs()) {
                        throw new IOException("Unable to create ouput folder: "
                                + outputFolder.getAbsolutePath());
                    }
                }

                // Select the user-specified schema compiler extension
                if (bindingStyle != null) {
                    if (CompilerExtensionRegistry.getAvailableExtensionIds().contains(bindingStyle)) {
                        CompilerExtensionRegistry.setActiveExtension(bindingStyle);

                    } else {
                        throw new MojoFailureException("Invalid binding style specified: "
                                + bindingStyle);
                    }
                }

                // Execute the compilation and return
                CompileAllCompilerTask compilerTask = TaskFactory
                        .getTask(CompileAllCompilerTask.class);
                Log log = getLog();

                log.info("Compiling OTA2 Library: " + libraryFile.getName());
                compilerTask.applyTaskOptions(this);

                ValidationFindings findings = compilerTask.compileOutput(libraryFile);

                if (findings.hasFinding()) {
                    String[] messages = findings
                            .getAllValidationMessages(FindingMessageFormat.IDENTIFIED_FORMAT);

                    log.info("Errors/warnings detected during compilation:");
                    for (String message : messages) {
                        log.info(message);
                    }
                }
                if (!findings.hasFinding(FindingType.ERROR)) {
                    log.info("Library compilation completed successfully.");
                } else {
                    throw new MojoFailureException("Schema compilation aborted due to errors.");
                }
            } catch (Throwable t) {
                throw new MojoExecutionException("Error during OTA2 library compilation.", t);
            }
        }
    }

    /**
     * Displays the configuration options for this Mojo for debugging purposes.
     */
    protected void displayOptions() {
        Log log = getLog();

        log.info("libraryFile                   = "
                + ((libraryFile == null) ? "[NULL]" : libraryFile.getAbsolutePath()));
        log.info("catalog                       = "
                + ((catalog == null) ? "[NULL]" : catalog.getAbsolutePath()));
        log.info("outputFolder                  = "
                + ((outputFolder == null) ? "[NULL]" : outputFolder.getAbsolutePath()));
        log.info("bindingStyle                  = " + bindingStyle);
        log.info("compileSchemas                = " + compileSchemas);
        log.info("compileServices               = " + compileServices);
        log.info("generateExamples              = " + generateExamples);
        log.info("serviceEndpointUrl            = " + serviceEndpointUrl);
        log.info("generateMaxDetailsForExamples = " + generateMaxDetailsForExamples);
        log.info("exampleContext                = " + exampleContext);
        log.info("exampleMaxRepeat              = " + exampleMaxRepeat);
        log.info("exampleMaxDepth               = " + exampleMaxDepth);
    }

    /**
     * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileSchemas()
     */
    @Override
    public boolean isCompileSchemas() {
        return compileSchemas;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileServices()
     */
    @Override
    public boolean isCompileServices() {
        return compileServices;
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
        return (compileServices && !compileSchemas) ? URLUtils.toURL(libraryFile) : null;
    }

    /**
     * @see org.opentravel.schemacompiler.task.ServiceCompilerTaskOptions#getServiceEndpointUrl()
     */
    @Override
    public String getServiceEndpointUrl() {
        return serviceEndpointUrl;
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

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}

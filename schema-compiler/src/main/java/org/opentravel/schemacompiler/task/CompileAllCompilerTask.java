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
import java.net.URL;
import java.util.Collection;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.util.SchemaCompilerException;

/**
 * Task used to orchestrate the execution of up to three separate compilation tasks: full library
 * schemas, service (WSDL) documents, and resource action service (WSDL) documents.
 * 
 * @author S. Livezey
 */
public class CompileAllCompilerTask extends AbstractCompilerTask implements CompileAllTaskOptions {

    private boolean compileSchemas = true;
    private boolean compileServices = true;
    private URL serviceLibraryUrl;
    private String serviceEndpointUrl;
    private boolean generateExamples = true;
    private boolean generateMaxDetailsForExamples = true;
    private String exampleContext;
    private Integer exampleMaxRepeat;
    private Integer exampleMaxDepth;

    /**
     * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#generateOutput(java.util.Collection,
     *      java.util.Collection)
     */
    @Override
    protected void generateOutput(Collection<TLLibrary> userDefinedLibraries,
            Collection<XSDLibrary> legacySchemas) throws SchemaCompilerException {
        CodeGenerationContext compileAllContext = createContext();

        if (compileSchemas) {
            SchemaCompilerTask schemaTask = new SchemaCompilerTask(projectFilename);

            schemaTask.applyTaskOptions(this);
            schemaTask.setOutputFolder(getSubtaskOutputFolder(compileAllContext, "schemas"));
            schemaTask.generateOutput(userDefinedLibraries, legacySchemas);
            addGeneratedFiles(schemaTask.getGeneratedFiles());
        }
        if (compileServices) {
            if (projectFilename != null) {
                ServiceProjectCompilerTask serviceTask = new ServiceProjectCompilerTask(
                        projectFilename);

                serviceTask.applyTaskOptions(this);
                serviceTask.setOutputFolder(getSubtaskOutputFolder(compileAllContext, "services"));
                serviceTask.generateOutput(userDefinedLibraries, legacySchemas);
                addGeneratedFiles(serviceTask.getGeneratedFiles());

            } else { // non-project service compilation
                ServiceCompilerTask serviceTask = new ServiceCompilerTask();

                serviceTask.applyTaskOptions(this);
                serviceTask.setOutputFolder(getSubtaskOutputFolder(compileAllContext, "services"));
                serviceTask.generateOutput(userDefinedLibraries, legacySchemas);
                addGeneratedFiles(serviceTask.getGeneratedFiles());
            }
        }
    }

    /**
     * Returns the path of a sub-folder within the given context's output folder location.
     * 
     * @param context
     *            the code generation context
     * @param subtaskFolder
     *            the location where all output should be created for the sub-task
     * @return String
     */
    private String getSubtaskOutputFolder(CodeGenerationContext context, String subtaskFolder) {
        String rootOutputFolder = context.getValue(CodeGenerationContext.CK_OUTPUT_FOLDER);

        if (rootOutputFolder == null) {
            rootOutputFolder = System.getProperty("user.dir");
        }
        return new File(rootOutputFolder, subtaskFolder).getAbsolutePath();
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#createContext()
     */
    protected CodeGenerationContext createContext() {
        CodeGenerationContext context = super.createContext();

        if (serviceEndpointUrl != null) {
            context.setValue(CodeGenerationContext.CK_SERVICE_ENDPOINT_URL, serviceEndpointUrl);
        }
        if (!generateMaxDetailsForExamples) {
            context.setValue(CodeGenerationContext.CK_EXAMPLE_DETAIL_LEVEL, "MINIMUM");
        }
        if (exampleContext != null) {
            context.setValue(CodeGenerationContext.CK_EXAMPLE_CONTEXT, exampleContext);
        }
        if (exampleMaxRepeat != null) {
            context.setValue(CodeGenerationContext.CK_EXAMPLE_CONTEXT, exampleMaxRepeat.toString());
        }
        if (exampleMaxDepth != null) {
            context.setValue(CodeGenerationContext.CK_EXAMPLE_CONTEXT, exampleMaxDepth.toString());
        }
        return context;
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#applyTaskOptions(org.opentravel.schemacompiler.task.CommonCompilerTaskOptions)
     */
    @Override
    public void applyTaskOptions(CommonCompilerTaskOptions taskOptions) {
        if (taskOptions instanceof CompileAllTaskOptions) {
            CompileAllTaskOptions compileAllOptions = (CompileAllTaskOptions) taskOptions;

            setCompileSchemas(compileAllOptions.isCompileSchemas());
            setCompileServices(compileAllOptions.isCompileServices());
        }
        if (taskOptions instanceof SchemaCompilerTaskOptions) {
            // No explicit options currently implemented
        }
        if (taskOptions instanceof ExampleCompilerTaskOptions) {
            ExampleCompilerTaskOptions exampleOptions = (ExampleCompilerTaskOptions) taskOptions;

            setGenerateExamples(exampleOptions.isGenerateExamples());
            setGenerateMaxDetailsForExamples(exampleOptions.isGenerateMaxDetailsForExamples());
            setExampleContext(exampleOptions.getExampleContext());
            setExampleMaxRepeat(exampleOptions.getExampleMaxRepeat());
            setExampleMaxDepth(exampleOptions.getExampleMaxDepth());
        }
        if (taskOptions instanceof ServiceCompilerTaskOptions) {
            ServiceCompilerTaskOptions serviceOptions = (ServiceCompilerTaskOptions) taskOptions;

            setServiceLibraryUrl(serviceOptions.getServiceLibraryUrl());
            setServiceEndpointUrl(serviceOptions.getServiceEndpointUrl());
        }
        super.applyTaskOptions(taskOptions);
    }

    /**
     * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileSchemas()
     */
    @Override
    public boolean isCompileSchemas() {
        return compileSchemas;
    }

    /**
     * Assigns the option flag indicating that XML schema files should be generated.
     * 
     * @param compileSchemas
     *            the task option value to assign
     */
    public void setCompileSchemas(boolean compileSchemas) {
        this.compileSchemas = compileSchemas;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileServices()
     */
    @Override
    public boolean isCompileServices() {
        return compileServices;
    }

    /**
     * Assigns the option flag indicating that WSDL documents should be generated.
     * 
     * @param compileServices
     *            the task option value to assign
     */
    public void setCompileServices(boolean compileServices) {
        this.compileServices = compileServices;
    }

    /**
     * @see org.opentravel.schemacompiler.task.ServiceCompilerTaskOptions#getServiceLibraryUrl()
     */
    @Override
    public URL getServiceLibraryUrl() {
        return serviceLibraryUrl;
    }

    /**
     * Assigns the URL of the OTM library that contains the single service to be generated. If
     * present, only that service's WSDL will be generated. If not present, WSDL's will be generated
     * for all services that exist in the OTM model being processed.
     * 
     * @param serviceLibraryUrl
     *            the service endpoint URL to assign
     */
    public void setServiceLibraryUrl(URL serviceLibraryUrl) {
        this.serviceLibraryUrl = serviceLibraryUrl;
    }

    /**
     * @see org.opentravel.schemacompiler.task.ServiceCompilerTaskOptions#getServiceEndpointUrl()
     */
    @Override
    public String getServiceEndpointUrl() {
        return serviceEndpointUrl;
    }

    /**
     * Assigns the base URL for all service endpoints generated in WSDL documents.
     * 
     * @param serviceEndpointUrl
     *            the service endpoint URL to assign
     */
    public void setServiceEndpointUrl(String serviceEndpointUrl) {
        this.serviceEndpointUrl = serviceEndpointUrl;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#isGenerateExamples()
     */
    @Override
    public boolean isGenerateExamples() {
        return generateExamples;
    }

    /**
     * Assigns the option flag indicating that example XML documents should be generated.
     * 
     * @param compileRAS
     *            the task option value to assign
     */
    public void setGenerateExamples(boolean generateExamples) {
        this.generateExamples = generateExamples;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#isGenerateMaxDetailsForExamples()
     */
    @Override
    public boolean isGenerateMaxDetailsForExamples() {
        return generateMaxDetailsForExamples;
    }

    /**
     * Assigns the flag indicating whether the maximum amount of detail is to be included in
     * generated example data. If false, minimum detail will be generated.
     * 
     * @param generateMaxDetailsForExamples
     *            the boolean flag value to assign
     */
    public void setGenerateMaxDetailsForExamples(boolean generateMaxDetailsForExamples) {
        this.generateMaxDetailsForExamples = generateMaxDetailsForExamples;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#getExampleContext()
     */
    @Override
    public String getExampleContext() {
        return exampleContext;
    }

    /**
     * Assigns the preferred context to use when producing example values for simple data types.
     * 
     * @param exampleContext
     *            the context ID to assign
     */
    public void setExampleContext(String exampleContext) {
        this.exampleContext = exampleContext;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#getExampleMaxRepeat()
     */
    @Override
    public Integer getExampleMaxRepeat() {
        return exampleMaxRepeat;
    }

    /**
     * Assigns the maximum number of times that repeating elements should be displayed in generated
     * example output.
     * 
     * @param exampleMaxRepeat
     *            the max repeat value to assign
     */
    public void setExampleMaxRepeat(Integer exampleMaxRepeat) {
        this.exampleMaxRepeat = exampleMaxRepeat;
    }

    /**
     * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#getExampleMaxDepth()
     */
    @Override
    public Integer getExampleMaxDepth() {
        return exampleMaxDepth;
    }

    /**
     * Assigns the maximum depth that should be included for nested elements in generated example
     * output.
     * 
     * @param exampleMaxDepth
     *            the max depth value to assign
     */
    public void setExampleMaxDepth(Integer exampleMaxDepth) {
        this.exampleMaxDepth = exampleMaxDepth;
    }

}

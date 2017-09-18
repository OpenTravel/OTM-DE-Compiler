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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.DefaultCodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.impl.DependencyFilterBuilder;
import org.opentravel.schemacompiler.codegen.impl.LibraryMemberTrimmedFilenameBuilder;
import org.opentravel.schemacompiler.codegen.impl.LibraryTrimmedFilenameBuilder;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.util.SchemaCompilerException;

/**
 * Compiler task used to generate WSDL documents for the services defined in a project, as well as
 * the trimmed schema (XSD) files that contain the entities upon which those services depend. If
 * multiple services are defined in the project's libraries, WSDL's will be generated for all of
 * those services in the same output directory, and the trimmed schemas will be a union of the
 * dependencies from all of the compiled services.
 * 
 * @author S. Livezey
 */
public class ServiceProjectCompilerTask extends AbstractSchemaCompilerTask implements
        ServiceCompilerTaskOptions {

    private String serviceEndpointUrl;

    /**
     * Constructor that specifies the filename of the project for which schemas are being compiled.
     * 
     * @param projectFilename  the name of the project (.otp) file
     */
    public ServiceProjectCompilerTask(String projectFilename) {
        this.projectFilename = projectFilename;
    }

    /**
     * Constructor that specifies the filename of the project for which schemas are being compiled.
     * 
     * @param projectFilename  the name of the project (.otp) file
     * @param repositoryManager  the repository manager to use when retrieving managed content
     */
    public ServiceProjectCompilerTask(String projectFilename, RepositoryManager repositoryManager) {
    	super( repositoryManager );
        this.projectFilename = projectFilename;
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#generateOutput(java.util.Collection,
     *      java.util.Collection)
     */
    @Override
    protected void generateOutput(Collection<TLLibrary> userDefinedLibraries,
            Collection<XSDLibrary> legacySchemas) throws SchemaCompilerException {
        Map<String, Boolean> duplicateServiceNameIndicators = new HashMap<String, Boolean>();
        DependencyFilterBuilder filterBuilder = new DependencyFilterBuilder().setIncludeExtendedLegacySchemas( true );
        List<TLService> serviceList = new ArrayList<TLService>();

        // Collect the list of services to generate and identify the services that will require
        // version identification as part of their WSDL filename.
        for (TLLibrary library : userDefinedLibraries) {
            TLService service = library.getService();

            if (service != null) {
                String serviceName = service.getName();

                if (duplicateServiceNameIndicators.containsKey(serviceName)) {
                    duplicateServiceNameIndicators.put(serviceName, Boolean.TRUE);

                } else {
                    duplicateServiceNameIndicators.put(serviceName, Boolean.FALSE);
                }
                filterBuilder.addLibraryMember(service);
                serviceList.add(service);
            }
        }

        // Generate the WSDL documents for each service
        CodeGenerationContext context = createContext();

        for (TLService service : serviceList) {
            boolean duplicateName = duplicateServiceNameIndicators.get(service.getName());

            CodeGenerator<TLService> serviceWsdlGenerator = CodeGeneratorFactory.getInstance()
                    .newCodeGenerator(CodeGeneratorFactory.WSDL_TARGET_FORMAT, TLService.class);

            serviceWsdlGenerator
                    .setFilenameBuilder(new LibraryMemberTrimmedFilenameBuilder<TLService>(service,
                            duplicateName));
            addGeneratedFiles(serviceWsdlGenerator.generateOutput(service, context));

            // Generate example files if required; examples are only created for the operation
            // messages (not the contents of the trimmed schemas)
            if (isGenerateExamples()) {
                generateExampleArtifacts(userDefinedLibraries, context,
                        new LibraryTrimmedFilenameBuilder(null), createExampleFilter(service),
                        CodeGeneratorFactory.XML_TARGET_FORMAT);
            }
        }

        // Generate the trimmed schemas for all of the services as a union of their dependencies
        if (!serviceList.isEmpty()) {
            compileXmlSchemas(userDefinedLibraries, legacySchemas, context,
                    new LibraryTrimmedFilenameBuilder(null), filterBuilder.buildFilter());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractSchemaCompilerTask#getExampleOutputFolder(org.opentravel.schemacompiler.model.LibraryMember,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected String getExampleOutputFolder(LibraryMember libraryMember,
            CodeGenerationContext context) {
        String rootOutputFolder = context.getValue(CodeGenerationContext.CK_OUTPUT_FOLDER);

        if (rootOutputFolder == null) {
            rootOutputFolder = System.getProperty("user.dir");
        }
        return new File(rootOutputFolder, "examples").getAbsolutePath();
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractSchemaCompilerTask#getSchemaRelativeFolderPath(org.opentravel.schemacompiler.model.LibraryMember,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected String getSchemaRelativeFolderPath(LibraryMember libraryMember,
            CodeGenerationContext context) {
        return "../";
    }

    /**
     * Returns a <code>CodeGenerationFilter</code> that only includes the service, its operations,
     * and their facets. None of the other dependent elements from the supporting library schemas
     * are included.
     * 
     * @param service
     *            the service for which to create a filter
     * @return CodeGenerationFilter
     */
    protected CodeGenerationFilter createExampleFilter(TLService service) {
        DefaultCodeGenerationFilter filter = new DefaultCodeGenerationFilter();

        for (TLOperation operation : service.getOperations()) {
            if (operation.getRequest().declaresContent()) {
                filter.addProcessedElement(operation.getRequest());
            }
            if (operation.getResponse().declaresContent()) {
                filter.addProcessedElement(operation.getResponse());
            }
            if (operation.getNotification().declaresContent()) {
                filter.addProcessedElement(operation.getNotification());
            }
            filter.addProcessedElement(operation);
        }
        filter.addProcessedElement(service);
        filter.addProcessedLibrary(service.getOwningLibrary());
        return filter;
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#createContext()
     */
    protected CodeGenerationContext createContext() {
        CodeGenerationContext context = super.createContext();

        if (serviceEndpointUrl != null) {
            context.setValue(CodeGenerationContext.CK_SERVICE_ENDPOINT_URL, serviceEndpointUrl);
        }
        return context;
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#applyTaskOptions(org.opentravel.schemacompiler.task.CommonCompilerTaskOptions)
     */
    @Override
    public void applyTaskOptions(CommonCompilerTaskOptions taskOptions) {
        if (taskOptions instanceof ServiceCompilerTaskOptions) {
            ServiceCompilerTaskOptions serviceOptions = (ServiceCompilerTaskOptions) taskOptions;

            setServiceEndpointUrl(serviceOptions.getServiceEndpointUrl());
        }
        super.applyTaskOptions(taskOptions);
    }

    /**
     * @see org.opentravel.schemacompiler.task.ServiceCompilerTaskOptions#getServiceLibraryUrl()
     */
    @Override
    public URL getServiceLibraryUrl() {
        return null;
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

}

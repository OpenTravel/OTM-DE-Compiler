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

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.DefaultCodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.impl.DependencyFilterBuilder;
import org.opentravel.schemacompiler.codegen.impl.LibraryTrimmedFilenameBuilder;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * Compiler task used to generate WSDL documents for the services defined in a model, as well as the trimmed schema
 * (XSD) files that contain the entities upon which those services depend.
 * 
 * @author S. Livezey
 */
public class ServiceCompilerTask extends AbstractSchemaCompilerTask implements ServiceCompilerTaskOptions {

    private URL serviceLibraryUrl;
    private String serviceEndpointUrl;

    /**
     * Default constructor.
     */
    public ServiceCompilerTask() {}

    /**
     * Constructor that assigns the repository manager for this task instance.
     * 
     * @param repositoryManager the repository manager to use when retrieving managed content
     */
    public ServiceCompilerTask(RepositoryManager repositoryManager) {
        super( repositoryManager );
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#generateOutput(java.util.Collection,
     *      java.util.Collection)
     */
    @Override
    protected void generateOutput(Collection<TLLibrary> userDefinedLibraries, Collection<XSDLibrary> legacySchemas)
        throws SchemaCompilerException {
        CodeGenerationContext modelContext = createContext();
        CodeGenerationContext serviceContext = modelContext.getCopy();
        URL selectedLibraryUrl = getServiceLibraryUrl();

        for (TLLibrary library : userDefinedLibraries) {
            boolean skip = false;

            try {
                if ((selectedLibraryUrl != null)
                    && !selectedLibraryUrl.toURI().equals( library.getLibraryUrl().toURI() )) {
                    skip = true;
                }

            } catch (URISyntaxException e) {
                skip = true;
            }
            if (skip) {
                continue;
            }

            TLService service = library.getService();

            if ((service != null) && isLatestServiceVersion( service )) {
                CodeGenerationFilter filter = new DependencyFilterBuilder().setIncludeExtendedLegacySchemas( true )
                    .addLibraryMember( service ).buildFilter();
                serviceContext.setValue( CodeGenerationContext.CK_OUTPUT_FOLDER,
                    getServiceOutputFolder( service, modelContext ) );

                // Generate the WSDL document
                CodeGenerator<TLService> serviceWsdlGenerator = CodeGeneratorFactory.getInstance()
                    .newCodeGenerator( CodeGeneratorFactory.WSDL_TARGET_FORMAT, TLService.class );
                addGeneratedFiles( serviceWsdlGenerator.generateOutput( service, serviceContext ) );

                // Generate the trimmed XML schema documents for the service
                compileXmlSchemas( userDefinedLibraries, legacySchemas, serviceContext,
                    new LibraryTrimmedFilenameBuilder( service ), filter );

                // Generate EXAMPLE files if required; examples are only created
                // for the operation messages (not the contents of the trimmed schemas)
                if (isGenerateExamples()) {
                    generateExampleArtifacts( userDefinedLibraries, serviceContext,
                        new LibraryTrimmedFilenameBuilder( service ), createExampleFilter( service ),
                        CodeGeneratorFactory.XML_TARGET_FORMAT );
                }
            }
        }
    }

    /**
     * Returns the location of the output folder for the specified service.
     * 
     * @param service the service for which the EXAMPLE output folder is needed
     * @param context the code generation context
     * @return String
     * @throws SchemaCompilerException thrown if an error occurs due to an unrecognized version scheme
     */
    protected String getServiceOutputFolder(TLService service, CodeGenerationContext context)
        throws SchemaCompilerException {
        String rootOutputFolder = context.getValue( CodeGenerationContext.CK_OUTPUT_FOLDER );
        String outputFolder;

        if (rootOutputFolder == null) {
            rootOutputFolder = System.getProperty( "user.dir" );
        }

        // Unless a single library has been selected for output generation, put each service in
        // its own individual folder
        if (getServiceLibraryUrl() == null) {
            String serviceVersion = getMajorVersion( service );
            String serviceFolder = service.getName() + "_v" + serviceVersion.replaceAll( "\\.", "_" );

            outputFolder = new File( rootOutputFolder, serviceFolder ).getAbsolutePath();

        } else {
            outputFolder = rootOutputFolder;
        }
        return outputFolder;
    }

    /**
     * Returns true if the given service instance is the latest version of that service.
     * 
     * @param service the service instance to analyze
     * @return boolean
     * @throws SchemaCompilerException thrown if an error occurs due to an unrecognized version scheme
     */
    private boolean isLatestServiceVersion(TLService service) throws SchemaCompilerException {
        try {
            TLLibrary owningLibrary = (TLLibrary) service.getOwningLibrary();
            List<TLLibrary> laterMinorVersions = new MinorVersionHelper().getLaterMinorVersions( owningLibrary );
            boolean isLatestVersion = true;

            for (TLLibrary laterVersion : laterMinorVersions) {
                if (laterVersion.getService() != null) {
                    isLatestVersion = false;
                    break;
                }
            }
            return isLatestVersion;

        } catch (VersionSchemeException e) {
            throw new SchemaCompilerException( e );
        }
    }

    /**
     * Returns the major version number of the owning library for the given service.
     * 
     * @param service the service for which to return the major version
     * @return String
     * @throws SchemaCompilerException thrown if an error occurs due to an unrecognized version scheme
     */
    private String getMajorVersion(TLService service) throws SchemaCompilerException {
        try {
            TLLibrary owningLibrary = (TLLibrary) service.getOwningLibrary();
            VersionScheme vScheme =
                VersionSchemeFactory.getInstance().getVersionScheme( owningLibrary.getVersionScheme() );

            return vScheme.getMajorVersion( owningLibrary.getVersion() );

        } catch (VersionSchemeException e) {
            throw new SchemaCompilerException( e );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractSchemaCompilerTask#getExampleOutputFolder(org.opentravel.schemacompiler.model.LibraryMember,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected String getExampleOutputFolder(LibraryMember libraryMember, CodeGenerationContext context) {
        String rootOutputFolder = context.getValue( CodeGenerationContext.CK_OUTPUT_FOLDER );

        if (rootOutputFolder == null) {
            rootOutputFolder = System.getProperty( "user.dir" );
        }
        return new File( rootOutputFolder, "examples" ).getAbsolutePath();
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractSchemaCompilerTask#getSchemaRelativeFolderPath(org.opentravel.schemacompiler.model.LibraryMember,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected String getSchemaRelativeFolderPath(LibraryMember libraryMember, CodeGenerationContext context) {
        return "../";
    }

    /**
     * Returns a <code>CodeGenerationFilter</code> that only includes the service, its operations, and their facets.
     * None of the other dependent elements from the supporting library schemas are included.
     * 
     * @param service the service for which to create a filter
     * @return CodeGenerationFilter
     */
    protected CodeGenerationFilter createExampleFilter(TLService service) {
        DefaultCodeGenerationFilter filter = new DefaultCodeGenerationFilter();

        for (TLOperation operation : service.getOperations()) {
            if (operation.getRequest().declaresContent()) {
                filter.addProcessedElement( operation.getRequest() );
            }
            if (operation.getResponse().declaresContent()) {
                filter.addProcessedElement( operation.getResponse() );
            }
            if (operation.getNotification().declaresContent()) {
                filter.addProcessedElement( operation.getNotification() );
            }
            filter.addProcessedElement( operation );
        }
        filter.addProcessedElement( service );
        filter.addProcessedLibrary( service.getOwningLibrary() );
        return filter;
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#createContext()
     */
    @Override
    protected CodeGenerationContext createContext() {
        CodeGenerationContext context = super.createContext();

        if (serviceEndpointUrl != null) {
            context.setValue( CodeGenerationContext.CK_SERVICE_ENDPOINT_URL, serviceEndpointUrl );
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

            setServiceLibraryUrl( serviceOptions.getServiceLibraryUrl() );
            setServiceEndpointUrl( serviceOptions.getServiceEndpointUrl() );
        }
        super.applyTaskOptions( taskOptions );
    }

    /**
     * @see org.opentravel.schemacompiler.task.ServiceCompilerTaskOptions#getServiceLibraryUrl()
     */
    @Override
    public URL getServiceLibraryUrl() {
        return serviceLibraryUrl;
    }

    /**
     * Assigns the URL of the OTM library that contains the single service to be generated. If present, only that
     * service's WSDL will be generated. If not present, WSDL's will be generated for all services that exist in the OTM
     * model being processed.
     * 
     * @param serviceLibraryUrl the service endpoint URL to assign
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
     * @param serviceEndpointUrl the service endpoint URL to assign
     */
    public void setServiceEndpointUrl(String serviceEndpointUrl) {
        this.serviceEndpointUrl = serviceEndpointUrl;
    }

}

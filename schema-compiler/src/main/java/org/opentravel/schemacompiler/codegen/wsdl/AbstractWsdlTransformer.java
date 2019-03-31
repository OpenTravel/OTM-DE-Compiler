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

package org.opentravel.schemacompiler.codegen.wsdl;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodegenTransformer;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.springframework.context.ApplicationContext;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Import;
import org.w3._2001.xmlschema.Include;
import org.w3._2001.xmlschema.Schema;
import org.xmlsoap.schemas.wsdl.TBinding;
import org.xmlsoap.schemas.wsdl.TBindingOperation;
import org.xmlsoap.schemas.wsdl.TDefinitions;
import org.xmlsoap.schemas.wsdl.TDocumentation;
import org.xmlsoap.schemas.wsdl.TMessage;
import org.xmlsoap.schemas.wsdl.TOperation;
import org.xmlsoap.schemas.wsdl.TPortType;
import org.xmlsoap.schemas.wsdl.TService;
import org.xmlsoap.schemas.wsdl.TTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all <code>ObjectTransformer</code> implementations that are part of the WSDL code generation
 * subsystem.
 * 
 * @param <S> the source type of the object transformation
 * @param <T> the target type of the object transformation
 * @author S. Livezey
 */
public abstract class AbstractWsdlTransformer<S, T> extends AbstractCodegenTransformer<S,T> {

    protected static org.xmlsoap.schemas.wsdl.ObjectFactory wsdlObjectFactory =
        new org.xmlsoap.schemas.wsdl.ObjectFactory();

    protected MinorVersionHelper versionHelper = new MinorVersionHelper();
    protected CodeGenerationWsdlBindings wsdlBindings = null;

    /**
     * Default constructor.
     */
    public AbstractWsdlTransformer() {
        ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();

        if (appContext.containsBean( SchemaCompilerApplicationContext.CODE_GENERATION_WSDL_BINDINGS )) {
            wsdlBindings = (CodeGenerationWsdlBindings) appContext
                .getBean( SchemaCompilerApplicationContext.CODE_GENERATION_WSDL_BINDINGS );
        }
    }

    /**
     * Returns the <code>CodeGenerationWsdlBindings</code> component that will be used to generate binding elements for
     * the WSDL document.
     * 
     * @return CodeGenerationWsdlBindings
     */
    public CodeGenerationWsdlBindings getWsdlBindings() {
        return wsdlBindings;
    }

    /**
     * Creates the types section of the WSDL document with imports for each of the namespaces and schemas required to
     * define the RAS service messages.
     * 
     * @param source the business object being transformed
     * @param filenameBuilder the builder to use for creating filenames in generated output
     * @return TTypes
     */
    protected TTypes createTypes(LibraryMember source, CodeGenerationFilenameBuilder<LibraryMember> filenameBuilder) {
        String targetNamespace = getTargetNamespace( source );
        Schema schema = new Schema();
        TTypes types = new TTypes();

        schema.setTargetNamespace( targetNamespace );
        types.getAny().add( schema );

        // Add the application info for this library
        Annotation schemaAnnotation = new Annotation();

        schemaAnnotation.getAppinfoOrDocumentation()
            .add( XsdCodegenUtils.getServiceAppInfo( source, context.getCodegenContext() ) );
        schema.getIncludeOrImportOrRedefine().add( schemaAnnotation );

        // Add the required import/include references to dependent files
        if (source instanceof TLService) {
            for (TLService dependentService : getDependentServices( (TLService) source )) {
                if (dependentService.getNamespace().equals( targetNamespace )) {
                    schema.getIncludeOrImportOrRedefine()
                        .add( newInclude( filenameBuilder.buildFilename( dependentService, "xsd" ) ) );

                } else {
                    schema.getIncludeOrImportOrRedefine().add( newImport( dependentService.getNamespace(),
                        filenameBuilder.buildFilename( dependentService, "xsd" ) ) );
                }
                addWsdlDependency( dependentService.getOwningLibrary() );
            }
        } else {
            schema.getIncludeOrImportOrRedefine().add( newInclude( filenameBuilder.buildFilename( source, "xsd" ) ) );
        }

        if (wsdlBindings != null) {
            for (SchemaDeclaration schemaDeclaration : wsdlBindings.getDependentSchemas()) {
                schema.getIncludeOrImportOrRedefine().add( newImport( schemaDeclaration ) );
            }
        }
        schema.getIncludeOrImportOrRedefine().add( newImport( SchemaDeclarations.OTA2_APPINFO_SCHEMA ) );

        return types;
    }

    /**
     * Returns the target namespace of the WSDL document.
     * 
     * @param libraryMember the library member for which to return a target namespace
     * @return String
     */
    protected String getTargetNamespace(LibraryMember libraryMember) {
        String targetNamespace;
        try {
            TLLibrary owningLibrary = (TLLibrary) libraryMember.getOwningLibrary();
            VersionScheme vScheme =
                VersionSchemeFactory.getInstance().getVersionScheme( owningLibrary.getVersionScheme() );

            targetNamespace = vScheme.getMajorVersionNamespace( libraryMember.getNamespace() );

        } catch (VersionSchemeException e) {
            targetNamespace = libraryMember.getNamespace();
        }
        return targetNamespace;
    }

    /**
     * Returns a list of all operations that are defined by or inherited from previous versions of the given source
     * service.
     * 
     * <p>
     * NOTE: This method assumed that the given service is the latest version of the owning library that contains
     * service/operation content.
     * 
     * @param source the source service for which to return operations
     * @return List&lt;TLOperation&gt;
     */
    protected List<TLOperation> getInheritedOperations(TLService source) {
        // Identify the prior versions of the given service
        List<TLService> serviceVersions = new ArrayList<>();
        try {
            TLLibrary library = (TLLibrary) source.getOwningLibrary();

            while (library != null) {
                if (library.getService() != null) {
                    serviceVersions.add( 0, library.getService() );
                }
                library = versionHelper.getPriorMinorVersion( library );
            }

        } catch (VersionSchemeException e) {
            // Ignore - The 'serviceVersions' list will contain the given service as the
            // sole member of the list
        }

        // Construct a registry of serivce operations that are defined for each version
        Map<String,List<TLOperation>> operationRegistry = new HashMap<>();
        List<String> operationNames = new ArrayList<>();

        for (TLService serviceVersion : serviceVersions) {
            for (TLOperation operation : serviceVersion.getOperations()) {
                List<TLOperation> operationList = operationRegistry.get( operation.getName() );

                if (operationList == null) {
                    operationList = new ArrayList<>();
                    operationRegistry.put( operation.getName(), operationList );
                    operationNames.add( operation.getName() );
                }
                operationList.add( operation );
            }
        }

        // Assembly the resulting list in the order that each of the operations were encountered
        List<TLOperation> inheritedOperations = new ArrayList<>();

        for (String operationName : operationNames) {
            inheritedOperations.addAll( operationRegistry.get( operationName ) );
        }
        return inheritedOperations;
    }

    /**
     * Returns a list of dependent services (including the one provided) that must be included or imported due to
     * inherited operations from previous versions of the service.
     * 
     * @param service the service for which to return dependencies
     * @return List&lt;TLService&gt;
     */
    protected List<TLService> getDependentServices(TLService service) {
        List<TLService> serviceList = new ArrayList<>();

        for (TLOperation operation : getInheritedOperations( service )) {
            if (!serviceList.contains( operation.getOwningService() )) {
                serviceList.add( operation.getOwningService() );
            }
        }
        return serviceList;
    }

    /**
     * Factory method that constructs a new <code>Include</code> instance using the schema location provided.
     * 
     * @param schemaLocation the schema location of the file to import
     * @return Include
     */
    protected Include newInclude(String schemaLocation) {
        Include include = new Include();

        include.setSchemaLocation( schemaLocation );
        return include;
    }

    /**
     * Factory method that constructs a new <code>Import</code> instance using the information provided.
     * 
     * @param schemaDeclaration the schema declaration to be imported
     * @return Import
     */
    protected Import newImport(SchemaDeclaration schemaDeclaration) {
        String importFolder = getBuiltInSchemaOutputLocation();
        Import nsImport = new Import();

        nsImport.setNamespace( schemaDeclaration.getNamespace() );
        nsImport.setSchemaLocation(
            importFolder + schemaDeclaration.getFilename( CodeGeneratorFactory.XSD_TARGET_FORMAT ) );
        return nsImport;
    }

    /**
     * Factory method that constructs a new <code>Import</code> instance using the information provided.
     * 
     * @param namespace the namespace to be imported
     * @param schemaLocation the schema location of the file to import
     * @return Import
     */
    protected Import newImport(String namespace, String schemaLocation) {
        Import nsImport = new Import();

        nsImport.setNamespace( namespace );
        nsImport.setSchemaLocation( schemaLocation );
        return nsImport;
    }

    /**
     * Creates the WSDL bindings and service elements using the information provided. This operation delegates the
     * actual content creation to the 'codeGenerationWsdlBindings' component defined in the compiler's application
     * context file. If a service endpoint URL is not specified in the code generation context, the service component
     * will not be generated.
     * 
     * @param definitions the top-level WSDL document element to which the bindings and service should be added
     * @param portType the port-type for which the bindings should be created
     * @param wsdlMessages the list of all messages defined in the WSDL document
     * @param cgContext the current code generation context
     */
    protected void addBindingAndService(TDefinitions definitions, TPortType portType, List<TMessage> wsdlMessages,
        CodeGenerationContext cgContext) {
        String serviceEndpointUrl = cgContext.getValue( CodeGenerationContext.CK_SERVICE_ENDPOINT_URL );

        if ((wsdlBindings != null) && (serviceEndpointUrl != null) && (serviceEndpointUrl.length() > 0)) {
            TBinding binding = wsdlBindings.createBinding( portType, definitions.getTargetNamespace(), wsdlMessages );

            if (binding != null) {
                TService service =
                    wsdlBindings.createService( binding, definitions.getTargetNamespace(), serviceEndpointUrl );

                for (TBindingOperation operation : binding.getOperation()) {
                    TOperation portTypeOperation = getPortTypeOperation( portType, operation.getName() );

                    if (portTypeOperation != null) {
                        operation.setDocumentation( cloneDocumentation( portTypeOperation.getDocumentation() ) );
                    }
                }
                binding.setDocumentation( cloneDocumentation( portType.getDocumentation() ) );
                definitions.getAnyTopLevelOptionalElement().add( binding );

                if (service != null) {
                    service.setDocumentation( cloneDocumentation( portType.getDocumentation() ) );
                    definitions.getAnyTopLevelOptionalElement().add( service );
                }
            }
        }
    }

    /**
     * Clones the given documentation element.
     * 
     * @param doc the documentation element to clone
     * @return TDocumentation
     */
    private TDocumentation cloneDocumentation(TDocumentation doc) {
        TDocumentation docCopy = null;

        if (doc != null) {
            docCopy = new TDocumentation();
            docCopy.getContent().addAll( doc.getContent() );
        }
        return docCopy;
    }

    /**
     * Returns the port type operation with the specified name.
     * 
     * @param portType the port type from which to retrieve the operation
     * @param operationName the name of the port type operation to return
     * @return TOperation
     */
    private TOperation getPortTypeOperation(TPortType portType, String operationName) {
        TOperation operation = null;

        for (TOperation op : portType.getOperation()) {
            if ((operationName != null) && operationName.equals( op.getName() )) {
                operation = op;
                break;
            }
        }
        return operation;
    }

    /**
     * Adds the given library to the list of dependencies for the WSDL document.
     * 
     * @param wsdlDependency the dependency to add
     */
    protected void addWsdlDependency(AbstractLibrary wsdlDependency) {
        CodeGenerator<?> codeGenerator = context.getCodeGenerator();

        if (codeGenerator instanceof AbstractWsdlCodeGenerator) {
            ((AbstractWsdlCodeGenerator<?>) codeGenerator).addWsdlDependency( wsdlDependency );
        }
    }

}

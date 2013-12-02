/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.task;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sabre.schemacompiler.codegen.CodeGenerationContext;
import com.sabre.schemacompiler.codegen.CodeGenerationFilter;
import com.sabre.schemacompiler.codegen.CodeGenerator;
import com.sabre.schemacompiler.codegen.CodeGeneratorFactory;
import com.sabre.schemacompiler.codegen.impl.DefaultCodeGenerationFilter;
import com.sabre.schemacompiler.codegen.impl.DependencyFilterBuilder;
import com.sabre.schemacompiler.codegen.impl.LibraryMemberTrimmedFilenameBuilder;
import com.sabre.schemacompiler.codegen.impl.LibraryTrimmedFilenameBuilder;
import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLService;
import com.sabre.schemacompiler.model.XSDLibrary;
import com.sabre.schemacompiler.util.SchemaCompilerException;

/**
 * Compiler task used to generate WSDL documents for the services defined in a project, as well as the
 * trimmed schema (XSD) files that contain the entities upon which those services depend.  If multiple
 * services are defined in the project's libraries, WSDL's will be generated for all of those services
 * in the same output directory, and the trimmed schemas will be a union of the dependencies from all
 * of the compiled services.
 * 
 * @author S. Livezey
 */
public class ServiceProjectCompilerTask extends AbstractSchemaCompilerTask implements ServiceCompilerTaskOptions {
	
	private String serviceEndpointUrl;
	
	/**
	 * Constructor that specifies the filename of the project for which services are being
	 * compiled.
	 * 
	 * @param projectFilename  the name of the project (.otp) file
	 */
	public ServiceProjectCompilerTask(String projectFilename) {
		this.projectFilename = projectFilename;
	}
	
	/**
	 * @see com.sabre.schemacompiler.task.AbstractCompilerTask#generateOutput(java.util.Collection, java.util.Collection)
	 */
	@Override
	protected void generateOutput(Collection<TLLibrary> userDefinedLibraries, Collection<XSDLibrary> legacySchemas) throws SchemaCompilerException {
		Map<String,Boolean> duplicateServiceNameIndicators = new HashMap<String,Boolean>();
		DependencyFilterBuilder filterBuilder = new DependencyFilterBuilder();
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
				filterBuilder.addLibraryMember( service );
				serviceList.add( service );
			}
		}
		
		// Generate the WSDL documents for each service
		CodeGenerationContext context = createContext();
		
		for (TLService service : serviceList) {
			boolean duplicateName = duplicateServiceNameIndicators.get( service.getName() );
			
			CodeGenerator<TLService> serviceWsdlGenerator = CodeGeneratorFactory.getInstance().newCodeGenerator(
					CodeGeneratorFactory.WSDL_TARGET_FORMAT, TLService.class);
			
			serviceWsdlGenerator.setFilenameBuilder( new LibraryMemberTrimmedFilenameBuilder<TLService>(service, duplicateName) );
			addGeneratedFiles( serviceWsdlGenerator.generateOutput(service, context) );
			
			// Generate example files if required; examples are only created for the operation
			// messages (not the contents of the trimmed schemas)
			if (isGenerateExamples()) {
				generateExampleArtifacts(userDefinedLibraries, context, new LibraryTrimmedFilenameBuilder(null),
						createExampleFilter(service));
			}
		}
		
		// Generate the trimmed schemas for all of the services as a union of their dependencies
		if (!serviceList.isEmpty()) {
			compileXmlSchemas(userDefinedLibraries, legacySchemas, context,
					new LibraryTrimmedFilenameBuilder(null), filterBuilder.buildFilter());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.task.AbstractSchemaCompilerTask#getExampleOutputFolder(com.sabre.schemacompiler.model.LibraryMember, com.sabre.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected String getExampleOutputFolder(LibraryMember libraryMember, CodeGenerationContext context) {
		String rootOutputFolder = context.getValue(CodeGenerationContext.CK_OUTPUT_FOLDER);
		
		if (rootOutputFolder == null) {
			rootOutputFolder = System.getProperty("user.dir");
		}
		return new File(rootOutputFolder, "examples").getAbsolutePath();
	}

	/**
	 * @see com.sabre.schemacompiler.task.AbstractSchemaCompilerTask#getSchemaRelativeFolderPath(com.sabre.schemacompiler.model.LibraryMember, com.sabre.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected String getSchemaRelativeFolderPath(LibraryMember libraryMember, CodeGenerationContext context) {
		return "../";
	}
	
	/**
	 * Returns a <code>CodeGenerationFilter</code> that only includes the service, its operations,
	 * and their facets.  None of the other dependent elements from the supporting library schemas
	 * are included.
	 * 
	 * @param service  the service for which to create a filter
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
	 * @see com.sabre.schemacompiler.task.AbstractCompilerTask#createContext()
	 */
	protected CodeGenerationContext createContext() {
		CodeGenerationContext context = super.createContext();
		
		if (serviceEndpointUrl != null) {
			context.setValue( CodeGenerationContext.CK_SERVICE_ENDPOINT_URL, serviceEndpointUrl);
		}
		return context;
	}
	
	/**
	 * @see com.sabre.schemacompiler.task.AbstractCompilerTask#applyTaskOptions(com.sabre.schemacompiler.task.CommonCompilerTaskOptions)
	 */
	@Override
	public void applyTaskOptions(CommonCompilerTaskOptions taskOptions) {
		if (taskOptions instanceof ServiceCompilerTaskOptions) {
			ServiceCompilerTaskOptions serviceOptions = (ServiceCompilerTaskOptions) taskOptions;
			
			setServiceEndpointUrl( serviceOptions.getServiceEndpointUrl() );
		}
		super.applyTaskOptions(taskOptions);
	}

	/**
	 * @see com.sabre.schemacompiler.task.ServiceCompilerTaskOptions#getServiceLibraryUrl()
	 */
	@Override
	public URL getServiceLibraryUrl() {
		return null;
	}

	/**
	 * @see com.sabre.schemacompiler.task.ServiceCompilerTaskOptions#getServiceEndpointUrl()
	 */
	@Override
	public String getServiceEndpointUrl() {
		return serviceEndpointUrl;
	}
	
	/**
	 * Assigns the base URL for all service endpoints generated in WSDL documents.
	 * 
	 * @param serviceEndpointUrl  the service endpoint URL to assign
	 */
	public void setServiceEndpointUrl(String serviceEndpointUrl) {
		this.serviceEndpointUrl = serviceEndpointUrl;
	}

}

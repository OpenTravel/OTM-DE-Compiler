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

import java.util.Collection;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.DefaultCodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.impl.DependencyFilterBuilder;
import org.opentravel.schemacompiler.codegen.impl.LibraryTrimmedFilenameBuilder;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.util.SchemaCompilerException;

/**
 * Compiler task used to generate Swagger documents for the resources defined in a model, as well as the
 * trimmed schemas (XSD & JSON) that contain the entities upon which those resources depend.
 */
public class SwaggerCompilerTask extends AbstractSchemaCompilerTask 
		implements ResourceCompilerTaskOptions {
	
    private String resourceBaseUrl;
    
	/**
	 * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#generateOutput(java.util.Collection, java.util.Collection)
	 */
	@Override
	protected void generateOutput(Collection<TLLibrary> userDefinedLibraries, Collection<XSDLibrary> legacySchemas)
			throws SchemaCompilerException {
        CodeGenerationContext modelContext = createContext();
        CodeGenerationContext resourceContext = modelContext.getCopy();
        
        for (TLLibrary library : userDefinedLibraries) {
        	for (TLResource resource : library.getResourceTypes()) {
        		if (resource.isAbstract()) continue;
                CodeGenerationFilter filter = new DependencyFilterBuilder( resource ).buildFilter();
                resourceContext.setValue( CodeGenerationContext.CK_OUTPUT_FOLDER,
                        getResourceOutputFolder( resource, modelContext ) );
        		
                // Generate the Swagger document
                CodeGenerator<TLResource> swaggerGenerator = CodeGeneratorFactory.getInstance()
                        .newCodeGenerator( CodeGeneratorFactory.SWAGGER_TARGET_FORMAT, TLResource.class );
                addGeneratedFiles(swaggerGenerator.generateOutput( resource, resourceContext ) );

                // Generate the trimmed XML & JSON schema documents for the service
                compileXmlSchemas( userDefinedLibraries, legacySchemas, resourceContext,
                        new LibraryTrimmedFilenameBuilder( null ), filter );
                compileJsonSchemas( userDefinedLibraries, legacySchemas, resourceContext,
                        new LibraryTrimmedFilenameBuilder( null ), filter );
                
                // Generate example files if required; examples are only created for the operation
                // messages (not the contents of the trimmed schemas)
                if (isGenerateExamples()) {
                    generateExampleArtifacts(userDefinedLibraries, resourceContext,
                            new LibraryTrimmedFilenameBuilder( resource ),
                            createExampleFilter( resource ), CodeGeneratorFactory.XML_TARGET_FORMAT );
                    generateExampleArtifacts(userDefinedLibraries, resourceContext,
                            new LibraryTrimmedFilenameBuilder( resource ),
                            createExampleFilter( resource ), CodeGeneratorFactory.JSON_TARGET_FORMAT );
                }
        	}
        }
	}

    /**
     * Returns the location of the output folder for the specified resource.
     * 
     * @param resource  the resource for which the output folder is needed
     * @param context  the code generation context
     * @return String
     * @throws SchemaCompilerException  thrown if an error occurs due to an unrecognized version scheme
     */
    protected String getResourceOutputFolder(TLResource resource, CodeGenerationContext context)
            throws SchemaCompilerException {
        String rootOutputFolder = context.getValue( CodeGenerationContext.CK_OUTPUT_FOLDER );
        StringBuilder outputFolder = new StringBuilder();

        if (rootOutputFolder == null) {
            rootOutputFolder = System.getProperty("user.dir");
        }
        outputFolder.append( rootOutputFolder );
        outputFolder.append('/').append( resource.getOwningLibrary().getName() );
        outputFolder.append('_').append( resource.getName() );
        outputFolder.append("/v").append( resource.getVersion().replaceAll("\\.", "_") );
        return outputFolder.toString();
    }

    /**
     * Returns a <code>CodeGenerationFilter</code> that only includes the resource, its actions,
     * and their request/response payloads. None of the other dependent elements from the supporting
     * library schemas are included.
     * 
     * @param resource  the resource for which to create a filter
     * @return CodeGenerationFilter
     */
    protected CodeGenerationFilter createExampleFilter(TLResource resource) {
        DefaultCodeGenerationFilter filter = new DefaultCodeGenerationFilter();
    	
        for (TLAction action : resource.getActions()) {
        	if (action.isCommonAction()) continue;
        	
        	if (action.getRequest() != null) {
        		filter.addProcessedElement( action.getRequest() );
        	}
        	for (TLActionResponse response : action.getResponses()) {
        		filter.addProcessedElement( response );
        	}
        }
        filter.addProcessedElement( resource );
        filter.addProcessedLibrary( resource.getOwningLibrary() );
        return filter;
    }
    
    /**
     * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#applyTaskOptions(org.opentravel.schemacompiler.task.CommonCompilerTaskOptions)
     */
    @Override
    public void applyTaskOptions(CommonCompilerTaskOptions taskOptions) {
        if (taskOptions instanceof ResourceCompilerTaskOptions) {
            setResourceBaseUrl(((ResourceCompilerTaskOptions) taskOptions).getResourceBaseUrl());
        }
        super.applyTaskOptions(taskOptions);
    }

	/**
	 * @see org.opentravel.schemacompiler.task.ResourceCompilerTaskOptions#getResourceBaseUrl()
	 */
	@Override
	public String getResourceBaseUrl() {
		return resourceBaseUrl;
	}
	
	/**
	 * Assigns the base URL path for all generated REST API specifications.
	 *
	 * @param resourceBaseUrl  the base URL path to assign
	 */
	public void setResourceBaseUrl(String resourceBaseUrl) {
		this.resourceBaseUrl = resourceBaseUrl;
	}

}

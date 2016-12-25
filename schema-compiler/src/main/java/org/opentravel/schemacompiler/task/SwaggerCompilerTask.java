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
import java.util.Collection;
import java.util.List;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.DefaultCodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.impl.DependencyFilterBuilder;
import org.opentravel.schemacompiler.codegen.impl.LibraryTrimmedFilenameBuilder;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLMimeType;
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
                resourceContext.setValue( CodeGenerationContext.CK_ENABLE_SINGLE_FILE_SWAGGER, "false" );
                resourceContext.setValue( CodeGenerationContext.CK_OUTPUT_FOLDER,
                        getResourceOutputFolder( resource, modelContext, false ) );
        		
                // Generate the Swagger document
                CodeGenerator<TLResource> swaggerGenerator = CodeGeneratorFactory.getInstance()
                        .newCodeGenerator( CodeGeneratorFactory.SWAGGER_TARGET_FORMAT, TLResource.class );
                Collection<File> swaggerFiles = swaggerGenerator.generateOutput( resource, resourceContext );
                
                if (!swaggerFiles.isEmpty()) {
                    addGeneratedFiles( swaggerFiles );

                    // Generate the trimmed XML & JSON schema documents for the service
                    compileXmlSchemas( userDefinedLibraries, legacySchemas, resourceContext,
                            new LibraryTrimmedFilenameBuilder( null ),
                            createSchemaFilter( resource, TLMimeType.TEXT_XML, TLMimeType.APPLICATION_XML ) );
                    compileJsonSchemas( userDefinedLibraries, legacySchemas, resourceContext,
                            new LibraryTrimmedFilenameBuilder( null ),
                            createSchemaFilter( resource, TLMimeType.TEXT_JSON, TLMimeType.APPLICATION_JSON ) );
                    
                    // Generate example files if required; examples are only created for the operation
                    // messages (not the contents of the trimmed schemas)
                    if (isGenerateExamples()) {
                        generateExampleArtifacts(userDefinedLibraries, resourceContext,
                                new LibraryTrimmedFilenameBuilder( null ),
                                createExampleFilter( resource, TLMimeType.TEXT_XML, TLMimeType.APPLICATION_XML ),
                                CodeGeneratorFactory.XML_TARGET_FORMAT );
                        generateExampleArtifacts(userDefinedLibraries, resourceContext,
                                new LibraryTrimmedFilenameBuilder( null ),
                                createExampleFilter( resource, TLMimeType.TEXT_JSON, TLMimeType.APPLICATION_JSON ),
                                CodeGeneratorFactory.JSON_TARGET_FORMAT );
                    }
                }
                
                // Also generate a standalone Swagger document that contains all of the JSON
                // definitions that are normally broken out into separate JSON schemas.
                resourceContext.setValue( CodeGenerationContext.CK_ENABLE_SINGLE_FILE_SWAGGER, "true" );
                resourceContext.setValue( CodeGenerationContext.CK_OUTPUT_FOLDER,
                        getResourceOutputFolder( resource, modelContext, true ) );
                swaggerGenerator.setFilter( createSchemaFilter(
                		resource, TLMimeType.TEXT_JSON, TLMimeType.APPLICATION_JSON ) );
                addGeneratedFiles( swaggerGenerator.generateOutput( resource, resourceContext ) );
        	}
        }
	}

    /**
     * Returns the location of the output folder for the specified resource.
     * 
     * @param resource  the resource for which the output folder is needed
     * @param context  the code generation context
     * @param isSingleFileEnabled  flag indicating whether single-file swagger document
     *							   generation is enabled
     * @return String
     * @throws SchemaCompilerException  thrown if an error occurs due to an unrecognized version scheme
     */
    protected String getResourceOutputFolder(TLResource resource, CodeGenerationContext context,
    		boolean isSingleFileEnabled) throws SchemaCompilerException {
        String rootOutputFolder = context.getValue( CodeGenerationContext.CK_OUTPUT_FOLDER );
        StringBuilder outputFolder = new StringBuilder();
        
        if (rootOutputFolder == null) {
            rootOutputFolder = System.getProperty("user.dir");
        }
        outputFolder.append( rootOutputFolder );
        outputFolder.append('/').append( resource.getOwningLibrary().getName() );
        outputFolder.append('_').append( resource.getName() );
        
        if (!isSingleFileEnabled) {
            outputFolder.append("/v").append( resource.getVersion().replaceAll("\\.", "_") );
        }
        return outputFolder.toString();
    }

    /**
	 * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#getExampleOutputFolder(org.opentravel.schemacompiler.model.LibraryMember, org.opentravel.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected String getExampleOutputFolder(LibraryMember libraryMember, CodeGenerationContext context) {
        String rootOutputFolder = context.getValue( CodeGenerationContext.CK_OUTPUT_FOLDER );

        if (rootOutputFolder == null) {
            rootOutputFolder = System.getProperty( "user.dir" );
        }
        return new File( rootOutputFolder, "examples/" ).getAbsolutePath();
	}

	/**
	 * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#getSchemaRelativeFolderPath(org.opentravel.schemacompiler.model.LibraryMember, org.opentravel.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected String getSchemaRelativeFolderPath(LibraryMember libraryMember, CodeGenerationContext context) {
        return "../";
	}
	
	/**
	 * Constructs a filter for the generation of XML and JSON schemas.
	 * 
	 * @param resource  the resource for which to create a filter
     * @param contentTypes  the MIME types for which to generate schemas
	 * @return CodeGenerationFilter
	 */
    protected CodeGenerationFilter createSchemaFilter(TLResource resource, TLMimeType... contentTypes) {
    	DependencyFilterBuilder builder = new DependencyFilterBuilder().setIncludeExtendedLegacySchemas( true );
    	
    	for (TLAction action : ResourceCodegenUtils.getInheritedActions( resource )) {
    		if (action.isCommonAction()) continue;
    		TLActionRequest request = ResourceCodegenUtils.getDeclaredOrInheritedRequest( action );
    		
        	if ((request != null) && containsSupportedType( request.getMimeTypes(), contentTypes)) {
    			builder.addLibraryMember( request.getPayloadType() );
    		}
    		for (TLActionResponse response : ResourceCodegenUtils.getInheritedResponses( action )) {
        		if (containsSupportedType( response.getMimeTypes(), contentTypes)) {
        			builder.addLibraryMember( response.getPayloadType() );
        		}
    		}
    	}
    	return builder.buildFilter();
    }
    
	/**
     * Returns a <code>CodeGenerationFilter</code> that only includes the resource, its actions,
     * and their request/response payloads. None of the other dependent elements from the supporting
     * library schemas are included.
     * 
     * @param resource  the resource for which to create a filter
     * @param contentTypes  the MIME types for which to generate example files
     * @return CodeGenerationFilter
     */
    protected CodeGenerationFilter createExampleFilter(TLResource resource, TLMimeType... contentTypes) {
        DefaultCodeGenerationFilter filter = new DefaultCodeGenerationFilter();
    	
        for (TLAction action : resource.getActions()) {
        	if (action.isCommonAction()) continue;
        	TLActionRequest request = ResourceCodegenUtils.getDeclaredOrInheritedRequest( action );
        	
        	if ((request != null) && containsSupportedType( request.getMimeTypes(), contentTypes)) {
        		NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( request.getPayloadType() );
        		
        		if (payloadType != null) {
        			appendToFilter( payloadType, filter );
        		}
        	}
        	for (TLActionResponse response : ResourceCodegenUtils.getInheritedResponses( action )) {
        		if (containsSupportedType( response.getMimeTypes(), contentTypes)) {
            		NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( response.getPayloadType() );
            		
            		if (payloadType != null) {
            			appendToFilter( payloadType, filter );
            		}
        		}
        	}
        }
		appendToFilter( resource, filter );
        return filter;
    }
    
    private void appendToFilter(NamedEntity entity, DefaultCodeGenerationFilter filter) {
    	NamedEntity currentEntity = entity;
    	
    	while (currentEntity != null) {
    		filter.addProcessedElement( currentEntity );
            filter.addProcessedLibrary( currentEntity.getOwningLibrary() );
    		
            if (currentEntity instanceof TLAlias) {
            	currentEntity = ((TLAlias) currentEntity).getOwningEntity();
            	
            } else if (currentEntity instanceof TLFacet) {
            	currentEntity = ((TLFacet) currentEntity).getOwningEntity();
            	
            } else {
            	currentEntity = null;
            }
    	}
    }
    
    /**
     * Returns true if the give list of MIME types contains at least one of the supported types.
     * 
     * @param mimeTypes  the list of MIME types to check
     * @param supportedTypes  the array of supported MIME types
     * @return boolean
     */
    private boolean containsSupportedType(List<TLMimeType> mimeTypes, TLMimeType... supportedTypes) {
    	boolean supported = false;
    	
    	for (TLMimeType supportedType : supportedTypes) {
    		supported |= mimeTypes.contains( supportedType );
    	}
    	return supported;
    }
    
    /**
	 * @see org.opentravel.schemacompiler.task.AbstractSchemaCompilerTask#createContext()
	 */
	@Override
	protected CodeGenerationContext createContext() {
        CodeGenerationContext context = super.createContext();

        if (resourceBaseUrl != null) {
            context.setValue(CodeGenerationContext.CK_RESOURCE_BASE_URL, resourceBaseUrl);
        }
        return context;
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

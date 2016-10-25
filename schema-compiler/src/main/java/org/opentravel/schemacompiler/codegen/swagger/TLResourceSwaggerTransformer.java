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
package org.opentravel.schemacompiler.codegen.swagger;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.QualifiedAction;
import org.opentravel.schemacompiler.codegen.json.model.JsonLibraryInfo;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerDocument;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerInfo;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerOperation;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerOtmResource;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerPathItem;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerScheme;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils.URLComponents;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>TLResource</code> objects to the Swagger model
 * objects used to produce the output.
 */
public class TLResourceSwaggerTransformer extends AbstractSwaggerCodegenTransformer<TLResource,SwaggerDocument> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public SwaggerDocument transform(TLResource source) {
		SwaggerDocument swaggerDoc = new SwaggerDocument();
		URLComponents urlComponents = getUrlComponents();
		String basePath = urlComponents.getPath();
		
		// Populate the network address components of the Swagger document
		if (basePath.equals("/")) {
			basePath = source.getBasePath();
		} else {
			basePath += source.getBasePath();
		}
		swaggerDoc.setHost( urlComponents.getAuthority() );
		swaggerDoc.getSchemes().add( urlComponents.getScheme() );
		swaggerDoc.setBasePath( basePath );
		
		// Populate the information section of the Swagger document
		JsonLibraryInfo libraryInfo = jsonUtils.getResourceInfo( source );
		SwaggerOtmResource swaggerResource = new SwaggerOtmResource();
		SwaggerInfo info = new SwaggerInfo();
		
		swaggerResource.setNamespace( source.getNamespace() );
		swaggerResource.setLocalName( source.getLocalName() );
		swaggerDoc.setOtmResource( swaggerResource );
		info.setTitle( source.getName() );
		info.setLibraryInfo( libraryInfo );
		info.setVersion( libraryInfo.getLibraryVersion() );
		transformDocumentation( source, info );
		swaggerDoc.setInfo( info );
		
		// Construct a map of operations indexed by path template and HTTP method
        ObjectTransformer<QualifiedAction,SwaggerOperation,CodeGenerationTransformerContext> actionTransformer =
        		getTransformerFactory().getTransformer(QualifiedAction.class, SwaggerOperation.class);
		Map<String,Map<TLHttpMethod,SwaggerOperation>> operationMap = new HashMap<>();
		List<String> pathList = new ArrayList<>();
		
		for (QualifiedAction qAction : ResourceCodegenUtils.getQualifiedActions( source )) {
			if (qAction.getAction().isCommonAction()) continue;
			TLActionRequest actionRequest = qAction.getActionRequest();
			String pathTemplate = qAction.getPathTemplate();
			TLHttpMethod httpMethod = actionRequest.getHttpMethod();
			SwaggerOperation operation = actionTransformer.transform( qAction );
			Map<TLHttpMethod,SwaggerOperation> methodMap = operationMap.get( pathTemplate );
			
			if (methodMap == null) {
				methodMap = new HashMap<>();
				pathList.add( pathTemplate );
				operationMap.put( pathTemplate, methodMap );
			}
			methodMap.put( httpMethod, operation );
		}
		
		// Use the 'operationMap' to construct the path items for the Swagger document
		for (String pathTemplate : pathList) {
			Map<TLHttpMethod,SwaggerOperation> methodMap = operationMap.get( pathTemplate );
			SwaggerPathItem pathItem = new SwaggerPathItem();
			
			for (Entry<TLHttpMethod,SwaggerOperation> entry : methodMap.entrySet()) {
				switch (entry.getKey()) {
					case GET:
						pathItem.setGetOperation( entry.getValue() );
						break;
					case POST:
						pathItem.setPostOperation( entry.getValue() );
						break;
					case PUT:
						pathItem.setPutOperation( entry.getValue() );
						break;
					case DELETE:
						pathItem.setDeleteOperation( entry.getValue() );
						break;
					case HEAD:
						pathItem.setHeadOperation( entry.getValue() );
						break;
					case OPTIONS:
						pathItem.setOptionsOperation( entry.getValue() );
						break;
					case PATCH:
						pathItem.setPatchOperation( entry.getValue() );
						break;
				}
			}
			pathItem.setPathTemplate( pathTemplate );
			swaggerDoc.getPathItems().add( pathItem );
		}
		
		// If required, generate definitions for the Swagger document
		if (isSingleFileEnabled()) {
			swaggerDoc.getDefinitions().addAll( buildJsonDefinitions( source.getOwningModel() ) );
		}
		
		// Add any extensions provided by the Swagger code generation bindings
		if (swaggerBindings != null) {
			if (swaggerBindings.getSupportedSchemes() != null) {
				for (SwaggerScheme scheme : swaggerBindings.getSupportedSchemes()) {
					if (!swaggerDoc.getSchemes().contains( scheme )) {
						swaggerDoc.getSchemes().add( scheme );
					}
				}
			}
			if (swaggerBindings.getGlobalParameters() != null) {
				swaggerDoc.getGlobalParameters().addAll( swaggerBindings.getGlobalParameters() );
			}
			if (swaggerBindings.getSecuritySchemes() != null) {
				swaggerDoc.getSecuritySchemes().addAll( swaggerBindings.getSecuritySchemes() );
			}
		}
		return swaggerDoc;
	}
	
	/**
	 * Builds the set of all definitions that should be included in the Swagger document.  The
	 * definitions that are included are based on the current code generation filter.
	 * 
	 * @param model  the model from which to generate JSON definitions
	 * @return List<JsonSchemaNamedReference>
	 */
	private List<JsonSchemaNamedReference> buildJsonDefinitions(TLModel model) {
		List<JsonSchemaNamedReference> definitions = new ArrayList<>();
		CodeGenerator<?> codeGenerator = context.getCodeGenerator();
		CodeGenerationFilter filter = codeGenerator.getFilter();
		
		// Add definitions for all of the OTM entities that are within the
		// scope of the current filter
		for (TLLibrary library : model.getUserDefinedLibraries()) {
			if (!filter.processLibrary( library )) continue;
			
	        for (LibraryMember member : library.getNamedMembers()) {
	            ObjectTransformer<LibraryMember, CodegenArtifacts, CodeGenerationTransformerContext> transformer =
	            		getTransformerFactory().getTransformer(member, CodegenArtifacts.class);
	            
	            if ((transformer != null) && ((filter == null) || filter.processEntity(member))) {
	                CodegenArtifacts artifacts = transformer.transform(member);

	                if (artifacts != null) {
	                    for (JsonSchemaNamedReference memberDef : artifacts.getArtifactsOfType(JsonSchemaNamedReference.class)) {
	                    	definitions.add( memberDef );
	                    }
	                }
	            }
	        }
		}
		
		// Add hard-coded built-in definitions (not a great solution, but expedient for now)
		JsonSchema emptySchema = new JsonSchema();
		JsonSchema enumExtensionSchema = new JsonSchema();
		JsonSchema localDateTimeSchema = new JsonSchema();
		JsonSchema extensionPointSchema = new JsonSchema();
		
		emptySchema.setType( JsonType.jsonString );
		emptySchema.setMaxLength( 0 );
		definitions.add( new JsonSchemaNamedReference( "Empty", new JsonSchemaReference( emptySchema ) ) );
		
		enumExtensionSchema.setType( JsonType.jsonString );
		enumExtensionSchema.setMinLength( 1 );
		enumExtensionSchema.setMaxLength( 128 );
		definitions.add( new JsonSchemaNamedReference( "String_EnumExtension", new JsonSchemaReference( enumExtensionSchema ) ) );
		
		localDateTimeSchema.setType( JsonType.jsonDateTime );
		localDateTimeSchema.setPattern( ".+T[^Z+\\-]+" );
		definitions.add( new JsonSchemaNamedReference( "LocalDateTime", new JsonSchemaReference( localDateTimeSchema ) ) );
		
		definitions.add( new JsonSchemaNamedReference( "ExtensionPoint", new JsonSchemaReference( extensionPointSchema ) ) );
		definitions.add( new JsonSchemaNamedReference( "ExtensionPoint_Summary", new JsonSchemaReference( extensionPointSchema ) ) );
		definitions.add( new JsonSchemaNamedReference( "ExtensionPoint_Detail", new JsonSchemaReference( extensionPointSchema ) ) );
		definitions.add( new JsonSchemaNamedReference( "ExtensionPoint_Custom", new JsonSchemaReference( extensionPointSchema ) ) );
		definitions.add( new JsonSchemaNamedReference( "ExtensionPoint_Query", new JsonSchemaReference( extensionPointSchema ) ) );
		definitions.add( new JsonSchemaNamedReference( "ExtensionPoint_Update", new JsonSchemaReference( extensionPointSchema ) ) );
		definitions.add( new JsonSchemaNamedReference( "ExtensionPoint_Shared", new JsonSchemaReference( extensionPointSchema ) ) );
		definitions.add( new JsonSchemaNamedReference( "ExtensionPoint_Choice", new JsonSchemaReference( extensionPointSchema ) ) );
		
		return definitions;
	}
	
	/**
	 * Returns true if single-file Swagger document generation is enabled.
	 * 
	 * @return boolean
	 */
	private boolean isSingleFileEnabled() {
		CodeGenerationContext cgContext = (context == null) ? null : context.getCodegenContext();
		boolean result = false;
		
		if (cgContext != null) {
	         result = "true".equalsIgnoreCase(
	        		 cgContext.getValue( CodeGenerationContext.CK_ENABLE_SINGLE_FILE_SWAGGER ) );
		}
		return result;
	}
	
	/**
	 * Retrieves the base resource URL from the code generation context and returns
	 * its components.  If a base URL was not specified, default values are returned.
	 * 
	 * @return URLComponents
	 */
	private URLComponents getUrlComponents() {
		String baseUrl = context.getCodegenContext().getValue( CodeGenerationContext.CK_RESOURCE_BASE_URL );
		URLComponents components = null;
		
		try {
			if (baseUrl != null) {
				components = ResourceCodegenUtils.parseUrl( baseUrl );
			}
		} catch (MalformedURLException e) {
			// No error - use default values
		}
		if (components == null) {
			components = new URLComponents( SwaggerScheme.HTTP, "127.0.0.1", "/", null );
		}
		return components;
	}
	
}

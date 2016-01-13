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
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerDocument;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerInfo;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerOperation;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerPathItem;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils.URLComponents;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLHttpMethod;
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
		SwaggerInfo info = new SwaggerInfo();
		
		info.setTitle( source.getName() + " API Specification" );
		info.setLibraryInfo( jsonUtils.getResourceInfo( source ) );
		swaggerDoc.setInfo( info );
		
		// Construct a map of operations indexed by path template and HTTP method
        ObjectTransformer<TLAction,SwaggerOperation,CodeGenerationTransformerContext> actionTransformer =
        		getTransformerFactory().getTransformer(TLAction.class, SwaggerOperation.class);
		Map<String,Map<TLHttpMethod,SwaggerOperation>> operationMap = new HashMap<>();
		List<String> pathList = new ArrayList<>();
		
		for (TLAction action : ResourceCodegenUtils.getInheritedActions( source )) {
			if (action.isCommonAction()) continue;
			TLActionRequest actionRequest = ResourceCodegenUtils.getDeclaredOrInheritedRequest( action );
			String pathTemplate = actionRequest.getPathTemplate();
			TLHttpMethod httpMethod = actionRequest.getHttpMethod();
			SwaggerOperation operation = actionTransformer.transform( action );
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
		return swaggerDoc;
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
			components = new URLComponents( "http", "127.0.0.1", "/", null );
		}
		return components;
	}
	
}

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

import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerResponse;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerXmlSchemaRef;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLMimeType;

/**
 * Performs the translation from <code>TLActionResponse</code> objects to the Swagger model
 * objects used to produce the output.
 */
public class TLActionResponseSwaggerTransformer extends AbstractSwaggerCodegenTransformer<TLActionResponse,CodegenArtifacts> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public CodegenArtifacts transform(TLActionResponse source) {
		CodegenArtifacts artifacts = new CodegenArtifacts();
		
		if (source.getStatusCodes().isEmpty()) {
			artifacts.addArtifact( createResponse( source, null ) );
			
		} else {
			for (Integer statusCode : source.getStatusCodes()) {
				artifacts.addArtifact( createResponse( source, statusCode ) );
			}
		}
		return artifacts;
	}
	
	/**
	 * Constructs a new Swagger response for the indicated status code.  If the status code
	 * is null, a default response will be created.
	 * 
	 * @param source  the action response from which to construct the Swagger response
	 * @param statusCode  the status code for the Swagger response
	 * @return SwaggerResponse
	 */
	private SwaggerResponse createResponse(TLActionResponse source, Integer statusCode) {
		NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( source.getPayloadType() );
		SwaggerResponse response = new SwaggerResponse();
		
		response.setDefaultResponse( (statusCode == null) );
		response.setStatusCode( statusCode );
		transformDocumentation( source, response );
		
		if (payloadType != null) {
			if (containsSupportedType( source.getMimeTypes(), TLMimeType.TEXT_JSON, TLMimeType.APPLICATION_JSON )) {
				response.setSchema( new JsonSchemaReference(
						jsonUtils.getSchemaReferencePath( payloadType, null ) ));
			}
			if (containsSupportedType( source.getMimeTypes(), TLMimeType.TEXT_XML, TLMimeType.APPLICATION_XML )) {
				response.setXmlSchema( new SwaggerXmlSchemaRef(
						jsonUtils.getXmlSchemaReferencePath( payloadType ) ) );
			}
			
			if (payloadType instanceof TLDocumentationOwner) {
				transformDocumentation( (TLDocumentationOwner) payloadType, response );
			}
		}
		return response;
	}
	
}

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
import org.opentravel.schemacompiler.codegen.json.facet.FacetJsonSchemaDelegate;
import org.opentravel.schemacompiler.codegen.json.facet.FacetJsonSchemaDelegateFactory;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerResponse;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLReferenceType;

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
		SwaggerResponse response = new SwaggerResponse();
		
		response.setDefaultResponse( (statusCode == null) );
		response.setStatusCode( statusCode );
		transformDocumentation( source, response );
		
		if (source.getPayloadType() != null) {
			NamedEntity referencedEntity = null;
			
			if (source.getPayloadType() instanceof TLActionFacet) {
				TLActionFacet actionFacet = (TLActionFacet) source.getPayloadType();
		        FacetJsonSchemaDelegateFactory delegateFactory = new FacetJsonSchemaDelegateFactory( context );
		        FacetJsonSchemaDelegate<?> delegate = delegateFactory.getDelegate( actionFacet );
				
				if (delegate.hasContent() || (actionFacet.getReferenceType() != TLReferenceType.NONE)) {
					if (delegate.hasContent() || (actionFacet.getReferenceRepeat() != 0)) {
						referencedEntity = source;
						
					} else {
						referencedEntity = ResourceCodegenUtils.getBusinessObjectElement( actionFacet ).getType();
					}
				}
				
			} else {
				referencedEntity = source.getPayloadType();
			}
			
			if (referencedEntity != null) {
				response.setSchema( new JsonSchemaReference(
						jsonUtils.getSchemaReferencePath( referencedEntity, source ) ));
				
				if (referencedEntity instanceof TLDocumentationOwner) {
					transformDocumentation( (TLDocumentationOwner) referencedEntity, response );
				}
			}
		}
		return response;
	}
	
}

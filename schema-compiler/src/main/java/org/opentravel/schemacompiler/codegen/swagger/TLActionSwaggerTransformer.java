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

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.json.facet.FacetJsonSchemaDelegate;
import org.opentravel.schemacompiler.codegen.json.facet.FacetJsonSchemaDelegateFactory;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerOperation;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParamType;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParameter;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerResponse;
import org.opentravel.schemacompiler.codegen.util.JsonSchemaNamingUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>TLAction</code> objects to the Swagger model
 * objects used to produce the output.
 */
public class TLActionSwaggerTransformer extends AbstractSwaggerCodegenTransformer<TLAction,SwaggerOperation> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public SwaggerOperation transform(TLAction source) {
        ObjectTransformer<TLActionResponse,CodegenArtifacts,CodeGenerationTransformerContext> responseTransformer =
        		getTransformerFactory().getTransformer(TLActionResponse.class, CodegenArtifacts.class);
		TLActionRequest sourceRequest = ResourceCodegenUtils.getDeclaredOrInheritedRequest( source );
		List<TLActionResponse> sourceResponses = ResourceCodegenUtils.getInheritedResponses( source );
		SwaggerParameter bodyParam = createBodyParameter( sourceRequest );
		SwaggerOperation swaggerOp = new SwaggerOperation();
		
		swaggerOp.setOperationId( source.getActionId() );
		swaggerOp.setSummary( source.getOwner().getName() + " - " + source.getActionId() );
		transformDocumentation( source, swaggerOp );
		swaggerOp.setDeprecated( DocumentationFinder.isDeprecated( source ) );
		
		for (TLMimeType produces : sourceRequest.getMimeTypes()) {
			swaggerOp.getProduces().add( produces.toContentType() );
		}
		for (TLMimeType consumes : getResponseMimeTypes( sourceResponses )) {
			swaggerOp.getConsumes().add( consumes.toContentType() );
		}
		
		if (sourceRequest.getParamGroup() != null) {
	        ObjectTransformer<TLParameter,SwaggerParameter,CodeGenerationTransformerContext> paramTransformer =
	        		getTransformerFactory().getTransformer(TLParameter.class, SwaggerParameter.class);
	        
			for (TLParameter sourceParam : sourceRequest.getParamGroup().getParameters()) {
				swaggerOp.getParameters().add( paramTransformer.transform( sourceParam ) );
			}
		}
		if (bodyParam != null) {
			swaggerOp.getParameters().add( bodyParam );
		}
		
		for (TLActionResponse response : ResourceCodegenUtils.getInheritedResponses( source )) {
			swaggerOp.getResponses().addAll( responseTransformer.transform( response )
					.getArtifactsOfType( SwaggerResponse.class ) );
		}
		return swaggerOp;
	}
	
	/**
	 * Returns the union of MIME types that are supported by the given responses.
	 * 
	 * @param sourceResponses  the list of responses for which to return response MIME types
	 * @return List<TLMimeType>
	 */
	private List<TLMimeType> getResponseMimeTypes(List<TLActionResponse> sourceResponses) {
		List<TLMimeType> mimeTypes = new ArrayList<>();
		
		for (TLActionResponse response : sourceResponses) {
			for (TLMimeType mimeType : response.getMimeTypes()) {
				if (!mimeTypes.contains( mimeType )) {
					mimeTypes.add( mimeType );
				}
			}
		}
		return mimeTypes;
	}
	
	/**
	 * If the request specifies a payload, this method will return the Swagger parameter
	 * that specifies its content.  The no payload is specified, this method will return
	 * null.
	 * 
	 * @param request  the action request for which to return a body parameter
	 * @return SwaggerParameter
	 */
	private SwaggerParameter createBodyParameter(TLActionRequest request) {
		SwaggerParameter param = null;
		
		if (request.getPayloadType() != null) {
			TLActionFacet actionFacet = request.getPayloadType();
	        FacetJsonSchemaDelegateFactory delegateFactory = new FacetJsonSchemaDelegateFactory( context );
	        FacetJsonSchemaDelegate<?> delegate = delegateFactory.getDelegate( actionFacet );
			NamedEntity referencedEntity = null;
			boolean isRequired = false;
			
			if (delegate.hasContent() || (actionFacet.getReferenceType() != TLReferenceType.NONE)) {
				if (delegate.hasContent() || (actionFacet.getReferenceRepeat() != 0)) {
					referencedEntity = request;
					isRequired = true;
					
				} else {
					TLProperty boElement = ResourceCodegenUtils.getBusinessObjectElement( actionFacet );
					
					referencedEntity = boElement.getType();
					isRequired = boElement.isMandatory();
				}
			}
			
			if (referencedEntity != null) {
				param = new SwaggerParameter();
				param.setRequired( isRequired );
				param.setIn( SwaggerParamType.BODY );
				param.setName( JsonSchemaNamingUtils.getGlobalDefinitionName( referencedEntity ) );
				param.setRequestSchema( new JsonSchemaReference(
						jsonUtils.getSchemaReferencePath( referencedEntity, request ) ));
				
				if (referencedEntity instanceof TLDocumentationOwner) {
					transformDocumentation( (TLDocumentationOwner) referencedEntity, param );
				}
			}
			
		}
		return param;
	}
	
}

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
import org.opentravel.schemacompiler.codegen.impl.QualifiedAction;
import org.opentravel.schemacompiler.codegen.impl.QualifiedParameter;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerOperation;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParamType;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParameter;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerResponse;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerXmlSchemaRef;
import org.opentravel.schemacompiler.codegen.util.JsonSchemaNamingUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>QualifiedAction</code> objects to the Swagger model
 * objects used to produce the output.
 */
public class TLActionSwaggerTransformer extends AbstractSwaggerCodegenTransformer<QualifiedAction,SwaggerOperation> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public SwaggerOperation transform(QualifiedAction source) {
        ObjectTransformer<QualifiedParameter,SwaggerParameter,CodeGenerationTransformerContext> paramTransformer =
        		getTransformerFactory().getTransformer(QualifiedParameter.class, SwaggerParameter.class);
        ObjectTransformer<TLActionResponse,CodegenArtifacts,CodeGenerationTransformerContext> responseTransformer =
        		getTransformerFactory().getTransformer(TLActionResponse.class, CodegenArtifacts.class);
		List<TLResourceParentRef> sourceParentRefs = source.getParentRefs();
		TLAction sourceAction = source.getAction();
		TLActionRequest sourceRequest = ResourceCodegenUtils.getDeclaredOrInheritedRequest( sourceAction );
		List<TLActionResponse> sourceResponses = ResourceCodegenUtils.getInheritedResponses( sourceAction );
		SwaggerParameter bodyParam = createBodyParameter( sourceRequest );
		SwaggerOperation swaggerOp = new SwaggerOperation();
		StringBuilder operationId = new StringBuilder( sourceAction.getActionId() );
		
		for (TLResourceParentRef parentRef : sourceParentRefs) {
			if (operationId.length() > 0) {
				operationId.append("_");
			}
			operationId.append( parentRef.getParentResourceName() );
		}
		swaggerOp.setOperationId( operationId.toString() );
		swaggerOp.setSummary( sourceAction.getOwner().getName() + " - " + sourceAction.getActionId() );
		transformDocumentation( sourceAction, swaggerOp );
		swaggerOp.setDeprecated( DocumentationFinder.isDeprecated( sourceAction ) );
		
		if (bodyParam != null) {
			for (TLMimeType consumes : sourceRequest.getMimeTypes()) {
				swaggerOp.getConsumes().add( consumes.toContentType() );
			}
		}
		for (TLMimeType produces : getResponseMimeTypes( sourceResponses )) {
			swaggerOp.getProduces().add( produces.toContentType() );
		}
		
		for (QualifiedParameter sourceParam : source.getParameters()) {
			swaggerOp.getParameters().add( paramTransformer.transform( sourceParam ) );
		}
		if (bodyParam != null) {
			swaggerOp.getParameters().add( bodyParam );
		}
		
		for (TLActionResponse response : ResourceCodegenUtils.getInheritedResponses( sourceAction )) {
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
		NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( request.getPayloadType() );
		SwaggerParameter param = null;
		
		if (payloadType != null) {
			TLActionFacet actionFacet = request.getPayloadType(); // Not the same as the 'payloadType' variable above
			boolean isRequired = true;
			
			if ((payloadType != actionFacet) &&
					(actionFacet.getReferenceType() == TLReferenceType.OPTIONAL)) {
				isRequired = false;
			}
			param = new SwaggerParameter();
			param.setRequired( isRequired );
			param.setIn( SwaggerParamType.BODY );
			param.setName( JsonSchemaNamingUtils.getGlobalDefinitionName( payloadType ) );
			
			if (containsSupportedType( request.getMimeTypes(), TLMimeType.TEXT_JSON, TLMimeType.APPLICATION_JSON )) {
				param.setRequestSchema( new JsonSchemaReference(
						jsonUtils.getSchemaReferencePath( payloadType, null ) ));
			}
			if (containsSupportedType( request.getMimeTypes(), TLMimeType.TEXT_XML, TLMimeType.APPLICATION_XML )) {
				param.setRequestXmlSchema( new SwaggerXmlSchemaRef(
						jsonUtils.getXmlSchemaReferencePath( payloadType ) ) );
			}
			
			if (payloadType instanceof TLDocumentationOwner) {
				transformDocumentation( (TLDocumentationOwner) payloadType, param );
			}
		}
		return param;
	}
	
}

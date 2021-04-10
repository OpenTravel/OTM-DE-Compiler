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

package org.opentravel.schemacompiler.codegen.openapi;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.impl.QualifiedAction;
import org.opentravel.schemacompiler.codegen.impl.QualifiedParameter;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiOperation;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiParameter;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiRequestBody;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiResponse;
import org.opentravel.schemacompiler.codegen.swagger.AbstractSwaggerCodegenTransformer;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.transform.ObjectTransformer;

import java.util.List;

/**
 * Performs the translation from <code>QualifiedAction</code> objects to the OpenAPI model objects used to produce the
 * output.
 */
public class TLActionOpenApiTransformer extends AbstractSwaggerCodegenTransformer<QualifiedAction,OpenApiOperation> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public OpenApiOperation transform(QualifiedAction source) {
        ObjectTransformer<QualifiedParameter,OpenApiParameter,CodeGenerationTransformerContext> paramTransformer =
            getTransformerFactory().getTransformer( QualifiedParameter.class, OpenApiParameter.class );
        ObjectTransformer<TLActionRequest,OpenApiRequestBody,CodeGenerationTransformerContext> requestTransformer =
            getTransformerFactory().getTransformer( TLActionRequest.class, OpenApiRequestBody.class );
        ObjectTransformer<TLActionResponse,CodegenArtifacts,CodeGenerationTransformerContext> responseTransformer =
            getTransformerFactory().getTransformer( TLActionResponse.class, CodegenArtifacts.class );
        TLAction sourceAction = source.getAction();
        TLActionRequest sourceRequest = ResourceCodegenUtils.getDeclaredOrInheritedRequest( sourceAction );
        List<TLActionResponse> sourceResponses = ResourceCodegenUtils.getInheritedResponses( sourceAction );
        OpenApiOperation openapiOp = new OpenApiOperation();

        openapiOp.setOperationId( source.getActionId() );
        openapiOp.setSummary( sourceAction.getOwner().getName() + " - " + sourceAction.getActionId() );
        transformDocumentation( sourceAction, openapiOp );
        openapiOp.setDeprecated( DocumentationFinder.isDeprecated( sourceAction ) );

        for (QualifiedParameter sourceParam : source.getParameters()) {
            openapiOp.getParameters().add( paramTransformer.transform( sourceParam ) );
        }
        if (sourceRequest != null) {
            openapiOp.setRequestBody( requestTransformer.transform( sourceRequest ) );
        }
        for (TLActionResponse sourceResponse : sourceResponses) {
            CodegenArtifacts artifacts = responseTransformer.transform( sourceResponse );

            openapiOp.getResponses().addAll( artifacts.getArtifactsOfType( OpenApiResponse.class ) );
        }
        return openapiOp;
    }

}

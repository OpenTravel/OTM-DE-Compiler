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

import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiMediaType;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiResponse;
import org.opentravel.schemacompiler.codegen.swagger.AbstractSwaggerCodegenTransformer;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLMimeType;

/**
 * Performs the translation from <code>TLActionResponse</code> objects to the OpenAPI model objects used to produce the
 * output.
 */
public class TLActionResponseOpenApiTransformer
    extends AbstractSwaggerCodegenTransformer<TLActionResponse,CodegenArtifacts> {

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
     * Constructs a new OpenAPI response for the indicated status code. If the status code is null, a default response
     * will be created.
     * 
     * @param source the action response from which to construct the OpenAPI response
     * @param statusCode the status code for the OpenAPI response
     * @return OpenApiResponse
     */
    private OpenApiResponse createResponse(TLActionResponse source, Integer statusCode) {
        NamedEntity payloadType =
            getLatestMinorVersion( ResourceCodegenUtils.getPayloadType( source.getPayloadType() ) );
        OpenApiResponse response = new OpenApiResponse();

        response.setDefaultResponse( (statusCode == null) );
        response.setStatusCode( statusCode );
        transformDocumentation( source, response );

        for (TLMimeType mimeType : source.getMimeTypes()) {
            OpenApiMediaType mediaType = new OpenApiMediaType();

            switch (mimeType) {
                case APPLICATION_JSON:
                case TEXT_JSON:
                    mediaType.setMediaType( "application/json" );
                    mediaType.setRequestType(
                        new JsonSchemaReference( jsonUtils.getSchemaReferencePath( payloadType, null ) ) );
                    break;
                case APPLICATION_XML:
                case TEXT_XML:
                    mediaType.setMediaType( "application/xml" );
                    mediaType.setRequestType(
                        new JsonSchemaReference( jsonUtils.getXmlSchemaReferencePath( payloadType ) ) );
                    break;
                default:
                    break;
            }
            response.getContent().add( mediaType );
        }

        // TODO: Implement Global Response Headers for OpenAPI/Swagger bindings
        // if ((swaggerBindings != null) && (swaggerBindings.getGlobalResponseHeaders() != null)) {
        // response.getHeaders().addAll( swaggerBindings.getGlobalResponseHeaders() );
        // }
        return response;
    }

}

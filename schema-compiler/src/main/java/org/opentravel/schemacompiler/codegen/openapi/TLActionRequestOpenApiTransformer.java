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

import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiMediaType;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiRequestBody;
import org.opentravel.schemacompiler.codegen.swagger.AbstractSwaggerCodegenTransformer;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.model.TLReferenceType;

/**
 * Performs the translation from <code>TLActionRequest</code> objects to the OpenAPI model objects used to produce the
 * output.
 */
public class TLActionRequestOpenApiTransformer
    extends AbstractSwaggerCodegenTransformer<TLActionRequest,OpenApiRequestBody> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public OpenApiRequestBody transform(TLActionRequest source) {
        NamedEntity payloadType =
            getLatestMinorVersion( ResourceCodegenUtils.getPayloadType( source.getPayloadType() ) );
        OpenApiRequestBody requestBody = null;

        if (payloadType != null) {
            TLActionFacet actionFacet = source.getPayloadType(); // Not the same as the 'payloadType' variable above
            boolean isRequired = true;

            if ((payloadType != actionFacet) && (actionFacet.getReferenceType() == TLReferenceType.OPTIONAL)) {
                isRequired = false;
            }
            requestBody = new OpenApiRequestBody();
            requestBody.setRequired( isRequired );

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
                requestBody.getContent().add( mediaType );
            }

            if (payloadType instanceof TLDocumentationOwner) {
                transformDocumentation( (TLDocumentationOwner) payloadType, requestBody );
            }
        }
        return requestBody;
    }

}

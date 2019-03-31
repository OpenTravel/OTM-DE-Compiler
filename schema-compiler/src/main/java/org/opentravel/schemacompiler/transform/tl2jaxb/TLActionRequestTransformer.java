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

package org.opentravel.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_05.ActionRequest;
import org.opentravel.ns.ota2.librarymodel_v01_05.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_05.HttpMethod;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLActionRequest</code> type to the <code>ActionRequest</code>
 * type.
 *
 * @author S. Livezey
 */
public class TLActionRequestTransformer extends TLComplexTypeTransformer<TLActionRequest,ActionRequest> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public ActionRequest transform(TLActionRequest source) {
        TLActionFacet sourcePayloadType = source.getPayloadType();
        ActionRequest request = new ActionRequest();

        request.setHttpMethod( transformHttpMethod( source.getHttpMethod() ) );
        request.setPathTemplate( trimString( source.getPathTemplate(), false ) );
        request.getMimeTypes().addAll( TLActionTransformer.transformMimeTypes( source.getMimeTypes() ) );

        if (source.getParamGroup() != null) {
            request.setParamGroup( source.getParamGroup().getName() );
        } else {
            request.setParamGroup( source.getParamGroupName() );
        }

        if (sourcePayloadType != null) {
            request.setPayloadType( context.getSymbolResolver().buildEntityName( sourcePayloadType.getNamespace(),
                sourcePayloadType.getLocalName() ) );
        }
        if (request.getPayloadType() == null) {
            request.setPayloadType( source.getPayloadTypeName() );
        }

        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
                getTransformerFactory().getTransformer( TLDocumentation.class, Documentation.class );

            request.setDocumentation( docTransformer.transform( source.getDocumentation() ) );
        }

        return request;
    }

    /**
     * Transforms the given <code>TLHttpMethod</code> value.
     * 
     * @param sourceMethod the enumeration value to transform
     * @return HttpMethod
     */
    private HttpMethod transformHttpMethod(TLHttpMethod sourceMethod) {
        HttpMethod method;

        if (sourceMethod != null) {
            switch (sourceMethod) {
                case GET:
                    method = HttpMethod.GET;
                    break;
                case POST:
                    method = HttpMethod.POST;
                    break;
                case PUT:
                    method = HttpMethod.PUT;
                    break;
                case DELETE:
                    method = HttpMethod.DELETE;
                    break;
                case HEAD:
                    method = HttpMethod.HEAD;
                    break;
                case OPTIONS:
                    method = HttpMethod.OPTIONS;
                    break;
                case PATCH:
                    method = HttpMethod.PATCH;
                    break;
                default:
                    method = null;
                    break;
            }
        } else {
            method = null;
        }
        return method;
    }

}

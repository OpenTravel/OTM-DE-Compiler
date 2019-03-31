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

package org.opentravel.schemacompiler.transform.jaxb15_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_05.Action;
import org.opentravel.ns.ota2.librarymodel_v01_05.ActionRequest;
import org.opentravel.ns.ota2.librarymodel_v01_05.ActionResponse;
import org.opentravel.ns.ota2.librarymodel_v01_05.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_05.MimeType;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the transformation of objects from the <code>Action</code> type to the <code>TLAction</code> type.
 *
 * @author S. Livezey
 */
public class ActionTransformer extends ComplexTypeTransformer<Action,TLAction> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLAction transform(Action source) {
        ObjectTransformer<ActionResponse,TLActionResponse,DefaultTransformerContext> responseTransformer =
            getTransformerFactory().getTransformer( ActionResponse.class, TLActionResponse.class );
        TLAction action = new TLAction();

        action.setActionId( trimString( source.getActionId() ) );
        action.setCommonAction( source.isCommon() );

        if (source.getActionRequest() != null) {
            ObjectTransformer<ActionRequest,TLActionRequest,DefaultTransformerContext> requestTransformer =
                getTransformerFactory().getTransformer( ActionRequest.class, TLActionRequest.class );

            action.setRequest( requestTransformer.transform( source.getActionRequest() ) );
        }

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
                getTransformerFactory().getTransformer( Documentation.class, TLDocumentation.class );

            action.setDocumentation( docTransformer.transform( source.getDocumentation() ) );
        }

        for (ActionResponse sourceResponse : source.getActionResponse()) {
            action.addResponse( responseTransformer.transform( sourceResponse ) );
        }

        return action;
    }

    /**
     * Transforms the list of JAXB MIME types.
     * 
     * @param sourceMimeTypes the list of JAXB MIME types to transform
     * @return List&lt;TLMimeType&gt;
     */
    protected static List<TLMimeType> transformMimeTypes(List<MimeType> sourceMimeTypes) {
        List<TLMimeType> mimeTypes = new ArrayList<>();

        if (sourceMimeTypes != null) {
            for (MimeType sourceMimeType : sourceMimeTypes) {
                TLMimeType mimeType;

                if (sourceMimeType != null) {
                    switch (sourceMimeType) {
                        case APPLICATION_XML:
                            mimeType = TLMimeType.APPLICATION_XML;
                            break;
                        case TEXT_XML:
                            mimeType = TLMimeType.TEXT_XML;
                            break;
                        case APPLICATION_JSON:
                            mimeType = TLMimeType.APPLICATION_JSON;
                            break;
                        case TEXT_JSON:
                            mimeType = TLMimeType.TEXT_JSON;
                            break;
                        default:
                            mimeType = null;
                            break;
                    }
                } else {
                    mimeType = null;
                }

                if ((mimeType != null) && !mimeTypes.contains( mimeType )) {
                    mimeTypes.add( mimeType );
                }
            }
        }
        return mimeTypes;
    }

}

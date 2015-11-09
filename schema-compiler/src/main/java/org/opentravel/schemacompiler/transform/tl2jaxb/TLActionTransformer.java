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

import java.util.ArrayList;
import java.util.List;

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
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLAction</code> type to the
 * <code>Action</code> type.
 *
 * @author S. Livezey
 */
public class TLActionTransformer extends TLComplexTypeTransformer<TLAction,Action> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public Action transform(TLAction source) {
        ObjectTransformer<TLActionResponse, ActionResponse, SymbolResolverTransformerContext> responseTransformer =
        		getTransformerFactory().getTransformer(TLActionResponse.class, ActionResponse.class);
		Action action = new Action();
		
		action.setActionId(trimString(source.getActionId(), false));
		action.setCommon(source.isCommonAction());
		
        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation, Documentation, SymbolResolverTransformerContext> docTransformer =
            		getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
            
            action.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
        
        if (source.getRequest() != null) {
            ObjectTransformer<TLActionRequest, ActionRequest, SymbolResolverTransformerContext> requestTransformer =
            		getTransformerFactory().getTransformer(TLActionRequest.class, ActionRequest.class);
        	
            action.setActionRequest(requestTransformer.transform(source.getRequest()));
        }
        
        for (TLActionResponse sourceResponse : source.getResponses()) {
        	action.getActionResponse().add(responseTransformer.transform(sourceResponse));
        }
        
		return action;
	}
	
	/**
	 * Transforms the given list of MIME types.
	 * 
	 * @param sourceMimeTypes  the list of enumeration values to transform
	 * @return List<MimeType>
	 */
	protected static List<MimeType> transformMimeTypes(List<TLMimeType> sourceMimeTypes) {
		List<MimeType> mimeTypes = new ArrayList<>();
		
		if (sourceMimeTypes != null) {
			for (TLMimeType sourceMimeType : sourceMimeTypes) {
				if (sourceMimeType != null) {
					MimeType mimeType;
					
					switch (sourceMimeType) {
						case APPLICATION_XML:
							mimeType = MimeType.APPLICATION_XML;
							break;
						case TEXT_XML:
							mimeType = MimeType.TEXT_XML;
							break;
						case APPLICATION_JSON:
							mimeType = MimeType.APPLICATION_JSON;
							break;
						case TEXT_JSON:
							mimeType = MimeType.TEXT_JSON;
							break;
						default:
							mimeType = null;
							break;
					}
					if (mimeType != null) {
						mimeTypes.add(mimeType);
					}
				}
			}
		}
		return mimeTypes;
	}
	
}

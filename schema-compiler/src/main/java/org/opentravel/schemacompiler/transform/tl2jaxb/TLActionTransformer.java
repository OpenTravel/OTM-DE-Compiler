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
import org.opentravel.ns.ota2.librarymodel_v01_05.HttpMethod;
import org.opentravel.ns.ota2.librarymodel_v01_05.MimeType;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.model.TLParamGroup;
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
        ObjectTransformer<TLDocumentation, Documentation, SymbolResolverTransformerContext> docTransformer =
        		getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
        ObjectTransformer<TLActionResponse, ActionResponse, SymbolResolverTransformerContext> responseTransformer =
        		getTransformerFactory().getTransformer(TLActionResponse.class, ActionResponse.class);
		TLActionRequest sourceRequest = source.getRequest();
		TLParamGroup sourceParamGroup = sourceRequest.getParamGroup();
		TLActionFacet sourceActionFacet = sourceRequest.getActionFacet();
		ActionRequest request = new ActionRequest();
		Action action = new Action();
		
		action.setActionId(trimString(source.getActionId(), false));
		action.setPathTemplate(trimString(source.getPathTemplate(), false));
		action.setActionRequest(request);
		
		request.setHttpMethod(transformHttpMethod(sourceRequest.getHttpMethod()));
		request.getMimeTypes().addAll(transformMimeTypes(sourceRequest.getMimeTypes()));
		
		if (sourceParamGroup != null) {
			request.setParamGroup(sourceParamGroup.getName());
		} else {
			request.setParamGroup(sourceRequest.getParamGroupName());
		}
		
		if (sourceActionFacet != null) {
			request.setActionFacet(context.getSymbolResolver().buildEntityName(
					sourceActionFacet.getNamespace(), sourceActionFacet.getLocalName()));
		} else {
			request.setActionFacet(sourceRequest.getActionFacetName());
		}
		
        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            action.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
        if ((sourceRequest.getDocumentation() != null) && !sourceRequest.getDocumentation().isEmpty()) {
        	request.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
        
        for (TLActionResponse sourceResponse : source.getResponses()) {
        	action.getActionResponse().add(responseTransformer.transform(sourceResponse));
        }
        
		return action;
	}
	
	/**
	 * Transforms the given <code>TLHttpMethod</code> value.
	 * 
	 * @param sourceMethod  the enumeration value to transform
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

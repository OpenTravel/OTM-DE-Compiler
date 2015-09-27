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

import org.opentravel.ns.ota2.librarymodel_v01_05.ActionRequest;
import org.opentravel.ns.ota2.librarymodel_v01_05.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_05.HttpMethod;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;

/**
 * Handles the transformation of objects from the <code>ActionRequest</code> type to the
 * <code>TLActionRequest</code> type.
 *
 * @author S. Livezey
 */
public class ActionRequestTransformer extends ComplexTypeTransformer<ActionRequest,TLActionRequest> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLActionRequest transform(ActionRequest source) {
		TLActionRequest request = new TLActionRequest();
		
		request.setHttpMethod(transformHttpMethod(source.getHttpMethod()));
		request.setParamGroupName(trimString(source.getParamGroup()));
		request.setActionFacetName(trimString(source.getActionFacet()));
		request.setMimeTypes(ActionTransformer.transformMimeTypes(source.getMimeTypes()));
		
        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation, TLDocumentation, DefaultTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(Documentation.class, TLDocumentation.class);

            request.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
        
		return request;
	}
	
	/**
	 * Transforms the given <code>HttpMethod</code> value.
	 * 
	 * @param sourceHttpMethod  the JAXB HTTP method value
	 * @return TLHttpMethod
	 */
	protected static TLHttpMethod transformHttpMethod(HttpMethod sourceHttpMethod) {
		TLHttpMethod httpMethod;
		
		if (sourceHttpMethod != null) {
			switch (sourceHttpMethod) {
				case GET:
					httpMethod = TLHttpMethod.GET;
					break;
				case POST:
					httpMethod = TLHttpMethod.POST;
					break;
				case PUT:
					httpMethod = TLHttpMethod.PUT;
					break;
				case DELETE:
					httpMethod = TLHttpMethod.DELETE;
					break;
				case HEAD:
					httpMethod = TLHttpMethod.HEAD;
					break;
				case OPTIONS:
					httpMethod = TLHttpMethod.OPTIONS;
					break;
				case PATCH:
					httpMethod = TLHttpMethod.PATCH;
					break;
				default:
					httpMethod = null;
					break;
			}
		} else {
			httpMethod = null;
		}
		return httpMethod;
	}
	
}

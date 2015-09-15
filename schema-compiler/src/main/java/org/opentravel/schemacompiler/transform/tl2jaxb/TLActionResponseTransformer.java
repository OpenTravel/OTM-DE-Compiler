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

import org.opentravel.ns.ota2.librarymodel_v01_05.ActionResponse;
import org.opentravel.ns.ota2.librarymodel_v01_05.Documentation;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLActionResponse</code> type to the
 * <code>ActionResponse</code> type.
 *
 * @author S. Livezey
 */
public class TLActionResponseTransformer extends TLComplexTypeTransformer<TLActionResponse,ActionResponse> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public ActionResponse transform(TLActionResponse source) {
		TLActionFacet sourceActionFacet = source.getActionFacet();
		ActionResponse response = new ActionResponse();
		
		response.getStatusCodes().addAll(source.getStatusCodes());
		response.getMimeTypes().addAll(TLActionTransformer.transformMimeTypes(source.getMimeTypes()));
		
		if (sourceActionFacet != null) {
			response.setActionFacet(context.getSymbolResolver().buildEntityName(
					sourceActionFacet.getNamespace(), sourceActionFacet.getLocalName()));
		} else {
			response.setActionFacet(source.getActionFacetName());
		}
		
        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation, Documentation, SymbolResolverTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(TLDocumentation.class, Documentation.class);

            response.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
        
		return response;
	}
	
}

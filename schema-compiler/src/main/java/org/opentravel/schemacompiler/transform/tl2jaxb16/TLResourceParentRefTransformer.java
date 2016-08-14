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
package org.opentravel.schemacompiler.transform.tl2jaxb16;

import org.opentravel.ns.ota2.librarymodel_v01_06.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_06.ResourceParentRef;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLResourceParentRef</code> type to the
 * <code>ResourceParentRef</code> type.
 *
 * @author S. Livezey
 */
public class TLResourceParentRefTransformer extends TLComplexTypeTransformer<TLResourceParentRef,ResourceParentRef> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public ResourceParentRef transform(TLResourceParentRef source) {
		TLResource sourceParentResource = source.getParentResource();
		TLParamGroup sourceParentParamGroup = source.getParentParamGroup();
		ResourceParentRef parentRef = new ResourceParentRef();
		
		parentRef.setPathTemplate(trimString(source.getPathTemplate(), false));
		
        if (sourceParentResource != null) {
        	parentRef.setParent(context.getSymbolResolver().buildEntityName(
        			sourceParentResource.getNamespace(), sourceParentResource.getLocalName()));
        } else {
        	parentRef.setParent(trimString(source.getParentResourceName(), false));
        }
		
        if (sourceParentParamGroup != null) {
        	parentRef.setParentParamGroup(sourceParentParamGroup.getName());
        } else {
        	parentRef.setParentParamGroup(trimString(source.getParentParamGroupName(), false));
        }
		
        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation, Documentation, SymbolResolverTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(TLDocumentation.class, Documentation.class);

            parentRef.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
        
		return parentRef;
	}
	
}

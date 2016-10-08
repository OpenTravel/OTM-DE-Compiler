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

import org.opentravel.ns.ota2.librarymodel_v01_06.Action;
import org.opentravel.ns.ota2.librarymodel_v01_06.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_06.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_06.FacetAction;
import org.opentravel.ns.ota2.librarymodel_v01_06.ParamGroup;
import org.opentravel.ns.ota2.librarymodel_v01_06.Resource;
import org.opentravel.ns.ota2.librarymodel_v01_06.ResourceParentRef;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLResource</code> type to the
 * <code>Resource</code> type.
 *
 * @author S. Livezey
 */
public class TLResourceTransformer extends TLComplexTypeTransformer<TLResource,Resource> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public Resource transform(TLResource source) {
        ObjectTransformer<TLResourceParentRef, ResourceParentRef, SymbolResolverTransformerContext> parentRefTransformer =
        		getTransformerFactory().getTransformer(TLResourceParentRef.class, ResourceParentRef.class);
        ObjectTransformer<TLParamGroup, ParamGroup, SymbolResolverTransformerContext> paramGroupTransformer =
        		getTransformerFactory().getTransformer(TLParamGroup.class, ParamGroup.class);
        ObjectTransformer<TLActionFacet, FacetAction, SymbolResolverTransformerContext> actionFacetTransformer =
        		getTransformerFactory().getTransformer(TLActionFacet.class, FacetAction.class);
        ObjectTransformer<TLAction, Action, SymbolResolverTransformerContext> actionTransformer =
        		getTransformerFactory().getTransformer(TLAction.class, Action.class);
        TLBusinessObject boRef = source.getBusinessObjectRef();
		Resource resource = new Resource();
        
		resource.setName(trimString(source.getName(), false));
		resource.setBasePath(trimString(source.getBasePath()));
		resource.setAbstract(source.isAbstract());
		resource.setFirstClass(source.isFirstClass());
		
		if (boRef != null) {
			resource.setBusinessObjectRef(context.getSymbolResolver().buildEntityName(
					boRef.getNamespace(), boRef.getLocalName()));
        }
        if (resource.getBusinessObjectRef() == null) {
			resource.setBusinessObjectRef(trimString(source.getBusinessObjectRefName(), false));
		}
		
        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation, Documentation, SymbolResolverTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(TLDocumentation.class, Documentation.class);

            resource.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
        
        if (source.getExtension() != null) {
            ObjectTransformer<TLExtension, Extension, SymbolResolverTransformerContext> extensionTransformer = getTransformerFactory()
                    .getTransformer(TLExtension.class, Extension.class);

            resource.setExtension(extensionTransformer.transform(source.getExtension()));
        }
        
        for (TLResourceParentRef sourceParentRef : source.getParentRefs()) {
        	resource.getResourceParentRef().add(parentRefTransformer.transform(sourceParentRef));
        }
        for (TLParamGroup sourceParamGroup : source.getParamGroups()) {
        	resource.getParamGroup().add(paramGroupTransformer.transform(sourceParamGroup));
        }
        for (TLActionFacet sourceFacet : source.getActionFacets()) {
        	resource.getActionFacet().add(actionFacetTransformer.transform(sourceFacet));
        }
        for (TLAction sourceAction : source.getActions()) {
        	resource.getAction().add(actionTransformer.transform(sourceAction));
        }
        
		return resource;
	}
	
}

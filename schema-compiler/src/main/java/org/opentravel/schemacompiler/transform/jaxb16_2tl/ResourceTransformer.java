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
package org.opentravel.schemacompiler.transform.jaxb16_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_06.Action;
import org.opentravel.ns.ota2.librarymodel_v01_06.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_06.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_06.FacetAction;
import org.opentravel.ns.ota2.librarymodel_v01_06.ParamGroup;
import org.opentravel.ns.ota2.librarymodel_v01_06.Resource;
import org.opentravel.ns.ota2.librarymodel_v01_06.ResourceParentRef;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;

/**
 * Handles the transformation of objects from the <code>Resource</code> type to the
 * <code>TLResource</code> type.
 *
 * @author S. Livezey
 */
public class ResourceTransformer extends ComplexTypeTransformer<Resource,TLResource> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLResource transform(Resource source) {
        ObjectTransformer<ResourceParentRef, TLResourceParentRef, DefaultTransformerContext> parentRefTransformer =
        		getTransformerFactory().getTransformer(ResourceParentRef.class, TLResourceParentRef.class);
        ObjectTransformer<ParamGroup, TLParamGroup, DefaultTransformerContext> paramGroupTransformer =
        		getTransformerFactory().getTransformer(ParamGroup.class, TLParamGroup.class);
        ObjectTransformer<FacetAction, TLActionFacet, DefaultTransformerContext> actionFacetTransformer =
        		getTransformerFactory().getTransformer(FacetAction.class, TLActionFacet.class);
        ObjectTransformer<Action, TLAction, DefaultTransformerContext> actionTransformer =
        		getTransformerFactory().getTransformer(Action.class, TLAction.class);
		TLResource resource = new TLResource();
		
		resource.setName(trimString(source.getName()));
		resource.setBasePath(trimString(source.getBasePath()));
		resource.setAbstract(source.isAbstract());
		resource.setFirstClass(source.isFirstClass());
		resource.setBusinessObjectRefName(source.getBusinessObjectRef());

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation, TLDocumentation, DefaultTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(Documentation.class, TLDocumentation.class);

            resource.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }

        if (source.getExtension() != null) {
            ObjectTransformer<Extension, TLExtension, DefaultTransformerContext> extensionTransformer = getTransformerFactory()
                    .getTransformer(Extension.class, TLExtension.class);

            resource.setExtension(extensionTransformer.transform(source.getExtension()));
        }
        
        for (ResourceParentRef sourceParentRef : source.getResourceParentRef()) {
        	resource.addParentRef(parentRefTransformer.transform(sourceParentRef));
        }
        for (ParamGroup sourceParamGroup : source.getParamGroup()) {
        	resource.addParamGroup(paramGroupTransformer.transform(sourceParamGroup));
        }
        for (FacetAction sourceFacet : source.getActionFacet()) {
        	resource.addActionFacet(actionFacetTransformer.transform(sourceFacet));
        }
        for (Action sourceAction : source.getAction()) {
        	resource.addAction(actionTransformer.transform(sourceAction));
        }
        return resource;
	}
	
}

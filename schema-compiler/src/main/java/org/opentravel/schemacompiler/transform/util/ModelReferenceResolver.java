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

package org.opentravel.schemacompiler.transform.util;

import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Provides a global static method for resolving all entity references within
 * an OTM model.
 */
public final class ModelReferenceResolver {
	
	/**
	 * Resolves all entity references within the given OTM model.
	 * 
	 * @param model  the model for which to resolve references
	 */
	public static void resolveReferences(TLModel model) {
        boolean listenerFlag = model.isListenersEnabled();
        try {
        	model.setListenersEnabled(false);
        	resolveContextualFacetOwners(model);
        	
        	EntityReferenceResolutionVisitor visitor = new EntityReferenceResolutionVisitor(model);
        	
            ModelNavigator.navigate(model, visitor);
            resolveParameters(model, visitor);
            
        } finally {
        	model.setListenersEnabled(listenerFlag);
        }
	}
	
    /**
     * Visits and resolves the contextual facets owners within the given model.  This is
     * necessary as a first-pass during model resolution since the qualified name of the
     * contextual facets is composed of the owner name.  Therefore, it is necessary to
     * resolve the facet owners and rebuid the symbol table prior to resolving other
     * entity references.
     * 
     * @param model  the model for which to resolve contextual facet owners
     */
    private static void resolveContextualFacetOwners(TLModel model) {
        ModelNavigator.navigate(model, new ContextualFacetResolutionVisitor(model));
    }
    
    /**
     * Visits and resolves the parameters of each library resource.  This covers
     * an edge case that causes some parameters not to be resolved on the initial
     * visit since some of the field references may not yet have been resolved on
     * the first pass.
     * 
     * @param model  the model for which to resolve parameter field references
     */
    private static void resolveParameters(TLModel model, EntityReferenceResolutionVisitor visitor) {
    	
    	visitor.reset();
    	
        for (TLLibrary library : model.getUserDefinedLibraries()) {
        	for (TLResource resource : library.getResourceTypes()) {
        		for (TLParamGroup paramGroup : resource.getParamGroups()) {
        			for (TLParameter param : paramGroup.getParameters()) {
                		visitor.visitParameter(param);
        			}
        		}
        	}
        }
    }
    
}

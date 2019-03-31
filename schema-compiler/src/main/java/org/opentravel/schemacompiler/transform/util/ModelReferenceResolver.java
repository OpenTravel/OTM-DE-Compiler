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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a global static method for resolving all entity references within an OTM model.
 */
public final class ModelReferenceResolver {

    private static Logger log = LoggerFactory.getLogger( ModelReferenceResolver.class );

    private static Class<? extends ObsoleteBuiltInVisitor> obsoleteBuiltInVisitorType = ObsoleteBuiltInVisitor.class;

    /**
     * Private constructor to prevent instantiation.
     */
    private ModelReferenceResolver() {}

    /**
     * Resolves all entity references within the given OTM model.
     * 
     * @param model the model for which to resolve references
     */
    public static void resolveReferences(TLModel model) {
        boolean listenerFlag = model.isListenersEnabled();
        try {
            model.setListenersEnabled( false );
            ContextualFacetResolutionVisitor.resolveReferences( model );
            EntityReferenceResolutionVisitor visitor = new EntityReferenceResolutionVisitor( model );
            ModelNavigator.navigate( model, visitor );
            resolveParameters( model, visitor );
            resolveObsoleteBuiltInReferences( model );

        } finally {
            model.setListenersEnabled( listenerFlag );
        }
    }

    /**
     * Visits and resolves the parameters of each library resource. This covers an edge case that causes some parameters
     * not to be resolved on the initial visit since some of the field references may not yet have been resolved on the
     * first pass.
     * 
     * @param model the model for which to resolve parameter field references
     */
    private static void resolveParameters(TLModel model, EntityReferenceResolutionVisitor visitor) {

        visitor.reset();

        for (TLLibrary library : model.getUserDefinedLibraries()) {
            for (TLResource resource : library.getResourceTypes()) {
                for (TLParamGroup paramGroup : resource.getParamGroups()) {
                    for (TLParameter param : paramGroup.getParameters()) {
                        visitor.visitParameter( param );
                    }
                }
            }
        }
    }

    /**
     * Attempts to resolve any references to the obsolete built-in library that is now managed in the OpenTravel
     * repository. If any missing references are detected, the managed library is loaded and references are resolved
     * from the managed built-in.
     * 
     * @param model the model for which to resolve obsolete build-in references
     */
    private static void resolveObsoleteBuiltInReferences(TLModel model) {
        try {
            ObsoleteBuiltInVisitor visitor = obsoleteBuiltInVisitorType.newInstance();

            ModelNavigator.navigate( model, visitor );
            visitor.resolveObsoleteBuiltInReferences();

        } catch (InstantiationException | IllegalAccessException e) {
            log.error( "Error initializing ObsoleteBuiltInVisitor.", e );
        }
    }

    /**
     * Overrides the type of the <code>ObsoleteBuiltInVisitor</code> class to use during reference resolution
     * processing.
     * 
     * @param obsoleteBuiltInVisitorType the visitor type to use during resolution processing
     */
    public static void setObsoleteBuiltInVisitorType(
        Class<? extends ObsoleteBuiltInVisitor> obsoleteBuiltInVisitorType) {
        ModelReferenceResolver.obsoleteBuiltInVisitorType = obsoleteBuiltInVisitorType;
    }

}

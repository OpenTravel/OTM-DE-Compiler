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

import org.opentravel.ns.ota2.librarymodel_v01_06.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_06.ResourceParentRef;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;

/**
 * Handles the transformation of objects from the <code>ResourceParentRef</code> type to the
 * <code>TLResourceParentRef</code> type.
 *
 * @author S. Livezey
 */
public class ResourceParentRefTransformer extends ComplexTypeTransformer<ResourceParentRef,TLResourceParentRef> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLResourceParentRef transform(ResourceParentRef source) {
        TLResourceParentRef parentRef = new TLResourceParentRef();

        parentRef.setParentResourceName( trimString( source.getParent() ) );
        parentRef.setParentParamGroupName( trimString( source.getParentParamGroup() ) );
        parentRef.setPathTemplate( trimString( source.getPathTemplate() ) );

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
                getTransformerFactory().getTransformer( Documentation.class, TLDocumentation.class );

            parentRef.setDocumentation( docTransformer.transform( source.getDocumentation() ) );
        }

        return parentRef;
    }

}

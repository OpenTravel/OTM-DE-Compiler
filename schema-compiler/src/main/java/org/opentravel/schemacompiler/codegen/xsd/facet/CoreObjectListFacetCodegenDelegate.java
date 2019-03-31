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

package org.opentravel.schemacompiler.codegen.xsd.facet;

import org.opentravel.schemacompiler.model.TLListFacet;
import org.w3._2001.xmlschema.Annotated;

/**
 * Code generation delegate for <code>TLListFacet</code> instances with a facet type of <code>INFO</code> and a facet
 * owner of type <code>TLCoreObject</code>.
 * 
 * @author S. Livezey
 */
public class CoreObjectListFacetCodegenDelegate extends TLListFacetCodegenDelegate {

    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet the source facet
     */
    public CoreObjectListFacetCodegenDelegate(TLListFacet sourceFacet) {
        super( sourceFacet );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#createType()
     */
    @Override
    protected Annotated createType() {
        return null; // No type generated for complex list facet types
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#getLocalBaseFacet()
     */
    @Override
    public TLListFacet getLocalBaseFacet() {
        return null;
    }

}

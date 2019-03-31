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

import org.opentravel.schemacompiler.model.TLFacet;

import javax.xml.namespace.QName;

/**
 * Code generation delegate for <code>TLFacet</code> instances with a facet type of <code>ID</code> and a facet owner of
 * type <code>TLBusinessObject</code>.
 * 
 * @author S. Livezey
 */
public class BusinessObjectIDFacetCodegenDelegate extends BusinessObjectFacetCodegenDelegate {

    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet the source facet
     */
    public BusinessObjectIDFacetCodegenDelegate(TLFacet sourceFacet) {
        super( sourceFacet );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#hasSubstitutionGroupElement()
     */
    @Override
    protected boolean hasSubstitutionGroupElement() {
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#hasNonSubstitutableElement()
     */
    @Override
    public boolean hasNonSubstitutableElement() {
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacet()
     */
    @Override
    public TLFacet getLocalBaseFacet() {
        return null; // No base type for ID facets
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getExtensionPointElement()
     */
    @Override
    public QName getExtensionPointElement() {
        return null;
    }

}

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

import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLListFacet;

/**
 * Base class for facet code generation delegates used to generate code artifacts for
 * <code>TLListFacet</code> model elements.
 * 
 * @author S. Livezey
 */
public abstract class TLListFacetCodegenDelegate extends FacetCodegenDelegate<TLListFacet> {

    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet
     *            the source facet
     */
    public TLListFacetCodegenDelegate(TLListFacet sourceFacet) {
        super(sourceFacet);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#hasContent()
     */
    @Override
    public boolean hasContent() {
        TLAbstractFacet itemFacet = getSourceFacet().getItemFacet();
        boolean result = false;

        if (itemFacet != null) {
            result = new FacetCodegenDelegateFactory(transformerContext).getDelegate(itemFacet)
                    .hasContent();
        }
        return result;
    }

}

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
package org.opentravel.schemacompiler.transform.jaxb13_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_03.FacetContextual;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * Handles the transformation of objects from the <code>FacetContextual</code> type to the
 * <code>TLFacet</code> type.
 * 
 * @author S. Livezey
 */
public class FacetContextualTransformer extends ComplexTypeTransformer<FacetContextual, TLFacet> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLFacet transform(FacetContextual source) {
        final TLFacet facet = new TLFacet();

        facet.setNotExtendable(!(source.getExtendable() != null));
        facet.setContext(trimString(source.getContext()));

        for (TLAttribute attribute : transformAttributes(source.getAttribute())) {
            facet.addAttribute(attribute);
        }
        for (TLProperty element : transformElements(source.getElement())) {
            facet.addElement(element);
        }
        for (TLIndicator indicator : transformIndicators(source.getIndicator())) {
            facet.addIndicator(indicator);
        }
        return facet;
    }

}

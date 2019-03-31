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

import org.opentravel.ns.ota2.librarymodel_v01_06.FacetContextual;
import org.opentravel.ns.ota2.librarymodel_v01_06.FacetContextualType;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * Handles the transformation of objects from the <code>FacetContextual</code> type to the
 * <code>TLContextualFacet</code> type.
 * 
 * @author S. Livezey
 */
public class FacetContextualTransformer extends ComplexTypeTransformer<FacetContextual,TLContextualFacet> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLContextualFacet transform(FacetContextual source) {
        final TLContextualFacet facet = new TLContextualFacet();

        facet.setName( trimString( source.getName() ) );
        facet.setFacetType( getTargetType( source.getType() ) );
        facet.setOwningEntityName( trimString( source.getFacetOwner() ) );
        facet.setNotExtendable( (source.isNotExtendable() != null) && source.isNotExtendable() );
        facet.setFacetNamespace( trimString( source.getFacetNamespace() ) );

        for (TLAttribute attribute : transformAttributes( source.getAttribute() )) {
            facet.addAttribute( attribute );
        }
        for (TLProperty element : transformElements( source.getElement() )) {
            facet.addElement( element );
        }
        for (TLIndicator indicator : transformIndicators( source.getIndicator() )) {
            facet.addIndicator( indicator );
        }
        return facet;
    }

    /**
     * Returns the model facet type that corresponds with the given JAXB contextual facet type.
     * 
     * @param sourceType the JAXB contextual facet type
     * @return TLFacetType
     */
    private TLFacetType getTargetType(FacetContextualType sourceType) {
        TLFacetType targetType = null;

        if (sourceType != null) {
            switch (sourceType) {
                case CHOICE:
                    targetType = TLFacetType.CHOICE;
                    break;
                case CUSTOM:
                    targetType = TLFacetType.CUSTOM;
                    break;
                case QUERY:
                    targetType = TLFacetType.QUERY;
                    break;
                case UPDATE:
                    targetType = TLFacetType.UPDATE;
                    break;
                default:
                    // No default action required
            }
        }
        return targetType;
    }

}

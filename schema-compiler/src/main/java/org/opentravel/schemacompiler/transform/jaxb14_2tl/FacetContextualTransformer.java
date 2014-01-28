package org.opentravel.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.FacetContextual;
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

        facet.setContext(trimString(source.getContext()));
        facet.setLabel(trimString(source.getLabel()));
        facet.setNotExtendable((source.isNotExtendable() == null) ? false : source
                .isNotExtendable());

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

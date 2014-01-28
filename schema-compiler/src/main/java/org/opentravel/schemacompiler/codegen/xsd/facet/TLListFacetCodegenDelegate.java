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

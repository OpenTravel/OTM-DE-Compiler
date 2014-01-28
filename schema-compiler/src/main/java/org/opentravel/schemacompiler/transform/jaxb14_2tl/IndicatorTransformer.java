package org.opentravel.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Indicator;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Indicator</code> type to the
 * <code>TLIndicator</code> type.
 * 
 * @author S. Livezey
 */
public class IndicatorTransformer extends
        BaseTransformer<Indicator, TLIndicator, DefaultTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLIndicator transform(Indicator source) {
        ObjectTransformer<Equivalent, TLEquivalent, DefaultTransformerContext> equivTransformer = getTransformerFactory()
                .getTransformer(Equivalent.class, TLEquivalent.class);
        TLIndicator indicator = new TLIndicator();

        indicator.setName(trimString(source.getName()));
        indicator.setPublishAsElement((source.isPublishAsElement() != null)
                && source.isPublishAsElement());

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation, TLDocumentation, DefaultTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(Documentation.class, TLDocumentation.class);

            indicator.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
        for (Equivalent sourceEquiv : source.getEquivalent()) {
            indicator.addEquivalent(equivTransformer.transform(sourceEquiv));
        }
        return indicator;
    }

}

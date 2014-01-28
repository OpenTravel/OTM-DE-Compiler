package org.opentravel.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.Attribute;
import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Example;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLAttribute</code> type to the
 * <code>Attribute</code> type.
 * 
 * @author S. Livezey
 */
public class TLAttributeTransformer extends
        BaseTransformer<TLAttribute, Attribute, SymbolResolverTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public Attribute transform(TLAttribute source) {
        ObjectTransformer<TLEquivalent, Equivalent, SymbolResolverTransformerContext> equivTransformer = getTransformerFactory()
                .getTransformer(TLEquivalent.class, Equivalent.class);
        ObjectTransformer<TLExample, Example, SymbolResolverTransformerContext> exTransformer = getTransformerFactory()
                .getTransformer(TLExample.class, Example.class);
        TLAttributeType attributeType = source.getType();
        Attribute attribute = new Attribute();

        attribute.setName(trimString(source.getName(), false));
        attribute.setMandatory(source.isMandatory() ? Boolean.TRUE : null);

        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation, Documentation, SymbolResolverTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(TLDocumentation.class, Documentation.class);

            attribute.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }

        for (TLEquivalent sourceEquiv : source.getEquivalents()) {
            attribute.getEquivalent().add(equivTransformer.transform(sourceEquiv));
        }

        for (TLExample sourceEx : source.getExamples()) {
            attribute.getExample().add(exTransformer.transform(sourceEx));
        }

        if (source.getType() != null) {
            attribute.setType(context.getSymbolResolver().buildEntityName(
                    attributeType.getNamespace(), attributeType.getLocalName()));
        } else {
            attribute.setType(trimString(source.getTypeName(), false));
        }
        return attribute;
    }

}

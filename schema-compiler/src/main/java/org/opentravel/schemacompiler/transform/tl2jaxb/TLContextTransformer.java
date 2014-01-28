package org.opentravel.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.ContextDeclaration;
import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLContext</code> type to the
 * <code>ContextDeclaration</code> type.
 * 
 * @author S. Livezey
 */
public class TLContextTransformer extends
        BaseTransformer<TLContext, ContextDeclaration, SymbolResolverTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public ContextDeclaration transform(TLContext source) {
        ContextDeclaration context = new ContextDeclaration();

        context.setContext(source.getContextId());
        context.setApplicationContext(source.getApplicationContext());

        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation, Documentation, SymbolResolverTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(TLDocumentation.class, Documentation.class);

            context.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
        return context;
    }

}

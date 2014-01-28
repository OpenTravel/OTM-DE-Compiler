package org.opentravel.schemacompiler.transform.symbols;

import org.opentravel.schemacompiler.transform.ObjectTransformerContext;
import org.opentravel.schemacompiler.transform.TransformerFactory;

/**
 * Default implementation of the <code> implements ObjectTransformerContext</code>.
 * 
 * @author S. Livezey
 */
public class DefaultTransformerContext implements ObjectTransformerContext {

    private TransformerFactory<?> factory;

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformerContext#getTransformerFactory()
     */
    @Override
    public TransformerFactory<?> getTransformerFactory() {
        return factory;
    }

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformerContext#setTransformerFactory(org.opentravel.schemacompiler.transform.TransformerFactory)
     */
    @Override
    public void setTransformerFactory(TransformerFactory<?> factory) {
        this.factory = factory;
    }

}

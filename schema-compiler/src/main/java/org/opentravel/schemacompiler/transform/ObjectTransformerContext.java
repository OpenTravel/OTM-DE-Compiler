package org.opentravel.schemacompiler.transform;

/**
 * Base interface for contexts that provide required information for the transformation process
 * beyond the immediate scope of the object being converted.
 * 
 * @author S. Livezey
 */
public interface ObjectTransformerContext {

    /**
     * Returns the transformer factory associated with the context.
     * 
     * @return TransformerFactory<?>
     */
    public TransformerFactory<?> getTransformerFactory();

    /**
     * Assigns the factory instance to be associated with this context.
     * 
     * @param factory
     *            the factory instance to associated with the context
     */
    public void setTransformerFactory(TransformerFactory<?> factory);

}

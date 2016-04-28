/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import org.opentravel.schemacompiler.codegen.html.Content;

/**
 * @author Eric.Bronson
 *
 */
public interface FieldOwnerWriter {

    /**
     * Get the member summary tree.
     *
     * @param memberTree the content tree used to build the summary tree
     */
    public void addPropertyInfo(Content memberTree);
    
    /**
     * Get the member summary tree.
     *
     * @param memberTree the content tree used to build the summary tree
     */
    public void addAttributeInfo(Content memberTree);
    
    /**
     * Get the member summary tree.
     *
     * @param memberTree the content tree used to build the summary tree
     */
    public void addIndicatorInfo(Content memberTree);
    
    /**
     * Get the member summary tree.
     *
     * @param memberTree the content tree used to build the summary tree
     */
    public void addExampleInfo(Content memberTree);

}

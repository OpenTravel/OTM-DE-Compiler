/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import org.opentravel.schemacompiler.codegen.html.Content;

/**
 * @author Eric.Bronson
 *
 */
public interface FacetOwnerWriter {
	
    /**
     * Get the member summary tree.
     *
     * @param memberTree the content tree used to build the summary tree
     * @return a content tree for the member summary
     */
    public void addFacetInfo(Content objectTree);

}

/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import org.opentravel.schemacompiler.codegen.html.Content;

/**
 * @author Eric.Bronson
 *
 */
public interface AliasOwnerWriter {

	   /**
     * Get the member summary tree.
     *
     * @param memberTree the content tree used to build the summary tree
     * @return a content tree for the member summary
     */
    public void addAliasInfo(Content aliasTree);

//    /**
//     * Get the member details tree.
//     *
//     * @param memberTree the content tree used to build the details tree
//     * @return a content tree for the member details
//     */
//    public Content getAliasDetailsTree(Content memberTree);

}

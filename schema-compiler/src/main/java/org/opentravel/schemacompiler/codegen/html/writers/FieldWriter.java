package org.opentravel.schemacompiler.codegen.html.writers;

import org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.FieldDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Content;

/**
 * The interface for writing field output.
 *
 * This code is not part of an API.
 * It is implementation that is subject to change.
 * Do not use it as an API
 *
 * @author Jamie Ho
 * @author Bhavesh Patel (Modified)
 * @since 1.5
 */

public interface FieldWriter {

    /**
     * Get the field details tree header.
     *
     * @param classDoc the class being documented
     * @param memberDetailsTree the content tree representing member details
     * @return content tree for the field details header
     */
    public Content getFieldDetailsTreeHeader(AbstractDocumentationBuilder<?> classDoc,
            Content memberDetailsTree);

    /**
     * Get the field documentation tree header.
     *
     * @param field the constructor being documented
     * @param fieldDetailsTree the content tree representing field details
     * @return content tree for the field documentation header
     */
    public Content getFieldTreeHeader(FieldDocumentationBuilder<?> field,
            Content fieldDetailsTree);


    /**
     * Add the comments for the given field.
     *
     * @param field the field being documented
     * @param fieldDocTree the content tree to which the comments will be added
     */
    public void addComments(FieldDocumentationBuilder<?> field, Content fieldDocTree);

    /**
     * Get the field details tree.
     *
     * @param memberDetailsTree the content tree representing member details
     * @return content tree for the field details
     */
    public Content getFieldDetails(Content memberDetailsTree);

    /**
     * Get the field documentation.
     *
     * @param fieldDocTree the content tree representing field documentation
     * @param isLastContent true if the content to be added is the last content
     * @return content tree for the field documentation
     */
    public Content getFieldDoc(Content fieldDocTree, boolean isLastContent);

}

/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

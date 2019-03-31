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

import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;

/**
 * The interface for writing library member output.
 * 
 * @author eric.bronson
 */

public interface LibraryMemberWriter {

    /**
     * Get the header of the page.
     *
     * @return header content that needs to be added to the documentation
     */
    public Content getHeader();

    /**
     * Get the class content header.
     *
     * @return class content header that needs to be added to the documentation
     */
    public Content getContentHeader();

    /**
     * Add the class tree documentation.
     *
     * @param classContentTree class content tree to which the documentation will be added
     */
    public void addMemberInheritanceTree(Content classContentTree);

    /**
     * Get the class information tree header.
     *
     * @return class informaion tree header that needs to be added to the documentation
     */
    public Content getMemberInfoItemTree();

    /**
     * Get the class information.
     *
     * @param classInfoTree content tree conatining the class information
     * @return a content tree for the class
     */
    public Content getMemberInfoTree(Content classInfoTree);

    /**
     * Build the class description.
     *
     * @param classInfoTree content tree to which the documentation will be added
     */
    void addDocumentationInfo(Content classInfoTree);

    /**
     * Get the member tree header for the class.
     *
     * @return a content tree for the member tree header
     */
    public Content getMemberTreeHeader();

    /**
     * Add the FOOTER of the page.
     *
     * @param contentTree content tree to which the FOOTER will be added
     */
    public void addFooter(Content contentTree);

    /**
     * Print the document.
     *
     * @param contentTree content tree that will be printed as a document
     */
    public void printDocument(Content contentTree);

    /**
     * Return the library member being documented.
     *
     * @return the member being documented.
     */
    public DocumentationBuilder getMember();

}

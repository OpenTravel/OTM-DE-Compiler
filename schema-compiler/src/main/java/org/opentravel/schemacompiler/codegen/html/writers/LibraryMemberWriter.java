package org.opentravel.schemacompiler.codegen.html.writers;


import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Content;

/**
 * The interface for writing library member output.
 *
 * This code is not part of an API.
 * It is implementation that is subject to change.
 * Do not use it as an API
 *
 * @author eric.bronson
 */

public interface LibraryMemberWriter {

    /**
     * Get the header of the page.
     *
     * @param header the header string to write
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
     * Add the footer of the page.
     *
     * @param contentTree content tree to which the footer will be added
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

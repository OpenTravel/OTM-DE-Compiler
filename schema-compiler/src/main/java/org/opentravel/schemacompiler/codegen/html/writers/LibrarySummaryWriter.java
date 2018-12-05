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

import java.io.IOException;
import org.opentravel.schemacompiler.codegen.html.Content;



/**
 * The interface for writing library summary output.
 */

public interface LibrarySummaryWriter {

    /**
     * Return the name of the output file.
     *
     * @return the name of the output file.
     */
    public String getOutputFileName();

    /**
     * Get the header for the summary.
     *
     * @param heading Package name.
     * @return the header to be added to the content tree
     */
    public Content getHeader();

    /**
     * Get the header for the package content.
     *
     * @return a content tree for the package content header
     */
    public Content getContentHeader();

    /**
     * Get the header for the package summary.
     *
     * @return a content tree with the package summary header
     */
    public Content getSummaryHeader();

    /**
     * Adds the table of classes to the documentation tree.
     *
     * @param classes the array of classes to document.
     * @param label the label for this table.
     * @param tableSummary the summary string for the table
     * @param tableHeader array of table headers
     * @param summaryContentTree the content tree to which the summaries will be added
     */
    public void addObjectsSummary(Content summaryContentTree);

    /**
     * Adds the package DESCRIPTION from the "packages.html" file to the documentation
     * tree.
     *
     * @param packageContentTree the content tree to which the package DESCRIPTION
     *                           will be added
     */
    public void addNamespaceDescription(Content packageContentTree);


    /**
     * Adds the FOOTER to the documentation tree.
     *
     * @param contentTree the tree to which the FOOTER will be added
     */
    public void addFooter(Content contentTree);

    /**
     * Print the package summary document.
     *
     * @param contentTree the content tree that will be printed
     */
    public void printDocument(Content contentTree);

    /**
     * Close the writer.
     */
    public void close() throws IOException;

}

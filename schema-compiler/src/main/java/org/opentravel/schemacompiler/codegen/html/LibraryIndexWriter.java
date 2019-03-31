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

package org.opentravel.schemacompiler.codegen.html;

import org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.io.IOException;
import java.util.List;


/**
 * Generate the package index page "overview-summary.html" for the right-hand frame. A click on the package name on this
 * page will update the same frame with the "library-summary.html" file for the clicked package.
 *
 * @author Atul M Dambalkar
 * @author Bhavesh Patel (Modified)
 */
public class LibraryIndexWriter extends AbstractNamespaceIndexWriter {

    /**
     * The default file name for this writer.
     */
    public static final String DEFAULT_FILENAME = "overview-summary.html";

    /**
     * Construct the PackageIndexWriter. Also constructs the grouping information as provided on the command line by
     * "-group" option. Stores the order of groups specified by the user.
     * 
     * @param configuration the current configuration of the doclet.
     * @param filename the filename to which output will be written
     * @throws IOException thrown if an error occurs during writer initialization
     */
    public LibraryIndexWriter(Configuration configuration, String filename) throws IOException {
        super( configuration, filename );
    }

    /**
     * Generate the package index page for the right-hand frame.
     *
     * @param configuration the current configuration of the doclet.
     */
    public static void generate(Configuration configuration) {
        try (LibraryIndexWriter packgen = new LibraryIndexWriter( configuration, DEFAULT_FILENAME );) {
            packgen.buildLibraryIndexFile( "doclet.Window_Overview_Summary", true );

        } catch (IOException exc) {
            configuration.message.error( "doclet.exception_encountered", exc.toString(), DEFAULT_FILENAME );
            throw new DocletAbortException();
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void addLibraryList(List<TLLibrary> libraries, String text, String tableSummary, Content body) {
        Content table = HtmlTree.table( HtmlStyle.OVERVIEW_SUMMARY, 0, 3, 0, tableSummary, getTableCaption( text ) );
        table.addContent( getSummaryTableHeader( libraryTableHeader, "col" ) );
        Content tbody = new HtmlTree( HtmlTag.TBODY );
        addLibrariesList( libraries, tbody );
        table.addContent( tbody );
        Content div = HtmlTree.div( HtmlStyle.CONTENT_CONTAINER, table );
        body.addContent( div );
    }

    /**
     * Adds list of packages in the index table. Generate link to each package.
     *
     * @param libraries libraries to which link is to be generated
     * @param tbody the documentation tree to which the list will be added
     */
    protected void addLibrariesList(List<TLLibrary> libraries, Content tbody) {
        for (TLLibrary lib : libraries) {
            if (lib != null) {
                String name = AbstractDocumentationBuilder.getLibraryName( lib );
                Content packageLinkContent = getLibraryLink( name, getNamespaceName( name ) );
                Content tdPackage = HtmlTree.td( HtmlStyle.COL_FIRST, packageLinkContent );
                HtmlTree tdSummary = new HtmlTree( HtmlTag.TD );
                HtmlTree tr = HtmlTree.tr( tdPackage );

                tdSummary.setStyle( HtmlStyle.COL_LAST );
                tdSummary.addContent( getSpace() );
                tr.addContent( tdSummary );

                if (libraries.indexOf( lib ) % 2 == 0) {
                    tr.setStyle( HtmlStyle.ALT_COLOR );
                } else {
                    tr.setStyle( HtmlStyle.ROW_COLOR );
                }
                tbody.addContent( tr );
            }
        }
    }

    /**
     * Adds the overview summary comment for this documentation. Add one line summary at the top of the page and
     * generate a link to the description, which is added at the end of this page.
     *
     * @param body the documentation tree to which the overview header will be added
     */
    protected void addOverviewHeader(Content body) {
        // No action required
    }

    /**
     * Adds the overview comment as provided in the file specified by the "-overview" option on the command line.
     *
     * @param htmltree the documentation tree to which the overview comment will be added
     */
    protected void addOverviewComment(Content htmltree) {
        htmltree.addContent( getMarkerAnchor( "overview_description" ) );
        HtmlTree div = new HtmlTree( HtmlTag.DIV );
        div.setStyle( HtmlStyle.SUB_TITLE );
        htmltree.addContent( div );
    }

    /**
     * Adds the tag information as provided in the file specified by the "-overview" option on the command line.
     *
     * @param body the documentation tree to which the overview will be added
     */
    @Override
    protected void addOverview(Content body) throws IOException {
        HtmlTree div = new HtmlTree( HtmlTag.DIV );
        div.setStyle( HtmlStyle.FOOTER );
        addOverviewComment( div );
        body.addContent( div );
    }

    /**
     * Adds the top text (from the -top option), the upper navigation bar, and then the title (from the"-title" option),
     * at the top of page.
     *
     * @param body the documentation tree to which the navigation bar header will be added
     */
    protected void addNavigationBarHeader(Content body) {
        addNavLinks( true, body );
        addConfigurationTitle( body );
    }

    /**
     * Adds the lower navigation bar and the bottom text (from the -bottom option) at the bottom of page.
     *
     * @param body the documentation tree to which the navigation bar FOOTER will be added
     */
    protected void addNavigationBarFooter(Content body) {
        addNavLinks( false, body );
    }
}

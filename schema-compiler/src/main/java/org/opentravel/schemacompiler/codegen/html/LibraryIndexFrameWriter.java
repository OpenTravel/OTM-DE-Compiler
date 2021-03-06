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
import org.opentravel.schemacompiler.codegen.html.markup.HtmlAttr;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlConstants;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.io.IOException;
import java.util.List;


/**
 * Generate the library index for the left-hand frame in the generated output. A click on the library name in this frame
 * will update the page in the bottom left hand frame with the listing of contents of the clicked package.
 *
 * @author Atul M Dambalkar
 * @author eric.bronson(modified for otm)
 */
public class LibraryIndexFrameWriter extends AbstractNamespaceIndexWriter {

    private static final String LIBRARY_FRAME_HTML = "library-frame.html";
    private static final String LIBRARY_FRAME = "libraryFrame";

    /**
     * The default file name for this writer.
     */
    public static final String DEFAULT_FILENAME = "overview-frame.html";

    /**
     * Construct the PackageIndexFrameWriter object.
     *
     * @param configuration the configuration used for output generation
     * @param filename Name of the package index file to be generated.
     * @throws IOException thrown if an error occurs during writer initialization
     */
    public LibraryIndexFrameWriter(Configuration configuration, String filename) throws IOException {
        super( configuration, filename );
    }

    /**
     * Generate the package index file named "overview-frame.html".
     * 
     * @param configuration the configuration used for output generation
     * @throws DocletAbortException thrown if an unrecoverable error occurs
     */
    public static void generate(Configuration configuration) {
        try (LibraryIndexFrameWriter packgen = new LibraryIndexFrameWriter( configuration, DEFAULT_FILENAME );) {
            packgen.buildLibraryIndexFile( "doclet.Window_Overview", false );

        } catch (IOException exc) {
            configuration.message.error( "doclet.exception_encountered", exc.toString(), DEFAULT_FILENAME );
            throw new DocletAbortException();
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void addLibraryList(List<TLLibrary> libraries, String text, String tableSummary, Content body) {
        Content heading = HtmlTree.heading( HtmlConstants.PACKAGE_HEADING, true, librariesLabel );
        Content div = HtmlTree.div( HtmlStyle.INDEX_CONTAINER, heading );
        HtmlTree ul = new HtmlTree( HtmlTag.UL );
        ul.addAttr( HtmlAttr.TITLE, librariesLabel.toString() );
        for (TLLibrary lib : libraries) {
            // Do not list the package if -nodeprecated option is set and the
            // package is marked as deprecated.
            if (lib != null) {
                ul.addContent( getLibraryName( AbstractDocumentationBuilder.getLibraryName( lib ) ) );
            }
        }
        div.addContent( ul );
        body.addContent( div );
    }

    /**
     * Gets each package name as a separate link.
     *
     * @param ns namespace
     * @return content for the package link
     */
    protected Content getLibraryName(String ns) {
        Content packageLinkContent;
        Content packageLabel;
        if (ns.length() > 0) {
            packageLabel = getNamespaceLabel( ns );
            packageLinkContent =
                getHyperLink( pathString( ns, LIBRARY_FRAME_HTML ), "", packageLabel, "", LIBRARY_FRAME );
        } else {
            packageLabel = new RawHtml( "&lt;unnamed package&gt;" );
            packageLinkContent = getHyperLink( LIBRARY_FRAME_HTML, "", packageLabel, "", LIBRARY_FRAME );
        }
        return HtmlTree.li( packageLinkContent );
    }

    /**
     * {@inheritDoc}
     */
    protected void addNavigationBarHeader(Content body) {
        // No action required
    }

    /**
     * Do nothing as there is no overview information in this page.
     */
    protected void addOverviewHeader(Content body) {
        // No action required
    }

    /**
     * Adds "All Classes" link for the top of the left-hand frame page to the documentation tree.
     *
     * @param body the Content object to which the all classes link should be added
     */
    @Override
    protected void addAllObjectsLink(Content body) {
        Content linkContent = getHyperLink( "allmembers-frame.html", "", allMembersLabel, "", LIBRARY_FRAME );
        Content div = HtmlTree.div( HtmlStyle.INDEX_HEADER, linkContent );
        body.addContent( div );
    }

    /**
     * {@inheritDoc}
     */
    protected void addNavigationBarFooter(Content body) {
        Content p = HtmlTree.paragraph( getSpace() );
        body.addContent( p );
    }
}

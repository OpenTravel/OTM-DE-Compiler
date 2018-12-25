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
/*
 * Copyright (c) 1998, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.opentravel.schemacompiler.codegen.html;

import java.io.IOException;
import java.util.List;

import org.opentravel.schemacompiler.codegen.html.markup.HtmlConstants;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlWriter;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.model.TLLibrary;


/**
 * Abstract class to generate the overview files in
 * Frame and Non-Frame format. This will be sub-classed by to
 * generate overview-frame.html as well as overview-summary.html.
 *
 */
public abstract class AbstractNamespaceIndexWriter extends HtmlWriter {

    /**
     * Array of Namespaces to be documented.
     */
    protected List<TLLibrary> libraries;

    /**
     * Constructor. Also initialises the namespaces variable.
     *
     * @param filename Name of the package index file to be generated.
     */
    public AbstractNamespaceIndexWriter(Configuration configuration,
                                      String filename) throws IOException {
        super(configuration, filename);
        this.setRelativepathNoSlash(".");
        libraries = configuration.getLibraries();
    }

    /**
     * Adds the navigation bar header to the documentation tree.
     *
     * @param body the document tree to which the navigation bar header will be added
     */
    protected abstract void addNavigationBarHeader(Content body);

    /**
     * Adds the navigation bar FOOTER to the documentation tree.
     *
     * @param body the document tree to which the navigation BAR FOOTER will be added
     */
    protected abstract void addNavigationBarFooter(Content body);

    /**
     * Adds the overview header to the documentation tree.
     *
     * @param body the document tree to which the overview header will be added
     */
    protected abstract void addOverviewHeader(Content body);

    /**
     * Adds the packages list to the documentation tree.
     *
     * @param namespaces an array of packagedoc objects
     * @param text caption for the table
     * @param tableSummary summary for the table
     * @param body the document tree to which the packages list will be added
     */
    protected abstract void addLibraryList(List<TLLibrary> libraries, String text,
            String tableSummary, Content body);

    /**
     * Generate and prints the contents in the package index file. Call appropriate
     * methods from the sub-class in order to generate Frame or Non
     * Frame format.
     *
     * @param title the title of the window.
     * @param includeScript boolean set true if windowtitle script is to be included
     */
    protected void buildLibraryIndexFile(String title, boolean includeScript) throws IOException {
        String windowOverview = configuration.getText(title);
        Content body = getBody(includeScript, getWindowTitle(windowOverview));
        addNavigationBarHeader(body);
        addOverviewHeader(body);
        addIndex(body);
        addOverview(body);
        addNavigationBarFooter(body);
        printHtmlDocument(null, includeScript, body);
    }

    /**
     * Default to no overview, override to add overview.
     *
     * @param body the document tree to which the overview will be added
     */
    protected void addOverview(Content body) throws IOException {
    }

    /**
     * Adds the frame or non-frame package index to the documentation tree.
     *
     * @param body the document tree to which the index will be added
     */
    protected void addIndex(Content body) {
        addIndexContents(libraries, configuration.getText("doclet.Libraries"),
                configuration.getText("doclet.Member_Table_Summary",
                configuration.getText("doclet.Library_Summary"),
                configuration.getText("doclet.libraries")), body);
    }

    /**
     * Adds package index contents. Call appropriate methods from
     * the sub-classes. Adds it to the body HtmlTree
     *
     * @param namespaces array of packages to be documented
     * @param text string which will be used as the heading
     * @param tableSummary summary for the table
     * @param body the document tree to which the index contents will be added
     */
    protected void addIndexContents(List<TLLibrary> libraries, String text,
            String tableSummary, Content body) {
        if (!libraries.isEmpty()) {
            addAllObjectsLink(body);
            addLibraryList(libraries, text, tableSummary, body);
        }
    }

    /**
     * Adds the doctitle to the documentation tree, if it is specified on the command line.
     *
     * @param body the document tree to which the title will be added
     */
    protected void addConfigurationTitle(Content body) {
        if (configuration.getDoctitle().length() > 0) {
            Content title = new RawHtml(configuration.getDoctitle());
            Content heading = HtmlTree.heading(HtmlConstants.TITLE_HEADING,
                    HtmlStyle.TITLE, title);
            Content div = HtmlTree.div(HtmlStyle.HEADER, heading);
            body.addContent(div);
        }
    }

    /**
     * Returns highlighted "Overview", in the navigation bar as this is the
     * overview page.
     *
     * @return a Content object to be added to the documentation tree
     */
    @Override
    protected Content getNavLinkContents() {
        return HtmlTree.li(HtmlStyle.NAV_BAR_CELL1_REV, overviewLabel);
    }

    /**
     * Do nothing. This will be overridden in PackageIndexFrameWriter.
     *
     * @param body the document tree to which the all classes link will be added
     */
    protected void addAllObjectsLink(Content body) {
    }
}

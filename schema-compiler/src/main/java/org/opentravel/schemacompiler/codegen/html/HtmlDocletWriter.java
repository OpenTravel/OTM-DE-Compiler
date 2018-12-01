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
 * Copyright (c) 1998, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLLibrary;

import org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.FacetDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.FieldDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.LinkFactoryImpl;
import org.opentravel.schemacompiler.codegen.html.LinkInfoImpl;
import org.opentravel.schemacompiler.codegen.html.LinkOutputImpl;
import org.opentravel.schemacompiler.codegen.html.markup.Comment;
import org.opentravel.schemacompiler.codegen.html.markup.DocType;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlAttr;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlConstants;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlDocWriter;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlDocument;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.markup.StringContent;
import org.opentravel.schemacompiler.codegen.html.DirectoryManager;
import org.opentravel.schemacompiler.codegen.html.DocletConstants;
import org.opentravel.schemacompiler.codegen.html.Util;

/**
 * Class for the Html Format Code Generation specific to JavaDoc. This Class
 * contains methods related to the Html Code Generation which are used
 * extensively while generating the entire documentation.
 *
 * @since 1.2
 * @author Atul M Dambalkar
 * @author Robert Field
 * @author Bhavesh Patel (Modified)
 * @author Eric Bronson (Modified for otm)
 */
public class HtmlDocletWriter extends HtmlDocWriter {

	/**
	 * Relative path from the file getting generated to the destination
	 * directory. For example, if the file getting generated is
	 * "java/lang/Object.html", then the relative path string is "../../". This
	 * string can be empty if the file getting generated is in the destination
	 * directory.
	 */
	public String relativePath = "";

	/**
	 * Same as relativepath, but normalized to never be empty or end with a
	 * slash.
	 */
	public String relativepathNoSlash = "";

	/**
	 * Platform-dependent directory path from the current or the destination
	 * directory to the file getting generated. Used when creating the file. For
	 * example, if the file getting generated is "java/lang/Object.html", then
	 * the path string is "java/lang".
	 */
	public String path = "";

	/**
	 * Name of the file getting generated. If the file getting generated is
	 * "java/lang/Object.html", then the filename is "Object.html".
	 */
	public String filename = "";

	/**
	 * The display length used for indentation while generating the class page.
	 */
	public int displayLength = 0;

	/**
	 * The global configuration information for this run.
	 */
	public Configuration configuration;

	/**
	 * Constructor to construct the HtmlStandardWriter object.
	 *
	 * @param filename
	 *            File to be generated.
	 */
	public HtmlDocletWriter(Configuration configuration, String filename)
			throws IOException {
		super(configuration, filename);
		this.configuration = configuration;
		this.filename = filename;
	}

	/**
	 * Constructor to construct the HtmlStandardWriter object.
	 *
	 * @param path
	 *            Platform-dependent {@link #path} used when creating file.
	 * @param filename
	 *            Name of file to be generated.
	 * @param relativePath
	 *            Value for the variable {@link #relativePath}.
	 */
	public HtmlDocletWriter(Configuration configuration, String path,
			String filename, String relativePath) throws IOException {
		super(configuration, path, filename);
		this.configuration = configuration;
		this.path = path;
		this.relativePath = relativePath;
		this.relativepathNoSlash = DirectoryManager
				.getPathNoTrailingSlash(this.relativePath);
		this.filename = filename;
	}


	/**
	 * Print Html Hyper Link, with target frame. This link will only appear if
	 * page is not in a frame.
	 *
	 * @param link
	 *            String name of the file.
	 * @param where
	 *            Position in the file
	 * @param target
	 *            Name of the target frame.
	 * @param label
	 *            Tag for the link.
	 * @param strong
	 *            Whether the label should be strong or not?
	 */
	public void printNoFramesTargetHyperLink(String link, String where,
			String target, String label, boolean strong) {
		script();
		println("  <!--");
		println("  if(window==top) {");
		println("    document.writeln('"
				+ getHyperLinkString(link, where, label, strong, "", "", target)
				+ "');");
		println("  }");
		println("  //-->");
		scriptEnd();
		noScript();
		println("  "
				+ getHyperLinkString(link, where, label, strong, "", "", target));
		noScriptEnd();
		println(DocletConstants.NL);
	}

	/**
	 * Get the script to show or hide the All classes link.
	 *
	 * @param id
	 *            id of the element to show or hide
	 * @return a content tree for the script
	 */
	public Content getAllClassesLinkScript(String id) {
		HtmlTree script = new HtmlTree(HtmlTag.SCRIPT);
		script.addAttr(HtmlAttr.TYPE, "text/javascript");
		String scriptCode = "<!--" + DocletConstants.NL
				+ "  allClassesLink = document.getElementById(\"" + id + "\");"
				+ DocletConstants.NL + "  if(window==top) {"
				+ DocletConstants.NL
				+ "    allClassesLink.style.display = \"block\";"
				+ DocletConstants.NL + "  }" + DocletConstants.NL + "  else {"
				+ DocletConstants.NL
				+ "    allClassesLink.style.display = \"none\";"
				+ DocletConstants.NL + "  }" + DocletConstants.NL + "  //-->"
				+ DocletConstants.NL;
		Content scriptContent = new RawHtml(scriptCode);
		script.addContent(scriptContent);
		Content div = HtmlTree.div(script);
		return div;
	}

	/**
	 * Get the script to show or hide the All classes link.
	 *
	 * @param id
	 *            id of the element to show or hide
	 * @return a content tree for the script
	 */
	public Content getJQueryScript() {
		HtmlTree script = new HtmlTree(HtmlTag.SCRIPT);
		script.addAttr(HtmlAttr.TYPE, "text/javascript");
		script.addAttr(HtmlAttr.SRC,
				"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js");
		return script;
	}

	/**
	 * Get the script to show or hide the All classes link.
	 *
	 * @param id
	 *            id of the element to show or hide
	 * @return a content tree for the script
	 */
	public Content getToggleScript() {
		HtmlTree script = new HtmlTree(HtmlTag.SCRIPT);
		script.addAttr(HtmlAttr.TYPE, "text/javascript");
		String s = // "<!--" + DocletConstants.NL
		"$(document).ready(function(){" + DocletConstants.NL
				+ "$('[data-toggle=\"collapsed\"]').click(function(){"
				+ DocletConstants.NL + "var target = $(this).data('target');"
				+ DocletConstants.NL + "$(target).toggleClass('collapsed');"
				+ DocletConstants.NL
				+ "var imgTarget = $(this).data('imgtarget');"
				+ DocletConstants.NL + "var img = $(imgTarget);"
				+ DocletConstants.NL + "img.addClass('toggleButton');"
				+ DocletConstants.NL + "$(this).removeClass('toggleButton');"
				+ DocletConstants.NL + "});" + DocletConstants.NL + "});";
		// + DocletConstants.NL + "  //-->";
		script.addContent(new RawHtml(s));
		return script;
	}
	
	protected void printTagsInfoHeader() {
		dl();
	}

	protected void printTagsInfoFooter() {
		dlEnd();
	}

	/**
	 * Get Package link, with target frame.
	 *
	 * @param pd
	 *            The link will be to the "package-summary.html" page for this
	 *            package
	 * @param target
	 *            name of the target frame
	 * @param label
	 *            tag for the link
	 * @return a content for the target package link
	 */
	public Content getTargetLibraryLink(String namespace, String target,
			Content label) {
		return getHyperLink(pathString(namespace, "library-summary.html"), "",
				label, "", target);
	}

	/**
	 * Generates the HTML document tree and prints it out.
	 *
	 * @param metakeywords
	 *            Array of String keywords for META tag. Each element of the
	 *            array is assigned to a separate META tag. Pass in null for no
	 *            array
	 * @param includeScript
	 *            true if printing windowtitle script false for files that
	 *            appear in the left-hand frames
	 * @param body
	 *            the body htmltree to be included in the document
	 */
	public void printHtmlDocument(String[] metakeywords, boolean includeScript,
			Content body) {
		Content htmlDocType = DocType.Html5();
		Content htmlComment = new Comment(
				configuration.getText("doclet.New_Page"));
		Content head = new HtmlTree(HtmlTag.HEAD);
		Content headComment = new Comment("Generated by otm (version "
				+ Configuration.VERSION + ") on " + today());
		head.addContent(headComment);
		head.addContent(getTitle());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Content meta = HtmlTree.meta("date", dateFormat.format(new Date()));
		head.addContent(meta);
		head.addContent(getStyleSheetProperties());
		Content htmlTree = HtmlTree.html(configuration.getLocale()
				.getLanguage(), head, body);
		Content script = getJQueryScript();
		htmlTree.addContent(script);
		script = getToggleScript();
		htmlTree.addContent(script);
		Content htmlDocument = new HtmlDocument(htmlDocType, htmlComment,
				htmlTree);
		print(htmlDocument.toString());
	}

	/**
	 * Get the window title.
	 *
	 * @param title
	 *            the title string to construct the complete window title
	 * @return the window title string
	 */
	public String getWindowTitle(String title) {
		if (configuration.windowtitle.length() > 0) {
			title += " (" + configuration.windowtitle + ")";
		}
		return title;
	}


	/**
	 * Adds the navigation bar for the Html page at the top and and the bottom.
	 *
	 * @param header
	 *            If true print navigation bar at the top of the page else
	 * @param body
	 *            the HtmlTree to which the nav links will be added
	 */
	protected void addNavLinks(boolean header, Content body) {
		String allClassesId = "allclasses_";
		HtmlTree navDiv = new HtmlTree(HtmlTag.DIV);
		if (header) {
			body.addContent(HtmlConstants.START_OF_TOP_NAVBAR);
			navDiv.setStyle(HtmlStyle.topNav);
			allClassesId += "navbar_top";
			Content a = getMarkerAnchor("navbar_top");
			navDiv.addContent(a);
			Content skipLinkContent = getHyperLink("", "skip-navbar_top",
					HtmlTree.EMPTY,
					configuration.getText("doclet.Skip_navigation_links"), "");
			navDiv.addContent(skipLinkContent);
		} else {
			body.addContent(HtmlConstants.START_OF_BOTTOM_NAVBAR);
			navDiv.setStyle(HtmlStyle.bottomNav);
			allClassesId += "navbar_bottom";
			Content a = getMarkerAnchor("navbar_bottom");
			navDiv.addContent(a);
			Content skipLinkContent = getHyperLink("", "skip-navbar_bottom",
					HtmlTree.EMPTY,
					configuration.getText("doclet.Skip_navigation_links"), "");
			navDiv.addContent(skipLinkContent);
		}
		if (header) {
			navDiv.addContent(getMarkerAnchor("navbar_top_firstrow"));
		} else {
			navDiv.addContent(getMarkerAnchor("navbar_bottom_firstrow"));
		}
		HtmlTree navList = new HtmlTree(HtmlTag.UL);
		navList.setStyle(HtmlStyle.navList);
		navList.addAttr(HtmlAttr.TITLE,
				configuration.getText("doclet.Navigation"));
		if (configuration.createoverview) {
			navList.addContent(getNavLinkContents());
		}
		List<TLLibrary> libraries = configuration.getLibraries();
		int size = libraries.size();
		if (size == 1) {
			navList.addContent(getNavLinkLibrary(libraries.get(0).getName()));
		} else if (size > 1) {
			navList.addContent(getNavLinkLibrary());
		}
		navList.addContent(getNavLinkObject());
		if (configuration.classuse) {
			navList.addContent(getNavLinkClassUse());
		}
		if (configuration.createtree) {
			navList.addContent(getNavLinkTree());
		}
		if (configuration.createindex) {
			navList.addContent(getNavLinkIndex());
		}
		if (!configuration.nohelp) {
			navList.addContent(getNavLinkHelp());
		}
		navDiv.addContent(navList);
		body.addContent(navDiv);
		Content ulNav = HtmlTree.ul(HtmlStyle.navList, getNavLinkPrevious());
		ulNav.addContent(getNavLinkNext());
		Content subDiv = HtmlTree.div(HtmlStyle.subNav, ulNav);
		Content ulFrames = HtmlTree.ul(HtmlStyle.navList, getNavShowLists());
		ulFrames.addContent(getNavHideLists(filename));
		subDiv.addContent(ulFrames);
		HtmlTree ulAllClasses = HtmlTree.ul(HtmlStyle.navList,
				getNavLinkClassIndex());
		ulAllClasses.addAttr(HtmlAttr.ID, allClassesId.toString());
		subDiv.addContent(ulAllClasses);
		subDiv.addContent(getAllClassesLinkScript(allClassesId.toString()));
		//addSummaryDetailLinks(subDiv);
		if (header) {
			subDiv.addContent(getMarkerAnchor("skip-navbar_top"));
			body.addContent(subDiv);
			body.addContent(HtmlConstants.END_OF_TOP_NAVBAR);
		} else {
			subDiv.addContent(getMarkerAnchor("skip-navbar_bottom"));
			body.addContent(subDiv);
			body.addContent(HtmlConstants.END_OF_BOTTOM_NAVBAR);
		}
	}

	/**
	 * Get the word "NEXT" to indicate that no link is available. Override this
	 * method to customize next link.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkNext() {
		return getNavLinkNext(null);
	}

	/**
	 * Get the word "PREV" to indicate that no link is available. Override this
	 * method to customize prev link.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkPrevious() {
		return getNavLinkPrevious(null);
	}


	/**
	 * Get link to the "overview-summary.html" page.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkContents() {
		Content linkContent = getHyperLink(relativePath
				+ "overview-summary.html", "", overviewLabel, "", "");
		Content li = HtmlTree.li(linkContent);
		return li;
	}

	/**
	 * Get link to the "namespace-summary.html" page for the package passed.
	 *
	 * @param ns
	 *            Namespace to which link will be generated
	 * @return a content tree for the link
	 */
	protected Content getNavLinkLibrary(String ns) {
		Content linkContent = getLibraryLink(ns, libraryLabel);
		Content li = HtmlTree.li(linkContent);
		return li;
	}

	/**
	 * Get the word "Package" , to indicate that link is not available here.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkLibrary() {
		Content li = HtmlTree.li(libraryLabel);
		return li;
	}


	/**
	 * Get the word "Use", to indicate that link is not available.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkClassUse() {
		Content li = HtmlTree.li(useLabel);
		return li;
	}


	/**
	 * Get link for previous file.
	 *
	 * @param prev
	 *            File name for the prev link
	 * @return a content tree for the link
	 */
	public Content getNavLinkPrevious(String prev) {
		Content li;
		if (prev != null) {
			li = HtmlTree.li(getHyperLink(prev, "", prevLabel, "", ""));
		} else
			li = HtmlTree.li(prevLabel);
		return li;
	}

	/**
	 * Get link for next file. If next is null, just print the label without
	 * linking it anywhere.
	 *
	 * @param next
	 *            File name for the next link
	 * @return a content tree for the link
	 */
	public Content getNavLinkNext(String next) {
		Content li;
		if (next != null) {
			li = HtmlTree.li(getHyperLink(next, "", nextLabel, "", ""));
		} else
			li = HtmlTree.li(nextLabel);
		return li;
	}

	/**
	 * Get "FRAMES" link, to switch to the frame version of the output.
	 *
	 * @param link
	 *            File to be linked, "index.html"
	 * @return a content tree for the link
	 */
	protected Content getNavShowLists(String link) {
		Content framesContent = getHyperLink(link + "?" + path + filename, "",
				framesLabel, "", "_top");
		Content li = HtmlTree.li(framesContent);
		return li;
	}


	/**
	 * Get "FRAMES" link, to switch to the frame version of the output.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavShowLists() {
		return getNavShowLists(relativePath + "index.html");
	}


	/**
	 * Get "NO FRAMES" link, to switch to the non-frame version of the output.
	 *
	 * @param link
	 *            File to be linked
	 * @return a content tree for the link
	 */
	protected Content getNavHideLists(String link) {
		Content noFramesContent = getHyperLink(link, "", noframesLabel, "",
				"_top");
		Content li = HtmlTree.li(noFramesContent);
		return li;
	}


	/**
	 * Get "Tree" link in the navigation bar. If there is only one package
	 * specified on the command line, then the "Tree" link will be to the only
	 * "package-tree.html" file otherwise it will be to the "overview-tree.html"
	 * file.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkTree() {
		Content treeLinkContent;
		treeLinkContent = getHyperLink(relativePath + "overview-tree.html", "",
				treeLabel, "", "");
		Content li = HtmlTree.li(treeLinkContent);
		return li;
	}

	/**
	 * Get the overview tree link for the main tree.
	 *
	 * @param label
	 *            the label for the link
	 * @return a content tree for the link
	 */
	protected Content getNavLinkMainTree(String label) {
		Content mainTreeContent = getHyperLink(relativePath
				+ "overview-tree.html", new StringContent(label));
		Content li = HtmlTree.li(mainTreeContent);
		return li;
	}


	/**
	 * Get the word "Class", to indicate that class link is not available.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkObject() {
		Content li = HtmlTree.li(objectLabel);
		return li;
	}

	/**
	 *TODO keep this?
	 * Get link for generated index. If the user has used "-splitindex" command
	 * line option, then link to file "index-files/index-1.html" is generated
	 * otherwise link to file "index-all.html" is generated.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkClassIndex() {
		return HtmlTree.EMPTY;
	}

	/**
	 * TODO keep this?
	 * Get link for generated class index.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkIndex() {
//		Content linkContent = getHyperLink(
//				relativePath
//						+ (configuration.splitindex ? DirectoryManager.getPath("index-files")
//								+ fileseparator
//								: "")
//						+ (configuration.splitindex ? "index-1.html"
//								: "index-all.html"), "", indexLabel, "", "");
//		Content li = HtmlTree.LI(linkContent);
		return HtmlTree.EMPTY;
	}

	/**
	 * Get help file link. If user has provided a help file, then generate a
	 * link to the user given file, which is already copied to current or
	 * destination directory.
	 *
	 * @return a content tree for the link
	 */
	protected Content getNavLinkHelp() {
		String helpfilenm = configuration.helpfile;
		if (helpfilenm.equals("")) {
			helpfilenm = "help-doc.html";
		} else {
			int lastsep;
			if ((lastsep = helpfilenm.lastIndexOf(File.separatorChar)) != -1) {
				helpfilenm = helpfilenm.substring(lastsep + 1);
			}
		}
		Content linkContent = getHyperLink(relativePath + helpfilenm, "",
				helpLabel, "", "");
		Content li = HtmlTree.li(linkContent);
		return li;
	}
	
	/**
	 * Get summary table header.
	 *
	 * @param header
	 *            the header for the table
	 * @param scope
	 *            the scope of the headers
	 * @return a content tree for the header
	 */
	public Content getSummaryTableHeader(String[] header, String scope) {
		Content tr = new HtmlTree(HtmlTag.TR);
		int size = header.length;
		Content tableHeader;
		if (size == 1) {
			tableHeader = new StringContent(header[0]);
			tr.addContent(HtmlTree.th(HtmlStyle.colOne, scope, tableHeader));
			return tr;
		}
		for (int i = 0; i < size; i++) {
			tableHeader = new StringContent(header[i]);
			if (i == 0)
				tr.addContent(HtmlTree.th(HtmlStyle.colFirst, scope,
						tableHeader));
			else if (i == (size - 1))
				tr.addContent(HtmlTree
						.th(HtmlStyle.colLast, scope, tableHeader));
			else
				tr.addContent(HtmlTree.th(scope, tableHeader));
		}
		return tr;
	}

	/**
	 * Get table caption.
	 *
	 * @param rawText
	 *            the caption for the table which could be raw Html
	 * @return a content tree for the caption
	 */
	public Content getTableCaption(String rawText) {
		Content title = new RawHtml(rawText);
		Content captionSpan = HtmlTree.span(title);
		Content space = getSpace();
		Content tabSpan = HtmlTree.span(HtmlStyle.tabEnd, space);
		Content caption = HtmlTree.caption(captionSpan);
		caption.addContent(tabSpan);
		return caption;
	}

	/**
	 * Get the marker anchor which will be added to the documentation tree.
	 *
	 * @param anchorName
	 *            the anchor name attribute
	 * @return a content tree for the marker anchor
	 */
	public Content getMarkerAnchor(String anchorName) {
		return getMarkerAnchor(anchorName, null);
	}

	/**
	 * Get the marker anchor which will be added to the documentation tree.
	 *
	 * @param anchorName
	 *            the anchor name attribute
	 * @param anchorContent
	 *            the content that should be added to the anchor
	 * @return a content tree for the marker anchor
	 */
	public Content getMarkerAnchor(String anchorName, Content anchorContent) {
		if (anchorContent == null)
			anchorContent = new Comment(" ");
		Content markerAnchor = HtmlTree.aName(anchorName, anchorContent);
		return markerAnchor;
	}

	/**
	 * Returns a packagename content.
	 *
	 * @param namespace
	 *            the package to check
	 * @return package name content
	 */
	public Content getNamespaceName(String namespace) {
		return namespace == null || namespace.length() == 0 ? defaultPackageLabel
				: getNamespaceLabel(namespace);
	}

	/**
	 * Returns a package name label.
	 *
	 * @param parsedName
	 *            the package name
	 * @return the package name content
	 */
	public Content getNamespaceLabel(String packageName) {
		return new StringContent(packageName);
	}

	//TODO keep this in we may want to use this later
	// /**
	// * Add package deprecation information to the documentation tree
	// *
	// * @param deprPkgs list of deprecated packages
	// * @param headingKey the caption for the deprecated package table
	// * @param tableSummary the summary for the deprecated package table
	// * @param tableHeader table headers for the deprecated package table
	// * @param contentTree the content tree to which the deprecated package
	// table will be added
	// */
	// protected void addPackageDeprecatedAPI(List<Doc> deprPkgs, String
	// headingKey,
	// String tableSummary, String[] tableHeader, Content contentTree) {
	// if (deprPkgs.size() > 0) {
	// Content table = HtmlTree.TABLE(0, 3, 0, tableSummary,
	// getTableCaption(configuration().getText(headingKey)));
	// table.addContent(getSummaryTableHeader(tableHeader, "col"));
	// Content tbody = new HtmlTree(HtmlTag.TBODY);
	// for (int i = 0; i < deprPkgs.size(); i++) {
	// PackageDoc pkg = (PackageDoc) deprPkgs.get(i);
	// HtmlTree td = HtmlTree.TD(HtmlStyle.colOne,
	// getNamespaceLink(pkg, getNamespaceName(pkg)));
	// if (pkg.tags("deprecated").length > 0) {
	// addInlineDeprecatedComment(pkg, pkg.tags("deprecated")[0], td);
	// }
	// HtmlTree tr = HtmlTree.TR(td);
	// if (i % 2 == 0) {
	// tr.addStyle(HtmlStyle.altColor);
	// } else {
	// tr.addStyle(HtmlStyle.rowColor);
	// }
	// tbody.addContent(tr);
	// }
	// table.addContent(tbody);
	// Content li = HtmlTree.LI(HtmlStyle.blockList, table);
	// Content ul = HtmlTree.UL(HtmlStyle.blockList, li);
	// contentTree.addContent(ul);
	// }
	// }

	/**
	 * Prine table header information about color, column span and the font.
	 *
	 * @param color
	 *            Background color.
	 * @param span
	 *            Column span.
	 */
	public void tableHeaderStart(String color, int span) {
		trBgcolorStyle(color, "TableHeadingColor");
		thAlignColspan("left", span);
		font("+2");
	}

	/**
	 * Print table header for the inherited members summary tables. Print the
	 * background color information.
	 *
	 * @param color
	 *            Background color.
	 */
	public void tableInheritedHeaderStart(String color) {
		trBgcolorStyle(color, "TableSubHeadingColor");
		thAlign("left");
	}

	/**
	 * Print "Use" table header. Print the background color and the column span.
	 *
	 * @param color
	 *            Background color.
	 */
	public void tableUseInfoHeaderStart(String color) {
		trBgcolorStyle(color, "TableSubHeadingColor");
		thAlignColspan("left", 2);
	}

	/**
	 * Print table header with the background color with default column span 2.
	 *
	 * @param color
	 *            Background color.
	 */
	public void tableHeaderStart(String color) {
		tableHeaderStart(color, 2);
	}

	/**
	 * Print table header with the column span, with the default color #CCCCFF.
	 *
	 * @param span
	 *            Column span.
	 */
	public void tableHeaderStart(int span) {
		tableHeaderStart("#CCCCFF", span);
	}

	/**
	 * Print table header with default column span 2 and default color #CCCCFF.
	 */
	public void tableHeaderStart() {
		tableHeaderStart(2);
	}

	/**
	 * Print table header end tags for font, column and row.
	 */
	public void tableHeaderEnd() {
		fontEnd();
		thEnd();
		trEnd();
	}

	/**
	 * Print table header end tags in inherited tables for column and row.
	 */
	public void tableInheritedHeaderEnd() {
		thEnd();
		trEnd();
	}

	/**
	 * Print the summary table row cell attribute width.
	 *
	 * @param width
	 *            Width of the table cell.
	 */
	public void summaryRow(int width) {
		if (width != 0) {
			tdWidth(width + "%");
		} else {
			td();
		}
	}

	/**
	 * Print the summary table row cell end tag.
	 */
	public void summaryRowEnd() {
		tdEnd();
	}

	/**
	 * Print the heading in Html &lt;H2> format.
	 *
	 * @param str
	 *            The Header string.
	 */
	public void printIndexHeading(String str) {
		h2();
		print(str);
		h2End();
	}

	/**
	 * Print Html tag &lt;FRAMESET=arg&gt;.
	 *
	 * @param arg
	 *            Argument for the tag.
	 */
	public void frameSet(String arg) {
		println("<FRAMESET " + arg + ">");
	}

	/**
	 * Print Html closing tag &lt;/FRAMESET&gt;.
	 */
	public void frameSetEnd() {
		println("</FRAMESET>");
	}

	/**
	 * Print Html tag &lt;FRAME=arg&gt;.
	 *
	 * @param arg
	 *            Argument for the tag.
	 */
	public void frame(String arg) {
		println("<FRAME " + arg + ">");
	}

	/**
	 * Print Html closing tag &lt;/FRAME&gt;.
	 */
	public void frameEnd() {
		println("</FRAME>");
	}

	/**
	 * Return path to the class page for a classdoc. For example, the class name
	 * is "java.lang.Object" and if the current file getting generated is
	 * "java/io/File.html", then the path string to the class, returned is
	 * "../../java/lang.Object.html".
	 *
	 * @param cd
	 *            Class to which the path is requested.
	 */
	protected String pathToObject(DocumentationBuilder cd) {
		return pathString(cd.getOwningLibrary(), cd.getName() + ".html");
	}

	/**
	 * Return the path to the class page for a classdoc. Works same as
	 * {@link #pathToObject(ClassDoc)}.
	 *
	 * @param cd
	 *            Class to which the path is requested.
	 * @param name
	 *            Name of the file(doesn't include path).
	 */
	protected String pathString(DocumentationBuilder cd, String name) {
		return pathString(cd.getOwningLibrary(), name);
	}

	/**
	 * Return path to the given file name in the given package. So if the name
	 * passed is "Object.html" and the name of the package is "java.lang", and
	 * if the relative path is "../.." then returned string will be
	 * "../../java/lang/Object.html"
	 *
	 * @param pd
	 *            Package in which the file name is assumed to be.
	 * @param name
	 *            File name, to which path string is.
	 */
	protected String pathString(String pd, String name) {
		StringBuffer buf = new StringBuffer(relativePath);
		buf.append(DirectoryManager.getPathToLibrary(pd, name));
		return buf.toString();
	}

	/**
	 * Print the link to the given package.
	 *
	 * @param pkg
	 *            the package to link to.
	 * @param label
	 *            the label for the link.
	 * @param isStrong
	 *            true if the label should be strong.
	 */
	public void printNamespaceLink(String pkg, String label, boolean isStrong) {
		print(getNamespaceLinkString(pkg, label, isStrong));
	}

	/**
	 * Print the link to the given package.
	 *
	 * @param pkg
	 *            the package to link to.
	 * @param label
	 *            the label for the link.
	 * @param isStrong
	 *            true if the label should be strong.
	 * @param style
	 *            the font of the package link label.
	 */
	public void printNamespaceLink(String pkg, String label, boolean isStrong,
			String style) {
		print(getNamespaceLinkString(pkg, label, isStrong, style));
	}

	/**
	 * Return the link to the given package.
	 *
	 * @param pkg
	 *            the package to link to.
	 * @param label
	 *            the label for the link.
	 * @param isStrong
	 *            true if the label should be strong.
	 * @return the link to the given package.
	 */
	public String getNamespaceLinkString(String pkg, String label,
			boolean isStrong) {
		return getNamespaceLinkString(pkg, label, isStrong, "");
	}

	/**
	 * Return the link to the given package.
	 *
	 * @param pkg
	 *            the package to link to.
	 * @param label
	 *            the label for the link.
	 * @param isStrong
	 *            true if the label should be strong.
	 * @param style
	 *            the font of the package link label.
	 * @return the link to the given package.
	 */
	public String getNamespaceLinkString(String pkg, String label,
			boolean isStrong, String style) {
		String crossPkgLink = getCrossNamespaceLink(pkg);
		if (crossPkgLink != null) {
			return getHyperLinkString(crossPkgLink, "", label, isStrong, style);
		} else {
			return label;
		}
		// }
	}

	/**
	 * Return the link to the given package.
	 *
	 * @param pkg
	 *            the package to link to.
	 * @param label
	 *            the label for the link.
	 * @return a content tree for the package link.
	 */
	public Content getLibraryLink(String namespace, Content label) {
		if (namespace != null) {
			return getHyperLink(pathString(namespace, "library-summary.html"),
					"", label);
		} else {
			String crossPkgLink = getCrossNamespaceLink(namespace);
			if (crossPkgLink != null) {
				return getHyperLink(crossPkgLink, "", label);
			} else {
				return label;
			}
		}
	}

	public String italicsObjectName(LibraryMember member, boolean qual) {
		String name = (qual) ? configuration.getQualifiedName(member) : member
				.getLocalName();
		return name;
	}

	/**
	 * Return the link to the given class.
	 *
	 * @param linkInfo
	 *            the information about the link.
	 *
	 * @return the link for the given class.
	 */
	public String getLink(LinkInfoImpl linkInfo) {
		LinkFactoryImpl factory = new LinkFactoryImpl(this);
		String link = null;
		try {
			link = ((LinkOutputImpl) factory.getLinkOutput(linkInfo))
					.toString();
		} catch (NullPointerException npe) {
			// TODO: This occurs numerous times during document generation
			configuration.printNotice("Missing link for: " + linkInfo.label);
		}
		displayLength += linkInfo.displayLength;
		return link;
	}

	/**
	 * Print the link to the given class.
	 */
	public void printLink(LinkInfoImpl linkInfo) {
		print(getLink(linkInfo));
	}

	/*************************************************************
	 * Return a class cross link to external class documentation. The name must
	 * be fully qualified to determine which package the class is in. The -link
	 * option does not allow users to link to external classes in the "default"
	 * package.
	 *
	 * @param qualifiedClassName
	 *            the qualified name of the external class.
	 * @param refMemName
	 *            the name of the member being referenced. This should be null
	 *            or empty string if no member is being referenced.
	 * @param label
	 *            the label for the external link.
	 * @param strong
	 *            true if the link should be strong.
	 * @param style
	 *            the style of the link.
	 * @param code
	 *            true if the label should be code font.
	 */
	public String getCrossClassLink(String qualifiedClassName,
			String refMemName, String label, boolean strong, String style,
			boolean code) {
		String className = "", packageName = qualifiedClassName == null ? ""
				: qualifiedClassName;
		int periodIndex;
		while ((periodIndex = packageName.lastIndexOf('.')) != -1) {
			className = packageName.substring(periodIndex + 1,
					packageName.length())
					+ (className.length() > 0 ? "." + className : "");
			packageName = packageName.substring(0, periodIndex);
			if (getCrossNamespaceLink(packageName) != null) {
//				String defaultLabel = code ? getCode() + className + getCodeEnd() : className;
//				// The package exists in external documentation, so link to the
//				// external
//				// class (assuming that it exists). This is definitely a
//				// limitation of
//				// the -link option. There are ways to determine if an external
//				// package
//				// exists, but no way to determine if the external class exists.
//				// We just
//				// have to assume that it does.
//				return getHyperLinkString(configuration.extern.getExternalLink(
//						packageName, relativePath, className
//								+ ".html?is-external=true"),
//						refMemName == null ? "" : refMemName, label == null
//								|| label.length() == 0 ? defaultLabel : label,
//						strong, style, configuration.getText(
//								"doclet.Href_Class_Or_Interface_Title",
//								packageName), "");
			}
		}
		return null;
	}

	public boolean isObjectLinkable(DocumentationBuilder builder) {
		return true;// configuration.extern.isExternal(cd);
	}

	public String getCrossNamespaceLink(String pkgName) {
		return null;
//		return configuration.extern.getExternalLink(pkgName, relativePath,
//				"/library-summary.html?is-external=true");
	}

	/**
	 * Get the class link.
	 *
	 * @param context
	 *            the id of the context where the link will be added
	 * @param cd
	 *            the class doc to link to
	 * @return a content tree for the link
	 */
	public Content getQualifiedClassLink(int context,
			AbstractDocumentationBuilder<?> cd) {
		return new RawHtml(getLink(new LinkInfoImpl(context, cd,
				configuration.getQualifiedName(cd), "")));
	}

	/**
	 * Add the class link.
	 *
	 * @param context
	 *            the id of the context where the link will be added
	 * @param cd
	 *            the class doc to link to
	 * @param contentTree
	 *            the content tree to which the link will be added
	 */
	public void addPreQualifiedClassLink(int context,
			AbstractDocumentationBuilder<?> cd, Content contentTree) {
		addPreQualifiedClassLink(context, cd, false, contentTree);
	}

	/**
	 * Retrieve the class link with the package portion of the label in plain
	 * text. If the qualifier is excluded, it willnot be included in the link
	 * label.
	 *
	 * @param cd
	 *            the class to link to.
	 * @param isStrong
	 *            true if the link should be strong.
	 * @return the link with the package portion of the label in plain text.
	 */
	public String getPreQualifiedMemberLink(int context,
			AbstractDocumentationBuilder<?> cd, boolean isStrong) {
		String classlink = "";
		String pd = cd.getNamespace();
		if (pd != null) {// && !configuration.shouldExcludeQualifier(pd)) {
			classlink = getNamespace(cd);
		}
		classlink += getLink(new LinkInfoImpl(context, cd, cd.getName(),
				isStrong));
		return classlink;
	}

	/**
	 * Add the class link with the package portion of the label in plain text.
	 * If the qualifier is excluded, it will not be included in the link label.
	 *
	 * @param context
	 *            the id of the context where the link will be added
	 * @param cd
	 *            the class to link to
	 * @param isStrong
	 *            true if the link should be strong
	 * @param contentTree
	 *            the content tree to which the link with be added
	 */
	public void addPreQualifiedClassLink(int context,
			AbstractDocumentationBuilder<?> cd, boolean isStrong,
			Content contentTree) {
		String pd = cd.getNamespace();
		if (pd != null) {// && !configuration.shouldExcludeQualifier(pd.name()))
							// {
			contentTree.addContent(getNamespace(cd));
		}
		contentTree.addContent(new RawHtml(getLink(new LinkInfoImpl(context,
				cd, cd.getName(), isStrong))));
	}

	/**
	 * Add the class link with the package portion of the label in plain text.
	 * If the qualifier is excluded, it will not be included in the link label.
	 *
	 * @param context
	 *            the id of the context where the link will be added
	 * @param cd
	 *            the class to link to
	 * @param isStrong
	 *            true if the link should be strong
	 * @param contentTree
	 *            the content tree to which the link with be added
	 */
	public void addPreQualifiedClassLink(int context, LibraryMember cd,
			boolean isStrong, Content contentTree) {
		String pd = cd.getNamespace();
		if (pd != null) {
			contentTree.addContent(getNamespace(cd));
		}
		contentTree.addContent(new RawHtml(getLink(new LinkInfoImpl(context,
				cd, cd.getLocalName(), isStrong))));
	}

	/**
	 * Add the class link, with only class name as the strong link and prefixing
	 * plain package name.
	 *
	 * @param context
	 *            the id of the context where the link will be added
	 * @param cd
	 *            the class to link to
	 * @param contentTree
	 *            the content tree to which the link with be added
	 */
	public void addPreQualifiedStrongClassLink(int context,
			AbstractDocumentationBuilder<?> cd, Content contentTree) {
		addPreQualifiedClassLink(context, cd, true, contentTree);
	}

	public void printText(String key) {
		print(configuration.getText(key));
	}

	public void printText(String key, String a1) {
		print(configuration.getText(key, a1));
	}

	public void printText(String key, String a1, String a2) {
		print(configuration.getText(key, a1, a2));
	}

	public void strongText(String key) {
		strong(configuration.getText(key));
	}

	public void strongText(String key, String a1) {
		strong(configuration.getText(key, a1));
	}

	public void strongText(String key, String a1, String a2) {
		strong(configuration.getText(key, a1, a2));
	}

	/**
	 * Get the link for the given member.
	 *
	 * @param context
	 *            the id of the context where the link will be added
	 * @param doc
	 *            the member being linked to
	 * @param label
	 *            the label for the link
	 * @return a content tree for the doc link
	 */
	public Content getDocLink(int context, FieldDocumentationBuilder<?> doc,
			String label) {
		return getDocLink(context, doc.getOwner(), doc, label);
	}

	/**
	 * Print the link for the given member.
	 *
	 * @param context
	 *            the id of the context where the link will be printed.
	 * @param classDoc
	 *            the classDoc that we should link to. This is not necessarily
	 *            equal to doc.containingClass(). We may be inheriting comments.
	 * @param doc
	 *            the member being linked to.
	 * @param label
	 *            the label for the link.
	 * @param strong
	 *            true if the link should be strong.
	 */
	public void printDocLink(int context,
			AbstractDocumentationBuilder<?> classDoc,
			FieldDocumentationBuilder<?> doc, String label, boolean strong) {
		print(getDocLink(context, classDoc, doc, label, strong));
	}

	/**
	 * Return the link for the given member.
	 *
	 * @param context
	 *            the id of the context where the link will be printed.
	 * @param doc
	 *            the member being linked to.
	 * @param label
	 *            the label for the link.
	 * @param strong
	 *            true if the link should be strong.
	 * @return the link for the given member.
	 */
	public String getDocLink(int context, FieldDocumentationBuilder<?> doc,
			String label, boolean strong) {
		return getDocLink(context, doc.getOwner(), doc, label, strong);
	}

	/**
	 * Return the link for the given member.
	 *
	 * @param context
	 *            the id of the context where the link will be printed.
	 * @param doc
	 *            the member being linked to.
	 * @param label
	 *            the label for the link.
	 * @param strong
	 *            true if the link should be strong.
	 * @return the link for the given member.
	 */
	public String getDocLink(int context, FacetDocumentationBuilder doc,
			String label, boolean strong) {
		return getDocLink(context, doc.getOwner(), doc, label, strong);
	}

	/**
	 * Return the link for the given member.
	 *
	 * @param context
	 *            the id of the context where the link will be printed.
	 * @param classDoc
	 *            the classDoc that we should link to. This is not necessarily
	 *            equal to doc.containingClass(). We may be inheriting comments.
	 * @param doc
	 *            the member being linked to.
	 * @param label
	 *            the label for the link.
	 * @param strong
	 *            true if the link should be strong.
	 * @return the link for the given member.
	 */
	public String getDocLink(int context,
			DocumentationBuilder classDoc, DocumentationBuilder doc,
			String label, boolean strong) {
		if (!(Util.isLinkable(classDoc, configuration()))) {
			return label;
		} else{
			return getLink(new LinkInfoImpl(context, classDoc, doc.getName(),
					label, strong));
		}
	}

	/**
	 * Return the link for the given member.
	 *
	 * @param context
	 *            the id of the context where the link will be added
	 * @param classDoc
	 *            the classDoc that we should link to. This is not necessarily
	 *            equal to doc.containingClass(). We may be inheriting comments
	 * @param doc
	 *            the member being linked to
	 * @param label
	 *            the label for the link
	 * @return the link for the given member
	 */
	public Content getDocLink(int context,
			AbstractDocumentationBuilder<?> classDoc,
			FieldDocumentationBuilder<?> doc, String label) {
		if (!(Util.isLinkable(classDoc, configuration()))) {
			return new StringContent(label);
		} else if (doc instanceof FieldDocumentationBuilder<?>) {
			return new RawHtml(getLink(new LinkInfoImpl(context, classDoc,
					doc.getName(), label, false)));
		} else {
			return new StringContent(label);
		}
	}

	public void anchor(AbstractDocumentationBuilder<?> emd) {
		anchor(getAnchor(emd));
	}

	public String getAnchor(AbstractDocumentationBuilder<?> emd) {
		return getAnchor(emd, false);
	}

	public String getAnchor(AbstractDocumentationBuilder<?> emd,
			boolean isProperty) {
		if (isProperty) {
			return emd.getName();
		}
		// StringBuilder signature = new StringBuilder(emd.signature());
		// StringBuilder signatureParsed = new StringBuilder();
		// int counter = 0;
		// for (int i = 0; i < signature.length(); i++) {
		// char c = signature.charAt(i);
		// if (c == '<') {
		// counter++;
		// } else if (c == '>') {
		// counter--;
		// } else if (counter == 0) {
		// signatureParsed.append(c);
		// }
		// }
		return emd.getName();// + signatureParsed.toString();
	}


	/**
	 * Adds the summary content.
	 *
	 * @param doc
	 *            the doc for which the summary will be generated
	 * @param htmltree
	 *            the documentation tree to which the summary will be added
	 */
	public void addSummaryComment(DocumentationBuilder doc, Content htmltree) {
		String desc = doc.getDescription();
		addSummaryComment(desc == null ? "" : desc, htmltree);
	}

	/**
	 * Adds the summary content.
	 *
	 * @param doc
	 *            the doc for which the summary will be generated
	 * @param htmltree
	 *            the documentation tree to which the summary will be added
	 */
	public void addSummaryComment(TLDocumentationOwner owner, Content htmltree) {
		TLDocumentation doc = owner.getDocumentation();
		String desc = "";
		if (doc != null) {
			desc = doc.getDescription();
		}
		addSummaryComment(desc, htmltree);
	}


	/**
	 * Adds the summary content.
	 *
	 * @param doc
	 *            the doc for which the summary will be generated
	 * @param firstSentenceTags
	 *            the first sentence tags for the doc
	 * @param htmltree
	 *            the documentation tree to which the summary will be added
	 */
	public void addSummaryComment(String firstSentenceTags, Content htmltree) {
		addCommentTags(firstSentenceTags, false, true, htmltree);
	}

	/**
	 * Adds the inline comment.
	 *
	 * @param doc
	 *            the doc for which the inline comments will be generated
	 * @param htmltree
	 *            the documentation tree to which the inline comments will be
	 *            added
	 */
	public void addInlineComment(String comment, Content htmltree) {
		addCommentTags(comment, false, false, htmltree);
	}


	/**
	 * Adds the comment tags.
	 *
	 * @param doc
	 *            the doc for which the comment tags will be generated
	 * @param comment
	 *            the first sentence tags for the doc
	 * @param depr
	 *            true if it is deprecated
	 * @param first
	 *            true if the first sentenge tags should be added
	 * @param htmltree
	 *            the documentation tree to which the comment tags will be added
	 */
	private void addCommentTags(String comment, boolean depr, boolean first,
			Content htmltree) {
		Content div;
		Content result = new RawHtml(comment);
		if (depr) {
			Content italic = HtmlTree.i(result);
			div = HtmlTree.div(HtmlStyle.block, italic);
			htmltree.addContent(div);
		} else {
			div = HtmlTree.div(HtmlStyle.block, result);
			htmltree.addContent(div);
		}
		if (comment.length() == 0) {
			htmltree.addContent(getSpace());
		}
	}

	

	public String removeNonInlineHtmlTags(String text) {
		if (text.indexOf('<') < 0) {
			return text;
		}
		String noninlinetags[] = { "<ul>", "</ul>", "<ol>", "</ol>", "<dl>",
				"</dl>", "<table>", "</table>", "<tr>", "</tr>", "<td>",
				"</td>", "<th>", "</th>", "<p>", "</p>", "<li>", "</li>",
				"<dd>", "</dd>", "<dir>", "</dir>", "<dt>", "</dt>", "<h1>",
				"</h1>", "<h2>", "</h2>", "<h3>", "</h3>", "<h4>", "</h4>",
				"<h5>", "</h5>", "<h6>", "</h6>", "<pre>", "</pre>", "<menu>",
				"</menu>", "<listing>", "</listing>", "<hr>", "<blockquote>",
				"</blockquote>", "<center>", "</center>", "<UL>", "</UL>",
				"<OL>", "</OL>", "<DL>", "</DL>", "<TABLE>", "</TABLE>",
				"<TR>", "</TR>", "<TD>", "</TD>", "<TH>", "</TH>", "<P>",
				"</P>", "<LI>", "</LI>", "<DD>", "</DD>", "<DIR>", "</DIR>",
				"<DT>", "</DT>", "<H1>", "</H1>", "<H2>", "</H2>", "<H3>",
				"</H3>", "<H4>", "</H4>", "<H5>", "</H5>", "<H6>", "</H6>",
				"<PRE>", "</PRE>", "<MENU>", "</MENU>", "<LISTING>",
				"</LISTING>", "<HR>", "<BLOCKQUOTE>", "</BLOCKQUOTE>",
				"<CENTER>", "</CENTER>" };
		for (int i = 0; i < noninlinetags.length; i++) {
			text = replace(text, noninlinetags[i], "");
		}
		return text;
	}

	public String replace(String text, String tobe, String by) {
		while (true) {
			int startindex = text.indexOf(tobe);
			if (startindex < 0) {
				return text;
			}
			int endindex = startindex + tobe.length();
			StringBuilder replaced = new StringBuilder();
			if (startindex > 0) {
				replaced.append(text.substring(0, startindex));
			}
			replaced.append(by);
			if (text.length() > endindex) {
				replaced.append(text.substring(endindex));
			}
			text = replaced.toString();
		}
	}

	public void printStyleSheetProperties() {
		String filename = configuration.stylesheetfile;
		if (filename.length() > 0) {
			File stylefile = new File(filename);
			String parent = stylefile.getParent();
			filename = (parent == null) ? filename : filename.substring(parent
					.length() + 1);
		} else {
			filename = "stylesheet.css";
		}
		filename = relativePath + filename;
		link("REL =\"stylesheet\" TYPE=\"text/css\" HREF=\"" + filename + "\" "
				+ "TITLE=\"Style\"");
	}

	/**
	 * Returns a link to the stylesheet file.
	 *
	 * @return an HtmlTree for the lINK tag which provides the stylesheet
	 *         location
	 */
	public HtmlTree getStyleSheetProperties() {
		String filename = configuration.stylesheetfile;
		if (filename.length() > 0) {
			File stylefile = new File(filename);
			String parent = stylefile.getParent();
			filename = (parent == null) ? filename : filename.substring(parent
					.length() + 1);
		} else {
			filename = HtmlDoclet.DEFAULT_STYLESHEET;
		}
		filename = relativePath + filename;
		HtmlTree link = HtmlTree.link("stylesheet", "text/css", filename,
				"Style");
		return link;
	}

	/**
	 * According to <cite>The Java&trade; Language Specification</cite>, all the
	 * outer classes and static nested classes are core classes.
	 */
	public boolean isCoreClass(AbstractDocumentationBuilder<?> cd) {
		return !(cd instanceof FacetDocumentationBuilder || cd instanceof FieldDocumentationBuilder<?>);
	}

	// /**
	// * Write the annotatation types for the given String.
	// *
	// * @param String
	// * the package to write annotations for.
	// */
	// public void writeAnnotationInfo(String packageDoc) {
	// writeAnnotationInfo(packageDoc, packageDoc.annotations());
	// }
	//
	// /**
	// * Adds the annotatation types for the given packageDoc.
	// *
	// * @param packageDoc
	// * the package to write annotations for.
	// * @param htmltree
	// * the documentation tree to which the annotation info will be
	// * added
	// */
	// public void addAnnotationInfo(String packageDoc, Content htmltree) {
	// addAnnotationInfo(packageDoc, packageDoc.annotations(), htmltree);
	// }
	//
	// /**
	// * Write the annotatation types for the given doc.
	// *
	// * @param doc
	// * the doc to write annotations for.
	// */
	// public void writeAnnotationInfo(ProgramElementDoc doc) {
	// writeAnnotationInfo(doc, doc.annotations());
	// }
	//
	// /**
	// * Adds the annotatation types for the given doc.
	// *
	// * @param packageDoc
	// * the package to write annotations for
	// * @param htmltree
	// * the content tree to which the annotation types will be added
	// */
	// public void addAnnotationInfo(ProgramElementDoc doc, Content htmltree) {
	// addAnnotationInfo(doc, doc.annotations(), htmltree);
	// }
	//
	// /**
	// * Write the annotatation types for the given doc and parameter.
	// *
	// * @param indent
	// * the number of spaced to indent the parameters.
	// * @param doc
	// * the doc to write annotations for.
	// * @param param
	// * the parameter to write annotations for.
	// */
	// public boolean writeAnnotationInfo(int indent, Doc doc, Parameter param)
	// {
	// return writeAnnotationInfo(indent, doc, param.annotations(), false);
	// }
	//
	// /**
	// * Add the annotatation types for the given doc and parameter.
	// *
	// * @param indent
	// * the number of spaces to indent the parameters.
	// * @param doc
	// * the doc to write annotations for.
	// * @param param
	// * the parameter to write annotations for.
	// * @param tree
	// * the content tree to which the annotation types will be added
	// */
	// public boolean addAnnotationInfo(int indent, Doc doc, Parameter param,
	// Content tree) {
	// return addAnnotationInfo(indent, doc, param.annotations(), false, tree);
	// }
	//
	// /**
	// * Write the annotatation types for the given doc.
	// *
	// * @param doc
	// * the doc to write annotations for.
	// * @param descList
	// * the array of {@link AnnotationDesc}.
	// */
	// private void writeAnnotationInfo(Doc doc, AnnotationDesc[] descList) {
	// writeAnnotationInfo(0, doc, descList, true);
	// }
	//
	// /**
	// * Adds the annotatation types for the given doc.
	// *
	// * @param doc
	// * the doc to write annotations for.
	// * @param descList
	// * the array of {@link AnnotationDesc}.
	// * @param htmltree
	// * the documentation tree to which the annotation info will be
	// * added
	// */
	// private void addAnnotationInfo(Doc doc, AnnotationDesc[] descList,
	// Content htmltree) {
	// addAnnotationInfo(0, doc, descList, true, htmltree);
	// }
	//
	// /**
	// * Write the annotatation types for the given doc.
	// *
	// * @param indent
	// * the number of extra spaces to indent the annotations.
	// * @param doc
	// * the doc to write annotations for.
	// * @param descList
	// * the array of {@link AnnotationDesc}.
	// */
	// private boolean writeAnnotationInfo(int indent, Doc doc,
	// AnnotationDesc[] descList, boolean lineBreak) {
	// List<String> annotations = getAnnotations(indent, descList, lineBreak);
	// if (annotations.size() == 0) {
	// return false;
	// }
	// fontNoNewLine("-1");
	// for (Iterator<String> iter = annotations.iterator(); iter.hasNext();) {
	// print(iter.next());
	// }
	// fontEnd();
	// return true;
	// }
	//
	// /**
	// * Adds the annotatation types for the given doc.
	// *
	// * @param indent
	// * the number of extra spaces to indent the annotations.
	// * @param doc
	// * the doc to write annotations for.
	// * @param descList
	// * the array of {@link AnnotationDesc}.
	// * @param htmltree
	// * the documentation tree to which the annotation info will be
	// * added
	// */
	// private boolean addAnnotationInfo(int indent, Doc doc,
	// AnnotationDesc[] descList, boolean lineBreak, Content htmltree) {
	// List<String> annotations = getAnnotations(indent, descList, lineBreak);
	// if (annotations.size() == 0) {
	// return false;
	// }
	// Content annotationContent;
	// for (Iterator<String> iter = annotations.iterator(); iter.hasNext();) {
	// annotationContent = new RawHtml(iter.next());
	// htmltree.addContent(annotationContent);
	// }
	// return true;
	// }
	//
	// /**
	// * Return the string representations of the annotation types for the given
	// * doc.
	// *
	// * @param indent
	// * the number of extra spaces to indent the annotations.
	// * @param descList
	// * the array of {@link AnnotationDesc}.
	// * @param linkBreak
	// * if true, add new line between each member value.
	// * @return an array of strings representing the annotations being
	// * documented.
	// */
	// private List<String> getAnnotations(int indent, AnnotationDesc[]
	// descList,
	// boolean linkBreak) {
	// List<String> results = new ArrayList<String>();
	// StringBuffer annotation;
	// for (int i = 0; i < descList.length; i++) {
	// AnnotationTypeDoc annotationDoc = descList[i].annotationType();
	// if (!Util.isDocumentedAnnotation(annotationDoc)) {
	// continue;
	// }
	// annotation = new StringBuffer();
	// LinkInfoImpl linkInfo = new LinkInfoImpl(
	// LinkInfoImpl.CONTEXT_ANNOTATION, annotationDoc);
	// linkInfo.label = "@" + annotationDoc.name();
	// annotation.append(getLink(linkInfo));
	// AnnotationDesc.ElementValuePair[] pairs = descList[i]
	// .elementValues();
	// if (pairs.length > 0) {
	// annotation.append('(');
	// for (int j = 0; j < pairs.length; j++) {
	// if (j > 0) {
	// annotation.append(",");
	// if (linkBreak) {
	// annotation.append(DocletConstants.NL);
	// int spaces = annotationDoc.name().length() + 2;
	// for (int k = 0; k < (spaces + indent); k++) {
	// annotation.append(' ');
	// }
	// }
	// }
	// annotation.append(getDocLink(
	// LinkInfoImpl.CONTEXT_ANNOTATION,
	// pairs[j].element(), pairs[j].element().name(),
	// false));
	// annotation.append('=');
	// AnnotationValue annotationValue = pairs[j].value();
	// List<AnnotationValue> annotationTypeValues = new
	// ArrayList<AnnotationValue>();
	// if (annotationValue.value() instanceof AnnotationValue[]) {
	// AnnotationValue[] annotationArray = (AnnotationValue[]) annotationValue
	// .value();
	// for (int k = 0; k < annotationArray.length; k++) {
	// annotationTypeValues.add(annotationArray[k]);
	// }
	// } else {
	// annotationTypeValues.add(annotationValue);
	// }
	// annotation.append(annotationTypeValues.size() == 1 ? ""
	// : "{");
	// for (Iterator<AnnotationValue> iter = annotationTypeValues
	// .iterator(); iter.hasNext();) {
	// annotation.append(annotationValueToString(iter.next()));
	// annotation.append(iter.hasNext() ? "," : "");
	// }
	// annotation.append(annotationTypeValues.size() == 1 ? ""
	// : "}");
	// }
	// annotation.append(")");
	// }
	// annotation.append(linkBreak ? DocletConstants.NL : "");
	// results.add(annotation.toString());
	// }
	// return results;
	// }
	//
	// private String annotationValueToString(AnnotationValue annotationValue) {
	// if (annotationValue.value() instanceof Type) {
	// Type type = (Type) annotationValue.value();
	// if (type.asClassDoc() != null) {
	// LinkInfoImpl linkInfo = new LinkInfoImpl(
	// LinkInfoImpl.CONTEXT_ANNOTATION, type);
	// linkInfo.label = (type.asClassDoc().isIncluded() ? type
	// .typeName() : type.qualifiedTypeName())
	// + type.dimension() + ".class";
	// return getLink(linkInfo);
	// } else {
	// return type.typeName() + type.dimension() + ".class";
	// }
	// } else if (annotationValue.value() instanceof AnnotationDesc) {
	// List<String> list = getAnnotations(0,
	// new AnnotationDesc[] { (AnnotationDesc) annotationValue
	// .value() }, false);
	// StringBuffer buf = new StringBuffer();
	// for (Iterator<String> iter = list.iterator(); iter.hasNext();) {
	// buf.append(iter.next());
	// }
	// return buf.toString();
	// } else if (annotationValue.value() instanceof MemberDoc) {
	// return getDocLink(LinkInfoImpl.CONTEXT_ANNOTATION,
	// (MemberDoc) annotationValue.value(),
	// ((MemberDoc) annotationValue.value()).name(), false);
	// } else {
	// return annotationValue.toString();
	// }
	// }

	/**
	 * Return the configuation for this doclet.
	 *
	 * @return the configuration for this doclet.
	 */
	public Configuration configuration() {
		return configuration;
	}
}

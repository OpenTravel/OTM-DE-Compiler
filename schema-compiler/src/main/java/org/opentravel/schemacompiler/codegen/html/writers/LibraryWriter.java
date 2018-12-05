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
import org.opentravel.schemacompiler.codegen.html.builders.LibraryDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlConstants;
import org.opentravel.schemacompiler.codegen.html.markup.StringContent;
import org.opentravel.schemacompiler.codegen.html.DirectoryManager;
import org.opentravel.schemacompiler.codegen.html.writers.info.LibraryInfoWriter;

public class LibraryWriter extends SubWriterHolderWriter implements
		LibrarySummaryWriter {

	/**
	 * The prev library name in the alpha-order list.
	 */
	protected LibraryDocumentationBuilder prev;

	/**
	 * The next library name in the alpha-order list.
	 */
	protected LibraryDocumentationBuilder next;

	/**
	 * The library being documented.
	 */
	protected LibraryDocumentationBuilder library;


	/**
	 * The name of the output file.
	 */
	public static final String OUTPUT_FILE_NAME = "library-summary.html";

	/**
	 * Return the name of the output file.
	 *
	 * @return the name of the output file.
	 */
	public String getOutputFileName() {
		return OUTPUT_FILE_NAME;
	}
	
	/**
	 * Constructor to construct LibraryWriter object and to generate
	 * "library-summary.html" file in the respective library directory. 
	 *
	 * @param configuration
	 *            the configuration of the doclet.
	 * @param library
	 *            Library under consideration.
	 * @param prev
	 *            Previous library in the sorted array.
	 * @param next
	 *            Next library in the sorted array.
	 */
	public LibraryWriter(Configuration configuration,
			LibraryDocumentationBuilder library,
			LibraryDocumentationBuilder prev, LibraryDocumentationBuilder next)
			throws IOException {
		super(configuration, DirectoryManager.getDirectoryPath(library
				.getName()), OUTPUT_FILE_NAME, DirectoryManager
				.getRelativePath(library.getName()));
		this.prev = prev;
		this.next = next;
		this.library = library;
	}

	/**
	 * {@inheritDoc}
	 */
	public Content getHeader() {
		String namespace = library.getNamespace();
		Content bodyTree = getBody(true, getWindowTitle(namespace));
		addNavLinks(true, bodyTree);
		HtmlTree div = new HtmlTree(HtmlTag.DIV);
		div.addStyle(HtmlStyle.HEADER);
		Content tHeading = HtmlTree.heading(HtmlConstants.TITLE_HEADING, true,
				HtmlStyle.TITLE, libraryLabel);
		tHeading.addContent(getSpace());
		Content libraryHead = new RawHtml(library.getName());
		tHeading.addContent(libraryHead);
		div.addContent(tHeading);
		tHeading = HtmlTree.heading(HtmlConstants.TITLE_HEADING, true,
				HtmlStyle.TITLE, namespaceLabel );
		tHeading.addContent(getSpace());
		libraryHead = new RawHtml(namespace);
		tHeading.addContent(libraryHead);
		div.addContent(tHeading);
		bodyTree.addContent(div);
		return bodyTree;
	}

	/**
	 * {@inheritDoc}
	 */
	public Content getSummaryHeader() {
		HtmlTree ul = new HtmlTree(HtmlTag.UL);
		ul.addStyle(HtmlStyle.BLOCK_LIST);
		return ul;
	}
	
	public void addObjectsSummary(Content summaryContentTree) {
		LibraryInfoWriter infoWriter = new LibraryInfoWriter(this, library);
		infoWriter.addInfo(summaryContentTree);
	}


	@Override
	public void addNamespaceDescription(Content packageContentTree) {
		String doc = library.getDescription();
		if (doc != null) {
			packageContentTree
					.addContent(getMarkerAnchor("library_description"));
			Content h2Content = new StringContent(configuration.getText(
					"doclet.Library_Description", library.getName()));
			packageContentTree.addContent(HtmlTree.heading(
					HtmlConstants.PACKAGE_HEADING, true, h2Content));
			addInlineComment(doc, packageContentTree);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addFooter(Content contentTree) {
		addNavLinks(false, contentTree);
	}

	/**
	 * {@inheritDoc}
	 */
	public void printDocument(Content contentTree) {
		printHtmlDocument(null, true, contentTree);
	}


	/**
	 * Get "Use" link for this pacakge in the navigation bar.
	 *
	 * @return a content tree for the class use link
	 */
	@Override
	protected Content getNavLinkClassUse() {
		Content useLink = getHyperLink("library-use.html", "", useLabel, "", "");
		return HtmlTree.li(useLink);
	}

	/**
	 * Get "PREV PACKAGE" link in the navigation bar.
	 *
	 * @return a content tree for the previous link
	 */
	@Override
	public Content getNavLinkPrevious() {
		Content li;
		if (prev == null) {
			li = HtmlTree.li(prevLibraryLabel);
		} else {
			String path = DirectoryManager.getRelativePath(library.getName(),
					prev.getName());
			li = HtmlTree.li(getHyperLink(path + OUTPUT_FILE_NAME, "",
					prevLibraryLabel, "", ""));
		}
		return li;
	}

	/**
	 * Get "NEXT PACKAGE" link in the navigation bar.
	 *
	 * @return a content tree for the next link
	 */
	@Override
	public Content getNavLinkNext() {
		Content li;
		if (next == null) {
			li = HtmlTree.li(nextLibraryLabel);
		} else {
			String path = DirectoryManager.getRelativePath(library.getName(),
					next.getName());
			li = HtmlTree.li(getHyperLink(path + OUTPUT_FILE_NAME, "",
					nextLibraryLabel, "", ""));
		}
		return li;
	}

	/**
	 * Get "Tree" link in the navigation bar. This will be link to the package
	 * tree file.
	 *
	 * @return a content tree for the tree link
	 */
	@Override
	protected Content getNavLinkTree() {
		Content useLink = getHyperLink("package-tree.html", "", treeLabel, "", "");
		return HtmlTree.li(useLink);
	}

	/**
	 * Highlight "Library" in the navigation bar, as this is the package page.
	 *
	 * @return a content tree for the package link
	 */
	@Override
	protected Content getNavLinkLibrary() {
		return HtmlTree.li(HtmlStyle.NAV_BAR_CELL1_REV, libraryLabel);
	}

}

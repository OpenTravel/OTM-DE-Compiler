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

import org.opentravel.schemacompiler.model.TLDocumentation;

import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.NamedEntityDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.LinkInfoImpl;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlConstants;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.markup.StringContent;
import org.opentravel.schemacompiler.codegen.html.DirectoryManager;
import org.opentravel.schemacompiler.codegen.html.writers.info.DocumentationInfoWriter;
import org.opentravel.schemacompiler.codegen.html.writers.info.InfoWriter;

/**
 * @author Eric.Bronson
 *
 */
public class NamedEntityWriter<T extends NamedEntityDocumentationBuilder<?>>
		extends SubWriterHolderWriter implements LibraryMemberWriter {

	protected T member;

	protected DocumentationBuilder prev;

	protected DocumentationBuilder next;

	/**
	 * @param member
	 *            the class being documented.
	 * @param prev2
	 *            the previous class that was documented.
	 * @param next2
	 *            the next class being documented.
	 * @param classTree
	 *            the class tree for the given class.
	 */
	public NamedEntityWriter(T member, DocumentationBuilder prev,
			DocumentationBuilder next) throws Exception {
		super(Configuration.getInstance(), DirectoryManager
				.getDirectoryPath(member.getOwningLibrary()), member.getName()
				+ ".html", DirectoryManager.getRelativePath(member
				.getOwningLibrary()));
		this.member = member;
		configuration.setCurrentMember(member);
		this.prev = prev;
		this.next = next;
	}

	/**
	 * Get this library link.
	 *
	 * @return a content tree for the package link
	 */
	@Override
	protected Content getNavLinkLibrary() {
		Content linkContent = getHyperLink(LibraryWriter.OUTPUT_FILE_NAME, member.getOwningLibrary(),
				libraryLabel);
		return HtmlTree.li(linkContent);
	}

	/**
	 * Get the class link.
	 *
	 * @return a content tree for the class link
	 */
	@Override
	protected Content getNavLinkObject() {
		return HtmlTree.li(HtmlStyle.NAV_BAR_CELL1_REV, objectLabel);
	}

	/**
	 * Get the class use link.
	 *
	 * @return a content tree for the class use link
	 */
	@Override
	protected Content getNavLinkClassUse() {
		Content linkContent = getHyperLink("class-use/" + getFilename(), "", useLabel);
		return HtmlTree.li(linkContent);
	}

	/**
	 * Get link to previous class.
	 *
	 * @return a content tree for the previous class link
	 */
	@Override
	public Content getNavLinkPrevious() {
		Content li;
		if (prev != null) {
			Content prevLink = new RawHtml(getLink(new LinkInfoImpl(
					LinkInfoImpl.CONTEXT_CLASS, prev, "",
					configuration.getText("doclet.Prev_Object"), true)));
			li = HtmlTree.li(prevLink);
		} else {
			li = HtmlTree.li(prevObjectLabel);
		}
		return li;
	}

	/**
	 * Get link to next class.
	 *
	 * @return a content tree for the next class link
	 */
	@Override
	public Content getNavLinkNext() {
		Content li;
		if (next != null) {
			Content nextLink = new RawHtml(getLink(new LinkInfoImpl(
					LinkInfoImpl.CONTEXT_CLASS, next, "",
					configuration.getText("doclet.Next_Object"), true)));
			li = HtmlTree.li(nextLink);
		} else {
			li = HtmlTree.li(nextObjectLabel);
		}
		return li;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.LibraryMemberWriter
	 * #getHeader()
	 */
	@Override
	public Content getHeader() {
		String namespace = (member.getNamespace() != null) ? member
				.getNamespace() : "";
		String clname = member.getName();
		Content bodyTree = getBody(true, getWindowTitle(clname));
		addNavLinks(true, bodyTree);
		bodyTree.addContent(HtmlConstants.START_OF_CLASS_DATA);
		HtmlTree div = new HtmlTree(HtmlTag.DIV);
		div.setStyle(HtmlStyle.HEADER);
		if (namespace.length() > 0) {
			Content nsNameContent = new StringContent(namespace);
			Content nsNameDiv = HtmlTree.div(HtmlStyle.SUB_TITLE, nsNameContent);
			div.addContent(nsNameDiv);
		}
		LinkInfoImpl linkInfo = new LinkInfoImpl(
				LinkInfoImpl.CONTEXT_CLASS_HEADER, member, false);
		// Let's not link to ourselves in the header.
		linkInfo.setLinkToSelf(false);
		Content headerContent = new StringContent(member.getDocType()
				.toString() + " " + member.getName());
		Content heading = HtmlTree.heading(HtmlConstants.CLASS_PAGE_HEADING,
				true, HtmlStyle.TITLE, headerContent);
		div.addContent(heading);
		bodyTree.addContent(div);
		return bodyTree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.LibraryMemberWriter
	 * #addMemberTree(org.opentravel.schemacompiler.codegen.html.Content)
	 */
	@Override
	public void addMemberInheritanceTree(Content classContentTree) {
		classContentTree.addContent(getMemberInheritenceTree(member));
	}

	/**
	 * Get the class hierarchy tree for the given class.
	 *
	 * @param type
	 *            the class to print the hierarchy for
	 * @return a content tree for class inheritence
	 */
	protected Content getMemberInheritenceTree(T type) {
		DocumentationBuilder sup = type;
		HtmlTree classTreeUl = new HtmlTree(HtmlTag.UL);
		classTreeUl.setStyle(HtmlStyle.INHERITANCE);
		Content liTree = null;
		while (sup != null) {
			HtmlTree ul = new HtmlTree(HtmlTag.UL);
			ul.setStyle(HtmlStyle.INHERITANCE);
			ul.addContent(getTreeForClassHelper(sup));
			if (liTree != null) {
				ul.addContent(liTree);
			}
			Content li = HtmlTree.li(ul);
			liTree = li;
			if (sup instanceof NamedEntityDocumentationBuilder) {
				sup = getParent((NamedEntityDocumentationBuilder<?>) sup);
			} else {
				sup = null;
			}
		}
		if (liTree != null) {
			classTreeUl.addContent(liTree);
		}
		return classTreeUl;
	}

	/**
	 * Get the class helper tree for the given class.
	 *
	 * @param sup
	 *            the class to print the helper for
	 * @return a content tree for class helper
	 */
	protected Content getTreeForClassHelper(DocumentationBuilder sup) {
		Content li = new HtmlTree(HtmlTag.LI);
		Content link = new RawHtml(getLink(new LinkInfoImpl(
				LinkInfoImpl.CONTEXT_CLASS_TREE_PARENT, sup,
				sup.getQualifiedName(), false)));
		
		li.addContent(link);
		return li;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.LibraryMemberWriter
	 * #getMemberInfoTreeHeader()
	 */
	@Override
	public Content getMemberInfoItemTree() {
		return getMemberTreeHeader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.LibraryMemberWriter
	 * #getMemberInfo(org.opentravel.schemacompiler.codegen.html.Content)
	 */
	@Override
	public Content getMemberInfoTree(Content classInfoTree) {
		return HtmlTree.div(HtmlStyle.SUMMARY, classInfoTree);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.LibraryMemberWriter
	 * #
	 * addMemberDescription(org.opentravel.schemacompiler.codegen.html.Content
	 * )
	 */
	@Override
	public void addDocumentationInfo(Content classInfoTree) {
		TLDocumentation doc = member.getElement().getDocumentation();
		if (doc != null) {
			InfoWriter docWriter = new DocumentationInfoWriter(this, doc);
			docWriter.addInfo(classInfoTree);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.LibraryMemberWriter
	 * #printDocument(org.opentravel.schemacompiler.codegen.html.Content)
	 */
	@Override
	public void printDocument(Content contentTree) {
		printHtmlDocument(null, true, contentTree);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.LibraryMemberWriter
	 * #addFooter(org.opentravel.schemacompiler.codegen.html.Content)
	 */
	@Override
	public void addFooter(Content contentTree) {
		contentTree.addContent(HtmlConstants.END_OF_CLASS_DATA);
		addNavLinks(false, contentTree);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.LibraryMemberWriter
	 * #getMemberDoc()
	 */
	@Override
	public T getMember() {
		return member;
	}

	protected DocumentationBuilder getParent(
			NamedEntityDocumentationBuilder<?> classDoc) {
		return classDoc.getSuperType();
	}

	/**
	 * Add gap between navigation bar elements.
	 *
	 * @param liNav
	 *            the content tree to which the gap will be added
	 */
	protected void addNavGap(Content liNav) {
		liNav.addContent(getSpace());
		liNav.addContent("|");
		liNav.addContent(getSpace());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Content getNavLinkTree() {
		Content treeLinkContent = getHyperLink("namespace-tree.html", "", treeLabel, "", "");
		return HtmlTree.li(treeLinkContent);
	}

}

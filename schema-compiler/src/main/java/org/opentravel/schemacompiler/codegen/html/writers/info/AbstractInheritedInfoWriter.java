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
package org.opentravel.schemacompiler.codegen.html.writers.info;

import java.util.List;

import org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.LinkInfoImpl;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlConstants;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public abstract class AbstractInheritedInfoWriter<T extends AbstractDocumentationBuilder<?>, S extends AbstractDocumentationBuilder<?>> extends AbstractInfoWriter<T> {

	/**
	 * @param writer
	 * @param owner
	 */
	public AbstractInheritedInfoWriter(SubWriterHolderWriter writer,
			T owner) {
		super(writer, owner);
	}
	
	/* (non-Javadoc)
	 * @see org.opentravel.schemacompiler.codegen.documentation.html.writers.AbstractInfoWriter#addInfo(org.opentravel.schemacompiler.codegen.documentation.html.Content)
	 */
	@Override
	public void addInfo(Content memberTree) {
		Content infoTree = getInfoTree(); // ul
		Content content = getInfoTreeHeader(); // li
		addInfoSummary(content);
		infoTree.addContent(content);
		memberTree.addContent(infoTree);
		infoTree = getInfoTree(); // ul
		content = getInfoTreeHeader(); // li
		addInheritedInfoSummary(content);
		infoTree.addContent(content);
		memberTree.addContent(infoTree);
	}


	/**
	 * Build the inherited member summary for the given fields.
	 *
	 * @param writer
	 *            the writer for this member summary.
	 * @param visibleMemberMap
	 *            the map for the members to document.
	 * @param summaryTreeList
	 *            list of content trees to which the documentation will be added
	 */
	protected abstract void addInheritedInfoSummary(Content summaryTree);


	protected abstract void addInheritedInfoAnchor(T parent,
			Content inheritedTree);

	protected abstract T getParent(T classDoc);
	
	protected abstract Content getInfo(S field, int counter, boolean addCollapse);
	
	/**
	 * Add the inherited member summary.
	 *
	 * @param mw
	 *            the writer for the member being documented
	 * @param cd
	 *            the class being documented
	 * @param member
	 *            the member being documented
	 * @param isFirst
	 *            true if its the first link being documented
	 * @param linksTree
	 *            the content tree to which the summary will be added
	 */
	protected void addInheritedInfo(List<S> inherited, T parent,
			Content linksTree) {
		for (S pdb : inherited) {
			if (!(inherited.indexOf(pdb) == 0)) {
				linksTree.addContent(", ");
			}
			addInheritedInfoLink(parent, pdb, linksTree);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	protected void addInheritedInfoLabel(T parent, Content inheritedTree,
			String labelKey) {
		Content classLink = new RawHtml(writer.getPreQualifiedMemberLink(
				LinkInfoImpl.CONTEXT_MEMBER, parent, false));
		Content label = writer.getResource((labelKey));
		Content labelHeading = HtmlTree.heading(
				HtmlConstants.INHERITED_SUMMARY_HEADING, label);
		labelHeading.addContent(writer.getSpace());
		labelHeading.addContent(classLink);
		inheritedTree.addContent(labelHeading);
	}

	protected void addInheritedInfoLink(T cd, S member, Content linksTree) {
		linksTree.addContent(new RawHtml(writer.getDocLink(
				LinkInfoImpl.CONTEXT_MEMBER, cd, member, member.getName(),
				false)));
	}
	
	/**
	 * Add the inherited summary header.
	 *
	 * @param mw
	 *            the writer for the member being documented
	 * @param cd
	 *            the classdoc to be documented
	 * @param inheritedTree
	 *            the content tree to which the inherited summary header will be
	 *            added
	 */
	protected void addInheritedInfoHeader(T parent, Content inheritedTree,
			String labelKey) {
		addInheritedInfoAnchor(parent, inheritedTree);
		addInheritedInfoLabel(parent, inheritedTree, labelKey);
	}


}

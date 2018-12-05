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

import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;

import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.markup.StringContent;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public class DocumentationInfoWriter extends
		AbstractInfoWriter<TLDocumentation> {

	/**
	 * @param writer
	 * @param owner
	 */
	public DocumentationInfoWriter(SubWriterHolderWriter writer,
			TLDocumentation source) {
		super(writer, source);
		caption = writer.newConfiguration().getText(
				"doclet.Additional_Documentation");
		title = writer.getResource("doclet.Description");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.documentation.html.writers.AbstractInfoWriter
	 * #addInfo(org.opentravel.schemacompiler.codegen.documentation.html.Content)
	 */
	@Override
	public void addInfo(Content memberTree) {
		Content infoTree = getInfoTree(); // ul
		Content content = getInfoTreeHeader(); // li
		addInfoSummary(content);
		infoTree.addContent(content);
		memberTree.addContent(infoTree);
		memberTree.addContent(new HtmlTree(HtmlTag.HR)); // dividing line
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.documentation.html.writers.AbstractInfoWriter
	 * #addInfoSummary(org.opentravel.schemacompiler.codegen.documentation.html.Content)
	 */
	@Override
	protected void addInfoSummary(Content infoTree) {
		Content label = getInfoLabel();
		infoTree.addContent(label);
		Content desc = getDescriptionTree();
		infoTree.addContent(desc);
		Content tableTree = getTableTree();
		Configuration config = writer.newConfiguration();
		List<? extends TLDocumentationItem> values = source.getImplementers();
		boolean hasAdditionalDoc = false;
		if (!values.isEmpty()) {
			addDocumentationSummary(config.getText("doclet.Implementers"),
					values, tableTree, 1);
			hasAdditionalDoc = true;
		}
		values = source.getDeprecations();
		if (!values.isEmpty()) {
			addDocumentationSummary(config.getText("doclet.Deprecations"),
					values, tableTree, 0);
			hasAdditionalDoc = true;
		}
		values = source.getReferences();
		if (!values.isEmpty()) {
			addDocumentationSummary(config.getText("doclet.ReferenceLinks"),
					values, tableTree, 1);
			hasAdditionalDoc = true;
		}
		values = source.getMoreInfos();
		if (!values.isEmpty()) {
			addDocumentationSummary(config.getText("doclet.MoreInfos"), values,
					tableTree, 0);
			hasAdditionalDoc = true;
		}
		values = source.getOtherDocs();
		if (!values.isEmpty()) {
			addDocumentationSummary(config.getText("doclet.OtherDocs"), values,
					tableTree, 1);
			hasAdditionalDoc = true;
		}
		if (hasAdditionalDoc) {
			infoTree.addContent(tableTree);
		}
	}

	private Content getDescriptionTree() {
		String desc = source.getDescription();
		Content content;
		if (desc == null) {
			content = HtmlTree.EMPTY;
		} else {
			HtmlTree tdDesc = HtmlTree.td(writer
					.getResource("doclet.Description"));
			HtmlTree tdComment = HtmlTree.td(HtmlTree.heading(HtmlTag.H5,
					new StringContent(desc)));
			HtmlTree descRow = HtmlTree.tr(tdDesc);
			descRow.addContent(tdComment);
			HtmlTree descTree = new HtmlTree(HtmlTag.PRE);
			descTree.addContent(desc);
			content = HtmlTree.div(HtmlStyle.DESCRIPTION, descTree);
		}
		return content;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.documentation.html.writers.AbstractInfoWriter
	 * #getInfoTableSummary()
	 */
	@Override
	protected String getInfoTableSummary() {
		Configuration config = writer.newConfiguration();
		return config.getText("doclet.Documentation_Table_Summary",
				config.getText("doclet.Documentation_Summary"),
				config.getText("doclet.documentation"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.documentation.html.writers.AbstractInfoWriter
	 * #getInfoTableHeader()
	 */
	@Override
	protected String[] getInfoTableHeader() {
		Configuration config = writer.newConfiguration();
		return new String[] { config.getText("doclet.Type"),
				config.getText("doclet.Value") };
	}

	/**
	 * Add the member summary for the given class.
	 *
	 * @param member
	 *            the class that is being documented
	 * @param member
	 *            the member being documented
	 * @param firstSentenceTags
	 *            the first sentence tags to be added to the summary
	 * @param tableTree
	 *            the content tree to which the documentation will be added
	 * @param counter
	 *            the counter for determing style for the table row
	 */
	protected void addDocumentationSummary(String type,
			List<? extends TLDocumentationItem> values, Content tableTree,
			int counter) {
		HtmlTree tdDocType = new HtmlTree(HtmlTag.TD);
		tdDocType.setStyle(HtmlStyle.COL_FIRST);
		addDocumentationType(type, tdDocType);
		HtmlTree tdValue = new HtmlTree(HtmlTag.TD);
		setInfoColumnStyle(tdValue);
		addDocumentationValue(values, tdValue);
		HtmlTree tr = HtmlTree.tr(tdDocType);
		tr.addContent(tdValue);
		addRowStyle(tr, counter);
		tableTree.addContent(tr);
	}

	/**
	 * Add the member summary for the given class.
	 *
	 * @param member
	 *            the class that is being documented
	 * @param member
	 *            the member being documented
	 * @param firstSentenceTags
	 *            the first sentence tags to be added to the summary
	 * @param tableTree
	 *            the content tree to which the documentation will be added
	 * @param counter
	 *            the counter for determing style for the table row
	 */
	public void addDocumentationSummary(String type, String value,
			Content tableTree, int counter) {
		HtmlTree tdDocType = new HtmlTree(HtmlTag.TD);
		tdDocType.setStyle(HtmlStyle.COL_FIRST);
		addDocumentationType(type, tdDocType);
		HtmlTree tdValue = new HtmlTree(HtmlTag.TD);
		setInfoColumnStyle(tdValue);
		addDocumentationValue(value, tdValue);
		HtmlTree tr = HtmlTree.tr(tdDocType);
		tr.addContent(tdValue);
		addRowStyle(tr, counter);
		tableTree.addContent(tr);
	}

	protected void addDocumentationType(String name, Content tdSummaryType) {
		HtmlTree code = new HtmlTree(HtmlTag.CODE);
		code.addContent(new RawHtml(name));
		Content strong = HtmlTree.strong(code);
		tdSummaryType.addContent(strong);
	}

	/**
	 * Add the summary link for the member.
	 *
	 * @param cd
	 *            the class doc to be documented
	 * @param member
	 *            the member to be documented
	 * @param tdSummary
	 *            the content tree to which the link will be added
	 */
	protected void addDocumentationValue(
			List<? extends TLDocumentationItem> values, Content tdValue) {
		boolean firstValue = true;
		
		for (TLDocumentationItem value : values) {
			if (!firstValue) {
				tdValue.addContent(new StringContent(", "));
			}
			Content link = HtmlTree.code(new RawHtml(value.getText()));
			tdValue.addContent(link);
			firstValue = false;
		}
	}

	/**
	 * Add the summary link for the member.
	 *
	 * @param cd
	 *            the class doc to be documented
	 * @param member
	 *            the member to be documented
	 * @param tdSummary
	 *            the content tree to which the link will be added
	 */
	protected void addDocumentationValue(String value, Content tdValue) {
		Content link = HtmlTree.code(new RawHtml(value));
		tdValue.addContent(link);
	}

}

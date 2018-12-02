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

import org.opentravel.schemacompiler.codegen.html.builders.AliasOwnerDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlAttr;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlConstants;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public class AliasInfoWriter extends AbstractInfoWriter<AliasOwnerDocumentationBuilder>{

	/**
	 * 
	 */
	public AliasInfoWriter(SubWriterHolderWriter writer,
			AliasOwnerDocumentationBuilder owner) {
		super(writer, owner);
		caption = writer.newConfiguration().getText("doclet.Aliases");
	}

	

	public void addInfo(Content memberTree) {
		Content infoTree = getInfoTree(); // ul
		Content content = getInfoTreeHeader(); // li
		addInfoSummary(content);
		infoTree.addContent(content);
		memberTree.addContent(infoTree);
	}

	/**
	 * Add the member summary for the given class.
	 *
	 * @param classDoc
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
	public void addAliasSummary(List<String> aliases, Content tableTree) {
		HtmlTree tdAliasName = new HtmlTree(HtmlTag.TD);
		tdAliasName.setStyle(HtmlStyle.colOne);
		for (String alias : aliases) {
			if (aliases.indexOf(alias) != 0) {
				tdAliasName.addContent(", ");
			}
			addAliasName(alias, tdAliasName);
		}

		// HtmlTree tdValue = new HtmlTree(HtmlTag.TD);
		// setSummaryColumnStyle(tdValue);
		// addDocumentationValue(value, tdValue);
		HtmlTree tr = HtmlTree.tr(tdAliasName);
		// tr.addContent(tdValue);
		// if (counter % 2 == 0)
		// tr.addStyle(HtmlStyle.altColor);
		// else
		tr.addAttr(HtmlAttr.CLASS, HtmlStyle.rowColor + " " + HtmlStyle.rowOne);
		tableTree.addContent(tr);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void addAliasName(String name, Content tdSummaryType) {
		HtmlTree code = new HtmlTree(HtmlTag.CODE);
		code.addContent(new RawHtml(name));
		Content strong = HtmlTree.strong(code);
		tdSummaryType.addContent(strong);
	}

	/**
	 * Get the summary table.
	 *
	 * @param mw
	 *            the writer for the member being documented
	 * @param cd
	 *            the classdoc to be documented
	 * @return the content tree for the summary table
	 */
	@Override
	public Content getTableTree() {
		HtmlTree table = HtmlTree.table(HtmlStyle.overviewSummary, 0, 3, 0,
				getInfoTableSummary(),
				writer.getTableCaption(caption));
		table.addStyle(HtmlStyle.borderTop);
//		 table.addContent(getSummaryTableHeader(getInfoTableHeader(),
//		 "col"));
		return table;
	}

	/**
	 * {@inheritDoc}TODO pass key to method.
	 */
	protected String getInfoTableSummary() {
		Configuration config = writer.newConfiguration();
		return config.getText("doclet.Alias_Table_Summary",
				config.getText("doclet.Alias_Summary"),
				config.getText("doclet.aliases"));
	}


	@Override
	protected String[] getInfoTableHeader() {
		// no table header for aliases
		return new String[0];
	}

	@Override
	protected void addInfoSummary(Content memberTree) {
		Content label = HtmlTree.heading(HtmlConstants.SUMMARY_HEADING,writer.getResource("doclet.Alias_Summary"));
		memberTree.addContent(label);
		List<String> aliases = source.getAliases();
		if (!aliases.isEmpty()) {
			Content tableTree = getTableTree();
			addAliasSummary(aliases, tableTree);
			memberTree.addContent(tableTree);
		}
	}

}

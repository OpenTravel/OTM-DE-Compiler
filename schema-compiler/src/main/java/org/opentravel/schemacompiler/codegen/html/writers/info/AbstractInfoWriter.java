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

import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlAttr;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlConstants;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public abstract class AbstractInfoWriter<T> implements InfoWriter{
	
	protected Content title;
	
	protected String caption;

	/**
	 * The source of the info.
	 */
	protected T source;

	/**
	 * The subwriter, usually the writer calling this writer.
	 */
	protected final SubWriterHolderWriter writer;

	/**
	 * 
	 */
	public AbstractInfoWriter(SubWriterHolderWriter writer, T source) {
		this.source = source;
		this.writer = writer;
	}

	/**
	 * Add the actual info content.
	 * 
	 * @param memberTree
	 */
	protected abstract void addInfoSummary(Content memberTree);

	/**
	 * Add row style depending on rank.
	 * 
	 * @param tr
	 *            the row
	 * @param counter
	 *            the rank.
	 */
	protected void addRowStyle(HtmlTree tr, int counter) {
		if (counter % 2 == 0)
			tr.setStyle(HtmlStyle.ROW_COLOR);
		else
			tr.setStyle(HtmlStyle.ALT_COLOR);
	}

	/**
	 * Add the open/closed icons to the content.
	 * 
	 * @param content
	 *            the content to add the icons too.
	 * @param targetId
	 *            the id to collapse
	 * @param triggerId
	 *            the icon id, used to flip between open/closed.
	 */
	protected void addCollapseTrigger(Content content, String targetId,
			String triggerId) {
		HtmlTree openSpan = HtmlTree.span(HtmlTree.EMPTY);
		openSpan.addAttr(HtmlAttr.CLASS, HtmlStyle.TOGGLE_BUTTON + " "
				+ HtmlStyle.IMG_OPEN);
		String openId = triggerId + "Open";
		String closedId = triggerId + "Closed";
		openSpan.addAttr(HtmlAttr.ID, openId);
		openSpan.addAttr(HtmlAttr.TITLE, "open");
		openSpan.addDataAttr("target", "#" + targetId);
		openSpan.addDataAttr("toggle", "COLLAPSED");
		openSpan.addDataAttr("imgTarget", "#" + closedId);
		content.addContent(openSpan);
		HtmlTree closedSpan = HtmlTree.span(HtmlTree.EMPTY);
		closedSpan.setStyle(HtmlStyle.IMG_CLOSED);
		closedSpan.addAttr(HtmlAttr.ID, closedId);
		closedSpan.addAttr(HtmlAttr.TITLE, "closed");
		closedSpan.addDataAttr("target", "#" + targetId);
		closedSpan.addDataAttr("toggle", "COLLAPSED");
		closedSpan.addDataAttr("imgTarget", "#" + openId);
		content.addContent(closedSpan);
	}

	/**
	 * Make an html tree collapsible.
	 * 
	 * @param tree
	 *            the html tree to collapse.
	 * @param id
	 *            the html tree id. Used by a collapse trigger.
	 */
	protected void makeCollapsible(HtmlTree tree, String id) {
		tree.addAttr(HtmlAttr.ID, id);
		tree.setStyle(HtmlStyle.COLLAPSED);
	}

	/**
	 * Get an html tree for a table.
	 * 
	 * @param captionKey
	 *            the key used for the caption. If null there will be no
	 *            caption.
	 * @return the table html tree.
	 */
	protected Content getTableTree() {
		Content content = caption == null ? HtmlTree.EMPTY : writer
				.getTableCaption(caption);
		Content table = HtmlTree.table(HtmlStyle.OVERVIEW_SUMMARY, 0, 3, 0,
				getInfoTableSummary(), content);
		table.addContent(writer.getSummaryTableHeader(getInfoTableHeader(),
				"col"));
		return table;
	}

	/**
	 * Get the summary for the table.
	 */
	protected abstract String getInfoTableSummary();

	/**
	 * Get the table headers.
	 * @return
	 */
	protected abstract String[] getInfoTableHeader();
	
	protected Content getInfoLabel() {
		return HtmlTree.heading(HtmlConstants.SUMMARY_HEADING,
				title);
	}

	/**
	 * Get the table caption.
	 */
	protected String getCaption() {
		return caption;
	}
	
	/**
	 * Get the table caption.
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * Set the style for the summary column.
	 *
	 * @param tdTree
	 *            the column for which the style will be set
	 */
	protected void setInfoColumnStyle(HtmlTree tdTree) {
		tdTree.setStyle(HtmlStyle.COL_LAST);
	}

	/**
	 * Get the member header tree
	 *
	 * @return a content tree the member header
	 */
	public Content getInfoTreeHeader() {
		HtmlTree li = new HtmlTree(HtmlTag.LI);
		li.setStyle(HtmlStyle.BLOCK_LIST);
		return li;
	}

	/**
	 * Get the info tree
	 *
	 * @param contentTree
	 *            the tree used to generate the complete info tree
	 * @return a content tree for the info
	 */
	public Content getInfoTree() {
		HtmlTree ul = new HtmlTree(HtmlTag.UL);
		ul.setStyle(HtmlStyle.BLOCK_LIST);
		return ul;
	}


	/**
	 * @param title the title to set
	 */
	public void setTitle(Content title) {
		this.title = title;
	}

}

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

import org.opentravel.schemacompiler.codegen.html.builders.OperationDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.ServiceDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public class OperationInfoWriter extends
		AbstractInfoWriter<ServiceDocumentationBuilder> {

	/**
	 * @param writer
	 * @param member
	 */
	public OperationInfoWriter(SubWriterHolderWriter writer,
			ServiceDocumentationBuilder member) {
		super(writer, member);
		title = writer.getResource("doclet.Operation_Summary");
		caption = writer.newConfiguration().getText("doclet.Operations");
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.documentation.html.writers.AbstractInfoWriter
	 * #addInfoSummary(org.opentravel.schemacompiler.codegen.documentation.html.Content)
	 */
	@Override
	protected void addInfoSummary(Content memberTree) {
		Content label = getInfoLabel();
		memberTree.addContent(label);
		List<OperationDocumentationBuilder> facets = source.getOperations();
		if (!facets.isEmpty()) {
			Content tableTree = getTableTree();
			for (OperationDocumentationBuilder fdb : facets) {
				Content facetSummary = getInfo(fdb);
				tableTree.addContent(facetSummary);
			}
			memberTree.addContent(tableTree);
		}
	}

	private Content getInfo(OperationDocumentationBuilder fdb) {
		HtmlTree tdOperation = new HtmlTree(HtmlTag.TD);
		tdOperation.setStyle(HtmlStyle.colOne);
		tdOperation.addContent(HtmlTree.heading(HtmlTag.H4, new RawHtml(fdb.getName())));
		TLDocumentation doc = fdb.getElement().getDocumentation();
		if (doc != null) {
			DocumentationInfoWriter docWriter = new DocumentationInfoWriter(
					writer, doc);
			docWriter.addInfo(tdOperation);
		}
		FacetInfoWriter facetWriter = new FacetInfoWriter(writer, fdb);
		facetWriter.addInfo(tdOperation);
		HtmlTree tr = HtmlTree.tr(tdOperation);
		tr.setStyle(HtmlStyle.rowColor);
		return tr;
	}

	protected String getInfoTableSummary() {
		Configuration config = writer.newConfiguration();
		return config.getText("doclet.Operation_Table_Summary",
				config.getText("doclet.Operation_Summary"),
				config.getText("doclet.operations"));
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
		return new String[] { config.getText("doclet.Name") };
	}

}

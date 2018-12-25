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
import java.util.List;

import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.EnumValueDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.EnumerationDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.writers.info.AbstractInfoWriter;
import org.opentravel.schemacompiler.codegen.html.writers.info.InfoWriter;

/**
 * @author Eric.Bronson
 *
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EnumerationWriter extends NamedEntityWriter<EnumerationDocumentationBuilder> {

	/**
	 * @param member
	 * @param prev
	 * @param next
	 * @throws Exception
	 */
	public EnumerationWriter(EnumerationDocumentationBuilder member,
			DocumentationBuilder prev,
			DocumentationBuilder next) throws IOException {
		super(member, prev, next);
	}

	
	public void addValueInfo(Content content){
		InfoWriter valueWriter = new ValueInfoWriter(this, member);
		valueWriter.addInfo(content);
	}
	
	private class ValueInfoWriter extends AbstractInfoWriter<EnumerationDocumentationBuilder>{

		public ValueInfoWriter(SubWriterHolderWriter writer,
				EnumerationDocumentationBuilder source) {
			super(writer, source);
			title = writer.getResource("doclet.Value_Summary");
			caption = writer.newConfiguration().getText("doclet.Values");
		}

		@Override
		public void addInfo(Content memberTree) {
			Content infoTree = getInfoTree(); // ul
			Content content = getInfoTreeHeader(); // li
			addInfoSummary(content);
			infoTree.addContent(content);
			memberTree.addContent(infoTree);
		}

		@Override
		protected void addInfoSummary(Content memberTree) {
			Content label = getInfoLabel();
			memberTree.addContent(label);
			List<EnumValueDocumentationBuilder> values = source.getValues();
			if (!values.isEmpty()) {
				Content tableTree = getTableTree();
				for (EnumValueDocumentationBuilder edb : values) {
					HtmlTree tdName = new HtmlTree(HtmlTag.TD);
					tdName.setStyle(HtmlStyle.COL_FIRST);
					Content strong = HtmlTree.strong(new RawHtml(edb.getName()));
					Content code = HtmlTree.code(strong);
					tdName.addContent(code);
					HtmlTree tdDescription = new HtmlTree(HtmlTag.TD);
					setInfoColumnStyle(tdDescription);			
					writer.addSummaryComment(edb, tdDescription);
					HtmlTree tr = HtmlTree.tr(tdName);
					tr.addContent(tdDescription);
					addRowStyle(tr, values.indexOf(edb));
					tableTree.addContent(tr);
				}
				memberTree.addContent(tableTree);
			}
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getInfoTableSummary() {
			Configuration config = writer.newConfiguration();
			return config.getText("doclet.Enum_Value_Table_Summary",
					config.getText("doclet.Value_Summary"),
					config.getText("doclet.enum_values"));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.opentravel.schemacompiler.codegen.html.writers.AbstractInfoWriter
		 * #getInfoTableHeader()
		 */
		@Override
		protected String[] getInfoTableHeader() {
			Configuration config = writer.newConfiguration();
			
			return new String[] { config.getText("doclet.Name"),
					config.getText("doclet.Description") };
		}
		
	}

}

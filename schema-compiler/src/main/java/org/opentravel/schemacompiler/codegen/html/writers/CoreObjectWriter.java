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
import org.opentravel.schemacompiler.codegen.html.builders.CoreObjectDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.writers.info.AbstractInfoWriter;
import org.opentravel.schemacompiler.codegen.html.writers.info.InfoWriter;
import org.opentravel.schemacompiler.model.TLRole;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class CoreObjectWriter extends ComplexObjectWriter<CoreObjectDocumentationBuilder> {
	
	/**
	 * @param coreObject
	 *            the class being documented.
	 * @param prev
	 *            the previous class that was documented.
	 * @param next
	 *            the next class being documented.
	 * @param classTree
	 *            the class tree for the given class.
	 */
	public CoreObjectWriter(CoreObjectDocumentationBuilder coreObject,
			DocumentationBuilder prev, DocumentationBuilder next)
			throws IOException {
		super(coreObject, prev, next);		
	}
	

	public void addRoleInfo(Content content){
		InfoWriter roleWriter = new RoleInfoWriter(this, member);
		roleWriter.addInfo(content);
	}
	
	private class RoleInfoWriter extends AbstractInfoWriter<CoreObjectDocumentationBuilder>{

		public RoleInfoWriter(SubWriterHolderWriter writer,
				CoreObjectDocumentationBuilder source) {
			super(writer, source);
			title = writer.getResource("doclet.Role_Summary");
			caption = writer.newConfiguration().getText("doclet.Roles");
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
			List<TLRole> roles = source.getRoles();
			if (!roles.isEmpty()) {
				Content tableTree = getTableTree();
				for (TLRole role : roles) {
					HtmlTree tdName = new HtmlTree(HtmlTag.TD);
					tdName.setStyle(HtmlStyle.COL_FIRST);
					Content strong = HtmlTree.strong(new RawHtml(role.getName()));
					Content code = HtmlTree.code(strong);
					tdName.addContent(code);
					HtmlTree tdDescription = new HtmlTree(HtmlTag.TD);
					setInfoColumnStyle(tdDescription);			
					writer.addSummaryComment(role, tdDescription);
					HtmlTree tr = HtmlTree.tr(tdName);
					tr.addContent(tdDescription);
					addRowStyle(tr, roles.indexOf(role));
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
			return config.getText("doclet.Object_Table_Summary",
					config.getText("doclet.Role_Summary"),
					config.getText("doclet.Roles"));
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
			return new String[] { config.getText("doclet.Role"),
					config.getText("doclet.Description") };
		}
		
	}
}

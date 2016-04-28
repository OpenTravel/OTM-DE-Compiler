package org.opentravel.schemacompiler.codegen.html.writers;

import java.util.List;

import org.opentravel.schemacompiler.model.TLRole;

import org.opentravel.schemacompiler.codegen.html.builders.CoreObjectDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.writers.info.AbstractInfoWriter;
import org.opentravel.schemacompiler.codegen.html.writers.info.InfoWriter;

public class CoreObjectWriter 
	extends ComplexObjectWriter<CoreObjectDocumentationBuilder> {
	
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
			throws Exception {
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
			caption = writer.configuration().getText("doclet.Roles");
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
			if (roles.size() > 0) {
				Content tableTree = getTableTree();
				for (TLRole role : roles) {
					HtmlTree tdName = new HtmlTree(HtmlTag.TD);
					tdName.setStyle(HtmlStyle.colFirst);
					Content strong = HtmlTree.STRONG(new RawHtml(role.getName()));
					Content code = HtmlTree.CODE(strong);
					tdName.addContent(code);
					HtmlTree tdDescription = new HtmlTree(HtmlTag.TD);
					setInfoColumnStyle(tdDescription);			
					writer.addSummaryComment(role, tdDescription);
					HtmlTree tr = HtmlTree.TR(tdName);
					tr.addContent(tdDescription);
					addRowStyle(tr, roles.indexOf(role));
					tableTree.addContent(tr);
				}
				memberTree.addContent(tableTree);
			}
		}


		/**
		 * {@inheritDoc}TODO pass key to method.
		 */
		@Override
		protected String getInfoTableSummary() {
			Configuration config = writer.configuration();
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
			Configuration config = writer.configuration();
			String[] header = new String[] { config.getText("doclet.Role"),
					config.getText("doclet.Description") };
			return header;
		}
		
	}
}

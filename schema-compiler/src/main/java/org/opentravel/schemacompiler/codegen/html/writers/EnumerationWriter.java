/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import java.util.List;

import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.EnumValueDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.EnumerationDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
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
public class EnumerationWriter extends NamedEntityWriter<EnumerationDocumentationBuilder> {

	/**
	 * @param member
	 * @param prev
	 * @param next
	 * @throws Exception
	 */
	public EnumerationWriter(EnumerationDocumentationBuilder member,
			DocumentationBuilder prev,
			DocumentationBuilder next) throws Exception {
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
			caption = writer.configuration().getText("doclet.Values");
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
			if (values.size() > 0) {
				Content tableTree = getTableTree();
				for (EnumValueDocumentationBuilder edb : values) {
					HtmlTree tdName = new HtmlTree(HtmlTag.TD);
					tdName.setStyle(HtmlStyle.colFirst);
					Content strong = HtmlTree.STRONG(new RawHtml(edb.getName()));
					Content code = HtmlTree.CODE(strong);
					tdName.addContent(code);
					HtmlTree tdDescription = new HtmlTree(HtmlTag.TD);
					setInfoColumnStyle(tdDescription);			
					writer.addSummaryComment(edb, tdDescription);
					HtmlTree tr = HtmlTree.TR(tdName);
					tr.addContent(tdDescription);
					addRowStyle(tr, values.indexOf(edb));
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
			Configuration config = writer.configuration();
			String[] header = new String[] { config.getText("doclet.Name"),
					config.getText("doclet.Description") };
			return header;
		}
		
	}

}

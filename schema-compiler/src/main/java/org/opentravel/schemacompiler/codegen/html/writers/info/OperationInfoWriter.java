/**
 * 
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
		caption = writer.configuration().getText("doclet.Operations");
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
		if (facets.size() > 0) {
			Content tableTree = getTableTree();
			for (OperationDocumentationBuilder fdb : facets) {
				Content facetSummary = getInfo(fdb, facets.indexOf(fdb),
						false);
				tableTree.addContent(facetSummary);
			}
			memberTree.addContent(tableTree);
		}
	}

	private Content getInfo(OperationDocumentationBuilder fdb,
			int counter, boolean b) {
		HtmlTree tdOperation = new HtmlTree(HtmlTag.TD);
		tdOperation.setStyle(HtmlStyle.colOne);
		tdOperation.addContent(HtmlTree.HEADING(HtmlTag.H4, new RawHtml(fdb.getName())));
		TLDocumentation doc = fdb.getElement().getDocumentation();
		if (doc != null) {
			DocumentationInfoWriter docWriter = new DocumentationInfoWriter(
					writer, doc);
			docWriter.addInfo(tdOperation);
		}
		FacetInfoWriter facetWriter = new FacetInfoWriter(writer, fdb);
		facetWriter.addInfo(tdOperation);
		HtmlTree tr = HtmlTree.TR(tdOperation);
		tr.setStyle(HtmlStyle.rowColor);
		return tr;
	}

	protected String getInfoTableSummary() {
		Configuration config = writer.configuration();
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
		Configuration config = writer.configuration();
		String[] header = new String[] { config.getText("doclet.Name") };
		return header;
	}

}

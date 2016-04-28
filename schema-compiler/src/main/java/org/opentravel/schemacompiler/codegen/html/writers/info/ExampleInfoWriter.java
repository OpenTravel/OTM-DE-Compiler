/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers.info;

import org.opentravel.schemacompiler.codegen.html.builders.AttributeOwnerDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlConstants;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public class ExampleInfoWriter extends
		AbstractInfoWriter<AttributeOwnerDocumentationBuilder<?>> {

	/**
	 * @param writer
	 * @param owner
	 */
	public ExampleInfoWriter(SubWriterHolderWriter writer,
			AttributeOwnerDocumentationBuilder<?> owner) {
		super(writer, owner);
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
		addInfoSummary(memberTree);
	}

	protected void addInfoSummary(Content memberTree) {
		Content exampleTree = getExampleTree();
		addExample(source.getExampleXML(), "XML", exampleTree );
		addExample(source.getExampleJSON(), "JSON", exampleTree );
		memberTree.addContent(exampleTree);
	}
	

    /**
     * Get the member tree
     *
     * @param contentTree the tree used to generate the complete member tree
     * @return a content tree for the member
     */
    public Content getExampleTree() {    	
        HtmlTree div = new HtmlTree(HtmlTag.DIV);
        div.setStyle(HtmlStyle.example);
        return div;
    }

	protected Content getExampleHeader(String labelKey) {
		Content header = new HtmlTree(HtmlTag.DIV);
		Content label = writer.getResource(("doclet.Example_" + labelKey));
		HtmlTree labelHeading = HtmlTree.HEADING(
				HtmlConstants.INHERITED_SUMMARY_HEADING, label);
		addCollapseTrigger(labelHeading, "example" + labelKey, labelKey);
		header.addContent(labelHeading);
		return header;
	}

	protected void addExample(String example, String labelKey, Content exampleTree) {
		exampleTree.addContent(getExampleHeader(labelKey));
		HtmlTree pre = new HtmlTree(HtmlTag.PRE);
		pre.addContent(example);
		HtmlTree exampleDiv = HtmlTree.DIV(pre);
		makeCollapsible(exampleDiv, "example" + labelKey);
		exampleTree.addContent(exampleDiv);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.documentation.html.writers.AbstractInfoWriter
	 * #getTableSummary()
	 */
	@Override
	protected String getInfoTableSummary() {
		return "";
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
		return new String[0];
	}

}

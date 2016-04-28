/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers.info;

import java.util.List;

import org.opentravel.schemacompiler.codegen.html.builders.AttributeDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.AttributeOwnerDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public abstract class AbstractAttributeInfoWriter<T extends AttributeOwnerDocumentationBuilder<?>>
		extends AbstractFieldInfoWriter<T, AttributeDocumentationBuilder> {

	/**
	 * @param writer
	 * @param owner
	 * @param configuration
	 */
	public AbstractAttributeInfoWriter(SubWriterHolderWriter writer, T owner) {
		super(writer, owner);
		title = writer.getResource("doclet.Attribute_Summary");
		caption = writer.configuration().getText("doclet.Attributes");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemacompiler.codegen.documentation.html.writers.
	 * AbstractFieldInfoWriter
	 * #getFieldSummary(org.opentravel.schemacompiler.codegen.documentation.html.Content)
	 */
	@Override
	protected void addInfoSummary(Content summaryTree) {
		Content label = getInfoLabel();
		summaryTree.addContent(label);
		List<AttributeDocumentationBuilder> attributes = source.getAttributes();
		if (attributes.size() > 0) {
			Content tableTree = getTableTree();
			for (AttributeDocumentationBuilder adb : attributes) {
				Content propSummary = getInfo(adb, attributes.indexOf(adb), true);
				Content propDetail = getDetailedInfo(adb);
				tableTree.addContent(propSummary);
				tableTree.addContent(propDetail);
			}
			summaryTree.addContent(tableTree);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemacompiler.codegen.documentation.html.writers.
	 * AbstractFieldInfoWriter
	 * #getInheritedFieldSummary(org.opentravel.schemacompiler.codegen
	 * .documentation.html.Content)
	 */
	@Override
	protected void addInheritedInfoSummary(Content summaryTree) {
		T parent = getParent(source);
		while (parent != null) {
			if (parent.getAttributes().size() > 0) {
				addInheritedInfoHeader(parent, summaryTree,
						"doclet.Attributes_Inherited_From");
				addInheritedInfo(parent.getAttributes(), parent, summaryTree);
			}
			parent = getParent(parent);
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemacompiler.codegen.documentation.html.writers.
	 * AbstractFieldInfoWriter
	 * #addInheritedFieldAnchor(org.opentravel.schemacompiler.codegen
	 * .documentation.DocumentationBuilder,
	 * org.opentravel.schemacompiler.codegen.documentation.html.Content)
	 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addInheritedInfoAnchor(T parent, Content inheritedTree) {
		inheritedTree.addContent(writer
				.getMarkerAnchor("attributes_inherited_from_"
						+ parent.getQualifiedName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.documentation.html.writers.AbstractInfoWriter
	 * #getTableSummary()
	 */
	/**
	 * {@inheritDoc}TODO pass key to method.
	 */
	@Override
	protected String getInfoTableSummary() {
		Configuration config = writer.configuration();
		return config.getText("doclet.Attribute_Table_Summary",
				config.getText("doclet.Attribute_Summary"),
				config.getText("doclet.attributes"));
	}
	
	/**
	 * {@inheritDoc}TODO pass key to method.
	 */
	protected String getDetailInfoTableSummary() {
		Configuration config = writer.configuration();
		return config.getText("doclet.Attribute_Detail_Table_Summary",
				config.getText("doclet.Attribute_Detail_Summary"),
				config.getText("doclet.attributes"));
	}

}

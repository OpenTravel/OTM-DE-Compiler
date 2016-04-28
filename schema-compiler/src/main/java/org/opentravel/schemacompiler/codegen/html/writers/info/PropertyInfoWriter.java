/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers.info;

import java.util.List;

import org.opentravel.schemacompiler.codegen.html.builders.FacetDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.PropertyDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public class PropertyInfoWriter
		extends
		AbstractFieldInfoWriter<FacetDocumentationBuilder, PropertyDocumentationBuilder> {

	/**
	 * 
	 */
	public PropertyInfoWriter(SubWriterHolderWriter writer,
			FacetDocumentationBuilder owner) {
		super(writer, owner);
		title = writer.getResource("doclet.Property_Summary");
		caption = writer.configuration().getText("doclet.Properties");
	}

	@Override
	protected void addInfoSummary(Content memberTree) {
		Content label = getInfoLabel();
		memberTree.addContent(label);
		List<PropertyDocumentationBuilder> properties = source.getProperties();
		if (properties.size() > 0) {
			Content tableTree = getTableTree();
			for (PropertyDocumentationBuilder pdb : properties) {
				Content propSummary = getInfo(pdb, properties.indexOf(pdb), true);
				Content propDetail = getDetailedInfo(pdb);
				tableTree.addContent(propSummary);
				tableTree.addContent(propDetail);
			}
			memberTree.addContent(tableTree);
		}
	}
	
	/**
	 * Build the inherited member summary for the given methods.
	 *
	 * @param writer
	 *            the writer for this member summary.
	 * @param visibleMemberMap
	 *            the map for the members to document.
	 * @param summaryTreeList
	 *            list of content trees to which the documentation will be added
	 */
	@Override
	protected void addInheritedInfoSummary(Content summaryTree) {
		FacetDocumentationBuilder parentFacet = getParent(source);
		while (parentFacet != null) {
			if (parentFacet.getProperties().size() > 0) {
				addInheritedInfoHeader(parentFacet, summaryTree,
						"doclet.Properties_Inherited_From");
				addInheritedInfo(parentFacet.getProperties(), parentFacet, summaryTree);
			}
			parentFacet = getParent(parentFacet);
		}
	}

	@Override
	protected FacetDocumentationBuilder getParent(
			FacetDocumentationBuilder classDoc) {
		FacetDocumentationBuilder parent = (FacetDocumentationBuilder) classDoc.getSuperType();
		return parent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addInheritedInfoAnchor(FacetDocumentationBuilder parent,
			Content inheritedTree) {
		inheritedTree.addContent(writer
				.getMarkerAnchor("properties_inherited_from_object_"
						+ parent.getQualifiedName()));
	}

	/**
	 * {@inheritDoc}TODO pass key to method.
	 */
	@Override
	protected String getInfoTableSummary() {
		Configuration config = writer.configuration();
		return config.getText("doclet.Property_Table_Summary",
				config.getText("doclet.Property_Summary"),
				config.getText("doclet.properties"));
	}

	/**
	 * {@inheritDoc}TODO pass key to method.
	 */
	@Override
	protected String getDetailInfoTableSummary() {
		Configuration config = writer.configuration();
		return config.getText("doclet.Property_Detail_Table_Summary",
				config.getText("doclet.Property_Detail_Summary"),
				config.getText("doclet.properties"));
	}

}

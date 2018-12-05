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
		caption = writer.newConfiguration().getText("doclet.Properties");
	}

	@Override
	protected void addInfoSummary(Content memberTree) {
		Content label = getInfoLabel();
		memberTree.addContent(label);
		List<PropertyDocumentationBuilder> properties = source.getProperties();
		if (!properties.isEmpty()) {
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
			if (!parentFacet.getProperties().isEmpty()) {
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
		return (FacetDocumentationBuilder) classDoc.getSuperType();
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
	 * {@inheritDoc}
	 */
	@Override
	protected String getInfoTableSummary() {
		Configuration config = writer.newConfiguration();
		return config.getText("doclet.Property_Table_Summary",
				config.getText("doclet.Property_Summary"),
				config.getText("doclet.properties"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getDetailInfoTableSummary() {
		Configuration config = writer.newConfiguration();
		return config.getText("doclet.Property_Detail_Table_Summary",
				config.getText("doclet.Property_Detail_Summary"),
				config.getText("doclet.properties"));
	}

}

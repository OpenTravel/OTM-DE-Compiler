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
		caption = writer.newConfiguration().getText("doclet.Attributes");
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
		if (!attributes.isEmpty()) {
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
			if (!parent.getAttributes().isEmpty()) {
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
	 * {@inheritDoc}
	 */
	@Override
	protected String getInfoTableSummary() {
		Configuration config = writer.newConfiguration();
		return config.getText("doclet.Attribute_Table_Summary",
				config.getText("doclet.Attribute_Summary"),
				config.getText("doclet.attributes"));
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected String getDetailInfoTableSummary() {
		Configuration config = writer.newConfiguration();
		return config.getText("doclet.Attribute_Detail_Table_Summary",
				config.getText("doclet.Attribute_Detail_Summary"),
				config.getText("doclet.attributes"));
	}

}

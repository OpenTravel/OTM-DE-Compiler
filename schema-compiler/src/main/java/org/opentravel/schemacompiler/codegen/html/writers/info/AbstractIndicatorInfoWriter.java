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

import org.opentravel.schemacompiler.codegen.html.builders.AttributeOwnerDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.IndicatorDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public abstract class AbstractIndicatorInfoWriter<T extends AttributeOwnerDocumentationBuilder<?>> extends AbstractFieldInfoWriter<T, IndicatorDocumentationBuilder> {

	/**
	 * @param writer
	 * @param owner
	 */
	public AbstractIndicatorInfoWriter(SubWriterHolderWriter writer,
			T owner) {
		super(writer, owner);
		title = writer.getResource("doclet.Indicator_Summary");
		caption = writer.newConfiguration().getText("doclet.Indicators");
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
		List<IndicatorDocumentationBuilder> indicators = source.getIndicators();
		if (!indicators.isEmpty()) {
			Content tableTree = getTableTree();
			for (IndicatorDocumentationBuilder idb : indicators) {
				Content propSummary = getInfo(idb, indicators.indexOf(idb), false);
				tableTree.addContent(propSummary);
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
			if (!parent.getIndicators().isEmpty()) {
				addInheritedInfoHeader(parent, summaryTree,
						"doclet.Indicators_Inherited_From");
				addInheritedInfo(parent.getIndicators(), parent, summaryTree);
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
				.getMarkerAnchor("indicators_inherited_from_"
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
		Configuration config = writer.newConfiguration();
		return config.getText("doclet.Indicator_Table_Summary",
				config.getText("doclet.Indicator_Summary"),
				config.getText("doclet.indicators"));
	}
	
	protected String getDetailInfoTableSummary(){
		return "";
	}
	
	@Override
	protected Content getDetailedInfo(IndicatorDocumentationBuilder field) {
		return HtmlTree.EMPTY;
	}

}

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

import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.LibraryDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.LinkInfoImpl;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlStyle;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.markup.RawHtml;
import org.opentravel.schemacompiler.codegen.html.markup.StringContent;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public class LibraryInfoWriter extends AbstractInfoWriter<LibraryDocumentationBuilder> {

	private String tableSummaryKey;
	private String summaryKey;
	private String typeKey;
	
	private List<DocumentationBuilder> objects;

	public LibraryInfoWriter(SubWriterHolderWriter writer,
			LibraryDocumentationBuilder source) {
		super(writer, source);
		title = new StringContent("Library: " + source.getName());
		tableSummaryKey = "doclet.Object_Table_Summary";
	}

	/* (non-Javadoc)
	 * @see org.opentravel.schemacompiler.codegen.html.writers.info.AbstractInfoWriter#addInfo(org.opentravel.schemacompiler.codegen.documentation.html.Content)
	 */
	@Override
	public void addInfo(Content memberTree) {
		Content infoTree = getInfoTree(); // ul
		addBusinessObjects(infoTree);
		addCoreObjects(infoTree);
		addVWAs(infoTree);
		addEnums(infoTree);
		addServices(infoTree);
		addSimpleTypes(infoTree);
		memberTree.addContent(infoTree);
	}

	private void addBusinessObjects(Content infoTree) {
		objects = source.getBusinessObjects();
		summaryKey = "doclet.BusinessObject_Summary";
		typeKey = "doclet.BusinessObjects";
		caption = writer.configuration().getText("doclet.BusinessObjects");
		Content objectTree = getObjectsTree();
		infoTree.addContent(objectTree);
	}
	
	private void addCoreObjects(Content infoTree) {
		objects = source.getCoreObjects();
		summaryKey = "doclet.CoreObject_Summary";
		typeKey = "doclet.CoreObjects";
		caption = writer.configuration().getText("doclet.CoreObjects");
		Content objectTree = getObjectsTree();
		infoTree.addContent(objectTree);
	}
	
	private void addVWAs(Content infoTree) {
		objects = source.getVWAs();		
		summaryKey="doclet.VWA_Summary";
		typeKey="doclet.VWA";
		caption = writer.configuration().getText("doclet.VWA");
		Content objectTree = getObjectsTree();
		infoTree.addContent(objectTree);
	}
	
	private void addServices(Content infoTree) {
		objects = source.getServices();		
		summaryKey="doclet.Services_Summary";
		typeKey = "doclet.Services";
		caption = writer.configuration().getText("doclet.Services");
		Content objectTree = getObjectsTree();
		infoTree.addContent(objectTree);
	}
	
	private void addEnums(Content infoTree) {
		objects = source.getEnums();		
		summaryKey="doclet.Enumeration_Summary";
		typeKey="doclet.Enums";
		caption = writer.configuration().getText("doclet.Enums");
		Content objectTree = getObjectsTree();
		infoTree.addContent(objectTree);
	}
	
	private void addSimpleTypes(Content infoTree) {
		objects = source.getSimpleObjects();		
		summaryKey="doclet.SimpleType_Summary";
		typeKey="doclet.SimpleTypes";
		caption = writer.configuration().getText("doclet.SimpleTypes");
		Content objectTree = getObjectsTree();
		infoTree.addContent(objectTree);
	}

	private Content getObjectsTree() {
		Content content = getInfoTreeHeader(); // li
		addInfoSummary(content);
		return content;
	}

	/* (non-Javadoc)
	 * @see org.opentravel.schemacompiler.codegen.html.writers.info.AbstractInfoWriter#addInfoSummary(org.opentravel.schemacompiler.codegen.documentation.html.Content)
	 */
	@Override
	protected void addInfoSummary(Content memberTree) {
		if (objects.size() > 0) {
			Content tableTree = getTableTree();
			for (DocumentationBuilder object : objects) {
				if (!writer.configuration().isGeneratedDoc(object)) {
					continue;
				}
				Content facetSummary = getInfo(object, objects.indexOf(object));
				tableTree.addContent(facetSummary);
			}
			memberTree.addContent(tableTree);
		}
	}
	
	private Content getInfo(DocumentationBuilder object,
			int counter) {
		HtmlTree tdObjectName = new HtmlTree(HtmlTag.TD);
		tdObjectName.setStyle(HtmlStyle.colFirst);
		addObject(object, tdObjectName);
		HtmlTree tdSummary = new HtmlTree(HtmlTag.TD);
		setInfoColumnStyle(tdSummary);
		writer.addSummaryComment(object, tdSummary);
		HtmlTree tr = HtmlTree.TR(tdObjectName);
		tr.addContent(tdSummary);
		addRowStyle(tr, counter);
		return tr;	
	}

	private void addObject(DocumentationBuilder object, HtmlTree tree) {
		HtmlTree code = new HtmlTree(HtmlTag.CODE);
		code.addContent(new RawHtml(writer.getLink(new LinkInfoImpl(
				LinkInfoImpl.CONTEXT_LIBRARY, object))));
		Content strong = HtmlTree.STRONG(code);
		tree.addContent(strong);
	}

	protected String getInfoTableSummary() {
		Configuration config = writer.configuration();
		return config.getText(tableSummaryKey,
				config.getText(summaryKey),
				config.getText(typeKey));
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
		String[] header = new String[] { config.getText("doclet.Name"), config.getText("doclet.Description") };
		return header;
	}

}

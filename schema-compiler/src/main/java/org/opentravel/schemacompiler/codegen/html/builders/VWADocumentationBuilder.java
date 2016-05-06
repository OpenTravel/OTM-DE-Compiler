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
package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.VWAWriter;

/**
 * @author Eric.Bronson
 *
 */
public class VWADocumentationBuilder
		extends
		AttributeOwnerDocumentationBuilder<TLValueWithAttributes> {
		
	private TLDocumentation valueDoc;
	
	public VWADocumentationBuilder(TLValueWithAttributes t) {
		super(t);
		TLAttributeType parent = t.getParentType();
		superType = DocumentationBuilderFactory.getInstance().getDocumentationBuilder(parent);
		valueDoc = t.getValueDocumentation();
	}

	public TLDocumentation getValueDoc() {
		return valueDoc;
	}

	@Override
	public void build() throws Exception {
		VWAWriter writer = new VWAWriter(this, prev, next);
		Content contentTree = writer.getHeader();
		writer.addMemberInheritanceTree(contentTree);
		Content classContentTree = writer.getContentHeader();
		Content tree = writer.getMemberTree(classContentTree);
		Content classInfoTree = writer.getMemberInfoItemTree();
		writer.addDocumentationInfo(classInfoTree);
		tree.addContent(classInfoTree);
	
		classInfoTree = writer.getMemberInfoItemTree();
		writer.addExampleInfo(classInfoTree);
		tree.addContent(classInfoTree);

		classInfoTree = writer.getMemberInfoItemTree();
		writer.addAttributeInfo(classInfoTree);
		tree.addContent(classInfoTree);

		classInfoTree = writer.getMemberInfoItemTree();
		writer.addIndicatorInfo(classInfoTree);
		tree.addContent(classInfoTree);

		Content desc = writer.getMemberInfoTree(tree);
		classContentTree.addContent(desc);
		contentTree.addContent(classContentTree);
		writer.addFooter(contentTree);
		writer.printDocument(contentTree);
		writer.close();
	}

	@Override
	public DocumentationBuilderType getDocType() {
		return DocumentationBuilderType.VWA;
	}

}

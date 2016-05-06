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
import org.opentravel.schemacompiler.model.TLSimple;

import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.NamedEntityWriter;

/**
 * @author Eric.Bronson
 *
 */
public class SimpleDocumentationBuilder extends
		NamedEntityDocumentationBuilder<TLSimple> {

	/**
	 * @param manager
	 */
	public SimpleDocumentationBuilder(TLSimple t) {
		super(t);
		TLAttributeType parentType = t.getParentType();
		superType = DocumentationBuilderFactory.getInstance()
				.getDocumentationBuilder(parentType);
	}

	@Override
	public void build() throws Exception {
		NamedEntityWriter<SimpleDocumentationBuilder> writer = new NamedEntityWriter<SimpleDocumentationBuilder>(
				this, prev, next);
		Content contentTree = writer.getHeader();
		writer.addMemberInheritanceTree(contentTree);
		Content classContentTree = writer.getContentHeader();
		Content tree = writer.getMemberTree(classContentTree);

		Content classInfoTree = writer.getMemberInfoItemTree();
		writer.addDocumentationInfo(classInfoTree);
		tree.addContent(classInfoTree);

		classContentTree.addContent(tree);
		contentTree.addContent(classContentTree);
		writer.addFooter(contentTree);
		writer.printDocument(contentTree);
		writer.close();
	}

	@Override
	public DocumentationBuilderType getDocType() {
		return DocumentationBuilderType.SIMPLE;
	}
}

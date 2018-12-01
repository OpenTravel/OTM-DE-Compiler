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

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;

import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.EnumerationWriter;

/**
 * @author Eric.Bronson
 *
 */
public class EnumerationDocumentationBuilder extends
		NamedEntityDocumentationBuilder<TLAbstractEnumeration> {

	private boolean isOpen;

	List<EnumValueDocumentationBuilder> values = new ArrayList<>();

	/**
	 * @param manager
	 */
	public EnumerationDocumentationBuilder(TLAbstractEnumeration t) {
		super(t);
		if (t instanceof TLOpenEnumeration) {
			isOpen = true;
		}
		for (TLEnumValue value : t.getValues()) {
			values.add(new EnumValueDocumentationBuilder(value));
		}
	}

	public boolean isOpen() {
		return isOpen;
	}

	public List<EnumValueDocumentationBuilder> getValues() {
		return values;
	}

	@Override
	public DocumentationBuilderType getDocType() {
		return isOpen ? DocumentationBuilderType.OPEN_ENUM
				: DocumentationBuilderType.CLOSED_ENUM;
	}

	@Override
	public void build() throws Exception {
		EnumerationWriter writer = new EnumerationWriter(this, prev, next);
		Content contentTree = writer.getHeader();
		Content classContentTree = writer.getContentHeader();
		Content tree = writer.getMemberTree(classContentTree);
		
		Content classInfoTree = writer.getMemberInfoItemTree();
		writer.addDocumentationInfo(classInfoTree);
		tree.addContent(classInfoTree);
		
		classInfoTree = writer.getMemberInfoItemTree();
		writer.addValueInfo(classInfoTree);
		tree.addContent(classInfoTree);
		
		Content desc = writer.getMemberInfoTree(tree);
		classContentTree.addContent(desc);
		contentTree.addContent(classContentTree);
		writer.addFooter(contentTree);
		writer.printDocument(contentTree);
		writer.close();
	}

}

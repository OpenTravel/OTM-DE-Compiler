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
package org.opentravel.schemacompiler.codegen.html.writers;

import java.io.IOException;

import org.opentravel.schemacompiler.model.TLDocumentation;

import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.VWADocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.info.DocumentationInfoWriter;
import org.opentravel.schemacompiler.codegen.html.writers.info.ExampleInfoWriter;
import org.opentravel.schemacompiler.codegen.html.writers.info.InfoWriter;
import org.opentravel.schemacompiler.codegen.html.writers.info.VWAAttributeInfoWriter;
import org.opentravel.schemacompiler.codegen.html.writers.info.VWAIndicatorInfoWriter;

public class VWAWriter extends 
	NamedEntityWriter<VWADocumentationBuilder> implements
		FieldOwnerWriter {
	
	/**
	 * @param configuration
	 * @param filename
	 * @throws IOException
	 */
	public VWAWriter(VWADocumentationBuilder classDoc,
			DocumentationBuilder prev,
			DocumentationBuilder next) throws Exception {
		super(classDoc, prev, next);
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.FieldOwnerWriter
	 * #getAttributeTree
	 * (org.opentravel.schemacompiler.codegen.html.Content)
	 */
	@Override
	public void addAttributeInfo(Content memberTree) {
		if (!member.getAttributes().isEmpty()) {
			new VWAAttributeInfoWriter(this, member).addInfo(memberTree);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.FieldOwnerWriter
	 * #getIndicatorTree
	 * (org.opentravel.schemacompiler.codegen.html.Content)
	 */
	@Override
	public void addIndicatorInfo(Content memberTree) {
		if (!member.getIndicators().isEmpty()) {
			new VWAIndicatorInfoWriter(this, member).addInfo(memberTree);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.FieldOwnerWriter
	 * #getExampleTree(org.opentravel.schemacompiler.codegen.html.Content)
	 */
	@Override
	public void addExampleInfo(Content memberTree) {
		if ((member.getExampleJSON() != null && !"".equals(member.getExampleJSON()))
				|| (member.getExampleXML() != null && !"".equals(member.getExampleXML()))) {
			ExampleInfoWriter exampleWriter = new ExampleInfoWriter(this,
					member);
			exampleWriter.addInfo(memberTree);
		}
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.LibraryMemberWriter
	 * #
	 * addMemberDescription(org.opentravel.schemacompiler.codegen.html.Content
	 * )
	 */
	@Override
	public void addDocumentationInfo(Content classInfoTree) {
		super.addDocumentationInfo(classInfoTree);
		addValueDocumentation(classInfoTree);
	}


	private void addValueDocumentation(Content classInfoTree) {
		TLDocumentation doc = member.getValueDoc();
		if(doc != null){
			InfoWriter docWriter = new DocumentationInfoWriter(this, doc);
			docWriter.setTitle(getResource("doclet.Value_Documentation_Summary"));
			docWriter.addInfo(classInfoTree);
		}
	}


	@Override
	public void addPropertyInfo(Content memberTree) {
		// No action required
	}
	

}

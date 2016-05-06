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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.TestLibraryProvider;
import org.opentravel.schemacompiler.codegen.html.builders.EnumerationDocumentationBuilder;

public class EnumerationWriterTest extends WriterTest {
	private static EnumerationDocumentationBuilder builder;
	private static EnumerationWriter writer;
	private static TLAbstractEnumeration tlEnum;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		tlEnum = TestLibraryProvider.getClosedEnum("SampleEnum_Closed");
		tlEnum.setDocumentation(getTestDocumentation());
		builder = new EnumerationDocumentationBuilder(tlEnum);
		writer = new EnumerationWriter(builder, null, null);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		writer.close();
	}

	@Test
	public void testItShouldHaveTheValues() {
		Content content = writer.getContentHeader();
		writer.addValueInfo(content);
		String str = content.toString();
		for(TLEnumValue value : tlEnum.getValues()){
			assertTrue(str.contains(value.getLiteral()));
		}
	}
	
	@Test
	public void testAll() {
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
	}

}

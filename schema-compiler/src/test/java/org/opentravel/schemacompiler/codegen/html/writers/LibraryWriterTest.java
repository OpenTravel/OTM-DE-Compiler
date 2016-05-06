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
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.TestLibraryProvider;
import org.opentravel.schemacompiler.codegen.html.builders.LibraryDocumentationBuilder;

/**
 * @author Eric.Bronson
 *
 */
public class LibraryWriterTest extends WriterTest {

	private static LibraryWriter writer;
	private static TLLibrary library;


	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		WriterTest.setUpBeforeClass();
		library = TestLibraryProvider.getLibrary();
		LibraryDocumentationBuilder builder = new LibraryDocumentationBuilder(library);
		TLLibrary next = new TLLibrary();
		next.setNamespace("http://www/sample/com/next/v0");
		writer = new LibraryWriter(config, builder, null, new LibraryDocumentationBuilder(next));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		writer.close();
	}

	@Test
	public void testItShouldAddNameAndNamespace() {
		Content content = writer.getHeader();
		String str = content.toString();
		assertTrue("No name", str.contains(library.getName()));
		assertTrue("No namespace.", str.contains(library.getNamespace()));
		writer.addFooter(content);
		writer.printDocument(content);
	}

	@Test
	public void testItShouldAddAllObjects() {
		Content content = writer.getHeader();
		Content libraryTree = writer.getContentHeader();
		writer.addObjectsSummary(libraryTree);
		String str = libraryTree.toString();
		for (TLBusinessObject bo : library.getBusinessObjectTypes()) {
			assertTrue("No name", str.contains(bo.getName()));
		}
		for (TLCoreObject bo : library.getCoreObjectTypes()) {
			assertTrue("No name", str.contains(bo.getName()));
		}
		for (TLClosedEnumeration bo : library.getClosedEnumerationTypes()) {
			assertTrue("No name", str.contains(bo.getName()));
		}
		for (TLOpenEnumeration bo : library.getOpenEnumerationTypes()) {
			assertTrue("No name", str.contains(bo.getName()));
		}
		for (TLSimple bo : library.getSimpleTypes()) {
			assertTrue("No name", str.contains(bo.getName()));
		}		
		for (TLValueWithAttributes bo : library.getValueWithAttributesTypes()) {
			assertTrue("No name", str.contains(bo.getName()));
		}
		content.addContent(libraryTree);
		writer.addFooter(content);
		writer.printDocument(content);
	}

}

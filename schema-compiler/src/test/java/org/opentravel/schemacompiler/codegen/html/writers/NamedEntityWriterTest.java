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

import java.util.Collection;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder.DocumentationBuilderType;
import org.opentravel.schemacompiler.codegen.html.builders.NamedEntityDocumentationBuilder;
import org.opentravel.schemacompiler.event.ModelElementListener;

/**
 * @author Eric.Bronson
 *
 */
public class NamedEntityWriterTest extends AbstractWriterTest {

	NamedEntityWriter<NamedEntityDocumentationBuilder<?>> writer;

	TestEntity entity;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		final TLLibrary lib = new TLLibrary();
		lib.setName("TestLibrary");
		entity = new TestEntity();
		entity.setOwningLibrary(lib);
		writer = new NamedEntityWriter<NamedEntityDocumentationBuilder<?>>(
				new NamedEntityDocumentationBuilder<TestEntity>(entity) {

					@Override
					public void build() throws Exception {

					}

					@Override
					public DocumentationBuilderType getDocType() {
						return DocumentationBuilderType.FACET;
					}

				}, null, null) {

		};
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		writer.close();
	}

	@Test
	public void testItShouldAddTheNameToTheHeader() {
		Content contentTree = writer.getHeader();
		assertNotNull(contentTree);
		String content = contentTree.toString();
		assertTrue("Improper title.", content.contains(entity.getLocalName()));
		assertTrue("Improper title.", content.contains(DocumentationBuilderType.FACET.toString()));
		assertTrue("Improper namespace.", content.contains(entity.getNamespace()));
	}

	@Test
	public void testItShouldAddTheNameToTheContent() {
		Content classContentTree = writer.getContentHeader();
		assertNotNull(classContentTree);
		String content = classContentTree.toString();
		assertTrue("Improper content.", content.contains("contentContainer"));
	}
	
	private class TestEntity implements NamedEntity, TLDocumentationOwner{

		private AbstractLibrary library;
		
		@Override
		public AbstractLibrary getOwningLibrary() {
			return library;
		}

		public void setOwningLibrary(AbstractLibrary library) {
			this.library = library;
		}
		
		@Override
		public LibraryElement cloneElement() {
			return null;
		}

		@Override
		public LibraryElement cloneElement(AbstractLibrary namingContext) {
			return null;
		}

		@Override
		public TLModel getOwningModel() {
			return null;
		}

		@Override
		public String getValidationIdentity() {
			return null;
		}

		@Override
		public TLDocumentation getDocumentation() {
			return null;
		}

		@Override
		public void setDocumentation(TLDocumentation documentation) {
		}

		@Override
		public String getNamespace() {
			return "http://www.test.com/v0";
		}

		@Override
		public String getLocalName() {
			return "TestLocalName";
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ModelElement#addListener(org.opentravel.schemacompiler.event.ModelElementListener)
		 */
		@Override
		public void addListener(ModelElementListener listener) {
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ModelElement#removeListener(org.opentravel.schemacompiler.event.ModelElementListener)
		 */
		@Override
		public void removeListener(ModelElementListener listener) {
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ModelElement#getListeners()
		 */
		@Override
		public Collection<ModelElementListener> getListeners() {
			return Collections.emptyList();
		}
		
	}

}

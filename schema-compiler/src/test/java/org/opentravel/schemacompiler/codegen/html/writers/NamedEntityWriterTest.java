/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import static org.junit.Assert.*;

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

/**
 * @author Eric.Bronson
 *
 */
public class NamedEntityWriterTest extends WriterTest {

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

		@Override
		public AbstractLibrary getOwningLibrary() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public LibraryElement cloneElement() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public LibraryElement cloneElement(AbstractLibrary namingContext) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public TLModel getOwningModel() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getValidationIdentity() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public TLDocumentation getDocumentation() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setDocumentation(TLDocumentation documentation) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getNamespace() {
			return "http://www.test.com/v0";
		}

		@Override
		public String getLocalName() {
			return "TestLocalName";
		}
		
	}

}

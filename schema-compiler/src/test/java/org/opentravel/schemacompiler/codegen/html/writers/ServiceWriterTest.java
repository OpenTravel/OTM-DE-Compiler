/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.TestLibraryProvider;
import org.opentravel.schemacompiler.codegen.html.builders.ServiceDocumentationBuilder;

/**
 * @author Eric.Bronson
 *
 */
public class ServiceWriterTest extends WriterTest {


	private ServiceDocumentationBuilder builder;
	private ServiceWriter writer;
	private TLService service;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		service = TestLibraryProvider.getTestService();
		service.setDocumentation(getTestDocumentation());
		for(TLOperation op : service.getOperations()){
			op.setDocumentation(getTestDocumentation());
		}
		builder = new ServiceDocumentationBuilder(service);
		writer = new ServiceWriter(builder, null, null);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		writer.close();
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
		writer.addOperationInfo(classInfoTree);
		tree.addContent(classInfoTree);
		
		classContentTree.addContent(tree);
		contentTree.addContent(classContentTree);
		writer.addFooter(contentTree);
		writer.printDocument(contentTree);
	}
}

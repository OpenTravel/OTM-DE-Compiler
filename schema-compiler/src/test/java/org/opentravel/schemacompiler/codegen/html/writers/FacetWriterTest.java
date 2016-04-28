/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.TestLibraryProvider;
import org.opentravel.schemacompiler.codegen.html.builders.FacetDocumentationBuilder;

/**
 * @author Eric.Bronson
 *
 */
public class FacetWriterTest extends WriterTest{

	private static FacetDocumentationBuilder builder;
	private static FacetWriter writer;
	private static TLFacet facet;


	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		writer.close();
	}
	
	@Test
	public void testAll() throws Exception {
		TLBusinessObject bo = TestLibraryProvider.getBusinessObject();
		facet = bo.getDetailFacet();
		facet.setDocumentation(getTestDocumentation());
		builder = new FacetDocumentationBuilder(facet);
		writer = new FacetWriter(builder, null, null);
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
		writer.addPropertyInfo(classInfoTree);
		tree.addContent(classInfoTree);
		
		classInfoTree = writer.getMemberInfoItemTree();
		writer.addAttributeInfo(classInfoTree);
		tree.addContent(classInfoTree);
		
		classInfoTree = writer.getMemberInfoItemTree();
		writer.addIndicatorInfo(classInfoTree);
		tree.addContent(classInfoTree);
	
		Content desc = writer.getMemberInfoTree(tree);
		writer.addAliasInfo(desc);
		classContentTree.addContent(desc);
		contentTree.addContent(classContentTree);
		writer.addFooter(contentTree);
		writer.printDocument(contentTree);
	}
	
	@Test
	public void testExtendedAll() throws Exception {
		TLBusinessObject bo = TestLibraryProvider.getExtendedBusinessObject();
		TLBusinessObject ext = (TLBusinessObject) bo.getExtension().getExtendsEntity();
		facet = bo.getDetailFacet();
		facet.setDocumentation(getTestDocumentation());
		builder = new FacetDocumentationBuilder(facet);
		writer = new FacetWriter(builder, null, null);
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
		writer.addPropertyInfo(classInfoTree);
		tree.addContent(classInfoTree);
		
		classInfoTree = writer.getMemberInfoItemTree();
		writer.addAttributeInfo(classInfoTree);
		tree.addContent(classInfoTree);
		
		classInfoTree = writer.getMemberInfoItemTree();
		writer.addIndicatorInfo(classInfoTree);
		tree.addContent(classInfoTree);
	
		Content desc = writer.getMemberInfoTree(tree);
		writer.addAliasInfo(desc);
		classContentTree.addContent(desc);
		contentTree.addContent(classContentTree);
		writer.addFooter(contentTree);
		writer.printDocument(contentTree);
		String content = contentTree.toString();
		TLFacet idFacet = FacetCodegenUtils.getFacetOfType(ext, TLFacetType.ID);
		assertTrue(content.contains(idFacet.getLocalName()));
		for(TLAttribute att : idFacet.getAttributes()){
			assertTrue(content.contains(att.getName()));
		}
	}
}

/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.TestLibraryProvider;
import org.opentravel.schemacompiler.codegen.html.builders.BusinessObjectDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;

/**
 * @author Eric.Bronson
 *
 */
public class BusinessObjectWriterTest extends WriterTest {

	private BusinessObjectDocumentationBuilder builder;
	private BusinessObjectWriter writer;
	private TLBusinessObject bo;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		bo = TestLibraryProvider.getBusinessObject();
		builder = new BusinessObjectDocumentationBuilder(bo);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		if (writer != null) {
			writer.close();
		}
	}

	@Test
	public void testItShouldAddFacetsToTheContent() throws Exception {
		writer = new BusinessObjectWriter(builder, null, null);
		HtmlTree div = new HtmlTree(HtmlTag.DIV);
		writer.addFacetInfo(div);
		String content = div.toString();
		assertTrue("No facet.",
				content.contains(bo.getIdFacet().getLocalName()));
		assertTrue("No facet.",
				content.contains(bo.getDetailFacet().getLocalName()));
		assertTrue("No facet.",
				content.contains(bo.getSummaryFacet().getLocalName()));
		assertTrue("No facet type.",
				content.contains(bo.getIdFacet().getFacetType().name()));
		assertTrue("No facet.",
				content.contains(bo.getDetailFacet().getFacetType().name()));
		assertTrue("No facet.",
				content.contains(bo.getSummaryFacet().getFacetType().name()));
		assertTrue("Incorrect header", content.contains("Facets"));
	}

	@Test
	public void testItShouldAddInheritedFacetsToTheContent() throws Exception {
		TLBusinessObject extendedBO = TestLibraryProvider
				.getExtendedBusinessObject();
		BusinessObjectDocumentationBuilder builder = new BusinessObjectDocumentationBuilder(
				extendedBO);
		writer = new BusinessObjectWriter(builder, null, null);
		HtmlTree div = new HtmlTree(HtmlTag.DIV);
		writer.addFacetInfo(div);
		String content = div.toString();
		assertTrue("No facet.",
				content.contains(bo.getSummaryFacet().getLocalName()));
	}

	@Test
	public void testItShouldAddAliasesToTheContent() throws Exception {
		writer = new BusinessObjectWriter(builder, null, null);
		HtmlTree div = new HtmlTree(HtmlTag.DIV);
		writer.addAliasInfo(div);
		String content = div.toString();
		assertTrue("Incorrect header", content.contains("Aliases"));
		for (TLAlias alias : bo.getAliases()) {
			assertTrue("No alias.", content.contains(alias.getLocalName()));
		}
	}

	@Test
	public void testAll() throws Exception {
		writer = new BusinessObjectWriter(builder, null, null);
		Content contentTree = writer.getHeader();
		Content classContentTree = writer.getContentHeader();
		Content tree = writer.getMemberTree(classContentTree);
		Content classInfoTree = writer.getMemberInfoItemTree();
		writer.addDocumentationInfo(classInfoTree);
		tree.addContent(classInfoTree);

		classInfoTree = writer.getMemberInfoItemTree();
		writer.addFacetInfo(classInfoTree);
		tree.addContent(classInfoTree);

		classInfoTree = writer.getMemberInfoItemTree();
		writer.addAliasInfo(classInfoTree);
		tree.addContent(classInfoTree);
		classContentTree.addContent(tree);
		contentTree.addContent(classContentTree);
		writer.addFooter(contentTree);
		writer.printDocument(contentTree);
	}

	@Test
	public void testAllExtended() throws Exception {
		TLBusinessObject extendedBO = TestLibraryProvider
				.getExtendedBusinessObject();
		BusinessObjectDocumentationBuilder builder = new BusinessObjectDocumentationBuilder(
				extendedBO);
		writer = new BusinessObjectWriter(builder, null, null);
		Content contentTree = writer.getHeader();
		writer.addMemberInheritanceTree(contentTree);
		Content classContentTree = writer.getContentHeader();
		Content tree = writer.getMemberTree(classContentTree);

		Content classInfoTree = writer.getMemberInfoItemTree();
		writer.addDocumentationInfo(classInfoTree);
		tree.addContent(classInfoTree);

		classInfoTree = writer.getMemberInfoItemTree();
		writer.addFacetInfo(classInfoTree);
		tree.addContent(classInfoTree);

		classInfoTree = writer.getMemberInfoItemTree();
		writer.addAliasInfo(classInfoTree);
		tree.addContent(classInfoTree);

		Content desc = writer.getMemberInfoTree(tree);
		writer.addAliasInfo(desc);
		classContentTree.addContent(desc);
		contentTree.addContent(classContentTree);
		writer.addFooter(contentTree);
		writer.printDocument(contentTree);
	}

	@Test
	public void testItShouldAddDocumentation() throws Exception {
		TLDocumentation doc = bo.getDocumentation();
		Content classInfoTree = new HtmlTree(HtmlTag.DIV);
		writer = new BusinessObjectWriter(builder, null, null);
		writer.addDocumentationInfo(classInfoTree);
		String html = classInfoTree.toString();
		assertTrue("No Documentation header.",
				html.contains(config.getText("doclet.Documentation_Summary")));
		assertTrue("No Description.", html.contains(doc.getDescription()));
		assertTrue("No Implementers.",
				html.contains(doc.getImplementers().get(0).getText()));
		assertTrue("No Deprecations.",
				html.contains(doc.getDeprecations().get(0).getText()));
		assertTrue("No References.",
				html.contains(doc.getReferences().get(0).getText()));
		assertTrue("No More infos.",
				html.contains(doc.getMoreInfos().get(0).getText()));
	}

}

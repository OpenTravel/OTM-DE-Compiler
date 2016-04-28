/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.codegen.html.TestLibraryProvider;
import org.opentravel.schemacompiler.codegen.html.builders.BusinessObjectDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.writers.info.FacetInfoWriter;

/**
 * @author Eric.Bronson
 *
 */
public class FacetInfoWriterTest extends WriterTest{

	private static FacetInfoWriter writer;

	private static TLBusinessObject bo;
	
	private BusinessObjectWriter boWriter;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		WriterTest.setUpBeforeClass();
		bo = TestLibraryProvider.getBusinessObject();
	}

	@After
	public void tearDown(){
		if(boWriter != null){
			boWriter.close();
		}
	}

	@Test
	public void testItShouldAddFacetsToTheContent() throws Exception {
		BusinessObjectDocumentationBuilder builder = new BusinessObjectDocumentationBuilder(
				bo);
		boWriter = new BusinessObjectWriter(builder, null,
				null);
		writer = new FacetInfoWriter(boWriter, builder);
		HtmlTree div = new HtmlTree(HtmlTag.DIV);
		writer.addInfo(div);
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
		boWriter = new BusinessObjectWriter(builder, null,
				null);
		writer = new FacetInfoWriter(boWriter, builder);
		HtmlTree div = new HtmlTree(HtmlTag.DIV);
		writer.addInfo(div);
		String content = div.toString();
		assertTrue("No facet.",
				content.contains(bo.getSummaryFacet().getLocalName()));
	}
	
	@Test
	public void testItShouldNotAddInheritedFacetsToTheContent() throws Exception {
		TLBusinessObject extendedBO = TestLibraryProvider
				.getExtendedBusinessObject();
		BusinessObjectDocumentationBuilder builder = new BusinessObjectDocumentationBuilder(
				extendedBO);
		boWriter = new BusinessObjectWriter(builder, null,
				null);
		writer = new FacetInfoWriter(boWriter, builder);
		HtmlTree div = new HtmlTree(HtmlTag.DIV);
		writer.addInfo(div);
		String content = div.toString();
		assertFalse("Facet is not supposed to be there.",
				content.contains(bo.getIdFacet().getLocalName()));
	}

}

/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.codegen.html.TestLibraryProvider;
import org.opentravel.schemacompiler.codegen.html.builders.FacetDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTag;
import org.opentravel.schemacompiler.codegen.html.markup.HtmlTree;
import org.opentravel.schemacompiler.codegen.html.writers.info.PropertyInfoWriter;

/**
 * @author Eric.Bronson
 *
 */
public class PropertyInfoWriterTest extends WriterTest{

	private static FacetDocumentationBuilder builder;
	private static PropertyInfoWriter writer;
	private static TLFacet facet;
	private static FacetWriter facetWriter;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		WriterTest.setUpBeforeClass();
		TLBusinessObject bo = TestLibraryProvider.getBusinessObject();
		facet = bo.getDetailFacet();
		builder = new FacetDocumentationBuilder(facet);
		facetWriter = new FacetWriter(builder, null, null);
		writer = new PropertyInfoWriter(facetWriter, builder);
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		facetWriter.close();
		WriterTest.tearDownAfterClass();
	}

	@Test
	public void testItShouldAddPropertiesToTheContent() {
		HtmlTree div = new HtmlTree(HtmlTag.DIV);
		writer.addInfo(div);
		String content = div.toString();
		for(TLProperty prop : facet.getElements()){
			String name = prop.getName();
		assertTrue("No property " + name,
				content.contains(name));
		}
		assertTrue("Incorrect header", content.contains("Properties"));
	}
	
	@Test
	public void testItShouldAddInheritedFacetsToTheContent() throws Exception {
		HtmlTree div = new HtmlTree(HtmlTag.DIV);
		writer.addInfo(div);
		String content = div.toString();
		TLFacet id = FacetCodegenUtils.getFacetOfType(facet.getOwningEntity(), TLFacetType.SUMMARY);
		for(TLProperty prop : id.getElements()){
			String name = prop.getName();
		assertTrue("No property " + name,
				content.contains(name));
		}
	}

}

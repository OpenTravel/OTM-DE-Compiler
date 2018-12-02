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
public class PropertyInfoWriterTest extends AbstractWriterTest{

	private static FacetDocumentationBuilder builder;
	private static PropertyInfoWriter writer;
	private static TLFacet facet;
	private static FacetWriter facetWriter;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractWriterTest.setUpBeforeClass();
		TLBusinessObject bo = TestLibraryProvider.getBusinessObject("CompoundBusinessObject");
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
		AbstractWriterTest.tearDownAfterClass();
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

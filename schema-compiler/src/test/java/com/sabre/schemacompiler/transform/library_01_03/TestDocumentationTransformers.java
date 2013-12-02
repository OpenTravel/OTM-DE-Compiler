/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.library_01_03;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Simple;

import com.sabre.schemacompiler.ioc.SchemaCompilerApplicationContext;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.TransformerFactory;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Verifies the operation of the transformers that handle conversions to and from
 * <code>TLDocumentation</code> objects.
 *
 * @author S. Livezey
 */
public class TestDocumentationTransformers extends Abstract_1_3_TestTransformers {
	
	@Test
	public void testDocumentationTransformer() throws Exception {
		TLDocumentation doc = getSimpleType(PACKAGE_2_NAMESPACE, "library_2_p2", "SampleDocumentation").getDocumentation();
		
		assertNotNull(doc);
		assertEquals("SampleDocumentation-description-line_1\nSampleDocumentation-description-line_2", doc.getDescription());
		assertEquals(2, doc.getDeprecations().size());
		assertEquals("SampleDocumentation-deprecation-line_1", doc.getDeprecations().get(0).getText());
		assertEquals("SampleDocumentation-deprecation-line_2", doc.getDeprecations().get(1).getText());
		assertEquals(2, doc.getReferences().size());
		assertEquals("http://www.OpenTravel.org/ns/OTA2/SampleDocumentation/reference/1", doc.getReferences().get(0).getText());
		assertEquals("http://www.OpenTravel.org/ns/OTA2/SampleDocumentation/reference/2", doc.getReferences().get(1).getText());
		assertEquals(2, doc.getImplementers().size());
		assertEquals("SampleDocumentation-developer-line_1", doc.getImplementers().get(0).getText());
		assertEquals("SampleDocumentation-developer-line_2", doc.getImplementers().get(1).getText());
		assertEquals(2, doc.getMoreInfos().size());
		assertEquals("http://www.OpenTravel.org/ns/OTA2/SampleDocumentation/moreInfo/1", doc.getMoreInfos().get(0).getText());
		assertEquals("http://www.OpenTravel.org/ns/OTA2/SampleDocumentation/moreInfo/2", doc.getMoreInfos().get(1).getText());
		assertEquals(2, doc.getOtherDocs().size());
		assertEquals("use_1", doc.getOtherDocs().get(0).getContext());
		assertEquals("SampleDocumentation-otherDoc-line_1", doc.getOtherDocs().get(0).getText());
		assertEquals("use_2", doc.getOtherDocs().get(01).getContext());
		assertEquals("SampleDocumentation-otherDoc-line_2", doc.getOtherDocs().get(1).getText());
	}
	
	@Test
	public void testTLDocumentationTransformer() throws Exception {
		Documentation doc = transformSimpleType(PACKAGE_2_NAMESPACE, "library_2_p2", "SampleDocumentation").getDocumentation();
		
		assertNotNull(doc);
		assertEquals("SampleDocumentation-description-line_1\nSampleDocumentation-description-line_2", doc.getDescription().getValue());
		assertEquals(2, doc.getDeprecated().size());
		assertEquals("SampleDocumentation-deprecation-line_1", doc.getDeprecated().get(0).getValue());
		assertEquals("SampleDocumentation-deprecation-line_2", doc.getDeprecated().get(1).getValue());
		assertEquals(2, doc.getReference().size());
		assertEquals("http://www.OpenTravel.org/ns/OTA2/SampleDocumentation/reference/1", doc.getReference().get(0));
		assertEquals("http://www.OpenTravel.org/ns/OTA2/SampleDocumentation/reference/2", doc.getReference().get(1));
		assertEquals(2, doc.getImplementer().size());
		assertEquals(0, doc.getDeveloper().size());
		assertEquals("SampleDocumentation-developer-line_1", doc.getImplementer().get(0).getValue());
		assertEquals("SampleDocumentation-developer-line_2", doc.getImplementer().get(1).getValue());
		assertEquals(2, doc.getMoreInfo().size());
		assertEquals("http://www.OpenTravel.org/ns/OTA2/SampleDocumentation/moreInfo/1", doc.getMoreInfo().get(0));
		assertEquals("http://www.OpenTravel.org/ns/OTA2/SampleDocumentation/moreInfo/2", doc.getMoreInfo().get(1));
		assertEquals(2, doc.getOtherDoc().size());
		assertEquals("use_1", doc.getOtherDoc().get(0).getContext());
		assertEquals("SampleDocumentation-otherDoc-line_1", doc.getOtherDoc().get(0).getValue());
		assertEquals("use_2", doc.getOtherDoc().get(1).getContext());
		assertEquals("SampleDocumentation-otherDoc-line_2", doc.getOtherDoc().get(1).getValue());
	}
	
	private TLSimple getSimpleType(String namespace, String libraryName, String typeName) throws Exception {
		TLLibrary library = getLibrary(namespace, libraryName);
		
		return (library == null) ? null : library.getSimpleType(typeName);
	}
	
	private Simple transformSimpleType(String namespace, String libraryName, String typeName) throws Exception {
		TLSimple origType = getSimpleType(namespace, libraryName, typeName);
		TransformerFactory<SymbolResolverTransformerContext> factory =
				TransformerFactory.getInstance(SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
						getContextJAXBTransformation(origType.getOwningLibrary()));
		ObjectTransformer<TLSimple,Simple,SymbolResolverTransformerContext> transformer =
				factory.getTransformer(origType, Simple.class);
		
		return transformer.transform(origType);
	}
	
}

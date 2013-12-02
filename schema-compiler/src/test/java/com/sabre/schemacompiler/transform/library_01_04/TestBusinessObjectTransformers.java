/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.library_01_04;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentravel.ns.ota2.librarymodel_v01_04.BusinessObject;

import com.sabre.schemacompiler.ioc.SchemaCompilerApplicationContext;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.TransformerFactory;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Verifies the operation of the transformers that handle conversions to and from
 * <code>TLBusinessObject</code> objects.
 *
 * @author S. Livezey
 */
public class TestBusinessObjectTransformers extends Abstract_1_4_TestTransformers {
	
	@Test
	public void testBusinessObjectTransformerAllFacetsPopulated() throws Exception {
		TLBusinessObject type = getBusinessObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleBusinessObject");
		
		assertNotNull(type);
		assertNotNull(type.getAliases());
		assertEquals(2, type.getAliases().size());
		assertEquals("SampleBusinessObject_Alias1", type.getAliases().get(0).getName());
		assertEquals("SampleBusinessObject_Alias2", type.getAliases().get(1).getName());
		assertNotNull(type.getDocumentation());
		assertNotNull(type.getIdFacet());
		assertNotNull(type.getSummaryFacet());
		assertNotNull(type.getDetailFacet());
		assertFalse(type.getQueryFacets().isEmpty());
		assertEquals(2, type.getQueryFacets().size());
		assertFalse(type.getCustomFacets().isEmpty());
		assertEquals(2, type.getCustomFacets().size());
		assertTrue(type.isNotExtendable());
		assertNotNull(type.getExtension());
		assertNotNull(type.getExtension().getExtendsEntity());
		assertEquals("EmptyBusinessObject", type.getExtension().getExtendsEntity().getLocalName());
		assertEquals("EmptyBusinessObject", type.getExtension().getExtendsEntityName());
	}
	
	@Test
	public void testBusinessObjectTransformerNoFacets() throws Exception {
		TLBusinessObject type = getBusinessObject(PACKAGE_2_NAMESPACE, "library_1_p2", "EmptyBusinessObject");
		
		assertNotNull(type);
		assertNotNull(type.getAliases());
		assertEquals(0, type.getAliases().size());
		assertNull(type.getDocumentation());
		assertNotNull(type.getIdFacet());
		assertNotNull(type.getSummaryFacet());
		assertNotNull(type.getDetailFacet());
		assertTrue(type.getQueryFacets().isEmpty());
		assertNotNull(type.getCustomFacets());
		assertEquals(0, type.getCustomFacets().size());
		assertFalse(type.isNotExtendable());
		assertNull(type.getExtension());
	}
	
	@Test
	public void testTLBusinessObjectTransformerAllFacetsPopulated() throws Exception {
		BusinessObject type = transformBusinessObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleBusinessObject");
		
		assertNotNull(type);
		assertNotNull(type.getAliases());
		assertEquals(2, type.getAliases().size());
		assertEquals("SampleBusinessObject_Alias1", type.getAliases().get(0));
		assertEquals("SampleBusinessObject_Alias2", type.getAliases().get(1));
		assertNotNull(type.getDocumentation());
		assertNotNull(type.getID());
		assertNotNull(type.getSummary());
		assertNotNull(type.getDetail());
		assertEquals(2, type.getQuery().size());
		assertEquals(2, type.getCustom().size());
		assertTrue(type.isNotExtendable());
		assertNotNull(type.getExtension());
		assertEquals("EmptyBusinessObject", type.getExtension().getExtends());
	}
	
	@Test
	public void testTLBusinessObjectTransformerNoFacets() throws Exception {
		BusinessObject type = transformBusinessObject(PACKAGE_2_NAMESPACE, "library_1_p2", "EmptyBusinessObject");
		
		assertNotNull(type);
		assertNotNull(type.getAliases());
		assertEquals(0, type.getAliases().size());
		assertNull(type.getDocumentation());
		assertNotNull(type.getID());
		assertNotNull(type.getSummary());
		assertNotNull(type.getDetail());
		assertTrue(type.getQuery().isEmpty());
		assertTrue(type.getCustom().isEmpty());
		assertFalse(type.isNotExtendable());
		assertNull(type.getExtension());
	}
	
	private TLBusinessObject getBusinessObject(String namespace, String libraryName, String typeName) throws Exception {
		TLLibrary library = getLibrary(namespace, libraryName);
		
		return (library == null) ? null : library.getBusinessObjectType(typeName);
	}
	
	private BusinessObject transformBusinessObject(String namespace, String libraryName, String typeName) throws Exception {
		TLBusinessObject origType = getBusinessObject(namespace, libraryName, typeName);
		TransformerFactory<SymbolResolverTransformerContext> factory =
				TransformerFactory.getInstance(SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
						getContextJAXBTransformation(origType.getOwningLibrary()));
		ObjectTransformer<TLBusinessObject,BusinessObject,SymbolResolverTransformerContext> transformer =
				factory.getTransformer(origType, BusinessObject.class);
		
		return transformer.transform(origType);
	}
	
}

/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.legacy_xsd;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.Test;

import com.sabre.schemacompiler.model.XSDSimpleType;

/**
 * Verifies the operation of the transformers that handle conversions to
 * <code>XSDSimpleType</code> objects.
 *
 * @author S. Livezey
 */
public class TestXSDSimpleTypeTransformer extends AbstractXSDTestTransformers {
	
	@Test
	public void testXSDSimpleTypeTransformer() throws Exception {
 		XSDSimpleType member = (XSDSimpleType) getNamedEntity(PACKAGE_3_NAMESPACE, "SampleXSDSimpleType");
		
		assertNotNull(member);
		assertEquals(PACKAGE_3_NAMESPACE, member.getNamespace());
		assertEquals("SampleXSDSimpleType", member.getName());
		assertEquals("SampleXSDSimpleType", member.getLocalName());
		assertNotNull(member.getOwningLibrary());
		assertNotNull(member.getOwningModel());
		assertEquals("legacy_schema_2", member.getOwningLibrary().getName());
		assertNotNull(member.getJaxbType());
	}
	
}

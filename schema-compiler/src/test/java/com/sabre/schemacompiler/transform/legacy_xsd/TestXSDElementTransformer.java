/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.legacy_xsd;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.Test;

import com.sabre.schemacompiler.model.XSDElement;

/**
 * Verifies the operation of the transformers that handle conversions to
 * <code>XSDElement</code> objects.
 *
 * @author S. Livezey
 */
public class TestXSDElementTransformer extends AbstractXSDTestTransformers {
	
	@Test
	public void testXSDElementTransformer() throws Exception {
 		XSDElement member = (XSDElement) getNamedEntity(LEGACY_NAMESPACE, "SampleXSDElement");
		
		assertNotNull(member);
		assertEquals(LEGACY_NAMESPACE, member.getNamespace());
		assertEquals("SampleXSDElement", member.getName());
		assertEquals("SampleXSDElement", member.getLocalName());
		assertNotNull(member.getOwningLibrary());
		assertNotNull(member.getOwningModel());
		assertEquals("legacy_schema_3", member.getOwningLibrary().getName());
		assertNotNull(member.getJaxbElement());
	}
	
}

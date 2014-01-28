/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform.legacy_xsd;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.Test;
import org.opentravel.schemacompiler.model.XSDComplexType;

/**
 * Verifies the operation of the transformers that handle conversions to
 * <code>XSDComplexType</code> objects.
 *
 * @author S. Livezey
 */
public class TestXSDComplexTypeTransformer extends AbstractXSDTestTransformers {
	
	@Test
	public void testXSDComplexTypeTransformer() throws Exception {
 		XSDComplexType member = (XSDComplexType) getNamedEntity(LEGACY_NAMESPACE, "SampleXSDComplexType");
		
		assertNotNull(member);
		assertEquals(LEGACY_NAMESPACE, member.getNamespace());
		assertEquals("SampleXSDComplexType", member.getName());
		assertEquals("SampleXSDComplexType", member.getLocalName());
		assertNotNull(member.getOwningLibrary());
		assertNotNull(member.getOwningModel());
		assertEquals("legacy_schema_3", member.getOwningLibrary().getName());
		assertNotNull(member.getJaxbType());
	}
	
}

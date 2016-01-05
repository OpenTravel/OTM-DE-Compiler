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
package org.opentravel.schemacompiler.transform.legacy_xsd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opentravel.schemacompiler.model.XSDSimpleType;

/**
 * Verifies the operation of the transformers that handle conversions to <code>XSDSimpleType</code>
 * objects.
 * 
 * @author S. Livezey
 */
public class TestXSDSimpleTypeTransformer extends AbstractXSDTestTransformers {

    @Test
    public void testXSDSimpleTypeTransformer() throws Exception {
        XSDSimpleType member = (XSDSimpleType) getNamedEntity(PACKAGE_3_NAMESPACE,
                "SampleXSDSimpleType");

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

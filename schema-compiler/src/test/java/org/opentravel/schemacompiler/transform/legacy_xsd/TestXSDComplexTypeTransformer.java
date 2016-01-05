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
import org.opentravel.schemacompiler.model.XSDComplexType;

/**
 * Verifies the operation of the transformers that handle conversions to <code>XSDComplexType</code>
 * objects.
 * 
 * @author S. Livezey
 */
public class TestXSDComplexTypeTransformer extends AbstractXSDTestTransformers {

    @Test
    public void testXSDComplexTypeTransformer() throws Exception {
        XSDComplexType member = (XSDComplexType) getNamedEntity(LEGACY_NAMESPACE,
                "SampleXSDComplexType");

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

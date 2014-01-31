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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.XSDLibrary;

/**
 * Verifies the operation of the transformers that handle conversions to <code>XSDLibrary</code>
 * objects.
 * 
 * @author S. Livezey
 */
public class TestXSDLibraryTransformer extends AbstractXSDTestTransformers {

    @Test
    public void testXSDLibraryTransformer() throws Exception {
        XSDLibrary library = getLibrary(PACKAGE_3_NAMESPACE, "legacy_schema_1");

        assertNotNull(library);
        assertEquals(PACKAGE_3_NAMESPACE, library.getNamespace());
        assertEquals("legacy_schema_1", library.getName());

        assertEquals(1, library.getIncludes().size());
        assertEquals("legacy_schema_2.xsd", library.getIncludes().get(0).getPath());

        assertEquals(1, library.getNamespaceImports().size());
        assertEquals(LEGACY_NAMESPACE, library.getNamespaceImports().get(0).getNamespace());
        assertEquals("ns1", library.getNamespaceImports().get(0).getPrefix());
        assertEquals(1, library.getNamespaceImports().get(0).getFileHints().size());
        assertEquals("legacy_schema_3.xsd", library.getNamespaceImports().get(0).getFileHints()
                .get(0));
    }

    private XSDLibrary getLibrary(String namespace, String libraryName) throws Exception {
        XSDLibrary library = null;

        for (AbstractLibrary lib : testModel.getLibrariesForNamespace(namespace)) {
            if (lib.getName().equals(libraryName)) {
                library = (XSDLibrary) lib;
                break;
            }
        }
        return library;
    }

}

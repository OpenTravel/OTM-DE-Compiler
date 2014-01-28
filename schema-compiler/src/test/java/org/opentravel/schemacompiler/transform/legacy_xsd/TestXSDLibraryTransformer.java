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

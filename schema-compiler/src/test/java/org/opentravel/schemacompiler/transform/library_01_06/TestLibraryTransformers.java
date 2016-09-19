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
package org.opentravel.schemacompiler.transform.library_01_06;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import org.junit.Test;
import org.opentravel.ns.ota2.librarymodel_v01_06.Library;
import org.opentravel.ns.ota2.librarymodel_v01_06.NamespaceImport;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * Verifies the operation of the transformers that handle conversions to and from
 * <code>TLLibrary</code> objects.
 * 
 * @author S. Livezey
 */
public class TestLibraryTransformers extends Abstract_1_6_TestTransformers {

    @Test
    public void testLibraryTransformer() throws Exception {
        TLLibrary library = getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");

        assertNotNull(library);

        assertEquals(PACKAGE_2_NAMESPACE, library.getNamespace());
        assertEquals("pkg2", library.getPrefix());
        assertEquals("library_1_p2", library.getName());
        assertEquals("OTA2", library.getVersionScheme());
        assertEquals("2.0.0", library.getVersion());
        assertEquals("Test Library", library.getComments());

        assertEquals(3, library.getIncludes().size());
        assertEquals("file1.xsd", library.getIncludes().get(0).getPath());
        assertEquals("file2.xsd", library.getIncludes().get(1).getPath());
        assertEquals("library_2_p2.xml", library.getIncludes().get(2).getPath());

        Map<String, TLNamespaceImport> libraryImports = new HashMap<String, TLNamespaceImport>();
        TLNamespaceImport testImport = null;

        for (TLNamespaceImport nsImport : library.getNamespaceImports()) {
            libraryImports.put(nsImport.getPrefix(), nsImport);
        }
        assertEquals(4, libraryImports.size());

        assertNotNull(testImport = libraryImports.get("xsd"));
        assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, testImport.getNamespace());
        assertEquals(0, testImport.getFileHints().size());

        assertNotNull(testImport = libraryImports.get("ota"));
        assertEquals(SchemaDeclarations.OTM_COMMON_SCHEMA.getNamespace(),
                testImport.getNamespace());
        assertEquals(0, testImport.getFileHints().size());

        assertNotNull(testImport = libraryImports.get("pkg1"));
        assertEquals(PACKAGE_1_NAMESPACE, testImport.getNamespace());
        assertEquals(2, testImport.getFileHints().size());
        assertTrue(testImport.getFileHints().contains("../test-package_v1/library_1_p1.xml"));
        assertTrue(testImport.getFileHints().contains("../test-package_v1/library_2_p1.xml"));

        assertNotNull(testImport = libraryImports.get("ext"));
        assertEquals(PACKAGE_EXT_NAMESPACE, testImport.getNamespace());
        assertEquals(1, testImport.getFileHints().size());
        assertTrue(testImport.getFileHints().contains("library_3_ext.xml"));

        assertEquals(27, library.getNamedMembers().size());
        assertEquals(2, library.getSimpleTypes().size());
        assertEquals(0, library.getClosedEnumerationTypes().size());
        assertEquals(2, library.getOpenEnumerationTypes().size());
        assertEquals(2, library.getValueWithAttributesTypes().size());
        assertEquals(2, library.getChoiceObjectTypes().size());
        assertEquals(4, library.getCoreObjectTypes().size());
        assertEquals(3, library.getBusinessObjectTypes().size());
        assertEquals(8, library.getContextualFacetTypes().size());
        assertEquals(3, library.getResourceTypes().size());
        assertNotNull(library.getService());
    }

    @Test
    public void testTLLibraryTransformer() throws Exception {
        TLLibrary modelLibrary = getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
        TransformerFactory<SymbolResolverTransformerContext> factory = TransformerFactory
                .getInstance(SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
                        getContextJAXBTransformation(modelLibrary));
        ObjectTransformer<TLLibrary, Library, SymbolResolverTransformerContext> transformer = factory
                .getTransformer(modelLibrary, Library.class);

        Library jaxbLibrary = transformer.transform(modelLibrary);

        assertNotNull(jaxbLibrary);

        assertEquals(PACKAGE_2_NAMESPACE, jaxbLibrary.getNamespace());
        assertEquals("OTA2", jaxbLibrary.getVersionScheme());
        assertNull(jaxbLibrary.getPatchLevel());
        assertEquals("pkg2", jaxbLibrary.getPrefix());
        assertEquals("library_1_p2", jaxbLibrary.getName());
        assertEquals("Test Library", jaxbLibrary.getComments());

        assertEquals(3, jaxbLibrary.getIncludes().size());
        assertTrue(jaxbLibrary.getIncludes().contains("file1.xsd"));
        assertTrue(jaxbLibrary.getIncludes().contains("file2.xsd"));
        assertTrue(jaxbLibrary.getIncludes().contains("library_2_p2.xml"));

        Map<String, NamespaceImport> libraryImports = new HashMap<String, NamespaceImport>();
        NamespaceImport testImport = null;

        for (NamespaceImport nsImport : jaxbLibrary.getImport()) {
            libraryImports.put(nsImport.getPrefix(), nsImport);
        }
        assertEquals(4, libraryImports.size());

        assertNotNull(testImport = libraryImports.get("xsd"));
        assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, testImport.getNamespace());
        assertNull(testImport.getFileHints());

        assertNotNull(testImport = libraryImports.get("ota"));
        assertEquals(SchemaDeclarations.OTM_COMMON_SCHEMA.getNamespace(),
                testImport.getNamespace());
        assertNull(testImport.getFileHints());

        assertNotNull(testImport = libraryImports.get("pkg1"));
        assertEquals(PACKAGE_1_NAMESPACE, testImport.getNamespace());
        assertTrue(testImport.getFileHints().indexOf("../test-package_v1/library_1_p1.xml") >= 0);
        assertTrue(testImport.getFileHints().indexOf("../test-package_v1/library_2_p1.xml") >= 0);

        assertNotNull(testImport = libraryImports.get("ext"));
        assertEquals(PACKAGE_EXT_NAMESPACE, testImport.getNamespace());
        assertTrue(testImport.getFileHints().indexOf("library_3_ext.xml") >= 0);

        // Make sure the total number of terms matches the count from
        // the original model library
        int termAdjust = (modelLibrary.getService() == null) ? 0 : 1;

        assertEquals(modelLibrary.getNamedMembers().size() - termAdjust, jaxbLibrary.getTerms().size());
    }

    @Test
    public void testPatchLevelConversion() throws Exception {
        TransformerFactory<DefaultTransformerContext> transformerFactory = TransformerFactory
                .getInstance(SchemaCompilerApplicationContext.LOADER_TRANSFORMER_FACTORY,
                        new DefaultTransformerContext());
        ObjectTransformer<Library, TLLibrary, DefaultTransformerContext> transformer = transformerFactory
                .getTransformer(Library.class, TLLibrary.class);
        Library jaxbLibrary = new Library();

        jaxbLibrary.setVersionScheme(VersionSchemeFactory.getInstance().getDefaultVersionScheme());
        jaxbLibrary.setNamespace(PACKAGE_2_NAMESPACE);
        jaxbLibrary.setPatchLevel("5");

        TLLibrary library = transformer.transform(jaxbLibrary);
        assertEquals("http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2_0_5",
                library.getNamespace());
    }

}

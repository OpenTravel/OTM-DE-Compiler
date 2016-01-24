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
package org.opentravel.schemacompiler.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;

import org.junit.Test;
import org.opentravel.schemacompiler.loader.impl.CatalogLibraryNamespaceResolver;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Validates the functions of the <code>LibraryModelLoader</code>.
 * 
 * @author S. Livezey
 */
public class TestLibraryModelLoader {

    public static final String PACKAGE_1_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v1";
    public static final String PACKAGE_2_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2";
    public static final String PACKAGE_3_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v3";
    public static final String PACKAGE_EXT_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package-ext_v2";
    public static final String LEGACY_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/legacy_namespace";

    private static final String OTA2_COMMON_SCHEMA_URI = "http://www.opentravel.org/OTM/Common/v0";

    @Test
    public void testLoadLibraryNoDependencies() throws Exception {
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
        modelLoader.setNamespaceResolver(getNamespaceResolver());
        ValidationFindings findings = modelLoader.loadLibraryModel(new URI(PACKAGE_1_NAMESPACE));

        TLModel model = modelLoader.getLibraryModel();
        assertNotNull(model);
        SchemaCompilerTestUtils.printFindings(findings, FindingType.ERROR);
        assertFalse(findings.hasFinding(FindingType.ERROR));

        List<AbstractLibrary> libraryList = model.getAllLibraries();
        assertNotNull(libraryList);

        Set<String> libraryNames = new HashSet<String>();

        for (AbstractLibrary library : libraryList) {
            libraryNames.add(library.getName());
        }
        assertNotNull(libraryList);
        assertEquals(4, libraryList.size());
        assertEquals(3, model.getNamespaces().size());
        assertTrue(model.getNamespaces().contains(XMLConstants.W3C_XML_SCHEMA_NS_URI));
        assertTrue(model.getNamespaces().contains(OTA2_COMMON_SCHEMA_URI));
        assertTrue(model.getNamespaces().contains(PACKAGE_1_NAMESPACE));
        assertEquals(1, model.getLibrariesForNamespace(XMLConstants.W3C_XML_SCHEMA_NS_URI).size());
        assertEquals(1, model.getLibrariesForNamespace(OTA2_COMMON_SCHEMA_URI).size());
        assertEquals(2, model.getLibrariesForNamespace(PACKAGE_1_NAMESPACE).size());
        assertTrue(libraryNames.contains("XMLSchema"));
        assertTrue(libraryNames.contains("OTM_BuiltIns.xsd"));
        assertTrue(libraryNames.contains("library_1_p1"));
        assertTrue(libraryNames.contains("library_2_p1"));
    }

    @Test
    public void testLoadNamespaceWithDependencies() throws Exception {
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
        modelLoader.setNamespaceResolver(getNamespaceResolver());
        ValidationFindings findings = modelLoader.loadLibraryModel(new URI(PACKAGE_2_NAMESPACE));

        TLModel model = modelLoader.getLibraryModel();
        assertNotNull(model);
        SchemaCompilerTestUtils.printFindings(findings, FindingType.ERROR);
        assertFalse(findings.hasFinding(FindingType.ERROR));

        List<AbstractLibrary> libraryList = model.getAllLibraries();
        assertNotNull(libraryList);

        Set<String> libraryNames = new HashSet<String>();

        for (AbstractLibrary library : libraryList) {
            libraryNames.add(library.getName());
        }
        assertNotNull(libraryList);
        assertEquals(7, libraryList.size());
        assertEquals(7, libraryNames.size());
        assertEquals(5, model.getNamespaces().size());
        assertTrue(model.getNamespaces().contains(XMLConstants.W3C_XML_SCHEMA_NS_URI));
        assertTrue(model.getNamespaces().contains(OTA2_COMMON_SCHEMA_URI));
        assertTrue(model.getNamespaces().contains(PACKAGE_1_NAMESPACE));
        assertTrue(model.getNamespaces().contains(PACKAGE_2_NAMESPACE));
        assertTrue(model.getNamespaces().contains(PACKAGE_EXT_NAMESPACE));
        assertEquals(1, model.getLibrariesForNamespace(XMLConstants.W3C_XML_SCHEMA_NS_URI).size());
        assertEquals(1, model.getLibrariesForNamespace(OTA2_COMMON_SCHEMA_URI).size());
        assertEquals(2, model.getLibrariesForNamespace(PACKAGE_1_NAMESPACE).size());
        assertEquals(2, model.getLibrariesForNamespace(PACKAGE_2_NAMESPACE).size());
        assertEquals(1, model.getLibrariesForNamespace(PACKAGE_EXT_NAMESPACE).size());
        assertTrue(libraryNames.contains("XMLSchema"));
        assertTrue(libraryNames.contains("OTM_BuiltIns.xsd"));
        assertTrue(libraryNames.contains("library_1_p1"));
        assertTrue(libraryNames.contains("library_2_p1"));
        assertTrue(libraryNames.contains("library_1_p2"));
        assertTrue(libraryNames.contains("library_2_p2"));
        assertTrue(libraryNames.contains("library_3_ext"));
    }

    /**
     * Verifies the operation of the default namespace resolver (utilizing file hints from the
     * library itself).
     * 
     * @throws Exception
     */
    @Test
    public void testLoadLibraryWithDependenciesEmptyCatalog() throws Exception {
        LibraryNamespaceResolver namespaceResolver = new CatalogLibraryNamespaceResolver(new File(
                SchemaCompilerTestUtils.getBaseLibraryLocation() + "/empty-catalog.xml"));
        LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(new File(
                SchemaCompilerTestUtils.getBaseLibraryLocation()
                        + "/test-package_v2/library_1_p2.xml"));
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();

        modelLoader.setNamespaceResolver(namespaceResolver);
        ValidationFindings findings = modelLoader.loadLibraryModel(libraryInput);

        TLModel model = modelLoader.getLibraryModel();
        assertNotNull(model);
        SchemaCompilerTestUtils.printFindings(findings, FindingType.ERROR);
        assertFalse(findings.hasFinding(FindingType.ERROR));

        List<AbstractLibrary> libraryList = model.getAllLibraries();
        assertNotNull(libraryList);

        Set<String> libraryNames = new HashSet<String>();

        for (AbstractLibrary library : libraryList) {
            libraryNames.add(library.getName());
        }
        assertNotNull(libraryList);
        assertEquals(7, libraryList.size());
        assertEquals(7, libraryNames.size());
        assertEquals(5, model.getNamespaces().size());
        assertTrue(model.getNamespaces().contains(XMLConstants.W3C_XML_SCHEMA_NS_URI));
        assertTrue(model.getNamespaces().contains(OTA2_COMMON_SCHEMA_URI));
        assertTrue(model.getNamespaces().contains(PACKAGE_1_NAMESPACE));
        assertTrue(model.getNamespaces().contains(PACKAGE_2_NAMESPACE));
        assertTrue(model.getNamespaces().contains(PACKAGE_EXT_NAMESPACE));
        assertEquals(1, model.getLibrariesForNamespace(XMLConstants.W3C_XML_SCHEMA_NS_URI).size());
        assertEquals(1, model.getLibrariesForNamespace(OTA2_COMMON_SCHEMA_URI).size());
        assertEquals(2, model.getLibrariesForNamespace(PACKAGE_1_NAMESPACE).size());
        assertEquals(2, model.getLibrariesForNamespace(PACKAGE_2_NAMESPACE).size());
        assertEquals(1, model.getLibrariesForNamespace(PACKAGE_EXT_NAMESPACE).size());
        assertTrue(libraryNames.contains("XMLSchema"));
        assertTrue(libraryNames.contains("OTM_BuiltIns.xsd"));
        assertTrue(libraryNames.contains("library_1_p1"));
        assertTrue(libraryNames.contains("library_2_p1"));
        assertTrue(libraryNames.contains("library_1_p2"));
        assertTrue(libraryNames.contains("library_2_p2"));
        assertTrue(libraryNames.contains("library_3_ext"));
    }

    @Test
    public void testLoadLibraryWithIncludes() throws Exception {
        LibraryNamespaceResolver namespaceResolver = new CatalogLibraryNamespaceResolver(new File(
                SchemaCompilerTestUtils.getBaseLibraryLocation() + "/empty-catalog.xml"));
        LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(new File(
                SchemaCompilerTestUtils.getBaseLibraryLocation()
                        + "/test-package_v3/sample_library.xml"));
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();

        modelLoader.setNamespaceResolver(namespaceResolver);

        Set<String> libraryNames = new HashSet<String>();
        ValidationFindings findings = modelLoader.loadLibraryModel(libraryInput);
        TLModel model = modelLoader.getLibraryModel();

        SchemaCompilerTestUtils.printFindings(findings);
        assertNotNull(model);
        assertFalse(findings.hasFinding());

        for (AbstractLibrary library : model.getAllLibraries()) {
            libraryNames.add(library.getName());
        }

        assertEquals(9, libraryNames.size());
        assertEquals(4, model.getNamespaces().size());
        assertTrue(model.getNamespaces().contains(XMLConstants.W3C_XML_SCHEMA_NS_URI));
        assertTrue(model.getNamespaces().contains(OTA2_COMMON_SCHEMA_URI));
        assertTrue(model.getNamespaces().contains(PACKAGE_3_NAMESPACE));
        assertTrue(model.getNamespaces().contains(LEGACY_NAMESPACE));
        assertEquals(1, model.getLibrariesForNamespace(XMLConstants.W3C_XML_SCHEMA_NS_URI).size());
        assertEquals(1, model.getLibrariesForNamespace(OTA2_COMMON_SCHEMA_URI).size());
        assertEquals(4, model.getLibrariesForNamespace(PACKAGE_3_NAMESPACE).size());
        assertEquals(2, model.getLibrariesForNamespace(LEGACY_NAMESPACE).size());
        assertTrue(libraryNames.contains("XMLSchema"));
        assertTrue(libraryNames.contains("OTM_BuiltIns.xsd"));
        assertTrue(libraryNames.contains("sample_library"));
        assertTrue(libraryNames.contains("included_library"));
        assertTrue(libraryNames.contains("legacy_schema_1"));
        assertTrue(libraryNames.contains("legacy_schema_2"));
        assertTrue(libraryNames.contains("legacy_schema_3"));
        assertTrue(libraryNames.contains("legacy_schema_4"));
        assertTrue(libraryNames.contains("chameleon_schema")); // imported twice in two separate
                                                               // namespaces
    }

    @Test
    public void testLoadLegacySchema() throws Exception {
        LibraryNamespaceResolver namespaceResolver = new CatalogLibraryNamespaceResolver(new File(
                SchemaCompilerTestUtils.getBaseLibraryLocation() + "/empty-catalog.xml"));
        LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(new File(
                SchemaCompilerTestUtils.getBaseLibraryLocation()
                        + "/test-package_v3/legacy_schema_1.xsd"));
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();

        modelLoader.setNamespaceResolver(namespaceResolver);

        Set<String> libraryNames = new HashSet<String>();
        ValidationFindings findings = modelLoader.loadLibraryModel(libraryInput);
        TLModel model = modelLoader.getLibraryModel();

        assertNotNull(model);
        SchemaCompilerTestUtils.printFindings(findings);
        assertFalse(findings.hasFinding());

        for (AbstractLibrary library : model.getAllLibraries()) {
            libraryNames.add(library.getName());
        }

        assertEquals(6, libraryNames.size());
        assertEquals(4, model.getNamespaces().size());
        assertTrue(model.getNamespaces().contains(XMLConstants.W3C_XML_SCHEMA_NS_URI));
        assertTrue(model.getNamespaces().contains(OTA2_COMMON_SCHEMA_URI));
        assertTrue(model.getNamespaces().contains(PACKAGE_3_NAMESPACE));
        assertTrue(model.getNamespaces().contains(LEGACY_NAMESPACE));
        assertEquals(1, model.getLibrariesForNamespace(XMLConstants.W3C_XML_SCHEMA_NS_URI).size());
        assertEquals(1, model.getLibrariesForNamespace(OTA2_COMMON_SCHEMA_URI).size());
        assertEquals(2, model.getLibrariesForNamespace(PACKAGE_3_NAMESPACE).size());
        assertEquals(1, model.getLibrariesForNamespace(LEGACY_NAMESPACE).size());
        assertTrue(libraryNames.contains("XMLSchema"));
        assertTrue(libraryNames.contains("OTM_BuiltIns.xsd"));
        assertTrue(libraryNames.contains("legacy_schema_1"));
        assertTrue(libraryNames.contains("legacy_schema_2"));
        assertTrue(libraryNames.contains("legacy_schema_3"));
        assertTrue(libraryNames.contains("chameleon_schema")); // imported twice in two separate
                                                               // namespaces
    }

    // @Test
    public void testLoadLibrary_manualTest() throws Exception {
        File sourceFile = new File(System.getProperty("user.dir"),
                "../../../temp/schemas/test/Amtrak_Look.otm");
        LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(sourceFile);
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();

        ValidationFindings findings = modelLoader.loadLibraryModel(libraryInput);
        TLModel model = modelLoader.getLibraryModel();

        assertNotNull(model);
        assertFalse(findings.hasFinding(FindingType.ERROR));
    }

    protected LibraryNamespaceResolver getNamespaceResolver() throws Exception {
        return new CatalogLibraryNamespaceResolver(new File(
                SchemaCompilerTestUtils.getBaseLibraryLocation() + "/library-catalog.xml"));
    }

}

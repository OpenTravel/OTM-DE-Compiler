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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.loader.LibraryNamespaceResolver;
import org.opentravel.schemacompiler.loader.impl.CatalogLibraryNamespaceResolver;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * Validates the functions of the <code>CatalogLibraryNamespaceResolver</code>.
 * 
 * @author S. Livezey
 */
public class TestCatalogLibraryNamespaceResolver {

    public static final String CATALOG_FILE = "library-catalog-err.xml";

    @Test
    public void testNamespaceUrlLookup() throws Exception {
        File catalogFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation(), CATALOG_FILE);
        File catalogFolder = catalogFile.getParentFile();
        LibraryNamespaceResolver resolver = new CatalogLibraryNamespaceResolver(catalogFile);
        Collection<URL> namespace1Urls = resolver.resovleLibraryImport(new URI(
                "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v1"), null, null);
        Collection<URL> namespace2Urls = resolver.resovleLibraryImport(new URI(
                "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2"), null, null);

        assertTrue(namespace1Urls.contains(URLUtils.toURL(new File(catalogFolder,
                "/test-package_v1/library_1_p1.xml"))));
        assertTrue(namespace1Urls.contains(URLUtils.toURL(new File(catalogFolder,
                "/test-package_v1/library_2_p1.xml"))));

        assertTrue(namespace2Urls.contains(URLUtils.toURL(new File(catalogFolder,
                "/test-package_v2/library_1_p2.xml"))));
        assertTrue(namespace2Urls.contains(URLUtils.toURL(new File(catalogFolder,
                "/test-package_v2/library_2_p2.xml"))));
        assertTrue(namespace2Urls.contains(new URL("http://somehost/nowhere/library_3_p2.xml")));
    }

    @Test
    public void testLibraryUrlResolutionWithCatalogFile() throws Exception {
        File catalogFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation(), CATALOG_FILE);
        LibraryNamespaceResolver resolver = new CatalogLibraryNamespaceResolver(catalogFile);
        Collection<URL> namespaceUrls = resolver.resovleLibraryImport(new URI(
                "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v1"), null, null);

        assertFalse(namespaceUrls.isEmpty());
        URL libraryUrl = namespaceUrls.iterator().next();

        try {
            InputStream is = libraryUrl.openStream();
            is.close();

        } catch (IOException e) {
            Assert.fail("Unresolvable URL: " + libraryUrl.toExternalForm());
        }
    }

    @Test
    public void testLibraryUrlResolutionWithCatalogUrl() throws Exception {
        File catalogFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation(), CATALOG_FILE);
        LibraryNamespaceResolver resolver = new CatalogLibraryNamespaceResolver(
                URLUtils.toURL(catalogFile));
        Collection<URL> namespaceUrls = resolver.resovleLibraryImport(new URI(
                "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v1"), null, null);

        assertFalse(namespaceUrls.isEmpty());
        URL libraryUrl = namespaceUrls.iterator().next();

        try {
            InputStream is = libraryUrl.openStream();
            is.close();

        } catch (IOException e) {
            Assert.fail("Unresolvable URL: " + libraryUrl.toExternalForm());
        }
    }

}

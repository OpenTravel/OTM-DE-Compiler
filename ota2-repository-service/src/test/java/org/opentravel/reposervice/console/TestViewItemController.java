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

package org.opentravel.reposervice.console;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Verifies the functions of the <code>LoginController</code> class by invoking the web console using a headless
 * browser.
 */
public class TestViewItemController extends AbstractConsoleTest {

    @BeforeClass
    public static void setupTests() throws Exception {
        startSmtpTestServer( 1590 );
        setupWorkInProcessArea( TestViewItemController.class );
        startTestServer( "versions-repository", 9296, defaultRepositoryConfig, true, true,
            TestViewItemController.class );
    }

    @AfterClass
    public static void tearDownTests() throws Exception {
        shutdownTestServer();
    }

    @Test
    public void testReleaseViews() throws Exception {
        String releaseUrl = getLibraryUrl( "/console/releaseView.html",
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", "Version_Release_1_0_0.otr", "1.0.0" );
        String[] viewLinks = new String[] {"Assemblies"};

        try (WebClient client = newWebClient( true )) {
            HtmlPage page = client.getPage( releaseUrl );

            for (String linkName : viewLinks) {
                page = page.getAnchorByText( linkName ).click();
            }
        }
    }

    @Test
    public void testReleaseViews_notAuthorized() throws Exception {
        String[] urlPaths = new String[] {"/console/releaseView.html", "/console/releaseAssemblies.html"};

        try (WebClient client = newWebClient( false )) {
            for (String urlPath : urlPaths) {
                String releaseUrl =
                    getLibraryUrl( urlPath, "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test",
                        "Version_Release_1_0_0.otr", "1.0.0" );
                HtmlPage page = client.getPage( releaseUrl );

                assertTrue( page.asText().contains( ViewItemController.RELEASE_NOT_AUTHORIZED ) );
            }
        }
    }

    @Test
    public void testAssemblyViews() throws Exception {
        String releaseUrl = getLibraryUrl( "/console/assemblyView.html",
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", "Version_Assembly_1_0_0.osm", "1.0.0" );

        try (WebClient client = newWebClient( true )) {
            client.getPage( releaseUrl );
        }
    }

    @Test
    public void testAssemblyViews_notAuthorized() throws Exception {
        String[] urlPaths = new String[] {"/console/assemblyView.html"};

        try (WebClient client = newWebClient( false )) {
            for (String urlPath : urlPaths) {
                String releaseUrl =
                    getLibraryUrl( urlPath, "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test",
                        "Version_Assembly_1_0_0.osm", "1.0.0" );
                HtmlPage page = client.getPage( releaseUrl );

                assertTrue( page.asText().contains( ViewItemController.ASSEMBLY_NOT_AUTHORIZED ) );
            }
        }
    }

    @Test
    public void testLibraryViews() throws Exception {
        String libraryUrl = getLibraryUrl( "/console/libraryDictionary.html",
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", "Version_Test_1_0_0.otm", "1.0.0" );
        String[] viewLinks = new String[] {"Uses / Where-Used", "Errors & Warnings", "Releases", "Assemblies",
            "History", "General Info", "Raw Content"};

        try (WebClient client = newWebClient( true )) {
            HtmlPage page = client.getPage( libraryUrl );

            for (String linkName : viewLinks) {
                page = page.getAnchorByText( linkName ).click();
            }
        }
    }

    @Test
    public void testLibraryViews_notAuthorized() throws Exception {
        String[] urlPaths = new String[] {"/console/libraryDictionary.html", "/console/libraryUsage.html",
            "/console/libraryValidation.html", "/console/libraryReleases.html", "/console/libraryAssemblies.html",
            "/console/libraryHistory.html", "/console/libraryInfo.html", "/console/libraryRawContent.html"};

        try (WebClient client = newWebClient( false )) {
            for (String urlPath : urlPaths) {
                String libraryUrl =
                    getLibraryUrl( urlPath, "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test",
                        "Version_Test_1_0_0.otm", "1.0.0" );
                HtmlPage page = client.getPage( libraryUrl );

                assertTrue( page.asText().contains( ViewItemController.LIBRARY_NOT_AUTHORIZED ) );
            }
        }
    }

    @Test
    public void testLibraryViews_notFound() throws Exception {
        String[] urlPaths = new String[] {"/console/libraryDictionary.html", "/console/libraryUsage.html",
            "/console/libraryValidation.html", "/console/libraryReleases.html", "/console/libraryHistory.html",
            "/console/libraryInfo.html", "/console/libraryRawContent.html"};

        try (WebClient client = newWebClient( true )) {
            for (String urlPath : urlPaths) {
                String libraryUrl =
                    getLibraryUrl( urlPath, "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test/errorNS",
                        "Version_Test_1_0_0.otm", "1.0.0" );
                HtmlPage page = client.getPage( libraryUrl );

                assertTrue( page.asText().contains( ViewItemController.ERROR_DISPLAYING_LIBRARY2 ) );
            }
        }
    }

    @Test
    public void testEntityViews() throws Exception {
        String[] entityNames = new String[] {"SimpleType_01_00", "SimpleCore", "SimpleChoice", "SimpleBusinessObject",
            "SimpleService_Operation"};
        String[] viewLinks = new String[] {"Where-Used", "Errors & Warnings"};

        try (WebClient client = newWebClient( true )) {
            for (String entityName : entityNames) {
                String entityUrl = getEntityUrl( "/console/entityDictionary.html",
                    "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test/v01_00", entityName );
                HtmlPage page = client.getPage( entityUrl );

                assertFalse( page.asText().contains( ViewItemController.ENTITY_NOT_FOUND ) );

                for (String linkName : viewLinks) {
                    page = page.getAnchorByText( linkName ).click();
                    assertFalse( page.asText().contains( ViewItemController.ENTITY_NOT_FOUND ) );
                }
            }
        }
    }

    @Test
    public void testEntityViews_notAuthorized() throws Exception {
        String[] urlPaths = new String[] {"/console/entityDictionary.html", "/console/entityUsage.html",
            "/console/entityValidation.html"};

        try (WebClient client = newWebClient( false )) {
            for (String urlPath : urlPaths) {
                String entityUrl = getEntityUrl( urlPath,
                    "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test/v01_00", "SimpleBusinessObject" );
                HtmlPage page = client.getPage( entityUrl );

                assertTrue( page.asText().contains( ViewItemController.ENTITY_NOT_AUTHORIZED ) );
            }
        }
    }

    @Test
    public void testEntityViews_notFound() throws Exception {
        String[] urlPaths = new String[] {"/console/entityDictionary.html", "/console/entityUsage.html",
            "/console/entityValidation.html"};

        try (WebClient client = newWebClient( true )) {
            for (String urlPath : urlPaths) {
                String entityUrl = getEntityUrl( urlPath,
                    "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test/errorNS/v01_00",
                    "SimpleBusinessObject" );
                HtmlPage page = client.getPage( entityUrl );

                assertTrue( page.asText().contains( ViewItemController.ENTITY_NOT_FOUND ) );
            }
        }
    }

}

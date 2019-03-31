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

package org.opentravel.schemacompiler.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.net.URLEncoder;

/**
 * Verifies the functions of the <code>BrowseController</code> class by invoking the web console using a headless
 * browser.
 */
public abstract class TestBrowseController extends AbstractConsoleTest {

    @Test
    public void testBrowsePage() throws Exception {
        String[] linksToFollow =
            new String[] {"http://www.OpenTravel.org", "ns", "OTA2", "SchemaCompiler", "version-test", "all versions"};

        try (WebClient client = newWebClient( true )) {
            String pageUrl = jettyServer.get().getRepositoryUrl( "/console/browse.html" );
            HtmlPage page = client.getPage( pageUrl );

            assertEquals( "OTA2.0 Repository - Browse", page.getTitleText() );

            for (String linkName : linksToFollow) {
                HtmlAnchor link = page.getAnchorByText( linkName );

                assertNotNull( link );
                page = link.click();
            }
        }
    }

    @Test
    public void testNamespaceCreateDelete() throws Exception {
        try (WebClient client = newWebClient( true )) {
            String pageUrl = jettyServer.get().getRepositoryUrl( "/console/browse.html" );
            HtmlPage page = client.getPage( pageUrl );
            HtmlInput nsExtension;
            HtmlForm form;

            // Create a new namespace
            page = page.getAnchorByText( "http://www.OpenTravel.org" ).click();
            page = page.getAnchorByText( "Create a Namespace Extension" ).click();
            form = (HtmlForm) page.getElementById( "confirmForm" );
            nsExtension = form.getInputByName( "nsExtension" );
            nsExtension.setValueAttribute( "testNS" );
            page = form.getInputByValue( "Create" ).click();

            // Delete the namespace we just created
            page = page.getAnchorByText( "testNS" ).click();
            page = page.getAnchorByText( "Delete This Namespace" ).click();
            form = (HtmlForm) page.getElementById( "confirmForm" );
            page = form.getInputByValue( "Delete" ).click();

            try {
                page.getAnchorByText( "testNS" );
                fail( "Error - test namespace was not deleted." );

            } catch (ElementNotFoundException e) {
                // Expected exception - no error
            }
        }
    }

    @Test
    public void testViewLockedLibraries() throws Exception {
        String errorMessage = "You must login in order to view your locked libraries.";
        String lockedLibrariesUrl = jettyServer.get().getRepositoryUrl( "/console/lockedLibraries.html" );

        // View as an authenticated user
        try (WebClient client = newWebClient( true )) {
            HtmlPage page = client.getPage( lockedLibrariesUrl );

            assertFalse( page.asText().contains( errorMessage ) );
        }

        // View as an anonymous (unauthenticated user)
        try (WebClient client = newWebClient( false )) {
            HtmlPage page = client.getPage( lockedLibrariesUrl );

            assertTrue( page.asText().contains( errorMessage ) );
        }
    }

    @Test
    public void testNamespaceSubscriptions() throws Exception {
        String errorMessage = "You must login in order to view your subscriptions.";
        String subscriptionUrl = jettyServer.get().getRepositoryUrl( "/console/subscriptions.html" );

        // View/edit as an authenticated user
        try (WebClient client = newWebClient( true )) {
            String browseUrl = jettyServer.get().getRepositoryUrl( "/console/browse.html" );
            HtmlPage page = client.getPage( browseUrl );
            HtmlCheckBoxInput etLibraryPublish;
            HtmlForm form;

            page = page.getAnchorByText( "http://www.OpenTravel.org" ).click();
            page = page.getAnchorByText( "Subscribe" ).click();
            form = (HtmlForm) page.getElementById( "editUserForm" );
            etLibraryPublish = form.getInputByName( "etLibraryPublish" );
            etLibraryPublish.setChecked( true );
            page = form.getInputByValue( "Update Subscriptions" ).click();

            page = client.getPage( subscriptionUrl );
        }

        // View as an anonymous (unauthenticated user)
        try (WebClient client = newWebClient( false )) {
            HtmlPage page = client.getPage( subscriptionUrl );

            assertTrue( page.asText().contains( errorMessage ) );
        }
    }

    @Test
    public void testLibrarySubscriptions_singleVersion() throws Exception {
        String subscriptionUrl = getLibrarySubscriptionUrl(
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", "Version_Test", "1.0.0", false );

        // View/edit as an authenticated user
        try (WebClient client = newWebClient( true )) {
            HtmlPage page = client.getPage( subscriptionUrl );
            HtmlCheckBoxInput etLibraryPublish;
            HtmlForm form;

            form = (HtmlForm) page.getElementById( "editUserForm" );
            etLibraryPublish = form.getInputByName( "etLibraryPublish" );
            etLibraryPublish.setChecked( true );
            page = form.getInputByValue( "Update Subscriptions" ).click();

            page = client.getPage( jettyServer.get().getRepositoryUrl( "/console/subscriptions.html" ) );
        }
    }

    @Test
    public void testLibrarySubscriptions_allVersions() throws Exception {
        String subscriptionUrl = getLibrarySubscriptionUrl(
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", "Version_Test", "1.0.0", true );

        // View/edit as an authenticated user
        try (WebClient client = newWebClient( true )) {
            HtmlPage page = client.getPage( subscriptionUrl );
            HtmlCheckBoxInput etLibraryPublish;
            HtmlForm form;

            form = (HtmlForm) page.getElementById( "editUserForm" );
            etLibraryPublish = form.getInputByName( "etLibraryPublish" );
            etLibraryPublish.setChecked( true );
            page = form.getInputByValue( "Update Subscriptions" ).click();

            page = client.getPage( jettyServer.get().getRepositoryUrl( "/console/subscriptions.html" ) );
        }
    }

    private String getLibrarySubscriptionUrl(String baseNamespace, String libraryName, String version,
        boolean allVersions) throws Exception {
        String filename = libraryName + "_" + version.replace( '.', '_' ) + ".otm";

        return getLibraryUrl( "/console/librarySubscription.html", baseNamespace, filename, version ) + "&libraryName="
            + URLEncoder.encode( libraryName, "UTF-8" ) + "&allVersions=" + allVersions;
    }

}

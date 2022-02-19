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

import org.opentravel.reposervice.repository.RepositoryTestBase;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.net.URLEncoder;

/**
 * Base class for all tests that utilize a headless web browser to verifiy the functions of the repository web console.
 */
public abstract class AbstractConsoleTest extends RepositoryTestBase {

    protected WebClient newWebClient(boolean loginUser) throws Exception {
        WebClient client = new WebClient( BrowserVersion.CHROME );

        if (loginUser) {
            loginTestUser( client, "testuser", "password" );
        }
        return client;
    }

    protected static HtmlPage loginTestUser(WebClient client, String userid, String password) throws Exception {
        String pageUrl = jettyServer.get().getRepositoryUrl( "/console/index.html" );
        HtmlPage page = client.getPage( pageUrl );
        HtmlForm loginForm = (HtmlForm) page.getElementById( "headerlogin" );
        HtmlInput userIdInput = loginForm.getInputByName( "userid" );
        HtmlInput passwordInput = loginForm.getInputByName( "password" );
        HtmlInput submitButton = loginForm.getInputByValue( "Login" );

        userIdInput.setValueAttribute( userid );
        passwordInput.setValueAttribute( password );
        return submitButton.click();
    }

    protected String getLibraryUrl(String urlPath, String baseNamespace, String filename, String version)
        throws Exception {
        StringBuilder url = new StringBuilder( jettyServer.get().getRepositoryUrl( urlPath ) );

        url.append( "?baseNamespace=" ).append( URLEncoder.encode( baseNamespace, "UTF-8" ) );
        url.append( "&filename=" ).append( URLEncoder.encode( filename, "UTF-8" ) );
        url.append( "&version=" ).append( URLEncoder.encode( version, "UTF-8" ) );
        return url.toString();
    }

    protected String getEntityUrl(String urlPath, String namespace, String localName) throws Exception {
        StringBuilder url = new StringBuilder( jettyServer.get().getRepositoryUrl( urlPath ) );

        url.append( "?namespace=" ).append( URLEncoder.encode( namespace, "UTF-8" ) );
        url.append( "&localName=" ).append( URLEncoder.encode( localName, "UTF-8" ) );
        return url.toString();
    }

}

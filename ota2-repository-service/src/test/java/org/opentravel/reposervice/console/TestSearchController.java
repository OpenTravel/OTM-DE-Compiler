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

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

/**
 * Verifies the functions of the <code>SearchController</code> class by invoking the web console using a headless
 * browser.
 */
public class TestSearchController extends AbstractConsoleTest {

    @BeforeClass
    public static void setupTests() throws Exception {
        startSmtpTestServer( 1589 );
        setupWorkInProcessArea( TestSearchController.class );
        startTestServer( "versions-repository", 9297, defaultRepositoryConfig, true, true, TestSearchController.class );
    }

    @AfterClass
    public static void tearDownTests() throws Exception {
        shutdownTestServer();
    }

    @Test
    public void testKeywordLibrarySearch() throws Exception {
        try (WebClient client = newWebClient( true )) {
            String pageUrl = jettyServer.get().getRepositoryUrl( "/console/search.html" );
            HtmlPage page = client.getPage( pageUrl );
            HtmlForm form = (HtmlForm) page.getElementById( "searchForm" );
            HtmlInput keywords = form.getInputByName( "keywords" );

            keywords.setValueAttribute( "Version" );
            page = form.getInputByValue( "Search" ).click();
            assertTrue( page.asText().contains( "Version_Test" ) );
            assertTrue( page.asText().contains( "Version_Release" ) );
            assertTrue( page.asText().contains( "Version_Assembly" ) );
        }
    }

    @Test
    public void testKeywordEntitySearch() throws Exception {
        try (WebClient client = newWebClient( true )) {
            String pageUrl = jettyServer.get().getRepositoryUrl( "/console/search.html" );
            HtmlPage page = client.getPage( pageUrl );
            HtmlForm form = (HtmlForm) page.getElementById( "searchForm" );
            HtmlSelect entityType = (HtmlSelect) form.getSelectByName( "entityType" );
            HtmlOption boOption = entityType.getOptionByValue( TLBusinessObject.class.getSimpleName() );
            HtmlInput keywords = form.getInputByName( "keywords" );

            entityType.setSelectedAttribute( boOption, true );
            keywords.setValueAttribute( "SimpleBusinessObject" );
            page = form.getInputByValue( "Search" ).click();
            assertTrue( page.asText().contains( "SimpleBusinessObject" ) );
        }
    }

}

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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.repository.RepositoryTestBase;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Verifies the functions of the <code>BrowseController</code> class by invoking
 * the web console using a headless browser.
 */
public class TestBrowseController extends RepositoryTestBase {
	
    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea(TestBrowseController.class);
        startTestServer("versions-repository", 9294, true, TestBrowseController.class);
    }

    @AfterClass
    public static void tearDownTests() throws Exception {
        shutdownTestServer();
    }
    
    @Test
    public void testBrowsePage() throws Exception {
    	try (WebClient client = newWebClient()) {
    		String pageUrl = jettyServer.get().getRepositoryUrl( "/console/browse.html" );
    		HtmlPage page = client.getPage( pageUrl );
    		
    		assertEquals( "OTA2.0 Repository - Browse", page.getTitleText() );
    	}
    }
    
}

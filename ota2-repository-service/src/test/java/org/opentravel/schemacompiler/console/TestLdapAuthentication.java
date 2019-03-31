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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;

/**
 * Base class that defines LDAP authentication tests that should pass regardless of the server's configuration.
 */
public abstract class TestLdapAuthentication extends AbstractConsoleTest {

    @Test
    public void testUserLoginAndLogout() throws Exception {
        try (WebClient client = newWebClient( true )) {
            String pageUrl = jettyServer.get().getRepositoryUrl( "/console/browse.html" );
            HtmlPage page = client.getPage( pageUrl );
            HtmlForm form;

            page = page.getAnchorByText( "Logout" ).click();
            form = (HtmlForm) page.getElementById( "headerlogin" );
            form.getInputByValue( "Login" );
        }
    }

    @Test
    public void testFailedUserLogin() throws Exception {
        try (WebClient client = newWebClient( false )) {
            HtmlPage page = loginTestUser( client, "testuser", "bad-password" );

            assertTrue( page.asText().contains( "Invalid user ID or password credentials." ) );
        }
    }

    @Test
    public void testManageLdapUsers() throws Exception {
        try (WebClient client = newWebClient( true )) {
            String pageUrl = jettyServer.get().getRepositoryUrl( "/console/adminUsers.html" );
            HtmlPage page = client.getPage( pageUrl );

            // Display the users admin page and click the link to add a new account
            page = page.getAnchorByText( "Add a New User" ).click();
            HtmlForm form = (HtmlForm) page.getElementById( "addUserForm" );
            HtmlInput searchFilter = form.getInputByName( "searchFilter" );

            // Search for an LDAP user from the directory
            searchFilter.setValueAttribute( "Doe" );
            page = form.getInputByValue( "Search User Directory" ).click();

            // Select an LDAP user and create the new repository account
            form = (HtmlForm) page.getElementById( "addUserForm" );

            for (HtmlRadioButtonInput userRadio : form.getRadioButtonsByName( "userId" )) {
                if (userRadio.getValueAttribute().equals( "doejane" )) {
                    userRadio.click();
                }
            }
            page = form.getInputByValue( "Create User" ).click();

            // Delete the user account
            page =
                page.getAnchorByHref( "/ota2-repository-service/console/adminUsersDelete.html?userId=doejane" ).click();
            form = (HtmlForm) page.getElementById( "confirmForm" );
            page = form.getInputByValue( "Delete User Account" ).click();
            assertTrue( page.asText().contains( "User 'doejane' deleted successfully." ) );
        }
    }

}

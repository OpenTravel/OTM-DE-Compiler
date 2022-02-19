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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Verifies the functions of the <code>LoginController</code> class by invoking the web console using a headless
 * browser.
 */
public class TestLoginController extends AbstractConsoleTest {

    @BeforeClass
    public static void setupTests() throws Exception {
        startSmtpTestServer( 1588 );
        setupWorkInProcessArea( TestLoginController.class );
        startTestServer( "versions-repository", 9295, defaultRepositoryConfig, true, true, TestLoginController.class );
    }

    @AfterClass
    public static void tearDownTests() throws Exception {
        shutdownTestServer();
    }

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
    public void testChangePassword() throws Exception {
        String[][] passwordAttempts = new String[][] {new String[] {"badPassword", "password", "password"},
            new String[] {"password", "newpassword1", "newpassword2"},
            new String[] {"password", "invalid password", "invalid password"}, new String[] {"password", "", ""},
            new String[] {"", "password", "password"}, new String[] {"password", "password", "password"}};

        try (WebClient client = newWebClient( true )) {
            String pageUrl = jettyServer.get().getRepositoryUrl( "/console/changePassword.html" );
            HtmlPage page = client.getPage( pageUrl );

            for (String[] attempt : passwordAttempts) {
                HtmlForm form = (HtmlForm) page.getElementById( "changePasswordForm" );
                HtmlInput oldPassword = form.getInputByName( "oldPassword" );
                HtmlInput newPassword = form.getInputByName( "newPassword" );
                HtmlInput newPasswordConfirm = form.getInputByName( "newPasswordConfirm" );
                HtmlInput submitButton = form.getInputByValue( "Change Password" );

                oldPassword.setValueAttribute( attempt[0] );
                newPassword.setValueAttribute( attempt[1] );
                newPasswordConfirm.setValueAttribute( attempt[2] );
                page = submitButton.click();
            }
            assertNull( page.getElementById( "changePasswordForm" ) );
        }
    }

    @Test
    public void testEditUserProfile() throws Exception {
        String[] emailAttempts = new String[] {"bad#email", "John.Doe@opentravel.org"};

        try (WebClient client = newWebClient( true )) {
            String pageUrl = jettyServer.get().getRepositoryUrl( "/console/editUserProfile.html" );
            HtmlPage page = client.getPage( pageUrl );

            for (String emailAttempt : emailAttempts) {
                HtmlForm form = (HtmlForm) page.getElementById( "editUserForm" );
                HtmlInput lastName = form.getInputByName( "lastName" );
                HtmlInput firstName = form.getInputByName( "firstName" );
                HtmlInput emailAddress = form.getInputByName( "emailAddress" );
                HtmlInput submitButton = form.getInputByValue( "Update Profile" );

                lastName.setValueAttribute( "Doe" );
                firstName.setValueAttribute( "John" );
                emailAddress.setValueAttribute( emailAttempt );
                page = submitButton.click();
            }
            assertTrue( page.asText().contains( "John Doe" ) );
        }
    }

}

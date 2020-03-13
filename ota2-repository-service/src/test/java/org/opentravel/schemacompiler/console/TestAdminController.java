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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.repository.RepositoryItem;

import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

import java.io.File;

/**
 * Verifies the functions of the <code>AdminController</code> class by invoking the web console using a headless
 * browser.
 */
public class TestAdminController extends AbstractConsoleTest {

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestAdminController.class );
        startTestServer( "versions-repository", 9297, jmsIndexRepositoryConfig, false, true,
            TestAdminController.class );
    }

    @AfterClass
    public static void tearDownTests() throws Exception {
        shutdownTestServer();
    }

    @Test
    public void testAdminHome() throws Exception {
        try (WebClient client = newWebClient( true )) {
            client.getPage( jettyServer.get().getRepositoryUrl( "/console/adminHome.html" ) );
        }
    }

    @Test
    public void testRenameRepository() throws Exception {
        try (WebClient client = newWebClient( true )) {
            String pageUrl = jettyServer.get().getRepositoryUrl( "/console/adminChangeRepositoryName.html" );
            HtmlPage page = client.getPage( pageUrl );
            HtmlForm form = (HtmlForm) page.getElementById( "renameForm" );
            HtmlInput displayName = form.getInputByName( "displayName" );

            displayName.setValueAttribute( "Updated Repository Name" );
            page = form.getInputByValue( "Update" ).click();
            assertTrue( page.asText().contains( "updated successfully" ) );
        }
    }

    @Test
    public void testChangeRepositoryImage() throws Exception {
        File customLogo = new File( System.getProperty( "user.dir" ), "/src/test/resources/test_logo.png" );
        File tempLogo = new File( System.getProperty( "java.io.tmpdir" ), "/test_logo.png" );
        File managedLogo =
            new File( repositoryManager.get().getRepositoryLocation(), "../test-repository/custom_logo.png" );

        try (WebClient client = newWebClient( true )) {
            String pageUrl = jettyServer.get().getRepositoryUrl( "/console/adminChangeRepositoryImage.html" );
            HtmlPage page = client.getPage( pageUrl );
            HtmlForm form = (HtmlForm) page.getElementById( "changeImageForm" );
            HtmlFileInput fileInput = (HtmlFileInput) form.getInputByName( "bannerImageFile" );
            HtmlElement fakeSubmitButton = (HtmlElement) page.createElement( "button" );

            // Start a change and then cancel...
            fileInput.setValueAttribute( customLogo.getAbsolutePath() );
            assertTrue( tempLogo.exists() );
            page = fakeSubmitButton.click();
            form = (HtmlForm) page.getElementById( "changeImageForm" );

            fileInput.setValueAttribute( customLogo.getAbsolutePath() );
            assertTrue( tempLogo.exists() );
            page = fakeSubmitButton.click();
            form = (HtmlForm) page.getElementById( "changeImageForm" );

            page = form.getInputByValue( "Cancel" ).click();
            assertTrue( page.asXml().contains( "/ota_logo.png" ) );
            assertTrue( !managedLogo.exists() );

            // Select a custom banner image for the repository...
            page = client.getPage( pageUrl );
            form = (HtmlForm) page.getElementById( "changeImageForm" );
            fileInput = (HtmlFileInput) form.getInputByName( "bannerImageFile" );
            fakeSubmitButton = (HtmlElement) page.createElement( "button" );
            fakeSubmitButton.setAttribute( "type", "submit" );
            form.appendChild( fakeSubmitButton );

            fileInput.setValueAttribute( customLogo.getAbsolutePath() );
            assertTrue( tempLogo.exists() );
            page = fakeSubmitButton.click();
            form = (HtmlForm) page.getElementById( "changeImageForm" );

            page = form.getInputByValue( "Save Changes" ).click();
            assertTrue( page.asXml().contains( "/service/customLogo" ) );
            assertTrue( managedLogo.exists() );

            // Revert back to the default banner image...
            page = client.getPage( pageUrl );
            form = (HtmlForm) page.getElementById( "changeImageForm" );
            form.getInputByValue( "DEFAULT" ).click();
            page = form.getInputByValue( "Save Changes" ).click();
            assertTrue( page.asXml().contains( "/ota_logo.png" ) );
            assertFalse( managedLogo.exists() );
        }
    }

    @Test
    public void testManageRootNamespaces() throws Exception {
        try (WebClient client = newWebClient( true )) {
            String pageUrl = jettyServer.get().getRepositoryUrl( "/console/adminManageRootNamespaces.html" );
            HtmlPage page = client.getPage( pageUrl );
            HtmlForm form = (HtmlForm) page.getElementById( "rootNamespaceForm" );
            HtmlInput newRootNamespace = form.getInputByName( "newRootNamespace" );

            // Attempt to create a duplicate root namespace
            newRootNamespace.setValueAttribute( "http://www.OpenTravel.org" );
            page = form.getInputByValue( "Create" ).click();
            assertTrue( page.asText()
                .contains( "The root namespace cannot be created because it conflicts with an existing one." ) );

            // Create a new root namespace
            form = (HtmlForm) page.getElementById( "rootNamespaceForm" );
            newRootNamespace = form.getInputByName( "newRootNamespace" );
            newRootNamespace.setValueAttribute( "http://test.rootns.org" );
            page = form.getInputByValue( "Create" ).click();

            // Delete the root namespace we just created
            client.setAlertHandler( new CollectingAlertHandler() );
            page = page.getAnchorByHref( "#" ).click();
            assertFalse( page.asText().contains( "http://test.rootns.org" ) );
        }
    }

    @Test
    public void testManageNamespacePermissions() throws Exception {
        try (WebClient client = newWebClient( true )) {
            String pageUrl = jettyServer.get().getRepositoryUrl( "/console/adminPermissions.html" );
            HtmlPage page = client.getPage( pageUrl );
            HtmlForm form = (HtmlForm) page.getElementById( "selectForm" );
            HtmlSelect select = form.getSelectByName( "namespace" );
            HtmlOption option =
                select.getOptionByValue( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test" );

            // Select and display permissions for a child namespace
            select.setSelectedAttribute( option, true );
            page = form.getInputByValue( "Show Permissions" ).click();
            assertTrue( page.asText().contains( "No permissions defined for this namespace." ) );

            // Navigate to the edit page the selected namespace permissions
            form = (HtmlForm) page.getElementById( "permissionsForm" );
            page = form.getInputByValue( "Edit" ).click();

            // Update the permissions for the selected namespace
            form = (HtmlForm) page.getElementById( "editForm" );
            select = form.getSelectByName( "permissions[2].grantPermission" );
            select.setSelectedAttribute( select.getOptionByValue( "WRITE" ), true );
            page = form.getInputByValue( "Save Permissions" ).click();
            assertTrue( page.asText().contains( "Permissions updated successfully." ) );
        }
    }

    @Test
    public void testManageLocalUsers() throws Exception {
        String[][] createAttempts =
            new String[][] {new String[] {"testuser", "Doe", "John", "john.doe@opentravel.org", "password", "password"},
                new String[] {"doej", "", "John", "john.doe@opentravel.org", "password", "password"},
                new String[] {"doej", "Doe", "John", "", "password", "password"},
                new String[] {"doej", "Doe", "John", "john.doe", "password", "password"},
                new String[] {"doej", "Doe", "John", "john.doe@opentravel.org", "", ""},
                new String[] {"doej", "Doe", "John", "john.doe@opentravel.org", "bad password", "bad password"},
                new String[] {"doej", "Doe", "John", "john.doe@opentravel.org", "password", "mismatch"},
                new String[] {"doej", "Doe", "John", "john.doe@opentravel.org", "password", "password"}};
        String[][] editAttempts =
            new String[][] {new String[] {"", "John", "john.doe@opentravel.org"}, new String[] {"Doe", "John", ""},
                new String[] {"Doe", "John", "john.doe"}, new String[] {"Doe", "John", "john.doe@opentravel.org"},};
        String[][] passwordAttempts =
            new String[][] {new String[] {"", ""}, new String[] {"bad password", "bad password"},
                new String[] {"newpassword", "mismatch"}, new String[] {"newpassword", "newpassword"},};

        try (WebClient client = newWebClient( true )) {
            String pageUrl = jettyServer.get().getRepositoryUrl( "/console/adminUsers.html" );
            HtmlPage page = client.getPage( pageUrl );

            // Display the users admin page and click the link to add a new account
            page = page.getAnchorByText( "Add a New User" ).click();

            for (int i = 0; i < createAttempts.length; i++) {
                HtmlForm form = (HtmlForm) page.getElementById( "addUserForm" );
                HtmlInput userId = form.getInputByName( "userId" );
                HtmlInput lastName = form.getInputByName( "lastName" );
                HtmlInput firstName = form.getInputByName( "firstName" );
                HtmlInput emailAddress = form.getInputByName( "emailAddress" );
                HtmlInput password = form.getInputByName( "password" );
                HtmlInput passwordConfirm = form.getInputByName( "passwordConfirm" );

                userId.setValueAttribute( createAttempts[i][0] );
                lastName.setValueAttribute( createAttempts[i][1] );
                firstName.setValueAttribute( createAttempts[i][2] );
                emailAddress.setValueAttribute( createAttempts[i][3] );
                password.setValueAttribute( createAttempts[i][4] );
                passwordConfirm.setValueAttribute( createAttempts[i][5] );
                page = form.getInputByValue( "Create User Account" ).click();

                if ((i + 1) < createAttempts.length) {
                    // All but the last attempt should fail
                    assertTrue( page.asXml().contains( "<div id=\"errorMessage\">" ) );
                }
            }

            // Edit the user account we just created
            page =
                page.getAnchorByHref( "/ota2-repository-service/console/adminUsersEditLocal.html?userId=doej" ).click();

            for (int i = 0; i < editAttempts.length; i++) {
                HtmlForm form = (HtmlForm) page.getElementById( "editUserForm" );
                HtmlInput lastName = form.getInputByName( "lastName" );
                HtmlInput firstName = form.getInputByName( "firstName" );
                HtmlInput emailAddress = form.getInputByName( "emailAddress" );

                lastName.setValueAttribute( editAttempts[i][0] );
                firstName.setValueAttribute( editAttempts[i][1] );
                emailAddress.setValueAttribute( editAttempts[i][2] );
                page = form.getInputByValue( "Update User Profile" ).click();

                if ((i + 1) < editAttempts.length) {
                    // All but the last attempt should fail
                    assertTrue( page.asXml().contains( "<div id=\"errorMessage\">" ) );
                }
            }

            // Change the password for the user account
            page = page.getAnchorByHref( "/ota2-repository-service/console/adminUsersChangePassword.html?userId=doej" )
                .click();

            for (int i = 0; i < passwordAttempts.length; i++) {
                HtmlForm form = (HtmlForm) page.getElementById( "changePasswordForm" );
                HtmlInput password = form.getInputByName( "newPassword" );
                HtmlInput passwordConfirm = form.getInputByName( "newPasswordConfirm" );

                password.setValueAttribute( passwordAttempts[i][0] );
                passwordConfirm.setValueAttribute( passwordAttempts[i][1] );
                page = form.getInputByValue( "Change Password" ).click();

                if ((i + 1) < passwordAttempts.length) {
                    // All but the last attempt should fail
                    assertTrue( page.asXml().contains( "<div id=\"errorMessage\">" ) );
                }
            }

            // Delete the user account
            page = page.getAnchorByHref( "/ota2-repository-service/console/adminUsersDelete.html?userId=doej" ).click();
            HtmlForm form = (HtmlForm) page.getElementById( "confirmForm" );
            page = form.getInputByValue( "Delete User Account" ).click();
            assertTrue( page.asText().contains( "User 'doej' deleted successfully." ) );
        }
    }

    @Test
    public void testManageGroups() throws Exception {
        String[] createAttempts = new String[] {"SchemaAuthors", "Test Group", "", "TestGroup"};

        try (WebClient client = newWebClient( true )) {
            String pageUrl = jettyServer.get().getRepositoryUrl( "/console/adminGroups.html" );
            HtmlPage page = client.getPage( pageUrl );

            // Navigate to the add-group page
            page = page.getAnchorByText( "Add a New Group" ).click();

            for (int i = 0; i < createAttempts.length; i++) {
                HtmlForm form = (HtmlForm) page.getElementById( "confirmForm" );
                HtmlInput groupName = form.getInputByName( "groupName" );

                groupName.setValueAttribute( createAttempts[i] );
                page = form.getInputByValue( "Create Group" ).click();

                if ((i + 1) < createAttempts.length) {
                    // All but the last attempt should fail
                    assertTrue( page.asXml().contains( "<div id=\"errorMessage\">" ) );
                }
            }

            // Edit the group members
            page = page.getAnchorByHref( "/ota2-repository-service/console/adminGroupsEdit.html?groupName=TestGroup" )
                .click();
            HtmlForm form = (HtmlForm) page.getElementById( "editForm" );
            HtmlSelect select = (HtmlSelect) page.getElementById( "groupEditKnownUsers" );

            select.setSelectedAttribute( select.getOptionByValue( "testuser" ), true );
            form.getInputByValue( "Add Member ->" ).click();
            page = form.getInputByValue( "Save Group Membership" ).click();

            // Delete the group
            page = page.getAnchorByHref( "/ota2-repository-service/console/adminGroupsDelete.html?groupName=TestGroup" )
                .click();
            form = (HtmlForm) page.getElementById( "confirmForm" );
            page = form.getInputByValue( "Delete Group" ).click();
            assertTrue( page.asText().contains( "Group 'TestGroup' deleted successfully." ) );
        }
    }

    @Test
    public void testManageLibraries() throws Exception {
        RepositoryItem item = repositoryManager.get().getRepositoryItem(
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test", "Version_Test_1_1_1.otm", "1.1.1" );

        // Lock the library so we can unlock it through the web console
        repositoryManager.get().lock( item );

        try (WebClient client = newWebClient( true )) {
            String pageUrl = getLibraryUrl( "/console/libraryInfo.html", item.getBaseNamespace(), item.getFilename(),
                item.getVersion() );
            HtmlPage page = client.getPage( pageUrl );
            HtmlForm form;

            // Unlock the library
            page = page.getAnchorByText( "Unlock this item" ).click();
            form = (HtmlForm) page.getElementById( "confirmForm" );
            page = form.getInputByValue( "Unlock Item" ).click();

            // Promote the library to Under Review
            page = page.getAnchorByText( "Promote to Under Review" ).click();
            form = (HtmlForm) page.getElementById( "confirmForm" );
            page = form.getInputByValue( "Promote Item" ).click();

            // Recalculate the library's CRC
            page = page.getAnchorByText( "Recalculate CRC" ).click();
            form = (HtmlForm) page.getElementById( "confirmForm" );
            page = form.getInputByValue( "Recalculate Item CRC" ).click();

            // Demote the library to Draft
            page = page.getAnchorByText( "Demote to Draft" ).click();
            form = (HtmlForm) page.getElementById( "confirmForm" );
            page = form.getInputByValue( "Demote Item" ).click();

            // Delete the library
            page = page.getAnchorByText( "Delete this Item" ).click();
            form = (HtmlForm) page.getElementById( "confirmForm" );
            page = form.getInputByValue( "Delete Item" ).click();
            assertTrue( page.asText().contains( "Repository item deleted successfully" ) );
        }
    }

    @Test
    public void testIndexRepository() throws Exception {
        try (WebClient client = newWebClient( true )) {
            String pageUrl = jettyServer.get().getRepositoryUrl( "/console/adminSearchIndex.html" );
            HtmlPage page = client.getPage( pageUrl );
            HtmlForm form = (HtmlForm) page.getElementById( "confirmForm" );

            form.getInputByValue( "Continue with Search Indexing" ).click();
        }
    }

}

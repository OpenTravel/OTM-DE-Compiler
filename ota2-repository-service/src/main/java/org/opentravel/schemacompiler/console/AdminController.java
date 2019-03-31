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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.ns.ota2.security_v01_00.NamespaceAuthorizations;
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.index.FreeTextSearchService;
import org.opentravel.schemacompiler.index.FreeTextSearchServiceFactory;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RepositoryUtils;
import org.opentravel.schemacompiler.security.AuthenticationProvider;
import org.opentravel.schemacompiler.security.AuthorizationResource;
import org.opentravel.schemacompiler.security.GroupAssignmentsResource;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserGroup;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.opentravel.schemacompiler.security.impl.SecurityFileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

/**
 * Controller that handles interactions with the admin page of the OTA2.0 repository console.
 * 
 * @author S. Livezey
 */
@Controller
public class AdminController extends BaseController {

    private static final String BASE_NAMESPACE = "baseNamespace";
    private static final String FILENAME = "filename";
    private static final String VERSION = "version";
    private static final String EMAIL_ADDRESS = "emailAddress";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String USER_ID = "userId";
    private static final String RI_DOES_NOT_EXIST = "The requested repository item does not exist.";
    private static final String REDIRECT_LIBRARY_INFO = "redirect:/console/libraryInfo.html";
    private static final String REDIRECT_ADMIN_USERS = "redirect:/console/adminUsers.html";
    private static final String REDIRECT_ADMIN_GROUPS = "redirect:/console/adminGroups.html";

    protected static Pattern emailPattern = Pattern.compile(
        "^([\\!#\\$%&'\\*\\+/\\=?\\^`\\{\\|\\}~a-zA-Z0-9_-]+[\\.]?)+[\\!#\\$%&'\\*\\+/\\=?\\^`\\{\\|\\}~a-zA-Z0-9_-]+"
            + "@{1}((([0-9A-Za-z_-]+)([\\.]{1}[0-9A-Za-z_-]+)*\\.{1}"
            + "([A-Za-z]){1,6})|(([0-9]{1,3}[\\.]{1}){3}([0-9]{1,3}){1}))$" );

    private static Log log = LogFactory.getLog( AdminController.class );

    private FreeTextSearchService searchService;

    /**
     * Default constructor.
     */
    public AdminController() {
        try {
            FreeTextSearchServiceFactory.initializeSingleton( getRepositoryManager() );
            searchService = FreeTextSearchServiceFactory.getInstance();

        } catch (Exception e) {
            log.error( "Error initializing the free-text search service.", e );
        }
    }

    /**
     * Called by the Spring MVC controller to display the application administration page.
     * 
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminHome.html", "/adminHome.htm"})
    public String adminHomePage(HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );

        if (homeRedirect != null) {
            return homeRedirect;
        }
        return applyCommonValues( session, model, "adminHome" );
    }

    /**
     * Called by the Spring MVC controller to display and edit the display name of the repository.
     * 
     * @param displayName the updated display name for the repository
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminChangeRepositoryName.html", "/adminChangeRepositoryName.htm"})
    public String adminChangeRepositoryName(@RequestParam(value = "displayName", required = false) String displayName,
        HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        RepositoryManager repositoryManager = getRepositoryManager();
        String targetPage = null;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        if ((displayName != null) && (displayName.length() > 0)) {
            String repositoryId = repositoryManager.getId();

            try {
                repositoryManager.updateLocalRepositoryIdentity( repositoryId, displayName );
                setStatusMessage( "Repository display name updated successfully.", redirectAttrs );
                targetPage = "redirect:/console/adminHome.html";

            } catch (RepositoryException e) {
                setErrorMessage( "Error updating the repository's display name (see server log for details).", model );
                log.error( "Error updating the repository's display name.", e );
            }
        } else {
            displayName = repositoryManager.getDisplayName();
        }

        if (targetPage == null) {
            model.addAttribute( "displayName", displayName );
            targetPage = applyCommonValues( session, model, "adminChangeRepositoryName" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display and edit the root namespaces of the repository.
     * 
     * @param rootNamespace the name of the new root namespace to create or delete
     * @param formAction indicates whether the root namespace is to be created or deleted (null for first-time display)
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminManageRootNamespaces.html", "/adminManageRootNamespaces.htm"})
    public String adminManageRootNamespaces(
        @RequestParam(value = "rootNamespace", required = false) String rootNamespace,
        @RequestParam(value = "action", required = false) String formAction, HttpSession session, Model model,
        RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        String targetPage = null;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        try {
            if ((formAction == null) || (formAction.length() == 0)) {
                // No action required for first-time display of the page

            } else if (formAction.equals( "create" )) {
                getRepositoryManager().createRootNamespace( rootNamespace );
                setStatusMessage( "Root namespace created successfully.", redirectAttrs );
                targetPage = "redirect:/console/adminManageRootNamespaces.html";

            } else if (formAction.equals( "delete" )) {
                getRepositoryManager().deleteRootNamespace( rootNamespace );
                setStatusMessage( "Root namespace deleted successfully.", redirectAttrs );
                targetPage = "redirect:/console/adminManageRootNamespaces.html";

            } else {
                throw new RepositoryException( "Unrecognized form action: " + formAction );
            }

        } catch (Exception e) {
            log.error( "Error managing root namespaces.", e );
            setErrorMessage( e.getMessage(), model );
            model.addAttribute( "newRootNamespace", rootNamespace );
        }

        if (targetPage == null) {
            buildRootNamespaceFormData( model );
        }
        return targetPage;
    }

    /**
     * Builds the form data needed to render the root namespace admin page.
     * 
     * @param model the model context to be used when rendering the page view
     */
    private void buildRootNamespaceFormData(Model model) {
        List<NamespaceItem> rootNamespaceItems = new ArrayList<>();

        try {
            for (String rootNS : getRepositoryManager().listRootNamespaces()) {
                NamespaceItem nsItem = new NamespaceItem( rootNS );

                nsItem.setCanDelete( getRepositoryManager().listNamespaceChildren( rootNS ).isEmpty() );
                rootNamespaceItems.add( nsItem );
            }

        } catch (RepositoryException e) {
            log.error( "Error retrieving root namespaces.", e );
        }
        model.addAttribute( "rootNamespaceItems", rootNamespaceItems );
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to manage namespace
     * permissions.
     * 
     * @param baseNamespace the base namespace for which permissions should be displayed
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminPermissions.html", "/adminPermissions.htm"})
    public String adminPermissionsPage(@RequestParam(value = "namespace", required = false) String baseNamespace,
        HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );

        if (homeRedirect != null) {
            return homeRedirect;
        }

        try {
            if ((baseNamespace != null) && (baseNamespace.length() == 0)) {
                baseNamespace = null;
            }
            SecurityFileUtils fileUtils = new SecurityFileUtils( getRepositoryManager() );
            File authorizationFile = fileUtils.getAuthorizationFile( baseNamespace );
            NamespaceAuthorizations jaxbAuthorizations = null;

            if (authorizationFile.exists()) {
                jaxbAuthorizations = fileUtils.loadNamespaceAuthorizations( authorizationFile );
            }
            model.addAttribute( "permissions", new NamespacePermissions( baseNamespace, jaxbAuthorizations ) );
            model.addAttribute( "baseNamespaces", getRepositoryManager().listBaseNamespaces() );

        } catch (Exception e) {
            log.error( "Error displaying security permissions.", e );
            setErrorMessage( "Error displaying security permissions (see server log for details).", model );

        } finally {
            model.addAttribute( BASE_NAMESPACE, baseNamespace );
        }
        return applyCommonValues( session, model, "adminPermissions" );
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to edit namespace
     * permissions.
     * 
     * @param baseNamespace the base namespace for which permissions should be edited
     * @param processForm flag indicating whether updates have been submitted by the user (if false, page is being
     *        displayed for the first time)
     * @param permissions the list of update permissions that were submitted by the user
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminPermissionsEdit.html", "/adminPermissionsEdit.htm"})
    public String adminPermissionsEditPage(@RequestParam(value = "namespace", required = true) String baseNamespace,
        @RequestParam(value = "processForm", required = false) boolean processForm,
        @ModelAttribute("permissions") NamespacePermissions permissions, HttpSession session, Model model,
        RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        List<String> permissionOptions = new ArrayList<>();
        String targetPage = null;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        try {
            if ((baseNamespace != null) && (baseNamespace.length() == 0)) {
                baseNamespace = null;
            }

            if (processForm) {
                SecurityFileUtils fileUtils = new SecurityFileUtils( getRepositoryManager() );
                AuthorizationResource authResource = new AuthorizationResource( fileUtils, baseNamespace );

                authResource.saveNamespaceAuthorizations( permissions.toJaxbAuthorizations() );
                redirectAttrs.addAttribute( "namespace", baseNamespace );
                setStatusMessage( "Permissions updated successfully.", redirectAttrs );
                targetPage = "redirect:/console/adminPermissions.html";

            } else { // displaying the page for the first time
                SecurityFileUtils fileUtils = new SecurityFileUtils( getRepositoryManager() );
                File authorizationFile = fileUtils.getAuthorizationFile( baseNamespace );
                NamespaceAuthorizations jaxbAuthorizations = null;

                if (authorizationFile.exists()) {
                    jaxbAuthorizations = fileUtils.loadNamespaceAuthorizations( authorizationFile );
                }
                permissions = new NamespacePermissions( baseNamespace, jaxbAuthorizations );
                permissions.createGroupPermissions(
                    Arrays.asList( new GroupAssignmentsResource( getRepositoryManager() ).getGroupNames() ) );
            }

        } catch (Exception e) {
            log.error( "Error displaying or updating security permissions.", e );
            setErrorMessage( "Error displaying or updating security permissions (see server log for details).", model );
        }

        if (targetPage == null) {
            for (RepositoryPermission permissionOption : RepositoryPermission.values()) {
                permissionOptions.add( permissionOption.toString() );
            }
            model.addAttribute( BASE_NAMESPACE, baseNamespace );
            model.addAttribute( "permissions", permissions );
            model.addAttribute( "permissionOptions", permissionOptions );
            targetPage = applyCommonValues( session, model, "adminPermissionsEdit" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to manage local user
     * accounts.
     * 
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminUsers.html", "/adminUsers.htm"})
    public String adminUsersPage(HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        RepositorySecurityManager securityManager = RepositoryComponentFactory.getDefault().getSecurityManager();
        List<UserPrincipal> allUsers = securityManager.getAllUsers();

        if (homeRedirect != null) {
            return homeRedirect;
        }

        model.addAttribute( "userAccounts", allUsers );
        model.addAttribute( "isLocalUserManagement", isLocalUserManagement() );
        return applyCommonValues( session, model, "adminUsers" );
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to manage local user
     * accounts.
     * 
     * @param userId the ID of the user account to add
     * @param lastName the last name for the user account to add
     * @param firstName the first name for the user account to add
     * @param emailAddress the email address for the user account to add
     * @param password the password of the user to add
     * @param passwordConfirm the confirmation of the user's password
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping(value = {"/adminUsersAddLocal.html", "/adminUsersAddLocal.htm"})
    public String adminUsersAddLocalPage(@RequestParam(value = USER_ID, required = false) String userId,
        @RequestParam(value = LAST_NAME, required = false) String lastName,
        @RequestParam(value = FIRST_NAME, required = false) String firstName,
        @RequestParam(value = EMAIL_ADDRESS, required = false) String emailAddress,
        @RequestParam(value = "password", required = false) String password,
        @RequestParam(value = "passwordConfirm", required = false) String passwordConfirm, HttpSession session,
        Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        boolean success = false;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        try {
            if (userId != null) {
                boolean formError = hasErrors( userId, lastName, emailAddress, password, passwordConfirm, model );

                if (!formError) { // If everything is ok, add the user
                    RepositorySecurityManager securityManager =
                        RepositoryComponentFactory.getDefault().getSecurityManager();
                    UserPrincipal newUser = new UserPrincipal();

                    newUser.setUserId( userId );
                    newUser.setLastName( lastName );
                    newUser.setFirstName( firstName );
                    newUser.setEmailAddress( emailAddress );

                    securityManager.addUser( newUser );
                    securityManager.setUserPassword( userId, password );
                    setStatusMessage( String.format( "User '%s' created successfully", userId ), redirectAttrs );
                    success = true;
                }
            }

            if (!success) {
                model.addAttribute( USER_ID, userId );
                model.addAttribute( LAST_NAME, lastName );
                model.addAttribute( FIRST_NAME, firstName );
                model.addAttribute( EMAIL_ADDRESS, emailAddress );
            }

        } catch (RepositoryException e) {
            String message = String.format( "Unable to create user account: %s", userId );

            setErrorMessage( message, model );
            log.error( message, e );
        }
        return success ? REDIRECT_ADMIN_USERS : "adminUsersAddLocal";
    }

    /**
     * Checks the given form entries for errors and returns true if any exist.
     * 
     * @param userId the ID of the user account to add
     * @param lastName the last name for the user account to add
     * @param firstName the first name for the user account to add
     * @param emailAddress the email address for the user account to add
     * @param password the password of the user to add
     * @param passwordConfirm the confirmation of the user's password
     * @param model the model context to be used when rendering the page view
     * @return boolean
     */
    private boolean hasErrors(String userId, String lastName, String emailAddress, String password,
        String passwordConfirm, Model model) {
        RepositorySecurityManager securityManager = RepositoryComponentFactory.getDefault().getSecurityManager();
        boolean formError = false;

        if (securityManager.getUser( userId ) != null) {
            setErrorMessage( String.format( "A user with the ID '%s' already exists.", userId ), model );
            formError = true;

        } else if ((lastName == null) || (lastName.length() == 0)) {
            setErrorMessage( "The last name is a required value.", model );
            formError = true;

        } else if ((emailAddress != null) && !emailPattern.matcher( emailAddress ).matches()) {
            setErrorMessage( "The email provided is not a valid address.", model );
            formError = true;

        } else if ((password == null) || (password.length() == 0)) {
            setErrorMessage( "The password is a required value.", model );
            formError = true;

        } else if (password.indexOf( ' ' ) >= 0) {
            setErrorMessage( "White space characters are not permitted in passwords.", model );
            formError = true;

        } else if (!password.equals( passwordConfirm )) {
            setErrorMessage( "The passwords do not match.", model );
            formError = true;
        }
        return formError;
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to manage local user
     * accounts.
     * 
     * @param userId the ID of the user account to add
     * @param lastName the last name for the user account to add
     * @param firstName the first name for the user account to add
     * @param emailAddress the email address for the user account to add
     * @param updateUser flag indicating whether the user updates should be applied
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping(value = {"/adminUsersEditLocal.html", "/adminUsersEditLocal.htm"})
    public String adminUsersEditLocalPage(@RequestParam(value = USER_ID, required = false) String userId,
        @RequestParam(value = LAST_NAME, required = false) String lastName,
        @RequestParam(value = FIRST_NAME, required = false) String firstName,
        @RequestParam(value = EMAIL_ADDRESS, required = false) String emailAddress,
        @RequestParam(value = "updateUser", required = false) boolean updateUser, HttpSession session, Model model,
        RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        boolean success = false;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        try {
            RepositorySecurityManager securityManager = RepositoryComponentFactory.getDefault().getSecurityManager();
            UserPrincipal user = securityManager.getUser( userId );

            if (user == null) {
                setErrorMessage( "No user exists with the ID '" + userId + "'.", model );
                success = true; // Not a success, but will reroute back to the main user's page
            }

            if (updateUser) {
                if ((lastName == null) || (lastName.length() == 0)) {
                    setErrorMessage( "The last name is a required value.", model );

                } else if ((emailAddress != null) && !emailPattern.matcher( emailAddress ).matches()) {
                    setErrorMessage( "The email provided is not a valid address.", model );

                } else if (user != null) { // everything is ok - add the user
                    user.setUserId( userId );
                    user.setLastName( lastName );
                    user.setFirstName( firstName );
                    user.setEmailAddress( emailAddress );

                    securityManager.updateUser( user );

                    setStatusMessage( String.format( "User '%s' updated successfully.", userId ), redirectAttrs );
                    success = true;
                }

                if (!success) {
                    model.addAttribute( USER_ID, userId );
                    model.addAttribute( LAST_NAME, lastName );
                    model.addAttribute( FIRST_NAME, firstName );
                    model.addAttribute( EMAIL_ADDRESS, emailAddress );
                }

            } else if (user != null) {
                model.addAttribute( USER_ID, user.getUserId() );
                model.addAttribute( LAST_NAME, user.getLastName() );
                model.addAttribute( FIRST_NAME, user.getFirstName() );
                model.addAttribute( EMAIL_ADDRESS, user.getEmailAddress() );
            }

        } catch (RepositoryException e) {
            String message = String.format( "Unable to create user account: %s", userId );

            setErrorMessage( message, model );
            log.error( message, e );
        }
        return success ? REDIRECT_ADMIN_USERS : "adminUsersEditLocal";
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to manage local user
     * accounts.
     * 
     * @param searchFilter the search string that will be used for the directory search
     * @param maxResults the maximum number of results to return from the directory search
     * @param userId the ID of the user account to add
     * @param createUser flag indicating that the user to create has been selected
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping(value = {"/adminUsersAddDirectory.html", "/adminUsersAddDirectory.htm"})
    public String adminUsersAddDirectoryPage(
        @RequestParam(value = "searchFilter", required = false) String searchFilter,
        @RequestParam(value = "maxResults", required = false) Integer maxResults,
        @RequestParam(value = USER_ID, required = false) String userId,
        @RequestParam(value = "createUser", required = false) boolean createUser, HttpSession session, Model model,
        RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        boolean success = false;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        try {
            if (createUser && (userId != null) && (userId.length() > 0)) {
                RepositorySecurityManager securityManager =
                    RepositoryComponentFactory.getDefault().getSecurityManager();

                if (securityManager.getUser( userId ) == null) {
                    UserPrincipal newUser = new UserPrincipal();

                    newUser.setUserId( userId );
                    securityManager.addUser( newUser );
                    setStatusMessage( "User '" + userId + "' created successfully.", redirectAttrs );
                    success = true;

                } else {
                    setErrorMessage( "A user with the ID '" + userId + "' already exists.", model );
                }

            }

            if (!success && (searchFilter != null) && (searchFilter.length() > 0)) {
                AuthenticationProvider authProvider =
                    RepositoryComponentFactory.getDefault().getAuthenticationProvider();
                List<UserPrincipal> candidateUsers = authProvider.searchCandidateUsers( searchFilter, maxResults );

                model.addAttribute( "candidateUsers", candidateUsers );
            }

        } catch (RepositoryException e) {
            setErrorMessage( "Unable to create user account: " + userId, model );
            log.error( "Unable to create user account: " + userId, e );
        }

        if (!success) {
            model.addAttribute( "searchFilter", searchFilter );
            model.addAttribute( "maxResults", maxResults );
        }
        return success ? REDIRECT_ADMIN_USERS : "adminUsersAddDirectory";
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to manage local user
     * accounts.
     * 
     * @param userId the ID of the user account to delete
     * @param confirmDelete flag indicating that the user has confirmed the deletion
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping(value = {"/adminUsersDelete.html", "/adminUsersDelete.htm"})
    public String adminUsersDeletePage(@RequestParam(value = USER_ID, required = true) String userId,
        @RequestParam(value = "confirmDelete", required = false) boolean confirmDelete, HttpSession session,
        Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );

        if (homeRedirect != null) {
            return homeRedirect;
        }

        try {
            if (confirmDelete) {
                RepositorySecurityManager securityManager =
                    RepositoryComponentFactory.getDefault().getSecurityManager();

                securityManager.deleteUser( userId );
                setStatusMessage( "User '" + userId + "' deleted successfully.", redirectAttrs );

            } else {
                model.addAttribute( USER_ID, userId );
            }

        } catch (RepositoryException e) {
            setErrorMessage( "Unable to delete user account: " + userId, model );
            log.error( "Unable to delete user account: " + userId, e );
        }
        return confirmDelete ? REDIRECT_ADMIN_USERS : "adminUsersDelete";
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to manage local user
     * accounts.
     * 
     * @param userId the ID of the user account to add
     * @param newPassword the new password for the user
     * @param newPasswordConfirm the confirmation of the new password for the user
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping(value = {"/adminUsersChangePassword.html", "/adminUsersChangePassword.htm"})
    public String adminUsersChangePasswordPage(@RequestParam(value = USER_ID, required = true) String userId,
        @RequestParam(value = "newPassword", required = false) String newPassword,
        @RequestParam(value = "newPasswordConfirm", required = false) String newPasswordConfirm, HttpSession session,
        Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        boolean success = false;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        try {
            if ((newPassword == null) && (newPasswordConfirm == null)) {
                // no action - initial display of the form

            } else {
                if ((newPassword == null) || (newPassword.length() == 0)) {
                    setErrorMessage( "The new password is a required value.", model );

                } else if (newPassword.indexOf( ' ' ) >= 0) {
                    setErrorMessage( "White space characters are not permitted in passwords.", model );

                } else if (!newPassword.equals( newPasswordConfirm )) {
                    setErrorMessage( "The passwords do not match.", model );

                } else { // everything is ok - change the password
                    RepositorySecurityManager securityManager =
                        RepositoryComponentFactory.getDefault().getSecurityManager();

                    securityManager.setUserPassword( userId, newPassword );
                    setStatusMessage( "The password for '" + userId + "' was changed successfully.", redirectAttrs );
                    success = true;
                }
            }
            model.addAttribute( USER_ID, userId );

        } catch (RepositoryException e) {
            setErrorMessage( "Unable to change password for user: " + userId, model );
            log.error( "Unable to change password for user: " + userId, e );
        }
        return success ? REDIRECT_ADMIN_USERS : "adminUsersChangePassword";
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to manage group
     * assignments for users.
     * 
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminGroups.html", "/adminGroups.htm"})
    public String adminGroupsPage(HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );

        if (homeRedirect != null) {
            return homeRedirect;
        }

        try {
            RepositorySecurityManager securityManager = RepositoryComponentFactory.getDefault().getSecurityManager();
            List<UserPrincipal> allUsers = securityManager.getAllUsers();
            List<UserGroup> allGroups = new ArrayList<>();

            for (String groupName : securityManager.getGroupNames()) {
                allGroups.add( securityManager.getGroup( groupName ) );
            }
            model.addAttribute( "allGroups", allGroups );
            model.addAttribute( "allUsers", allUsers );

        } catch (RepositoryException e) {
            log.error( "Error displaying group assignments.", e );
            setErrorMessage( "Error displaying group assignments (see server log for details).", model );
        }
        return applyCommonValues( session, model, "adminGroups" );
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to add a new user group.
     * 
     * @param groupName the name of the group to be added
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminGroupsAdd.html", "/adminGroupsAdd.htm"})
    public String adminGroupsAddPage(@RequestParam(value = "groupName", required = false) String groupName,
        HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        String targetPage = null;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        if (groupName != null) {
            try {
                GroupAssignmentsResource groupsResource = new GroupAssignmentsResource( getRepositoryManager() );
                List<UserGroup> groupList = new ArrayList<>();
                List<String> groupNames = Arrays.asList( groupsResource.getGroupNames() );

                if (groupName.length() == 0) {
                    setErrorMessage( "Group name is a required value.", model );

                } else if (groupName.indexOf( ' ' ) >= 0) {
                    setErrorMessage( "White space characters are not allowed in group names.", model );

                } else if (groupNames.contains( groupName )) {
                    setErrorMessage( "A group named '" + groupName + "' already exists.", model );

                } else {
                    UserGroup newGroup = new UserGroup( groupName, new ArrayList<String>() );

                    for (String existingGroupName : groupNames) {
                        String[] groupMembers = groupsResource.getAssignedUsers( existingGroupName );
                        groupList.add( new UserGroup( existingGroupName, Arrays.asList( groupMembers ) ) );
                    }
                    groupList.add( newGroup );
                    groupsResource.saveGroupAssignments( groupList );
                    setStatusMessage( String.format( "Group '%s' created successfully.", groupName ), redirectAttrs );
                    targetPage = REDIRECT_ADMIN_GROUPS;
                }

            } catch (RepositoryException e) {
                log.error( "Error creating user group.", e );
                setErrorMessage( "Error creating user group (see server log for details).", model );
            }
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to delete a user group.
     * 
     * @param groupName the ID of the group to be edited
     * @param confirmDelete flag indicating that the user has confirmed the deletion
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminGroupsDelete.html", "/adminGroupsDelete.htm"})
    public String adminGroupsDeletePage(@RequestParam(value = "groupName", required = true) String groupName,
        @RequestParam(value = "confirmDelete", required = false) boolean confirmDelete, HttpSession session,
        Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        String targetPage = null;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        if (confirmDelete) {
            try {
                GroupAssignmentsResource groupsResource = new GroupAssignmentsResource( getRepositoryManager() );
                List<UserGroup> groupList = new ArrayList<>();
                List<String> groupNames = Arrays.asList( groupsResource.getGroupNames() );

                for (String existingGroupName : groupNames) {
                    if (existingGroupName.equals( groupName )) {
                        continue;
                    }
                    String[] groupMembers = groupsResource.getAssignedUsers( existingGroupName );
                    groupList.add( new UserGroup( existingGroupName, Arrays.asList( groupMembers ) ) );
                }
                groupsResource.saveGroupAssignments( groupList );
                setStatusMessage( "Group '" + groupName + "' deleted successfully.", redirectAttrs );
                targetPage = REDIRECT_ADMIN_GROUPS;

            } catch (RepositoryException e) {
                log.error( "Error deleting user group.", e );
                setErrorMessage( "Error deleting user group (see server log for details).", model );
            }

        } else {
            model.addAttribute( "groupName", groupName );
        }

        if (targetPage == null) {
            targetPage = applyCommonValues( session, model, "adminGroupsDelete" );
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to edit the members of a
     * group
     * 
     * @param groupName the ID of the group to be edited
     * @param groupMembers the updated list of user ID's for the group members
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminGroupsEdit.html", "/adminGroupsEdit.htm"})
    public String adminGroupsEditPage(@RequestParam(value = "groupName", required = true) String groupName,
        @RequestParam(value = "groupMembers", required = false) String groupMembers, HttpSession session, Model model,
        RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        GroupAssignmentsResource groupsResource = new GroupAssignmentsResource( getRepositoryManager() );
        String targetPage = null;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        if (groupMembers != null) {
            try {
                updateGroupMembers( groupName, groupMembers, groupsResource );
                setStatusMessage( "Group '" + groupName + "' updated successfully.", redirectAttrs );
                targetPage = REDIRECT_ADMIN_GROUPS;

            } catch (RepositoryException e) {
                log.error( "Error displaying or updating group assignments.", e );
                setErrorMessage( "Error displaying or updating group assignments (see server log for details).",
                    model );
            }
        }
        if (targetPage == null) {
            buildGroupFormData( groupName, groupMembers, model, groupsResource );
            targetPage = applyCommonValues( session, model, "adminGroupsEdit" );
        }
        return targetPage;
    }

    /**
     * Updates the members of the given group.
     * 
     * @param groupName the name of the group to update
     * @param groupMembers the updated list of group members
     * @param groupsResource maintains the mappings from the group ID's to the individual group members
     * @throws RepositoryException thrown if an error occurs while accessing the repository
     */
    private void updateGroupMembers(String groupName, String groupMembers, GroupAssignmentsResource groupsResource)
        throws RepositoryException {
        List<String> groupNames = Arrays.asList( groupsResource.getGroupNames() );
        List<UserGroup> groupList = new ArrayList<>();

        for (String existingGroupName : groupNames) {
            if (existingGroupName.equals( groupName )) {
                List<String> memberIds = new ArrayList<>();

                for (String memberId : groupMembers.split( "," )) {
                    if ((memberId != null) && (memberId.length() > 0)) {
                        memberIds.add( memberId );
                    }
                }
                groupList.add( new UserGroup( groupName, memberIds ) );

            } else {
                groupList.add( new UserGroup( existingGroupName,
                    Arrays.asList( groupsResource.getAssignedUsers( existingGroupName ) ) ) );
            }
        }
        groupsResource.saveGroupAssignments( groupList );
    }

    /**
     * Creates the form data required for the edit-group-members page.
     * 
     * @param groupName the name of the group
     * @param groupMembers comma-separated list of group members
     * @param model the model context to be used when rendering the page view
     * @param groupsResource provides mappings from the group ID's to the individual group members
     */
    private void buildGroupFormData(String groupName, String groupMembers, Model model,
        GroupAssignmentsResource groupsResource) {
        RepositorySecurityManager securityManager = RepositoryComponentFactory.getDefault().getSecurityManager();

        if (groupMembers == null) {
            StringBuilder memberIds = new StringBuilder();

            for (String userId : groupsResource.getAssignedUsers( groupName )) {
                memberIds.append( userId ).append( "," );
            }
            groupMembers = memberIds.toString();
        }
        model.addAttribute( "groupName", groupName );
        model.addAttribute( "allUsers", securityManager.getAllUsers() );
        model.addAttribute( "groupMembers", groupMembers );
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to reindex the free-text
     * search folder.
     * 
     * @param confirmReindexing flag indicating that the user has confirmed the reindexing task
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminSearchIndex.html", "/adminSearchIndex.htm"})
    public String adminSearchIndexPage(
        @RequestParam(value = "confirmReindexing", required = false) boolean confirmReindexing, HttpSession session,
        Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        boolean success = false;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        if (confirmReindexing) {
            try {
                if (searchService == null) {
                    throw new RepositoryException( "The search indexing service is not available." );
                }
                searchService.indexAllRepositoryItems();
                setStatusMessage( "The free-text search index is being refreshed...", redirectAttrs );
                success = true;

            } catch (RepositoryException e) {
                log.error( "Error processing repository re-indexing request.", e );
                setErrorMessage( e.getMessage(), model );

            } finally {
                model.addAttribute( "confirmReindexing", confirmReindexing );
            }
        }
        return success ? "redirect:/console/adminHome.html" : applyCommonValues( session, model, "adminSearchIndex" );
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to delete an item from
     * the repository.
     * 
     * @param baseNamespace the base namespace of the repository item to be deleted
     * @param filename the filename of the repository item to be deleted
     * @param version the version of the repository item to be deleted
     * @param confirmDeletion flag indicating that the user has confirmed the deletion task
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminDeleteItem.html", "/adminDeleteItem.htm"})
    public String adminDeleteItemPage(@RequestParam(value = BASE_NAMESPACE, required = true) String baseNamespace,
        @RequestParam(value = FILENAME, required = true) String filename,
        @RequestParam(value = VERSION, required = true) String version,
        @RequestParam(value = "confirmDelete", required = false) boolean confirmDeletion, HttpSession session,
        Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        String targetPage = null;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        if (confirmDeletion) {
            try {
                RepositoryManager repositoryManager = getRepositoryManager();
                RepositoryItem item = repositoryManager.getRepositoryItem( baseNamespace, filename, version );

                repositoryManager.delete( item );
                searchService.deleteRepositoryItemIndex( item );

                setStatusMessage( "Repository item deleted successfully: " + filename, redirectAttrs );
                redirectAttrs.addAttribute( BASE_NAMESPACE, baseNamespace );
                targetPage = "redirect:/console/browse.html";

            } catch (Exception e) {
                String message = getErrorMessage( e );

                log.error( "Unable to delete the repository item.", e );
                setErrorMessage( "Unable to delete the repository item " + message, redirectAttrs );
                redirectAttrs.addAttribute( BASE_NAMESPACE, baseNamespace );
                redirectAttrs.addAttribute( FILENAME, filename );
                redirectAttrs.addAttribute( VERSION, version );
                targetPage = REDIRECT_LIBRARY_INFO;
            }
        }
        if (targetPage == null) {
            try {
                RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );

                model.addAttribute( "item", item );
                targetPage = applyCommonValues( session, model, "adminDeleteItem" );

            } catch (RepositoryException e) {
                log.error( RI_DOES_NOT_EXIST, e );
                setErrorMessage( RI_DOES_NOT_EXIST, redirectAttrs );
                redirectAttrs.addAttribute( BASE_NAMESPACE, baseNamespace );
                targetPage = "redirect:/console/browse.html";
            }
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to promote a repository
     * item to FINAL state.
     * 
     * @param baseNamespace the base namespace of the repository item to be promoted
     * @param filename the filename of the repository item to be promoted
     * @param version the version of the repository item to be promoted
     * @param confirmPromote flag indicating that the user has confirmed the promotion task
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminPromoteItem.html", "/adminPromoteItem.htm"})
    public String adminPromoteItemPage(@RequestParam(value = BASE_NAMESPACE, required = true) String baseNamespace,
        @RequestParam(value = FILENAME, required = true) String filename,
        @RequestParam(value = VERSION, required = true) String version,
        @RequestParam(value = "confirmPromote", required = false) boolean confirmPromote, HttpSession session,
        Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        String targetPage = null;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        if (confirmPromote) {
            try {
                UserPrincipal user = getCurrentUser( session );
                RepositoryManager repositoryManager = getRepositoryManager();
                RepositoryItem item = repositoryManager.getRepositoryItem( baseNamespace, filename, version );

                repositoryManager.getFileManager().setCurrentUserId( user.getUserId() );
                repositoryManager.promote( item );
                searchService.indexRepositoryItem( item );

                setStatusMessage( "Repository item promoted successfully: " + filename, redirectAttrs );

            } catch (Exception e) {
                String message = getErrorMessage( e );

                log.error( "Unable to promote the repository item.", e );
                setErrorMessage( "Unable to promote the repository item" + message, redirectAttrs );

            } finally {
                redirectAttrs.addAttribute( BASE_NAMESPACE, baseNamespace );
                redirectAttrs.addAttribute( FILENAME, filename );
                redirectAttrs.addAttribute( VERSION, version );
                targetPage = REDIRECT_LIBRARY_INFO;
            }
        }
        if (targetPage == null) {
            try {
                RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );
                boolean otm16Enabled = RepositoryUtils.isOTM16Library( item, getRepositoryManager() );

                model.addAttribute( "item", item );
                model.addAttribute( "otm16Enabled", otm16Enabled );
                targetPage = applyCommonValues( session, model, "adminPromoteItem" );

            } catch (RepositoryException e) {
                log.error( RI_DOES_NOT_EXIST, e );
                setErrorMessage( RI_DOES_NOT_EXIST, model );
                targetPage = new BrowseController().browsePage( baseNamespace, null, session, model );
            }
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to demote a repository
     * item to DRAFT state.
     * 
     * @param baseNamespace the base namespace of the repository item to be demoted
     * @param filename the filename of the repository item to be demoted
     * @param version the version of the repository item to be demoted
     * @param confirmDemote flag indicating that the user has confirmed the demotion task
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminDemoteItem.html", "/adminDemoteItem.htm"})
    public String adminDemoteItemPage(@RequestParam(value = BASE_NAMESPACE, required = true) String baseNamespace,
        @RequestParam(value = FILENAME, required = true) String filename,
        @RequestParam(value = VERSION, required = true) String version,
        @RequestParam(value = "confirmDemote", required = false) boolean confirmDemote, HttpSession session,
        Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        String targetPage = null;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        if (confirmDemote) {
            try {
                UserPrincipal user = getCurrentUser( session );
                RepositoryManager repositoryManager = getRepositoryManager();
                RepositoryItem item = repositoryManager.getRepositoryItem( baseNamespace, filename, version );

                repositoryManager.getFileManager().setCurrentUserId( user.getUserId() );
                repositoryManager.demote( item );
                searchService.indexRepositoryItem( item );
                setStatusMessage( "Repository item demoted successfully: " + filename, redirectAttrs );

            } catch (Exception e) {
                String message = getErrorMessage( e );

                log.error( "Unable to demote the repository item.", e );
                setErrorMessage( "Unable to demote the repository item" + message, redirectAttrs );

            } finally {
                redirectAttrs.addAttribute( BASE_NAMESPACE, baseNamespace );
                redirectAttrs.addAttribute( FILENAME, filename );
                redirectAttrs.addAttribute( VERSION, version );
                targetPage = REDIRECT_LIBRARY_INFO;
            }
        }
        if (targetPage == null) {
            try {
                RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );
                boolean otm16Enabled = RepositoryUtils.isOTM16Library( item, getRepositoryManager() );

                model.addAttribute( "item", item );
                model.addAttribute( "otm16Enabled", otm16Enabled );
                targetPage = applyCommonValues( session, model, "adminDemoteItem" );

            } catch (RepositoryException e) {
                log.error( RI_DOES_NOT_EXIST, e );
                setErrorMessage( RI_DOES_NOT_EXIST, model );
                targetPage = new BrowseController().browsePage( baseNamespace, null, session, model );
            }
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to unlock a repository
     * item.
     * 
     * @param baseNamespace the base namespace of the repository item to be unlocked
     * @param filename the filename of the repository item to be unlocked
     * @param version the version of the repository item to be unlocked
     * @param confirmUnlock flag indicating that the user has confirmed the unlock task
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminUnlockItem.html", "/adminUnlockItem.htm"})
    public String adminUnlockItemPage(@RequestParam(value = BASE_NAMESPACE, required = true) String baseNamespace,
        @RequestParam(value = FILENAME, required = true) String filename,
        @RequestParam(value = VERSION, required = true) String version,
        @RequestParam(value = "confirmUnlock", required = false) boolean confirmUnlock, HttpSession session,
        Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        String targetPage = null;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        if (confirmUnlock) {
            try {
                RepositoryManager repositoryManager = getRepositoryManager();
                RepositoryItem item = repositoryManager.getRepositoryItem( baseNamespace, filename, version );

                repositoryManager.unlock( item, false, null );
                searchService.indexRepositoryItem( item );
                setStatusMessage( "Repository item unlocked successfully: " + filename, redirectAttrs );

            } catch (Exception e) {
                String message = getErrorMessage( e );

                log.error( "Unable to unlock the repository item.", e );
                setErrorMessage( "Unable to unlock the repository item" + message, redirectAttrs );

            } finally {
                redirectAttrs.addAttribute( BASE_NAMESPACE, baseNamespace );
                redirectAttrs.addAttribute( FILENAME, filename );
                redirectAttrs.addAttribute( VERSION, version );
                targetPage = REDIRECT_LIBRARY_INFO;
            }
        }
        if (targetPage == null) {
            try {
                RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );

                model.addAttribute( "item", item );
                targetPage = applyCommonValues( session, model, "adminUnlockItem" );

            } catch (RepositoryException e) {
                log.error( RI_DOES_NOT_EXIST, e );
                setErrorMessage( RI_DOES_NOT_EXIST, model );
                targetPage = new BrowseController().browsePage( baseNamespace, null, session, model );
            }
        }
        return targetPage;
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to recalculate the CRC of
     * a repository item that is in the final state.
     * 
     * @param baseNamespace the base namespace of the repository item whose CRC is to be recalculated
     * @param filename the filename of the repository item whose CRC is to be recalculated
     * @param version the version of the repository item whose CRC is to be recalculated
     * @param confirmRecalculate flag indicating that the user has confirmed the recalculation task
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model context to be used when rendering the page view
     * @param redirectAttrs request attributes for the redirect in the case of success or an unauthorized user
     * @return String
     */
    @RequestMapping({"/adminRecalculateItemCrc.html", "/adminRecalculateItemCrc.htm"})
    public String adminRecalculateItemCrcPage(
        @RequestParam(value = BASE_NAMESPACE, required = true) String baseNamespace,
        @RequestParam(value = FILENAME, required = true) String filename,
        @RequestParam(value = VERSION, required = true) String version,
        @RequestParam(value = "confirmRecalculate", required = false) boolean confirmRecalculate, HttpSession session,
        Model model, RedirectAttributes redirectAttrs) {
        String homeRedirect = checkAdminAccess( session, redirectAttrs );
        String targetPage = null;

        if (homeRedirect != null) {
            return homeRedirect;
        }

        if (confirmRecalculate) {
            try {
                RepositoryManager repositoryManager = getRepositoryManager();
                RepositoryItem item = repositoryManager.getRepositoryItem( baseNamespace, filename, version );

                if (item.getStatus() == TLLibraryStatus.DRAFT) {
                    setErrorMessage( "Only repository items in non-Draft status are assigned a CRC value.",
                        redirectAttrs );

                } else {
                    UserPrincipal user = getCurrentUser( session );

                    repositoryManager.getFileManager().setCurrentUserId( user.getUserId() );
                    repositoryManager.recalculateCrc( item );
                    searchService.indexRepositoryItem( item );

                    setStatusMessage( "Repository item CRC recalculated successfully: " + filename, redirectAttrs );
                }

            } catch (Exception e) {
                String message = getErrorMessage( e );

                log.error( "Unable to recalculate the repository item's CRC.", e );
                setErrorMessage( "Unable to recalculate the repository item's CRC" + message, redirectAttrs );

            } finally {
                redirectAttrs.addAttribute( BASE_NAMESPACE, baseNamespace );
                redirectAttrs.addAttribute( FILENAME, filename );
                redirectAttrs.addAttribute( VERSION, version );
                targetPage = REDIRECT_LIBRARY_INFO;
            }
        }
        if (targetPage == null) {
            try {
                RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );

                model.addAttribute( "item", item );
                targetPage = applyCommonValues( session, model, "adminRecalculateItemCrc" );

            } catch (RepositoryException e) {
                log.error( RI_DOES_NOT_EXIST, e );
                setErrorMessage( RI_DOES_NOT_EXIST, model );
                targetPage = new BrowseController().browsePage( baseNamespace, null, session, model );
            }
        }
        return targetPage;
    }

    /**
     * Applies all required common values to the model. If the user does not have administrative rights, the target page
     * will be changed to the home page of the repository instead of the requested admin page.
     * 
     * @param session the HTTP session that contains information about an authenticated user
     * @param model the model to which the common values should be applied
     * @param targetPage the target navigation page for the controller
     * @return String
     */
    protected String applyCommonValues(HttpSession session, Model model, String targetPage) {
        String result = applyCommonValues( model, targetPage );
        Boolean isAdministrator = (Boolean) session.getAttribute( "isAdminAuthorized" );

        if ((isAdministrator == null) || !isAdministrator.booleanValue()) {
            result = "homePage";
        }
        return result;
    }

    /**
     * Verifies that the current user has been authenticated with administrative access. If successful, this method will
     * return null. If not, a redirect URL for the will be returned application home page will be returned where an
     * error message will be displayed.
     * 
     * @param session the HTTP session that contains information about an authenticated user
     * @param redirectAttrs request attributes for the redirect in the case of an unauthorized user
     * @return String
     */
    private String checkAdminAccess(HttpSession session, RedirectAttributes redirectAttrs) {
        Boolean isAdministrator = (Boolean) session.getAttribute( "isAdminAuthorized" );
        UserPrincipal currentUser = getCurrentUser( session );
        String homePageRedirect = null;

        if ((currentUser == null) || (currentUser == UserPrincipal.ANONYMOUS_USER) || (isAdministrator == null)
            || !isAdministrator) {
            setErrorMessage( "UNAUTHORIZED: You must be logged in as an administrator to access the requested page.",
                redirectAttrs );
            homePageRedirect = "redirect:/console/index.html";
        }
        return homePageRedirect;
    }

    /**
     * Returns an error message for the given throwable.
     * 
     * @param t the exception/error for which to return a message
     * @return String
     */
    private String getErrorMessage(Throwable t) {
        String message = null;

        while ((message == null) && (t != null)) {
            message = t.getMessage();
            t = t.getCause();
        }

        if (message == null) {
            message = " (see server log for details)";
        } else {
            message = "<br><pre>" + message + "</pre>";
        }
        return message;
    }

}

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

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositorySecurityException;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller that handles user interactions for login and logout operations.
 * 
 * @author S. Livezey
 */
@Controller
public class LoginController extends BaseController {

    private static Log log = LogFactory.getLog(LoginController.class);

    /**
     * Called by the Spring MVC controller to process a login request from a user.
     * 
     * @param userId  the ID of the user to be authenticated
     * @param password  the password credentials to be authenticated
     * @param currentPageId  the current page being displayed at the time of the login request
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @param redirectAttrs  request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping(method = RequestMethod.POST, value = { "/", "/login.html", "/login.htm" })
    public String login(@RequestParam("userid") String userId,
            @RequestParam("password") String password,
            HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        RepositorySecurityManager securityManager = RepositoryComponentFactory.getDefault().getSecurityManager();
        UserPrincipal user = null;

        if ((userId != null) && (userId.length() > 0) && (password != null)
                && (password.length() > 0)) {
            try {
                user = securityManager.authenticateUser(userId, password);

            } catch (RepositorySecurityException e) {
                // Authentication failed.
            }
        }

        if ((user != null) && !user.getUserId().equals(UserPrincipal.ANONYMOUS_USER_ID)) {
            session.setAttribute("user", user);
            session.setAttribute("isAdminAuthorized", securityManager.isAdministrator(user));
            return "redirect:/console/index.html";
            
        } else { // authentication failed
            model.addAttribute("loginError", true);
            model.addAttribute("userId", userId);
            return new SearchController().defaultSearchPage(session, model);
        }
    }

    /**
     * Called by the Spring MVC controller to process a logout request from a user. This method
     * simply clears all user-related information from the current HTTP session.
     * 
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @param redirectAttrs  request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping(method = RequestMethod.GET, value = { "/", "/logout.html", "/logout.htm" })
    public String logout(HttpSession session, Model model) {
        session.removeAttribute("user");
        session.removeAttribute("isAdminAuthorized");
        return "redirect:/console/index.html";
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to
     * manage local user accounts.
     * 
     * @param userId  the ID of the user account to add
     * @param lastName  the last name for the user account to add
     * @param firstName  the first name for the user account to add
     * @param emailAddress  the email address for the user account to add
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @param redirectAttrs  request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping(value = { "/editUserProfile.html", "/editUserProfile.htm" })
    public String editProfilePage(
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "emailAddress", required = false) String emailAddress,
            @RequestParam(value = "updateUser", required = false) boolean updateUser,
            HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        boolean success = false;
        try {
            UserPrincipal currentUser = (UserPrincipal) session.getAttribute("user");
            String userId = (currentUser == null) ? null : currentUser.getUserId();
            
            emailAddress = trimString( emailAddress );
            
        	if (currentUser == null) {
                setErrorMessage("You must be logged in to edit your profile.", model);
        		success = true; // Not a success, but will reroute back to the home page
        		
        	} else if (updateUser) {
            	if ((lastName == null) || (lastName.length() == 0)) {
                    setErrorMessage("The last name is a required value.", model);

            	} else if ((emailAddress != null) && !AdminController.emailPattern.matcher( emailAddress ).matches()) {
                    setErrorMessage("The email provided is not a valid address.", model);

                } else { // everything is ok - add the user
                    RepositorySecurityManager securityManager = RepositoryComponentFactory.getDefault().getSecurityManager();
                    
                	currentUser.setUserId( userId );
                	currentUser.setLastName( lastName );
                	currentUser.setFirstName( firstName );
                	currentUser.setEmailAddress( emailAddress );
                	
                	securityManager.updateUser( currentUser );
                    setStatusMessage("User '" + userId + "' updated successfully.", redirectAttrs);
                    success = true;
                }
                if (!success) {
                    model.addAttribute("userId", userId);
                    model.addAttribute("lastName", lastName);
                    model.addAttribute("firstName", firstName);
                    model.addAttribute("emailAddress", emailAddress);
                }
                
            } else {
                model.addAttribute("userId", userId);
                model.addAttribute("lastName", currentUser.getLastName());
                model.addAttribute("firstName", currentUser.getFirstName());
                model.addAttribute("emailAddress", currentUser.getEmailAddress());
            }
            
            
        } catch (RepositoryException e) {
            setErrorMessage("Unable to update user profile.", model);
            log.error("Unable to update user profile.", e);
        }
        return success ? "redirect:/console/index.html" : applyCommonValues(model, "editUserProfile");
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to
     * manage local user accounts.
     * 
     * @param oldPassword  the old password for the user
     * @param newPassword  the new password for the user
     * @param newPasswordConfirm  the confirmation of the new password for the user
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @param redirectAttrs  request attributes for the redirect in the case of a successful operation
     * @return String
     */
    @RequestMapping(value = { "/changePassword.html", "/changePassword.htm" })
    public String changePasswordPage(
            @RequestParam(value = "oldPassword", required = false) String oldPassword,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "newPasswordConfirm", required = false) String newPasswordConfirm,
            HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        boolean success = false;
        try {
            UserPrincipal currentUser = (UserPrincipal) session.getAttribute("user");

            if (currentUser == null) {
                setErrorMessage("You must be logged in to change your password.", model);

            } else if ((oldPassword == null) && (newPassword == null)
                    && (newPasswordConfirm == null)) {
                // no action - initial display of the form

            } else {
                if ((oldPassword == null) || (oldPassword.length() == 0)) {
                    setErrorMessage("The old password is a required value.", model);

                } else if ((newPassword == null) || (newPassword.length() == 0)) {
                    setErrorMessage("The new password is a required value.", model);

                } else if ((oldPassword.indexOf(' ') >= 0) || (newPassword.indexOf(' ') >= 0)) {
                    setErrorMessage("White space characters are not permitted in passwords.", model);

                } else if (!newPassword.equals(newPasswordConfirm)) {
                    setErrorMessage("The new passwords do not match.", model);

                } else { // everything is ok - change the password
                    try {
                        RepositorySecurityManager securityManager = RepositoryComponentFactory.getDefault().getSecurityManager();
                        
                        // throws an exception if the old password is not valid
                        securityManager.authenticateUser(currentUser.getUserId(), oldPassword);
                        securityManager.setUserPassword(currentUser.getUserId(), newPassword);
                        setStatusMessage("Your password has been changed.", redirectAttrs);
                        success = true;

                    } catch (RepositorySecurityException e) {
                        setErrorMessage("The old password you provided is invalid.", model);
					}
                }
            }

        } catch (RepositoryException e) {
            setErrorMessage("Unable to change your password - please contact your system administrator.", model);
            log.error("Unable to change password for user: " + session.getAttribute("user"), e);
        }
        if (success) {
            new BrowseController().browsePage(null, null, session, model);
        }
        return success ? "redirect:/console/index.html" : applyCommonValues(model, "changePassword");
    }
    
}

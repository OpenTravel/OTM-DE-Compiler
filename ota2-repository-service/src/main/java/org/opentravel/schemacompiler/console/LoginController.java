package org.opentravel.schemacompiler.console;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.security.RepositorySecurityException;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.opentravel.schemacompiler.security.impl.FileAuthenticationProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
     * @param userId
     *            the ID of the user to be authenticated
     * @param password
     *            the password credentials to be authenticated
     * @param currentPageId
     *            the current page being displayed at the time of the login request
     * @param session
     *            the HTTP session that contains information about an authenticated user
     * @param model
     *            the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping(method = RequestMethod.POST, value = { "/", "/login.html", "/login.htm" })
    public String login(@RequestParam("userid") String userId,
            @RequestParam("password") String password,
            @RequestParam("currentPage") String currentPageId, HttpSession session, Model model) {
        RepositorySecurityManager securityManager = RepositoryComponentFactory.getDefault()
                .getSecurityManager();
        UserPrincipal user = null;

        if ((userId != null) && (userId.length() > 0) && (password != null)
                && (password.length() > 0)) {
            try {
                user = securityManager.getUser(userId, password);

            } catch (RepositorySecurityException e) {
                // Authentication failed.
            }
        }

        if ((user != null) && !user.getUserId().equals(UserPrincipal.ANONYMOUS_USER_ID)) {
            session.setAttribute("user", user);
            session.setAttribute("isAdminAuthorized", securityManager.isAdministrator(user));

        } else { // authentication failed
            model.addAttribute("loginError", true);
            model.addAttribute("userId", userId);
        }
        new BrowseController().browsePage(null, null, session, model);
        return applyCommonValues(model, ((currentPageId == null) ? "homePage" : currentPageId));
    }

    /**
     * Called by the Spring MVC controller to process a logout request from a user. This method
     * simply clears all user-related information from the current HTTP session.
     * 
     * @param session
     *            the HTTP session that contains information about an authenticated user
     * @param model
     *            the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping(method = RequestMethod.GET, value = { "/", "/logout.html", "/logout.htm" })
    public String logout(HttpSession session, Model model) {
        session.removeAttribute("user");
        session.removeAttribute("isAdminAuthorized");
        new BrowseController().browsePage(null, null, session, model);
        return applyCommonValues(model, "homePage");
    }

    /**
     * Called by the Spring MVC controller to display the application administration page used to
     * manage local user accounts.
     * 
     * @param oldPassword
     *            the old password for the user
     * @param newPassword
     *            the new password for the user
     * @param newPasswordConfirm
     *            the confirmation of the new password for the user
     * @param session
     *            the HTTP session that contains information about an authenticated user
     * @param model
     *            the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping(value = { "/changePassword.html", "/changePassword.htm" })
    public String changePasswordPage(
            @RequestParam(value = "oldPassword", required = false) String oldPassword,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "newPasswordConfirm", required = false) String newPasswordConfirm,
            HttpSession session, Model model) {
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
                        RepositorySecurityManager securityManager = RepositoryComponentFactory
                                .getDefault().getSecurityManager();
                        RepositoryFileManager fileManager = getRepositoryManager().getFileManager();

                        securityManager.getUser(currentUser.getUserId(), oldPassword); // throws an
                                                                                       // exception
                                                                                       // if
                                                                                       // password
                                                                                       // is not
                                                                                       // valid
                        FileAuthenticationProvider.saveUserCredentials(currentUser.getUserId(),
                                newPassword, false, fileManager);
                        setStatusMessage("Your password has been changed.", model);
                        success = true;

                    } catch (RepositorySecurityException e) {
                        setErrorMessage("The old password you provided is invalid.", model);
                    }
                }
            }

        } catch (IOException e) {
            setErrorMessage(
                    "Unable to change your password - please contact your system administrator.",
                    model);
            log.error("Unable to change password for user: " + session.getAttribute("user"), e);
        }
        if (success) {
            new BrowseController().browsePage(null, null, session, model);
        }
        return applyCommonValues(model, success ? "homePage" : "changePassword");
    }

}

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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionEventType;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.schemacompiler.index.FreeTextSearchService;
import org.opentravel.schemacompiler.index.FreeTextSearchServiceFactory;
import org.opentravel.schemacompiler.index.SubscriptionSearchResult;
import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.security.RepositorySecurityException;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.opentravel.schemacompiler.subscription.SubscriptionManager;
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
                user = securityManager.authenticateUser(userId, password);

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
     * @param userId
     *            the ID of the user account to add
     * @param lastName
     *            the last name for the user account to add
     * @param firstName
     *            the first name for the user account to add
     * @param emailAddress
     *            the email address for the user account to add
     * @param session
     *            the HTTP session that contains information about an authenticated user
     * @param model
     *            the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping(value = { "/editUserProfile.html", "/editUserProfile.htm" })
    public String editProfilePage(
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "emailAddress", required = false) String emailAddress,
            @RequestParam(value = "updateUser", required = false) boolean updateUser,
            HttpSession session, Model model) {
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
                    setStatusMessage("User '" + userId + "' updated successfully.", model);
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
        return applyCommonValues(model, success ? "homePage" : "editUserProfile");
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
                        RepositorySecurityManager securityManager = RepositoryComponentFactory.getDefault().getSecurityManager();
                        
                        // throws an exception if the old password is not valid
                        securityManager.authenticateUser(currentUser.getUserId(), oldPassword);
                        securityManager.setUserPassword(currentUser.getUserId(), newPassword);
                        setStatusMessage("Your password has been changed.", model);
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
        return applyCommonValues(model, success ? "homePage" : "changePassword");
    }
    
    /**
     * Called by the Spring MVC controller to display a consolidated list of subscriptions
     * for the current user.
     * 
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping({ "/subscriptions.html", "/subscriptions.htm" })
    public String viewSubscriptions(HttpSession session, Model model) {
        try {
            UserPrincipal user = getCurrentUser(session);
        	List<SubscriptionSearchResult> subscriptions;
        	
            if (user != null) {
            	FreeTextSearchService searchService = FreeTextSearchServiceFactory.getInstance();
            	subscriptions = searchService.getSubscriptions( user.getUserId() );
            } else {
            	subscriptions = new ArrayList<>();
            }
            model.addAttribute("user", user);
            model.addAttribute("subscriptions", subscriptions);
            
        } catch (Throwable t) {
            log.error("An error occured while displaying the subscriptions page.", t);
            setErrorMessage("An error occured while displaying the page (see server log for details).", model);
        }
        return applyCommonValues(model, "subscriptions");
    }

    /**
     * Called by the Spring MVC controller to display the page used to edit a user's subscription
     * to namespace events.
     * 
     * @param ns  the namespace for which to edit the user's subscription
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping(value = { "/namespaceSubscription.html", "/namespaceSubscription.htm" })
    public String namespaceSubscription(
            @RequestParam(value = "baseNamespace", required = false) String baseNamespace,
            @RequestParam(value = "cts", required = false) boolean cancelToSubscriptionPage,
            @RequestParam(value = "etLibraryPublish", required = false) boolean etLibraryPublish,
            @RequestParam(value = "etLibraryNewVersion", required = false) boolean etLibraryNewVersion,
            @RequestParam(value = "etLibraryStatusChange", required = false) boolean etLibraryStatusChange,
            @RequestParam(value = "etLibraryStateChange", required = false) boolean etLibraryStateChange,
            @RequestParam(value = "etLibraryCommit", required = false) boolean etLibraryCommit,
            @RequestParam(value = "etLibraryMoveOrRename", required = false) boolean etLibraryMoveOrRename,
            @RequestParam(value = "etNamespaceAction", required = false) boolean etNamespaceAction,
            @RequestParam(value = "updateSubscription", required = false) boolean updateSubscription,
            HttpSession session, Model model) {
    	String targetPage = "namespaceSubscription";
        boolean success = false;
        try {
        	SubscriptionManager subscriptionManager = RepositoryComponentFactory.getDefault().getSubscriptionManager();
            UserPrincipal currentUser = (UserPrincipal) session.getAttribute("user");
            
            if (currentUser == null) {
                setErrorMessage("You must be logged in to edit your subscription settings.", model);
                
            } else if (baseNamespace == null) {
                setErrorMessage("Unable to edit subscription settings - namespace not specified.", model);
                targetPage = new BrowseController().browsePage(null, null, session, model);
                
            } else if (updateSubscription) {
            	List<SubscriptionEventType> eventTypes = new ArrayList<>();
            	
                if (etLibraryPublish) eventTypes.add( SubscriptionEventType.LIBRARY_PUBLISH );
                if (etLibraryNewVersion) eventTypes.add( SubscriptionEventType.LIBRARY_NEW_VERSION );
                if (etLibraryStatusChange) eventTypes.add( SubscriptionEventType.LIBRARY_STATUS_CHANGE );
                if (etLibraryStateChange) eventTypes.add( SubscriptionEventType.LIBRARY_STATE_CHANGE );
                if (etLibraryCommit) eventTypes.add( SubscriptionEventType.LIBRARY_COMMIT );
                if (etLibraryMoveOrRename) eventTypes.add( SubscriptionEventType.LIBRARY_MOVE_OR_RENAME );
                if (etNamespaceAction) eventTypes.add( SubscriptionEventType.NAMESPACE_ACTION );
                subscriptionManager.updateNamespaceSubscriptions(
                		baseNamespace, currentUser.getUserId(), eventTypes );
            	success = true;
            	
            } else {
            	List<SubscriptionEventType> eventTypes =
            			subscriptionManager.getNamespaceSubscriptions( baseNamespace, currentUser.getUserId() );
            	
                etLibraryPublish = eventTypes.contains( SubscriptionEventType.LIBRARY_PUBLISH );
                etLibraryNewVersion = eventTypes.contains( SubscriptionEventType.LIBRARY_NEW_VERSION );
                etLibraryStatusChange = eventTypes.contains( SubscriptionEventType.LIBRARY_STATUS_CHANGE );
                etLibraryStateChange = eventTypes.contains( SubscriptionEventType.LIBRARY_STATE_CHANGE );
                etLibraryCommit = eventTypes.contains( SubscriptionEventType.LIBRARY_COMMIT );
                etLibraryMoveOrRename = eventTypes.contains( SubscriptionEventType.LIBRARY_MOVE_OR_RENAME );
                etNamespaceAction = eventTypes.contains( SubscriptionEventType.NAMESPACE_ACTION );
            }
            model.addAttribute("baseNamespace", baseNamespace);
            model.addAttribute("cts", cancelToSubscriptionPage);
            model.addAttribute("etLibraryPublish", etLibraryPublish);
            model.addAttribute("etLibraryNewVersion", etLibraryNewVersion);
            model.addAttribute("etLibraryStatusChange", etLibraryStatusChange);
            model.addAttribute("etLibraryStateChange", etLibraryStateChange);
            model.addAttribute("etLibraryCommit", etLibraryCommit);
            model.addAttribute("etLibraryMoveOrRename", etLibraryMoveOrRename);
            model.addAttribute("etNamespaceAction", etNamespaceAction);
            
        } catch (RepositoryException e) {
            setErrorMessage("Error updating namespace subscription settings - please contact your system administrator.", model);
            log.error("Error updating namespace subscription settings.", e);
        }
        if (success) {
        	setStatusMessage("Subscription settings updated successfully.", model);
        	
        	if (cancelToSubscriptionPage) {
        		// Wait for changes to propagage through the indexing process before redisplaying
        		// the subscriptions page.  This is a hack, but good enough for now.
        		try {
        			Thread.sleep(1000);
        		} catch (InterruptedException e) {}
                targetPage = viewSubscriptions(session, model);
        	} else {
                targetPage = new BrowseController().browsePage(baseNamespace, null, session, model);
        	}
        }
        return applyCommonValues(model, targetPage);
    }
    
    /**
     * Called by the Spring MVC controller to display the page used to edit a user's subscription
     * to namespace events.
     * 
     * @param ns  the namespace for which to edit the user's subscription
     * @param session  the HTTP session that contains information about an authenticated user
     * @param model  the model context to be used when rendering the page view
     * @return String
     */
    @RequestMapping(value = { "/librarySubscription.html", "/librarySubscription.htm" })
    public String librarySubscription(
            @RequestParam(value = "baseNamespace", required = false) String baseNamespace,
            @RequestParam(value = "libraryName", required = false) String libraryName,
            @RequestParam(value = "version", required = false) String version,
            @RequestParam(value = "filename", required = false) String filename,
            @RequestParam(value = "allVersions", required = false) boolean allVersions,
            @RequestParam(value = "etLibraryPublish", required = false) boolean etLibraryPublish,
            @RequestParam(value = "etLibraryNewVersion", required = false) boolean etLibraryNewVersion,
            @RequestParam(value = "etLibraryStatusChange", required = false) boolean etLibraryStatusChange,
            @RequestParam(value = "etLibraryStateChange", required = false) boolean etLibraryStateChange,
            @RequestParam(value = "etLibraryCommit", required = false) boolean etLibraryCommit,
            @RequestParam(value = "etLibraryMoveOrRename", required = false) boolean etLibraryMoveOrRename,
            @RequestParam(value = "updateSubscription", required = false) boolean updateSubscription,
            HttpSession session, Model model) {
    	String targetPage = "librarySubscription";
        boolean success = false;
        try {
        	SubscriptionManager subscriptionManager = RepositoryComponentFactory.getDefault().getSubscriptionManager();
        	SubscriptionTarget sTarget = SubscriptionManager.getSubscriptionTarget( baseNamespace, libraryName, version );
            UserPrincipal currentUser = (UserPrincipal) session.getAttribute("user");
        	
        	sTarget.setBaseNamespace( baseNamespace );
        	sTarget.setLibraryName( libraryName );
        	sTarget.setVersion( version );
            
            if (currentUser == null) {
                setErrorMessage("You must be logged in to edit your subscription settings.", model);
                
            } else if ((baseNamespace == null) || (libraryName == null)) {
                setErrorMessage("Unable to edit subscription settings - library information not specified.", model);
                targetPage = new BrowseController().browsePage(null, null, session, model);
                
            } else if (updateSubscription) {
            	List<SubscriptionEventType> eventTypes = new ArrayList<>();
            	
                if (etLibraryPublish) eventTypes.add( SubscriptionEventType.LIBRARY_PUBLISH );
                if (etLibraryNewVersion) eventTypes.add( SubscriptionEventType.LIBRARY_NEW_VERSION );
                if (etLibraryStatusChange) eventTypes.add( SubscriptionEventType.LIBRARY_STATUS_CHANGE );
                if (etLibraryStateChange) eventTypes.add( SubscriptionEventType.LIBRARY_STATE_CHANGE );
                if (etLibraryCommit) eventTypes.add( SubscriptionEventType.LIBRARY_COMMIT );
                if (etLibraryMoveOrRename) eventTypes.add( SubscriptionEventType.LIBRARY_MOVE_OR_RENAME );
                
                if (allVersions) {
                    subscriptionManager.updateAllVersionsSubscriptions( sTarget, currentUser.getUserId(), eventTypes );
                	
                } else {
                    subscriptionManager.updateSingleVersionSubscriptions( sTarget, currentUser.getUserId(), eventTypes );
                }
            	success = true;
            	
            } else {
            	List<SubscriptionEventType> eventTypes;
            	
            	if (allVersions) {
                	eventTypes = subscriptionManager.getAllVersionsSubscriptions( sTarget, currentUser.getUserId() );
                	
            	} else {
                	eventTypes = subscriptionManager.getSingleVersionSubscriptions( sTarget, currentUser.getUserId() );
            	}
            	
                etLibraryPublish = eventTypes.contains( SubscriptionEventType.LIBRARY_PUBLISH );
                etLibraryNewVersion = eventTypes.contains( SubscriptionEventType.LIBRARY_NEW_VERSION );
                etLibraryStatusChange = eventTypes.contains( SubscriptionEventType.LIBRARY_STATUS_CHANGE );
                etLibraryStateChange = eventTypes.contains( SubscriptionEventType.LIBRARY_STATE_CHANGE );
                etLibraryCommit = eventTypes.contains( SubscriptionEventType.LIBRARY_COMMIT );
                etLibraryMoveOrRename = eventTypes.contains( SubscriptionEventType.LIBRARY_MOVE_OR_RENAME );
            }
            model.addAttribute("baseNamespace", baseNamespace);
            model.addAttribute("libraryName", libraryName);
            model.addAttribute("version", version);
            model.addAttribute("filename", filename);
            model.addAttribute("allVersions", allVersions);
            model.addAttribute("etLibraryPublish", etLibraryPublish);
            model.addAttribute("etLibraryNewVersion", etLibraryNewVersion);
            model.addAttribute("etLibraryStatusChange", etLibraryStatusChange);
            model.addAttribute("etLibraryStateChange", etLibraryStateChange);
            model.addAttribute("etLibraryCommit", etLibraryCommit);
            model.addAttribute("etLibraryMoveOrRename", etLibraryMoveOrRename);
            
        } catch (RepositoryException e) {
            setErrorMessage("Error updating namespace subscription settings - please contact your system administrator.", model);
            log.error("Error updating namespace subscription settings.", e);
        }
        if (success) {
        	setStatusMessage("Subscription settings updated successfully.", model);
        	targetPage = new ViewItemController().libraryInfo(baseNamespace, filename, version, session, model);
        }
        return applyCommonValues(model, targetPage);
    }
    
}

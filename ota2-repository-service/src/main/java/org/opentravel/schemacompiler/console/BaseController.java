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

import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.RepositoryMetadataResource;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.opentravel.schemacompiler.security.impl.FileAuthenticationProvider;
import org.opentravel.schemacompiler.util.RepositoryLogoImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Abstract base class of all Spring Web MVC controllers for the OTA2.0 repository console application.
 * 
 * @author S. Livezey
 */
public abstract class BaseController {

    private static final String STATUS_MESSAGE = "statusMessage";
    private static final String ERROR_MESSAGE = "errorMessage";

    private RepositoryMetadataResource repositoryMetadataResource;
    private RepositoryManager repositoryManager;
    private RepositorySecurityManager securityManager;
    @Autowired
    private HttpServletRequest servletRequest;

    /**
     * Default constructor.
     */
    public BaseController() {
        RepositoryComponentFactory componentFactory = RepositoryComponentFactory.getDefault();

        this.repositoryMetadataResource = new RepositoryMetadataResource( componentFactory.getRepositoryLocation() );
        this.repositoryManager = componentFactory.getRepositoryManager();
        this.securityManager = componentFactory.getSecurityManager();
    }

    /**
     * Returns the manager of the locally-managed OTA2.0 repository.
     * 
     * @return RepositoryManager
     */
    protected RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    /**
     * Returns the security manager of the locally-managed OTA2.0 repository.
     * 
     * @return RepositorySecurityManager
     */
    protected RepositorySecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * Applies all values to the given model that are common to the entire repository console application. If
     * 'targetPage' value will always be returned by this method if a non-null value is provided. If null, a default
     * value of "homePage" will be returned.
     * 
     * @param model the model to which the common values should be applied
     * @param targetPage the target navigation page for the controller
     * @return String
     */
    protected String applyCommonValues(Model model, String targetPage) {
        if (servletRequest != null) {
            model.addAttribute( ERROR_MESSAGE, servletRequest.getParameter( ERROR_MESSAGE ) );
            model.addAttribute( STATUS_MESSAGE, servletRequest.getParameter( STATUS_MESSAGE ) );
        }
        model.addAttribute( "isLocalUserManagement", isLocalUserManagement() );
        model.addAttribute( "repositoryTitle", repositoryMetadataResource.getResource().getDisplayName() );
        model.addAttribute( "repositoryLogo", RepositoryLogoImage.getDefault().getLogoUrl() );
        return (targetPage == null) ? "homePage" : targetPage;
    }

    /**
     * Returns true if user accounts are managed locally (false for remote authentication via JNDI).
     * 
     * @return boolean
     */
    protected boolean isLocalUserManagement() {
        return (RepositoryComponentFactory.getDefault()
            .getAuthenticationProvider() instanceof FileAuthenticationProvider);
    }

    /**
     * Returns the current user associated with the given HTTP session. If an authenticated user is not associated with
     * the session, the anonymous user instance will be returned.
     * 
     * @param session the HTTP session that contains information about an authenticated user
     * @return UserPrincipal
     */
    protected UserPrincipal getCurrentUser(HttpSession session) {
        UserPrincipal user = (UserPrincipal) session.getAttribute( "user" );
        return (user == null) ? UserPrincipal.ANONYMOUS_USER : user;
    }

    /**
     * Assigns the status message for the console page that will be displayed.
     * 
     * @param statusMessage the status message text
     * @param model the model to which the status message should be applied
     */
    protected void setStatusMessage(String statusMessage, Model model) {
        model.addAttribute( STATUS_MESSAGE, statusMessage );
    }

    /**
     * Assigns the error message for the console page that will be displayed.
     * 
     * @param errorMessage the error message text
     * @param model the model to which the error message should be applied
     */
    protected void setErrorMessage(String errorMessage, Model model) {
        model.addAttribute( ERROR_MESSAGE, errorMessage );
    }

    /**
     * Trims the given string. If the resulting string length is zero, null will be returned.
     * 
     * @param str the string value to be trimmed
     * @return String
     */
    protected String trimString(String str) {
        String result = null;

        if (str != null) {
            result = str.trim();

            if (result.length() == 0) {
                result = null;
            }
        }
        return result;
    }


}

/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemacompiler.console;

import javax.servlet.http.HttpSession;

import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.opentravel.schemacompiler.security.impl.FileAuthenticationProvider;
import org.springframework.ui.Model;

/**
 * Abstract base class of all Spring Web MVC controllers for the OTA2.0 repository
 * console application.
 * 
 * @author S. Livezey
 */
public abstract class BaseController {
	
	private RepositoryManager repositoryManager;
	private RepositorySecurityManager securityManager;
	
	/**
	 * Default constructor.
	 */
	public BaseController() {
		RepositoryComponentFactory componentFactory = RepositoryComponentFactory.getDefault();
		
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
	 * Applies all values to the given model that are common to the entire repository console
	 * application.  If 'targetPage' value will always be returned by this method if a non-null
	 * value is provided.  If null, a default value of "homePage" will be returned.
	 * 
	 * @param model  the model to which the common values should be applied
	 * @param targetPage  the target navigation page for the controller
	 * @return String
	 */
	protected String applyCommonValues(Model model, String targetPage) {
		boolean isDevelopmentRepository = RepositoryComponentFactory.getDefault().isDevelopmentRepository();
		
		model.addAttribute("isLocalUserManagement", isLocalUserManagement());
		model.addAttribute("isDevelopmentRepository", isDevelopmentRepository);
		model.addAttribute("currentPage", targetPage);
		return (targetPage == null) ? "homePage" : targetPage;
	}
	
	/**
	 * Returns true if user accounts are managed locally (false for remote authentication
	 * via JNDI).
	 * 
	 * @return boolean
	 */
	protected boolean isLocalUserManagement() {
		return (RepositoryComponentFactory.getDefault().getAuthenticationProvider() instanceof FileAuthenticationProvider);
	}
	
	/**
	 * Returns the current user associated with the given HTTP session.  If an authenticated user
	 * is not associated with the session, the anonymous user instance will be returned.
	 * 
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @return UserPrincipal
	 */
	protected UserPrincipal getCurrentUser(HttpSession session) {
		UserPrincipal user = (UserPrincipal) session.getAttribute( "user" );
		return (user == null) ? UserPrincipal.ANONYMOUS_USER : user;
	}
	
	/**
	 * Assigns the status message for the console page that will be displayed.
	 * 
	 * @param statusMessage  the status message text
	 * @param model  the model to which the status message should be applied
	 */
	protected void setStatusMessage(String statusMessage, Model model) {
		model.addAttribute( "statusMessage", statusMessage );
	}
	
	/**
	 * Assigns the error message for the console page that will be displayed.
	 * 
	 * @param errorMessage  the error message text
	 * @param model  the model to which the error message should be applied
	 */
	protected void setErrorMessage(String errorMessage, Model model) {
		model.addAttribute( "errorMessage", errorMessage );
	}
	
}

/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.console;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sabre.schemacompiler.model.TLLibraryStatus;
import com.sabre.schemacompiler.repository.RepositoryItem;
import com.sabre.schemacompiler.security.RepositorySecurityManager;
import com.sabre.schemacompiler.security.UserPrincipal;

/**
 * Controller that handles interactions with the view item page(s) of the OTA2.0
 * repository console.
 *
 * @author S. Livezey
 */
@Controller
public class ViewItemController extends BaseController {
	
	private static Log log = LogFactory.getLog(BrowseController.class);
	
	/**
	 * Called by the Spring MVC controller to display the application browse page.
	 * 
	 * @param rootNamespace  the root namespace of the selected library
	 * @param path  the sub-namespace path relative to the base namespace
	 * @param filename  the filename of the selected library to view
	 * @param version  the version of the selected library to view
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/itemDetails.html", "/itemDetails.htm" } )
	public String itemDetails(@RequestParam(value="baseNamespace") String baseNamespace,
			@RequestParam(value="filename") String filename, @RequestParam(value="version") String version,
			HttpSession session, Model model) {
		String targetPage = null;
		try {
			RepositorySecurityManager securityManager = getSecurityManager();
			UserPrincipal user = getCurrentUser( session );
			RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );
			RepositoryPermission requiredPermission = (item.getStatus() == TLLibraryStatus.DRAFT)
					? RepositoryPermission.READ_DRAFT : RepositoryPermission.READ_FINAL;
				
			if (securityManager.isAuthorized(user, item.getNamespace(), requiredPermission)) {
				model.addAttribute("item", item);
				
			} else {
				setErrorMessage( "You are not authorized to view the requested repository item.", model );
				targetPage = new SearchController().searchPage( null, false, false, session, model );
			}
			
		} catch (Throwable t) {
			log.error("An error occured while displaying the repository item.", t);
			setErrorMessage( "An error occured while displaying the repository item (see server log for details).", model );
		}
		
		if (targetPage == null) {
			targetPage = applyCommonValues( model, "itemDetails" );
		}
		return targetPage;
	}
	
}

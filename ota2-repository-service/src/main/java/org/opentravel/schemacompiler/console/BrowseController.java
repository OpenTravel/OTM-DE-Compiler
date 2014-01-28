
package org.opentravel.schemacompiler.console;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.RepositoryNamespaceUtils;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller that handles interactions with the browse page of the OTA2.0 repository console.
 * 
 * @author S. Livezey
 */
@Controller
public class BrowseController extends BaseController {
	
	private static Log log = LogFactory.getLog(BrowseController.class);
	
	/**
	 * Called by the Spring MVC controller to display the application browse page.
	 * 
	 * @param baseNamespace  the root namespace parameter from the URL
	 * @param path  the sub-namespace path relative to the base namespace
	 * @param filename  the filename of the selected library
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/browse.html", "/browse.htm" } )
	public String browsePage(@RequestParam(value="baseNamespace", required=false) String baseNamespace,
			@RequestParam(value="filename", required=false) String filename,
			HttpSession session, Model model) {
		try {
			RepositoryManager repositoryManager = getRepositoryManager();
			RepositorySecurityManager securityManager = getSecurityManager();
			UserPrincipal user = getCurrentUser( session );
			List<NamespaceItem> browseItems = new ArrayList<NamespaceItem>();
			
			if (baseNamespace == null) { // display root namespaces
				for (String rootNS : repositoryManager.listRootNamespaces()) {
					try {
						if (securityManager.isAuthorized(user, rootNS, RepositoryPermission.READ_FINAL)) {
							browseItems.add( new NamespaceItem(rootNS) );
						}
						
					} catch (Throwable t) {
						log.warn("Error determining user access to root namespace: " + rootNS);
					}
				}
			} else if (filename != null) { // display all versions of the selected library
				List<RepositoryItem> allItems = repositoryManager.listItems( baseNamespace, false, true );
				
				for (RepositoryItem item : allItems) {
					if (item.getFilename().equals( filename )) {
						List<RepositoryItem> versionHistory = repositoryManager.getVersionHistory( item );
						
						for (RepositoryItem itemVersion : versionHistory) {
							RepositoryPermission requiredPermission = (itemVersion.getStatus() == TLLibraryStatus.DRAFT)
									? RepositoryPermission.READ_DRAFT : RepositoryPermission.READ_FINAL;
								
							if (securityManager.isAuthorized(user, itemVersion.getNamespace(), requiredPermission)) {
								browseItems.add( new NamespaceItem(itemVersion) );
							}
						}
						model.addAttribute( "libraryName", item.getLibraryName() );
					}
				}
				
			} else { // display sub-namespaces and latest version of each library
				List<String> nsChildren = repositoryManager.listNamespaceChildren( baseNamespace );
				List<RepositoryItem> itemList = repositoryManager.listItems( baseNamespace, true, true );
				
				for (String childPath : nsChildren) {
					String childNS = RepositoryNamespaceUtils.appendChildPath( baseNamespace, childPath );
					
					if (securityManager.isAuthorized(user, childNS, RepositoryPermission.READ_FINAL)) {
						browseItems.add( new NamespaceItem(childNS, childPath) );
					}
				}
				
				for (RepositoryItem item : itemList) {
					RepositoryPermission requiredPermission = (item.getStatus() == TLLibraryStatus.DRAFT)
							? RepositoryPermission.READ_DRAFT : RepositoryPermission.READ_FINAL;
						
					if (securityManager.isAuthorized(user, item.getNamespace(), requiredPermission)) {
						browseItems.add( new NamespaceItem(item) );
					}
				}
			}
			
			if (baseNamespace != null) {
				model.addAttribute( "parentItems", getParentNamespaceItems(baseNamespace) );
				model.addAttribute( "canCreateNamespaceExtension",
						securityManager.isAuthorized( user, baseNamespace, RepositoryPermission.WRITE ) );
				
				if (!repositoryManager.listRootNamespaces().contains(baseNamespace)
						&& repositoryManager.listNamespaceChildren(baseNamespace).isEmpty()
						&& repositoryManager.listItems(baseNamespace, false, false).isEmpty()) {
					String parentNS = RepositoryNamespaceUtils.getParentNamespace( baseNamespace, repositoryManager );
					
					model.addAttribute( "canDeleteNamespace",
							securityManager.isAuthorized( user, parentNS, RepositoryPermission.WRITE ) );
				}
			}
			model.addAttribute("baseNamespace", baseNamespace);
			model.addAttribute("filename", filename);
			model.addAttribute("browseItems", browseItems);
			
		} catch (Throwable t) {
			log.error("An error occured while displaying the browse page.", t);
			setErrorMessage( "An error occured while displaying the page (see server log for details).", model );
		}
		return applyCommonValues( model, "browse" );
	}
	
	/**
	 * Called by the Spring MVC controller to create a new child namespace URI for the repository.
	 * 
	 * @param baseNamespace  the base namespace from which the extension should be created
	 * @param nsExtension  the child namespace extension to create from the base
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/createNamespace.html", "/createNamespace.htm" } )
	public String createNamespace(@RequestParam(value="baseNamespace", required=true) String baseNamespace,
			@RequestParam(value="nsExtension", required=false) String nsExtension, HttpSession session, Model model) {
		String targetPage = null;
		
		if ((nsExtension != null) && (nsExtension.length() > 0)) {
			String newNS = RepositoryNamespaceUtils.appendChildPath( baseNamespace, nsExtension );
			try {
				getRepositoryManager().createNamespace( newNS );
				setStatusMessage( "Namespace created successfully.", model );
				targetPage = browsePage( baseNamespace, null, session, model );
				
			} catch (RepositoryException e) {
				log.error("Error creating namespace: " + newNS, e);
				setErrorMessage( e.getMessage(), model );
			}
		}
		
		if (targetPage == null) {
			model.addAttribute( "baseNamespace", baseNamespace );
			model.addAttribute( "nsExtension", nsExtension );
			targetPage = applyCommonValues( model, "createNamespace" );
		}
		return targetPage;
	}
	
	/**
	 * Called by the Spring MVC controller to create a new child namespace URI for the repository.
	 * 
	 * @param baseNamespace  the base namespace from which the extension should be created
	 * @param nsExtension  the child namespace extension to create from the base
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/deleteNamespace.html", "/deleteNamespace.htm" } )
	public String deleteNamespace(@RequestParam(value="baseNamespace", required=true) String baseNamespace,
			@RequestParam(value="confirmDelete", required=false) boolean confirmDelete, HttpSession session, Model model) {
		String targetPage = null;
		
		if (confirmDelete) {
			try {
				String parentNS = RepositoryNamespaceUtils.getParentNamespace( baseNamespace, getRepositoryManager() );
				
				getRepositoryManager().deleteNamespace( baseNamespace );
				setStatusMessage( "Namespace deleted successfully.", model );
				targetPage = browsePage( parentNS, null, session, model );
				
			} catch (RepositoryException e) {
				log.error("Error deleting namespace: " + baseNamespace, e);
				setErrorMessage( e.getMessage(), model );
			}
		}
		
		if (targetPage == null) {
			model.addAttribute( "baseNamespace", baseNamespace );
			targetPage = applyCommonValues( model, "deleteNamespace" );
		}
		return targetPage;
	}
	
	/**
	 * Returns the list of namespace items that represent the relative paths above the one
	 * specified.  The list of items is sorted from the root namespace to the lowest level child
	 * above the one provided.
	 * 
	 * @param ns  the namespace URI for which to construct parent namespace items
	 * @return List<NamespaceItem>
	 * @throws RepositoryException  thrown if the repository's root namespaces cannot be accessed or the
	 *								given namespace URI is not part of the base namespace hierarchy from
	 *								the associated repository
	 */
	private List<NamespaceItem> getParentNamespaceItems(String ns) throws RepositoryException {
		try {
			String rootNS = RepositoryNamespaceUtils.getRootNamespace( ns, getRepositoryManager() );
			String pathFromRoot = rootNS.equals(ns) ? null : ns.substring( rootNS.length() + 1 );
			List<NamespaceItem> parentItems = new ArrayList<NamespaceItem>();
			StringBuilder nsPath = new StringBuilder( rootNS );
			
			parentItems.add( new NamespaceItem( rootNS ) );
			
			if ((pathFromRoot != null) && (pathFromRoot.length() > 0)) {
				for (String pathPart : pathFromRoot.split("/")) {
					if (pathPart.length() == 0) continue;
					nsPath.append( "/" ).append( pathPart );
					parentItems.add( new NamespaceItem( nsPath.toString(), pathPart ) );
				}
			}
			return parentItems;
			
		} catch (IllegalArgumentException e) {
			throw new RepositoryException( e.getMessage(), e );
		}
	}
	
}

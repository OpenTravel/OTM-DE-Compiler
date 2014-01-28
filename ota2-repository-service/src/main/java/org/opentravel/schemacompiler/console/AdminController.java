/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemacompiler.console;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.ns.ota2.security_v01_00.NamespaceAuthorizations;
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.index.FreeTextSearchService;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.security.AuthorizationResource;
import org.opentravel.schemacompiler.security.GroupAssignmentsResource;
import org.opentravel.schemacompiler.security.RepositorySecurityException;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserGroup;
import org.opentravel.schemacompiler.security.UserPrincipal;
import org.opentravel.schemacompiler.security.impl.FileAuthenticationProvider;
import org.opentravel.schemacompiler.security.impl.SecurityFileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller that handles interactions with the admin page of the OTA2.0 repository console.
 * 
 * @author S. Livezey
 */
@Controller
public class AdminController extends BaseController {
	
	private static Log log = LogFactory.getLog(AdminController.class);
	
	private FreeTextSearchService searchService;
	
	/**
	 * Default constructor.
	 */
	public AdminController() {
		try {
			FreeTextSearchService.initializeSingleton(
					RepositoryComponentFactory.getDefault().getSearchIndexLocation(), getRepositoryManager() );
			searchService = FreeTextSearchService.getInstance();
			
		} catch (Throwable t) {
			log.error("Error initializing the free-text search service.", t);
		}
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page.
	 * 
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminHome.html", "/adminHome.htm" } )
	public String adminHomePage(HttpSession session, Model model) {
		return applyCommonValues( session, model, "adminHome" );
	}
	
	/**
	 * Called by the Spring MVC controller to display and edit the display name of the repository.
	 * 
	 * @param displayName  the updated display name for the repository
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminChangeRepositoryName.html", "/adminChangeRepositoryName.htm" } )
	public String adminChangeRepositoryName(@RequestParam(value="displayName", required=false) String displayName,
			HttpSession session, Model model) {
		RepositoryManager repositoryManager = getRepositoryManager();
		String targetPage = null;
		
		if ((displayName != null) && (displayName.length() > 0)) {
			String repositoryId = repositoryManager.getId();
			
			try {
				repositoryManager.updateLocalRepositoryIdentity( repositoryId, displayName );
				setStatusMessage( "Repository display name updated successfully.", model );
				targetPage = applyCommonValues( session, model, "adminHome" );
				
			} catch (RepositoryException e) {
				setErrorMessage("Error updating the repository's display name (see server log for details).", model);
				log.error("Error updating the repository's display name.", e);
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
	 * @param rootNamespace  the name of the new root namespace to create or delete
	 * @param formAction  indicates whether the root namespace is to be created or deleted (null for first-time display)
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminManageRootNamespaces.html", "/adminManageRootNamespaces.htm" } )
	public String adminManageRootNamespaces(@RequestParam(value="rootNamespace", required=false) String rootNamespace,
			@RequestParam(value="action", required=false) String formAction, HttpSession session, Model model) {
		String targetPage = null;
		try {
			UserPrincipal user = getCurrentUser( session );
			
			if ((formAction == null) || (formAction.length() == 0)) {
				// No action required for first-time display of the page
				
			} else if (formAction.equals("create")) {
				if (!getSecurityManager().isAdministrator(user)) {
					throw new RepositorySecurityException("You are not authorized to create a new root namespace.");
				}
				getRepositoryManager().createRootNamespace( rootNamespace );
				setStatusMessage("Root namespace created successfully.", model);
				targetPage = applyCommonValues( session, model, "adminHome" );
				
			} else if (formAction.equals("delete")) {
				if (!getSecurityManager().isAdministrator(user)) {
					throw new RepositorySecurityException("You are not authorized to delete a root namespace.");
				}
				getRepositoryManager().deleteRootNamespace( rootNamespace );
				setStatusMessage("Root namespace deleted successfully.", model);
				targetPage = applyCommonValues( session, model, "adminHome" );
				
			} else {
				throw new RepositoryException("Unrecognized form action: " + formAction);
			}
		} catch (Exception e) {
			log.error("Error managing root namespaces.", e);
			setErrorMessage( e.getMessage(), model );
			model.addAttribute( "newRootNamespace", rootNamespace );
		}
		
		if (targetPage == null) {
			List<NamespaceItem> rootNamespaceItems = new ArrayList<NamespaceItem>();
			
			try {
				for (String rootNS : getRepositoryManager().listRootNamespaces()) {
					NamespaceItem nsItem = new NamespaceItem(rootNS);
					
					nsItem.setCanDelete( getRepositoryManager().listNamespaceChildren(rootNS).isEmpty() );
					rootNamespaceItems.add( nsItem );
				}
			} catch (RepositoryException e) {
				log.error("Error retrieving root namespaces.", e);
			}
			model.addAttribute( "rootNamespaceItems", rootNamespaceItems );
			targetPage = applyCommonValues( session, model, "adminManageRootNamespaces" );
		}
		return targetPage;
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to manage namespace permissions.
	 * 
	 * @param baseNamespace  the base namespace for which permissions should be displayed
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminPermissions.html", "/adminPermissions.htm" } )
	public String adminPermissionsPage(@RequestParam(value="namespace", required=false) String baseNamespace,
			HttpSession session, Model model) {
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
			log.error("Error displaying security permissions.", e);
			setErrorMessage( "Error displaying security permissions (see server log for details).", model );
			
		} finally {
			model.addAttribute( "baseNamespace", baseNamespace );
		}
		return applyCommonValues( session, model, "adminPermissions" );
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to edit namespace permissions.
	 * 
	 * @param baseNamespace  the base namespace for which permissions should be edited
	 * @param processForm  flag indicating whether updates have been submitted by the
	 *					   user (if false, page is being displayed for the first time)
	 * @param permissions  the list of update permissions that were submitted by the user
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminPermissionsEdit.html", "/adminPermissionsEdit.htm" } )
	public String adminPermissionsEditPage(@RequestParam(value="namespace", required=true) String baseNamespace,
			@RequestParam(value="processForm", required=false) boolean processForm,
			@ModelAttribute("permissions") NamespacePermissions permissions, HttpSession session, Model model) {
		List<String> permissionOptions = new ArrayList<String>();
		String targetPage = null;
		try {
			if ((baseNamespace != null) && (baseNamespace.length() == 0)) {
				baseNamespace = null;
			}
			
			if (processForm) {
				SecurityFileUtils fileUtils = new SecurityFileUtils( getRepositoryManager() );
				AuthorizationResource authResource = new AuthorizationResource( fileUtils, baseNamespace );
				
				authResource.saveNamespaceAuthorizations( permissions.toJaxbAuthorizations() );
				targetPage = adminPermissionsPage( baseNamespace, session, model );
				
			} else { // displaying the page for the first time
				SecurityFileUtils fileUtils = new SecurityFileUtils( getRepositoryManager() );
				File authorizationFile = fileUtils.getAuthorizationFile( baseNamespace );
				NamespaceAuthorizations jaxbAuthorizations = null;
				
				if (authorizationFile.exists()) {
					jaxbAuthorizations = fileUtils.loadNamespaceAuthorizations( authorizationFile );
				}
				permissions = new NamespacePermissions( baseNamespace, jaxbAuthorizations );
				permissions.createGroupPermissions( Arrays.asList(
						new GroupAssignmentsResource( getRepositoryManager() ).getGroupNames()
					));
			}
			
		} catch (Exception e) {
			log.error("Error displaying or updating security permissions.", e);
			setErrorMessage( "Error displaying or updating security permissions (see server log for details).", model );
		}
		
		if (targetPage == null) {
			for (RepositoryPermission permissionOption : RepositoryPermission.values()) {
				permissionOptions.add( permissionOption.toString() );
			}
			model.addAttribute( "baseNamespace", baseNamespace );
			model.addAttribute( "permissions", permissions );
			model.addAttribute( "permissionOptions", permissionOptions );
			targetPage = applyCommonValues( session, model, "adminPermissionsEdit" );
		}
		return targetPage;
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to test namespace permissions.
	 * 
	 * @param baseNamespace  the base namespace for which permissions should be tested
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminPermissionsTest.html", "/adminPermissionsTest.htm" } )
	public String adminPermissionsTestPage(@RequestParam(value="namespace", required=false) String baseNamespace,
			HttpSession session, Model model) {
		if ((baseNamespace != null) && (baseNamespace.length() == 0)) {
			baseNamespace = null;
		}
		
		model.addAttribute( "baseNamespace", baseNamespace );
		return applyCommonValues( session, model, "adminPermissionsTest" );
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to manage local user accounts.
	 * 
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminUsers.html", "/adminUsers.htm" } )
	public String adminUsersPage(HttpSession session, Model model) {
		String[] userAccounts = FileAuthenticationProvider.getAllUserIds( getRepositoryManager().getRepositoryLocation() );
		
		model.addAttribute( "userAccounts", userAccounts );
		return applyCommonValues( session, model, "adminUsers" );
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to manage local user accounts.
	 * 
	 * @param userId  the ID of the user account to add
	 * @param password  the password of the user to add
	 * @param passwordConfirm  the confirmation of the user's password
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( value = { "/adminUsersAdd.html", "/adminUsersAdd.htm" } )
	public String adminUsersAddPage(@RequestParam(value="userId", required=false) String userId,
			@RequestParam(value="password", required=false) String password,
			@RequestParam(value="passwordConfirm", required=false) String passwordConfirm,
			HttpSession session, Model model) {
		boolean success = false;
		try {
			if (userId != null) {
				if ((password == null) || (password.length() == 0)) {
					setErrorMessage( "The password is a required value.", model );
					
				} else if (password.indexOf(' ') >= 0) {
					setErrorMessage( "White space characters are not permitted in passwords.", model );
					
				} else if (!password.equals(passwordConfirm)) {
					setErrorMessage( "The passwords do not match.", model );
					
				} else { // everything is ok - change the password
					RepositoryFileManager fileManager = getRepositoryManager().getFileManager();
					
					FileAuthenticationProvider.saveUserCredentials( userId, password, false, fileManager );
					setStatusMessage( "User '" + userId + "' created successfully.", model );
					success = true;
				}
			}
		} catch (IOException e) {
			setErrorMessage( "Unable to create user account: " + userId, model );
			log.error("Unable to create user account: " + userId, e);
		}
		return success ? adminUsersPage( session, model ) : "adminUsersAdd";
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to manage local user accounts.
	 * 
	 * @param userId  the ID of the user account to add
	 * @param password  the password of the user to add
	 * @param confirmDelete  flag indicating that the user has confirmed the deletion
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( value = { "/adminUsersDelete.html", "/adminUsersDelete.htm" } )
	public String adminUsersDeletePage(@RequestParam(value="userId", required=true) String userId,
			@RequestParam(value="confirmDelete", required=false) boolean confirmDelete,
			HttpSession session, Model model) {
		try {
			if (confirmDelete) {
				RepositoryFileManager fileManager = getRepositoryManager().getFileManager();
				
				FileAuthenticationProvider.saveUserCredentials( userId, null, true, fileManager );
				setStatusMessage( "User '" + userId + "' deleted successfully.", model );
				
			} else {
				model.addAttribute("userId", userId);
			}
			
		} catch (IOException e) {
			setErrorMessage( "Unable to delete user account: " + userId, model );
			log.error("Unable to delete user account: " + userId, e);
		}
		return confirmDelete ? adminUsersPage( session, model ) : "adminUsersDelete";
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to manage local user accounts.
	 * 
	 * @param userId  the ID of the user account to add
	 * @param newPassword  the new password for the user
	 * @param newPasswordConfirm  the confirmation of the new password for the user
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( value = { "/adminUsersChangePassword.html", "/adminUsersChangePassword.htm" } )
	public String adminUsersChangePasswordPage(@RequestParam(value="userId", required=true) String userId,
			@RequestParam(value="newPassword", required=false) String newPassword,
			@RequestParam(value="newPasswordConfirm", required=false) String newPasswordConfirm,
			HttpSession session, Model model) {
		boolean success = false;
		try {
			if ((newPassword == null) && (newPasswordConfirm == null)) {
				// no action - initial display of the form
				
			} else {
				if ((newPassword == null) || (newPassword.length() == 0)) {
					setErrorMessage( "The new password is a required value.", model );
					
				} else if (newPassword.indexOf(' ') >= 0) {
					setErrorMessage( "White space characters are not permitted in passwords.", model );
					
				} else if (!newPassword.equals(newPasswordConfirm)) {
					setErrorMessage( "The passwords do not match.", model );
					
				} else { // everything is ok - change the password
					RepositoryFileManager fileManager = getRepositoryManager().getFileManager();
					
					FileAuthenticationProvider.saveUserCredentials( userId, newPassword, false, fileManager );
					setStatusMessage( "The password for '" + userId + "' was changed successfully.", model );
					success = true;
				}
			}
			model.addAttribute("userId", userId);
			
		} catch (IOException e) {
			setErrorMessage( "Unable to change password for user: " + userId, model );
			log.error("Unable to change password for user: " + userId, e);
		}
		return success ? adminUsersPage( session, model ) : "adminUsersChangePassword";
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to manage group assignments for users.
	 * 
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminGroups.html", "/adminGroups.htm" } )
	public String adminGroupsPage(HttpSession session, Model model) {
		try {
			RepositorySecurityManager securityManager = RepositoryComponentFactory.getDefault().getSecurityManager();
			List<UserGroup> allGroups = new ArrayList<UserGroup>();
			
			for (String groupName : securityManager.getGroupNames()) {
				allGroups.add( securityManager.getGroup(groupName) );
			}
			model.addAttribute( "allGroups", allGroups );
			
		} catch (RepositoryException e) {
			log.error("Error displaying group assignments.", e);
			setErrorMessage( "Error displaying group assignments (see server log for details).", model );
		}
		return applyCommonValues( session, model, "adminGroups" );
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to add a new user group.
	 * 
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminGroupsAdd.html", "/adminGroupsAdd.htm" } )
	public String adminGroupsAddPage(@RequestParam(value="groupName", required=false) String groupName,
			HttpSession session, Model model) {
		String targetPage = null;
		
		if (groupName != null) {
			try {
				GroupAssignmentsResource groupsResource = new GroupAssignmentsResource( getRepositoryManager() );
				List<UserGroup> groupList = new ArrayList<UserGroup>();
				List<String> groupNames = Arrays.asList( groupsResource.getGroupNames() );
				
				if (groupName.indexOf(' ') >= 0) {
					setErrorMessage( "White space characters are not allowed in group names.", model );
					
				} else if (groupNames.contains( groupName )) {
					setErrorMessage( "A group named '" + groupName + "' already exists.", model );
					
				} else {
					UserGroup newGroup = new UserGroup( groupName, new ArrayList<String>() );
					
					for (String existingGroupName : groupNames) {
						String[] groupMembers = groupsResource.getAssignedUsers( existingGroupName );
						groupList.add( new UserGroup(existingGroupName, Arrays.asList(groupMembers)) );
					}
					groupList.add( newGroup );
					groupsResource.saveGroupAssignments( groupList );
					setStatusMessage( "Group '" + groupName + "' created successfully.", model );
					targetPage = adminGroupsPage( session, model );
				}
				
			} catch (RepositoryException e) {
				log.error("Error creating user group.", e);
				setErrorMessage( "Error creating user group (see server log for details).", model );
			}
		}
		if (targetPage == null) {
			targetPage = applyCommonValues( session, model, "adminGroupsAdd" );
		}
		return targetPage;
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to delete a user group.
	 * 
	 * @param groupName  the ID of the group to be edited
	 * @param confirmDelete  flag indicating that the user has confirmed the deletion
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminGroupsDelete.html", "/adminGroupsDelete.htm" } )
	public String adminGroupsDeletePage(@RequestParam(value="groupName", required=true) String groupName,
			@RequestParam(value="confirmDelete", required=false) boolean confirmDelete,
			HttpSession session, Model model) {
		String targetPage = null;
		
		if (confirmDelete) {
			try {
				GroupAssignmentsResource groupsResource = new GroupAssignmentsResource( getRepositoryManager() );
				List<UserGroup> groupList = new ArrayList<UserGroup>();
				List<String> groupNames = Arrays.asList( groupsResource.getGroupNames() );
				
				for (String existingGroupName : groupNames) {
					if (existingGroupName.equals( groupName )) {
						continue;
					}
					String[] groupMembers = groupsResource.getAssignedUsers( existingGroupName );
					groupList.add( new UserGroup(existingGroupName, Arrays.asList(groupMembers)) );
				}
				groupsResource.saveGroupAssignments( groupList );
				setStatusMessage( "Group '" + groupName + "' deleted successfully.", model );
				targetPage = adminGroupsPage( session, model );
				
			} catch (RepositoryException e) {
				log.error("Error deleting user group.", e);
				setErrorMessage( "Error deleting user group (see server log for details).", model );
			}
			
		} else {
			model.addAttribute("groupName", groupName);
		}
		
		if (targetPage == null) {
			targetPage = applyCommonValues( session, model, "adminGroupsDelete" );
		}
		return targetPage;
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to edit the members of a group
	 * 
	 * @param groupName  the ID of the group to be edited
	 * @param groupMembers  the updated list of user ID's for the group members
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminGroupsEdit.html", "/adminGroupsEdit.htm" } )
	public String adminGroupsEditPage(@RequestParam(value="groupName", required=true) String groupName,
			@RequestParam(value="groupMembers", required=false) String groupMembers,
			HttpSession session, Model model) {
		GroupAssignmentsResource groupsResource = new GroupAssignmentsResource( getRepositoryManager() );
		String targetPage = null;
		
		if (groupMembers != null) {
			try {
				List<String> groupNames = Arrays.asList( groupsResource.getGroupNames() );
				List<UserGroup> groupList = new ArrayList<UserGroup>();
				
				for (String existingGroupName : groupNames) {
					if (existingGroupName.equals( groupName )) {
						List<String> memberIds = new ArrayList<String>();
						
						for (String memberId : groupMembers.split(",")) {
							if ((memberId != null) && (memberId.length() > 0)) {
								memberIds.add( memberId );
							}
						}
						groupList.add( new UserGroup(groupName, memberIds) );
						
					} else {
						groupList.add( new UserGroup(existingGroupName,
								Arrays.asList(groupsResource.getAssignedUsers( existingGroupName ))) );
					}
				}
				groupsResource.saveGroupAssignments( groupList );
				setStatusMessage( "Group '" + groupName + "' updated successfully.", model );
				targetPage = adminGroupsPage( session, model );
				
			} catch (RepositoryException e) {
				log.error("Error displaying or updating group assignments.", e);
				setErrorMessage( "Error displaying or updating group assignments (see server log for details).", model );
			}
		}
		if (targetPage == null) {
			if (groupMembers == null) {
				StringBuilder memberIds = new StringBuilder();
				
				for (String userId : groupsResource.getAssignedUsers(groupName)) {
					memberIds.append( userId ).append( "," );
				}
				groupMembers = memberIds.toString();
			}
			model.addAttribute( "groupName", groupName );
			model.addAttribute( "knownUsers", getKnownUserIds() );
			model.addAttribute( "groupMembers", groupMembers );
			targetPage = applyCommonValues( session, model, "adminGroupsEdit" );
		}
		return targetPage;
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to reindex the free-text search folder.
	 * 
	 * @param confirmReindexing  flag indicating that the user has confirmed the reindexing task
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminSearchIndex.html", "/adminSearchIndex.htm" } )
	public String adminSearchIndexPage(@RequestParam(value="confirmReindexing", required=false) boolean confirmReindexing,
			HttpSession session, Model model) {
		if (confirmReindexing) {
			try {
				if (searchService == null) {
					throw new RepositoryException("The search indexing service is not available.");
				}
				searchService.indexAllRepositoryItems();
				
			} catch (RepositoryException e) {
				log.error("Error displaying or updating group assignments.", e);
				setErrorMessage( e.getMessage(), model );
				
			} finally {
				model.addAttribute("confirmReindexing", confirmReindexing);
			}
		}
		return applyCommonValues( session, model, "adminSearchIndex" );
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to delete an item from the repository.
	 * 
	 * @param baseNamespace  the base namespace of the repository item to be deleted
	 * @param filename  the filename of the repository item to be deleted
	 * @param version  the version of the repository item to be deleted
	 * @param confirmDeletion  flag indicating that the user has confirmed the deletion task
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminDeleteItem.html", "/adminDeleteItem.htm" } )
	public String adminDeleteItemPage(@RequestParam(value="baseNamespace", required=true) String baseNamespace,
			@RequestParam(value="filename", required=true) String filename,
			@RequestParam(value="version", required=true) String version,
			@RequestParam(value="confirmDelete", required=false) boolean confirmDeletion,
			HttpSession session, Model model) {
		String targetPage = null;
		
		if (confirmDeletion) {
			try {
				UserPrincipal user = getCurrentUser( session );
				
				if (getSecurityManager().isAdministrator(user)) {
					RepositoryManager repositoryManager = getRepositoryManager();
					RepositoryItem item = repositoryManager.getRepositoryItem( baseNamespace, filename, version );
					
					repositoryManager.delete( item );
					searchService.deleteRepositoryItemIndex( item );
					
					setStatusMessage( "Repository item deleted successfully: " + filename, model );
					
				} else {
					setErrorMessage( "You do not have permission to delete the repository item.", model );
					targetPage = new ViewItemController().itemDetails( baseNamespace, filename, version, session, model );
				}
			} catch (Exception e) {
				log.error("Unable to delete the repository item.", e);
				setErrorMessage( "Unable to delete the repository item (see server log for details).", model );
				
			} finally {
				if (targetPage == null) {
					targetPage = new BrowseController().browsePage( baseNamespace, null, session, model );
				}
			}
		}
		if (targetPage == null) {
			try {
				RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );
				
				model.addAttribute( "item", item );
				targetPage = applyCommonValues( session, model, "adminDeleteItem" );
				
			} catch (RepositoryException e) {
				log.error("The requested repository item does not exist.", e);
				setErrorMessage("The requested repository item does not exist.", model);
				targetPage = new BrowseController().browsePage( baseNamespace, null, session, model );
			}
		}
		return targetPage;
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to promote a repository item to FINAL state.
	 * 
	 * @param baseNamespace  the base namespace of the repository item to be promoted
	 * @param filename  the filename of the repository item to be promoted
	 * @param version  the version of the repository item to be promoted
	 * @param confirmPromote  flag indicating that the user has confirmed the promotion task
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminPromoteItem.html", "/adminPromoteItem.htm" } )
	public String adminPromoteItemPage(@RequestParam(value="baseNamespace", required=true) String baseNamespace,
			@RequestParam(value="filename", required=true) String filename,
			@RequestParam(value="version", required=true) String version,
			@RequestParam(value="confirmPromote", required=false) boolean confirmPromote,
			HttpSession session, Model model) {
		String targetPage = null;
		
		if (confirmPromote) {
			try {
				UserPrincipal user = getCurrentUser( session );
				
				if (getSecurityManager().isAdministrator(user)) {
					RepositoryManager repositoryManager = getRepositoryManager();
					RepositoryItem item = repositoryManager.getRepositoryItem( baseNamespace, filename, version );
					
					repositoryManager.promote( item );
					searchService.indexRepositoryItem( item );
					
					setStatusMessage( "Repository item promoted successfully: " + filename, model );
					
				} else {
					setErrorMessage( "You do not have permission to promote the repository item.", model );
				}
			} catch (Exception e) {
				log.error("Unable to promote the repository item.", e);
				setErrorMessage( "Unable to promote the repository item (see server log for details).", model );
				
			} finally {
				targetPage = new ViewItemController().itemDetails( baseNamespace, filename, version, session, model );
			}
		}
		if (targetPage == null) {
			try {
				RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );
				
				model.addAttribute( "item", item );
				targetPage = applyCommonValues( session, model, "adminPromoteItem" );
				
			} catch (RepositoryException e) {
				log.error("The requested repository item does not exist.", e);
				setErrorMessage("The requested repository item does not exist.", model);
				targetPage = new BrowseController().browsePage( baseNamespace, null, session, model );
			}
		}
		return targetPage;
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to demote a repository item to DRAFT state.
	 * 
	 * @param baseNamespace  the base namespace of the repository item to be demoted
	 * @param filename  the filename of the repository item to be demoted
	 * @param version  the version of the repository item to be demoted
	 * @param confirmDemote  flag indicating that the user has confirmed the demotion task
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminDemoteItem.html", "/adminDemoteItem.htm" } )
	public String adminDemoteItemPage(@RequestParam(value="baseNamespace", required=true) String baseNamespace,
			@RequestParam(value="filename", required=true) String filename,
			@RequestParam(value="version", required=true) String version,
			@RequestParam(value="confirmDemote", required=false) boolean confirmDemote,
			HttpSession session, Model model) {
		String targetPage = null;
		
		if (confirmDemote) {
			try {
				UserPrincipal user = getCurrentUser( session );
				
				if (getSecurityManager().isAdministrator(user)) {
					RepositoryManager repositoryManager = getRepositoryManager();
					RepositoryItem item = repositoryManager.getRepositoryItem( baseNamespace, filename, version );
					
					repositoryManager.demote( item );
					searchService.indexRepositoryItem( item );
					
					setStatusMessage( "Repository item demoted successfully: " + filename, model );
					
				} else {
					setErrorMessage( "You do not have permission to demote the repository item.", model );
				}
				
			} catch (Exception e) {
				log.error("Unable to demote the repository item.", e);
				setErrorMessage( "Unable to demote the repository item (see server log for details).", model );
				
			} finally {
				targetPage = new ViewItemController().itemDetails( baseNamespace, filename, version, session, model );
			}
		}
		if (targetPage == null) {
			try {
				RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );
				
				model.addAttribute( "item", item );
				targetPage = applyCommonValues( session, model, "adminDemoteItem" );
				
			} catch (RepositoryException e) {
				log.error("The requested repository item does not exist.", e);
				setErrorMessage("The requested repository item does not exist.", model);
				targetPage = new BrowseController().browsePage( baseNamespace, null, session, model );
			}
		}
		return targetPage;
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to recalculate the CRC of a repository item that is in the final state.
	 * 
	 * @param baseNamespace  the base namespace of the repository item whose CRC is to be recalculated
	 * @param filename  the filename of the repository item whose CRC is to be recalculated
	 * @param version  the version of the repository item whose CRC is to be recalculated
	 * @param confirmRecalculate  flag indicating that the user has confirmed the recalculation task
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminRecalculateItemCrc.html", "/adminRecalculateItemCrc.htm" } )
	public String adminRecalculateItemCrcPage(@RequestParam(value="baseNamespace", required=true) String baseNamespace,
			@RequestParam(value="filename", required=true) String filename,
			@RequestParam(value="version", required=true) String version,
			@RequestParam(value="confirmRecalculate", required=false) boolean confirmRecalculate,
			HttpSession session, Model model) {
		String targetPage = null;
		
		if (confirmRecalculate) {
			try {
				UserPrincipal user = getCurrentUser( session );
				
				if (getSecurityManager().isAdministrator(user)) {
					RepositoryManager repositoryManager = getRepositoryManager();
					RepositoryItem item = repositoryManager.getRepositoryItem( baseNamespace, filename, version );
					
					if (item.getStatus() != TLLibraryStatus.FINAL) {
						setErrorMessage( "Only repository items in FINAL status are assigned a CRC value.", model );
						
					} else {
						repositoryManager.recalculateCrc( item );
						searchService.indexRepositoryItem( item );
						
						setStatusMessage( "Repository item CRC recalculated successfully: " + filename, model );
					}
					targetPage = new ViewItemController().itemDetails( baseNamespace, filename, version, session, model );
					
				} else {
					setErrorMessage( "You do not have permission to recalculate the repository item's CRC.", model );
					targetPage = new ViewItemController().itemDetails( baseNamespace, filename, version, session, model );
				}
			} catch (Exception e) {
				log.error("Unable to recalculate the repository item's CRC.", e);
				setErrorMessage( "Unable to recalculate the repository item's CRC (see server log for details).", model );
				
			} finally {
				if (targetPage == null) {
					targetPage = new BrowseController().browsePage( baseNamespace, null, session, model );
				}
			}
		}
		if (targetPage == null) {
			try {
				RepositoryItem item = getRepositoryManager().getRepositoryItem( baseNamespace, filename, version );
				
				model.addAttribute( "item", item );
				targetPage = applyCommonValues( session, model, "adminRecalculateItemCrc" );
				
			} catch (RepositoryException e) {
				log.error("The requested repository item does not exist.", e);
				setErrorMessage("The requested repository item does not exist.", model);
				targetPage = new BrowseController().browsePage( baseNamespace, null, session, model );
			}
		}
		return targetPage;
	}
	
	/**
	 * Called by the Spring MVC controller to display the application administration page
	 * used to delete the contents of the repository.
	 * 
	 * @param confirmDeletion  flag indicating that the user has confirmed the deletion task
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model context to be used when rendering the page view
	 * @return String
	 */
	@RequestMapping( { "/adminDeleteRepository.html", "/adminDeleteRepository.htm" } )
	public String adminDeleteRepositoryPage(@RequestParam(value="confirmDeletion", required=false) boolean confirmDeletion,
			HttpSession session, Model model) {
		if (confirmDeletion) {
			try {
				deleteFolderContents( getRepositoryManager().getRepositoryLocation() );
				
				if (searchService != null) {
					searchService.indexAllRepositoryItems();
				}
			} catch (RepositoryException e) {
				log.error("Error deleting contents of the development repository.", e);
				setErrorMessage( e.getMessage(), model );
				
			} finally {
				model.addAttribute("confirmDeletion", confirmDeletion);
			}
		}
		return applyCommonValues( session, model, "adminDeleteRepository" );
	}
	
	/**
	 * Returns the list of all user ID's that are known locally to the repository.  If user accounts
	 * are managed locally, the local list is returned.  If user authentication is performed using JNDI,
	 * the list of user ID's is the union of all users assigned to one or more repository groups.
	 * 
	 * @return List<String>
	 */
	protected List<String> getKnownUserIds() {
		List<String> userIds = new ArrayList<String>();
		
		if (isLocalUserManagement()) {
			String[] localUserIds = FileAuthenticationProvider.getAllUserIds( getRepositoryManager().getRepositoryLocation() );
			
			for (String userId : localUserIds) {
				userIds.add( userId );
			}
		} else {  // collect the union of all known users from the local group assignments
			GroupAssignmentsResource groupsResource = new GroupAssignmentsResource( getRepositoryManager() );
			
			for (String groupName : groupsResource.getGroupNames()) {
				for (String userId : groupsResource.getAssignedUsers(groupName)) {
					if (!userIds.contains(userId)) {
						userIds.add( userId );
					}
				}
			}
		}
		Collections.sort( userIds );
		return userIds;
	}
	
	/**
	 * Applies all required common values to the model.  If the user does not have administrative rights,
	 * the target page will be changed to the home page of the repository instead of the requested
	 * admin page.
	 * 
	 * @param session  the HTTP session that contains information about an authenticated user
	 * @param model  the model to which the common values should be applied
	 * @param targetPage  the target navigation page for the controller
	 * @return String
	 */
	protected String applyCommonValues(HttpSession session, Model model, String targetPage) {
		String result = applyCommonValues( model, targetPage );
		Boolean isAdministrator = (Boolean) session.getAttribute("isAdminAuthorized");
		
		if ((isAdministrator == null) || !isAdministrator.booleanValue()) {
			result = "homePage";
		}
		return result;
	}
	
	/**
	 * Recursively deletes the contents of the specified folder.
	 * 
	 * @param folderItem  the repository folder/file to be deleted
	 */
	private void deleteFolderContents(File folderItem) {
		if (folderItem.isDirectory()) {
			for (File subFolderItem : folderItem.listFiles()) {
				deleteFolderContents(subFolderItem);
			}
			folderItem.delete(); // attempt to remove folder - may fail
			
		} else {
			String filename = folderItem.getName().toLowerCase();
			
			if (filename.endsWith(".otm") || filename.endsWith(".xsd")
					|| filename.endsWith("-info.xml")) {
				folderItem.delete();
			}
		}
	}
	
}

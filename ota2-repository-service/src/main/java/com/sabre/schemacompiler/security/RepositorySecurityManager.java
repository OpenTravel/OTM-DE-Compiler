/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.security;

import java.util.List;

import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;

/**
 * Provides all authentication and authorization services required for secure operation of the
 * OTA2.0 repository.
 * 
 * @author S. Livezey
 */
public interface RepositorySecurityManager {
	
	public static final String ADMINISTRATORS_GROUP_NAME = "Administrators";
	
	/**
	 * Returns the user principal indicated by the HTTP authorization header provided.  If the header
	 * value is null, the anonymous user will be returned.  If the user fails authentication, a
	 * <code>RepositorySecurityException</code> will be thrown.
	 * 
	 * @param authorizationHeader  the HTTP authorization header value
	 * @return UserPrincipal
	 * @throws RepositorySecurityException  thrown if the user's ID and password do not pass authentication
	 */
	public UserPrincipal getUser(String authorizationHeader) throws RepositorySecurityException;
	
	/**
	 * Returns the user principal indicated by the user ID and password credentials provided.  If the
	 * header value is null, the anonymous user will be returned.  If the user fails authentication, a
	 * <code>RepositorySecurityException</code> will be thrown.
	 * 
	 * @param userId  the ID of the user to be authenticated
	 * @param password  the password credentials to be authenticated
	 * @return UserPrincipal
	 * @throws RepositorySecurityException  thrown if the user's ID and password do not pass authentication
	 */
	public UserPrincipal getUser(String userId, String password) throws RepositorySecurityException;
	
	/**
	 * Returns true if the user is authorized to perform the specified operation within the requested
	 * namespace.
	 * 
	 * @param user  the user for which authorization is being requested
	 * @param namespace  the namespace to which the user is requesting access
	 * @param permission  the permission/action that is being requested by the user
	 * @return boolean
	 * @throws RepositorySecurityException  thrown if the user's authorizations cannot be resolved
	 */
	public boolean isAuthorized(UserPrincipal user, String namespace, RepositoryPermission permission) throws RepositorySecurityException;
	
	/**
	 * Returns true if the user is assigned to the 'Administrators' group.
	 * 
	 * @param user  the user for which authorization is being requested
	 * @return boolean
	 */
	public boolean isAdministrator(UserPrincipal user);
	
	/**
	 * Returns the permission that the user is authorized to perform on the specified namespace.
	 * 
	 * @param user  the user for which permissions are being requested
	 * @param namespace  the namespace to which the user is requesting access
	 * @return RepositoryPermission
	 * @throws RepositorySecurityException  thrown if an error occurs while resolving user authorizations
	 */
	public RepositoryPermission getAuthorization(UserPrincipal user, String namespace) throws RepositorySecurityException;
	
	/**
	 * Returns the list of all group names defined for the repository.
	 * 
	 * @return List<String>
	 * @throws RepositorySecurityException  thrown if the repository's group assignments cannot be retrieved
	 */
	public List<String> getGroupNames() throws RepositorySecurityException;
	
	/**
	 * Retrieves the specified user group for the repository.
	 * 
	 * @param groupName  the name of the group to retrieve
	 * @return UserGroup
	 * @throws RepositorySecurityException  thrown if the repository's group assignments cannot be retrieved
	 */
	public UserGroup getGroup(String groupName) throws RepositorySecurityException;
	
}

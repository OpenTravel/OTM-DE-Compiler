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
package org.opentravel.schemacompiler.security;

import java.util.List;

import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositorySecurityException;

/**
 * Provides all authentication and authorization services required for secure operation of the
 * OTA2.0 repository.
 * 
 * @author S. Livezey
 */
public interface RepositorySecurityManager {

    public static final String ADMINISTRATORS_GROUP_NAME = "Administrators";
    
    /**
     * Returns the user with the specified ID or null if such a user does not exist.
     * 
     * @param userId  the ID of the user account to return
     * @return UserPrincipal
     */
    public UserPrincipal getUser(String userId);
    
    /**
     * Returns a list of all known user accounts for the repository.
     * 
     * @return List<UserPrincipal>
     */
    public List<UserPrincipal> getAllUsers();
    
    /**
     * Returns the user principal indicated by the HTTP authorization header provided. If the header
     * value is null, the anonymous user will be returned. If the user fails authentication, a
     * <code>RepositorySecurityException</code> will be thrown.
     * 
     * @param authorizationHeader
     *            the HTTP authorization header value
     * @return UserPrincipal
     * @throws RepositorySecurityException
     *             thrown if the user's ID and password do not pass authentication
     */
    public UserPrincipal authenticateUser(String authorizationHeader) throws RepositorySecurityException;

    /**
     * Returns the user principal indicated by the user ID and password credentials provided. If the
     * header value is null, the anonymous user will be returned. If the user fails authentication,
     * a <code>RepositorySecurityException</code> will be thrown.
     * 
     * @param userId
     *            the ID of the user to be authenticated
     * @param password
     *            the password credentials to be authenticated
     * @return UserPrincipal
     * @throws RepositorySecurityException
     *             thrown if the user's ID and password do not pass authentication
     */
    public UserPrincipal authenticateUser(String userId, String password) throws RepositorySecurityException;
    
    /**
     * Adds a user to the list of accounts who are authorized to access the repository.
     * 
     * @param user  the account information of the user to add
     * @throws RepositoryException  thrown if the user account cannot be added for any reason
     */
    public void addUser(UserPrincipal user) throws RepositoryException;
    
    /**
     * Updates the account information for an existing repository user.
     * 
     * @param user  the user account information to update
     * @throws RepositoryException  thrown if the user account cannot be updated for any reason
     */
    public void updateUser(UserPrincipal user) throws RepositoryException;
    
    /**
     * Deletes a user from the list of accounts who are authorized to access the repository.
     * 
     * @param userId  the ID of the user account to delete
     * @throws RepositoryException  thrown if the user account cannot be deleted for any reason
     */
    public void deleteUser(String userId) throws RepositoryException;
    
    /**
     * Assigns or modifies the user's password.
     * 
     * @param userId  the ID of the user whose password is to be assigned
     * @param password  the clear-text password to assign
     * @throws RepositoryException  thrown if the user's password cannot be assigned
     */
    public void setUserPassword(String userId, String password) throws RepositoryException;
    
    /**
     * Returns true if the user is authorized to perform the specified operation within the
     * requested namespace.
     * 
     * @param user
     *            the user for which authorization is being requested
     * @param namespace
     *            the namespace to which the user is requesting access
     * @param permission
     *            the permission/action that is being requested by the user
     * @return boolean
     * @throws RepositorySecurityException
     *             thrown if the user's authorizations cannot be resolved
     */
    public boolean isAuthorized(UserPrincipal user, String namespace, RepositoryPermission permission)
    		throws RepositorySecurityException;

    /**
     * Returns true if the user is authorized to read the given repository item.
     * 
     * @param user
     *            the user for which authorization is being requested
     * @param item
     *            the repository item to check for read permission
     * @return boolean
     * @throws RepositorySecurityException
     *             thrown if the user's authorizations cannot be resolved
     */
    public boolean isReadAuthorized(UserPrincipal user, RepositoryItem item) throws RepositorySecurityException;

    /**
     * Returns true if the user is authorized to write the given repository item.  Note that
     * this method will always return false for items not in DRAFT status.
     * 
     * @param user
     *            the user for which authorization is being requested
     * @param item
     *            the repository item to check for write permission
     * @return boolean
     * @throws RepositorySecurityException
     *             thrown if the user's authorizations cannot be resolved
     */
    public boolean isWriteAuthorized(UserPrincipal user, RepositoryItem item) throws RepositorySecurityException;

    /**
     * Returns true if the user is authorized to promote the given repository item to the next-level
     * status.
     * 
     * @param user
     *            the user for which authorization is being requested
     * @param item
     *            the repository item to check for promote permission
     * @return boolean
     * @throws RepositorySecurityException
     *             thrown if the user's authorizations cannot be resolved
     */
    public boolean isPromoteAuthorized(UserPrincipal user, RepositoryItem item) throws RepositorySecurityException;

    /**
     * Returns true if the user is assigned to the 'Administrators' group.
     * 
     * @param user
     *            the user for which authorization is being requested
     * @return boolean
     */
    public boolean isAdministrator(UserPrincipal user);

    /**
     * Returns the permission that the user is authorized to perform on the specified namespace.
     * 
     * @param user
     *            the user for which permissions are being requested
     * @param namespace
     *            the namespace to which the user is requesting access
     * @return RepositoryPermission
     * @throws RepositorySecurityException
     *             thrown if an error occurs while resolving user authorizations
     */
    public RepositoryPermission getAuthorization(UserPrincipal user, String namespace)
            throws RepositorySecurityException;

    /**
     * Returns the list of all group names defined for the repository.
     * 
     * @return List<String>
     * @throws RepositorySecurityException
     *             thrown if the repository's group assignments cannot be retrieved
     */
    public List<String> getGroupNames() throws RepositorySecurityException;

    /**
     * Retrieves the specified user group for the repository.
     * 
     * @param groupName
     *            the name of the group to retrieve
     * @return UserGroup
     * @throws RepositorySecurityException
     *             thrown if the repository's group assignments cannot be retrieved
     */
    public UserGroup getGroup(String groupName) throws RepositorySecurityException;

}

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

package org.opentravel.repocommon.security.impl;

import org.opentravel.ns.ota2.repositoryinfoext_v01_00.UserInfo;
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.opentravel.repocommon.security.AuthenticationProvider;
import org.opentravel.repocommon.security.AuthorizationProvider;
import org.opentravel.repocommon.security.GroupAssignmentsResource;
import org.opentravel.repocommon.security.RepositorySecurityManager;
import org.opentravel.repocommon.security.UserGroup;
import org.opentravel.repocommon.security.UserPrincipal;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.RepositoryNamespaceUtils;
import org.opentravel.schemacompiler.repository.RepositorySecurityException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Default implementation of the <code>RepositorySecurityManager</code> interface.
 * 
 * @author S. Livezey
 */
public class DefaultRepositorySecurityManager implements RepositorySecurityManager {

    public static final String ADMINISTRATORS_GROUP_NAME = "Administrators";

    private AuthenticationProvider authenticationProvider;
    private GroupAssignmentsResource groupAssignmentsResource;
    private AuthorizationProvider authorizationProvider;

    /**
     * Constructor that provides all of the sub-components required to implement the security functions of the OTA2.0
     * repository.
     * 
     * @param repositoryManager the repository manager for which security is being provided
     * @param authenticationProvider the authentication provider instance
     * @param authorizationProvider the authorization provider instance
     */
    public DefaultRepositorySecurityManager(RepositoryManager repositoryManager,
        AuthenticationProvider authenticationProvider, AuthorizationProvider authorizationProvider) {
        this.groupAssignmentsResource = new GroupAssignmentsResource( repositoryManager );
        this.authenticationProvider = authenticationProvider;
        this.authorizationProvider = authorizationProvider;
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#getUser(java.lang.String)
     */
    @Override
    public UserPrincipal getUser(String userId) {
        UserInfo userInfo = authenticationProvider.getUserInfo( userId );

        return (userInfo == null) ? null
            : new UserPrincipal( userInfo, groupAssignmentsResource.getAssignedGroups( userId ) );
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#getAllUsers()
     */
    @Override
    public List<UserPrincipal> getAllUsers() {
        List<UserInfo> userInfos = authenticationProvider.getAllUsers();
        List<UserPrincipal> allUsers = new ArrayList<>();

        for (UserInfo userInfo : userInfos) {
            allUsers.add(
                new UserPrincipal( userInfo, groupAssignmentsResource.getAssignedGroups( userInfo.getUserId() ) ) );
        }
        return allUsers;
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#authenticateUser(java.lang.String)
     */
    @Override
    public UserPrincipal authenticateUser(String authorizationHeader) throws RepositorySecurityException {
        UserPrincipal user;

        if (authorizationHeader != null) {
            String[] credentials = getAuthorizationCredentials( authorizationHeader );

            user = authenticateUser( credentials[0], credentials[1] );

        } else {
            user = UserPrincipal.ANONYMOUS_USER;
        }
        return user;
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#authenticateUser(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public UserPrincipal authenticateUser(String userId, String password) throws RepositorySecurityException {
        UserPrincipal user;

        if ((userId != null) && (password != null)) {
            UserInfo userInfo = authenticationProvider.getUserInfo( userId );

            if ((userInfo == null) || !authenticationProvider.isValidUser( userId, password )) {
                throw new RepositorySecurityException(
                    "Invalid user name or password submitted for principal: " + userId );
            }
            user = new UserPrincipal( userInfo, groupAssignmentsResource.getAssignedGroups( userId ) );

        } else {
            user = UserPrincipal.ANONYMOUS_USER;
        }
        return user;
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#addUser(org.opentravel.repocommon.security.UserPrincipal)
     */
    @Override
    public void addUser(UserPrincipal user) throws RepositoryException {
        authenticationProvider.addUser( toUserInfo( user ) );
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#updateUser(org.opentravel.repocommon.security.UserPrincipal)
     */
    @Override
    public void updateUser(UserPrincipal user) throws RepositoryException {
        authenticationProvider.updateUser( toUserInfo( user ) );
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#deleteUser(java.lang.String)
     */
    @Override
    public void deleteUser(String userId) throws RepositoryException {
        authenticationProvider.deleteUser( userId );
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#setUserPassword(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void setUserPassword(String userId, String password) throws RepositoryException {
        authenticationProvider.setUserPassword( userId, password );
    }

    /**
     * Returns a new <code>UserInfo</code> object that is based upon the given principal.
     * 
     * @param user the user from which to create the UserInfo object
     * @return UserInfo
     */
    private UserInfo toUserInfo(UserPrincipal user) {
        UserInfo userInfo = new UserInfo();

        userInfo.setUserId( user.getUserId() );
        userInfo.setLastName( user.getLastName() );
        userInfo.setFirstName( user.getFirstName() );
        userInfo.setEmailAddress( user.getEmailAddress() );
        return userInfo;
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#isAuthorized(org.opentravel.repocommon.security.UserPrincipal,java.lang.String,
     *      org.opentravel.ns.ota2.security_v01_00.RepositoryPermission)
     */
    @Override
    public boolean isAuthorized(UserPrincipal user, String namespace, RepositoryPermission permission)
        throws RepositorySecurityException {
        boolean result = false;

        if (isAdministrator( user )) {
            result = true; // Administrators automatically have write access to everything

        } else {
            String authNS = RepositoryNamespaceUtils.normalizeUri( namespace );
            RepositoryPermission authorizedPermission = getAuthorization( user, authNS );

            switch (permission) {
                case READ_FINAL:
                    result |= (authorizedPermission == RepositoryPermission.READ_FINAL);
                    result |= (authorizedPermission == RepositoryPermission.READ_DRAFT);
                    result |= (authorizedPermission == RepositoryPermission.WRITE);
                    break;
                case READ_DRAFT:
                    result |= (authorizedPermission == RepositoryPermission.READ_DRAFT);
                    result |= (authorizedPermission == RepositoryPermission.WRITE);
                    break;
                case WRITE:
                    result |= (authorizedPermission == RepositoryPermission.WRITE);
                    break;
                default:
                    // No default action required
            }
        }
        return result;
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#isReadAuthorized(org.opentravel.repocommon.security.UserPrincipal,
     *      org.opentravel.repocommon.repository.RepositoryItem)
     */
    @Override
    public boolean isReadAuthorized(UserPrincipal user, RepositoryItem item) throws RepositorySecurityException {
        TLLibraryStatus status = item.getStatus();
        RepositoryPermission requiredPermission;

        if ((status == TLLibraryStatus.DRAFT) || (status == TLLibraryStatus.UNDER_REVIEW)) {
            requiredPermission = RepositoryPermission.READ_DRAFT;

        } else { // FINAL or OBSOLETE
            requiredPermission = RepositoryPermission.READ_FINAL;
        }
        return isAuthorized( user, item.getNamespace(), requiredPermission );
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#isWriteAuthorized(org.opentravel.repocommon.security.UserPrincipal,
     *      org.opentravel.repocommon.repository.RepositoryItem)
     */
    @Override
    public boolean isWriteAuthorized(UserPrincipal user, RepositoryItem item) throws RepositorySecurityException {
        boolean isAuthorized;

        if (item.getStatus() == TLLibraryStatus.DRAFT) {
            isAuthorized = isAuthorized( user, item.getNamespace(), RepositoryPermission.WRITE );

        } else { // no write authorization for any status but draft
            isAuthorized = false;
        }
        return isAuthorized;
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#isPromoteAuthorized(org.opentravel.repocommon.security.UserPrincipal,
     *      org.opentravel.repocommon.repository.RepositoryItem)
     */
    @Override
    public boolean isPromoteAuthorized(UserPrincipal user, RepositoryItem item) throws RepositorySecurityException {
        boolean isAuthorized;

        if (item.getStatus() != TLLibraryStatus.OBSOLETE) {
            isAuthorized = isAuthorized( user, item.getNamespace(), RepositoryPermission.WRITE );

        } else { // no promote authorization for obsolete items
            isAuthorized = false;
        }
        return isAuthorized;
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#isAdministrator(org.opentravel.repocommon.security.UserPrincipal)
     */
    @Override
    public boolean isAdministrator(UserPrincipal user) {
        boolean result = false;

        if (user != null) {
            result = user.getAssignedGroups().contains( ADMINISTRATORS_GROUP_NAME );
        }
        return result;
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#getAuthorization(org.opentravel.repocommon.security.UserPrincipal,
     *      java.lang.String)
     */
    @Override
    public RepositoryPermission getAuthorization(UserPrincipal user, String namespace)
        throws RepositorySecurityException {
        String authNS = RepositoryNamespaceUtils.normalizeUri( namespace );

        return authorizationProvider.getAuthorization( user, authNS );
    }

    /**
     * Returns an array of strings with a length of two. The first element is the user's ID from the authorization
     * header, and the second is the user's clear-text password.
     * 
     * @param authenticationProvider the authentication provider instance
     * @return String[]
     * @throws RepositorySecurityException thrown if the format of the authorization header is invalid
     */
    public static String[] getAuthorizationCredentials(String authorizationHeader) throws RepositorySecurityException {
        if ((authorizationHeader != null) && authorizationHeader.startsWith( "Basic " )) {
            String credentials =
                new String( org.apache.commons.codec.binary.Base64.decodeBase64( authorizationHeader.substring( 6 ) ) );
            int colonIdx = credentials.indexOf( ':' );
            String userId = "";
            String password = "";

            if (colonIdx < 0) {
                // "user", ""
                userId = credentials;

            } else if (colonIdx == (credentials.length() - 1)) {
                // "user:", ":"
                userId = credentials.substring( 0, colonIdx );

            } else {
                // "user:password", "user:pas:sword" ":password", ":pas:sword"
                userId = credentials.substring( 0, colonIdx );
                password = credentials.substring( colonIdx + 1 );
            }
            return new String[] {userId, password};

        } else {
            throw new RepositorySecurityException( "Invalid HTTP Authoriation Header: " + authorizationHeader );
        }
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#getGroupNames()
     */
    @Override
    public List<String> getGroupNames() throws RepositorySecurityException {
        groupAssignmentsResource.invalidateResource();
        return Arrays.asList( groupAssignmentsResource.getGroupNames() );
    }

    /**
     * @see org.opentravel.repocommon.security.RepositorySecurityManager#getGroup(java.lang.String)
     */
    @Override
    public UserGroup getGroup(String groupName) throws RepositorySecurityException {
        UserGroup group = null;

        if (groupName != null) {
            String[] groupMembers = groupAssignmentsResource.getAssignedUsers( groupName );
            group = new UserGroup( groupName, Arrays.asList( groupMembers ) );
        }
        return group;
    }

}

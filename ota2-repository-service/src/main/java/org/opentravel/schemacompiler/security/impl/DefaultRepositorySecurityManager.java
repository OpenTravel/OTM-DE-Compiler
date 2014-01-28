package org.opentravel.schemacompiler.security.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.RepositoryNamespaceUtils;
import org.opentravel.schemacompiler.security.AuthenticationProvider;
import org.opentravel.schemacompiler.security.AuthorizationProvider;
import org.opentravel.schemacompiler.security.GroupAssignmentsResource;
import org.opentravel.schemacompiler.security.RepositorySecurityException;
import org.opentravel.schemacompiler.security.RepositorySecurityManager;
import org.opentravel.schemacompiler.security.UserGroup;
import org.opentravel.schemacompiler.security.UserPrincipal;

/**
 * Default implementation of the <code>RepositorySecurityManager</code> interface.
 * 
 * @author S. Livezey
 */
public class DefaultRepositorySecurityManager implements RepositorySecurityManager {

    private AuthenticationProvider authenticationProvider;
    private GroupAssignmentsResource groupAssignmentsResource;
    private AuthorizationProvider authorizationProvider;

    /**
     * Constructor that provides all of the sub-components required to implement the security
     * functions of the OTA2.0 repository.
     * 
     * @param repositoryManager
     *            the repository manager for which security is being provided
     * @param authenticationProvider
     *            the authentication provider instance
     * @param authorizationProvider
     *            the authorization provider instance
     */
    public DefaultRepositorySecurityManager(RepositoryManager repositoryManager,
            AuthenticationProvider authenticationProvider,
            AuthorizationProvider authorizationProvider) {
        this.groupAssignmentsResource = new GroupAssignmentsResource(repositoryManager);
        this.authenticationProvider = authenticationProvider;
        this.authorizationProvider = authorizationProvider;
    }

    /**
     * @see org.opentravel.schemacompiler.security.RepositorySecurityManager#getUser(java.lang.String)
     */
    @Override
    public UserPrincipal getUser(String authorizationHeader) throws RepositorySecurityException {
        UserPrincipal user;

        if (authorizationHeader != null) {
            String[] credentials = getAuthorizationCredentials(authorizationHeader);

            user = getUser(credentials[0], credentials[1]);

        } else {
            user = UserPrincipal.ANONYMOUS_USER;
        }
        return user;
    }

    /**
     * @see org.opentravel.schemacompiler.security.RepositorySecurityManager#getUser(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public UserPrincipal getUser(String userId, String password) throws RepositorySecurityException {
        UserPrincipal user;

        if ((userId != null) && (password != null)) {
            if (!authenticationProvider.isValidUser(userId, password)) {
                throw new RepositorySecurityException(
                        "Invalid user name or password submitted for principal: " + userId);
            }
            user = new UserPrincipal(userId, groupAssignmentsResource.getAssignedGroups(userId));

        } else {
            user = UserPrincipal.ANONYMOUS_USER;
        }
        return user;
    }

    /**
     * @see org.opentravel.schemacompiler.security.RepositorySecurityManager#isAuthorized(org.opentravel.schemacompiler.security.UserPrincipal,
     *      java.lang.String, org.opentravel.ns.ota2.security_v01_00.RepositoryPermission)
     */
    @Override
    public boolean isAuthorized(UserPrincipal user, String namespace,
            RepositoryPermission permission) throws RepositorySecurityException {
        String authNS = RepositoryNamespaceUtils.normalizeUri(namespace);
        RepositoryPermission authorizedPermission = getAuthorization(user, authNS);
        boolean result = false;

        switch (permission) {
            case READ_FINAL:
                result |= (authorizedPermission == RepositoryPermission.READ_FINAL);
            case READ_DRAFT:
                result |= (authorizedPermission == RepositoryPermission.READ_DRAFT);
            case WRITE:
                result |= (authorizedPermission == RepositoryPermission.WRITE);
        }
        return result;
    }

    /**
     * @see org.opentravel.schemacompiler.security.RepositorySecurityManager#isAdministrator(org.opentravel.schemacompiler.security.UserPrincipal)
     */
    @Override
    public boolean isAdministrator(UserPrincipal user) {
        boolean result = false;

        if (user != null) {
            result = user.getAssignedGroups().contains(ADMINISTRATORS_GROUP_NAME);
        }
        return result;
    }

    /**
     * @see org.opentravel.schemacompiler.security.RepositorySecurityManager#getAuthorization(org.opentravel.schemacompiler.security.UserPrincipal,
     *      java.lang.String)
     */
    @Override
    public RepositoryPermission getAuthorization(UserPrincipal user, String namespace)
            throws RepositorySecurityException {
        String authNS = RepositoryNamespaceUtils.normalizeUri(namespace);

        return authorizationProvider.getAuthorization(user, authNS);
    }

    /**
     * Returns an array of strings with a length of two. The first element is the user's ID from the
     * authorization header, and the second is the user's clear-text password.
     * 
     * @param authenticationProvider
     *            the authentication provider instance
     * @return String[]
     * @throws RepositorySecurityException
     *             thrown if the format of the authorization header is invalid
     */
    private String[] getAuthorizationCredentials(String authorizationHeader)
            throws RepositorySecurityException {
        if ((authorizationHeader != null) && authorizationHeader.startsWith("Basic ")) {
            return new String(Base64.decodeBase64(authorizationHeader.substring(6))).split(":");

        } else {
            throw new RepositorySecurityException("Invalid HTTP Authoriation Header: "
                    + authorizationHeader);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.security.RepositorySecurityManager#getGroupNames()
     */
    @Override
    public List<String> getGroupNames() throws RepositorySecurityException {
        return Arrays.asList(groupAssignmentsResource.getGroupNames());
    }

    /**
     * @see org.opentravel.schemacompiler.security.RepositorySecurityManager#getGroup(java.lang.String)
     */
    @Override
    public UserGroup getGroup(String groupName) throws RepositorySecurityException {
        UserGroup group = null;

        if (groupName != null) {
            String[] groupMembers = groupAssignmentsResource.getAssignedUsers(groupName);
            group = new UserGroup(groupName, Arrays.asList(groupMembers));
        }
        return group;
    }

}

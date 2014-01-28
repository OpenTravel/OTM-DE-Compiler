package org.opentravel.schemacompiler.security;

import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;

/**
 * Provides information about which operations a particular user is authorized to perform on a
 * namespace in the OTA2.0 repository.
 * 
 * @author S. Livezey
 */
public interface AuthorizationProvider {

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

}

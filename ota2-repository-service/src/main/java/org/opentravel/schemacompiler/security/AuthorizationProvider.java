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

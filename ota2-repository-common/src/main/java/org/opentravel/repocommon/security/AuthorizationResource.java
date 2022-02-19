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

package org.opentravel.repocommon.security;

import org.opentravel.ns.ota2.security_v01_00.AuthorizationSpec;
import org.opentravel.ns.ota2.security_v01_00.NamespaceAuthorizations;
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.opentravel.repocommon.config.FileResource;
import org.opentravel.repocommon.security.impl.SecurityFileUtils;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.repository.RepositorySecurityException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * File-based resource that provides access to authorization grants (and denies) for a single namespace URI.
 * 
 * @author S. Livezey
 */
public class AuthorizationResource extends FileResource<NamespaceAuthorizations> {

    private RepositoryFileManager fileManager;
    private SecurityFileUtils fileUtils;
    private String namespace;

    /**
     * Constructor that specifies the namespace for which authorizations should be retrieved.
     * 
     * @param fileUtils the file utilities instance used for accessing security settings of the repository
     * @param namespace the namespace for which authorizations should be retrieved
     * @throws RepositorySecurityException thrown if the location of the authorization file resource cannot be
     *         identified
     */
    public AuthorizationResource(SecurityFileUtils fileUtils, String namespace) throws RepositorySecurityException {
        super( fileUtils.getAuthorizationFile( namespace ) );
        this.fileManager = fileUtils.getRepositoryManager().getFileManager();
        this.fileUtils = fileUtils;
        this.namespace = namespace;
        invalidateResource();
    }

    /**
     * Returns the namespace URI to which the authorization permissions apply.
     * 
     * @return String
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the permissions that have been explicitly granted to the specified user in this resource's namespace.
     * 
     * @param user the user for which granted permissions should be returned
     * @return Set&lt;RepositoryPermission&gt;
     */
    public Set<RepositoryPermission> getGrantedPermissions(UserPrincipal user) {
        Set<RepositoryPermission> grantedPermissions = new HashSet<>();
        NamespaceAuthorizations authorizations = getResource();

        // Search the grants/denies to determine which permissions apply to this user
        for (AuthorizationSpec grant : authorizations.getGrant()) {
            if (appliesToUser( grant, user )) {
                grantedPermissions.add( grant.getPermission() );
            }
        }
        return grantedPermissions;
    }

    /**
     * Returns the permissions that have been explicitly denied to the specified user in this resource's namespace.
     * 
     * @param user the user for which denied permissions should be returned
     * @return Set&lt;RepositoryPermission&gt;
     */
    public Set<RepositoryPermission> getDeniedPermissions(UserPrincipal user) {
        Set<RepositoryPermission> deniedPermissions = new HashSet<>();
        NamespaceAuthorizations authorizations = getResource();

        // Search the grants/denies to determine which permissions apply to this user
        for (AuthorizationSpec deny : authorizations.getDeny()) {
            if (appliesToUser( deny, user )) {
                deniedPermissions.add( deny.getPermission() );
            }
        }
        return deniedPermissions;
    }

    /**
     * Returns true if the given grant/deny specification applies to the indicated user.
     * 
     * @param spec the grant/deny specification to analyze
     * @param user the user whose permissions are to be checked
     * @return boolean
     */
    private boolean appliesToUser(AuthorizationSpec spec, UserPrincipal user) {
        boolean result = false;

        for (String authPrincipal : spec.getPrincipal()) {
            if (user.getAuthorizationIds().contains( authPrincipal )) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * @see org.opentravel.repocommon.config.FileResource#getDefaultResourceValue()
     */
    @Override
    protected NamespaceAuthorizations getDefaultResourceValue() {
        return new NamespaceAuthorizations();
    }

    /**
     * @see org.opentravel.repocommon.config.FileResource#loadResource(java.io.File)
     */
    @Override
    protected NamespaceAuthorizations loadResource(File dataFile) throws IOException {
        if (fileUtils == null) {
            return getDefaultResourceValue(); // Special case for constructor initialization
        }
        return fileUtils.loadNamespaceAuthorizations( dataFile );
    }

    /**
     * Saves the collection of namespace authorizations to the locally maintained authorizations file.
     * 
     * @param authorizations the authorizations to be saved
     * @throws RepositoryException thrown if the authorizations file cannot be saved
     */
    public synchronized void saveNamespaceAuthorizations(NamespaceAuthorizations authorizations)
        throws RepositoryException {
        fileManager.startChangeSet();
        boolean success = false;
        try {
            File dataFile = getDataFile();

            fileManager.addToChangeSet( dataFile );
            fileUtils.saveNamespaceAuthorizations( dataFile, authorizations );
            fileManager.commitChangeSet();
            success = true;

        } catch (IOException e) {
            throw new RepositoryException( "Error saving the group assignments file.", e );

        } finally {
            try {
                if (!success) {
                    fileManager.rollbackChangeSet();
                }
            } catch (Exception e) {
                // Ignore possible errors and continue
            }
        }
    }

}

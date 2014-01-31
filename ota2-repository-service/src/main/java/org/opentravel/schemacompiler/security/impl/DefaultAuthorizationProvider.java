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
package org.opentravel.schemacompiler.security.impl;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.ns.ota2.security_v01_00.AuthorizationSpec;
import org.opentravel.ns.ota2.security_v01_00.NamespaceAuthorizations;
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.security.AuthorizationProvider;
import org.opentravel.schemacompiler.security.AuthorizationResource;
import org.opentravel.schemacompiler.security.RepositorySecurityException;
import org.opentravel.schemacompiler.security.UserPrincipal;

/**
 * Default implementation of the <code>AuthorizationProvider</code> interface.
 * 
 * @author S. Livezey
 */
public class DefaultAuthorizationProvider implements AuthorizationProvider {

    private Log log = LogFactory.getLog(DefaultAuthorizationProvider.class);

    private Map<String, AuthorizationResource> authorizationCache = new HashMap<String, AuthorizationResource>();
    private SecurityFileUtils fileUtils;

    /**
     * Constructor that provides the location of the web service's repository on the local file
     * system.
     * 
     * @param repositoryManager
     *            the repository manager for all file-system resources
     */
    public DefaultAuthorizationProvider(RepositoryManager repositoryManager) {
        this.fileUtils = new SecurityFileUtils(repositoryManager);

        // Create a default authorization file if one does not already exist
        if (!hasAuthorizationFile(repositoryManager.getRepositoryLocation())) {
            createDefaultAuthorizationFile();
        }
    }

    /**
     * @see org.opentravel.schemacompiler.security.AuthorizationProvider#getAuthorization(org.opentravel.schemacompiler.security.UserPrincipal,
     *      java.lang.String)
     */
    @Override
    public RepositoryPermission getAuthorization(UserPrincipal user, String namespace)
            throws RepositorySecurityException {
        RepositoryPermission grantedPermission = null;

        // Traverse up the namespace hierarchy gathering all of the granted and denied permissions
        // for this user
        for (String ns : getNamespaceHierarchy(namespace)) {
            AuthorizationResource authResource = getAuthorizationResource(ns);
            Set<RepositoryPermission> nsGrants = authResource.getGrantedPermissions(user);
            Set<RepositoryPermission> nsDenies = authResource.getDeniedPermissions(user);
            RepositoryPermission deniedPermission = null;
            int grantRank = getRank(grantedPermission);
            int denyRank = 99;

            // Find the least-restrictive GRANT permission that we have seen so far (at any level)
            for (RepositoryPermission pGrant : nsGrants) {
                int pGrantRank = getRank(pGrant);

                if (pGrantRank > grantRank) {
                    grantedPermission = pGrant;
                    grantRank = pGrantRank;
                }
            }

            // Find the most-restrictive DENY permission declared at this level
            for (RepositoryPermission pDeny : nsDenies) {
                int pDenyRank = getRank(pDeny);

                if (pDenyRank < denyRank) {
                    deniedPermission = pDeny;
                    denyRank = pDenyRank;
                }
            }

            // Determine if the DENY permission at this level should downgrade our current GRANT
            if (deniedPermission == RepositoryPermission.READ_FINAL) {
                // Denying Read-Final means no granted access at all
                grantedPermission = null;

            } else if (deniedPermission == RepositoryPermission.READ_DRAFT) {
                // Denying Read-Draft means the maximum granted permission is Read-Final
                if (grantedPermission != null) {
                    grantedPermission = RepositoryPermission.READ_FINAL;
                }

            } else if (deniedPermission == RepositoryPermission.WRITE) {
                // Denying Write means the maximum granted permission is Read-Draft
                if (grantedPermission == RepositoryPermission.WRITE) {
                    grantedPermission = RepositoryPermission.READ_DRAFT;
                }
            }
        }

        return grantedPermission;
    }

    /**
     * Searches the repository folder structure, looking for at least one authorization file. If
     * even a single file is identified, this method will return true.
     * 
     * @param repositoryLocation
     *            the repository location to search
     * @return boolean
     */
    protected boolean hasAuthorizationFile(File repositoryLocation) {
        boolean result = false;

        if (repositoryLocation.isDirectory()) {
            for (File file : repositoryLocation.listFiles()) {
                if (result = hasAuthorizationFile(file)) {
                    break;
                }
            }

        } else if (repositoryLocation.getName().equals(SecurityFileUtils.AUTHORIZATION_FILENAME)) {
            result = true;
        }
        return result;
    }

    /**
     * Creates a default authorization file at the root directory of the repository folder
     * structure. If the file cannot be created, this method will simply log the error and return
     * with no action.
     */
    protected void createDefaultAuthorizationFile() {
        try {
            File authorizationFile = fileUtils.getAuthorizationFile(null);
            NamespaceAuthorizations authorizations = new NamespaceAuthorizations();
            AuthorizationSpec grant = new AuthorizationSpec();

            grant.setPermission(RepositoryPermission.READ_DRAFT);
            grant.getPrincipal().add(UserPrincipal.ANONYMOUS_USER_ID);
            authorizations.getGrant().add(grant);

            fileUtils.saveNamespaceAuthorizations(authorizationFile, authorizations);

        } catch (Exception e) {
            log.error("Unable to create default authorization file for repository.", e);
        }
    }

    /**
     * Returns the namespace hierarchy in the order that it must be traversed in order to determine
     * a user's authorization scheme.
     * 
     * @param namespace
     *            the namespace for which to return the hierarchy
     * @return List<String>
     * @throws RepositorySecurityException
     *             thrown if the namespace URI is not valid
     */
    private List<String> getNamespaceHierarchy(String namespace) throws RepositorySecurityException {
        try {
            List<String> hierarchy = new ArrayList<String>();
            URI ns = new URI(namespace);
            String nsScheme = ns.getScheme();
            String nsAuthority = ns.getAuthority();
            String nsPath = ns.getPath();

            hierarchy.add(null); // null hierarchy member represents global authorizations

            if ((nsScheme != null) && (nsAuthority != null) && (nsPath != null)) {
                String[] pathParts = ns.getPath().split("/");
                StringBuilder nsBuilder = new StringBuilder();

                nsBuilder.append(nsScheme).append("://").append(nsAuthority);
                hierarchy.add(nsBuilder.toString());

                for (String pathPart : pathParts) {
                    if ((pathPart != null) && !pathPart.equals("")) {
                        nsBuilder.append("/").append(pathPart);
                        hierarchy.add(nsBuilder.toString());
                    }
                }
            } else {
                throw new URISyntaxException(namespace, "Invalid namespace URI format.");
            }
            return hierarchy;

        } catch (URISyntaxException e) {
            throw new RepositorySecurityException(
                    "Unable to determine security hierarchy for namespace: " + namespace, e);
        }
    }

    /**
     * Returns an <code>AuthorizationResource</code> for the specified namespace.
     * 
     * @param namespace
     *            the namespace for which to returnt he authorization resource
     * @return AuthorizationResource
     * @throws RepositorySecurityException
     *             thrown if the authorization resource file cannot be identified
     */
    private synchronized AuthorizationResource getAuthorizationResource(String namespace)
            throws RepositorySecurityException {
        AuthorizationResource resource = authorizationCache.get(namespace);

        if (resource == null) {
            resource = new AuthorizationResource(fileUtils, namespace);
            authorizationCache.put(namespace, resource);
        }
        return resource;
    }

    /**
     * Returns the ordinal rank for the given permission.
     * 
     * @param permission
     *            the permission for which to return an ordinal rank
     * @return int
     */
    private int getRank(RepositoryPermission permission) {
        int rank = -1;

        if (permission != null) {
            switch (permission) {
                case READ_FINAL:
                    rank = 0;
                    break;
                case READ_DRAFT:
                    rank = 1;
                    break;
                case WRITE:
                    rank = 2;
                    break;
            }
        }
        return rank;
    }

}

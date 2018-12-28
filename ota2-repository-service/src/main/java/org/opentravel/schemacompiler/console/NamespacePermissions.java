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
package org.opentravel.schemacompiler.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentravel.ns.ota2.security_v01_00.AuthorizationSpec;
import org.opentravel.ns.ota2.security_v01_00.NamespaceAuthorizations;
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.security.UserPrincipal;

/**
 * Encapsulates the list of all permissions for a single namespace. This class also supports to and
 * from the JAXB representation of namespace permissions to facilitate storage into the local file
 * system.
 * 
 * @author S. Livezey
 */
public class NamespacePermissions {

    private static List<RepositoryPermission> prioritizedPermissions = Arrays.asList(
    		RepositoryPermission.READ_FINAL, RepositoryPermission.READ_DRAFT,
            RepositoryPermission.WRITE );

    private String namespace;
    private List<NamespacePermission> permissions = new ArrayList<>();

    /**
     * Default constructor.
     */
    public NamespacePermissions() {
    }

    /**
     * Constructor that assigns the namespace for which permissions are being viewed or configured.
     * 
     * @param namespace
     *            the namespace for which permissions are being viewed or configured
     */
    public NamespacePermissions(String namespace) {
        this(namespace, null);
    }

    /**
     * Constructor that configures the permissions using data from the JAXB namespace authorizations
     * file.
     * 
     * @param namespace
     *            the namespace for which permissions are being viewed or configured
     * @param jaxbPermissions
     *            the permissions loaded from the JAXB configuration file
     */
    public NamespacePermissions(String namespace, NamespaceAuthorizations jaxbPermissions) {
        if (jaxbPermissions != null) {
            init(jaxbPermissions);
        }
        this.namespace = namespace;
    }

    /**
     * Scans the list of permissions for this namespace and ensures that entries exist for all of
     * the groups in list provided (and the anonymous user as an implied group). If new permission
     * entries are created for a group, the grant and deny permissions will be null.
     * 
     * @param groupNames
     *            the list of group names for which permissions must exist in this namespace
     */
    public void createGroupPermissions(List<String> groupNames) {
        Map<String,NamespacePermission> principalPermissions = new HashMap<>();
        List<String> principalNames = new ArrayList<>();

        // Create an index of all permissions by principal
        for (NamespacePermission nsPermission : permissions) {
            principalPermissions.put(nsPermission.getPrincipal(), nsPermission);
        }
        principalNames.addAll(principalPermissions.keySet());

        // Add the group names provided to the list of principal names
        for (String groupName : groupNames) {
            if (!principalNames.contains(groupName)) {
                principalNames.add(groupName);
            }
        }
        Collections.sort(principalNames);

        // Make sure the list of principles contains the anonymous user (add if necessary)
        if (!principalNames.contains(UserPrincipal.ANONYMOUS_USER_ID)) {
            NamespacePermission anonymousPermission = new NamespacePermission();

            anonymousPermission.setPrincipal(UserPrincipal.ANONYMOUS_USER_ID);
            principalNames.add(0, UserPrincipal.ANONYMOUS_USER_ID);
            principalPermissions.put(UserPrincipal.ANONYMOUS_USER_ID, anonymousPermission);

        } else { // if already present, sort the anonymous user to the front of the list
            principalNames.remove(UserPrincipal.ANONYMOUS_USER_ID);
            principalNames.add(0, UserPrincipal.ANONYMOUS_USER_ID);
        }

        // Assemble a new list of permissions, adding new (empty) permissions for the missing
        // principals
        List<NamespacePermission> newPermissions = new ArrayList<>();

        for (String principalName : principalNames) {
            NamespacePermission nsPermission = principalPermissions.get(principalName);

            if (nsPermission == null) {
                nsPermission = new NamespacePermission();
                nsPermission.setPrincipal(principalName);
            }
            newPermissions.add(nsPermission);
        }
        this.permissions = newPermissions;
    }

    /**
     * Converts this collection of namespace permissions into its JAXB equivalent, allowing them to
     * be saved to the repository's file system. Any principals who are not explicitly granted or
     * denied any permissions are omitted from the resulting JAXB authorizations.
     * 
     * @return NamespaceAuthorizations
     */
    public NamespaceAuthorizations toJaxbAuthorizations() {
        Map<RepositoryPermission,AuthorizationSpec> jaxbGrants = new EnumMap<>( RepositoryPermission.class );
        Map<RepositoryPermission,AuthorizationSpec> jaxbDenies = new EnumMap<>( RepositoryPermission.class );
        NamespaceAuthorizations jaxbAuthorizations = new NamespaceAuthorizations();

        findPermissions( jaxbGrants, jaxbDenies );

        for (RepositoryPermission permission : RepositoryPermission.values()) {
            AuthorizationSpec jaxbGrant = jaxbGrants.get(permission);
            AuthorizationSpec jaxbDeny = jaxbDenies.get(permission);

            if (jaxbGrant != null) {
                jaxbAuthorizations.getGrant().add(jaxbGrant);
            }
            if (jaxbDeny != null) {
                jaxbAuthorizations.getDeny().add(jaxbDeny);
            }
        }
        return jaxbAuthorizations;
    }

	/**
	 * Assemble the set of granted and denied permissions.
	 * 
	 * @param jaxbGrants  the map of permissions that have been granted
	 * @param jaxbDenies  the map of permissions that have been granted
	 */
	private void findPermissions(Map<RepositoryPermission,AuthorizationSpec> jaxbGrants,
			Map<RepositoryPermission,AuthorizationSpec> jaxbDenies) {
		for (NamespacePermission nsPermission : permissions) {
            if (nsPermission.getGrantPermission() != null) {
                AuthorizationSpec jaxbGrant = jaxbGrants.get(nsPermission.getGrantPermission());

                if (jaxbGrant == null) {
                    jaxbGrant = new AuthorizationSpec();
                    jaxbGrant.setPermission(nsPermission.getGrantPermission());
                    jaxbGrants.put(nsPermission.getGrantPermission(), jaxbGrant);
                }
                jaxbGrant.getPrincipal().add(nsPermission.getPrincipal());
            }
            if (nsPermission.getDenyPermission() != null) {
                AuthorizationSpec jaxbDeny = jaxbDenies.get(nsPermission.getDenyPermission());

                if (jaxbDeny == null) {
                    jaxbDeny = new AuthorizationSpec();
                    jaxbDeny.setPermission(nsPermission.getDenyPermission());
                    jaxbDenies.put(nsPermission.getDenyPermission(), jaxbDeny);
                }
                jaxbDeny.getPrincipal().add(nsPermission.getPrincipal());
            }
        }
	}

    /**
     * Initializes the permissions using data from the JAXB namespace authorizations file.
     * 
     * @param jaxbPermissions
     *            the permissions loaded from the JAXB configuration file
     */
    private void init(NamespaceAuthorizations jaxbPermissions) {
        Map<String,NamespacePermission> principalPermissions = new HashMap<>();
        List<String> principalNames = new ArrayList<>();

        if (jaxbPermissions != null) {

            parsePermissions( jaxbPermissions, principalNames, principalPermissions );

            // Assemble the final list of permissions for this namespace. If permission(s) are
            // defined for the anonymous user, make sure 'anonymous' is sorted to the front of the list.
            Collections.sort(principalNames);

            if (principalNames.contains(UserPrincipal.ANONYMOUS_USER_ID)) {
                principalNames.remove(UserPrincipal.ANONYMOUS_USER_ID);
                principalNames.add(0, UserPrincipal.ANONYMOUS_USER_ID);
            }

            for (String principalName : principalNames) {
                this.permissions.add(principalPermissions.get(principalName));
            }
        }
    }

	/**
	 * Uses the given JAXB permission declarations to populate the list of principals and the
	 * map of their corresponding permissions.
	 * 
	 * @param jaxbPermissions  the JAXB permissions being parsed
	 * @param principalNames  the list of principal names found in the permission declarations
	 * @param principalPermissions  the map of namespace permissions by principal names
	 */
	private void parsePermissions(NamespaceAuthorizations jaxbPermissions, List<String> principalNames,
			Map<String,NamespacePermission> principalPermissions) {
		// Parse the list of granted permissions
		for (AuthorizationSpec jaxbGrant : jaxbPermissions.getGrant()) {
		    for (String principalName : jaxbGrant.getPrincipal()) {
		        NamespacePermission nsPermission = principalPermissions.get(principalName);

		        if (nsPermission == null) {
		            nsPermission = new NamespacePermission();
		            nsPermission.setPrincipal(principalName);
		            nsPermission.setGrantPermission(jaxbGrant.getPermission());
		            principalNames.add(principalName);
		            principalPermissions.put(principalName, nsPermission);

		        } else {
		            nsPermission.setGrantPermission(resolvePermission(
		                    nsPermission.getGrantPermission(), jaxbGrant.getPermission()));
		        }
		    }
		}

		// Parse the list of denied permissions
		for (AuthorizationSpec jaxbDeny : jaxbPermissions.getDeny()) {
		    for (String principalName : jaxbDeny.getPrincipal()) {
		        NamespacePermission nsPermission = principalPermissions.get(principalName);

		        if (nsPermission == null) {
		            nsPermission = new NamespacePermission();
		            nsPermission.setPrincipal(principalName);
		            nsPermission.setDenyPermission(jaxbDeny.getPermission());
		            principalNames.add(principalName);
		            principalPermissions.put(principalName, nsPermission);

		        } else {
		            nsPermission.setDenyPermission(resolvePermission(
		                    nsPermission.getDenyPermission(), jaxbDeny.getPermission()));
		        }
		    }
		}
	}

    /**
     * Resolves the two permissions by returning the one that takes precedent (e.g. Read-Draft has
     * priority over Read-Final).
     * 
     * @param p1
     *            the first permission to analyze and resolve (may be null)
     * @param p2
     *            the second permission to analyze and resolve (may be null)
     * @return RepositoryPermission
     */
    private RepositoryPermission resolvePermission(RepositoryPermission p1, RepositoryPermission p2) {
        int p1Priority = prioritizedPermissions.indexOf(p1);
        int p2Priority = prioritizedPermissions.indexOf(p2);

        return (p1Priority > p2Priority) ? p1 : p2;
    }

    /**
     * Returns the value of the 'namespace' field.
     * 
     * @return String
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Assigns the value of the 'namespace' field.
     * 
     * @param namespace
     *            the field value to assign
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Returns the value of the 'permissions' field.
     * 
     * @return List<NamespacePermission>
     */
    public List<NamespacePermission> getPermissions() {
        return permissions;
    }

    /**
     * Assigns the value of the 'permissions' field.
     * 
     * @param permissions
     *            the field value to assign
     */
    public void setPermissions(List<NamespacePermission> permissions) {
        this.permissions = permissions;
    }

}

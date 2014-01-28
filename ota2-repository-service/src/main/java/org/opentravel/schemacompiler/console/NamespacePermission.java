package org.opentravel.schemacompiler.console;

import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;

/**
 * Encapsulates the permissions for a single principal in a namespace.
 * 
 * @author S. Livezey
 */
public class NamespacePermission {

    private String principal;
    private RepositoryPermission grantPermission;
    private RepositoryPermission denyPermission;

    /**
     * Returns the value of the 'principal' field.
     * 
     * @return String
     */
    public String getPrincipal() {
        return principal;
    }

    /**
     * Assigns the value of the 'principal' field.
     * 
     * @param principal
     *            the field value to assign
     */
    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    /**
     * Returns the value of the 'grantPermission' field.
     * 
     * @return RepositoryPermission
     */
    public RepositoryPermission getGrantPermission() {
        return grantPermission;
    }

    /**
     * Assigns the value of the 'grantPermission' field.
     * 
     * @param grantPermission
     *            the field value to assign
     */
    public void setGrantPermission(RepositoryPermission grantPermission) {
        this.grantPermission = grantPermission;
    }

    /**
     * Returns the value of the 'denyPermission' field.
     * 
     * @return RepositoryPermission
     */
    public RepositoryPermission getDenyPermission() {
        return denyPermission;
    }

    /**
     * Assigns the value of the 'denyPermission' field.
     * 
     * @param denyPermission
     *            the field value to assign
     */
    public void setDenyPermission(RepositoryPermission denyPermission) {
        this.denyPermission = denyPermission;
    }

}

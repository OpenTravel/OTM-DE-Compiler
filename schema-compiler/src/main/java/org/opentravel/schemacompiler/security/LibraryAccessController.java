package org.opentravel.schemacompiler.security;

import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Interface to be implemented by components that can determine whether a user should be allowed
 * access to edit a user-defined library instance.
 * 
 * @author S. Livezey
 */
public interface LibraryAccessController {

    /**
     * Returns true if the current user is allowed to modify the given library.
     * 
     * @param library
     *            the user-defined library
     * @return boolean
     */
    public boolean hasModifyPermission(TLLibrary library);

    /**
     * Assigns the set of security credentials that determine which (if any) protected namespaces
     * the user has the authority to modify. It is up to the implementation to determine how this
     * information should be used to grant (or deny) write access to libraries assigned to protected
     * namespace authorities.
     * 
     * @param userCredentials
     *            the user credentials that provide access to update protected namespaces
     */
    public void setUserCredentials(ProtectedNamespaceCredentials userCredentials);

}

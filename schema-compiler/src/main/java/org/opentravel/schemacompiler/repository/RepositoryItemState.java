package org.opentravel.schemacompiler.repository;

/**
 * Indicates the state of a <code>RepositoryItem</code>.
 * 
 * @author S. Livezey
 */
public enum RepositoryItemState {

    /**
     * Indicates a managed item from a remote repository item that has not been locked for editing
     * by a user.
     */
    MANAGED_UNLOCKED,

    /**
     * Indicates a managed item from a remote repository item that has been locked for editing by a
     * user.
     */
    MANAGED_LOCKED,

    /**
     * Indicates a managed item in the local workspace project that has been locked for editing and
     * can be modified by the local user.
     */
    MANAGED_WIP,

    /**
     * An unmanaged item in the local workspace project that can be edited by the user, but is not
     * associated with a remote repository item.
     */
    UNMANAGED,

    /**
     * State reserved for project items that represent the built-in libraries of an OTM model.
     */
    BUILT_IN;

}

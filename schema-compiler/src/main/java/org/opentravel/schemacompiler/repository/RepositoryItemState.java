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

package org.opentravel.schemacompiler.repository;

/**
 * Indicates the state of a <code>RepositoryItem</code>.
 * 
 * @author S. Livezey
 */
public enum RepositoryItemState {

    /**
     * Indicates a managed item from a remote repository item that has not been locked for editing by a user.
     */
    MANAGED_UNLOCKED,

    /**
     * Indicates a managed item from a remote repository item that has been locked for editing by a user.
     */
    MANAGED_LOCKED,

    /**
     * Indicates a managed item in the local workspace project that has been locked for editing and can be modified by
     * the local user.
     */
    MANAGED_WIP,

    /**
     * An unmanaged item in the local workspace project that can be edited by the user, but is not associated with a
     * remote repository item.
     */
    UNMANAGED,

    /**
     * State reserved for project items that represent the built-in libraries of an OTM model.
     */
    BUILT_IN;

}

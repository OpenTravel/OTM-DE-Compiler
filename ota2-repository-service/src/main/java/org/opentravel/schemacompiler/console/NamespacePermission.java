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
     * @param principal the field value to assign
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
     * @param grantPermission the field value to assign
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
     * @param denyPermission the field value to assign
     */
    public void setDenyPermission(RepositoryPermission denyPermission) {
        this.denyPermission = denyPermission;
    }

}

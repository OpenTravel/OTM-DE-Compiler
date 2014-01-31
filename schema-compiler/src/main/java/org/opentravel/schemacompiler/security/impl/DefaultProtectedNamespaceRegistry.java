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

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.security.ProtectedNamespaceGroup;
import org.opentravel.schemacompiler.security.ProtectedNamespaceRegistry;

/**
 * Default implementation of the <code>ProtectedNamespaceRegistry</code> that is designed to obtain
 * its registry entries from spring injection or some other manual assignment method.
 * 
 * @author S. Livezey
 */
public class DefaultProtectedNamespaceRegistry extends ProtectedNamespaceRegistry {

    private List<ProtectedNamespaceGroup> nsGroups = new ArrayList<ProtectedNamespaceGroup>();

    /**
     * @see org.opentravel.schemacompiler.security.ProtectedNamespaceRegistry#getProtectedNamespaces()
     */
    @Override
    public List<ProtectedNamespaceGroup> getProtectedNamespaces() {
        return nsGroups;
    }

    /**
     * Assigns the list of protected namespace groups for this registry.
     * 
     * @param nsGroups
     *            the list of protected namespace groups
     */
    public void setProtectedNamespaces(List<ProtectedNamespaceGroup> nsGroups) {
        this.nsGroups = nsGroups;
    }

}

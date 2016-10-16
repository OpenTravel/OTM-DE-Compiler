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
package org.opentravel.schemacompiler.lock;

/**
 * Identity of a resource for which a read/write lock can be obtained.
 * 
 * @author S. Livezey
 */
public class LockableResource {

    private String namespace;
    private String resourceName;

    /**
     * Full constructor that assigns the namespace and local name of the resource.
     * 
     * @param namespace
     *            the namespace to which the locked resource is assigned
     * @param resourceName
     *            the local name of the locked resource
     * @param type
     *            the type of lock acquired for the resource
     */
    protected LockableResource(String namespace, String resourceName) {
        this.namespace = namespace;
        this.resourceName = resourceName;
    }

    /**
     * Returns the namespace to which the locked resource is assigned.
     * 
     * @return String
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the the local name of the locked resource.
     * 
     * @return String
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        boolean result;

        if (obj instanceof LockableResource) {
            LockableResource otherLock = (LockableResource) obj;

            result = ((this.namespace == null) ? (otherLock.namespace == null) : this.namespace
                    .equals(otherLock.namespace))
                    && ((this.resourceName == null) ? (otherLock.resourceName == null)
                            : this.resourceName.equals(otherLock.resourceName));
        } else {
            result = false;
        }
        return result;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return ((namespace == null) ? 0 : namespace.hashCode())
                + ((resourceName == null) ? 0 : resourceName.hashCode());
    }

}

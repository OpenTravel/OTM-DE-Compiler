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

import org.opentravel.schemacompiler.validate.Validatable;

import javax.xml.namespace.QName;

/**
 * Encapsulates a single release API within a service assembly.
 * 
 * <p>
 * Assembly members consist of a <code>RepositoryItem</code> for the corresponding OTM release, plus the qualified name
 * of an OTM resource that is the primary API. If a resource name is not supplied, all resources contained within the
 * corresponding release will be included in the model.
 */
public class ServiceAssemblyMember implements Validatable {

    private ServiceAssembly owner;
    private RepositoryItem releaseItem;
    private QName resourceName;

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();
        ServiceAssembly owningAssembly = getOwner();

        if (owningAssembly != null) {
            identity.append( owningAssembly.getValidationIdentity() ).append( " : " );
        }
        if ((releaseItem == null) || (releaseItem.getFilename() == null)) {
            identity.append( "[Unidentified Assembly Member Type]" );
        } else {
            identity.append( releaseItem.getFilename() );
        }
        return identity.toString();
    }

    /**
     * Returns the owning assembly for this item.
     *
     * @return ServiceAssembly
     */
    public ServiceAssembly getOwner() {
        return owner;
    }

    /**
     * Assigns the owning assembly for this item.
     *
     * @param owner the assembly to assign as this item's owner
     */
    public void setOwner(ServiceAssembly owner) {
        this.owner = owner;
    }

    /**
     * Returns the repository item for the associated OTM release.
     *
     * @return RepositoryItem
     */
    public RepositoryItem getReleaseItem() {
        return releaseItem;
    }

    /**
     * Assigns the repository item for the associated OTM release.
     *
     * @param releaseItem the repository item to assign
     */
    public void setReleaseItem(RepositoryItem releaseItem) {
        this.releaseItem = releaseItem;
    }

    /**
     * Returns the name of the primary API for the release.
     *
     * @return QName
     */
    public QName getResourceName() {
        return resourceName;
    }

    /**
     * Assigns the name of the primary API for the release.
     *
     * @param resourceName the qualified resource name to assign
     */
    public void setResourceName(QName resourceName) {
        this.resourceName = resourceName;
    }

}

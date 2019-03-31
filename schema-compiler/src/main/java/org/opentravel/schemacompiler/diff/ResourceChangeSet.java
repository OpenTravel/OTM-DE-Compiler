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

package org.opentravel.schemacompiler.diff;

import org.opentravel.schemacompiler.model.TLResource;

/**
 * Container for all change items identified during the comparison of two resources, as well as the change sets for
 * their member components.
 */
public class ResourceChangeSet extends ChangeSet<TLResource,ResourceChangeItem> {

    /**
     * Constructor that assigns the old and new version of a resource that was modified.
     * 
     * @param oldResource the old version of the resource
     * @param newResource the new version of the resource
     */
    public ResourceChangeSet(TLResource oldResource, TLResource newResource) {
        super( oldResource, newResource );
    }

    /**
     * @see org.opentravel.schemacompiler.diff.ChangeSet#getBookmarkId()
     */
    @Override
    public String getBookmarkId() {
        return getBookmarkId( (getNewVersion() != null) ? getNewVersion() : getOldVersion() );
    }

}

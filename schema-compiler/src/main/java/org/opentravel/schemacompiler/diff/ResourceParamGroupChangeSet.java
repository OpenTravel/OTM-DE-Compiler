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

import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;

/**
 * Container for all change items identified during the comparison of two resource parameter groups.
 */
public class ResourceParamGroupChangeSet extends ChangeSet<TLParamGroup,ResourceChangeItem> {

    /**
     * Constructor that assigns the old and new version of a parameter group that was modified.
     * 
     * @param oldParamGroup the old version of the parameter group
     * @param newParamGroup the new version of the parameter group
     */
    public ResourceParamGroupChangeSet(TLParamGroup oldParamGroup, TLParamGroup newParamGroup) {
        super( oldParamGroup, newParamGroup );
    }

    /**
     * @see org.opentravel.schemacompiler.diff.ChangeSet#getBookmarkId()
     */
    @Override
    public String getBookmarkId() {
        TLParamGroup paramGroup = (getNewVersion() != null) ? getNewVersion() : getOldVersion();
        TLResource owner = (paramGroup == null) ? null : paramGroup.getOwner();
        String paramGroupName = (paramGroup == null) ? null : paramGroup.getFacetRefName();

        return getBookmarkId( owner ) + "$pg$" + ((paramGroupName == null) ? "UNKNOWN_PARAMGRP" : paramGroupName);
    }

}

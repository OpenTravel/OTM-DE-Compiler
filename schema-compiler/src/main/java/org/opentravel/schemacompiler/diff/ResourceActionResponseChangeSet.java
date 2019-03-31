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

import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLResource;

/**
 * Container for all change items identified during the comparison of two action responses.
 */
public class ResourceActionResponseChangeSet extends ChangeSet<TLActionResponse,ResourceChangeItem> {

    /**
     * Constructor that assigns the old and new version of an action response that was modified.
     * 
     * @param oldActionResponse the old version of the action response
     * @param newActionResponse the new version of the action response
     */
    public ResourceActionResponseChangeSet(TLActionResponse oldActionResponse, TLActionResponse newActionResponse) {
        super( oldActionResponse, newActionResponse );
    }

    /**
     * @see org.opentravel.schemacompiler.diff.ChangeSet#getBookmarkId()
     */
    @Override
    public String getBookmarkId() {
        TLActionResponse response = (getNewVersion() != null) ? getNewVersion() : getOldVersion();
        TLAction action = (response.getOwner() == null) ? null : response.getOwner();
        TLResource owner = (action == null) ? null : action.getOwner();
        String actionId = (action == null) ? null : action.getActionId();
        StringBuilder responseId = new StringBuilder();

        for (Integer statusCode : response.getStatusCodes()) {
            responseId.append( statusCode );
        }

        return getBookmarkId( owner ) + "$action$" + ((actionId == null) ? "UNKNOWN_ACTION" : actionId) + "$resp$"
            + responseId.toString();
    }

}

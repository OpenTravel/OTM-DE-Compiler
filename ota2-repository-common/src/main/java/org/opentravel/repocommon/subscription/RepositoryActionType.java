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

package org.opentravel.repocommon.subscription;

import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionEventType;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Specifies a type of observable (and subscribable) action that can occur within an OTM repository.
 * 
 * @author S. Livezey
 */
public enum RepositoryActionType {

    PUBLISH(SubscriptionEventType.LIBRARY_PUBLISH),
    DELETE(SubscriptionEventType.LIBRARY_PUBLISH),
    NEW_VERSION(SubscriptionEventType.LIBRARY_NEW_VERSION),
    LOCK(SubscriptionEventType.LIBRARY_STATE_CHANGE),
    UNLOCK(SubscriptionEventType.LIBRARY_STATE_CHANGE),
    COMMIT(SubscriptionEventType.LIBRARY_COMMIT),
    PROMOTE(SubscriptionEventType.LIBRARY_STATUS_CHANGE),
    DEMOTE(SubscriptionEventType.LIBRARY_STATUS_CHANGE),
    CRC_UPDATED(SubscriptionEventType.LIBRARY_COMMIT),
    LIBRARY_MOVED(SubscriptionEventType.LIBRARY_MOVE_OR_RENAME),
    LIBRARY_RENAMED(SubscriptionEventType.LIBRARY_MOVE_OR_RENAME),
    NS_CREATED(SubscriptionEventType.NAMESPACE_ACTION),
    NS_DELETED(SubscriptionEventType.NAMESPACE_ACTION);

    private static final String RB_LOCATION = "org.opentravel.notification.notification_messages";

    private SubscriptionEventType eventType;

    /**
     * Constructor that specifies the subscription event type associated with this action type value.
     * 
     * @param eventType the associated subscription event type
     */
    private RepositoryActionType(SubscriptionEventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Returns the subscription event type associated with this action type value.
     *
     * @return RepositoryEventType
     */
    public SubscriptionEventType getEventType() {
        return eventType;
    }

    /**
     * Returns the display label for this action using the specified locale.
     * 
     * @param locale the locale to use when generating the display label
     * @return String
     */
    public String getDisplayLabel(Locale locale) {
        ResourceBundle displayLabels = ResourceBundle.getBundle( RB_LOCATION, locale );
        return displayLabels.getString( this.toString() );
    }

}

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
package org.opentravel.schemacompiler.model;

/**
 * Indicates the type of operation, based on the definition of request, response, and/or
 * notification facets for the <code>TLOperation</code> instance.
 * 
 * @author S. Livezey
 */
public enum OperationType {

    /**
     * An operation that supports an incoming request without any outbound response or notification.
     */
    ONE_WAY,

    /** An operation that supports notification output only. */
    NOTIFICATION,

    /** An operation that supports a request-response, without notification. */
    REQUEST_RESPONSE,

    /** An operation that supports a request and outgoing notification, without a response. */
    SOLICIT_NOTIFICATION,

    /** An operation that supports a request-response, as well as a notification facet. */
    REQUEST_RESPONSE_WITH_NOTIFICATION,

    /** An operation with an invalid configuration of request, response, and/or notification facets. */
    INVALID

}

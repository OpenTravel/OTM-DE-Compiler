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

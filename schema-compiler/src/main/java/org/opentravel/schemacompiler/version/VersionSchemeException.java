package org.opentravel.schemacompiler.version;

import org.opentravel.schemacompiler.util.SchemaCompilerException;

/**
 * Exception thrown when an error occurs during the processing of version information associated
 * with and/or encoded within a namespace URI.
 * 
 * @author S. Livezey
 */
public class VersionSchemeException extends SchemaCompilerException {

    /**
     * Default constructor.
     */
    public VersionSchemeException() {
    }

    /**
     * Constructs an exception with the specified message.
     * 
     * @param message
     *            the exception message
     */
    public VersionSchemeException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified underlying cause.
     * 
     * @param cause
     *            the underlying exception that caused this one
     */
    public VersionSchemeException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an exception with the specified message and underlying cause.
     * 
     * @param message
     *            the exception message
     * @param cause
     *            the underlying exception that caused this one
     */
    public VersionSchemeException(String message, Throwable cause) {
        super(message, cause);
    }

}

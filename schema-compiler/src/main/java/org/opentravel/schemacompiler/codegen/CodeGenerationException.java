package org.opentravel.schemacompiler.codegen;

import org.opentravel.schemacompiler.util.SchemaCompilerException;

/**
 * Exception thrown when errors are encountered during schema output generation.
 * 
 * @author S. Livezey
 */
public class CodeGenerationException extends SchemaCompilerException {

    /**
     * Default constructor.
     */
    public CodeGenerationException() {
    }

    /**
     * Constructs an exception with the specified message.
     * 
     * @param message
     *            the exception message
     */
    public CodeGenerationException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified underlying cause.
     * 
     * @param cause
     *            the underlying exception that caused this one
     */
    public CodeGenerationException(Throwable cause) {
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
    public CodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

}
